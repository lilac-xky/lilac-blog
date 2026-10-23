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

        <!-- 修改邮箱弹窗：两步流程，先验证身份拿到凭证，再绑定新邮箱 -->
        <a-modal v-model:open="emailModalOpen" title="修改邮箱" :confirm-loading="emailLoading" width="540px"
            :footer="null" @cancel="resetEmailForm">
            <!-- 步骤指示：让用户清楚当前处于哪一步 -->
            <a-steps :current="emailStep" size="small" class="email-steps">
                <a-step title="验证身份" />
                <a-step title="绑定新邮箱" />
            </a-steps>

            <!-- 第一步：身份验证，原邮箱验证码或当前密码二选一 -->
            <a-form v-if="emailStep === 0" ref="identityFormRef" :model="identityForm" :rules="identityRules"
                layout="vertical">
                <a-form-item label="当前邮箱">
                    <a-input :value="userStore.loginUser?.email || '未绑定'" disabled />
                </a-form-item>

                <a-form-item label="验证方式">
                    <a-radio-group v-model:value="identityForm.method" class="verify-switch"
                        @change="onVerifyMethodChange">
                        <a-radio-button value="email">原邮箱验证码</a-radio-button>
                        <a-radio-button value="password">当前密码</a-radio-button>
                    </a-radio-group>
                </a-form-item>

                <!-- 原邮箱验证码：验证码只发往账号已绑定的邮箱，前端不传目标地址 -->
                <template v-if="identityForm.method === 'email'">
                    <div class="verify-hint">
                        验证码将发送至当前邮箱 {{ userStore.loginUser?.email || '（未绑定）' }}，5 分钟内有效。
                    </div>
                    <a-form-item label="原邮箱验证码" name="originalCode">
                        <div class="code-row">
                            <a-input v-model:value="identityForm.originalCode" placeholder="请输入原邮箱收到的验证码"
                                allow-clear class="code-input" :maxlength="6" />
                            <a-button class="code-btn" :disabled="oldCodeCountdown > 0 || oldCodeSending"
                                :loading="oldCodeSending" @click="handleSendOriginalCode">
                                {{ oldCodeCountdown > 0 ? `${oldCodeCountdown}s 后重试` : '发送验证码' }}
                            </a-button>
                        </div>
                    </a-form-item>
                </template>

                <!-- 当前密码：原邮箱不可用时的兜底通道 -->
                <template v-else>
                    <div class="verify-hint">
                        原邮箱已停用时，可改用当前登录密码验证身份。
                    </div>
                    <a-form-item label="当前密码" name="password">
                        <a-input-password v-model:value="identityForm.password" placeholder="请输入当前登录密码"
                            allow-clear />
                    </a-form-item>
                </template>

                <!-- 第一步底部操作 -->
                <div class="modal-footer">
                    <a-button @click="resetEmailForm">取消</a-button>
                    <a-button type="primary" :loading="identityLoading" @click="handleVerifyIdentity">
                        下一步
                    </a-button>
                </div>
            </a-form>

            <!-- 第二步：身份已验证，填写并验证新邮箱 -->
            <a-form v-else ref="emailFormRef" :model="emailForm" :rules="emailRules" layout="vertical">
                <!-- 身份验证通过提示：让用户明确这一步不再需要重复验证 -->
                <a-alert type="success" show-icon class="verified-alert"
                    message="身份验证已通过" :description="`凭证有效期 5 分钟，请尽快完成新邮箱绑定。`" />

                <a-form-item label="新邮箱" name="email">
                    <a-input v-model:value="emailForm.email" placeholder="请输入新的邮箱地址" allow-clear />
                </a-form-item>

                <a-form-item label="新邮箱验证码" name="code">
                    <div class="code-row">
                        <a-input v-model:value="emailForm.code" placeholder="请输入新邮箱收到的验证码" allow-clear
                            class="code-input" :maxlength="6" />
                        <a-button class="code-btn" :disabled="newCodeCountdown > 0 || newCodeSending"
                            :loading="newCodeSending" @click="handleSendNewEmailCode">
                            {{ newCodeCountdown > 0 ? `${newCodeCountdown}s 后重试` : '发送验证码' }}
                        </a-button>
                    </div>
                </a-form-item>

                <!-- 第二步底部操作：可返回上一步重新验证 -->
                <div class="modal-footer">
                    <a-button @click="backToIdentityStep">上一步</a-button>
                    <a-button type="primary" :loading="emailLoading" @click="handleUpdateEmail">
                        确认修改
                    </a-button>
                </div>
            </a-form>
        </a-modal>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, h } from 'vue';
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
import {
    updatePassword,
    updateUser,
    updateEmailCode,
    updateEmailMethod,
    sendOriginalEmailCode,
    verifyEmailIdentity,
} from '@/api/userController';
import { uploadFile } from '@/api/fileController';

const userStore = useUserStore();

// 头像上传相关：隐藏 input 的引用与上传中状态
const avatarInputRef = ref<HTMLInputElement | null>(null);
const avatarUploading = ref(false);

