import { createApp } from 'vue'
import { createPinia } from 'pinia'
import antd from 'ant-design-vue'
import App from './App.vue'
import router from './router'

// 创建 Vue 应用并挂载全局插件
const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(antd)

app.mount('#app')
