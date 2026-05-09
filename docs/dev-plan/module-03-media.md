# Mizuki 后端开发计划 — Media 模块（相册 + 番剧 + 音乐）

> **模块编号**: MOD-03
> **包含子模块**: Albums（相册）、Anime（番剧）、Music（音乐播放列表）
> **涉及数据表**: albums, album_photos, anime, music_playlists, music_tracks
> **文档版本**: v1.0
> **最后更新**: 2026-05-08

---

## 1. 模块概述与业务目标

### 1.1 模块定位

Media 模块是 Mizuki 博客的**多媒体内容管理引擎**，负责三块独立但主题统一的媒体内容：相册（图片集）、番剧追踪（动漫观看记录）、音乐播放列表。该模块服务于博客的"个人兴趣展示"定位，为访客提供丰富的多媒体浏览体验。

### 1.2 业务目标

| 目标维度 | 具体目标 |
|---------|---------|
| 相册管理 | 完整 CRUD + 图片批量上传/删除 + 封面设置 + 瀑布流/网格布局控制 |
| 番剧追踪 | CRUD + 观看状态管理 + 评分 + 进度追踪 + 类型筛选 |
| 音乐播放 | 播放列表 CRUD + 曲目管理 + 排序控制 + 歌词存储 |
| 布局配置 | 相册支持 masonry（瀑布流）和 grid（网格）两种布局 + 列数自定义 |

### 1.3 前后端边界

- **前端负责**：相册瀑布流/网格渲染、图片懒加载、番剧封面展示、音乐播放器 UI
- **后端负责**：数据 CRUD、图片文件管理、播放列表/曲目排序

---

## 2. 核心业务逻辑实现任务

### 2.1 Albums 子模块（8 个业务功能）

#### 2.1.1 相册列表查询
- **输入**：分页、按 album_date 排序、按标签筛选（JSONB @>）
- **核心逻辑**：关联 album_photos 表预计算封面图（has_cover=TRUE 的记录）和图片总数
- **输出**：AlbumVO 列表（含封面 URL + 图片数量）

#### 2.1.2 相册详情查询（含图片列表）
- **核心逻辑**：查相册主表 + 查 album_photos（按 created_at 或 filename 排序）
- **输出**：AlbumDetailVO（含 photos 数组：url/width/height/size/mimeType/hasCover）

#### 2.1.3 相册创建
- **核心逻辑**：
  - album_key 唯一标识（目录名），支持自动生成（基于 title → slug）
  - layout 枚举校验（masonry/grid），默认 masonry
  - columns 范围 1-6，默认 3
  - tags JSONB 数组

#### 2.1.4 相册更新
- 部分字段更新 + 封面图片替换

#### 2.1.5 相册删除
- 软删除 + 级联软删除 album_photos + 清理物理图片文件

#### 2.1.6 图片上传（批量）
- 校验格式（jpg/png/webp）→ 校验大小 → 存储 → 解析宽高 → 写 album_photos 表
- 同一相册下 filename 唯一

#### 2.1.7 设置相册封面
- 将指定 photo 的 `has_cover` 设为 TRUE → 同时将同相册下其他 photo 的 `has_cover` 置 FALSE

#### 2.1.8 删除相册图片
- 软删除 album_photos → 物理删除文件 → 若删除的是封面则自动指定下一张为封面

---

### 2.2 Anime 子模块（5 个业务功能）

#### 2.2.1 番剧列表查询
- **输入**：分页、状态筛选（watching/completed/planned）、年份筛选、genre 筛选（JSONB @>）、排序选择（rating/sort_order/created_at）
- **核心逻辑**：genre JSONB 支持 @> 操作符精确匹配
- **输出**：AnimeVO 列表

#### 2.2.2 番剧详情查询

#### 2.2.3 番剧创建
- status 枚举校验（watching/completed/planned）
- rating 范围 0-10，支持小数一位
- progress ≤ total_episodes 校验
- genre JSONB 数组，如 `["冒险","奇幻","战斗"]`

#### 2.2.4 番剧更新
- 部分字段更新
- 状态变为 completed 时自动填充 end_date（若为空）
- progress 更新时禁止超过 total_episodes

#### 2.2.5 番剧删除
- 软删除

---

### 2.3 Music 子模块（6 个业务功能）

#### 2.3.1 播放列表查询
- 按 sort_order ASC 排序，仅返回 has_enabled=TRUE
- 预关联曲目数量

#### 2.3.2 播放列表详情（含曲目列表）
- 查 music_tracks（按 sort_order ASC）

#### 2.3.3 播放列表创建
- 必填：name
- 可选：description/cover_image/sort_order

#### 2.3.4 播放列表更新 + 启用/禁用切换

#### 2.3.5 播放列表删除
- 软删除 + 级联删除曲目

#### 2.3.6 曲目管理（CRUD）
- 创建：必填 playlist_id + title；可选 artist/album/cover_image/audio_url/external_url/lyrics/duration/sort_order
- 更新/删除：基于 id
- 支持批量排序调整

---

## 3. 业务规则实现详情

### 3.1 相册封面管理规则
- 每个相册有且仅有一张封面（has_cover=TRUE）
- 设置新封面时：事务中先清空同相册所有封面的 has_cover → 再设置目标为 TRUE
- 删除封面图片后：自动将同相册第一张图片设为封面（若还有图片）
- 相册无图片时：cover_image 字段可存储手动上传的独立封面图

