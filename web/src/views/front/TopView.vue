
<!--
 * 前台首页  方案A + A11y + 统一留白行高 + Skeleton
-->
<template>
  <div class="homepage">
    <search @search="handleSearch"></search>

    <div class="main-container">

      <!-- 顶部大横幅（骨架/真实） -->
      <section
        class="hero-banner"
        :aria-busy="isLoadingCarousel ? 'true' : 'false'"
        aria-label="首页推荐轮播"
      >
        <!-- Skeleton -->
        <div v-if="isLoadingCarousel" class="banner-skeleton skeleton-shimmer" role="img" aria-label="轮播加载中"></div>

        <!-- Carousel -->
        <el-carousel
          v-else
          height="420px"
          class="main-carousel"
          indicator-position="none"
          arrow="hover"
        >
          <el-carousel-item v-for="carousel in carousels" :key="carousel.id">
            <router-link
              :to="'/goodview/' + carousel.goodId"
              class="carousel-link"
              :aria-label="'查看商品：' + (carousel.title || '推荐商品')"
            >
              <img
                class="carousel-image"
                :src="baseApi + carousel.img"
                :alt="(carousel.title || '商品轮播图')"
              />
              <div class="carousel-overlay" aria-hidden="true">
                <div class="carousel-content">
                  <h3>精选推荐</h3>
                  <p>发现更多优质商品</p>
                </div>
              </div>
            </router-link>
          </el-carousel-item>
        </el-carousel>
      </section>

      <!-- 分类宫格（骨架/真实） -->
      <section
        class="categories-section"
        aria-labelledby="section-categories-title"
        :aria-busy="isLoadingIcons ? 'true' : 'false'"
      >
        <div class="section-header">
          <h2 id="section-categories-title" class="section-title">
            <span class="title-icon" aria-hidden="true">🧭</span>
            商品分类
          </h2>
          <div class="section-divider" aria-hidden="true"></div>
        </div>

        <!-- Skeleton -->
        <div v-if="isLoadingIcons" class="category-grid" role="list">
          <div v-for="n in 8" :key="'c-skel-'+n" class="category-tile">
            <div class="tile-top">
              <div class="icon-badge skeleton-shimmer" aria-hidden="true"></div>
              <div class="tile-title skeleton-line skeleton-shimmer" style="width:120px" aria-hidden="true"></div>
            </div>
            <div class="tile-links">
              <span class="chip skeleton-shimmer" style="width:60px" aria-hidden="true"></span>
              <span class="chip skeleton-shimmer" style="width:70px" aria-hidden="true"></span>
              <span class="chip skeleton-shimmer" style="width:50px" aria-hidden="true"></span>
              <span class="chip skeleton-shimmer more" style="width:56px" aria-hidden="true"></span>
            </div>
          </div>
        </div>

        <!-- Real -->
        <div v-else class="category-grid" role="list">
          <div
            v-for="(item, index) in icons"
            :key="index"
            class="category-tile"
            role="listitem"
          >
            <!-- Row 1：图标 + 标题 -->
            <div class="tile-top">
              <div class="icon-badge" aria-hidden="true">
                <span class="iconfont" v-html="item.value"></span>
              </div>
              <div class="tile-title">{{ firstCategoryName(item) }}</div>
            </div>

            <!-- Row 2：最多3个芯片 + 更多 -->
            <div class="tile-links">
              <router-link
                v-for="(c, i) in limitedCategories(item)"
                :key="i"
                :to="{ path: '/goodlist', query: { categoryId: c.id } }"
                class="chip"
                :aria-label="'查看 ' + c.name + ' 分类商品'"
              >
                {{ c.name }}
              </router-link>

              <!-- 更多：跳到列表页，带搜索词为该组标题 -->
              <button
                type="button"
                class="chip more"
                @click="goMore(item)"
                :aria-label="'查看更多：' + firstCategoryName(item)"
              >
                更多
              </button>
            </div>
          </div>
        </div>
      </section>

      <!-- 推荐商品（骨架/真实） -->
      <section
        class="products-section"
        aria-labelledby="section-products-title"
        :aria-busy="isLoadingGoods ? 'true' : 'false'"
      >
        <div class="section-header">
          <h2 id="section-products-title" class="section-title">
            <span class="title-icon" aria-hidden="true">🔥</span>
            推荐商品
          </h2>
        </div>
        <div class="section-divider" aria-hidden="true"></div>

        <!-- Skeleton -->
        <div v-if="isLoadingGoods" class="products-grid" role="list">
          <div v-for="n in 8" :key="'p-skel-'+n" class="product-card" role="listitem" aria-label="商品加载中">
            <div class="product-image-container">
              <div class="product-image skeleton-shimmer" aria-hidden="true"></div>
            </div>
            <div class="product-info">
              <div class="skeleton-line skeleton-shimmer" style="width:80%; margin-bottom:8px;" aria-hidden="true"></div>
              <div class="skeleton-line skeleton-shimmer" style="width:40%;" aria-hidden="true"></div>
            </div>
          </div>
        </div>

        <!-- Real -->
        <div v-else class="products-grid" role="list">
          <div v-for="good in good" :key="good.id" class="product-card" role="listitem">
            <router-link
              :to="'/goodview/' + good.id"
              class="product-link"
              :aria-label="'查看商品：' + good.name"
            >
              <div class="product-image-container">
                <img
                  :src="baseApi + good.imgs"
                  class="product-image"
                  :alt="good.name"
                />
                <div class="product-overlay" aria-hidden="true">
                  <div class="view-details">查看详情</div>
                </div>
              </div>
              <div class="product-info">
                <h3 class="product-name">{{ good.name }}</h3>
                <div class="product-price" aria-label="价格">
                  <span class="currency">￥</span>
                  <span class="amount">{{ good.price }}</span>
                </div>
              </div>
            </router-link>
          </div>
        </div>
      </section>

    </div>
  </div>
