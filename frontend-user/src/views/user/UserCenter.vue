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

        <!-- 统计卡：点击后跳转「我的文章」并自动选中对应状态的 Tab -->
        <section class="stat-grid">
            <router-link v-for="s in stats" :key="s.label" :to="{ path: '/user/articles', query: { tab: s.tab } }"
                class="stat-card glass-card">
                <div class="stat-icon" :style="{ color: s.color }">
                    <component :is="s.icon" />
                </div>
                <div class="stat-body">
                    <div class="stat-num">{{ s.value }}</div>
                    <div class="stat-label">{{ s.label }}</div>
                </div>
                <RightOutlined class="stat-arrow" />
            </router-link>
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
            <!-- 修改个人信息：修改昵称与头像，头像走文件上传拿外链 -->
            <div class="entry-card glass-card" @click="openProfileModal">
                <IdcardOutlined class="entry-icon" />
                <div>
                    <div class="entry-title">修改个人信息</div>
                    <div class="entry-desc">更新昵称与头像后立即生效</div>
                </div>
            </div>
            <!-- 修改邮箱：单独走 updateEmail 接口，与个人信息解耦 -->
            <div class="entry-card glass-card" @click="openEmailModal">
                <MailOutlined class="entry-icon" />
                <div>
                    <div class="entry-title">修改邮箱</div>
                    <div class="entry-desc">更换接收审核通知与找回密码的邮箱</div>
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

        <!-- 修改个人信息弹窗：昵称 + 头像，提交后同步刷新本地登录态 -->
        <a-modal v-model:open="profileModalOpen" title="修改个人信息" :confirm-loading="profileLoading" ok-text="保存"
            cancel-text="取消" @ok="handleUpdateProfile" @cancel="resetProfileForm">
            <div class="avatar-editor">
                <a-avatar :size="64" :src="profileForm.avatar || undefined" class="avatar-preview">
                    <template #icon>
                        <UserOutlined />
                    </template>
                </a-avatar>
                <div class="avatar-actions">
                    <a-button size="small" :loading="avatarUploading" @click="triggerAvatarUpload">
                        <UploadOutlined />
                        上传头像
                    </a-button>
                    <a-button v-if="profileForm.avatar" size="small" type="link" danger @click="clearAvatar">
                        恢复默认
                    </a-button>
                    <p class="avatar-hint">支持 jpg / png / webp，不超过 5MB</p>
                </div>
                <!-- 隐藏的文件输入，由「上传头像」按钮以编程方式触发 -->
                <input ref="avatarInputRef" type="file" accept="image/png,image/jpeg,image/jpg,image/webp"
                    class="avatar-file-input" @change="onAvatarChange" />
            </div>
            <a-form ref="profileFormRef" :model="profileForm" :rules="profileRules" layout="vertical">
                <a-form-item label="昵称" name="username">
                    <a-input v-model:value="profileForm.username" placeholder="请输入昵称" :maxlength="20" allow-clear />
                </a-form-item>
            </a-form>
        </a-modal>

        <!-- 修改邮箱弹窗 -->
        <a-modal v-model:open="emailModalOpen" title="修改邮箱" :confirm-loading="emailLoading" ok-text="确认修改"
            cancel-text="取消" @ok="handleUpdateEmail" @cancel="resetEmailForm">
            <a-form ref="emailFormRef" :model="emailForm" :rules="emailRules" layout="vertical">
                <a-form-item label="当前邮箱">
                    <a-input :value="userStore.loginUser?.email || '未绑定'" disabled />
                </a-form-item>
                <a-form-item label="新邮箱" name="email">
                    <a-input v-model:value="emailForm.email" placeholder="请输入新的邮箱地址" allow-clear />
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
    IdcardOutlined,
    UploadOutlined,
    RightOutlined,
} from '@ant-design/icons-vue';
import { useUserStore } from '@/stores/user';
import { listMyArticles } from '@/api/articleController';
import { getUnreadCount } from '@/api/messageController';
import { updatePassword, updateUser, updateUserEmail } from '@/api/userController';
import { uploadFile } from '@/api/fileController';

const userStore = useUserStore();

// 头像上传相关：隐藏 input 的引用与上传中状态
const avatarInputRef = ref<HTMLInputElement | null>(null);
const avatarUploading = ref(false);

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

