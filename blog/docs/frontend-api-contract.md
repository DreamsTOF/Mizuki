# 前端 API 接口契约文档（Auth + Song 模块）

> **覆盖范围**：8 个 Controller · 54 个端点操作
> **数据来源**：全量 Controller / Request / VO 源码逆向分析
> **生成时间**：2026-05-06
> **约定**：所有响应统一包裹在 `BaseResponse<T>` 中

---

## 一、通用类型索引

| 类型名 | 用途 | 所在模块 |
|--------|------|---------|
| `BaseResponse<T>` | 统一响应包装 (success, code, data, message, timestamp) | dreamtof-core |
| `PageReq` | 分页请求基类 (pageNum=1, pageSize=10) | dreamtof-core |
| `CursorReq` | 游标请求基类 (cursor, limit=20, next) | dreamtof-core |
| `PageResult<T>` | 分页响应 (records, total, pages, pageNum, pageSize) | dreamtof-core |
| `CursorResult<T>` | 游标响应 (records, nextCursor, hasNext) | dreamtof-core |

### 认证机制

| 端点类型 | Header 要求 |
|---------|------------|
| 公开端点 (`/public/**`) | 无需认证 |
| 其余端点 | `Authorization: Bearer <accessToken>` |

---

## 二、Auth 模块 — 认证/登录注册 (`/auth`)

### AuthController

| # | 方法 | 路径 | 认证 | Request Body | Response Data |
|---|------|------|------|-------------|---------------|
| 1 | POST | `/auth/register` | ❌ | `RegisterReq` | `LoginVO` |
| 2 | POST | `/auth/login` | ❌ | `LoginReq` | `LoginVO` |
| 3 | POST | `/auth/logout` | ✅ | 无 | `Void` |
| 4 | POST | `/auth/refresh-token` | ❌ | `RefreshTokenReq` | `String` (新 accessToken) |
| 5 | GET | `/auth/me` | ✅ | 无 | `UserVO` |

**涉及类型：**
- Request: `RegisterReq`, `LoginReq`, `RefreshTokenReq`
- Response: `LoginVO`, `UserVO`

---

### UserController — 用户信息管理 (`/auth/user`)

| # | 方法 | 路径 | 认证 | Request Body / PathVar | Response Data |
|---|------|------|------|----------------------|---------------|
| 6 | DELETE | `/auth/user/remove/{id}` | ✅ | Path: `UUID` | `Boolean` |
| 7 | DELETE | `/auth/user/removeByIds` | ✅ | `List<UUID>` | `Boolean` |
| 8 | PUT | `/auth/user/update` | ✅ | `UpdateUserReq` | `UserVO` |
| 9 | GET | `/auth/user/detail/{id}` | ✅ | Path: `UUID` | `UserVO` |
| 10 | GET | `/auth/user/list` | ✅ | 无 | `List<UserVO>` |
| 11 | POST | `/auth/user/page` | ✅ | `UserPageReq` → extends `PageReq` | `PageResult<UserVO>` |
| 12 | POST | `/auth/user/seek` | ✅ | `UserCursorReq` → extends `CursorReq` | `CursorResult<UserVO>` |
| 13 | POST | `/auth/user/fetch-bilibili` | ✅ | `BilibiliFetchReq` | `Boolean` |

**涉及类型：**
- Request: `UpdateUserReq`, `UserPageReq`, `UserCursorReq`, `BilibiliFetchReq`
- Response: `UserVO`

---

## 三、Song 模块 — 歌曲管理 (`/songs`)

### SongController

| # | 方法 | 路径 | 认证 | Request | Response Data |
|---|------|------|------|---------|---------------|
| 14 | POST | `/songs/save` | ✅ | `SongSaveReq` | `Song` (Entity) |
| 15 | PUT | `/songs/update` | ✅ | `SongUpdateReq` | `Song` (Entity) |
| 16 | DELETE | `/songs/remove/{id}` | ✅ | Path: `UUID` | `Boolean` |
| 17 | DELETE | `/songs/removeByIds` | ✅ | `List<UUID>` | `Boolean` |
| 18 | PUT | `/songs/batch/status` | ✅ | `SongBatchStatusReq` | `Boolean` |
| 19 | POST | `/songs/search` | ✅ | `SongSearchPageReq` → extends `PageReq` | `PageResult<Song>` |
| 20 | POST | `/songs/filter` | ✅ | `SongFilterPageReq` → extends `PageReq` | `PageResult<Song>` |
| 21 | POST | `/songs/{id}/click` | ✅ | `SongClickReq` (可选body) | `String` (跳转链接) |
| 22 | GET | `/songs/{id}/stats` | ✅ | Path: `UUID` | `SongClickStat` (Entity) |
| 23 | POST | `/songs/import` | ✅ | `ImportPlaylistReq` | `PlaylistImportVO` |

