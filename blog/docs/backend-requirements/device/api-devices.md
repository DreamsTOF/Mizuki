# Devices（设备）模块 API 需求文档

## 1. 获取设备列表（按分类分组返回）

### 功能说明
返回所有设备数据，按分类分组组织，供前端页面展示设备卡片列表及分类筛选。

### 请求数据
无需请求参数。

### 响应数据
- 数据结构：`Record<string, Device[]>`
- 每个键代表一个分类名称，对应的值为该分类下的设备数组。
- 每个设备对象包含以下字段：
  - `name`（string）：设备名称
  - `image`（string）：设备图片地址
  - `specs`（string）：设备规格参数
  - `description`（string）：设备描述
  - `link`（string）：设备外部链接

---

## 2. 获取单个设备详情

### 功能说明
根据设备名称和所属分类，获取单个设备的完整信息。

### 请求数据
- `category`（string）：设备所属分类名称
- `name`（string）：设备名称

### 响应数据
- 单个设备对象，包含以下字段：
  - `name`（string）：设备名称
  - `image`（string）：设备图片地址
  - `specs`（string）：设备规格参数
  - `description`（string）：设备描述
  - `link`（string）：设备外部链接

---

## 3. 创建设备

### 功能说明
创建一个新设备，并指定其所属分类。支持上传设备图片。若目标分类不存在，则自动创建该分类。

### 请求数据
- `name`（string，必填）：设备名称
- `image`（file，必填）：设备图片文件
- `specs`（string，必填）：设备规格参数
- `description`（string，必填）：设备描述
- `link`（string，必填）：设备外部链接
- `category`（string，必填）：设备所属分类名称

### 响应数据
- 创建成功的设备对象，包含以下字段：
  - `name`（string）：设备名称
  - `image`（string）：设备图片地址
  - `specs`（string）：设备规格参数
  - `description`（string）：设备描述
  - `link`（string）：设备外部链接

---

## 4. 更新设备

### 功能说明
更新已有设备的信息，支持变更设备所属分类。若变更后的目标分类不存在，则自动创建该分类。

### 请求数据
- `originalCategory`（string，必填）：设备原所属分类名称
- `originalName`（string，必填）：设备原名称
- `name`（string，可选）：新的设备名称
- `image`（file，可选）：新的设备图片文件
- `specs`（string，可选）：新的设备规格参数
- `description`（string，可选）：新的设备描述
- `link`（string，可选）：新的设备外部链接
- `category`（string，可选）：新的所属分类名称

### 响应数据
- 更新后的设备对象，包含以下字段：
  - `name`（string）：设备名称
  - `image`（string）：设备图片地址
  - `specs`（string）：设备规格参数
  - `description`（string）：设备描述
  - `link`（string）：设备外部链接

---

## 5. 删除设备

### 功能说明
删除指定分类下的指定设备。删除后，若该分类下已无任何设备，且该分类不是唯一存在的分类，则自动删除该分类。

### 请求数据
- `category`（string，必填）：设备所属分类名称
- `name`（string，必填）：设备名称

### 响应数据
- 删除操作的结果确认。

---

## 6. 获取所有设备分类

### 功能说明
返回当前系统中所有存在的设备分类名称列表，供前端展示分类筛选按钮或下拉选项。

### 请求数据
无需请求参数。

### 响应数据
- 分类名称数组：`string[]`
