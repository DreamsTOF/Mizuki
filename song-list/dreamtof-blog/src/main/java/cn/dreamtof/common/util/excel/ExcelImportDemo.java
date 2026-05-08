//package cn.dreamtof.common.util.excel;
//
//import lombok.Data;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.InputStream;
//import java.util.List;
//import java.util.Map;
//
///**
// * ExcelDynamicImportUtil 使用示例
// * * 包含三种场景：
// * 1. 基础映射
// * 2. 动态列 (混合模式)
// * 3. 多级表头处理
// */
//public class ExcelImportDemo {
//
//    // ==========================================
//    // 场景 1: 基础实体类 (用于简单映射)
//    // ==========================================
//    @Data
//    public static class SimpleUserDTO {
//        private String name;
//        private Integer age;
//        private String email;
//    }
//
//    /**
//     * 场景 1: 基础导入
//     * Excel 结构:
//     * [0]姓名 | [1]年龄 | [2]邮箱
//     * 张三   | 18     | zs@test.com
//     */
//    public void importSimpleCase(MultipartFile file) {
//        List<SimpleUserDTO> list = ExcelDynamicImportUtil.read(file, SimpleUserDTO.class, config -> {
//            // 1. 设置数据开始行 (跳过第0行表头，从第1行开始读数据)
//            config.headRowNumber(1);
//
//            // 2. 绑定列索引到字段 (利用 Lambda 获取字段名，避免手写字符串)
//            config.bind(0, SimpleUserDTO::getName);
//            config.bind(1, SimpleUserDTO::getAge);
//            config.bind(2, SimpleUserDTO::getEmail);
//        });
//
//        System.out.println("读取条数: " + list.size());
//    }
//
//
//    // ==========================================
//    // 场景 2: 动态属性实体类 (核心功能)
//    // ==========================================
//    @Data
//    public static class DynamicProductDTO {
//        // 固定字段
//        private String productCode;
//        private String productName;
//
//        // 动态字段：Excel 中除了 Code 和 Name 之外的列，都会丢进这里
//        // 工具类支持 String (存JSON字符串) 或 Map (存对象)
//        private Map<String, Object> extraData;
//    }
//
//    /**
//     * 场景 2: 动态列导入 (最常用的场景)
//     * Excel 结构:
//     * 行0: [0]编码 | [1]名称 | [2]2023销量 | [3]2024销量 | [4]备注 ... (后续列不确定)
//     * 行1: P001   | 手机   | 100        | 200        | 热销 ...
//     */
//    public void importDynamicCase(InputStream inputStream) {
//        List<DynamicProductDTO> list = ExcelDynamicImportUtil.read(inputStream, DynamicProductDTO.class, config -> {
//            // 1. 设定数据从第 1 行开始 (行号从0开始，所以跳过行0)
//            config.headRowNumber(1);
//
//            // 2. 绑定固定字段
//            config.bind(0, DynamicProductDTO::getProductCode);
//            config.bind(1, DynamicProductDTO::getProductName);
//
//            // 3. 【关键】绑定 JSON/Map 接收容器字段
//            config.bindJson(DynamicProductDTO::getExtraData);
//
//            // 4. 【关键】告诉工具去读取第 0 行作为动态字段的 Key
//            // 如果不配置这个，进入 extraData 的 key 将是列索引 "2", "3", "4"
//            // 配置后，key 将是 "2023销量", "2024销量", "备注"
//            config.useHeaderAsKey(0);
//        });
//
//        // 输出演示
//        list.forEach(item -> {
//            System.out.println("商品: " + item.getProductName());
//            System.out.println("2023销量: " + item.getExtraData().get("2023销量"));
//        });
//    }
//
//
//    // ==========================================
//    // 场景 3: 复杂多级表头
//    // ==========================================
//    @Data
//    public static class ComplexReportDTO {
//        private String deptName;
//        private String extraJson; // 这次我们用 String 接收，工具类会自动转为 JSON 字符串
//    }
//
//    /**
//     * 场景 3: 多级表头合并 + 排除特定行
//     * Excel 结构:
//     * 行0: 部门 |     绩效指标 (合并单元格)     |
//     * 行1: 部门 |  一月   |  二月   |  三月   |
//     * 行2: (一些无关的说明文字，需要跳过)
//     * 行3: 研发 |   80    |   90   |   85    |
//     *
//     * 期望逻辑:
//     * - "部门" 列绑定到 deptName
//     * - 其他列根据 [行0]_[行1] 组合成 Key (如 "绩效指标_一月") 存入 extraJson
//     * - 跳过 行2
//     * - 数据从 行3 开始
//     */
//    public void importMultiHeaderCase(InputStream inputStream) {
//        List<ComplexReportDTO> list = ExcelDynamicImportUtil.read(inputStream, ComplexReportDTO.class, config -> {
//            // 1. 数据真正开始的行号 (跳过 0,1,2 共三行)
//            config.headRowNumber(3);
//
//            // 2. 绑定固定列
//            config.bind(0, ComplexReportDTO::getDeptName);
//
//            // 3. 绑定 JSON 字段 (类型是 String)
//            config.bindJson(ComplexReportDTO::getExtraJson);
//
//            // 4. 指定使用 行0 和 行1 共同作为 Header Key
//            // 工具类会自动处理合并单元格填充，并用 "_" 连接多级表头
//            // 结果 Key 示例: "绩效指标_一月", "绩效指标_二月"
//            config.useHeaderAsKey(0, 1);
//
//            // 5. (可选) 如果数据区域中间夹杂了无关的说明行，可以强制排除
//            // 例如：虽然主要数据从行3开始，但如果行10是空行或分割线，可以 exclude(10)
//            config.exclude(2);
//
//            // 6. (可选) 手动修正某些列的 Key
//            // 假设第 5 列表头写的很乱，你想强制命名为 "total_score"
//            config.bindHeader(5, "total_score");
//        });
//
//        // 打印结果
//        list.forEach(dto -> {
//            System.out.println("JSON数据: " + dto.getExtraJson());
//        });
//    }
//}