</template>

<script>
import search from "../../components/Search";

export default {
  name: "TopView",
  components: { search },
  data() {
    return {
      userId: null,
      carousels: [],
      good: [],
      baseApi: this.$store.state.baseApi,
      icons: [],
      searchText: "",

      // loading flags for skeletons
      isLoadingCarousel: true,
      isLoadingGoods: true,
      isLoadingIcons: true,
    };
  },
  created() {
    // 用户 ID
    if (localStorage.getItem("user")) {
      var user = JSON.parse(localStorage.getItem("user"));
      this.userId = user.id;
    } else {
      this.userId = 0;
    }

    // 推荐商品
    this.request
      .get("/api/good/all?userId=" + this.userId)
      .then((res) => {
        if (res.code === "200") {
          this.good = res.data;
        } else {
          this.$message.error(res.msg);
        }
      })
      .finally(() => {
        this.isLoadingGoods = false;
      });

    // 分类 + 图标（4列布局，展示最多 12 个）
    this.request
      .get("/api/icon")
      .then((res) => {
        if (res.code === "200") {
          var list = res.data || [];
          this.icons = list.slice(0, 12);
        }
      })
      .finally(() => {
        this.isLoadingIcons = false;
      });

    // 轮播
    this.request
      .get("/api/carousel")
      .then((res) => {
        if (res.code === "200") {
          this.carousels = res.data;
        }
      })
      .finally(() => {
        this.isLoadingCarousel = false;
      });
  },
  methods: {
    handleSearch(text) {
      this.searchText = text;
      this.$router.push({
        path: "/goodlist",
        query: { searchText: this.searchText },
      });
    },
    // 兼容旧编译器：避免模板可选链
    firstCategoryName(item) {
      if (!item || !item.categories || !item.categories.length) return "分类";
      return item.categories[0].name || "分类";
    },
    limitedCategories(item) {
      if (!item || !item.categories) return [];
      return item.categories.slice(0, 3);
    },
    goMore(item) {
      var kw = this.firstCategoryName(item);
      this.$router.push({ path: "/goodlist", query: { searchText: kw } });
    },
  },
};
</script>

