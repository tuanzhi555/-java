<!--
 * 购物车内容项（去掉时间条）
 * 用法：<cart-item :cart="cart" @delete="delItem" />
-->
<template>
  <div class="cart-item" role="group" aria-label="购物车条目">
    <!-- 主体 -->
    <div class="item-body">
      <!-- 商品图片（固定尺寸，保证每行高度一致） -->
      <router-link :to="'/goodview/' + cart.goodId" class="thumb" :aria-label="'查看商品：' + cart.goodName">
        <img :src="baseApi + cart.img" :alt="cart.goodName" loading="lazy" />
      </router-link>

      <!-- 标题与规格（单行省略，防止因换行导致行高不一致） -->
      <div class="info">
        <router-link :to="'/goodview/' + cart.goodId" class="name">{{ cart.goodName }}</router-link>
        <div class="spec">{{ cart.standard }}</div>
      </div>

      <!-- 右侧信息（价格 / 数量 / 小计 / 操作） -->
      <div class="meta">
        <div class="cell">
          <div class="k">价格</div>
          <div class="v price">￥{{ realPriceFixed }}</div>
        </div>

        <div class="cell">
          <div class="k">数量</div>
          <div class="v qty">
            <el-button
              v-if="!editQty"
              size="mini"
              class="qty-pill"
              @click="editQty = true"
              :aria-label="'修改数量 当前：' + cart.count"
            >
              {{ cart.count }}
            </el-button>
            <el-input-number
              v-else
              v-model="localCount"
              :min="1"
              :max="cart.store"
              size="mini"
              @change="applyCount"
            />
          </div>
        </div>

        <div class="cell">
          <div class="k">小计</div>
          <div class="v subtotal">￥{{ totalPrice }}</div>
        </div>

        <div class="cell actions">
          <el-button size="mini" type="success" @click="pay">支付</el-button>
          <el-popconfirm title="确定删除？" @confirm="del">
            <el-button size="mini" type="danger" icon="el-icon-delete" slot="reference"></el-button>
          </el-popconfirm>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "CartItem",
  props: {
    cart: { type: Object, required: true },
  },
  data() {
    return {
      baseApi: this.$store.state.baseApi,
      editQty: false,
      localCount: this.cart?.count || 1,
    };
  },
  computed: {
    realPrice() {
      const p = Number(this.cart.price || 0) * Number(this.cart.discount || 1);
      return isNaN(p) ? 0 : p;
    },
    realPriceFixed() {
      return this.realPrice.toFixed(2);
    },
    totalPrice() {
      const t = this.realPrice * Number(this.cart.count || 1);
      return isNaN(t) ? "0.00" : t.toFixed(2);
    },
  },
  methods: {
    applyCount() {
      const n = Number(this.localCount || 1);
      this.cart.count = n < 1 ? 1 : n;
      this.$nextTick(() => (this.editQty = false));
    },
    del() {
      this.request.delete("/api/cart/" + this.cart.id).then((res) => {
        if (res.code === "200") {
          this.$message.success("删除成功");
          this.$emit("delete", this.cart.id);
        }
      });
    },
    pay() {
      const good = {
        id: this.cart.goodId,
        name: this.cart.goodName,
        imgs: this.cart.img,
        discount: this.cart.discount,
      };
      this.$router.push({
        name: "preOrder",
        query: {
          good: JSON.stringify(good),
          realPrice: this.realPrice,
          num: this.cart.count,
          standard: this.cart.standard,
          cartId: this.cart.id,
        },
      });
    },
  },
};
</script>

<style scoped>
/* 变量 */
:root {
  --space-xl: 24px;
  --space-lg: 20px;
  --space-md: 16px;
  --space-sm: 12px;

  --radius-lg: 16px;
  --radius-md: 12px;
  --radius-sm: 8px;

  --shadow-1: 0 12px 32px rgba(31, 45, 61, 0.08);
  --text-strong: #1f2937;
  --text-subtle: #6b7280;
}

/* 卡片 */
.cart-item {
  background: #fff;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-1);
  overflow: hidden;
}

/* 主体：固定最小行高，避免不同条目高度不一致 */
.item-body {
  display: grid;
  grid-template-columns: 96px 1fr;   /* 图 + 文字（小屏时 meta 会占整行） */
  gap: var(--space-lg);
  padding: var(--space-lg);
  min-height: 112px;                 /* 统一行高 */
  align-items: center;
}

/* 左图：固定尺寸，消除高度差 */
.thumb img {
  width: 96px;
  height: 96px;
  object-fit: cover;
  border-radius: 12px;
  display: block;
  box-shadow: 0 8px 20px rgba(0,0,0,.08);
}

/* 标题与规格：单行省略，防止多行文字撑高 */
.info { min-width: 0; }
.name {
  font-weight: 700;
  color: var(--text-strong);
  text-decoration: none;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.name:hover { color: #374151; }
.spec {
  margin-top: 6px;
  color: var(--text-subtle);
  font-size: 13px;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 右侧信息网格：小屏占整行，大屏放右侧 */
.meta {
  grid-column: 1 / -1; /* 默认整行 */
  display: grid;
  grid-template-columns: 140px 160px 160px 1fr; /* 价格、数量、小计、操作 */
  gap: var(--space-md);
  align-items: center;
}

/* 每格标题/值 */
.cell .k {
  color: var(--text-subtle);
  font-size: 12px;
  margin-bottom: 4px;
}
.cell .v { font-weight: 600; }
.price { color: #334155; }
.subtotal { color: #e0525c; }

/* 数量 pill 与步进器样式统一 */
.qty-pill {
  border-radius: 999px;
}
.cart-item >>> .el-input-number {
  border-radius: 10px;
}
.cart-item >>> .el-input-number .el-input__inner {
  text-align: center;
}

/* 操作按钮 */
.actions {
  justify-self: end;
  display: flex;
  gap: 10px;
}

/* 大屏：把 meta 放到右边一列 */
@media (min-width: 900px) {
  .item-body {
    grid-template-columns: 96px 1fr minmax(520px, 560px);
  }
  .meta {
    grid-column: auto;
    grid-template-columns: 120px 140px 140px 160px;
    justify-items: end;
  }
}

/* 焦点可见性（键盘导航） */
.cart-item :focus-visible {
  outline: 2px solid #7c90ff;
  outline-offset: 2px;
  border-radius: 6px;
}
</style>
