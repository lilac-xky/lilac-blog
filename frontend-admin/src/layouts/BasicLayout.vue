<template>
    <div id="basicLayout">
        <a-layout style="min-height: 100vh;">
            <a-layout-header class="header">
                <GlobalHeader />
            </a-layout-header>
            <a-layout>
                <a-layout-sider class="sider">
                    <GlobalSider />
                </a-layout-sider>
                <a-layout-content class="content">
                    <router-view />
                </a-layout-content>
            </a-layout>
            <a-layout-footer class="footer">
                created by lilac
            </a-layout-footer>
        </a-layout>
    </div>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue';
import GlobalSider from '@/components/GlobalSider.vue';
</script>

<style scoped>
#basicLayout .header {
    background: white;
    padding-inline: 20px;
}

#basicLayout .sider {
    background: white;
}

#basicLayout .content {
    background: linear-gradient(to right, #efefef, #fff);
    padding: 24px;
}

#basicLayout .footer {
    background: white;
    text-align: center;
    color: #999;
}
</style>