<style scoped>
/* ========== 统一变量：间距 / 字体 / 颜色 ========== */
:root {
  /* spacing scale */
  --space-xxl: 32px;
  --space-xl: 24px;
  --space-lg: 20px;
  --space-md: 16px;
  --space-sm: 12px;

  /* radii & shadows */
  --radius-lg: 16px;
  --radius-md: 12px;
  --radius-sm: 8px;
  --shadow-1: 0 12px 36px rgba(31, 45, 61, 0.08);

  /* brand */
  --brand-grad: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --brand: #5b6ee1;
  --text-strong: #2c3e50;
  --text-subtle: #4a5568;
}

/* 统一默认行高与段落节奏 */
.homepage {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: var(--space-lg);
}
.main-container {
  max-width: 1240px;
  margin: 0 auto;
}
.section-title { line-height: 1.35; }
.product-name { line-height: 1.4; }
.carousel-content p,
.chip,
.tile-title { line-height: 1.6; }

/* ========== 顶部横幅 ========== */
.hero-banner {
  background: #fff;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-1);
  margin-bottom: var(--space-xl);
}

/* banner skeleton */
.banner-skeleton {
  height: 420px;
  border-radius: var(--radius-lg);
}

/* carousel */
.main-carousel { height: 420px; }
.carousel-link { display: block; height: 100%; position: relative; }
.carousel-image {
  width: 100%; height: 100%;
  object-fit: cover; object-position: center;
  transition: transform .35s ease;
}
.carousel-link:hover .carousel-image { transform: scale(1.03); }
.carousel-overlay {
  position: absolute; inset: auto 0 0 0;
  padding: 28px 26px;
  background: linear-gradient(180deg, rgba(0,0,0,0) 0%, rgba(0,0,0,.55) 100%);
  color: #fff;
}
.carousel-content h3 { margin: 0 0 6px; font-size: 22px; font-weight: 700; }
.carousel-content p { margin: 0; opacity: .9; }

/* Focus 可见性（键盘可达） */
.carousel-link:focus-visible,
.product-link:focus-visible,
.chip:focus-visible,
.chip.more:focus-visible {
  outline: 2px solid #7c90ff;
  outline-offset: 3px;
  border-radius: 8px;
}

/* ========== 标题区通用 ========== */
.section-header { text-align: center; margin: var(--space-lg) 0 var(--space-sm); }
.section-title {
  display: inline-flex; align-items: center; gap: 10px;
  font-size: 24px; font-weight: 700; color: var(--text-strong); margin: 0;
}
.title-icon { font-size: 26px; }
.section-divider {
  width: 72px; height: 3px; background: var(--brand-grad);
  margin: var(--space-sm) auto 0; border-radius: 2px;
}

/* ========== 分类宫格（方案A） ========== */
.categories-section {
  background: #fff;
  border-radius: var(--radius-lg);
  padding: var(--space-lg);
  box-shadow: var(--shadow-1);
  margin-bottom: var(--space-xl);
}
.category-grid { display: grid; gap: var(--space-lg); }
/* 断点：≥1200 4列；992–1199 3列；≤768 2列 */
@media (min-width: 1200px) { .category-grid { grid-template-columns: repeat(4, 1fr); } }
@media (min-width: 992px) and (max-width: 1199px) { .category-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px) { .category-grid { grid-template-columns: repeat(2, 1fr); } }

/* 卡片：两行栅格，固定高度，节奏更稳 */
.category-tile {
  background: #fafbff;
  border: 1px solid #eef0fb;
  border-radius: 14px;
  padding: var(--space-md);
  display: grid;
  grid-template-rows: auto 1fr;        /* Row1 标题；Row2 芯片 */
  row-gap: var(--space-sm);
  height: 130px;                        /* 固定高度，版面整齐 */
  transition: all .22s ease;
}
.category-tile:hover {
  background: #fff;
  box-shadow: 0 12px 28px rgba(31,45,61,.10);
  transform: translateY(-2px);
}

