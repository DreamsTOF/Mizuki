# Albums 模块 API 设计

## 概述

本文档定义 Albums（相册）模块的 API 接口，涵盖相册的增删改查及图片管理操作。所有接口面向前端页面 `src/pages/albums.astro`、`src/pages/albums/[id]/index.astro` 和组件 `AlbumCard.astro`、`PhotoCard.astro` 提供数据支持。

---

## 1. 获取相册列表

**功能说明**
返回所有相册的列表，用于相册汇总页展示。

**请求**
- 方法：GET

**请求参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sortBy | string | 否 | 排序字段，如 `date`、`title` |
| order | string | 否 | 排序方向，`asc` 或 `desc` |

**响应数据**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 相册唯一标识（目录名） |
| title | string | 相册标题 |
| description | string | 相册描述 |
| date | string | 相册日期 |
| location | string | 拍摄地点 |
| tags | string[] | 标签列表 |
| coverUrl | string | 封面图片访问路径 |
| photoCount | number | 相册内图片数量 |

---

## 2. 获取单个相册详情

**功能说明**
返回指定相册的完整信息及图片列表，用于相册详情页展示。

**请求**
- 方法：GET

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | string | 是 | 相册唯一标识 |

**响应数据**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 相册唯一标识 |
| title | string | 相册标题 |
| description | string | 相册描述 |
| date | string | 相册日期 |
| location | string | 拍摄地点 |
| tags | string[] | 标签列表 |
| layout | "masonry" \| "grid" | 布局方式 |
| columns | number | 列数 |
| coverUrl | string | 封面图片访问路径 |
| photos | Photo[] | 图片列表 |

**Photo 结构**
| 字段 | 类型 | 说明 |
|------|------|------|
| filename | string | 图片文件名 |
| url | string | 图片访问路径 |
| width | number | 图片宽度 |
| height | number | 图片高度 |
| size | number | 文件大小（字节） |
| mimeType | string | MIME 类型 |
| isCover | boolean | 是否为封面 |

---

## 3. 创建相册

**功能说明**
创建新相册目录并写入元数据，可选同时上传封面图片。

**请求**
- 方法：POST
- Content-Type: multipart/form-data

**请求参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string | 是 | 相册标题 |
| description | string | 否 | 相册描述 |
| date | string | 否 | 相册日期 |
| location | string | 否 | 拍摄地点 |
| tags | string[] | 否 | 标签列表 |
| layout | string | 否 | 布局方式，默认 `masonry` |
| columns | number | 否 | 列数，默认 `3` |
| cover | file | 否 | 封面图片文件 |

**响应数据**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 新创建相册的标识 |
| title | string | 相册标题 |
| date | string | 相册日期 |
| coverUrl | string | 封面图片路径（如有） |

---

## 4. 更新相册信息

**功能说明**
更新已有相册的元数据或封面图片。

**请求**
- 方法：PUT / PATCH
- Content-Type: multipart/form-data（含文件时）或 application/json

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | string | 是 | 相册唯一标识 |

**请求参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string | 否 | 相册标题 |
| description | string | 否 | 相册描述 |
| date | string | 否 | 相册日期 |
| location | string | 否 | 拍摄地点 |
| tags | string[] | 否 | 标签列表 |
| layout | string | 否 | 布局方式 |
| columns | number | 否 | 列数 |
| cover | file | 否 | 新封面图片文件 |

**响应数据**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 相册标识 |
| title | string | 更新后的标题 |
| date | string | 更新后的日期 |
| coverUrl | string | 更新后的封面路径 |

---

## 5. 删除相册

**功能说明**
删除整个相册目录及其内部所有文件。

**请求**
- 方法：DELETE

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | string | 是 | 相册唯一标识 |

**响应数据**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 已删除相册的标识 |
| deleted | boolean | 是否删除成功 |

---

## 6. 上传相册图片（批量上传）

**功能说明**
向指定相册上传一张或多张图片。

**请求**
- 方法：POST
- Content-Type: multipart/form-data

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | string | 是 | 相册唯一标识 |

**请求参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| photos | file[] | 是 | 一个或多个图片文件 |

**响应数据**
| 字段 | 类型 | 说明 |
|------|------|------|
| uploaded | Photo[] | 上传成功的图片信息列表 |
| failed | object[] | 上传失败的文件及原因（可选） |

**Photo 结构**
| 字段 | 类型 | 说明 |
|------|------|------|
| filename | string | 图片文件名 |
| url | string | 图片访问路径 |
| width | number | 图片宽度 |
| height | number | 图片高度 |
| size | number | 文件大小 |
| mimeType | string | MIME 类型 |

---

## 7. 设置相册封面

**功能说明**
将相册内已有图片设为封面，或直接上传新封面。

**请求**
- 方法：PUT / PATCH
- Content-Type: multipart/form-data（上传新封面时）或 application/json

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | string | 是 | 相册唯一标识 |

**请求参数（二选一）**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| filename | string | 条件 | 相册内已有图片的文件名 |
| cover | file | 条件 | 新上传的封面图片文件 |

**响应数据**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 相册标识 |
| coverUrl | string | 新封面图片路径 |

---

## 8. 删除相册中的单张图片

**功能说明**
删除相册内指定的单张图片。若删除的是当前封面，后端需处理封面缺失情况。

**请求**
- 方法：DELETE

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | string | 是 | 相册唯一标识 |
| filename | string | 是 | 要删除的图片文件名 |

**响应数据**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | string | 相册标识 |
| filename | string | 已删除的图片文件名 |
| deleted | boolean | 是否删除成功 |
| coverChanged | boolean | 封面是否因删除而变更 |
| newCoverUrl | string | 变更后的新封面路径（如适用） |
