import axios from 'axios';
import { message } from 'ant-design-vue';
import { useUserStore } from '@/stores/user';
import router from '@/router';

const request = axios.create({
    baseURL: 'http://localhost:9090',
    timeout: 60000,
    withCredentials: true,
});

// 请求拦截器：注入 admin-token
request.interceptors.request.use(
    (config) => {
        const token = useUserStore().loginUser?.token;
        if (token) {
            config.headers.set('admin-token', token);
        }
        return config;
    },
    (error) => Promise.reject(error)
);

const AUTH_FAIL_CODES = new Set([401, 400001]);

// 处理认证失败：清除用户信息并跳转到登录页
function handleAuthFail() {
    useUserStore().clearLoginUser();
    if (router.currentRoute.value.path !== '/login') {
        router.push({
            path: '/login',
            query: { redirect: router.currentRoute.value.fullPath },
        });
    }
}

// 响应拦截器
request.interceptors.response.use(
    (response) => {
        const { data, config } = response;
        if (data.code !== 200) {
            if (AUTH_FAIL_CODES.has(data.code)) handleAuthFail();
            if (!(config as any)?.silentError) {
                message.error(data.msg || '请求出错');
            }
            return Promise.reject(data);
        }
        return response;
    },
    (error) => {
        const config = error.config;
        const data = error.response?.data;
        const code = data?.code;
        const msg = data?.msg || error.message || '网络请求出错';
        if (code && AUTH_FAIL_CODES.has(code)) handleAuthFail();
        if (!(config as any)?.silentError) {
            message.error(msg);
        }
        return Promise.reject(error);
    }
);

export default request;