### 3.2 番剧进度约束
```
progress ≤ total_episodes
若 progress == total_episodes → 建议 status = "completed"
若 status = "completed" && progress < total_episodes → 自动更新 progress = total_episodes
```

### 3.3 番剧评分规则
- 范围 0.0 ~ 10.0，DECIMAL(3,1)
- 创建时默认 0
- 业务层校验：0 ≤ rating ≤ 10

### 3.4 播放列表排序
- sort_order 为整数，越小越靠前
- 同一位置冲突时按 created_at 排序

### 3.5 音乐曲目存储策略
- `audio_url`：本地存储的音乐文件路径
- `external_url`：外部音乐链接（如网易云、QQ音乐）
- 两者至少提供一个，可以同时存在
- `lyrics`：LRC 格式歌词原文（若有）

---

## 4. Service 层开发任务

| 序号 | 任务 | 涉及类 | 说明 |
|------|------|--------|------|
| S26 | AlbumDomainService | domain/service/AlbumDomainService.java | 相册封面切换、图片数量维护、布局校验 |
| S27 | AlbumAppService | application/service/AlbumAppService.java | CRUD 编排 + 图片文件清理 |
| S28 | AlbumPhotoService | domain/service/AlbumPhotoService.java | 图片上传/删除 + 尺寸解析 + 封面自动指定 |
| S29 | AnimeDomainService | domain/service/AnimeDomainService.java | 进度约束校验、状态自动流转、评分范围校验 |
| S30 | AnimeAppService | application/service/AnimeAppService.java | CRUD 编排 |
| S31 | MusicPlaylistDomainService | domain/service/MusicPlaylistDomainService.java | 排序维护、启用/禁用切换 |
| S32 | MusicPlaylistAppService | application/service/MusicPlaylistAppService.java | CRUD 编排 |
| S33 | MusicTrackDomainService | domain/service/MusicTrackDomainService.java | 曲目排序、歌词存储 |
| S34 | MusicTrackAppService | application/service/MusicTrackAppService.java | CRUD 编排 |

---

## 5. 对象模型设计

### 5.1 Entity 领域实体

| 实体 | 关键方法 |
|------|---------|
| AlbumEntity | `setCoverPhoto()`, `getPhotoCount()`, `validateLayout()` |
| AlbumPhotoEntity | `isCover()`, `setAsCover()` |
| AnimeEntity | `updateProgress()`, `completeWatching()`, `validateRating()` |
| MusicPlaylistEntity | `enable()`, `disable()` |
| MusicTrackEntity | `validateAudioSource()` |

### 5.2 PO 持久化对象

| PO | 对应表 |
|----|--------|
| AlbumPO.java | albums |
| AlbumPhotoPO.java | album_photos |
| AnimePO.java | anime |
| MusicPlaylistPO.java | music_playlists |
| MusicTrackPO.java | music_tracks |

### 5.3 Repository

| Repository | 包路径 |
|------------|--------|
| AlbumRepository | domain/repository/AlbumRepository.java |
| AlbumPhotoRepository | domain/repository/AlbumPhotoRepository.java |
| AnimeRepository | domain/repository/AnimeRepository.java |
| MusicPlaylistRepository | domain/repository/MusicPlaylistRepository.java |
| MusicTrackRepository | domain/repository/MusicTrackRepository.java |

### 5.4 Request / VO

| 类型 | 类名 | 说明 |
|------|------|------|
| 相册请求 | AlbumPageReq, AlbumSaveReq | |
| 相册 VO | AlbumVO, AlbumDetailVO, AlbumPhotoVO | |
| 番剧请求 | AnimePageReq, AnimeSaveReq | |
| 番剧 VO | AnimeVO | |
| 音乐请求 | PlaylistSaveReq, MusicTrackSaveReq | |
| 音乐 VO | MusicPlaylistVO, MusicTrackVO | |

---

## 6. 集成点

### 6.1 与 System 模块集成
- **文件上传**：相册图片上传委托给 FileUploadService
- **站点配置**：featurePages 控制番剧/相册/音乐页面开关

### 6.2 与 Content 模块集成
- 相册与日记共享图片存储路径规范

---

## 7. 模块依赖

### 7.1 上游依赖

| 依赖模块/组件 | 用途 | 状态 |
|-------------|------|------|
| 数据库（PostgreSQL） | albums/album_photos/anime/music_playlists/music_tracks | 已完成 |
| IdGenerator | UUID 生成 | 系统工具 |
| FileUploadService（System） | 相册图片/封面存储 | 依赖 MOD-06 |

### 7.2 下游被依赖

| 被依赖方 | 依赖内容 | 说明 |
|---------|---------|------|
| MOD-06 System | 相册/番剧数据 | 站点统计聚合 |

---

## 8. 开发优先级与阶段划分

| 阶段 | 任务 | 优先级 |
|------|------|--------|
| Phase 1 | Albums CRUD + 图片上传/删除 + 封面管理 | P0 |
| Phase 2 | Anime CRUD + 状态/进度管理 | P1 |
| Phase 3 | Music 播放列表 CRUD + 曲目管理 | P2 |

---

## 9. 关键质量指标

| 指标 | 标准 |
|------|------|
| 封面唯一性 | 事务保证一相册一封面 |
| 图片上传安全 | 格式+大小双重校验 |
| 番剧进度一致性 | progress 变更时自动校验状态一致性 |
| 软删除级联 | 相册→图片、播放列表→曲目 |
