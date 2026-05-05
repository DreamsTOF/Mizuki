# 系统通用模块 — 通用后端需求

> 本文档定义 Mizuki 博客系统通用模块的后端功能点与数据需求。不包含接口URL路径、HTTP状态码、错误码、认证方式、后端返回风格等技术实现细节。

---

## 1. 站点配置获取

### 1.1 功能点

- 提供站点全局配置的查询能力
- 支持按配置组或单个配置项获取
- 支持获取全部配置的一次性导出

### 1.2 数据需求

**站点元信息**

| 字段 | 类型 | 说明 |
|------|------|------|
| title | string | 站点主标题 |
| subtitle | string | 站点副标题 |
| siteURL | string | 站点完整URL，以斜杠结尾 |
| keywords | string[] | SEO关键词列表 |
| siteStartDate | string | 站点启动日期，格式YYYY-MM-DD |
| timeZone | number | 时区偏移，范围-12至+12 |
| lang | string | 站点语言代码 |

**主题外观配置**

| 字段 | 类型 | 说明 |
|------|------|------|
| themeColor.hue | number | 主题色色相，0-360 |
| themeColor.fixed | boolean | 是否固定主题色（隐藏选择器） |
| font.asciiFont | object | 英文字体配置：字体族、字重、本地文件路径、是否启用压缩 |
| font.cjkFont | object | CJK字体配置：字体族、字重、本地文件路径、是否启用压缩 |
| tagStyle.useNewStyle | boolean | 标签样式开关 |
| wallpaperMode.defaultMode | string | 默认壁纸模式：banner/fullscreen/none |
| wallpaperMode.showModeSwitchOnMobile | string | 移动端布局切换按钮显示策略 |

**功能开关配置**

| 字段 | 类型 | 说明 |
|------|------|------|
| featurePages.anime | boolean | 番剧页面开关 |
| featurePages.diary | boolean | 日记页面开关 |
| featurePages.friends | boolean | 友链页面开关 |
| featurePages.projects | boolean | 项目页面开关 |
| featurePages.skills | boolean | 技能页面开关 |
| featurePages.timeline | boolean | 时间线页面开关 |
| featurePages.albums | boolean | 相册页面开关 |
| featurePages.devices | boolean | 设备页面开关 |

**导航配置**

| 字段 | 类型 | 说明 |
|------|------|------|
| navBarConfig.links | array | 导航链接列表，支持多级嵌套子菜单 |
| sidebarLayoutConfig.properties | array | 侧边栏组件属性列表（类型、位置、CSS类、动画延迟、响应式配置） |
| sidebarLayoutConfig.components.left | string[] | 左侧边栏组件顺序 |
| sidebarLayoutConfig.components.right | string[] | 右侧边栏组件顺序 |
| sidebarLayoutConfig.components.drawer | string[] | 抽屉菜单组件顺序 |
| sidebarLayoutConfig.defaultAnimation | object | 默认动画配置（启用开关、基础延迟、递增延迟） |
| sidebarLayoutConfig.responsive.breakpoints | object | 响应式断点（移动端、平板端、桌面端） |

**Banner 配置**

| 字段 | 类型 | 说明 |
|------|------|------|
| banner.src | object/string/array | 桌面/移动端图片源，支持单图、多图数组或分设备配置 |
| banner.position | string | 图片定位：top/center/bottom |
| banner.carousel.enable | boolean | 轮播开关 |
| banner.carousel.interval | number | 轮播间隔秒数 |
| banner.waves.enable | boolean | 水波纹效果开关 |
| banner.waves.performanceMode | boolean | 水波纹性能模式 |
| banner.waves.mobileDisable | boolean | 移动端禁用波纹 |
| banner.imageApi.enable | boolean | 外部图片API开关 |
| banner.imageApi.url | string | 图片API地址 |
| banner.homeText.enable | boolean | 主页文字显示开关 |
| banner.homeText.title | string | 主页主标题 |
| banner.homeText.subtitle | string/string[] | 副标题，支持单条或多条轮播 |
| banner.homeText.typewriter | object | 打字机效果配置（速度、删除速度、暂停时间） |
| banner.credit | object | 图片来源署名配置 |
| banner.navbar.transparentMode | string | 导航栏透明模式：semi/full/semifull |

**文章列表配置**

| 字段 | 类型 | 说明 |
|------|------|------|
| postListLayout.defaultMode | string | 默认布局：list/grid |
| postListLayout.allowSwitch | boolean | 是否允许用户切换 |
| postListLayout.categoryBar.enable | boolean | 分类导航条开关 |

