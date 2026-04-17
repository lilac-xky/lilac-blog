import axios from 'axios';
import { message } from 'ant-design-vue';

const request = axios.create({
    baseURL: 'http://localhost:9090',
    timeout: 60000,
    withCredentials: true,
});

// 请求拦截器
request.interceptors.request.use(
    (config) => {
        // todo 添加token
        return config;
    },
    (error) => Promise.reject(error)
);

// 响应拦截器
request.interceptors.response.use(
    (response) => {
        const { data } = response;
        if (data.code !== 200) {
            message.error(data.msg || '请求出错');
            return Promise.reject(data);
        }
        return response;
    },
);

export default request;