**涉及类型：**
- Request: `SongSaveReq`, `SongUpdateReq`, `SongBatchStatusReq`, `SongSearchPageReq`, `SongFilterPageReq`, `SongClickReq`, `ImportPlaylistReq`
- Response: `Song` (Entity), `SongClickStat` (Entity), `PlaylistImportVO`
- 枚举: `SongStatusEnum`, `SongRuleEnum`, `MusicPlatformEnum`, `SongSortFieldEnum`, `SortDirectionEnum`
- 值对象: `ClipsInfo`

---

## 四、Song 模块 — 歌单管理 (`/playlists`)

### PlaylistController

| # | 方法 | 路径 | 认证 | Request | Response Data |
|---|------|------|------|---------|---------------|
| 24 | GET | `/playlists/me` | ✅ | 无 | `PlaylistVO` |
| 25 | GET | `/playlists/{id}` | ✅ | Path: `UUID` | `PlaylistVO` |
| 26 | PUT | `/playlists/{id}` | ✅ | `PlaylistUpdateReq` | `PlaylistVO` |
| 27 | PUT | `/playlists/{id}/status` | ✅ | `PlaylistStatusReq` | `PlaylistVO` |
| 28 | GET | `/playlists/{id}/songs` | ✅ | Query: `PlaylistSongCursorReq` → extends `CursorReq` | `CursorResult<PlaylistSongVO>` |
| 29 | DELETE | `/playlists/{playlistId}/{songId}` | ✅ | Path: 2×`UUID` | `Boolean` |
| 30 | POST | `/playlists/{playlistId}/songs/{songId}` | ✅ | Path: 2×`UUID` | `PlaylistSongVO` |

**涉及类型：**
- Request: `PlaylistUpdateReq`, `PlaylistStatusReq`, `PlaylistSongCursorReq`
- Response: `PlaylistVO`, `PlaylistSongVO`
- 枚举: `PlaylistStatusEnum`

---

## 五、Song 模块 — 标签管理 (`/labels`)

### LabelController

| # | 方法 | 路径 | 认证 | Request | Response Data |
|---|------|------|------|---------|---------------|
| 31 | POST | `/labels/save` | ✅ | `LabelCreateReq` | `LabelVO` |
| 32 | DELETE | `/labels/remove/{id}` | ✅ | Path: `UUID` | `Boolean` |
| 33 | DELETE | `/labels/removeByIds` | ✅ | `List<UUID>` | `Boolean` |
| 34 | PUT | `/labels/update` | ✅ | `LabelUpdateReq` | `LabelVO` |
| 35 | GET | `/labels/detail/{id}` | ✅ | Path: `UUID` | `LabelVO` |
| 36 | GET | `/labels/list` | ✅ | 无 | `List<LabelVO>` |
| 37 | POST | `/labels/page` | ✅ | `LabelPageReq` → extends `PageReq` | `PageResult<LabelVO>` |
| 38 | POST | `/labels/seek` | ✅ | `LabelCursorReq` → extends `CursorReq` | `CursorResult<LabelVO>` |
| 39 | GET | `/labels/system` | ✅ | 无 | `List<LabelVO>` (系统预设) |
| 40 | GET | `/labels/mine` | ✅ | 无 | `List<LabelVO>` (我的自定义) |

**涉及类型：**
- Request: `LabelCreateReq`, `LabelUpdateReq`, `LabelPageReq`, `LabelCursorReq`
- Response: `LabelVO`, `LabelBriefVO`

---

## 六、Song 模块 — 公开信息 (`/public`) ⚠️ 全部无需认证

### PublicController

| # | 方法 | 路径 | 认证 | Request | Response Data |
|---|------|------|------|---------|---------------|
| 41 | GET | `/public/{slug}` | ❌ | Path: `String` | `PublicInfoVO` |
| 42 | GET | `/public/{slug}/songs` | ❌ | Query: `PublicSongPageReq` → extends `PageReq` | `PublicSongListVO` |
| 43 | GET | `/public/{slug}/songs/all` | ❌ | Path: `String` | `PublicSongListAllVO` |

**涉及类型：**
- Request: `PublicSongPageReq`
- Response: `PublicInfoVO`, `PublicSongListVO`, `PublicSongListAllVO`, `SongListVO`, `LabelBriefVO`, `HostBriefInfo`

---

## 七、Song 模块 — 歌曲标签关联 (`/song/songLabel`)

### SongLabelController（标准 CRUD + 分页 + 游标）

