---
alwaysApply: false
description: 编写表单校验时触发
---
# 表单验证参考

> 本文是 [skill.md](../skill.md) 的补充，覆盖 vee-validate + zod 的标准使用模式。

## 标准模式

Zod schema 直接定义在 `.vue` 文件的 `<script setup>` 中，无需单独建文件：

```vue
<script setup lang="ts">
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import { z } from 'zod'

const formSchema = toTypedSchema(z.object({
  title: z.string().min(1, '请输入歌曲名称').max(100, '歌曲名称最多100个字符'),
  artist: z.string().min(1, '请输入歌手名称').max(50, '歌手名称最多50个字符'),
  duration: z.number().min(0, '时长不能为负数').max(3600, '时长不能超过1小时'),
}))

const { handleSubmit } = useForm({
  validationSchema: formSchema,
})

const onSubmit = handleSubmit(async (values) => {
  await createMutation.mutateAsync(values)
})
</script>

<template>
  <form @submit="onSubmit">
    <Field name="title" v-slot="{ field, errorMessage }">
      <Input v-bind="field" placeholder="歌曲名称" />
      <span v-if="errorMessage" class="text-red-500 text-sm">{{ errorMessage }}</span>
    </Field>
    <Button type="submit" :disabled="createMutation.isPending.value">
      <Loader2 v-if="createMutation.isPending.value" class="animate-spin" />
      提交
    </Button>
  </form>
</template>
```

## 关键规则

1. Zod schema 直接写在 `.vue` 文件里，不单独建 `validators/` 文件
2. 错误消息统一使用中文
3. 提交中按钮 `disabled` + Loader2 图标
4. 成功：`Notify.success()` + invalidateQueries 刷新数据
5. 失败：`Notify.error()` + 保留用户输入（不重置表单）
6. 不要在模板中手写验证逻辑，统一走 vee-validate + Zod
