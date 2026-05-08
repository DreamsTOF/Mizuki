/**
 * 格式化工具函数
 *
 * 纯函数，无副作用，提供完整 TypeScript 类型。
 */

// ============================================================================
// 时长格式化
// ============================================================================

/**
 * 将秒数格式化为 mm:ss 或 hh:mm:ss 格式
 *
 * @example
 * formatDuration(65)   // "01:05"
 * formatDuration(3661) // "1:01:01"
 */
export function formatDuration(seconds: number): string {
  if (!Number.isFinite(seconds) || seconds < 0) return '00:00'

  const totalSeconds = Math.floor(seconds)
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const secs = totalSeconds % 60

  const mm = String(minutes).padStart(2, '0')
  const ss = String(secs).padStart(2, '0')

  if (hours > 0) {
    return `${hours}:${mm}:${ss}`
  }
  return `${mm}:${ss}`
}

// ============================================================================
// 日期格式化
// ============================================================================

/**
 * 格式化日期为本地化字符串
 *
 * @example
 * formatDate('2024-01-15T00:00:00Z') // "2024年1月15日"
 * formatDate(new Date())             // 根据系统语言
 */
export function formatDate(date: Date | string | undefined | null): string {
  if (!date) return '未知日期'

  const d = typeof date === 'string' ? new Date(date) : date
  if (Number.isNaN(d.getTime())) return '无效日期'

  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

// ============================================================================
// 语言归一化
// ============================================================================

/** 预设语言列表 */
const LANGUAGES = ['中', '英', '日', '韩'] as const

/** 语言关键词映射 */
const LANGUAGE_MAP: Record<string, string> = {
  中文: '中',
  汉语: '中',
  国语: '中',
  普通话: '中',
  粤语: '中',
  英文: '英',
  英语: '英',
  英文歌: '英',
  日文: '日',
  日语: '日',
  韩文: '韩',
  韩语: '韩',
  朝鲜语: '韩',
}

/**
 * 语言归一化：将各种语言名称映射为单字标识
 *
 * @example
 * normalizeLanguage('中文') // "中"
 * normalizeLanguage('英语') // "英"
 * normalizeLanguage('中')   // "中"（已标准化，直接返回）
 */
export function normalizeLanguage(lang: string): string {
  if (!lang) return '未知'

  const trimmed = lang.trim()

  // 先检查是否已经是标准单字
  if (LANGUAGES.includes(trimmed as (typeof LANGUAGES)[number])) {
    return trimmed
  }

  // 模糊匹配：检查是否包含已知语言关键词
  for (const [key, value] of Object.entries(LANGUAGE_MAP)) {
    if (trimmed.includes(key)) {
      return value
    }
  }

  // 未知语言，返回原文
  return trimmed
}

// ============================================================================
// 拼音首字母
// ============================================================================

/**
 * 获取拼音首字母
 *
 * @example
 * formatPinyinInitial('zhongwen') // "z"
 * formatPinyinInitial('ABC')      // "a"
 */
export function formatPinyinInitial(pinyin: string): string {
  if (!pinyin) return ''

  const trimmed = pinyin.trim()
  if (trimmed.length === 0) return ''

  const first = trimmed.charAt(0).toLowerCase()

  if (/^[a-z]$/.test(first)) {
    return first
  }

  return trimmed
}
