<template>
    <div class="user-center">
        <!-- 顶部资料卡 -->
        <section class="profile-card glass-card">
            <a-avatar :size="80" :src="userStore.loginUser?.avatar || undefined" class="profile-avatar">
                <template #icon>
                    <UserOutlined />
                </template>
            </a-avatar>
            <div class="profile-info">
                <h2 class="profile-name">
                    {{ userStore.loginUser?.username || userStore.loginUser?.userAccount || '游客' }}
                </h2>
                <p class="profile-meta">
                    <MailOutlined />
                    {{ userStore.loginUser?.email || '—' }}
                </p>
            </div>
            <router-link to="/user/write" class="profile-cta">
                <EditOutlined />
                <span>开始投稿</span>
            </router-link>
        </section>

        <!-- 统计卡 -->
        <section class="stat-grid">
            <div class="stat-card glass-card" v-for="s in stats" :key="s.label">
                <div class="stat-icon" :style="{ color: s.color }">
                    <component :is="s.icon" />
                </div>
                <div class="stat-body">
                    <div class="stat-num">{{ s.value }}</div>
                    <div class="stat-label">{{ s.label }}</div>
                </div>
            </div>
        </section>

        <!-- 快捷入口 -->
        <section class="entry-grid">
            <router-link to="/user/articles" class="entry-card glass-card">
                <FileTextOutlined class="entry-icon" />
                <div>
                    <div class="entry-title">我的文章</div>
                    <div class="entry-desc">查看草稿、待审核与已发布的文章</div>
                </div>
            </router-link>
            <router-link to="/user/messages" class="entry-card glass-card">
                <a-badge :count="unreadCount" :offset="[-4, 4]">
                    <MessageOutlined class="entry-icon" />
                </a-badge>
                <div>
                    <div class="entry-title">消息中心</div>
                    <div class="entry-desc">审核结果与系统通知</div>
                </div>
            </router-link>
            <router-link to="/user/write" class="entry-card glass-card">
                <EditOutlined class="entry-icon" />
                <div>
                    <div class="entry-title">写文章</div>
                    <div class="entry-desc">用 Markdown 撰写并提交审核</div>
                </div>
            </router-link>
            <!-- 修改密码：点击打开弹窗，通过旧密码校验后修改 -->
            <div class="entry-card glass-card" @click="openPasswordModal">
                <LockOutlined class="entry-icon" />
                <div>
                    <div class="entry-title">修改密码</div>
                    <div class="entry-desc">通过旧密码验证后设置新密码</div>
                </div>
            </div>
        </section>

        <!-- 修改密码弹窗 -->
        <a-modal v-model:open="passwordModalOpen" title="修改密码" :confirm-loading="passwordLoading" ok-text="确认修改"
            cancel-text="取消" @ok="handleUpdatePassword" @cancel="resetPasswordForm">
            <a-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" layout="vertical">
                <a-form-item label="旧密码" name="oldPassword">
                    <a-input-password v-model:value="passwordForm.oldPassword" placeholder="请输入当前密码" allow-clear />
                </a-form-item>
                <a-form-item label="新密码" name="newPassword">
                    <a-input-password v-model:value="passwordForm.newPassword" placeholder="6-20 位新密码" allow-clear />
                </a-form-item>
                <a-form-item label="确认新密码" name="checkPassword">
                    <a-input-password v-model:value="passwordForm.checkPassword" placeholder="请再次输入新密码" allow-clear />
                </a-form-item>
            </a-form>
        </a-modal>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, h } from 'vue';
import { message } from 'ant-design-vue';
import type { FormInstance, Rule } from 'ant-design-vue/es/form';
import {
    UserOutlined,
    MailOutlined,
    EditOutlined,
    LockOutlined,
    FileTextOutlined,
    MessageOutlined,
    CheckCircleOutlined,
    ClockCircleOutlined,
    FileDoneOutlined,
} from '@ant-design/icons-vue';
import { useUserStore } from '@/stores/user';
import { listMyArticles } from '@/api/articleController';
import { getUnreadCount } from '@/api/messageController';
import { updatePassword } from '@/api/userController';

const userStore = useUserStore();

// 修改密码弹窗状态
const passwordModalOpen = ref(false);
const passwordLoading = ref(false);
const passwordFormRef = ref<FormInstance>();
const passwordForm = reactive({
    oldPassword: '',
    newPassword: '',
    checkPassword: '',
});