/* Row1：图标 + 标题 */
.tile-top { display: flex; align-items: center; gap: 10px; min-width: 0; }
.icon-badge {
  width: 44px; height: 44px; border-radius: 12px;
  background: rgba(102,126,234,.12);
  display: grid; place-items: center;
}
.icon-badge .iconfont { font-size: 20px; line-height: 1; color: var(--brand); }
.tile-title {
  font-weight: 700; color: #2d3748; white-space: nowrap;
  overflow: hidden; text-overflow: ellipsis;
}

/* Row2：芯片（左对齐），最多3个 + 更多 */
.tile-links {
  display: flex; flex-wrap: wrap; gap: 6px; justify-content: flex-start;
  align-content: flex-start;
}
.chip {
  text-decoration: none; color: var(--text-subtle); font-size: 12px;
  padding: 4px 10px; border-radius: 999px;
  background: #ffffff; border: 1px solid #e2e8f0;
  transition: all .15s ease;
  white-space: nowrap;
}
.chip:hover { color: #334155; border-color: #cbd5e1; transform: translateY(-1px); }
.chip.more {
  background: #f7f9ff; border-style: dashed; border-color: #d6d9e4;
  cursor: pointer;
}

/* ========== 推荐商品 ========== */
.products-section {
  background: white;
  border-radius: var(--radius-lg);
  padding: var(--space-lg);
  box-shadow: var(--shadow-1);
}
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: var(--space-lg);
}
.product-card {
  background: white;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  transition: all .25s ease;
}
.product-card:hover { transform: translateY(-6px); box-shadow: 0 16px 48px rgba(0,0,0,.12); }
.product-link { text-decoration: none; color: inherit; display: block; }
.product-image-container { position: relative; height: 210px; overflow: hidden; }
.product-image { width: 100%; height: 100%; object-fit: cover; transition: transform .3s; }
.product-card:hover .product-image { transform: scale(1.06); }
.product-overlay {
  position: absolute; inset: 0;
  background: rgba(0,0,0,.45); display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity .25s ease;
}
.product-card:hover .product-overlay { opacity: 1; }
.view-details {
  background: white; color: var(--text-strong); padding: 10px 18px; border-radius: 999px; font-weight: 700;
  transform: translateY(12px); transition: transform .25s ease;
}
.product-card:hover .view-details { transform: translateY(0); }
.product-info { padding: var(--space-md); }
.product-name {
  font-size: 16px; font-weight: 700; color: var(--text-strong); margin: 0 0 10px;
  line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.product-price { display: flex; align-items: baseline; color: #e74c3c; font-weight: 700; }
.currency { font-size: 14px; margin-right: 2px; }
.amount { font-size: 20px; }

/* ElementUI 轮播箭头（scoped 深度选择器） */
.main-carousel >>> .el-carousel__arrow {
  background: rgba(255, 255, 255, 0.88);
  color: var(--text-strong);
  width: 44px; height: 44px; border-radius: 50%; border: none; font-size: 16px;
}
.main-carousel >>> .el-carousel__arrow:hover { background: rgba(255,255,255,.96); }

/* ========== Skeleton 通用样式 ========== */
.skeleton-shimmer {
  position: relative;
  overflow: hidden;
  background: #eef1f6;
}
.skeleton-line { height: 12px; border-radius: 6px; }
.skeleton-shimmer::after {
  content: "";
  position: absolute; inset: 0;
  transform: translateX(-100%);
  background: linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,.6) 50%, rgba(255,255,255,0) 100%);
  animation: shimmer 1.4s infinite;
}
@keyframes shimmer {
  100% { transform: translateX(100%); }
}

/* ========== 减少动效（无障碍） ========== */
@media (prefers-reduced-motion: reduce) {
  .carousel-link:hover .carousel-image,
  .product-card:hover .product-image,
  .product-card,
  .category-tile,
  .view-details,
  .chip {
    transition: none !important;
    transform: none !important;
  }
  .skeleton-shimmer::after { animation: none !important; }
}
  
/* ========== 移动端调整 ========== */
@media (max-width: 768px) {
  .main-container { padding: 0 4px; }
  .main-carousel { height: 360px; }
  .products-grid { grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 14px; }
  .products-section { padding: 18px; }
}
</style>
