package cn.dreamtof.common.util.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: lzy & Gemini
 * @Description: 高级导出工具类
 * 1. 支持 EasyExcel 多级表头原生合并
 * 2. 支持多个 List 字段动态横向平铺 (第i次业务名)
 * 3. 支持 @ExcelDict 字典转换与日期格式化缓存
 */
@Slf4j
public class ExportUtil {

    private ExportUtil() {}

    /**
     * 动态列配置类：用于定义 List 字段如何展开
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicHeaderColumn {
        private String businessName; // 业务名词，如 "检查记录"、"专家建议"
        private Map<String, String> fieldToHeaderMap; // 子对象属性名 -> Excel表头显示名
    }

    private static class ExcelFieldMeta {
        Field field;
        String[] headerPath; // 多级表头路径
        Map<String, String> dictMap;
        DateTimeFormatter dateFormatter;
    }

    private static final Map<Class<?>, List<ExcelFieldMeta>> META_CACHE = new ConcurrentHashMap<>();

    /**
     * 【核心方法】复杂动态导出：支持固定列 + 多个 List 字段动态横向平铺
     */
    public static void exportComplexDynamicExcel(HttpServletResponse response, List<?> dataList, Class<?> clazz,
                                                 Map<String, DynamicHeaderColumn> dynamicConfig, String fileName) {
        try {
            setExcelResponseHeader(response, fileName);

            // 1. 获取固定列元数据
            List<ExcelFieldMeta> fixedMetas = getCachedMetas(clazz);

            // 2. 统计各动态 List 的最大长度（决定导出列宽度）
            Map<String, Integer> maxSizes = new HashMap<>();
            if (dataList != null) {
                for (Object obj : dataList) {
                    for (String fieldName : dynamicConfig.keySet()) {
                        if (checkExpandEnable(obj, fieldName)) {
                            List<?> list = (List<?>) getFieldValue(obj, fieldName);
                            if (list != null) {
                                maxSizes.put(fieldName, Math.max(maxSizes.getOrDefault(fieldName, 0), list.size()));
                            }
                        }
                    }
                }
            }

            // 3. 构建动态全表头 (List<List<String>>)
            List<List<String>> finalHead = new ArrayList<>();
            // A. 固定表头部分
            fixedMetas.forEach(meta -> finalHead.add(new ArrayList<>(Arrays.asList(meta.headerPath))));

            // B. 动态平铺表头部分
            dynamicConfig.forEach((fieldName, config) -> {
                int size = maxSizes.getOrDefault(fieldName, 0);
                for (int i = 1; i <= size; i++) {
                    String subTitle = "第" + i + "次" + config.getBusinessName();
                    config.getFieldToHeaderMap().forEach((attr, header) -> {
                        // 🌟 核心修改：去掉最外层的 "xxx明细"，直接保留两级表头
                        finalHead.add(new ArrayList<>(Arrays.asList(subTitle, header)));
                    });
                }
            });

            // 4. 填充数据行
            List<List<Object>> rows = new ArrayList<>();
            if (dataList != null) {
                for (Object obj : dataList) {
                    List<Object> row = new ArrayList<>();
                    // A. 填充固定列数据
                    for (ExcelFieldMeta meta : fixedMetas) {
                        row.add(formatValue(getFieldValue(obj, meta.field), meta));
                    }
                    // B. 填充动态列数据
                    dynamicConfig.forEach((fieldName, config) -> {
                        List<?> list = (List<?>) getFieldValue(obj, fieldName);
                        int maxSize = maxSizes.getOrDefault(fieldName, 0);
                        for (int i = 0; i < maxSize; i++) {
                            Object subObj = (list != null && i < list.size()) ? list.get(i) : null;
                            config.getFieldToHeaderMap().keySet().forEach(attrName -> {
                                row.add(subObj == null ? "" : getSubFieldValue(subObj, attrName));
                            });
                        }
                    });
                    rows.add(row);
                }
            }

            // 5. 写入流
            EasyExcel.write(response.getOutputStream())
                    .head(finalHead)
                    // 自动适配列宽
                    .registerWriteHandler(new com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy())
                    .sheet("数据明细")
                    .doWrite(rows);

        } catch (Exception e) {
            log.error("动态Excel导出异常", e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    /**
     * 标准 Excel 导出 (支持多级表头)
     */
    public static void exportExcel(HttpServletResponse response, List<?> dataList, Class<?> clazz, String fileName) {
        try {
            setExcelResponseHeader(response, fileName);
            List<ExcelFieldMeta> metas = getCachedMetas(clazz);
            List<List<Object>> processedData = new ArrayList<>();
            if (dataList != null) {
                for (Object obj : dataList) {
                    List<Object> row = new ArrayList<>();
                    for (ExcelFieldMeta meta : metas) {
                        row.add(formatValue(getFieldValue(obj, meta.field), meta));
                    }
                    processedData.add(row);
                }
            }
            EasyExcel.write(response.getOutputStream()).head(clazz).sheet("Sheet1").doWrite(processedData);
        } catch (IOException e) {
            throw new RuntimeException("Excel导出失败", e);
        }
    }

    /**
     * CSV 导出
     */
    public static void exportCsv(HttpServletResponse response, List<?> dataList, Class<?> clazz, String fileName) {
        try {
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".csv");

            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF'); // BOM

            List<ExcelFieldMeta> metas = getCachedMetas(clazz);
            // 写入表头
            for (int i = 0; i < metas.size(); i++) {
                if (i > 0) writer.write(',');
                String[] path = metas.get(i).headerPath;
                writer.write(escapeCsv(path[path.length - 1]));
            }
            writer.write('\n');

            // 写入数据
            if (dataList != null) {
                for (Object obj : dataList) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < metas.size(); i++) {
                        if (i > 0) sb.append(',');
                        sb.append(escapeCsv(formatValue(getFieldValue(obj, metas.get(i).field), metas.get(i))));
                    }
                    writer.println(sb.toString());
                }
            }
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("CSV导出失败", e);
        }
    }

