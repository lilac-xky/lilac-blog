import axios from 'axios';
import { message } from 'ant-design-vue';
import { useUserStore } from '@/stores/user';

const request = axios.create({
    baseURL: 'http://localhost:9090',
    timeout: 60000,
    withCredentials: true,
});

request.interceptors.request.use(
    (config) => {
        const token = useUserStore().loginUser?.token;
        if (token) {
            config.headers.set('token', token);
        }
        return config;
    },
    (error) => Promise.reject(error)
);

const AUTH_FAIL_CODES = new Set([401, 400001]);

request.interceptors.response.use(
    (response) => {
        const { data } = response;
        if (data.code !== 200) {
            if (AUTH_FAIL_CODES.has(data.code)) {
                useUserStore().clearLoginUser();
            }
            message.error(data.msg || '请求出错');
            return Promise.reject(data);
        }
        return response;
    },
);

export default request;