**第三方集成配置**

| 字段 | 类型 | 说明 |
|------|------|------|
| bangumi.userId | string | Bangumi用户ID |
| bangumi.fetchOnDev | boolean | 开发环境是否获取 |
| bilibili.vmid | string | Bilibili用户ID |
| bilibili.fetchOnDev | boolean | 开发环境是否获取 |
| bilibili.coverMirror | string | 封面镜像源URL |
| bilibili.useWebp | boolean | 是否使用WebP |
| anime.mode | string | 番剧数据来源：bangumi/local/bilibili |
| commentConfig.enable | boolean | 评论系统总开关 |
| commentConfig.system | string | 评论系统类型：twikoo/giscus |
| commentConfig.twikoo | object | Twikoo配置（envId、语言） |
| commentConfig.giscus | object | Giscus配置（仓库、分类、主题等） |
| musicPlayerConfig.enable | boolean | 音乐播放器开关 |
| musicPlayerConfig.mode | string | 播放模式：local/meting |
| musicPlayerConfig.meting_api | string | Meting API地址模板 |
| musicPlayerConfig.id | string | 歌单ID |
| musicPlayerConfig.server | string | 音乐源服务器 |
| musicPlayerConfig.type | string | 播放列表类型 |
| thirdPartyAnalytics.enable | boolean | 第三方统计开关 |
| thirdPartyAnalytics.clarityId | string | Microsoft Clarity项目ID |

**特效与其他配置**

| 字段 | 类型 | 说明 |
|------|------|------|
| toc.enable | boolean | 目录系统总开关 |
| toc.mobileTop | boolean | 移动端顶部TOC按钮 |
| toc.desktopSidebar | boolean | 桌面端侧边栏TOC |
| toc.floating | boolean | 悬浮TOC按钮 |
| toc.depth | number | 目录深度1-6 |
| toc.useJapaneseBadge | boolean | 使用日语假名标记 |
| generateOgImages | boolean | OG图片生成开关 |
| showCoverInContent | boolean | 文章内容页显示封面 |
| showLastModified | boolean | 显示上次编辑时间 |
| pageProgressBar.enable | boolean | 页面顶部进度条 |
| pageProgressBar.height | number | 进度条高度 |
| pageProgressBar.duration | number | 动画时长 |
| shareConfig.enable | boolean | 分享功能开关 |
| licenseConfig.enable | boolean | 版权声明开关 |
| licenseConfig.name | string | 许可证名称 |
| licenseConfig.url | string | 许可证链接 |
| sakuraConfig | object | 樱花特效完整参数（数量、尺寸、透明度、速度、层级） |
| pioConfig | object | Pio看板娘配置（启用、模型路径、位置、尺寸、对话框文本） |
| permalinkConfig.enable | boolean | 固定链接功能开关 |
| permalinkConfig.format | string | 固定链接格式模板 |
| expressiveCodeConfig.theme | string | 代码高亮主题 |
| expressiveCodeConfig.hideDuringThemeTransition | boolean | 主题切换时隐藏代码块 |
| favicon | array | Favicon配置列表（路径、主题、尺寸） |
| announcementConfig | object | 公告配置（标题、内容、类型、可关闭、链接） |
| profileConfig | object | 个人资料配置（头像、名称、简介、打字机效果、社交链接） |
| footerConfig | object | 页脚配置（启用开关、自定义HTML） |
| relatedPostsConfig | object | 相关文章配置（启用开关、最大数量） |
| randomPostsConfig | object | 随机文章配置（启用开关、最大数量） |
| fullscreenWallpaperConfig | object | 全屏壁纸配置（图片源、位置、轮播、层级、透明度、模糊度） |

---

## 2. About 页面内容获取

### 2.1 功能点

- 提供 About 页面 Markdown 内容的查询能力
- 支持获取原始 Markdown 文本

### 2.2 数据需求

| 字段 | 类型 | 说明 |
|------|------|------|
| content | string | About 页面 Markdown 原始内容 |
| updatedAt | string/datetime | 内容最后更新时间 |

---

## 3. 文件上传通用能力

### 3.1 功能点

- 支持单文件上传
- 支持多文件批量上传
- 支持按业务类型分目录存储
- 支持上传后图片处理（格式转换、缩略图生成）
- 返回可访问的 URL

### 3.2 数据需求

**请求数据**

