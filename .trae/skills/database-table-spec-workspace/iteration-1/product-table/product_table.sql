-- 商品表建表语句

CREATE TABLE `product` (
    `id` UUID DEFAULT uuidv7() COMMENT '主键ID',
    
    -- 商品基本信息
    `product_name` VARCHAR(100) DEFAULT NULL COMMENT '商品名称',
    `product_code` VARCHAR(50) DEFAULT NULL COMMENT '商品编码',
    `category_id` UUID DEFAULT NULL COMMENT '分类ID',
    `price` DECIMAL(18,2) DEFAULT 0 COMMENT '商品价格',
    `stock` INT DEFAULT 0 COMMENT '库存数量',
    `status` TINYINT(4) DEFAULT 1 COMMENT '商品状态：0-下架，1-上架，2-售罄',
    `description` TEXT DEFAULT NULL COMMENT '商品描述',
    `image_url` VARCHAR(255) DEFAULT NULL COMMENT '商品图片URL',
    `specification` JSON DEFAULT NULL COMMENT '商品规格JSON',
    
    -- 必备字段
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` UUID DEFAULT NULL COMMENT '创建人ID',
    `updated_by` UUID DEFAULT NULL COMMENT '更新人ID',
    `deleted` INT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    `version` INT DEFAULT 0 COMMENT '版本号，用于乐观锁',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_code` (`product_code`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品信息表';

-- 字段清单说明
-- id: 主键，使用 UUID 类型，默认值为 uuidv7()
-- product_name: 商品名称，VARCHAR(100)
-- product_code: 商品编码，VARCHAR(50)，唯一索引
-- category_id: 分类ID，UUID 类型，外键字段
-- price: 商品价格，DECIMAL(18,2)，精确到分
-- stock: 库存数量，INT 类型
-- status: 商品状态，TINYINT(4)，枚举值已注释说明
-- description: 商品描述，TEXT 类型
-- image_url: 商品图片URL，VARCHAR(255)
-- specification: 商品规格，JSON 类型，存储复杂规格数据

-- 索引创建建议
-- 1. uk_product_code: 唯一索引，确保商品编码唯一
-- 2. idx_category_id: 普通索引，外键字段必须创建索引
-- 3. idx_status: 普通索引，常用查询条件
-- 4. idx_create_time: 普通索引，按时间查询
