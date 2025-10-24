// web/src/main.js
import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import '@/resource/global.css'
import ElementUI from 'element-ui'
import request from './utils/request'
import 'element-ui/lib/theme-chalk/index.css'

Vue.config.productionTip = false
Vue.use(ElementUI)

// 全局请求实例
Vue.prototype.request = request

/**
 * 统一覆盖 Element-UI 的 Message 默认配置：
 * - duration: 默认 1200ms（原来 3000ms）
 * - offset:   默认 88px（下移，避免挡住页面导航/按钮）
 * 如需微调，与你的头部高度一致即可（常见 72~96 之间）。
 */
const rawMsg = Vue.prototype.$message // 保存原始实现

Vue.prototype.$message = function (options) {
  if (typeof options === 'string') options = { message: options }
  options = options || {}
  options.duration  = options.duration  != null ? options.duration  : 1200
  options.offset    = options.offset    != null ? options.offset    : 88
  options.showClose = options.showClose != null ? options.showClose : false
  return rawMsg(options)
}

// 覆盖快捷方法：success / warning / info / error
;['success', 'warning', 'info', 'error'].forEach(type => {
  Vue.prototype.$message[type] = (opts) => {
    if (typeof opts === 'string') opts = { message: opts }
    opts.type       = type
    opts.duration   = opts.duration  != null ? opts.duration  : 1200
    opts.offset     = opts.offset    != null ? opts.offset    : 88
    opts.showClose  = opts.showClose != null ? opts.showClose : false
    return rawMsg(opts)
  }
})

// 可选：暴露 closeAll（防止多条堆叠时手动关闭）
if (rawMsg && typeof rawMsg.closeAll === 'function') {
  Vue.prototype.$message.closeAll = rawMsg.closeAll.bind(rawMsg)
}

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
