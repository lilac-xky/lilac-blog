<template>
    <div class="global-header">
        <a-row wrap>
            <a-col flex="260px">
                <router-link to="/">
                    <div class="title-bar">
                        <img src="../assets/logo.png" alt="logo" class="logo" />
                        <div class="title">lilac-blog后台管理系统</div>
                    </div>
                </router-link>
            </a-col>
            <a-col flex="auto" style="text-align: right;">
                <a-dropdown>
                    <a-space>
                        <a-avatar src="https://zos.alipayobjects.com/rmsportal/ODTLcjxAfvqbxHnVXCYX.png" />
                    </a-space>
                    <template #overlay>
                        <a-menu>
                            <a-menu-item key="1">个人中心</a-menu-item>
                            <a-menu-item key="2">设置</a-menu-item>
                            <a-menu-divider />
                            <a-menu-item key="3">退出登录</a-menu-item>
                        </a-menu>
                    </template>
                </a-dropdown>
            </a-col>
        </a-row>
    </div>
</template>

<script setup lang="ts">
</script>


<style scoped>
.title-bar {
    display: flex;
    align-items: center;
}

.title {
    font-size: 20px;
    font-weight: bold;
}

.logo {
    height: 40px;
    margin-right: 10px;
    border-radius: 50%;
}
</style>