<!--
 * 支付页面（前端模拟版，不改后端接口）
 * 仅用于演示/联调：点击支付 -> 调用 /api/order/paid/:orderNo 标记已支付
-->
<template>
  <div class="pay-wrap">
    <div class="card">
      <div class="title">订单支付</div>

      <table class="kv">
        <tr>
          <th>订单号</th>
          <td>{{ orderNo }}</td>
        </tr>
        <tr>
          <th>金额</th>
          <td class="price">￥{{ money }}</td>
        </tr>
      </table>

      <div class="divider"></div>

      <div class="subtitle">选择支付方式</div>

      <div class="methods">
        <div class="method" @click="confirmPay('微信支付')" :class="{ loading }">
          <img src="@/resource/img/微信支付.png" alt="wechat" />
          <span>微信支付</span>
        </div>
        <div class="method" @click="confirmPay('支付宝')" :class="{ loading }">
          <img src="@/resource/img/支付宝.png" alt="alipay" />
          <span>支付宝</span>
        </div>
      </div>

      <div class="tips">
        说明：当前为<strong>前端模拟支付</strong>，点击方式后会直接将订单标记为“已支付”，仅用于演示，不能用于真实收款。
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Pay",
  data() {
    return {
      orderNo: "",
      money: "0.00",
      loading: false,
    };
  },
  created() {
    const { money, orderNo } = this.$route.query || {};
    this.money = (parseFloat(money || 0).toFixed(2));
    this.orderNo = orderNo || "";
  },
  methods: {
    confirmPay(channelName) {
      if (this.loading) return;
      this.$confirm(
        `模拟使用「${channelName}」支付 ￥${this.money}，是否继续？`,
        "确认支付",
        { type: "warning" }
      )
        .then(() => this.doMockPay())
        .catch(() => {});
    },
    doMockPay() {
      if (!this.orderNo) {
        this.$message.error("订单号缺失");
        return;
      }
      this.loading = true;
      // 模拟处理耗时
      setTimeout(() => {
        this.request
          .get(`/api/order/paid/${this.orderNo}`)
          .then((res) => {
            if (res.code === "200") {
              this.$message.success(`已支付 ￥${this.money}`);
              this.$router.replace("/orderlist");
            } else {
              this.$message.error(res.msg || "支付失败");
            }
          })
          .catch(() => {
            this.$message.error("网络异常，请稍后重试");
          })
          .finally(() => {
            this.loading = false;
          });
      }, 800);
    },
  },
};
</script>

<style scoped>
.pay-wrap {
  width: 560px;
  margin: 24px auto;
}
.card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 28px rgba(31, 41, 55, 0.08);
  padding: 24px 24px 18px;
}
.title {
  font-weight: 700;
  font-size: 18px;
  color: #0f172a;
  margin-bottom: 14px;
}
.kv {
  width: 100%;
  border-collapse: collapse;
}
.kv th,
.kv td {
  text-align: left;
  padding: 8px 0;
  color: #334155;
}
.kv .price {
  color: #ef4444;
  font-weight: 800;
}
.divider {
  height: 1px;
  background: #eef2f7;
  margin: 14px 0;
}
.subtitle {
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 10px;
}
.methods {
  display: flex;
  gap: 14px;
}
.method {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all .2s ease;
}
.method:hover {
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
  transform: translateY(-1px);
}
.method.loading {
  pointer-events: none;
  opacity: 0.6;
}
.method img {
  width: 44px;
  height: 44px;
}
.method span {
  font-weight: 600;
}
.tips {
  margin-top: 18px;
  font-size: 12px;
  color: #64748b;
}
</style>
