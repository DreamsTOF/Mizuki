/**
 * Theme System - 主题系统工具
 *
 * 支持两套主题 × 两种模式的运行时动态切换
 * - 主题：together | mint
 * - 模式：dark | light
 *
 * 切换方式：
 * 1. 设置 document.documentElement 的 data-theme 和 data-mode 属性
 * 2. CSS 变量通过 [data-theme][data-mode] 选择器自动生效
 */

export type ThemeName = 'together' | 'mint'
export type ModeName = 'dark' | 'light'

export interface ThemeConfig {
  name: ThemeName
  displayName: string
  cssFile: string
}

export const THEMES: Record<ThemeName, ThemeConfig> = {
  together: { name: 'together', displayName: 'Together', cssFile: '' },
  mint: { name: 'mint', displayName: '清新薄荷', cssFile: '' },
}

const THEME_STORAGE_KEY = 'app-theme'
const MODE_STORAGE_KEY = 'app-mode'

/**
 * 获取当前主题（优先 localStorage，其次环境变量，默认 together）
 */
export function getCurrentTheme(): ThemeName {
  const stored = localStorage.getItem(THEME_STORAGE_KEY) as ThemeName | null
  if (stored && THEMES[stored]) return stored

  const envTheme = import.meta.env.VITE_APP_THEME as ThemeName | undefined
  if (envTheme && THEMES[envTheme]) return envTheme

  return 'together'
}

/**
 * 获取当前模式（优先 localStorage，其次系统偏好，默认 dark）
 */
export function getCurrentMode(): ModeName {
  const stored = localStorage.getItem(MODE_STORAGE_KEY) as ModeName | null
  if (stored === 'light' || stored === 'dark') return stored

  const envMode = import.meta.env.VITE_APP_MODE as ModeName | undefined
  if (envMode === 'light' || envMode === 'dark') return envMode

  // 检测系统偏好
  if (window.matchMedia && window.matchMedia('(prefers-color-scheme: light)').matches) {
    return 'light'
  }

  return 'dark'
}

/**
 * 设置主题
 */
export function setTheme(themeName: ThemeName): void {
  if (!THEMES[themeName]) {
    console.warn(`[Theme] Unknown theme: ${themeName}, falling back to together`)
    themeName = 'together'
  }

  localStorage.setItem(THEME_STORAGE_KEY, themeName)
  document.documentElement.setAttribute('data-theme', themeName)
  console.log(`[Theme] Switched to ${THEMES[themeName].displayName}`)
}

/**
 * 设置模式
 */
export function setMode(modeName: ModeName): void {
  localStorage.setItem(MODE_STORAGE_KEY, modeName)
  document.documentElement.setAttribute('data-mode', modeName)
  console.log(`[Theme] Mode switched to ${modeName}`)
}

/**
 * 同时设置主题和模式
 */
export function setThemeAndMode(themeName: ThemeName, modeName: ModeName): void {
  setTheme(themeName)
  setMode(modeName)
}

/**
 * 切换模式（dark <-> light）
 */
export function toggleMode(): ModeName {
  const current = getCurrentMode()
  const next = current === 'dark' ? 'light' : 'dark'
  setMode(next)
  return next
}

/**
 * 初始化主题系统
 */
export function initTheme(): void {
  const theme = getCurrentTheme()
  const mode = getCurrentMode()

  document.documentElement.setAttribute('data-theme', theme)
  document.documentElement.setAttribute('data-mode', mode)

  console.log(`[Theme] Initialized: ${THEMES[theme].displayName} (${mode})`)
}

/**
 * 监听系统主题变化
 */
export function watchSystemTheme(callback?: (isDark: boolean) => void): () => void {
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')

  const handler = (e: MediaQueryListEvent) => {
    callback?.(e.matches)
  }

  mediaQuery.addEventListener('change', handler)

  return () => {
    mediaQuery.removeEventListener('change', handler)
  }
}

/**
 * 获取所有可用主题列表
 */
export function getAvailableThemes(): ThemeConfig[] {
  return Object.values(THEMES)
}

/**
 * 获取当前主题和模式信息
 */
export function getThemeInfo(): { theme: ThemeName; mode: ModeName; displayName: string } {
  const theme = getCurrentTheme()
  const mode = getCurrentMode()
  return { theme, mode, displayName: THEMES[theme].displayName }
}
