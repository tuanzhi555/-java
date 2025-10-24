<!--
 * 销售额页面（青蓝/商务中性 主题）
 * - 仅样式与布局优化，不改接口与字段
 * - 保留 props: good / index / categories
 * - 图片仍使用 baseApi + good.imgs
 * @Author: ShanZhu
 * @Date: 2023-11-11
-->
<template>
  <div class="income-item-card">
    <!-- 顶部徽章（替代原橙色横条） -->
    <div class="rank-badge">销量第{{ index }}</div>

    <div class="content">
      <router-link :to="'/goodview/' + good.id" class="thumb-wrap">
        <img :src="baseApi + (good.imgs || '')" class="thumb" alt="商品图片" />
      </router-link>

      <div class="meta">
        <div class="meta-row">
          <span class="label">商品id</span>
          <span class="value mono">{{ good.id }}</span>
        </div>

        <div class="meta-row">
          <span class="label">商品名称</span>
          <router-link :to="'/goodview/' + good.id" class="value link ellipsis" :title="good.name">
            {{ good.name }}
          </router-link>
        </div>

        <div class="meta-row">
          <span class="label">销售额</span>
          <span class="value price">￥ {{ money(good.saleMoney) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "IncomeItem",
  props: {
    good: Object,
    index: Number,
    categories: Array
  },
  data() {
    return {
      baseApi: this.$store.state.baseApi
    };
  },
  methods: {
    money(v) {
      if (v == null || v === "") return 0;
      return Number(v).toLocaleString(undefined, { maximumFractionDigits: 2 });
    }
  }
};
</script>

<style scoped>
/* 容器卡片（浅灰背景 + 细边框） */
.income-item-card {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e5e7eb;       /* panel-border */
  background: #f9fafb;             /* panel-bg */
  box-shadow: 0 1px 3px rgba(0,0,0,.04);
}

/* 顶部徽章：青蓝渐变 */
.rank-badge {
  padding: 8px 14px;
  font-weight: 700;
  color: #002b36;                   /* badge-fg */
  background: linear-gradient(90deg, #0ea5e9, #22d3ee);
}

/* 内容布局：左图右信息 */
.content {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 16px;
  padding: 16px;
}

/* 商品图 */
.thumb-wrap { display: inline-block; }
.thumb {
  width: 120px; height: 120px; object-fit: cover;
  border-radius: 8px; border: 1px solid #e5e7eb; background: #fff;
}

/* 信息区：三行网格，标签/值对齐 */
.meta {
  display: grid;
  grid-template-columns: 120px 1fr;
  row-gap: 10px;
  column-gap: 16px;
  align-items: center;
}
.meta-row { display: contents; }

.label { color: #374151; font-weight: 600; }   /* slate-700 */
.value { color: #111827; }                     /* slate-900 */
.price { font-weight: 700; }
.mono { font-variant-numeric: tabular-nums; }
.link { color: #0ea5e9; text-decoration: none; }
.link:hover { text-decoration: underline; }

/* 文本省略 */
.ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 小屏自适应 */
@media (max-width: 640px) {
  .content { grid-template-columns: 96px 1fr; }
  .thumb { width: 96px; height: 96px; }
  .meta { grid-template-columns: 100px 1fr; }
}
</style>
