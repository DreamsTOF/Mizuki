package cn.dreamtof.common.util.excel;


import cn.dreamtof.core.utils.JsonUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.enums.CellExtraTypeEnum;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellExtra;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 动态 Excel 导入工具类 (兼容普通模式与合并填充模式)
 * <p>
 * 特性：
 * 1. 默认模式：单次流式读取，速度快，适用于无合并行的普通 Excel。
 * 2. 合并模式：通过 builder.supportMerge() 开启，采用"两次读取"策略，自动填充合并单元格。
 * 3. 严格遵守 headRowNumber 和 useHeaderAsKey 配置。
 */
@Slf4j
public class ExcelDynamicImportUtil {

    /**
     * 从 Lambda 方法引用中提取字段名（内部实现，不依赖外部工具类）
     */
    @SuppressWarnings("unchecked")
    private static <T, R> String extractFieldName(Function<T, R> getter) {
        if (getter == null) {
            return null;
        }
        try {
            Method writeReplaceMethod = getter.getClass().getDeclaredMethod("writeReplace");
            writeReplaceMethod.setAccessible(true);
            SerializedLambda serializedLambda = (SerializedLambda) writeReplaceMethod.invoke(getter);
            String methodName = serializedLambda.getImplMethodName();
            
            if (methodName.startsWith("get")) {
                return decapitalize(methodName.substring(3));
            } else if (methodName.startsWith("is")) {
                return decapitalize(methodName.substring(2));
            }
        } catch (Exception e) {
            log.warn("无法从 Lambda 表达式提取字段名", e);
        }
        return null;
    }

    private static String decapitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        if (str.length() > 1 && Character.isUpperCase(str.charAt(1))) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    public static <T> List<T> read(MultipartFile file, Class<T> targetClass, Consumer<MappingBuilder<T>> mappingConfig) {
        MappingBuilder<T> builder = new MappingBuilder<>();
        mappingConfig.accept(builder);

        try {
            // 模式 1：开启了合并单元格支持 -> 走两次读取逻辑 (MultipartFile 天然支持多次获取流，无需临时文件)
            if (builder.isSupportMerge()) {
                return readWithTwoPass(() -> {
                    try {
                        return file.getInputStream();
                    } catch (IOException e) {
                        throw new RuntimeException("获取文件流失败", e);
                    }
                }, targetClass, builder);
            }
            // 模式 2：普通模式 -> 直接单次读取
            else {
                return readDataCore(file.getInputStream(), targetClass, builder, Collections.emptyList());
            }
        } catch (IOException e) {
            log.error("读取 MultipartFile 失败", e);
            throw new RuntimeException("文件流读取异常", e);
        }
    }

