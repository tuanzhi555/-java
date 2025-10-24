<!--
 * 我的订单列表（UI 美化版｜仅样式与布局）
 * - 不改接口/字段/方法
 * - 状态列统一：状态标签 + 按钮（避免列数错位）
 * @Author: ShanZhu
 * @Date: 2023-11-11
-->
<template>
  <div class="order-item">
    <!-- 头部：时间 + 订单号 -->
    <div class="order-header">
      <span class="order-time">{{ order.create_time }}</span>
      <span class="order-no">订单编号：{{ order.order_no }}</span>
    </div>

    <!-- 主体：左图右表 -->
    <div class="order-body">
      <div class="thumb-wrap">
        <router-link :to="'goodview/' + order.good_id">
          <img :src="baseApi + order.imgs" class="thumb" alt="商品图" />
        </router-link>
      </div>

      <div class="info-wrap">
        <table class="info-table">
          <tr>
            <th>商品</th>
            <th>规格</th>
            <th>数量</th>
            <th>总价</th>
            <th>收货人</th>
            <th>订单状态</th>
          </tr>
          <tr>
            <!-- 商品名称 -->
            <td class="ellipsis">
              <a :href="'goodview/' + order.good_id" class="link" :title="order.good_name">
                {{ order.good_name }}
              </a>
            </td>

            <!-- 规格/数量/总价 -->
            <td>{{ order.standard }}</td>
            <td class="mono">{{ order.count }}</td>
            <td class="mono">￥{{ order.total_price }}</td>

            <!-- 收货人（悬浮显示电话/地址） -->
            <td>
              <el-popover
                placement="bottom-start"
                width="260"
                trigger="hover"
                :content="address"
              >
                <span slot="reference" class="receiver">{{ order.link_user }}</span>
              </el-popover>
            </td>

            <!-- 订单状态列（统一放一格里） -->
            <td class="status-cell">
              <!-- 已发货：可确认收货 -->
              <template v-if="order.state === '已发货'">
                <el-tag type="success" effect="plain">已发货</el-tag>
                <el-button
                  class="op-btn"
                  size="mini"
                  type="primary"
                  @click="receive"
                >确认收货</el-button>
              </template>

              <!-- 已收货 -->
              <template v-else-if="order.state === '已收货'">
                <el-tag type="success" effect="dark">
                  <i class="el-icon-check" style="margin-right: 4px"></i>已收货
                </el-tag>
              </template>

              <!-- 已支付：等待发货 -->
              <template v-else-if="order.state === '已支付'">
                <el-tag type="info" effect="plain">已支付</el-tag>
                <el-button size="mini" type="info" plain disabled class="op-btn">等待发货</el-button>
              </template>

              <!-- 其他（未支付等） -->
              <template v-else>
                <el-tag type="warning" effect="plain">{{ order.state }}</el-tag>
                <el-button
                  class="op-btn"
                  size="mini"
                  type="success"
                  @click="pay"
                >去支付</el-button>
              </template>
            </td>
          </tr>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "OrderItem",
  props: {
    order: Object,
  },
  data() {
    return {
      address: "电话:" + this.order.link_phone + " 地址:" + this.order.link_address,
      baseApi: this.$store.state.baseApi,
    };
  },
  methods: {
    // 跳转到支付页面
    pay() {
      this.$router.push({
        path: "pay",
        query: { money: this.order.total_price, orderNo: this.order.order_no },
      });
    },
    // 确认收货
    receive() {
      this.$confirm("是否确认收货?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "info",
      }).then(() => {
        this.request.get("/api/order/received/" + this.order.order_no).then((res) => {
          if (res.code === "200") {
            this.$message.success("收货成功");
            this.order.state = "已收货";
          }
        });
      });
    },
  },
};
</script>

<style scoped>
/* 容器卡片 */
.order-item {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0,0,0,.04);
}

/* 头部 */
.order-header {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 10px 16px;
  background: #f7f9fc;
  border-bottom: 1px solid #ebeef5;
  font-size: 14px;
  color: #606266;
}
.order-no {
  color: #303133;
  font-weight: 600;
}

/* 主体 */
.order-body {
  display: flex;
  align-items: center;
  padding: 16px;
  gap: 16px;
}

/* 商品图 */
.thumb-wrap { flex: 0 0 auto; }
.thumb {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

/* 信息表 */
.info-wrap { flex: 1 1 auto; overflow: hidden; }
.info-table {
  width: 100%;
  border-collapse: collapse;
}
.info-table th,
.info-table td {
  text-align: center;
  padding: 8px 6px;
  font-size: 14px;
  color: #303133;
}
.info-table th {
  font-size: 13px;
  color: #3472a6;
  font-weight: 600;
  background: #f9fafb;
}
.receiver {
  color: #409EFF;
  cursor: pointer;
}

/* 状态列：按钮与标签的间距 */
.status-cell {
  white-space: nowrap;
}
.op-btn {
  margin-left: 8px;
}

/* 文本优化 */
.ellipsis { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.mono { font-variant-numeric: tabular-nums; }

/* 窄屏适配 */
@media (max-width: 768px) {
  .order-body { flex-direction: column; align-items: flex-start; }
  .info-table th, .info-table td { padding: 6px 4px; }
  .status-cell { white-space: normal; }
  .thumb { width: 96px; height: 96px; }
}
</style>
