---
alwaysApply: false
description: 开发 Vue 页面、组件、Pinia Store、路由、表单、调用 API 时自动加载。涉及 .vue/.ts 文件的新增或修改务必参考本规范。
---

# 项目约束

## API

所有后端接口通过 Orval 自动生成，从 `@/api` 导入 Controller：

```typescript
import { SongController } from '@/api'
```

Controller 提供三件套：`queryKey()` / `queryFn()` / `useXxx()` mutation hook。Orval 生成的文件（`src/api/generated/`、`src/api/real/`、`src/api/index.ts`）**只读勿改**。

拦截器已自动完成，**业务代码禁止重复处理**：

- 请求/响应 Token 注入、设备指纹注入
- 业务错误自动 `Notify.error()` — **禁止组件重复弹错误提示**
- 40100 跳登录页、40101 清密钥、组件卸载自动取消请求
- **响应数据自动提取** — Controller 返回 `BaseResponse<T>`，业务层需访问 `response.data` 获取实际数据

API 类型从 `@/api/generated/{module}/model` 导入，**禁止手写 API 返回类型**。

**Mutation Hook 使用示例**：

```typescript
// ✅ 正确：访问 response.data 获取业务数据
const loginMutation = AuthController.usePostAuthLogin({
  onSuccess: (response) => {
    const data = response.data  // 获取 LoginVo
    if (data) {
      authStore.setAuth(data.accessToken ?? '', data)
      Notify.success('登录成功')
    }
  },
})

// ✅ 正确：如果不需要返回数据，可以忽略
const createMutation = SongController.usePostSongsSave({
  onSuccess: () => {
    Notify.success('添加成功')
    queryClient.invalidateQueries({ queryKey: songKeys.lists() })
  },
})
```

## 数据获取

**使用 `useQuery`**。Vue Query 的 Suspense 支持是实验性的，需要配合 Vue 的 `<Suspense>` 组件使用。

```vue
<script setup lang="ts">
import { useQuery } from '@tanstack/vue-query'
import { SongController } from '@/api'

const { data, isLoading } = useQuery({
  queryKey: SongController.getSongsSongListQueryKey(),
  queryFn: SongController.getSongsSongListQueryFn(),
})
</script>

<template>
  <Suspense>
    <template #default>
      <SongList :songs="data" />
    </template>
    <template #fallback>
      <Loading />
    </template>
  </Suspense>
</template>
```

**重要说明**：
- Vue Query 的 Suspense 支持仍处于实验阶段，官方文档明确说明这些 API 会变化
- 使用 `useQuery` 获取数据，Vue 的 `<Suspense>` 组件会自动处理加载状态
- **禁止使用 `useSuspenseQuery`**（该 hook 在 Vue Query 中不存在，是 React Query 的 API）
- 不要在 composable 或组件中手动调用 `suspense()` 方法
- 加载状态由 `<Suspense>` 的 `#fallback` 插槽处理，无需手动判断 `isLoading`

**错误示例**：
```typescript
// ❌ 错误：Vue Query 没有 useSuspenseQuery
import { useSuspenseQuery } from '@tanstack/vue-query'

// ❌ 错误：不要在 composable 中手动调用 suspense()
const { data, suspense } = useQuery({ ... })
await suspense() // 这会导致编译错误
```

**数据变更** 用 Controller 的 mutation hook：

```typescript
const queryClient = useQueryClient()
const createMutation = SongController.usePostSongsSongSave({
  onSuccess: () => queryClient.invalidateQueries({ queryKey: songKeys.lists() }),
})
createMutation.mutate(data)
```

**缓存失效**：创建/删除 → `xxxKeys.lists()` / 更新 → `xxxKeys.all()`

Query Key 工厂定义在 `src/api/keys/`，禁止跨组件重复调用同一 queryKey 的 `useSuspenseQuery`（抽到 composable）。禁止手动 `new QueryClient()`。

## UI

| 场景       | 方案                                                                   |
| -------- | -------------------------------------------------------------------- |
| 加载中      | `<Loading />`（`@/utils/Loading.vue`），Loader2 图标                      |
| 空状态      | `<BaseEmpty type="data" />`，搜索无结果 `type="search"`，404 用 `type="404"` |
| 消息通知     | `Notify.success/error/info/warning`（`@/utils/notify`）                |
| 页面进度     | NProgress 已全局配置，**禁止手动调用**                                           |
| 动态列表     | `v-auto-animate` 已全局注册，容器上加指令即可                                      |
| 所有图标     | `lucide-vue-next`                                                    |
| 所有 UI 组件 | `shadcn-vue`                                                         |
| 样式       | Tailwind + `--sk-*` 设计 Token，**禁止硬编码**颜色/字号/圆角                       |
| 错误边界     | 页面级：`<ErrorBoundary><Suspense>...</Suspense></ErrorBoundary>`        |
| 危险操作     | shadcn-vue `AlertDialog` 二次确认                                        |

## 表单

vee-validate + zod，schema 直接写在 `.vue` 的 `<script setup>` 中，不单独建文件。详见 [validation.md](references/validation.md)。

提交中：按钮 `disabled` + `Loader2` 图标。成功：`Notify.success()` + `invalidateQueries`。失败：`Notify.error()` + 保留用户输入。

## Pinia

需持久化的 Store 加 `{ persist: true }`。通过 actions 修改 state，禁止组件直接改。

## 路由

新增路由只追加，已有 token 鉴权守卫无需改动。

***

## 延伸阅读

- [validation.md](references/validation.md) — vee-validate + zod 完整表单模式

