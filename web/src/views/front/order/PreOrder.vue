<!--
 * 付费订单页面（UI 美化 + 底部吸附结算条）
 * 不修改任何接口与业务逻辑
-->
<template>
  <div class="checkout">
    <!-- 收货地址 -->
    <div class="card addresses">
      <div class="section-title">
        <div class="left">
          <span class="dot"></span>
          <span>收货地址</span>
        </div>
        <el-button type="primary" size="mini" icon="el-icon-plus" @click="addAddress">
          新增地址
        </el-button>
      </div>

      <div class="address-grid">
        <template v-for="(item, index) in addressData">
          <address-box
            :address="item"
            :key="item.id || index"
            :class="['address-item', { active: index === checkedIndex }]"
            @edit="editAddress(item)"
            @delete="deleteAddress(item)"
            @click.native="select(index)"
          />
        </template>
      </div>
    </div>

    <!-- 地址弹窗 -->
    <el-dialog title="地址信息" :visible.sync="dialogFormVisible" width="520px">
      <el-form label-width="90px" style="padding: 0 20px">
        <el-form-item label="联系人">
          <el-input v-model="address.linkUser" autocomplete="off" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="address.linkPhone" autocomplete="off" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="address.linkAddress" autocomplete="off" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="saveAddress">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 商品确认 -->
    <div class="card goods">
      <div class="section-title">
        <div class="left">
          <span class="dot"></span>
          <span>商品确认</span>
        </div>
      </div>

      <el-table
        class="goods-table"
        :data="goods"
        border
        stripe
        style="width: 100%"
        :header-cell-style="{ background: '#f8fafc', color: '#334155', fontWeight: 600 }"
        :cell-style="{ padding: '14px 12px' }"
      >
        <el-table-column label="商品图片" width="160" align="center">
          <template slot-scope="scope">
            <el-image
              :src="baseApi + scope.row.imgs"
              style="width: 100px; height: 100px; border-radius: 8px"
              fit="contain"
            />
          </template>
        </el-table-column>

        <el-table-column prop="name" label="商品名称" min-width="220" />
        <el-table-column prop="standard" label="规格" width="140" align="center" />
        <el-table-column prop="realPrice" label="单价" width="120" align="center" />
        <el-table-column prop="num" label="数量" width="120" align="center" />
        <el-table-column label="价格" width="140" align="center">
          <template slot-scope="scope">
            {{ (scope.row.realPrice * scope.row.num).toFixed(2) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 结算区（原位） -->
      <div class="summary" ref="summary">
        <div class="row">
          <span>总价：</span>
          <strong class="price">￥ {{ sumPrice }}</strong>
        </div>
        <div class="row muted">优惠：￥{{ sumDiscount }}</div>
        <el-button
          type="danger"
          class="submit-btn"
          size="medium"
          @click="submitOrder"
        >
          提交订单
        </el-button>
      </div>
    </div>

    <!-- 底部吸附结算条（当 summary 不在视口内时出现） -->
    <div class="dock-summary" v-show="showDock">
      <div class="row">
        <span>总价：</span>
        <strong class="price">￥ {{ sumPrice }}</strong>
      </div>
      <div class="row muted">优惠：￥{{ sumDiscount }}</div>
      <el-button type="danger" class="submit-btn" size="medium" @click="submitOrder">
        提交订单
      </el-button>
    </div>
  </div>
</template>

<script>
import API from "@/utils/request";
import addressBox from "@/components/AddressBox";

export default {
  name: "cart",
  components: { "address-box": addressBox },
  data() {
    return {
      baseApi: this.$store.state.baseApi,
      userId: 0,
      addressData: [],
      address: {},          // 临时地址
      checkedIndex: 0,
      dialogFormVisible: false,
      good: {},
      realPrice: -1,
      goods: [],
      cartId: "",
      showDock: false,      // 控制底部吸附结算条
    };
  },
  created() {
    this.loadAddress();
    this.good = JSON.parse(this.$route.query.good);
    this.good.realPrice = this.$route.query.realPrice;
    this.good.num = this.$route.query.num;
    this.good.standard = this.$route.query.standard;
    this.cartId = this.$route.query.cartId;
    this.goods.push(this.good);
  },
  mounted() {
    // 监听滚动，控制 dock 显示
    this.$nextTick(() => {
      this.handleScroll();
      window.addEventListener("scroll", this.handleScroll, { passive: true });
    });
  },
  beforeDestroy() {
    window.removeEventListener("scroll", this.handleScroll);
  },
  computed: {
    // 汇总价格
    sumPrice() {
      let sum = 0;
      this.goods.forEach((g) => (sum += g.realPrice * g.num));
      return sum.toFixed(2);
    },
    // 汇总折扣
    sumDiscount() {
      let sum = 0;
      this.goods.forEach(
        (g) => (sum += (g.realPrice / g.discount - g.realPrice) * g.num)
      );
      return sum.toFixed(2);
    },
  },
  methods: {
    handleScroll() {
      const el = this.$refs.summary;
      if (!el) return;
      const rect = el.getBoundingClientRect();
      // 当 summary 不完全在视口内时，显示底部吸附条
      const fullyVisible = rect.top >= 0 && rect.bottom <= window.innerHeight;
      this.showDock = !fullyVisible;
    },

    select(index) {
      this.checkedIndex = index;
    },
    addAddress() {
      this.address = {};
      this.dialogFormVisible = true;
    },
    editAddress(item) {
      this.address = JSON.parse(JSON.stringify(item)); // 深拷贝
      this.dialogFormVisible = true;
    },
    deleteAddress(item) {
      this.$confirm("您确认删除该地址吗?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        API.delete("api/address/" + item.id).then((res) => {
          if (res.code === "200") {
            this.$message.success("删除地址成功");
            this.loadAddress();
          }
        });
      });
    },
    saveAddress() {
      this.address.userId = this.userId;
      API.post("/api/address", this.address).then((res) => {
        if (res.code === "200") {
          this.$message.success("保存成功");
          this.loadAddress();
          this.dialogFormVisible = false;
        } else {
          this.$message.error(res.msg);
        }
      });
    },
    loadAddress() {
      API.get("/userid").then((res) => {
        this.userId = res;
        API.get("/api/address/" + res).then((res) => {
          if (res.code === "200") {
            this.addressData = res.data;
          }
        });
      });
    },
    submitOrder() {
      const address = this.addressData[this.checkedIndex];
      if (!address) {
        this.$message({ type: "warning", message: "请选择收货地址！" });
        return;
      }
      API.post("/api/order", {
        totalPrice: this.sumPrice,
        linkUser: address.linkUser,
        linkPhone: address.linkPhone,
        linkAddress: address.linkAddress,
        state: "待付款",
        goods: JSON.stringify(this.goods),
        cartId: this.cartId,
      }).then((res) => {
        if (res.code === "200") {
          const orderNo = res.data;
          this.$router.replace({
            path: "pay",
            query: { money: this.sumPrice, orderNo },
          });
        } else {
          this.$message({ type: "error", message: res.msg });
        }
      });
    },
  },
};
</script>

