<!--
 * 商品详情页面（前端美化 + 放大商品图 + 评论星级显示 + 星级筛选）
 * 不修改任何后端接口
-->
<template>
  <div class="goodview">
    <div class="main-box">
      <!-- 上半：图 + 详情 -->
      <div class="product-wrap">
        <!-- 左侧大图 -->
        <div class="image-box">
          <img :src="baseApi + good.imgs" class="image" alt="商品图片" />
        </div>

        <!-- 右侧信息 -->
        <div class="detail-box">
          <!-- 名称与描述 -->
          <div class="title">
            <h1 class="name">{{ good.name }}</h1>
            <p class="desc">{{ good.description }}</p>
          </div>

          <!-- 价格 -->
          <div class="price-box" v-if="good.discount < 1">
            <dl class="price-grid">
              <div>
                <dt>原价</dt>
                <dd class="origin">￥{{ price }}</dd>
              </div>
              <div>
                <dt>折扣</dt>
                <dd>{{ discount }}</dd>
              </div>
              <div>
                <dt>现价</dt>
                <dd class="now">￥{{ realPrice }}</dd>
              </div>
            </dl>
          </div>

          <div class="price-box" v-else>
            <dl class="price-grid">
              <div>
                <dt>价格</dt>
                <dd class="now">￥{{ price }}</dd>
              </div>
            </dl>
          </div>

          <!-- 月销量 / 库存 -->
          <div class="meta">
            <span>月销量：{{ good.sales }}</span>
            <span v-if="showStore" class="stock">库存：{{ store }}</span>
          </div>

          <!-- 规格 -->
          <div class="standards" v-if="standards.length !== 0">
            <el-radio-group
              v-for="(standard, index) in standards"
              :key="index"
              v-model="checkedStandard"
              @change="change(standard)"
              class="standard-group"
            >
              <el-radio-button class="standard" :label="standard.value" />
            </el-radio-group>
          </div>

          <!-- 数量 -->
          <div class="quantity">
            <el-input-number
              v-model="count"
              controls-position="right"
              :min="1"
              :max="store"
            />
          </div>

          <!-- 操作 -->
          <div class="actions">
            <el-button type="success" size="medium" class="buy-btn" @click="goToOrder">
              购买
            </el-button>
            <el-button
              type="primary"
              size="medium"
              class="cart-btn"
              icon="el-icon-shopping-cart-1"
              @click="addToCart"
            >
              加入购物车
            </el-button>
          </div>
        </div>
      </div>

      <!-- 下半：评论 -->
      <div id="message" class="message-box">
        <div class="message-header">评论</div>

        <div class="message-editor">
          <div class="editor-row">
            <el-input
              type="textarea"
              :rows="3"
              placeholder="评论内容"
              v-model="content"
              clearable
            />
          </div>
          <div class="editor-row editor-actions">
            <el-rate v-model="score" />
            <el-button type="primary" class="submit-btn" @click="submit">提交评价</el-button>
          </div>
        </div>

        <!-- 星级筛选（前端本地筛选当前页） -->
        <div class="star-filter">
          <el-radio-group v-model="filterStar" size="small">
            <el-radio-button :label="0">全部</el-radio-button>
            <el-radio-button :label="5">5★</el-radio-button>
            <el-radio-button :label="4">4★</el-radio-button>
            <el-radio-button :label="3">3★</el-radio-button>
            <el-radio-button :label="2">2★</el-radio-button>
            <el-radio-button :label="1">1★</el-radio-button>
          </el-radio-group>
          <span class="filter-tip"></span>
        </div>

        <!-- 评论列表 -->
        <div class="message-list">
          <el-card
            class="message-item"
            v-for="(data, index) in shownMsg"
            :key="index"
            shadow="hover"
          >
            <div class="msg-head">
              <el-rate
                :value="normalizeScore(data.score)"
                disabled
                show-score
              />
            </div>
            <p class="content">{{ data.content }}</p>
          </el-card>
        </div>

        <div class="pagination">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pagination.current"
            :page-sizes="[4,6,8,10]"
            :page-size="pagination.size"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.total"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "GoodView",
  data() {
    return {
      userId: null,
      baseApi: this.$store.state.baseApi,
      score: 5, // 提交用
      good: {},
      goodId: Number,
      price: -1,
      isDiscount: false,
      discount: "",
      standards: [],
      checkedStandard: "",
      store: 0,
      showStore: false,
      count: 1,
      title: "",
      content: "",
      pagination: { current: 1, total: null, size: 4 },
      msg: [],

      // 星级筛选：0 表示全部；1~5 表示对应星级
      filterStar: 0
    };
  },
  created() {
    this.goodId = this.$route.params.goodId;
    this.getMsg();

    this.request.get("/api/good/" + this.goodId).then((res) => {
      if (res.code === "200") {
        this.good = res.data;
        const discount = this.good.discount;
        if (discount < 1) {
          this.isDiscount = true;
          this.discount = discount * 10 + "折";
        }
      } else {
        this.$router.go(0);
      }
    });

    this.request.get("/api/good/standard/" + this.goodId).then((res) => {
      if (res.code === "200") {
        const standards = JSON.parse(res.data);
        this.standards = standards;
        // 默认取价格区间
        this.price = this.getPriceRange(standards);
      } else {
        // 无规格
        this.price = this.good.price;
        this.store = this.good.store;
        this.showStore = true;
      }
    });
  },
  computed: {
    // 折后价（或区间）
    realPrice() {
      if (this.good.discount < 1) {
        if (isNaN(this.price)) {
          const down =
            this.price.substring(0, this.price.indexOf("元")) *
            this.good.discount;
          const up =
            this.price.substring(this.price.lastIndexOf(" ")) *
            this.good.discount;
          return down.toFixed(2) + " ~ " + up.toFixed(2);
        } else {
          return (this.price * this.good.discount).toFixed(2);
        }
      }
      return this.price;
    },

    // 前端本地筛选（作用于当前页 msg）
    shownMsg() {
      if (!this.filterStar) return this.msg;
      return this.msg.filter(
        (m) => this.normalizeScore(m.score) === this.filterStar
      );
    }
  },
  methods: {
    // 规范化后端返回的 score，确保是 0~5 的数字
    normalizeScore(score) {
      const n = Number(score);
      if (!isFinite(n)) return 0;
      if (n < 0) return 0;
      if (n > 5) return 5;
      return Math.round(n);
    },

    getPriceRange(standards) {
      const arr = standards.map((i) => i.price).sort((a, b) => a - b);
      return arr[0] === arr[arr.length - 1]
        ? arr[0]
        : arr[0] + "元 ~ " + arr[arr.length - 1];
    },
    change(standard) {
      this.showStore = true;
      this.price = standard.price;
      this.store = standard.store;
    },
    goToOrder() {
      if (this.standards.length !== 0 && this.checkedStandard === "") {
        this.$message.warning("请选择规格");
        return;
      }
      this.$router.push({
        name: "preOrder",
        query: {
          good: JSON.stringify(this.good),
          realPrice: this.realPrice,
          num: this.count,
          standard: this.checkedStandard
        }
      });
    },
    addToCart() {
      if (!localStorage.getItem("user")) {
        this.$router.push("/login");
        return;
      }
      if (!this.checkedStandard && this.standards.length) {
        this.$message.error("请选择规格");
        return;
      }
      this.request.get("/userid").then((res) => {
        const userId = res;
        this.userId = res;
        const cart = {
          userId,
          goodId: this.goodId,
          standard: this.checkedStandard,
          count: this.count
        };
        this.request.post("/api/cart", cart).then((res) => {
          if (res.code === "200") {
            this.$message.success("成功添加购物车");
          }
        });
      });
    },
    getMsg() {
      this.request.get("/userid").then((res) => {
        this.userId = res;
      });
      this.request
        .get(`/messages/${this.goodId}/${this.pagination.current}/${this.pagination.size}`)
        .then((res) => {
          if (res.code === 200) {
            this.msg = res.data.records;
            this.pagination = res.data;
          }
        });
    },
    handleSizeChange(val) {
      this.pagination.size = val;
      this.getMsg();
    },
    handleCurrentChange(val) {
      this.pagination.current = val;
      this.getMsg();
    },
    submit() {
      const date = new Date();
      this.request
        .post("/message", {
          title: this.title,
          goodId: this.goodId,
          content: this.content,
          score: this.score, // 传入评分
          time: date,
          userId: this.userId
        })
        .then((res) => {
          const code = res.data.code;
          if (code === 200) {
            this.$message.success("评论成功");
          }
          this.title = "";
          this.content = "";
          this.getMsg();
        });
    },
    replay(messageId) {
      this.$prompt("回复留言", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        inputPattern: /^[\s\S]*.*[^\s][\s\S]*$/,
        inputErrorMessage: "回复不能为空"
      })
        .then(({ value }) => {
          const date = new Date();
          this.request
            .post("/replay", {
              replay: value,
              replayTime: date,
              messageId
            })
            .then(() => this.getMsg());
          this.$message.success("回复成功");
        })
        .catch(() => {
          this.$message.info("取消输入");
        });
    }
  }
};
</script>

