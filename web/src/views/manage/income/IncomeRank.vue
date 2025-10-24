<!--
 * 销售统计图-排行榜（UI 美化版｜仅样式与布局）
 * @Author: ShanZhu
 * @Date: 2023-11-11
-->
<template>
  <div class="rank-page">
    <el-card class="rank-card" shadow="never" v-loading="loading">
      <div slot="header" class="card-header">
        <span>收入排行榜</span>
        <span class="muted">Top {{ num }}</span>
      </div>

      <div class="rank-list">
        <div
          class="rank-item"
          v-for="(item, index) in good"
          :key="item.id || index"
        >
          <income-item
            :index="index + 1"
            :good="item"
            :categories="categories"
          />
        </div>

        <el-empty
          v-if="!loading && good.length === 0"
          description="暂无数据"
          :image-size="80"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import incomeItem from "@/components/IncomeItem";
export default {
  name: "IncomeRank",
  components: { "income-item": incomeItem },
  data() {
    return {
      num: 10,
      good: [],
      categories: [],
      loading: false,
    };
  },
  created() {
    this.loading = true;
    // 先查分类
    this.request.get("/api/category").then(res => {
      if (res.code === "200") {
        this.categories = res.data || [];
      }
      // 再查排行
      this.request
        .get("/api/good/rank/", { params: { num: this.num } })
        .then(res => {
          if (res.code === "200") {
            this.good = res.data || [];
          }
        })
        .finally(() => { this.loading = false; });
    }).catch(() => {
      this.loading = false;
    });
  },
};
</script>

<style scoped>
.rank-page {
  padding: 16px 0;
}

.rank-card {
  max-width: 980px;
  margin: 0 auto;
  border: 1px solid #ebeef5;
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #303133;
}
.muted { color: #909399; font-weight: 400; }

.rank-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.rank-item {
  /* 包一层，便于统一间距，不影响子组件样式 */
  padding: 0;
}
</style>