    public static <T> List<T> read(InputStream inputStream, Class<T> targetClass, Consumer<MappingBuilder<T>> mappingConfig) {
        MappingBuilder<T> builder = new MappingBuilder<>();
        mappingConfig.accept(builder);

        // 模式 1：普通模式 -> 直接读取，不生成临时文件，性能最优
        if (!builder.isSupportMerge()) {
            return readDataCore(inputStream, targetClass, builder, Collections.emptyList());
        }

        // 模式 2：合并模式 -> InputStream 只能读一次，必须缓存到临时文件以支持两次读取
        File tempFile = null;
        try {
            tempFile = File.createTempFile("excel_import_" + System.currentTimeMillis(), ".tmp");
            Files.copy(inputStream, tempFile.toPath());

            File finalTempFile = tempFile;
            return readWithTwoPass(() -> {
                try {
                    return new FileInputStream(finalTempFile);
                } catch (Exception e) {
                    throw new RuntimeException("读取临时文件失败", e);
                }
            }, targetClass, builder);
        } catch (IOException e) {
            throw new RuntimeException("创建临时缓存文件失败", e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * 核心方法：执行两次读取逻辑 (仅在 supportMerge=true 时调用)
     */
    private static <T> List<T> readWithTwoPass(Supplier<InputStream> streamSupplier, Class<T> targetClass, MappingBuilder<T> builder) {
        // --- 第一遍：仅读取合并单元格信息 ---
        List<CellExtra> mergeExtras = new ArrayList<>();
        EasyExcel.read(streamSupplier.get(), new AnalysisEventListener<Object>() {
            @Override
            public void invoke(Object data, AnalysisContext context) {} // 忽略数据
            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {}
            @Override
            public void extra(CellExtra extra, AnalysisContext context) {
                if (extra.getType() == CellExtraTypeEnum.MERGE) {
                    mergeExtras.add(extra);
                }
            }
        }).extraRead(CellExtraTypeEnum.MERGE).sheet().headRowNumber(builder.getHeadRowNumber()).doRead();

        // --- 第二遍：读取数据并实时填充 ---
        return readDataCore(streamSupplier.get(), targetClass, builder, mergeExtras);
    }

    /**
     * 真正的数据读取逻辑
     * @param mergeExtras 如果为空，则不执行合并填充逻辑，退化为普通读取
     */
    private static <T> List<T> readDataCore(InputStream inputStream, Class<T> targetClass, MappingBuilder<T> builder, List<CellExtra> mergeExtras) {
        List<T> resultList = new ArrayList<>();

        // 配置参数提取
        Map<Integer, String> columnMapping = builder.getMapping();
        Set<Integer> excludeRows = builder.getSpecificExcludeRows();
        String jsonFieldName = builder.getJsonFieldName();
        Map<Integer, String> manualHeaders = builder.getManualHeaders();
        boolean useHeaderAsKey = builder.isUseHeaderAsKey();
        Set<Integer> targetHeaderRows = builder.getTargetHeaderRows();
        int headRowNumber = builder.getHeadRowNumber();

        // 预处理合并信息：按"开始行"分组
        Map<Integer, List<CellExtra>> mergeStartMap;
        if (mergeExtras != null && !mergeExtras.isEmpty()) {
            mergeStartMap = mergeExtras.stream().collect(Collectors.groupingBy(CellExtra::getFirstRowIndex));
        } else {
            mergeStartMap = Collections.emptyMap();
        }

        EasyExcel.read(inputStream, new AnalysisEventListener<Map<Integer, String>>() {
            private final Map<Integer, List<String>> multiLevelHeaders = new HashMap<>();

            // 当前活跃的合并区域
            private final List<CellExtra> activeMerges = new ArrayList<>();
            // 缓存合并区域的基准值
            private final Map<String, String> mergeValues = new HashMap<>();

            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                if (!useHeaderAsKey) return;
                int currentRowIndex = context.readRowHolder().getRowIndex();
                if (!targetHeaderRows.isEmpty() && !targetHeaderRows.contains(currentRowIndex)) return;

                String lastVal = "";
                int maxCol = headMap.keySet().stream().max(Integer::compareTo).orElse(0);
                for (int i = 0; i <= maxCol; i++) {
                    String val = headMap.get(i);
                    // 简单的表头横向填充 (始终保留这个逻辑，因为表头合并很常见且处理开销极小)
                    if (StringUtils.isBlank(val)) val = lastVal;
                    else lastVal = val;

                    if (StringUtils.isNotBlank(val)) {
                        multiLevelHeaders.computeIfAbsent(i, k -> new ArrayList<>()).add(val);
                    }
                }
            }

            @Override
            public void invoke(Map<Integer, String> rowData, AnalysisContext context) {
                int rowIndex = context.readRowHolder().getRowIndex();

                // --- 仅当开启合并支持且有合并数据时，执行复杂的填充逻辑 ---
                if (!mergeStartMap.isEmpty() || !activeMerges.isEmpty()) {
                    if (mergeStartMap.containsKey(rowIndex)) {
                        activeMerges.addAll(mergeStartMap.get(rowIndex));
                    }
                    activeMerges.removeIf(extra -> extra.getLastRowIndex() < rowIndex);
                    processMergeFill(rowData, rowIndex);
                }

                if (excludeRows != null && excludeRows.contains(rowIndex)) return;
                if (rowData == null || rowData.isEmpty()) return;

                // 转换为对象
                try {
                    resultList.add(convertToObject(rowData));
                } catch (Exception e) {
                    log.error("Row {} parse error", rowIndex, e);
                    throw new RuntimeException("Excel解析错误 行号:" + rowIndex, e);
                }
            }

            private void processMergeFill(Map<Integer, String> rowData, int rowIndex) {
                if (activeMerges.isEmpty()) return;

                for (CellExtra extra : activeMerges) {
                    int firstRow = extra.getFirstRowIndex();
                    int firstCol = extra.getFirstColumnIndex();
                    int lastCol = extra.getLastColumnIndex();

                    for (int c = firstCol; c <= lastCol; c++) {
                        if (rowIndex == firstRow && c == firstCol) {
                            String val = rowData.get(c);
                            if (val != null) {
                                mergeValues.put(firstRow + "_" + firstCol, val);
                            }
                        } else {
                            String baseVal = mergeValues.get(firstRow + "_" + firstCol);
                            if (baseVal != null) {
                                rowData.put(c, baseVal);
                            }
                        }
                    }
                }
            }

            private T convertToObject(Map<Integer, String> rowData) throws Exception {
                T targetObject = targetClass.getDeclaredConstructor().newInstance();
                BeanWrapper beanWrapper = new BeanWrapperImpl(targetObject);
                beanWrapper.setAutoGrowNestedPaths(true);
                Map<String, Object> restDataMap = new LinkedHashMap<>();

                rowData.forEach((index, cellValue) -> {
                    if (StringUtils.isBlank(cellValue)) return;

                    if (columnMapping.containsKey(index)) {
                        beanWrapper.setPropertyValue(columnMapping.get(index), cellValue);
                    } else if (jsonFieldName != null) {
                        String headerName = null;
                        if (manualHeaders.containsKey(index)) {
                            headerName = manualHeaders.get(index);
                        } else if (useHeaderAsKey) {
                            List<String> levels = multiLevelHeaders.get(index);
                            if (levels != null && !levels.isEmpty()) {
                                headerName = levels.stream().distinct().collect(Collectors.joining("_"));
                            }
                        }
                        if (StringUtils.isNotBlank(headerName)) {
                            restDataMap.put(headerName, cellValue);
                        }
                    }
                });

                if (jsonFieldName != null && !restDataMap.isEmpty()) {
                    Class<?> propType = beanWrapper.getPropertyType(jsonFieldName);
                    if (String.class.equals(propType)) {
                        beanWrapper.setPropertyValue(jsonFieldName, JsonUtils.toJsonString(restDataMap));
                    } else if (propType != null && (Map.class.isAssignableFrom(propType) || Object.class.equals(propType))) {
                        beanWrapper.setPropertyValue(jsonFieldName, restDataMap);
                    }
                }
                return targetObject;
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {}
        }).sheet().headRowNumber(headRowNumber).doRead();

        return resultList;
    }

    @Getter
    public static class MappingBuilder<T> {
        private final Map<Integer, String> mapping = new HashMap<>();
        private int headRowNumber = 0;
        private final Set<Integer> specificExcludeRows = new HashSet<>();
        private String jsonFieldName = null;
        private final Map<Integer, String> manualHeaders = new HashMap<>();
        private boolean useHeaderAsKey = false;
        private final Set<Integer> targetHeaderRows = new HashSet<>();
        private boolean supportMerge = false;

        public <R> MappingBuilder<T> bind(int index, Function<T, R> func) {
            String fieldName = extractFieldName(func);
            if (fieldName != null) {
                mapping.put(index, fieldName);
            }
            return this;
        }

        public MappingBuilder<T> bindHeader(int index, String keyName) {
            this.manualHeaders.put(index, keyName);
            return this;
        }

        public <R> MappingBuilder<T> bindJson(Function<T, R> func) {
            this.jsonFieldName = extractFieldName(func);
            return this;
        }

        public MappingBuilder<T> useHeaderAsKey(int... headerRowIndices) {
            this.useHeaderAsKey = true;
            if (headerRowIndices != null && headerRowIndices.length > 0) {
                for (int i : headerRowIndices) {
                    this.targetHeaderRows.add(i);
                }
            }
            return this;
        }

        public MappingBuilder<T> headRowNumber(int rows) {
            this.headRowNumber = rows;
            return this;
        }

        public MappingBuilder<T> skip(int rows) {
            this.headRowNumber = rows;
            return this;
        }

        public MappingBuilder<T> exclude(Integer... rowIndices) {
            if (rowIndices != null) this.specificExcludeRows.addAll(Arrays.asList(rowIndices));
            return this;
        }

        /**
         * 开启合并单元格支持（会启用"两次读取"模式，略微降低性能）
         */
        public MappingBuilder<T> supportMerge() {
            this.supportMerge = true;
            return this;
        }
    }
}
