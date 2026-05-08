# Mizuki 后端开发计划 — Device 模块（设备展示）

> **模块编号**: MOD-05
> **包含子模块**: DeviceCategories（设备分类）、Devices（设备）
> **涉及数据表**: device_categories, devices
> **文档版本**: v1.0
> **最后更新**: 2026-05-08

---

## 1. 模块概述与业务目标

### 1.1 模块定位

Device 模块是 Mizuki 博客的**数字装备展示模块**，用于展示作者拥有的数字设备（电脑、手机、网络设备、外设等）。设备按分类组织，每个设备包含图片、灵活规格参数（JSONB）、描述和外部链接。

### 1.2 业务目标

| 目标维度 | 具体目标 |
|---------|---------|
| 分类管理 | 完整 CRUD + 排序 + 软删除 |
| 设备管理 | 完整 CRUD + 按分类分组展示 + JSONB 规格参数存储 |
| 灵活规格 | 不同设备类型的规格差异极大，通过 JSONB 灵活适配 |

### 1.3 前后端边界

- **前端负责**：设备卡片渲染、分类分组展示、规格参数格式化显示
- **后端负责**：数据 CRUD、分类排序、JSONB 查询支持

---

## 2. 核心业务逻辑实现任务

### 2.1 DeviceCategories 子模块（3 个业务功能）

#### 2.1.1 分类列表查询
- 按 sort_order ASC 排序
- 每个分类附带设备数量

#### 2.1.2 分类创建/更新
- name 全局唯一
- sort_order 默认为 0

#### 2.1.3 分类删除
- 软删除
- 若有设备关联该分类 → 拒绝删除或提供设备迁移选项

---

### 2.2 Devices 子模块（5 个业务功能）

#### 2.2.1 设备列表查询（按分类分组）
- **输入**：category_id 筛选（可选）
- **核心逻辑**：按分类分组 → 每个分类下按 sort_order ASC 排序
- **输出**：分组后的设备列表（适配前端 `Record<string, DeviceVO[]>` 结构）

#### 2.2.2 设备详情查询

#### 2.2.3 设备创建
- **核心逻辑**：
  - 必填：name + category_id
  - 可选：image + specs（JSONB）+ description + link + sort_order
  - 同一分类下 name 唯一（数据库 UNIQUE(category_id, name) 约束）
  - 若指定的 category_id 不存在 → 返回错误
- **输出**：DeviceVO

#### 2.2.4 设备更新
- 部分字段更新
- 支持变更所属分类（category_id）

#### 2.2.5 设备删除
- 软删除
- 清理设备图片物理文件

---

## 3. 业务规则实现详情

### 3.1 JSONB 规格参数规范
```json
{
  "cpu": "MediaTek MT7981B",
  "ram": "512MB DDR4",
  "storage": "128MB NAND",
  "ports": ["1x 2.5G WAN", "1x 2.5G LAN", "1x USB 3.0"],
  "wireless": {
    "wifi6": true,
    "bands": ["2.4GHz", "5GHz"]
  },
  "dimensions": {"width": 98, "height": 98, "depth": 30, "unit": "mm"}
}
```
- **后端职责**：原样存储，不做结构校验
- **前端职责**：根据设备类型决定如何渲染 specs

### 3.2 分类删除约束
- 删除分类前检查：`SELECT COUNT(*) FROM devices WHERE category_id = ? AND deleted_at IS NULL`
- 若 count > 0 → 拒绝删除，返回错误提示 "该分类下仍有设备"

### 3.3 图片管理
- 设备图片上传委托给 FileUploadService
- 删除设备时同步清理图片文件

---

## 4. Service 层开发任务

| 序号 | 任务 | 涉及类 | 说明 |
|------|------|--------|------|
| S41 | DeviceCategoryDomainService | domain/service/DeviceCategoryDomainService.java | 分类排序、删除前设备数检查 |
| S42 | DeviceCategoryAppService | application/service/DeviceCategoryAppService.java | CRUD 编排 |
| S43 | DeviceDomainService | domain/service/DeviceDomainService.java | 分类分组逻辑、JSONB specs 处理 |
| S44 | DeviceAppService | application/service/DeviceAppService.java | CRUD 编排 + 图片清理 |

---

## 5. 对象模型设计

### 5.1 Entity 领域实体

| 实体 | 关键方法 |
|------|---------|
| DeviceCategoryEntity | `canDelete()`, `hasDevices()` |
| DeviceEntity | `getSpecs()`, `setSpec()`, `belongsToCategory()` |

### 5.2 PO 持久化对象

| PO | 对应表 |
|----|--------|
| DeviceCategoryPO.java | device_categories |
| DevicePO.java | devices |

### 5.3 Repository

| Repository | 包路径 |
|------------|--------|
| DeviceCategoryRepository | domain/repository/DeviceCategoryRepository.java |
| DeviceRepository | domain/repository/DeviceRepository.java |

### 5.4 Request / VO

| 类型 | 类名 | 说明 |
|------|------|------|
| 分类请求 | DeviceCategorySaveReq | |
| 分类 VO | DeviceCategoryVO | 含设备数量 |
| 设备请求 | DeviceSaveReq | |
| 设备 VO | DeviceVO | |
| 分组 VO | DeviceGroupVO | `{category: DeviceCategoryVO, devices: DeviceVO[]}` |

---

## 6. 集成点

### 6.1 与 System 模块集成
- **文件上传**：设备图片委托 FileUploadService
- **站点配置**：featurePages.devices 控制页面开关

---

## 7. 模块依赖

### 7.1 上游依赖

| 依赖模块/组件 | 用途 | 状态 |
|-------------|------|------|
| 数据库（PostgreSQL） | device_categories/devices | 已完成 |
| IdGenerator | UUID 生成 | 系统工具 |
| FileUploadService（System） | 设备图片存储 | 依赖 MOD-06 |

### 7.2 下游被依赖

| 被依赖方 | 依赖内容 | 说明 |
|---------|---------|------|
| MOD-06 System | 设备分类/设备数量 | 站点统计 |

---

## 8. 开发优先级与阶段划分

| 阶段 | 任务 | 优先级 |
|------|------|--------|
| Phase 1 | DeviceCategories CRUD + Devices CRUD | P2 |
| Phase 2 | 图片上传/删除 + 分类分组查询优化 | P2 |

---

## 9. 关键质量指标

| 指标 | 标准 |
|------|------|
| 分类+设备联合唯一性 | DB UNIQUE(category_id, name) |
| JSONB 查询支持 | GIN 索引供高级搜索（可选） |
| 设备分组查询性能 | ≤100ms（预连接分类表） |