// 邮箱格式校验：与注册/找回密码页保持同一套简单规则
const EMAIL_PATTERN = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

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

// 重置个人信息表单并清空校验提示
function resetProfileForm() {
    profileForm.username = userStore.loginUser?.username || '';
    profileForm.avatar = userStore.loginUser?.avatar || '';
    profileFormRef.value?.clearValidate();
}

// 打开「修改个人信息」弹窗（先重置表单，避免残留脏数据）
function openProfileModal() {
    resetProfileForm();
    profileModalOpen.value = true;
}

// 清空头像表单字段，恢复默认头像
function clearAvatar() {
    profileForm.avatar = '';
}

// 触发头像文件选择
function triggerAvatarUpload() {
    avatarInputRef.value?.click();
}

// 头像选择后触发
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

// 提交修改个人信息
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

// ---------- 修改邮箱（两步流程） ----------
// 弹窗开关与当前步骤：0 身份验证，1 绑定新邮箱
const emailModalOpen = ref(false);
const emailStep = ref(0);
const emailLoading = ref(false);
const identityLoading = ref(false);
const emailFormRef = ref<FormInstance>();
const identityFormRef = ref<FormInstance>();

// 第一步身份验证通过后由后端签发的一次性凭证，第二步提交时带回
const emailTicket = ref('');

// 新邮箱验证码与原邮箱验证码各自独立的发送状态与重发倒计时
const newCodeSending = ref(false);
const newCodeCountdown = ref(0);
const oldCodeSending = ref(false);
const oldCodeCountdown = ref(0);
let newCodeTimer: ReturnType<typeof setInterval> | null = null;
let oldCodeTimer: ReturnType<typeof setInterval> | null = null;

// 第一步表单：验证方式与原邮箱验证码 / 当前密码
const identityForm = reactive({
    method: 'email' as 'email' | 'password',
    originalCode: '',
    password: '',
});

// 第二步表单：新邮箱与新邮箱验证码
const emailForm = reactive({
    email: '',
    code: '',
});

// 第一步校验：按当前验证方式要求对应字段必填
const identityRules: Record<string, Rule[]> = {
    originalCode: [
        {
            validator: async (_rule: Rule, value: string) => {
                if (identityForm.method !== 'email') {
                    return Promise.resolve();
                }
                if (!value) {
                    return Promise.reject('请输入原邮箱验证码');
                }
                return Promise.resolve();
            },
            trigger: 'blur',
        },
    ],
    password: [
        {
            validator: async (_rule: Rule, value: string) => {
                if (identityForm.method !== 'password') {
                    return Promise.resolve();
                }
                if (!value) {
                    return Promise.reject('请输入当前密码');
                }
                return Promise.resolve();
            },
            trigger: 'blur',
        },
    ],
};

// 第二步校验：新邮箱必填 + 格式正确 + 不能与当前邮箱相同（忽略大小写差异）
const emailRules: Record<string, Rule[]> = {
    email: [
        { required: true, message: '请输入新邮箱', trigger: 'blur' },
        {
            validator: async (_rule: Rule, value: string) => {
                if (!value) {
                    return Promise.resolve();
                }
                if (!EMAIL_PATTERN.test(value)) {
                    return Promise.reject('邮箱格式不正确');
                }
                if (value.trim().toLowerCase() === (userStore.loginUser?.email || '').toLowerCase()) {
                    return Promise.reject('新邮箱不能与当前邮箱相同');
                }
                return Promise.resolve();
            },
            trigger: 'blur',
        },
    ],
    code: [
        { required: true, message: '请输入新邮箱验证码', trigger: 'blur' },
        { len: 6, message: '验证码为 6 位数字', trigger: 'blur' },
    ],
};

// 重置整个修改邮箱流程：回到第一步并清空所有状态与凭证
function resetEmailForm() {
    emailStep.value = 0;
    emailTicket.value = '';
    identityForm.method = 'email';
    identityForm.originalCode = '';
    identityForm.password = '';
    emailForm.email = '';
    emailForm.code = '';
    identityFormRef.value?.clearValidate();
    emailFormRef.value?.clearValidate();
}

// 打开「修改邮箱」弹窗（先重置流程，避免残留上一次的状态）
function openEmailModal() {
    resetEmailForm();
    emailModalOpen.value = true;
}

// 切换验证方式后，清掉另一种方式已填的值与校验提示，避免脏数据被一起提交
function onVerifyMethodChange() {
    if (identityForm.method === 'email') {
        identityForm.password = '';
    } else {
        identityForm.originalCode = '';
    }
    identityFormRef.value?.clearValidate(['originalCode', 'password']);
}

/**
 * 启动重发倒计时
 * 输入：目标 ref 与定时器持有者，便于两路验证码复用同一套逻辑
 * 副作用：每秒递减，归零后自动清理定时器
 */
function startCountdown(target: { value: number }, setTimer: (t: ReturnType<typeof setInterval> | null) => void) {
    target.value = 60;
    const timer = setInterval(() => {
        target.value -= 1;
        if (target.value <= 0) {
            clearInterval(timer);
            setTimer(null);
        }
    }, 1000);
    setTimer(timer);
}