<style scoped>
/* 容器 */
.checkout {
  width: 1060px;
  margin: 20px auto 40px;
}

/* 卡片 */
.card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 8px 28px rgba(31, 41, 55, 0.08);
  padding: 18px 18px 12px;
  margin-bottom: 18px;
}

/* 标题行 */
.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 6px 12px;
  border-bottom: 1px solid #eef2f7;
  margin-bottom: 12px;
}
.section-title .left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 16px;
  color: #0f172a;
}
.section-title .dot {
  width: 8px;
  height: 8px;
  background: #3b82f6;
  border-radius: 50%;
}

/* 地址宫格 */
.address-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(260px, 1fr));
  gap: 14px;
}
.address-item {
  cursor: pointer;
  transition: all .2s ease;
  border-radius: 12px;
}
.address-item:hover {
  transform: translateY(-1px);
}
.address-item.active {
  outline: 2px solid #3b82f6;
  box-shadow: 0 0 0 4px rgba(59,130,246,.12);
  border-radius: 12px;
}

/* 商品表格 */
.goods-table {
  border-radius: 12px;
  overflow: hidden;
}

/* 原位结算区 */
.summary {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 18px;
  flex-wrap: wrap;
}
.summary .row {
  font-size: 14px;
  color: #1f2937;
}
.summary .row.muted {
  color: #94a3b8;
  font-size: 12px;
}
.summary .price {
  color: #ef4444;
  font-size: 22px;
  font-weight: 800;
  margin-left: 2px;
}
.submit-btn {
  min-width: 120px;
  height: 36px;
  border-radius: 10px;
}

/* 底部吸附结算条 */
.dock-summary {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1200;
  background: #ffffff;
  border-top: 1px solid #eef2f7;
  box-shadow: 0 -8px 24px rgba(15, 23, 42, 0.08);
  padding: 10px 24px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 18px;
}
.dock-summary .row {
  font-size: 14px;
  color: #1f2937;
}
.dock-summary .row.muted {
  color: #94a3b8;
  font-size: 12px;
}
.dock-summary .price {
  color: #ef4444;
  font-size: 20px;
  font-weight: 800;
  margin-left: 2px;
}

/* 响应式 */
@media (max-width: 1100px) {
  .checkout { width: 94%; }
  .address-grid { grid-template-columns: repeat(2, minmax(240px, 1fr)); }
}
@media (max-width: 720px) {
  .address-grid { grid-template-columns: 1fr; }
}
</style>
