# UI 交互规范

## 消息提示
统一使用二次封装的`notify`工具，支持success/error/warning/info四种类型。

## 确认框
统一使用`useConfirm`Hook处理确认操作，避免直接调用ElMessageBox。

## 空状态
统一使用`BaseEmpty`组件展示空状态，禁止自行编写空状态UI。

## 加载状态分级
| 级别 | 适用场景 | 实现方式 |
|------|----------|----------|
| 页面级 | 路由跳转、应用初始化 | NProgress进度条 |
| 块级 | 列表、详情页数据加载 | el-skeleton骨架屏 |
| 组件级 | 表单提交、按钮操作 | v-loading指令 |

## 异步状态要求
所有异步操作必须处理四种状态：
1. Loading：显示对应加载指示器
2. Success：展示实际数据
3. Empty：显示BaseEmpty空状态
4. Error：显示错误提示和重试按钮
