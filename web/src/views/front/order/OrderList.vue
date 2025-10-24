<!--
 * 订单列表（美化版 | 去掉时间）
 * 不修改后端接口；复用 <order-item />
-->
<template>
  <div class="order-page" v-loading="isLoading">
    <!-- 顶部 -->
    <section class="order-header">
      <h2 class="title">我的订单</h2>
      <div class="meta">共 {{ itemCount }} 笔订单</div>
      <div class="divider" aria-hidden="true"></div>
    </section>

    <!-- 空状态 -->
    <section v-if="!isLoading && itemCount === 0" class="empty-state">
      <div class="empty-illust" aria-hidden="true">📦</div>
      <h3>没有订单记录</h3>
      <p>去看看有没有心仪的商品吧～</p>
      <router-link to="/topview">
        <el-button type="primary" round>去逛逛</el-button>
      </router-link>
    </section>

    <!-- 列表 -->
    <section v-else class="order-list" aria-label="订单列表">
      <div
        v-for="order in orders"
        :key="order.id"
        class="order-card"
      >
        <order-item :order="order" />
      </div>
    </section>
  </div>
</template>

<script>
import OrderItem from "@/components/OrderItem";
import API from "@/utils/request";

export default {
  name: "OrderList",
  components: { "order-item": OrderItem },
  data() {
    return {
      orders: [],
      isLoading: true,
    };
  },
  computed: {
    itemCount() {
      return this.orders.length;
    },
  },
  created() {
    API.get("/userid")
      .then((uid) => API.get(`/api/order/userid/${uid}`))
      .then((res) => {
        if (res.code === "200") {
          // 去掉时间：把 create_time / createTime 置空，不改 OrderItem 模板
          this.orders = (res.data || []).map((o) => ({
            ...o,
            create_time: "",
            createTime: "",
          }));
        } else {
          this.$message.error(res.msg || "获取订单失败");
        }
      })
      .catch(() => this.$message.error("网络异常，稍后再试"))
      .finally(() => (this.isLoading = false));
  },
};
</script>

<style scoped>
/* 变量与版心 */
:root {
  --maxw: 1100px;
  --space-xl: 24px;
  --space-lg: 20px;
  --space-md: 16px;
  --space-sm: 12px;

  --radius-lg: 16px;
  --radius-md: 12px;

  --shadow-1: 0 14px 36px rgba(31, 45, 61, .08);
  --brand-grad: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --text-strong: #1f2937;
  --text-subtle: #64748b;
}

.order-page {
  max-width: var(--maxw);
  margin: 28px auto 60px;
  padding: 0 var(--space-md);
}

/* 顶部 */
.order-header {
  background: #fff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-1);
  padding: 18px var(--space-xl);
}
.title {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: var(--text-strong);
  line-height: 1.35;
}
.meta {
  margin-top: 4px;
  color: var(--text-subtle);
}
.divider {
  height: 3px;
  width: 72px;
  margin-top: 10px;
  border-radius: 2px;
  background: var(--brand-grad);
}

/* 列表 */
.order-list {
  margin-top: var(--space-xl);
  display: grid;
  gap: var(--space-xl);
}

/* 外层卡片 */
.order-card {
  background: #fff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-1);
  padding: var(--space-lg);
  overflow: hidden;
}

/* 空状态 */
.empty-state {
  background: #fff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-1);
  padding: 60px 20px;
  margin-top: var(--space-xl);
  text-align: center;
}
.empty-illust {
  width: 84px; height: 84px;
  margin: 0 auto 12px;
  display: grid; place-items: center;
  font-size: 46px;
  background: #f1f5ff; color: #6b7df0; border-radius: 20px;
}
.empty-state h3 {
  margin: 8px 0 4px; font-size: 18px; color: var(--text-strong);
}
.empty-state p {
  margin: 0 0 16px; color: var(--text-subtle);
}

/* 响应式 */
@media (max-width: 768px) {
  .order-page { margin: 16px auto 40px; }
  .title { font-size: 20px; }
  .order-card { padding: var(--space-md); }
}
</style>
