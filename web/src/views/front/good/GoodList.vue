<!--
 * 商品列表（美化版 | 不改接口、不改路由）
 * @Author: ShanZhu
 * @Date: 2023-11-11
-->
<template>
  <div class="goodlist-page">
    <search @search="handleSearch" />

    <div class="main-box">
      <!-- 顶部标题 -->
      <header class="section-head">
        <h2 class="title">
          <span class="dot"></span> 商品分类
        </h2>
        <div class="divider" aria-hidden="true"></div>
      </header>

      <!-- 分类入口 -->
      <div class="category-bar">
        <el-row :gutter="16" class="category-row">
          <el-col
            v-for="(item, index) in icons"
            :key="index"
            :span="6"
            class="category-col"
          >
            <div class="cat-box">
              <i class="iconfont cat-icon" v-html="item.value"></i>
              <div class="cat-links">
                <a
                  v-for="(category, index2) in item.categories"
                  :key="index2"
                  href="#"
                  class="cat-chip"
                  :class="{ active: categoryId == category.id }"
                  @click.prevent="load(category.id)"
                >
                  {{ category.name }}
                </a>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 商品网格 -->
      <section class="goods-grid" v-loading="isLoading">
        <el-row :gutter="24">
          <el-col
            v-for="good in good"
            :key="good.id"
            :span="6"
            class="good-col"
          >
            <router-link :to="'/goodview/' + good.id" class="card-link">
              <article class="product-card">
                <div class="thumb">
                  <img
                    :src="baseApi + good.imgs"
                    :alt="good.name"
                    @error="onImgError"
                  />
                  <div class="thumb-overlay">
                    <span>查看详情</span>
                  </div>
                </div>
                <div class="card-body">
                  <h3 class="name" :title="good.name">{{ good.name }}</h3>
                  <div class="price">￥{{ formatPrice(good.price) }}</div>
                </div>
              </article>
            </router-link>
          </el-col>
        </el-row>
      </section>

      <!-- 分页 -->
      <div class="pager">
        <el-pagination
          background
          :hide-on-single-page="false"
          :current-page="currentPage"
          :page-size="pageSize"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="handleCurrentPage"
        />
      </div>
    </div>
  </div>
</template>

<script>
import search from "../../../components/Search";
export default {
  name: "GoodList",
  components: { search },
  data() {
    return {
      icons: [],
      total: 0,
      pageSize: 9,
      currentPage: 1,
      categoryId: Number,
      searchText: "",
      good: [],
      baseApi: this.$store.state.baseApi,
      isLoading: false,
    };
  },
  created() {
    // 二者一般不同时存在
    this.searchText = this.$route.query.searchText;
    this.categoryId = this.$route.query.categoryId;
    this.loadCategories();
    this.load();
  },
  methods: {
    loadCategories() {
      this.request.get("/api/icon").then((res) => {
        if (res.code === "200") {
          this.icons = res.data || [];
        }
      });
    },
    handleCurrentPage(currentPage) {
      this.currentPage = currentPage;
      this.load();
    },
    handleSearch(text) {
      this.searchText = text;
      this.currentPage = 1;
      this.load();
    },
    load(categoryId) {
      if (categoryId !== undefined) {
        this.categoryId = categoryId;
        this.$router.push({
          path: "/goodlist",
          query: { categoryId: this.categoryId },
        });
      }
      this.isLoading = true;
      this.request
        .get("/api/good/page", {
          params: {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            searchText: this.searchText,
            categoryId: this.categoryId,
          },
        })
        .then((res) => {
          if (res.code === "200") {
            this.total = res.data.total || 0;
            this.good = res.data.records || [];
          } else {
            this.$message.error(res.msg || "加载失败");
          }
        })
        .finally(() => {
          this.isLoading = false;
        });
    },
    formatPrice(v) {
      const n = Number(v);
      return isNaN(n) ? "0.00" : n.toFixed(2);
    },
    onImgError(e) {
      // 兜底占位图（也可换成你自己的静态资源）
      e.target.style.opacity = 0.8;
      e.target.src =
        "data:image/svg+xml;utf8," +
        encodeURIComponent(
          `<svg xmlns='http://www.w3.org/2000/svg' width='400' height='240'>
             <rect width='100%' height='100%' fill='#f3f4f6'/>
             <text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' fill='#9ca3af' font-size='16'>No Image</text>
           </svg>`
        );
    },
  },
};
</script>

<style scoped>
.goodlist-page {
  padding-bottom: 24px;
}

/* 容器卡片 */
.main-box {
  background: #fff;
  border-radius: 24px;
  padding: 20px 28px 28px;
  margin: 10px auto;
  box-shadow: 0 12px 32px rgba(31, 45, 61, 0.08);
}

/* 标题区域 */
.section-head .title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 22px;
  font-weight: 800;
  color: #1f2937;
  margin: 0;
}
.section-head .dot {
  width: 10px;
  height: 10px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
}
.section-head .divider {
  width: 80px;
  height: 4px;
  border-radius: 2px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  margin: 10px 0 12px;
}

/* 分类条 */
.category-bar {
  margin-bottom: 8px;
}
.category-col {
  margin-bottom: 10px;
}
.cat-box {
  display: flex;
  align-items: center;
  gap: 10px;
}
.cat-icon {
  font-size: 18px;
  color: #667eea;
}
.cat-links {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.cat-chip {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  background: #f3f4f6;
  color: #374151;
  font-size: 13px;
  text-decoration: none;
  transition: all 0.2s ease;
}
.cat-chip:hover {
  background: #e5e7eb;
}
.cat-chip.active {
  background: #eef2ff;
  color: #4f46e5;
  box-shadow: 0 0 0 1px #c7d2fe inset;
}

/* 商品网格 */
.goods-grid {
  margin-top: 8px;
}

/* 卡片 */
.card-link {
  text-decoration: none;
  color: inherit;
}
.product-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 26px rgba(31, 45, 61, 0.08);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 36px rgba(31, 45, 61, 0.12);
}

/* 图片：统一高度，裁剪不变形 */
.thumb {
  position: relative;
  height: 220px;          /* 统一高度，行齐整 */
  overflow: hidden;
  background: #f3f4f6;
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;      /* 关键：裁剪不拉伸 */
  display: block;
  transition: transform 0.3s ease;
}
.product-card:hover .thumb img {
  transform: scale(1.05);
}

/* 悬浮文案 */
.thumb-overlay {
  position: absolute; inset: 0;
  display: grid; place-items: center;
  color: #fff; font-weight: 700;
  letter-spacing: 1px;
  background: rgba(0, 0, 0, 0.25);
  opacity: 0; transition: opacity 0.2s ease;
}
.product-card:hover .thumb-overlay {
  opacity: 1;
}

/* 文本 */
.card-body {
  padding: 12px 14px 14px;
}
.name {
  margin: 0 0 6px 0;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;   /* 最多两行 */
  -webkit-box-orient: vertical;
  overflow: hidden;
  color: #1f2937;
}
.price {
  color: #e0525c;
  font-weight: 800;
}

/* 分页 */
.pager {
  margin-top: 18px;
  text-align: center;
}

/* 小屏优化 */
@media (max-width: 1024px) {
  .thumb { height: 200px; }
}
@media (max-width: 768px) {
  .thumb { height: 180px; }
}
</style>
