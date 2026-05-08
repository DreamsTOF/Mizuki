/**
 * 项目常量配置
 *
 * 集中管理业务常量，避免魔法数字散落在代码中。
 */

// ============================================================================
// 歌曲状态
// ============================================================================

/** 歌曲状态枚举 */
export const SONG_STATUS = {
  AVAILABLE: 1,
  LEARNING: 2,
  UNAVAILABLE: 3,
} as const

/** 歌曲状态文字映射 */
export const SONG_STATUS_TEXT: Record<number, string> = {
  [SONG_STATUS.AVAILABLE]: '可点',
  [SONG_STATUS.LEARNING]: '学习中',
  [SONG_STATUS.UNAVAILABLE]: '暂不可点',
}

/** 歌曲状态对应的 CSS 颜色变量 */
export const SONG_STATUS_COLOR: Record<number, string> = {
  [SONG_STATUS.AVAILABLE]: 'var(--sk-color-success)',
  [SONG_STATUS.LEARNING]: 'var(--sk-color-warning)',
  [SONG_STATUS.UNAVAILABLE]: 'var(--sk-color-danger)',
}

// ============================================================================
// 付费等级
// ============================================================================

/** 付费等级映射 */
export const PAID_LEVELS: Record<number, string> = {
  0: '免费',
  1: '初级',
  2: '中级',
  3: '高级',
}

// ============================================================================
// 点歌规则
// ============================================================================

/** 点歌规则映射 */
export const REQUEST_RULES: Record<number, string> = {
  0: '无',
  1: '需关注',
  2: '需粉丝牌',
  3: '需舰长及以上',
  4: '需提督及以上',
  5: '需总督及以上',
  6: '需付费',
}

// ============================================================================
// 预设语言
// ============================================================================

/** 预设语言列表 */
export const LANGUAGES = ['中', '英', '日', '韩'] as const

// ============================================================================
// 预设曲风
// ============================================================================

/** 预设曲风列表 */
export const MUSIC_STYLES = [
  '流行',
  '摇滚',
  '民谣',
  '电子',
  '古典',
  '说唱',
  '爵士',
  'R&B',
  '国风',
  '轻音乐',
] as const

/** 曲风筛选选项（用于下拉选择） */
export const STYLE_OPTIONS = [...MUSIC_STYLES] as readonly string[]

// ============================================================================
// 排序方式
// ============================================================================

/** 排序方式枚举 */
export const SORT_OPTIONS = {
  CREATE_TIME: 'CREATE_TIME',
  NAME: 'NAME',
  TOTAL_CLICKS: 'TOTAL_CLICKS',
} as const

/** 排序方式文字映射 */
export const SORT_OPTIONS_TEXT: Record<string, string> = {
  [SORT_OPTIONS.CREATE_TIME]: '按时间',
  [SORT_OPTIONS.NAME]: '按名称',
  [SORT_OPTIONS.TOTAL_CLICKS]: '按热度',
}
