<template>
  <el-menu
    class="aside-menu"
    :default-openeds="['2', 'good']"
    background-color="rgb(28,28,28)"
    text-color="#fff"
    :collapse-transition="false"
    :collapse="isCollapse"
    :unique-opened="true"
    router
  >
    <!-- 顶部 LOGO / 标题 -->
    <div class="brand">
      <router-link to="/manage/home" class="brand-link">
        <!-- 路径保持你原来的相对路径；若报 404 可改为 @/resource/logo.png -->
        <img src="../resource/logo.png" class="brand-logo" alt="logo" />
        <span v-show="!isCollapse" class="brand-title">商城管理系统</span>
      </router-link>
    </div>

    <!-- 主页 -->
    <el-menu-item index="/manage/home">
      <template v-if="isCollapse">
        <el-tooltip :content="'主页'" :open-delay="tooltipOpenDelay" :placement="tooltipPlacement" effect="dark">
          <div class="mi"><i class="el-icon-house"></i></div>
        </el-tooltip>
      </template>
      <template v-else>
        <i class="el-icon-house"></i><span slot="title">主页</span>
      </template>
    </el-menu-item>

    <!-- 前台 -->
    <el-menu-item index="/">
      <template v-if="isCollapse">
        <el-tooltip :content="'前台'" :open-delay="tooltipOpenDelay" :placement="tooltipPlacement" effect="dark">
          <div class="mi"><i class="el-icon-s-home"></i></div>
        </el-tooltip>
      </template>
      <template v-else>
        <i class="el-icon-s-home"></i><span slot="title">前台</span>
      </template>
    </el-menu-item>

    <!-- 外层分组：index='2'（保留你的结构与默认展开） -->
    <el-submenu index="2" class="group">
      <template slot="title">
        <i class="el-icon-menu"></i>
        <span class="group-title">系统管理</span>
      </template>

      <!-- 账户相关 -->
      <el-submenu v-show="userGroup" index="user" class="subgroup">
        <template slot="title"><span class="subgroup-title">账户相关</span></template>
        <el-menu-item v-if="menuFlags.userMenu" index="/manage/user">
          <template v-if="isCollapse">
            <el-tooltip :content="'用户管理'" :open-delay="tooltipOpenDelay" :placement="tooltipPlacement" effect="dark">
              <div class="mi">用</div>
            </el-tooltip>
          </template>
          <template v-else>用户管理</template>
        </el-menu-item>
      </el-submenu>

      <!-- 文件相关 -->
      <el-submenu v-if="fileGroup" index="file" class="subgroup">
        <template slot="title"><span class="subgroup-title">文件相关</span></template>
        <el-menu-item v-if="menuFlags.fileMenu" index="/manage/file">
          <template v-if="isCollapse">
            <el-tooltip :content="'文件管理'" :open-delay="tooltipOpenDelay" :placement="tooltipPlacement" effect="dark">
              <div class="mi">文</div>
            </el-tooltip>
          </template>
          <template v-else>文件管理</template>
        </el-menu-item>
        <el-menu-item v-if="menuFlags.avatarMenu" index="/manage/avatar">
          <template v-if="isCollapse">
            <el-tooltip :content="'头像管理'" :open-delay="tooltipOpenDelay" :placement="tooltipPlacement" effect="dark">
              <div class="mi">头</div>
            </el-tooltip>
          </template>
          <template v-else>头像管理</template>
        </el-menu-item>
      </el-submenu>

      <!-- 商城管理 -->
      <el-submenu v-if="GoodGroup" index="good" class="subgroup">
        <template slot="title"><span class="subgroup-title">商城管理</span></template>
        <el-menu-item v-if="menuFlags.categoryMenu" index="/manage/category">
          <template v-if="isCollapse">
            <el-tooltip :content="'商品分类管理'" :open-delay="tooltipOpenDelay" :placement="tooltipPlacement" effect="dark">
              <div class="mi">分</div>
            </el-tooltip>
          </template>
          <template v-else>商品分类管理</template>
        </el-menu-item>
        <el-menu-item v-if="menuFlags.carouselMenu" index="/manage/carousel">
          <template v-if="isCollapse">
            <el-tooltip :content="'轮播图管理'" :open-delay="tooltipOpenDelay" :placement="tooltipPlacement" effect="dark">
              <div class="mi">轮</div>
            </el-tooltip>
          </template>
          <template v-else>轮播图管理</template>
        </el-menu-item>
        <el-menu-item v-if="menuFlags.goodMenu" index="/manage/good">
          <template v-if="isCollapse">
            <el-tooltip :content="'商品管理'" :open-delay="tooltipOpenDelay" :placement="tooltipPlacement" effect="dark">
              <div class="mi">品</div>
            </el-tooltip>
          </template>
          <template v-else>商品管理</template>
        </el-menu-item>
        <el-menu-item v-if="menuFlags.orderMenu" index="/manage/order">
          <template v-if="isCollapse">
            <el-tooltip :content="'订单管理'" :open-delay="tooltipOpenDelay" :placement="tooltipPlacement" effect="dark">
              <div class="mi">订</div>
            </el-tooltip>
          </template>
          <template v-else>订单管理</template>
        </el-menu-item>
      </el-submenu>

      <!-- 销售额统计 -->
      <el-submenu v-if="incomeGroup" index="income" class="subgroup">
        <template slot="title"><span class="subgroup-title">销售额统计</span></template>
        <el-menu-item v-if="menuFlags.incomeChartMenu" index="/manage/incomeChart">
          <template v-if="isCollapse">
            <el-tooltip :content="'图表分析'" :open-delay="tooltipOpenDelay" :placement="tooltipPlacement" effect="dark">
              <div class="mi">图</div>
            </el-tooltip>
          </template>
          <template v-else>图表分析</template>
        </el-menu-item>
        <el-menu-item v-if="menuFlags.incomeRankMenu" index="/manage/incomeRank">
          <template v-if="isCollapse">
            <el-tooltip :content="'收入排行榜'" :open-delay="tooltipOpenDelay" :placement="tooltipPlacement" effect="dark">
              <div class="mi">榜</div>
            </el-tooltip>
          </template>
          <template v-else>收入排行榜</template>
        </el-menu-item>
      </el-submenu>
    </el-submenu>
  </el-menu>
