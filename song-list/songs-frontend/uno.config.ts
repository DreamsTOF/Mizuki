import { defineConfig } from 'unocss'
import presetWind from '@unocss/preset-wind'
import presetAttributify from '@unocss/preset-attributify'
import presetIcons from '@unocss/preset-icons'
import transformerDirectives from '@unocss/transformer-directives'
import transformerVariantGroup from '@unocss/transformer-variant-group'

export default defineConfig({
  presets: [
    presetWind(),
    presetAttributify(),
    presetIcons({
      scale: 1.2,
      warn: true,
    }),
  ],
  transformers: [
    transformerDirectives(),
    transformerVariantGroup(),
  ],
  theme: {
    colors: {
      brand: {
        magenta: '#ef2cc1',
        orange: '#fc4c02',
      },
      'dark-blue': '#010120',
      'soft-lavender': '#bdbbff',
    },
    fontFamily: {
      sans: ['DM Sans', 'system-ui', '-apple-system', 'Segoe UI', 'Roboto', 'Ubuntu', 'Cantarell', 'Noto Sans', 'Helvetica', 'Arial', 'sans-serif'],
      mono: ['Space Mono', 'Courier New', 'Consolas', 'monospace'],
    },
    borderRadius: {
      sm: '4px',
      md: '4px',
      lg: '8px',
      xl: '8px',
    },
  },
  rules: [],
  shortcuts: {},
})
