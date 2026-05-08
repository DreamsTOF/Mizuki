# 前端开发规范 V2.0 - 后期功能池

这些是前期开发阶段暂不强制使用的功能，在项目上线前或团队扩张后再补充。

---

## 第一类：过度防御（前期鸡肋，后期一键补）

### 1. Sentry 全链路错误监控
**为什么前期不用**
- 开发阶段直接看控制台/Network面板足够清晰
- 配置DSN、环境判断、自定义追踪极其消耗精力
- Sentry集成完全无侵入，后期可快速补充

**后期怎么补**
```typescript
// src/main.ts
import * as Sentry from '@sentry/vue'

const initSentry = () => {
  const environment = import.meta.env.MODE
  const dsn = import.meta.env.VITE_SENTRY_DSN
  
  if (environment === 'development' || !dsn) return
  
  Sentry.init({
    dsn,
    environment,
    release: `songs-frontend@${import.meta.env.VITE_APP_VERSION}`,
    sampleRate: 1.0,
    tracesSampleRate: environment === 'staging' ? 1.0 : 0.1,
    integrations: [Sentry.browserTracingIntegration({ router })],
  })
}

initSentry()
```

**补充清单**
- Sentry初始化配置
- 错误分类上报
- 用户信息关联
- 敏感信息过滤
- 性能监控配置
- 会话重放采样

---

### 2. API请求加密
**为什么前期不用**
- 开发阶段Network面板全是乱码，调试接口非常痛苦
- 所有加解密都在axios拦截器处理，后期可一键开启

**后期怎么补**
```typescript
// src/api/api.ts
const ENCRYPT_ENABLED = import.meta.env.VITE_ENCRYPT_ENABLED === 'true'

axiosInstance.interceptors.request.use(config => {
  if (ENCRYPT_ENABLED && !isWhitelistApi(config.url)) {
    config.data = encrypt(config.data)
  }
  return config
})

axiosInstance.interceptors.response.use(response => {
  if (ENCRYPT_ENABLED && response.data.encrypted) {
    response.data = decrypt(response.data)
  }
  return response
})
```

**补充清单**
- AES加密实现
- 白名单接口配置
- 响应解密处理
- 密钥管理机制

---

### 3. 严格的Git提交格式
**为什么前期不用**
- 可能连Issue系统(Jira/禅道)都还没建立
- 每次提交还要编Issue号，纯属添乱

**后期怎么补**
```json
// package.json 配合 Husky + commitlint
{
  "commitlint": {
    "extends": ["@commitlint/config-conventional"],
    "rules": {
      "scope-enum": [2, "always", ["auth", "song", "user"]],
      "scope-empty": [2, "never"]
    }
  }
}
```

**补充清单**
- Husky + commitlint配置
- 提交格式规范: `[#Issue号] type(scope): subject`
- 自动检查钩子
- 提交信息模板

---

### 4. 权限控制系统
**为什么前期不用**
- 个人项目中通常只有一个用户角色，不需要角色矩阵
- 按钮级权限、动态路由添加等增加架构复杂度
- 前期只需要登录守卫判断 token 即可

**后期怎么补**
```typescript
// stores/domain/permissionStore.ts
export const usePermissionStore = defineStore('permission', () => {
  const permissions = ref<string[]>([])
  const asyncRoutes = ref<RouteRecordRaw[]>([])
  
  const hasPermission = (key: string) => permissions.value.includes(key)
  
  const generateRoutes = async () => {
    const routes = await fetchUserRoutes()
    asyncRoutes.value = filterAsyncRoutes(routes)
    asyncRoutes.value.forEach(route => router.addRoute(route))
  }
  
  return { permissions, asyncRoutes, hasPermission, generateRoutes }
}, { persist: true })
```

```vue
<!-- 按钮级权限 -->
<el-button v-permission="'song:delete'">删除</el-button>
```

**补充清单**
- permissionStore + 后端权限数组
- constantRoutes / asyncRoutes 分类
- beforeEach 动态添加路由
- v-permission 指令（按钮级权限）
- 权限格式：字符串数组 `['admin:view', 'song:edit']`

---

## 第二类：交互体验优化（前期费代码，后期慢慢加）

### 5. 乐观更新
**为什么前期不用**
- 要写极长的代码：取消查询、获取旧数据、写入新数据、处理Error回滚、Settled重新获取
- 为了点按钮快零点几秒，前期完全不值得
- 前期直接用普通useMutation + invalidateQueries足够

**后期怎么补**
```typescript
const mutation = useMutation({
  mutationFn: updateSong,
  onMutate: async newSong => {
    await queryClient.cancelQueries({ queryKey: songKeys.detail(newSong.id) })
    const previousSong = queryClient.getQueryData(songKeys.detail(newSong.id))
    queryClient.setQueryData(songKeys.detail(newSong.id), newSong)
    return { previousSong }
  },
  onError: (err, newSong, context) => {
    queryClient.setQueryData(songKeys.detail(newSong.id), context.previousSong)
  },
  onSettled: () => {
    queryClient.invalidateQueries({ queryKey: songKeys.lists() })
  },
})
```