    // --- 内部辅助方法 ---

    private static void setExcelResponseHeader(HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".xlsx");
    }

    private static boolean checkExpandEnable(Object obj, String fieldName) {
        try {
            // 约定 flag 字段名为: 字段名 + "Enable" (如 timeLogsEnable)
            Field f = obj.getClass().getDeclaredField(fieldName + "Enable");
            f.setAccessible(true);
            return Boolean.TRUE.equals(f.get(obj));
        } catch (NoSuchFieldException e) {
            return true; // 没有 flag 默认展开
        } catch (Exception e) {
            return false;
        }
    }

    private static Object getFieldValue(Object obj, Object fieldOrName) {
        try {
            if (fieldOrName instanceof Field) {
                return ((Field) fieldOrName).get(obj);
            } else {
                Field f = obj.getClass().getDeclaredField((String) fieldOrName);
                f.setAccessible(true);
                return f.get(obj);
            }
        } catch (Exception e) { return null; }
    }

    private static Object getSubFieldValue(Object obj, String fieldName) {
        try {
            Object val;
            if (obj instanceof Map) {
                val = ((Map<?, ?>) obj).get(fieldName);
            } else {
                Field f = obj.getClass().getDeclaredField(fieldName);
                f.setAccessible(true);
                val = f.get(obj);
            }

            // 🌟 核心修复：遇到时间类型直接转为格式化字符串，彻底解决 Excel 自动列宽不够导致显示 ###### 的问题
            if (val instanceof java.time.LocalDateTime) {
                return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format((java.time.LocalDateTime) val);
            }
            if (val instanceof Date) {
                return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) val);
            }

            return val;
        } catch (Exception e) {
            return "";
        }
    }
    private static String formatValue(Object value, ExcelFieldMeta meta) {
        if (value == null) return "";
        if (meta.dictMap != null) return meta.dictMap.getOrDefault(value.toString(), value.toString());
        if (meta.dateFormatter != null) {
            if (value instanceof TemporalAccessor) return meta.dateFormatter.format((TemporalAccessor) value);
            if (value instanceof Date) return meta.dateFormatter.format(((Date) value).toInstant().atZone(java.time.ZoneId.systemDefault()));
        }
        return value.toString();
    }

    private static List<ExcelFieldMeta> getCachedMetas(Class<?> clazz) {
        return META_CACHE.computeIfAbsent(clazz, k -> {
            List<ExcelFieldMeta> metas = new ArrayList<>();
            Class<?> current = k;
            while (current != null && current != Object.class) {
                for (Field field : current.getDeclaredFields()) {
                    if (field.isAnnotationPresent(ExcelProperty.class)) {
                        field.setAccessible(true);
                        ExcelFieldMeta meta = new ExcelFieldMeta();
                        meta.field = field;
                        meta.headerPath = field.getAnnotation(ExcelProperty.class).value();

                        if (field.isAnnotationPresent(ExcelDict.class)) {
                            ExcelDict dict = field.getAnnotation(ExcelDict.class);
                            if (!dict.readConverterExp().isEmpty()) meta.dictMap = parseDict(dict.readConverterExp());
                            if (!dict.format().isEmpty()) meta.dateFormatter = DateTimeFormatter.ofPattern(dict.format());
                        }
                        metas.add(meta);
                    }
                }
                current = current.getSuperclass();
            }
            metas.sort((m1, m2) -> {
                int i1 = m1.field.getAnnotation(ExcelProperty.class).index();
                int i2 = m2.field.getAnnotation(ExcelProperty.class).index();
                return Integer.compare(i1 == -1 ? Integer.MAX_VALUE : i1, i2 == -1 ? Integer.MAX_VALUE : i2);
            });
            return metas;
        });
    }

    private static Map<String, String> parseDict(String exp) {
        Map<String, String> map = new HashMap<>();
        for (String pair : exp.split("[,;]")) {
            String[] kv = pair.split("[:=]");
            if (kv.length == 2) map.put(kv[0].trim(), kv[1].trim());
        }
        return map;
    }

    private static String escapeCsv(String value) {
        if (value == null || value.isEmpty()) return "";
        if (value.contains(",") || value.contains("\n") || value.contains("\"") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * 动态导出 CSV (支持动态列)
     * @param headers 表头列表
     * @param dataList 数据行列表 (每行是一个 List<String>)
     */
    public static void exportDynamicCsv(HttpServletResponse response, List<String> headers, List<List<String>> dataList, String fileName) {
        try {
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setContentType("text/csv;charset=UTF-8");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName + ".csv");

            PrintWriter writer = response.getWriter();
            writer.write('\uFEFF'); // 写入 BOM 防止乱码

            // 1. 写入表头
            if (headers != null && !headers.isEmpty()) {
                for (int i = 0; i < headers.size(); i++) {
                    if (i > 0) writer.write(',');
                    writer.write(escapeCsv(headers.get(i)));
                }
                writer.write('\n');
            }

            // 2. 写入数据
            if (dataList != null && !dataList.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (List<String> row : dataList) {
                    if (row == null) continue;
                    sb.setLength(0);
                    for (int i = 0; i < row.size(); i++) {
                        if (i > 0) sb.append(',');
                        // row.get(i) 已经是处理好的字符串，直接转义即可
                        sb.append(escapeCsv(row.get(i)));
                    }
                    sb.append('\n');
                    writer.write(sb.toString());
                }
            }
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("CSV动态导出失败: " + e.getMessage(), e);
        }
    }
}