| # | 方法 | 路径 | 认证 | Request | Response Data |
|---|------|------|------|---------|---------------|
| 44 | POST | `/song/songLabel/save` | ✅ | `SongLabelSaveReq` | `SongLabel` (Entity) |
| 45 | DELETE | `/song/songLabel/remove/{id}` | ✅ | Path: `UUID` | `Boolean` |
| 46 | DELETE | `/song/songLabel/removeByIds` | ✅ | `List<UUID>` | `Boolean` |
| 47 | PUT | `/song/songLabel/update` | ✅ | `SongLabelUpdateReq` | `SongLabel` (Entity) |
| 48 | GET | `/song/songLabel/detail/{id}` | ✅ | Path: `UUID` | `SongLabel` (Entity) |
| 49a | GET | `/song/songLabel/list` | ✅ | 无 | `List<SongLabel>` |
| 49b | POST | `/song/songLabel/page` | ✅ | `SongLabelPageReq` → `PageReq` | `PageResult<SongLabel>` |
| 49c | POST | `/song/songLabel/seek` | ✅ | `SongLabelCursorReq` → `CursorReq` | `CursorResult<SongLabel>` |

**涉及类型：**
- Request: `SongLabelSaveReq`, `SongLabelUpdateReq`, `SongLabelPageReq`, `SongLabelCursorReq`
- Response: `SongLabel` (Entity)

---

## 八、Song 模块 — 歌曲点击统计 (`/song/songClickStat`)

### SongClickStatController

| # | 方法 | 路径 | 认证 | Request | Response Data |
|---|------|------|------|---------|---------------|
| 50 | POST | `/song/songClickStat/save` | ✅ | `SongClickStatSaveReq` | `SongClickStat` (Entity) |
| 51 | GET | `/song/songClickStat/detail/{id}` | ✅ | Path: `UUID` | `SongClickStat` (Entity) |
| 52 | GET | `/song/songClickStat/list` | ✅ | 无 | `List<SongClickStat>` |
| 53 | POST | `/song/songClickStat/page` | ✅ | `SongClickStatPageReq` → `PageReq` | `PageResult<SongClickStat>` |
| 54 | POST | `/song/songClickStat/seek` | ✅ | `SongClickStatCursorReq` → `CursorReq` | `CursorResult<SongClickStat>` |

**涉及类型：**
- Request: `SongClickStatSaveReq`, `SongClickStatPageReq`, `SongClickStatCursorReq`
- Response: `SongClickStat` (Entity), `SongClickStatVO`

---

## 九、完整类型索引汇总（供前端脚本扫描）

### Auth 模块 Request 类 (7 个)
`RegisterReq`, `LoginReq`, `RefreshTokenReq`, `UpdateUserReq`, `UserPageReq`, `UserCursorReq`, `BilibiliFetchReq`

### Auth 模块 VO/Response 类 (2 个)
`LoginVO`, `UserVO`

### Song 模块 Request 类 (23 个)
`SongSaveReq`, `SongUpdateReq`, `SongBatchStatusReq`, `SongSearchPageReq`, `SongFilterPageReq`, `SongClickReq`, `ImportPlaylistReq`, `PlaylistUpdateReq`, `PlaylistStatusReq`, `PlaylistSongCursorReq`, `LabelCreateReq`, `LabelUpdateReq`, `LabelPageReq`, `LabelCursorReq`, `SongLabelSaveReq`, `SongLabelUpdateReq`, `SongLabelPageReq`, `SongLabelCursorReq`, `SongClickStatSaveReq`, `SongClickStatPageReq`, `SongClickStatCursorReq`, `PublicSongPageReq`, `NetEaseImportReq`

### Song 模块 VO/Response 类 (14 个)
`PlaylistVO`, `PlaylistSongVO`, `LabelVO`, `LabelBriefVO`, `SongDetailVO`, `SongListVO`, `SongClickStatVO`, `PlaylistImportVO`, `NetEaseImportVO`, `PublicInfoVO`, `PublicSongListVO`, `PublicSongListAllVO`, `ImportFailureExcelRow`, `UserResponse`

### 枚举类 (6 个)
`SongStatusEnum`, `SongRuleEnum`, `MusicPlatformEnum`, `PlaylistStatusEnum`, `SongSortFieldEnum`, `SortDirectionEnum`

### Entity 类（直接作为 Response）(3 个)
`Song`, `SongLabel`, `SongClickStat`

### 值对象 (1 个)
`ClipsInfo`

---

## 十、端点速查总表