// ---------- 修改个人信息 ----------
// 弹窗开关、提交中状态与表单实例
const profileModalOpen = ref(false);
const profileLoading = ref(false);
const profileFormRef = ref<FormInstance>();
const profileForm = reactive({
    username: '',
    avatar: '',
});

// 昵称仅要求非空并限制最大长度，头像可留空（留空即恢复默认头像）
const profileRules: Record<string, Rule[]> = {
    username: [
        { required: true, message: '请输入昵称', trigger: 'blur' },
        { max: 20, message: '昵称最多 20 个字符', trigger: 'blur' },
    ],
};

/**
 * 重置个人信息表单为当前登录用户的数据
 * 输入：无（读取 userStore.loginUser）
 * 副作用：清空表单校验状态
 */
function resetProfileForm() {
    profileForm.username = userStore.loginUser?.username || '';
    profileForm.avatar = userStore.loginUser?.avatar || '';
    profileFormRef.value?.clearValidate();
}

/**
 * 打开「修改个人信息」弹窗，并在打开前用最新的登录态回填表单
 * 输入：无
 * 副作用：打开弹窗，重置表单
 */
function openProfileModal() {
    resetProfileForm();
    profileModalOpen.value = true;
}

/**
 * 将头像恢复为默认（清空头像地址），保存时即把 avatar 置空写回后端
 * 输入：无
 * 副作用：修改表单中的 avatar 字段
 */
function clearAvatar() {
    profileForm.avatar = '';
}

/**
 * 触发隐藏的 file input 点击，打开系统文件选择框
 * 输入：无
 * 副作用：有副作用，弹出文件选择器
 */
function triggerAvatarUpload() {
    avatarInputRef.value?.click();
}

/**
 * 处理头像文件选择：校验大小与类型后上传，成功后写入表单预览
 * 输入：change 事件（从中取第一个文件）
 * 副作用：调用上传接口，修改 profileForm.avatar
 */
async function onAvatarChange(e: Event) {
    const input = e.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
        return;
    }
    if (file.size > 5 * 1024 * 1024) {
        message.error('图片不能超过 5MB');
        input.value = '';
        return;
    }
    avatarUploading.value = true;
    try {
        const res = await uploadFile({ type: 'avatar' }, {}, file);
        const url = res.data?.data?.url;
        if (url) {
            profileForm.avatar = url;
            message.success('头像上传成功，点击保存后生效');
        }
    } catch {
        // 错误提示已由请求拦截器统一处理
    } finally {
        avatarUploading.value = false;
        // 清空 input 值，否则重复选择同一文件不会再次触发 change
        input.value = '';
    }
}

/**
 * 提交个人信息（昵称 + 头像）
 * 输入：表单中的 username、avatar
 * 副作用：调用 /user/update，成功后把最新资料写回本地登录态
 */
async function handleUpdateProfile() {
    try {
        await profileFormRef.value?.validate();
    } catch {
        return;
    }
    const loginUser = userStore.loginUser;
    if (!loginUser?.id) {
        message.warning('登录状态已失效，请重新登录');
        return;
    }
    profileLoading.value = true;
    try {
        const res = await updateUser({
            id: loginUser.id,
            username: profileForm.username,
            avatar: profileForm.avatar,
        });
        if (res.data?.data) {
            // 后端已刷新会话，这里同步本地 store 让顶栏头像/昵称立即更新
            userStore.setLoginUser({
                ...loginUser,
                username: profileForm.username,
                avatar: profileForm.avatar,
            });
            message.success('个人信息已更新');
            profileModalOpen.value = false;
        }
    } catch {
        // 错误提示已由请求拦截器统一处理
    } finally {
        profileLoading.value = false;
    }
}

// ---------- 修改邮箱 ----------
// 弹窗开关、提交中状态与表单实例
const emailModalOpen = ref(false);
const emailLoading = ref(false);
const emailFormRef = ref<FormInstance>();
const emailForm = reactive({
    email: '',
});

// 新邮箱校验：必填 + 格式（沿用注册页的 type: 'email'）+ 不能与当前邮箱相同
const emailRules: Record<string, Rule[]> = {
    email: [
        { required: true, message: '请输入新邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' },
        {
            validator: async (_rule: Rule, value: string) => {
                // 空值交给 required 与 type 规则处理，这里只拦截「填了但没变」的情况
                if (!value || value === userStore.loginUser?.email) {
                    return value
                        ? Promise.reject('新邮箱不能与当前邮箱相同')
                        : Promise.resolve();
                }
                return Promise.resolve();
            },
            trigger: 'blur',
        },
    ],
};

