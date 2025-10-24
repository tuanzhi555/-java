<!--
 * 地址 box（UI 美化版｜仅样式与布局）
 * - 保留 props: address
 * - 保留事件：$emit('edit') / $emit('delete')
 * - 不改任何接口/字段/业务逻辑
 * @Author: ShanZhu
 * @Date: 2023-11-11
-->
<template>
  <div class="addr-card">
    <div class="addr-header">
      <span class="name" :title="address.linkUser">{{ address.linkUser }}</span>
    </div>

    <div class="addr-line">
      <i class="el-icon-phone-outline icon"></i>
      <span class="text">{{ address.linkPhone }}</span>
    </div>

    <div class="addr-line ellipsis" :title="address.linkAddress">
      <i class="el-icon-location-outline icon"></i>
      <span class="text">{{ address.linkAddress }}</span>
    </div>

    <div class="addr-actions">
      <el-link type="primary" @click.stop="$emit('edit')">
        <i class="el-icon-edit" /> 修改
      </el-link>
      <el-link type="danger" @click.stop="$emit('delete')">
        <i class="el-icon-delete" /> 删除
      </el-link>
    </div>
  </div>
</template>

<script>
export default {
  name: "AddressBox",
  props: {
    address: { type: Object, required: true },
  },
};
</script>

<style scoped>
.addr-card {
  position: relative;
  display: inline-block;
  width: 300px;
  min-height: 120px;
  padding: 12px 12px 38px; /* 底部预留给操作区 */
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0,0,0,.04);
  transition: box-shadow .15s ease, border-color .15s ease, transform .05s ease;
}
.addr-card:hover {
  border-color: #d1d5db;
  box-shadow: 0 4px 14px rgba(0,0,0,.06);
}
.addr-card:active {
  transform: translateY(1px);
}

/* 顶部名称 */
.addr-header {
  margin-bottom: 8px;
}
.name {
  font-weight: 600;
  color: #303133;
}

/* 行样式 */
.addr-line {
  display: flex;
  align-items: center;
  gap: 6px;
  line-height: 1.6;
  margin: 4px 0;
}
.icon {
  color: #909399;
  font-size: 16px;
}
.text {
  color: #606266;
  font-size: 14px;
}

/* 长地址省略 */
.ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 底部操作区固定在卡片底部 */
.addr-actions {
  position: absolute;
  left: 12px;
  right: 12px;
  bottom: 10px;
  display: flex;
  justify-content: space-between;
}
</style>
