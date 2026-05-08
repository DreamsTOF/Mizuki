import { h, render } from 'vue'
import { AlertTriangle, Info, XCircle } from 'lucide-vue-next'

interface ConfirmOptions {
  message: string
  title?: string
  type?: 'warning' | 'info' | 'error'
}

function createConfirmDialog(options: ConfirmOptions): Promise<boolean> {
  const { message, title = '操作确认', type = 'warning' } = options

  return new Promise((resolve) => {
    const container = document.createElement('div')
    document.body.appendChild(container)

    const iconMap = {
      warning: AlertTriangle,
      info: Info,
      error: XCircle,
    }

    const iconColorMap = {
      warning: '#F59E0B',
      info: '#0071e3',
      error: '#EF4444',
    }

    const Icon = iconMap[type]
    const iconColor = iconColorMap[type]

    const vnode = h(
      'div',
      {
        class: 'confirm-overlay',
        onClick: (e: MouseEvent) => {
          if (e.target === e.currentTarget) {
            cleanup()
            resolve(false)
          }
        },
      },
      [
        h(
          'div',
          {
            class: 'confirm-dialog',
          },
          [
            h('div', { class: 'confirm-body' }, [
              h(Icon, {
                class: 'confirm-icon',
                size: 22,
                color: iconColor,
              }),
              h('div', { class: 'confirm-text' }, [
                h('h3', { class: 'confirm-title' }, title),
                h('p', { class: 'confirm-message' }, message),
              ]),
            ]),
            h('div', { class: 'confirm-actions' }, [
              h(
                'button',
                {
                  class: 'confirm-btn cancel',
                  onClick: () => {
                    cleanup()
                    resolve(false)
                  },
                },
                '取消'
              ),
              h(
                'button',
                {
                  class: `confirm-btn confirm ${type}`,
                  onClick: () => {
                    cleanup()
                    resolve(true)
                  },
                },
                '确定'
              ),
            ]),
          ]
        ),
      ]
    )

    function cleanup() {
      render(null, container)
      document.body.removeChild(container)
    }

    render(vnode, container)

    // 注入内联样式
    if (!document.getElementById('confirm-dialog-styles')) {
      const style = document.createElement('style')
      style.id = 'confirm-dialog-styles'
      style.textContent = `
        .confirm-overlay {
          position: fixed;
          inset: 0;
          z-index: 9999;
          display: flex;
          align-items: center;
          justify-content: center;
          background: rgba(0, 0, 0, 0.5);
          backdrop-filter: blur(4px);
          -webkit-backdrop-filter: blur(4px);
        }
        .confirm-dialog {
          width: 100%;
          max-width: 380px;
          margin: 0 16px;
          background: var(--sk-dark-surface-2);
          border-radius: var(--sk-radius-large);
          box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
          border: 1px solid rgba(255, 255, 255, 0.1);
          padding: 24px;
          animation: confirm-in 0.2s ease-out;
        }
        @keyframes confirm-in {
          from { opacity: 0; transform: scale(0.95) translateY(8px); }
          to { opacity: 1; transform: scale(1) translateY(0); }
        }
        .confirm-body {
          display: flex;
          align-items: flex-start;
          gap: 16px;
        }
        .confirm-icon {
          flex-shrink: 0;
          margin-top: 2px;
        }
        .confirm-text {
          flex: 1;
        }
        .confirm-title {
          font-size: var(--sk-text-body);
          font-family: var(--sk-font-display);
          font-weight: 600;
          color: var(--sk-text-primary);
          margin: 0;
        }
        .confirm-message {
          font-size: var(--sk-text-micro);
          font-family: var(--sk-font-text);
          color: var(--sk-text-secondary);
          margin-top: 8px;
          line-height: var(--sk-lh-body);
        }
        .confirm-actions {
          display: flex;
          justify-content: flex-end;
          gap: 12px;
          margin-top: 24px;
        }
        .confirm-btn {
          display: inline-flex;
          align-items: center;
          justify-content: center;
          height: 36px;
          padding: 0 16px;
          font-size: var(--sk-text-micro);
          font-family: var(--sk-font-text);
          font-weight: 500;
          border-radius: var(--sk-radius-standard);
          cursor: pointer;
          transition: background-color 0.2s ease;
          border: none;
        }
        .confirm-btn.cancel {
          color: var(--sk-text-primary);
          background: rgba(255, 255, 255, 0.1);
        }
        .confirm-btn.cancel:hover {
          background: rgba(255, 255, 255, 0.15);
        }
        .confirm-btn.confirm {
          color: #FFFFFF;
        }
        .confirm-btn.confirm.warning {
          background: #F59E0B;
        }
        .confirm-btn.confirm.warning:hover {
          background: #D97706;
        }
        .confirm-btn.confirm.info {
          background: var(--sk-apple-blue);
        }
        .confirm-btn.confirm.info:hover {
          background: #0077ED;
        }
        .confirm-btn.confirm.error {
          background: #EF4444;
        }
        .confirm-btn.confirm.error:hover {
          background: #DC2626;
        }
      `
      document.head.appendChild(style)
    }
  })
}

export const useConfirm = () => {
  const confirm = async (
    message: string,
    title = '操作确认',
    type: 'warning' | 'info' | 'error' = 'warning'
  ) => {
    return createConfirmDialog({ message, title, type })
  }

  const confirmDelete = (itemName = '') => {
    return confirm(
      `确定要删除 ${itemName ? `"${itemName}"` : '该项目'} 吗？此操作不可撤销。`,
      '安全警告',
      'error'
    )
  }

  return { confirm, confirmDelete }
}
