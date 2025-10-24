<!--
 * 销售统计图-如、图表（UI 美化版｜仅样式与布局）
 * @Author: ShanZhu
 * @Date: 2023-11-11
-->
<template>
  <div class="income-chart-page">
    <el-tabs v-model="activeName" @tab-click="handleClick" class="tabs">
      <!-- 总额悬浮卡片 -->
      <div class="total-card-wrapper">
        <el-card class="total-card" shadow="always">
          ￥总计：{{ total | numFilter }}
        </el-card>
      </div>

      <!-- 柱状图 -->
      <el-tab-pane label="各类收入柱状图" name="bar">
        <div id="bar" class="chart-box"></div>
      </el-tab-pane>

      <!-- 饼图 -->
      <el-tab-pane label="各类收入饼图" name="pie">
        <div id="pie" class="chart-box pie-box"></div>
      </el-tab-pane>

      <!-- 本周收入折线图 -->
      <el-tab-pane label="本周收入" name="line1">
        <div id="weekLine" class="chart-box"></div>
      </el-tab-pane>

      <!-- 本月收入折线图 -->
      <el-tab-pane label="本月收入" name="line2">
        <div id="monthLine" class="chart-box"></div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import * as echarts from "echarts";
export default {
  name: "IncomeChart",
  data() {
    return {
      sumIncome: 0,
      categoryIncomes: [],
      categoryNames: [],
      incomes: [],
      activeName: "bar",
      totalAll: 0,
      totalWeek: 0,
      totalMonth: 0,
      total: 0,
      // chart 实例
      barChart: null,
      pieChart: null,
      lineChart1: null,
      lineChart2: null,
    };
  },
  methods: {
    handleClick(tab) {
      switch (tab.name) {
        case "bar":
        case "pie":
          this.total = this.totalAll; break;
        case "line1":
          this.total = this.totalWeek; break;
        case "line2":
          this.total = this.totalMonth; break;
      }
      this.$nextTick(this.handleResize);
    },
    handleResize() {
      this.barChart && this.barChart.resize();
      this.pieChart && this.pieChart.resize();
      this.lineChart1 && this.lineChart1.resize();
      this.lineChart2 && this.lineChart2.resize();
    },
    fmtMoney(val) {
      if (val == null || val === "") return 0;
      return Number(val).toLocaleString(undefined, { maximumFractionDigits: 2 });
    }
  },

  mounted() {
    // 初始化实例
    this.barChart = echarts.init(document.getElementById("bar"));
    this.pieChart = echarts.init(document.getElementById("pie"));
    this.lineChart1 = echarts.init(document.getElementById("weekLine"));
    this.lineChart2 = echarts.init(document.getElementById("monthLine"));

    const axisStyle = { color: "#A0A3A9" };
    const splitLine = { lineStyle: { color: "#E9EEF3" } };

    const barOption = {
      color: ["#5B8FF9"],
      tooltip: {
        trigger: "item",
        formatter: (p) => `${p.name}<br/>收入：￥${this.fmtMoney(p.value)}`
      },
      title: { text: "收入统计柱状图", left: "center", top: 10 },
      grid: { left: "6%", right: "4%", bottom: "8%", top: 60, containLabel: true },
      xAxis: {
        type: "category",
        data: [],
        axisLine: { lineStyle: axisStyle },
        axisTick: { show: false },
        axisLabel: { color: "#606266" }
      },
      yAxis: {
        type: "value",
        axisLine: { lineStyle: axisStyle },
        splitLine,
        axisLabel: { color: "#606266" }
      },
      series: [{
        data: [],
        type: "bar",
        barMaxWidth: 48,
        label: {
          show: true,
          position: "top",
          formatter: (p) => this.fmtMoney(p.value)
        },
        itemStyle: { borderRadius: [6, 6, 0, 0] }
      }]
    };

    const pieOption = {
      color: ["#5B8FF9","#61DDAA","#65789B","#F6BD16","#7262fd","#78D3F8"],
      tooltip: {
        trigger: "item",
        formatter: (p) => `${p.name}<br/>收入：￥${this.fmtMoney(p.value)}（${p.percent}%）`
      },
      title: { text: "收入统计饼图", left: "center", top: 10 },
      series: [{
        type: "pie",
        radius: ["34%","58%"],
        center: ["50%","55%"],
        data: [],
        label: {
          formatter: ({ name, value }) => `${name}\n￥${this.fmtMoney(value)}`
        }
      }]
    };

    const lineOption1 = {
      color: ["#5AD8A6"],
      tooltip: { trigger: "item", formatter: (p) => `￥${this.fmtMoney(p.value)}` },
      title: { text: "本周收入", left: "center", top: 10 },
      grid: { left: "6%", right: "4%", bottom: "8%", top: 60, containLabel: true },
      xAxis: {
        type: "category",
        data: ["周一","周二","周三","周四","周五","周六","周日"],
        axisLine: { lineStyle: axisStyle }, axisTick: { show: false }, axisLabel: { color:"#606266" }
      },
      yAxis: { type: "value", axisLine: { lineStyle: axisStyle }, splitLine, axisLabel: { color:"#606266" } },
      series: [{ data: [], type: "line", smooth: true, symbol: "circle", symbolSize: 6, lineStyle: { width: 3 } }]
    };

    const lineOption2 = {
      color: ["#F6BD16"],
      tooltip: { trigger: "item", formatter: (p) => `￥${this.fmtMoney(p.value)}` },
      title: { text: "本月收入", left: "center", top: 10 },
      grid: { left: "6%", right: "4%", bottom: "8%", top: 60, containLabel: true },
      xAxis: {
        type: "category",
        data: [],
        axisLine: { lineStyle: axisStyle }, axisTick: { show: false }, axisLabel: { color:"#606266" }
      },
      yAxis: { type: "value", axisLine: { lineStyle: axisStyle }, splitLine, axisLabel: { color:"#606266" } },
      series: [{ data: [], type: "line", smooth: true, symbol: "circle", symbolSize: 6, lineStyle: { width: 3 } }]
    };

    // 渲染柱状 & 饼图
    this.request.get("/api/income/chart").then((res) => {
      if (res.code === "200") {
        const categoryIncomes = res.data.categoryIncomes || [];
        const categoryNames = categoryIncomes.map(i => i.categoryName);
        const incomes = categoryIncomes.map(i => i.categoryIncome);

        barOption.xAxis.data = categoryNames;
        barOption.series[0].data = incomes;
        this.barChart.setOption(barOption);

        pieOption.series[0].data = categoryNames.map((n, idx) => ({ name: n, value: incomes[idx] }));
        this.pieChart.setOption(pieOption);

        // 计算总和
        const sum = incomes.reduce((s, v) => s + v, 0);
        this.total = sum;
        this.totalAll = sum;
      }
    });

    // 本周折线
    this.request.get("/api/income/week").then((res) => {
      if (res.code === "200") {
        const weekIncome = res.data.weekIncome || [];
        lineOption1.series[0].data = weekIncome;
        this.lineChart1.setOption(lineOption1);
        this.totalWeek = weekIncome.reduce((s, v) => s + v, 0);
      }
    });

    // 本月折线
    this.request.get("/api/income/month").then((res) => {
      if (res.code === "200") {
        lineOption2.xAxis.data = res.data.monthDays || [];
        const monthIncome = res.data.monthIncome || [];
        lineOption2.series[0].data = monthIncome;
        this.lineChart2.setOption(lineOption2);
        this.totalMonth = monthIncome.reduce((s, v) => s + v, 0);
      }
    });

    window.addEventListener("resize", this.handleResize);
  },
  beforeDestroy() {
    window.removeEventListener("resize", this.handleResize);
    this.barChart && this.barChart.dispose();
    this.pieChart && this.pieChart.dispose();
    this.lineChart1 && this.lineChart1.dispose();
    this.lineChart2 && this.lineChart2.dispose();
  },
  filters: {
    numFilter(value) {
      // 截取到小数点后两位
      let realVal = Number(value).toFixed(2);
      return Number(realVal);
    },
  },
};
</script>

<style scoped>
.income-chart-page {
  padding: 8px 0;
}

/* 总额卡片悬浮在图表区域左上角 */
.total-card-wrapper {
  position: relative;
  height: 0;
}
.total-card {
  position: absolute;
  left: 24px;
  top: 16px;
  z-index: 2;
  border-radius: 12px;
  font-weight: bold;
  font-size: 20px;
  color: #ffb02a;
  padding: 10px 16px;
}

/* 图表容器自适应宽度 */
.chart-box {
  width: 100%;
  max-width: 1200px;
  height: 500px;
  margin: 64px auto 16px; /* 顶部留给悬浮卡片 */
}
.pie-box {
  max-width: 720px;
  height: 520px;
}

.tabs ::v-deep .el-tabs__nav-wrap { padding-left: 6px; }
</style>