</template>

<script>
import request from "@/utils/request";

export default {
  name: "Aside",
  props: {
    isCollapse: Boolean,
    tooltipPlacement: { type: String, default: "right" },
    tooltipOpenDelay: { type: Number, default: 200 },
  },
  data() {
    return {
      role: "user",
      menuFlags: {
        userMenu: false,
        fileMenu: false,
        avatarMenu: false,
        goodMenu: false,
        carouselMenu: false,
        orderMenu: false,
        categoryMenu: false,
        incomeChartMenu: false,
        incomeRankMenu: false,
      },
    };
  },
  computed: {
    userGroup() { return this.menuFlags.userMenu; },
    fileGroup() { return this.menuFlags.fileMenu || this.menuFlags.avatarMenu; },
    GoodGroup() { return this.menuFlags.goodMenu || this.menuFlags.orderMenu || this.menuFlags.categoryMenu || this.menuFlags.carouselMenu; },
    incomeGroup() { return this.menuFlags.incomeChartMenu || this.menuFlags.incomeRankMenu; },
  },
  created() {
    request.post("/role").then((res) => {
      if (res.code === "200") {
        this.role = res.data;
        if (this.role === "admin") {
          this.menuFlags = {
            userMenu: true, fileMenu: true, avatarMenu: true,
            goodMenu: true, carouselMenu: true, orderMenu: true, categoryMenu: true,
            incomeChartMenu: true, incomeRankMenu: true
          };
        } else if (this.role === "user") {
          // 按需开放
        }
      }
    });
  },
};
</script>

<style scoped>
/* 侧栏自身也可滚动（双保险） */
.aside-menu { height: 100%; overflow-y: auto; overflow-x: hidden; border-right: none; }

/* 滚动条 */
.aside-menu::-webkit-scrollbar { width: 8px; }
.aside-menu::-webkit-scrollbar-thumb { background: rgba(255,255,255,.18); border-radius: 4px; }

/* 顶部品牌区 */
.brand { height: 60px; line-height: 60px; padding-left: 20px; border-bottom: 1px solid rgba(255,255,255,.06); display: flex; align-items: center; }
.brand-link { display: inline-flex; align-items: center; text-decoration: none; }
.brand-logo { width: 40px; margin-right: 6px; }
.brand-title { color: #ecf5ff; font-size: 18px; font-weight: 600; letter-spacing: .5px; }

/* —— 深度选择器：Vue2 兼容（>>> /deep/ ::v-deep） —— */

/* 一级分组标题（外层 index='2'）更醒目 */
.group >>> .el-submenu__title,
.group /deep/ .el-submenu__title,
.group ::v-deep .el-submenu__title {
  font-weight: 700; color: #d6e4ff !important; font-size: 14px; letter-spacing: .5px;
}
.group >>> .el-submenu__title i,
.group /deep/ .el-submenu__title i,
.group ::v-deep .el-submenu__title i {
  margin-right: 6px; color: #bfcbd9;
}

/* 二级分组标题 */
.subgroup >>> .el-submenu__title,
.subgroup /deep/ .el-submenu__title,
.subgroup ::v-deep .el-submenu__title {
  font-weight: 600; color: #c9d6ff !important; font-size: 13px; padding-left: 16px !important;
}

/* 子菜单项：缩进 & 弱化 */
.el-submenu >>> .el-menu-item,
.el-submenu /deep/ .el-menu-item,
.el-submenu ::v-deep .el-menu-item {
  font-size: 13px; color: #d8d8d8; padding-left: 40px !important;
}

/* hover */
.el-menu-item:hover,
.group >>> .el-submenu__title:hover,
.group /deep/ .el-submenu__title:hover,
.group ::v-deep .el-submenu__title:hover,
.subgroup >>> .el-submenu__title:hover,
.subgroup /deep/ .el-submenu__title:hover,
.subgroup ::v-deep .el-submenu__title:hover {
  background: rgba(255,255,255,.06) !important;
}

/* 激活态（左侧高亮条） */
.el-menu-item.is-active {
  color: #409eff !important; background: rgba(64,158,255,.12) !important; position: relative;
}
.el-menu-item.is-active::before {
  content: ""; position: absolute; left: 0; top: 8px; bottom: 8px; width: 3px; background: #409eff; border-radius: 0 2px 2px 0;
}

/* 折叠时 tooltip 触发盒（图标/字占位） */
.mi { width: 40px; height: 40px; display: grid; place-items: center; }
</style>
