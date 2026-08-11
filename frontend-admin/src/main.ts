import { createApp } from 'vue'
import { createPinia } from 'pinia'
import antd from 'ant-design-vue'
import App from './App.vue'

import router from './router'
import './styles/index.css'
import { permissionDirective } from './directives/permission'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(antd)

// 注册全局权限指令
app.directive('permission', permissionDirective)

app.mount('#app')
