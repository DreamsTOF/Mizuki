# 前端配置变更说明

## Feature 1: Meting API 代理

后端新增 `/api/media/music/meting` 端点作为 Meting API 的代理层，用于替代前端直接调用外部 Meting API。

### 变更文件

#### 1. `src/components/widgets/music-player/constants.ts`

将 `DEFAULT_METING_API` 的值从外部 API 地址改为后端代理地址：

```diff
 export const DEFAULT_METING_API =
-  "https://www.bilibili.uno/api?server=:server&type=:type&id=:id&auth=:auth&r=:r";
+  "/api/media/music/meting?server=:server&type=:type&id=:id&auth=:auth&r=:r";
```

> **注意**：由于后端代理已将缓存和跨域逻辑处理，URL 前缀 `/api` 会被 Spring Boot 的 `context-path`（如有配置）自动处理。如果后端配置了 `server.servlet.context-path`，请相应调整。

#### 2. 站点配置中的 Meting API

如果在站点管理后台配置了自定义 `meting_api`，请将其值更新为：

```
/api/media/music/meting?server=:server&type=:type&id=:id&auth=:auth&r=:r
```

### 后端代理特性

| 特性 | 说明 |
|------|------|
| **缓存** | 使用 Guava Cache，5 分钟过期，最多缓存 50 个响应 |
| **超时** | 连接超时 10s，读取超时 30s |
| **User-Agent** | 设置为浏览器 UA 以通过反爬检查 |
| **Referer** | 设置为 `https://music.163.com` |

### API 参数说明

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `server` | string | `netease` | 音乐源（netease/tencent/xiami等） |
| `type` | string | `playlist` | 类型（playlist/song/album等） |
| `id` | string | `14164869977` | 资源 ID |
| `auth` | string | - | 认证参数（可选） |

响应格式：`BaseResponse<String>`，其中 `data` 字段为 Meting API 返回的 JSON 字符串。