// 确认新密码校验：必须与新密码一致
const validateCheckPassword = async (_rule: Rule, value: string) => {
    if (!value) {
        return Promise.reject('请再次输入新密码');
    }
    if (value !== passwordForm.newPassword) {
        return Promise.reject('两次输入的密码不一致');
    }
    return Promise.resolve();
};

const passwordRules: Record<string, Rule[]> = {
    oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
    newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度需在 6-20 个字符之间', trigger: 'blur' },
    ],
    checkPassword: [{ required: true, validator: validateCheckPassword, trigger: 'blur' }],
};

// 重置并关闭弹窗
function resetPasswordForm() {
    passwordForm.oldPassword = '';
    passwordForm.newPassword = '';
    passwordForm.checkPassword = '';
    passwordFormRef.value?.clearValidate();
}

function openPasswordModal() {
    resetPasswordForm();
    passwordModalOpen.value = true;
}

// 提交修改密码
async function handleUpdatePassword() {
    try {
        await passwordFormRef.value?.validate();
    } catch {
        return;
    }
    passwordLoading.value = true;
    try {
        const res = await updatePassword({
            oldPassword: passwordForm.oldPassword,
            newPassword: passwordForm.newPassword,
            checkPassword: passwordForm.checkPassword,
        });
        if (res.data?.data) {
            message.success('密码修改成功');
            passwordModalOpen.value = false;
            resetPasswordForm();
        }
    } catch (err) {
        // 错误提示已由请求拦截器统一处理
    } finally {
        passwordLoading.value = false;
    }
}

const draftCount = ref(0);
const auditCount = ref(0);
const publishCount = ref(0);
const unreadCount = ref(0);

async function fetchCount(status: number, ref: { value: number }) {
    try {
        const res = await listMyArticles(
            { current: 1, pageSize: 1, status },
            { silentError: true },
        );
        ref.value = Number(res.data?.data?.total ?? 0);
    } catch {
        ref.value = 0;
    }
}

async function fetchUnread() {
    try {
        const res = await getUnreadCount({ silentError: true });
        unreadCount.value = Number(res.data?.data ?? 0);
    } catch {
        unreadCount.value = 0;
    }
}

onMounted(() => {
    fetchCount(0, draftCount);
    fetchCount(1, auditCount);
    fetchCount(2, publishCount);
    fetchUnread();
});

const stats = computed(() => [
    { label: '草稿', value: draftCount.value, icon: h(FileDoneOutlined), color: '#94a3b8' },
    { label: '待审核', value: auditCount.value, icon: h(ClockCircleOutlined), color: '#fbbf24' },
    { label: '已发布', value: publishCount.value, icon: h(CheckCircleOutlined), color: '#34d399' },
]);
</script>

<style scoped>
.user-center {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.profile-card {
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 24px 28px;
}

.profile-avatar {
    flex-shrink: 0;
    border: 2px solid var(--border-soft);
}

.profile-info {
    flex: 1;
    min-width: 0;
}

.profile-name {
    font-size: 22px;
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 6px;
}

.profile-meta {
    color: var(--text-secondary);
    font-size: 13px;
    display: flex;
    align-items: center;
    gap: 6px;
}

.profile-cta {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 10px 22px;
    border-radius: var(--radius-pill);
    background: var(--accent);
    color: #fff !important;
    font-weight: 500;
    transition: all 0.2s;
}

.profile-cta:hover {
    background: var(--accent-strong);
    box-shadow: 0 6px 24px rgba(var(--accent-rgb), 0.4);
}

.stat-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
}

.stat-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px 24px;
}

.stat-icon {
    font-size: 32px;
    line-height: 1;
}

.stat-num {
    font-size: 26px;
    font-weight: 700;
    color: var(--text-primary);
    line-height: 1.2;
}

.stat-label {
    font-size: 13px;
    color: var(--text-secondary);
    margin-top: 4px;
}

.entry-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 16px;
}

.entry-card {
    display: flex;
    align-items: center;
    gap: 18px;
    padding: 22px 24px;
    transition: all 0.25s;
    cursor: pointer;
}

.entry-card:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-hover);
}

.entry-icon {
    font-size: 30px;
    color: var(--accent);
}

.entry-title {
    font-size: 15px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 4px;
}

.entry-desc {
    font-size: 12px;
    color: var(--text-secondary);
}

@media (max-width: 720px) {
    .stat-grid {
        grid-template-columns: repeat(3, 1fr);
        gap: 8px;
    }

    .stat-card {
        flex-direction: column;
        text-align: center;
        gap: 4px;
        padding: 12px;
    }

    .profile-card {
        flex-direction: column;
        text-align: center;
    }
}
</style>
