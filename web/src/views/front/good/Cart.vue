<!--
 * 我的购物车（美化版）
 * 不修改任何后端接口；仍然复用 <cart-item />
-->
<template>
  <div class="cart-page" v-loading="isLoading">
    <!-- 页头 -->
    <section class="cart-header">
      <h2 class="title">我的购物车</h2>
      <div class="meta" aria-live="polite">
        <span class="count">共 {{ itemCount }} 件商品</span>
        <span v-if="itemCount" class="total">
          预计合计 <b>￥{{ grandTotal.toFixed(2) }}</b>
        </span>
      </div>
      <div class="divider" aria-hidden="true"></div>
    </section>

    <!-- 空状态 -->
    <section v-if="!carts.length && !isLoading" class="empty-state">
      <div class="empty-illust" aria-hidden="true">🛒</div>
      <h3>购物车是空的哦</h3>
      <p>去逛逛，挑几样喜欢的吧～</p>
      <router-link to="/topview">
        <el-button type="primary" round>去逛逛</el-button>
      </router-link>
    </section>

    <!-- 列表 -->
    <section v-else class="cart-list" aria-label="购物车商品列表">
      <div
        v-for="cart in carts"
        :key="cart.id"
        class="order-card"
      >
        <!-- 保持现有 CartItem，不改内部逻辑 -->
        <cart-item :cart="cart" @delete="delItem" />
      </div>
    </section>
  </div>
</template>

<script>
import CartItem from "@/components/CartItem";

export default {
  name: "Cart",
  components: { "cart-item": CartItem },
  data() {
    return {
      userId: null,
      carts: [],
      isLoading: true,
    };
  },
  computed: {
    itemCount() {
      return this.carts.length;
    },
    // 兼容不同字段名：优先 total，其次 price*num，兜底 0
    grandTotal() {
      return this.carts.reduce((sum, c) => {
        const price = Number(c.price || c.goodPrice || 0);
        const num = Number(c.num || c.count || 1);
        const total = Number(c.total || c.amount || price * num || 0);
        return sum + (isNaN(total) ? 0 : total);
      }, 0);
    },
  },
  created() {
    this.request.get("/userid").then((res) => {
      this.userId = res;
      this.request.get("/api/cart/userid/" + this.userId).then((res2) => {
        if (res2.code === "200") {
          this.carts = res2.data || [];
          // 若后端返回 ISO 时间字符串，可做一次人性化处理（可留可删）
          for (let i = 0; i < this.carts.length; i++) {
            const t = this.carts[i].createTime;
            if (t && typeof t === "string") {
              this.carts[i].createTime = t.replace("T", " ").replace(/\.\d+Z?$/, "");
            }
          }
        } else {
          this.$message.error(res2.msg || "加载购物车失败");
        }
      }).finally(() => (this.isLoading = false));
    }).catch(() => (this.isLoading = false));
  },
  methods: {
    delItem(id) {
      this.carts = this.carts.filter((item) => item.id !== id);
    },
  },
};
</script>

<style scoped>
/* ========= 变量与版心 ========= */
:root{
  --maxw: 1100px;
  --space-xl: 24px;
  --space-lg: 20px;
  --space-md: 16px;
  --space-sm: 12px;

  --radius-lg: 16px;
  --radius-md: 12px;

  --shadow-1: 0 14px 36px rgba(31,45,61,.08);
  --brand-grad: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --text-strong: #2c3e50;
  --text-subtle: #64748b;
}

.cart-page{
  max-width: var(--maxw);
  margin: 28px auto 60px;
  padding: 0 var(--space-md);
}

/* ========= 页头 ========= */
.cart-header{
  background: #fff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-1);
  padding: 18px var(--space-xl);
}
.title{
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: var(--text-strong);
  line-height: 1.35;
}
.meta{
  margin-top: 4px;
  color: var(--text-subtle);
  display: flex;
  gap: 16px;
  align-items: baseline;
  flex-wrap: wrap;
}
.meta .total b{ font-size: 18px; color: #e0525c; }
.divider{
  height: 3px; width: 72px; margin-top: 10px; border-radius: 2px;
  background: var(--brand-grad);
}

/* ========= 列表 ========= */
.cart-list{
  margin-top: var(--space-xl);
  display: grid;
  gap: var(--space-xl);
}

/* 给每个 CartItem 外面包一层柔和卡片（不改子组件内部） */
.order-card{
  background: #fff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-1);
  padding: var(--space-lg);
  overflow: hidden;
}

/* 可选：柔化子组件里的按钮/输入（深度选择） */
.order-card >>> .el-button{
  border-radius: 999px;
}
.order-card >>> .el-input-number{
  border-radius: 10px;
}
.order-card >>> .el-input-number .el-input__inner{
  text-align: center;
}

/* ========= 空状态 ========= */
.empty-state{
  background: #fff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-1);
  padding: 60px 20px;
  margin-top: var(--space-xl);
  text-align: center;
}
.empty-illust{
  width: 84px; height: 84px; margin: 0 auto 12px;
  display: grid; place-items: center;
  font-size: 46px;
  background: #f1f5ff; color: #6b7df0; border-radius: 20px;
}
.empty-state h3{
  margin: 8px 0 4px; font-size: 18px; color: var(--text-strong);
}
.empty-state p{
  margin: 0 0 16px; color: var(--text-subtle);
}

/* ========= 响应式 ========= */
@media (max-width: 768px){
  .cart-page{ margin: 16px auto 40px; }
  .order-card{ padding: var(--space-md); }
  .title{ font-size: 20px; }
}
</style>