**补充清单**
- 乐观更新的完整流程
- 回滚机制
- 失败处理策略
- 成功后验证同步

---

### 6. TanStack Query 高级模式
**为什么前期不用**
- 受限于前期数据量，普通分页完全够用，不需要无限滚动
- 预取增加复杂度，用户体验提升有限
- 并行查询的场景在前期较少

**后期怎么补**

```typescript
// 游标分页 / 无限滚动
const {
  data,
  fetchNextPage,
  hasNextPage,
  isFetchingNextPage,
} = useInfiniteQuery({
  queryKey: songKeys.cursor(null),
  queryFn: ({ pageParam }) => SongController.cursorList({ cursor: pageParam }),
  initialPageParam: null,
  getNextPageParam: (lastPage) => lastPage.nextCursor,
})

// 并行查询
const queries = useQueries({
  queries: userIds.map(id => ({
    queryKey: userKeys.detail(id),
    queryFn: () => UserController.getUser(id),
  })),
})

// 预取
const prefetchSong = (id: string) => {
  queryClient.prefetchQuery({
    queryKey: songKeys.detail(id),
    queryFn: () => SongController.getSong(id),
  })
}
```

**补充清单**
- `useInfiniteQuery` 游标分页
- `useQueries` 并行查询
- `prefetchQuery` 预取数据
- 鼠标悬停预取详情

---

### 7. 表单验证高级特性
**为什么前期不用**
- 异步唯一性检查需要额外API接口，前期表单字段简单不需要
- 条件验证增加Schema复杂度
- 跨字段验证（如密码确认）前期手动处理即可

**后期怎么补**
```typescript
// 异步唯一性验证
export const uniqueUsername = z.string().refine(
  async (value) => {
    if (!value) return true
    const exists = await checkUsername(value)
    return !exists
  },
  { message: '该用户名已被使用' }
)

// 条件验证
const validationSchema = computed(() => z.object({
  cardNumber: paymentMethod.value === 'card'
    ? z.string().min(1, '请输入银行卡号')
    : z.string().optional(),
}))

// 跨字段验证
export const passwordConfirm = (getPassword: () => string) =>
  z.string().refine(v => v === getPassword(), { message: '两次密码不一致' })
```

**补充清单**
- Zod `refine` 异步唯一性检查
- `computed` 动态 Schema
- 跨字段 `refine` 验证
- 服务端错误映射 `setFieldError`

---

### 8. 全面铺开的Suspense+骨架屏
**为什么前期不用**
- 要构造各种骨架屏UI样式
- 前期外层包个简单v-loading就够用
- 现在我们已经有Suspense+Loader2了，比骨架屏更简洁
- 可后期给特定页面加骨架屏增强体验

**后期怎么补**
```vue
<template>
  <Suspense>
    <template #default>
      <SongList />
    </template>
    <template #fallback>
      <div class="skeleton-grid">
        <SkeletonCard v-for="i in 8" :key="i" />
      </div>
    </template>
  </Suspense>
</template>
```

**补充清单**
- 各类型骨架屏组件
- 动画效果优化
- 骨架屏与真实数据高度对齐

---

## 第三类：性能优化（前期用不到）

### 9. 长列表虚拟滚动
**为什么前期不用**
- 数据库里可能连50条测试数据都没有
- 虚拟滚动增加代码复杂度，还容易引发布局Bug
- 前期直接v-for足够流畅

**后期怎么补**
```vue
<template>
  <VirtualList
    :data-sources="songs"
    :data-key="'id'"
    :item-height="120"
    :item="SongCard"
  />
</template>
```

**补充清单**
- 选择虚拟滚动方案(VueUse vs shadcn-vue vs 自研)
- 处理动态高度
- 处理大量数据加载
- 性能测试与优化

---

## V2.0 实施建议

### 实施优先级
- **P0 上线前必须**: Sentry错误监控、API请求加密
- **P1 扩张前完成**: 权限控制系统、严格的Git提交格式、虚拟滚动(数据量大时)
- **P2 体验优化**: 乐观更新、TanStack Query高级模式、表单验证高级特性、骨架屏(可选)

### 实施流程
1. 先确认当前阶段不需要这些功能
2. 建立标记，记录哪些文件已预留扩展点
3. 项目上线前或团队达10人以上时，评估逐个补充
4. 补充后保持V1与V2的平滑迁移

### 向后兼容
- 所有后期补充功能都必须设计成**可选开关**
- 默认关闭，不影响现有代码运行
- 开启时有明确的开启条件和配置项
