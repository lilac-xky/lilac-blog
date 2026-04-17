<template>
    <div id="basicLayout">
        <a-layout class="app-layout">
            <a-layout-sider class="sider" :width="220">
                <GlobalSider />
            </a-layout-sider>
            <a-layout class="main-layout">
                <a-layout-header class="header">
                    <GlobalHeader />
                </a-layout-header>
                <a-layout-content class="content">
                    <router-view />
                </a-layout-content>
                <a-layout-footer class="footer">
                    created by lilac
                </a-layout-footer>
            </a-layout>
        </a-layout>
    </div>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue';
import GlobalSider from '@/components/GlobalSider.vue';
</script>

<style scoped>
#basicLayout {
    height: 100vh;
    overflow: hidden;
}

.app-layout {
    height: 100vh;
    background: var(--bg-page);
}

.main-layout {
    background: var(--bg-page);
    overflow: hidden;
}

#basicLayout .sider {
    background: var(--bg-sider);
    padding: 20px 12px;
    overflow: hidden;
}

#basicLayout .header {
    background: transparent;
    padding: 16px 28px 0 28px;
    height: auto;
    line-height: 1;
}

#basicLayout .content {
    background: transparent;
    padding: 16px 28px 8px 28px;
    overflow: hidden;
    flex: 1;
    min-height: 0;
}

#basicLayout .footer {
    background: transparent;
    text-align: center;
    color: var(--text-muted);
    padding: 8px;
    font-size: 12px;
}
</style>