```
┌─────────────────────┬────────┬──────────────────────────────────┬───────┐
│ 模块/功能           │ 方法   │ 路径                              │ 认证 │
├─────────────────────┼────────┼──────────────────────────────────┼───────┤
│ Auth - 注册         │ POST   │ /auth/register                    │ ❌   │
│ Auth - 登录         │ POST   │ /auth/login                      │ ❌   │
│ Auth - 登出         │ POST   │ /auth/logout                     │ ✅   │
│ Auth - 刷新Token    │ POST   │ /auth/refresh-token              │ ❌   │
│ Auth - 当前用户     │ GET    │ /auth/me                         │ ✅   │
│ User - 删除         │ DELETE │ /auth/user/remove/{id}           │ ✅   │
│ User - 批量删除     │ DELETE │ /auth/user/removeByIds           │ ✅   │
│ User - 更新         │ PUT    │ /auth/user/update                │ ✅   │
│ User - 详情         │ GET    │ /auth/user/detail/{id}           │ ✅   │
│ User - 列表         │ GET    │ /auth/user/list                  │ ✅   │
│ User - 分页         │ POST   │ /auth/user/page                  │ ✅   │
│ User - 游标         │ POST   │ /auth/user/seek                  │ ✅   │
│ User - 抓取B站      │ POST   │ /auth/user/fetch-bilibili        │ ✅   │
│ Song - 添加         │ POST   │ /songs/save                      │ ✅   │
│ Song - 编辑         │ PUT    │ /songs/update                    │ ✅   │
│ Song - 删除         │ DELETE │ /songs/remove/{id}               │ ✅   │
│ Song - 批量删除     │ DELETE │ /songs/removeByIds               │ ✅   │
│ Song - 批量改状态   │ PUT    │ /songs/batch/status              │ ✅   │
│ Song - 搜索         │ POST   │ /songs/search                    │ ✅   │
│ Song - 筛选         │ POST   │ /songs/filter                    │ ✅   │
│ Song - 点击记录     │ POST   │ /songs/{id}/click                │ ✅   │
│ Song - 点击统计     │ GET    │ /songs/{id}/stats                │ ✅   │
│ Song - 导入歌单     │ POST   │ /songs/import                    │ ✅   │
│ Playlist - 我的     │ GET    │ /playlists/me                    │ ✅   │
│ Playlist - 详情     │ GET    │ /playlists/{id}                  │ ✅   │
│ Playlist - 更新     │ PUT    │ /playlists/{id}                  │ ✅   │
│ Playlist - 改状态   │ PUT    │ /playlists/{id}/status           │ ✅   │
│ Playlist - 歌曲列表 │ GET    │ /playlists/{id}/songs            │ ✅   │
│ Playlist - 移除歌曲 │ DELETE │ /playlists/{pid}/{sid}           │ ✅   │
│ Playlist - 添加歌曲 │ POST   │ /playlists/{pid}/songs/{sid}     │ ✅   │
│ Label - 创建        │ POST   │ /labels/save                     │ ✅   │
│ Label - 删除        │ DELETE │ /labels/remove/{id}              │ ✅   │
│ Label - 批量删除    │ DELETE │ /labels/removeByIds              │ ✅   │
│ Label - 更新        │ PUT    │ /labels/update                   │ ✅   │
│ Label - 详情        │ GET    │ /labels/detail/{id}              │ ✅   │
│ Label - 列表        │ GET    │ /labels/list                     │ ✅   │
│ Label - 分页        │ POST   │ /labels/page                     │ ✅   │
│ Label - 游标        │ POST   │ /labels/seek                     │ ✅   │
│ Label - 系统预设    │ GET    │ /labels/system                   │ ✅   │
│ Label - 我的标签    │ GET    │ /labels/mine                     │ ✅   │
│ Public - 主播信息   │ GET    │ /public/{slug}                   │ ❌   │
│ Public - 歌单(分页) │ GET    │ /public/{slug}/songs             │ ❌   │
│ Public - 歌单(全部) │ GET    │ /public/{slug}/songs/all         │ ❌   │
│ SongLabel - 新增    │ POST   │ /song/songLabel/save             │ ✅   │
│ SongLabel - 删除    │ DELETE │ /song/songLabel/remove/{id}      │ ✅   │
│ SongLabel - 批删    │ DELETE │ /song/songLabel/removeByIds      │ ✅   │
│ SongLabel - 更新    │ PUT    │ /song/songLabel/update           │ ✅   │
│ SongLabel - 详情    │ GET    │ /song/songLabel/detail/{id}      │ ✅   │
│ SongLabel - 列表    │ GET    │ /song/songLabel/list             │ ✅   │
│ SongLabel - 分页    │ POST   │ /song/songLabel/page             │ ✅   │
│ SongLabel - 游标    │ POST   │ /song/songLabel/seek             │ ✅   │
│ ClickStat - 新增    │ POST   │ /song/songClickStat/save         │ ✅   │
│ ClickStat - 详情    │ GET    │ /song/songClickStat/detail/{id}  │ ✅   │
│ ClickStat - 列表    │ GET    │ /song/songClickStat/list         │ ✅   │
│ ClickStat - 分页    │ POST   │ /song/songClickStat/page         │ ✅   │
│ ClickStat - 游标    │ POST   │ /song/songClickStat/seek         │ ✅   │
└─────────────────────┴────────┴──────────────────────────────────┴───────┘
                              合计：54 个端点操作
```
