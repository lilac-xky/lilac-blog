<template>
  <div id="app">
    <a-config-provider :locale="zhCN">
      <BasicLayout />
    </a-config-provider>
  </div>
</template>

<script setup lang="ts">
import zhCN from 'ant-design-vue/lib/locale/zh_CN';
import BasicLayout from './layouts/BasicLayout.vue';
</script>

<style>
* {
  margin: 0;
  padding: 0;
  border: 0;
  font-family: inherit;
  font-size: 100%;
  vertical-align: baseline;
}

html {
  line-height: 1;
}

body {
  background: white;
  font-family: sans-serif;
}
</style>