// 发送原邮箱验证码：只传登录用户 id，目标邮箱由后端按库中记录决定
async function handleSendOriginalCode() {
    const loginUser = userStore.loginUser;
    if (!loginUser?.id) {
        message.warning('登录状态已失效，请重新登录');
        return;
    }
    if (!loginUser.email) {
        message.warning('当前账号未绑定邮箱，请改用密码验证');
        return;
    }
    oldCodeSending.value = true;
    try {
        const res = await sendOriginalEmailCode({ id: loginUser.id });
        if (res.data?.data) {
            message.success('验证码已发送至当前邮箱，请查收');
            startCountdown(oldCodeCountdown, (t) => {
                oldCodeTimer = t;
            });
        }
    } catch {
        // 错误提示已由请求拦截器统一处理
    } finally {
        oldCodeSending.value = false;
    }
}

// 第一步提交：身份验证通过后拿到凭证，进入第二步
async function handleVerifyIdentity() {
    try {
        await identityFormRef.value?.validate();
    } catch {
        return;
    }
    const loginUser = userStore.loginUser;
    if (!loginUser?.id) {
        message.warning('登录状态已失效，请重新登录');
        return;
    }
    identityLoading.value = true;
    try {
        const res = await verifyEmailIdentity({
            id: loginUser.id,
            method: identityForm.method,
            originalCode: identityForm.method === 'email' ? identityForm.originalCode.trim() : undefined,
            password: identityForm.method === 'password' ? identityForm.password : undefined,
        });
        const ticket = res.data?.data;
        if (ticket) {
            emailTicket.value = ticket;
            message.success('身份验证通过，请填写新邮箱');
            emailStep.value = 1;
        }
    } catch {
        // 错误提示已由请求拦截器统一处理
    } finally {
        identityLoading.value = false;
    }
}

// 返回第一步重新验证：同时作废已取得的凭证，避免旧凭证继续被使用
function backToIdentityStep() {
    emailTicket.value = '';
    emailForm.email = '';
    emailForm.code = '';
    emailFormRef.value?.clearValidate();
    emailStep.value = 0;
}

// 发送新邮箱验证码：目标地址取自「新邮箱」输入框
async function handleSendNewEmailCode() {
    const email = emailForm.email?.trim();
    if (!email) {
        message.warning('请先输入新邮箱');
        return;
    }
    if (!EMAIL_PATTERN.test(email)) {
        message.warning('邮箱格式不正确');
        return;
    }
    newCodeSending.value = true;
    try {
        const res = await updateEmailCode({ email });
        if (res.data?.data) {
            message.success('验证码已发送至新邮箱，请查收');
            startCountdown(newCodeCountdown, (t) => {
                newCodeTimer = t;
            });
        }
    } catch {
        // 错误提示已由请求拦截器统一处理
    } finally {
        newCodeSending.value = false;
    }
}

// 第二步提交：凭凭证完成新邮箱绑定
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
    if (!emailTicket.value) {
        message.warning('身份验证已失效，请返回上一步重新验证');
        emailStep.value = 0;
        return;
    }
    emailLoading.value = true;
    try {
        const newEmail = emailForm.email.trim();
        const res = await updateEmailMethod({
            id: loginUser.id,
            ticket: emailTicket.value,
            email: newEmail,
            code: emailForm.code.trim(),
        });
        if (res.data?.data) {
            // 后端已刷新会话，这里同步本地 store 让资料卡邮箱立即更新
            userStore.setLoginUser({ ...loginUser, email: newEmail });
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

// 离开页面时清理两个倒计时定时器，避免内存泄漏
onUnmounted(() => {
    if (newCodeTimer) clearInterval(newCodeTimer);
    if (oldCodeTimer) clearInterval(oldCodeTimer);
});

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

/* ---------- 修改邮箱弹窗 ---------- */
/* 步骤条：与下方表单保持呼吸感，不作为主要内容抢注意力 */
.email-steps {
    margin-bottom: 24px;
}

/* 验证码行：输入框占满剩余宽度，发送按钮固定宽度不换行 */
.code-row {
    display: flex;
    align-items: center;
    gap: 10px;
}

/* 验证码输入框：撑满剩余空间，避免按钮被挤出弹窗 */
.code-input {
    flex: 1;
    min-width: 0;
}

/* 验证码发送按钮：固定最小宽度，倒计时文案变化时不抖动 */
.code-btn {
    flex-shrink: 0;
    min-width: 108px;
}

/* 身份验证方式切换：占满整行，两个选项等分 */
.verify-switch {
    display: flex;
    width: 100%;
}

/* 验证方式说明文案：弱化层级，提示当前方式的作用范围 */
.verify-hint {
    font-size: 12px;
    color: var(--text-muted);
    margin-bottom: 10px;
    line-height: 1.6;
}

/* 身份验证通过提示：进入第二步时给明确的正向反馈 */
.verified-alert {
    margin-bottom: 20px;
}

/* 弹窗底部操作区：右对齐，取消/上一步在左，主操作在右 */
.modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 8px;
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
