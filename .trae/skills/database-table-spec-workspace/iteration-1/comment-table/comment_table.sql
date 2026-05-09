-- 评论表建表语句

CREATE TABLE `comment` (
    `id` UUID DEFAULT uuidv7() COMMENT '主键ID',
    
    -- 评论基本信息
    `user_id` UUID DEFAULT NULL COMMENT '用户ID',
    `product_id` UUID DEFAULT NULL COMMENT '商品ID',
    `content` TEXT DEFAULT NULL COMMENT '评论内容',
    `rating` TINYINT(4) DEFAULT 5 COMMENT '评分：1-5星',
    `images` JSON DEFAULT NULL COMMENT '评论图片JSON数组',
    `status` TINYINT(4) DEFAULT 1 COMMENT '评论状态：0-隐藏，1-显示，2-删除',
    `reply_count` INT DEFAULT 0 COMMENT '回复数量',
    `like_count` INT DEFAULT 0 COMMENT '点赞数量',
    
    -- 必备字段
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` UUID DEFAULT NULL COMMENT '创建人ID',
    `updated_by` UUID DEFAULT NULL COMMENT '更新人ID',
    `deleted` INT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    `version` INT DEFAULT 0 COMMENT '版本号，用于乐观锁',
    
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_product_id_create_time` (`product_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评论表';

-- 字段清单说明
-- id: 主键，使用 UUID 类型
-- user_id: 用户ID，UUID 类型，外键字段
-- product_id: 商品ID，UUID 类型，外键字段
-- content: 评论内容，TEXT 类型
-- rating: 评分，TINYINT(4)，1-5星
-- images: 评论图片，JSON 类型，存储图片URL数组
-- status: 评论状态，TINYINT(4)，枚举值已注释
-- reply_count: 回复数量，INT 类型
-- like_count: 点赞数量，INT 类型

-- 索引创建建议
-- 1. idx_user_id: 普通索引，外键字段
-- 2. idx_product_id: 普通索引，外键字段
-- 3. idx_status: 普通索引，常用查询条件
-- 4. idx_create_time: 普通索引，按时间查询
-- 5. idx_product_id_create_time: 组合索引，查询商品评论列表