<style scoped>
.goodview {
  --bg: #ffffff;
  --card: #f5f7fb;
  --muted: #64748b;
  --primary: #3b82f6;
  --success: #16a34a;
  --danger: #ef4444;
}

.main-box {
  width: 1060px;
  margin: 20px auto 40px;
  padding: 28px;
  background: var(--bg);
  border-radius: 18px;
  box-shadow: 0 8px 28px rgba(31, 41, 55, 0.12);
}

/* 顶部：图片 + 详情 */
.product-wrap {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 48px;
  align-items: start;
}

/* 图片容器：更大，贴合灰色框 */
.image-box {
  width: 100%;
  height: 520px;              /* 放大高度 */
  background: #eef1f6;        /* 灰色框 */
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image {
  width: 100%;
  height: 100%;
  object-fit: cover;          /* 贴合容器（必要时轻微裁切） */
  transition: transform .35s ease;
}
.image-box:hover .image {
  transform: scale(1.02);
}

/* 右侧 */
.detail-box { width: 100%; }
.title .name {
  margin: 0 0 6px 0;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}
.title .desc {
  margin: 0;
  color: var(--muted);
  line-height: 1.6;
}

.price-box {
  margin-top: 22px;
  background: #f3f4f6;
  border-radius: 12px;
  padding: 16px 18px;
}
.price-grid dt {
  float: left;
  width: 48px;
  color: var(--muted);
}
.price-grid dd {
  margin-left: 56px;
  margin-bottom: 10px;
  color: #111827;
  font-size: 16px;
}
.price-grid .origin { text-decoration: line-through; }
.price-grid .now {
  color: #ef4444;
  font-size: 28px;
  font-weight: 800;
}

/* 元信息 */
.meta {
  margin-top: 14px;
  color: #0f172a;
  display: flex;
  gap: 16px;
}
.meta .stock { color: #2563eb; }

/* 规格 */
.standards {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
}
.standard-group { margin: 0; }
.standard :deep(.el-radio-button__inner) {
  border-radius: 10px !important;
  padding: 8px 14px;
}

/* 数量 */
.quantity { margin-top: 16px; }

/* 操作按钮 */
.actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}
.buy-btn { background: var(--success); border-color: var(--success); }
.cart-btn { background: var(--primary); border-color: var(--primary); }

/* 评论区域 */
.message-box {
  margin-top: 38px;
  background: #fafbff;
  border-radius: 16px;
  padding: 18px 18px 8px;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.06) inset;
}
.message-header {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 12px;
}
.message-editor .editor-row + .editor-row { margin-top: 10px; }
.editor-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.submit-btn { margin-left: 4px; }

/* 星级筛选 */
.star-filter {
  margin: 10px 0 6px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.filter-tip {
  font-size: 12px;
  color: #94a3b8;
}

/* 列表 */
.message-list { margin-top: 10px; }
.message-item {
  margin-bottom: 10px;
  border-radius: 12px;
}
.msg-head {
  display: flex;
  align-items: center;
  gap: 8px;
}
.message-item .content {
  margin: 6px 0 0;
  color: #1f2937;
  line-height: 1.7;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 6px;
}

/* 响应式 */
@media (max-width: 1024px) {
  .product-wrap {
    grid-template-columns: 1fr;
    gap: 24px;
  }
  .image-box { height: 420px; }
}
</style>