/**
 * 重置邮箱表单并清空校验提示
 * 输入：无
 * 副作用：清空表单字段与校验状态
 */
function resetEmailForm() {
    emailForm.email = '';
    emailFormRef.value?.clearValidate();
}

/**
 * 打开「修改邮箱」弹窗（先清空上次输入，避免残留脏数据）
 * 输入：无
 * 副作用：打开弹窗，重置表单
 */
function openEmailModal() {
    resetEmailForm();
    emailModalOpen.value = true;
}

/**
 * 提交邮箱修改
 * 输入：表单中的 email
 * 副作用：调用 /user/updateEmail，成功后同步本地登录态
 */
async function handleUpdateEmail() {
    try {
        await emailFormRef.value?.validate();
    } catch {
        return;
    }
    const loginUser = userStore.loginUser;
    if (!loginUser?.id) {
        message.warning('登录状态已失效，请重新登录');
        return;
    }
    emailLoading.value = true;
    try {
        const res = await updateUserEmail({
            id: loginUser.id,
            email: emailForm.email,
        });
        if (res.data?.data) {
            userStore.setLoginUser({ ...loginUser, email: emailForm.email });
            message.success('邮箱修改成功');
            emailModalOpen.value = false;
            resetEmailForm();
        }
    } catch {
        // 错误提示已由请求拦截器统一处理
    } finally {
        emailLoading.value = false;
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

// 三张统计卡的数据源：tab 与「我的文章」页的状态 Tab key 一一对应，供点击跳转使用
const stats = computed(() => [
    { label: '草稿', value: draftCount.value, icon: h(FileDoneOutlined), color: '#94a3b8', tab: '0' },
    { label: '待审核', value: auditCount.value, icon: h(ClockCircleOutlined), color: '#fbbf24', tab: '1' },
    { label: '已发布', value: publishCount.value, icon: h(CheckCircleOutlined), color: '#34d399', tab: '2' },
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

/* 统计卡网格：三列等宽，窄屏沿用三列但压缩间距 */
.stat-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
}

/* 单张统计卡：整卡可点击跳转「我的文章」，hover 时轻微抬升并高亮描边 */
.stat-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px 24px;
    color: inherit;
    transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
}

/* 统计卡悬停态：与快捷入口卡保持一致的交互反馈 */
.stat-card:hover {
    transform: translateY(-2px);
    border-color: var(--border-accent);
    box-shadow: var(--shadow-hover);
}

/* 统计卡右侧箭头：默认淡出，hover 时变亮并右移，暗示可点击 */
.stat-arrow {
    margin-left: auto;
    font-size: 13px;
    color: var(--text-muted);
    opacity: 0;
    transform: translateX(-4px);
    transition: opacity 0.25s ease, transform 0.25s ease, color 0.25s ease;
}

/* 统计卡悬停时点亮箭头 */
.stat-card:hover .stat-arrow {
    opacity: 1;
    transform: translateX(0);
    color: var(--accent);
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

/* 快捷入口网格：自适应列宽，卡片最小 280px 后换行 */
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

/* ---------- 修改个人信息弹窗 ---------- */
/* 头像编辑区：左侧预览、右侧操作按钮，底部对齐 */
.avatar-editor {
    display: flex;
    align-items: center;
    gap: 18px;
    margin-bottom: 20px;
    position: relative;
}

/* 头像预览：圆形裁切 + 细描边，与资料卡头像风格统一 */
.avatar-preview {
    flex-shrink: 0;
    border: 2px solid var(--border-soft);
}

.avatar-actions {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
}

/* 头像上传提示文案：弱化层级，避免抢占按钮注意力 */
.avatar-hint {
    font-size: 12px;
    color: var(--text-muted);
    margin: 0;
}

/* 隐藏原生 file input：仅通过「上传头像」按钮触发，不占布局 */
.avatar-file-input {
    display: none;
}

/* 窄屏适配：统计卡纵向排列，资料卡居中 */
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

    /* 窄屏下箭头会挤占纵向布局，直接隐藏 */
    .stat-arrow {
        display: none;
    }

    .profile-card {
        flex-direction: column;
        text-align: center;
    }
}
</style>