| 字段 | 类型 | 说明 |
|------|------|------|
| files | file/array | 上传的文件或多文件 |
| folder | string | 目标目录类型：posts/albums/diary/devices/assets/avatars |
| convertToWebp | boolean | 是否自动转换为WebP格式 |
| generateThumbnail | boolean | 是否生成缩略图 |

**响应数据**

| 字段 | 类型 | 说明 |
|------|------|------|
| originalName | string | 原始文件名 |
| url | string | 文件可访问URL |
| thumbnailUrl | string | 缩略图URL（如生成） |
| width | number | 图片宽度（图片类型时） |
| height | number | 图片高度（图片类型时） |
| size | number | 文件大小（字节） |
| mimeType | string | 文件MIME类型 |

---

## 4. OG 图片生成

### 4.1 功能点

- 根据文章信息生成 1200x630 的 OpenGraph 分享图片
- 支持运行时动态生成或预生成缓存
- 生成的图片包含站点品牌标识和文章信息

### 4.2 数据需求

**输入数据**

| 字段 | 类型 | 说明 |
|------|------|------|
| postTitle | string | 文章标题 |
| postDescription | string | 文章描述（可选） |
| postPublished | string/date | 文章发布日期 |
| siteTitle | string | 站点标题 |
| themeHue | number | 主题色色相 |
| authorName | string | 作者名称 |
| authorAvatar | string/url | 作者头像URL |
| siteIcon | string/url | 站点图标URL |

**输出数据**

| 字段 | 类型 | 说明 |
|------|------|------|
| image | binary | PNG格式图片数据 |
| width | number | 图片宽度（1200） |
| height | number | 图片高度（630） |

---

## 5. RSS / Atom 订阅数据获取

### 5.1 功能点

- 提供生成 RSS 2.0 订阅源所需的全部数据
- 提供生成 Atom 1.0 订阅源所需的全部数据
- 数据需排除加密文章和草稿文章
- 文章内图片路径需支持转为绝对URL

### 5.2 数据需求

**站点信息**

| 字段 | 类型 | 说明 |
|------|------|------|
| siteTitle | string | 站点标题 |
| siteSubtitle | string | 站点副标题 |
| siteURL | string | 站点URL |
| siteLanguage | string | 站点语言代码 |

**文章条目列表**

| 字段 | 类型 | 说明 |
|------|------|------|
| title | string | 文章标题 |
| description | string | 文章描述/摘要 |
| published | datetime | 发布日期时间 |
| updated | datetime | 更新日期时间 |
| link | string | 文章完整URL |
| content | string | 文章正文HTML（Markdown渲染后，图片路径为绝对URL） |
| authorName | string | 作者名称 |
| category | string | 文章分类（可选） |
| tags | string[] | 文章标签列表（可选） |

---

## 6. 站点统计信息

### 6.1 功能点

- 提供站点维度的聚合统计数据
- 支持一次性获取全部统计项
- 总字数统计需排除代码块内容

### 6.2 数据需求

| 字段 | 类型 | 说明 |
|------|------|------|
| postCount | number | 已发布文章总数（排除草稿） |
| categoryCount | number | 不重复分类总数 |
| tagCount | number | 不重复标签总数 |
| totalWords | number | 所有文章正文字数合计（CJK字符逐个计数，非CJK按单词计数，排除代码块） |
| runningDays | number | 站点运行天数（从siteStartDate到当前日期） |
| lastUpdateDays | number | 距最新文章发布日的天数 |

---

## 7. 加密文章密码验证

### 7.1 功能点

- 提供加密文章的密码验证能力
- 验证通过后返回解密后的文章内容
- 当前前端使用 AES 加密，后端化后需保留兼容或提供等效能力

### 7.2 数据需求

**请求数据**

| 字段 | 类型 | 说明 |
|------|------|------|
| postId/slug | string | 文章标识 |
| password | string | 访问密码 |

**响应数据**

| 字段 | 类型 | 说明 |
|------|------|------|
| decryptedContent | string | 解密后的文章Markdown内容 |
| isValid | boolean | 密码是否正确 |

---

## 8. 多语言翻译数据

### 8.1 功能点

- 提供前端 i18n 所需的翻译字符串
- 支持按语言代码获取完整翻译表

### 8.2 数据需求

| 字段 | 类型 | 说明 |
|------|------|------|
| lang | string | 语言代码：en/ja/zh_CN/zh_TW等 |
| translations | object | 键值对形式的翻译字符串表 |

当前支持的语言及对应翻译键：

- `en` — 英语
- `ja` — 日语
- `zh_CN` — 简体中文
- `zh_TW` — 繁体中文
