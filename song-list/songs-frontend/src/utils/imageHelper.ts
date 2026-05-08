/**
 * 生成带文字和颜色的 SVG data URI 占位图
 * @param text - 显示在图片上的文字
 * @param color - 背景颜色（十六进制）
 * @param width - 图片宽度，默认 300
 * @param height - 图片高度，默认 300
 * @returns SVG data URI 字符串
 */
export function generatePlaceholder(
  text: string,
  color: string = '#6366F1',
  width: number = 300,
  height: number = 300,
): string {
  // 计算合适的字体大小
  const maxTextLength = 8
  const displayText = text.length > maxTextLength ? text.slice(0, maxTextLength) + '…' : text
  const fontSize = Math.min(width / 6, height / 6, 48)

  // 生成暗色/亮色判断，决定文字颜色
  const textColor = getContrastColor(color)

  // 构建 SVG
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}">
    <rect width="100%" height="100%" fill="${color}"/>
    <text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" fill="${textColor}" font-family="system-ui, -apple-system, sans-serif" font-size="${fontSize}" font-weight="600">${escapeXml(displayText)}</text>
  </svg>`

  // 转换为 base64 data URI
  const base64 = btoa(unescape(encodeURIComponent(svg)))
  return `data:image/svg+xml;base64,${base64}`
}

/**
 * 根据背景色判断使用深色还是浅色文字
 */
function getContrastColor(hexColor: string): string {
  const hex = hexColor.replace('#', '')
  const r = parseInt(hex.substring(0, 2), 16)
  const g = parseInt(hex.substring(2, 4), 16)
  const b = parseInt(hex.substring(4, 6), 16)
  const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255
  return luminance > 0.6 ? '#1F2937' : '#FFFFFF'
}

/**
 * 转义 SVG 中的特殊字符
 */
function escapeXml(text: string): string {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}
