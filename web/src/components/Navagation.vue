<!--
 * 前台页面-导航栏（UI 美化版｜仅样式与布局）
 * - 不改任何接口/字段/路由
 * - 使用 $route.path 作为激活高亮（不影响你的路由配置）
 * @Author: ShanZhu
 * @Date: 2023-11-11
-->
<template>
  <div class="navagation">
    <el-row type="flex" align="middle" class="nav-row">

      <!-- 左侧 Logo + 品牌 -->
      <el-col :span="4" class="brand-col">
        <a href="/" class="brand-link">
          <img src="../resource/logo.png" alt="logo" class="logo" />
          <span class="brand-text">好物集商城</span>
        </a>
      </el-col>

      <!-- 中间菜单 -->
      <el-col :span="16" class="menu-col">
        <el-menu
          :default-active="$route.path"
          class="main-menu"
          mode="horizontal"
          router
          background-color="transparent"
          text-color="#303133"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/" class="menu-item">商城首页</el-menu-item>
          <el-menu-item index="/goodList" class="menu-item">商品分类</el-menu-item>
          <el-menu-item index="/cart" class="menu-item">我的购物车</el-menu-item>
          <el-menu-item index="/orderlist" class="menu-item">我的订单</el-menu-item>
          <el-menu-item index="/manage" class="menu-item" v-if="role === 'admin'">后台管理</el-menu-item>
        </el-menu>
      </el-col>

      <!-- 右侧用户区 -->
      <el-col :span="4" class="user-col">
        <el-dropdown class="user-dropdown">
          <span class="el-dropdown-link user-entry">
            <img v-if="user.avatarUrl != null" :src="baseApi + user.avatarUrl" class="avatar" />
            <span class="nickname ellipsis">{{ user.nickname }}</span>
            <i class="el-icon-arrow-down caret"></i>
          </span>

          <el-dropdown-menu slot="dropdown" class="user-menu">
            <el-dropdown-item>
              <!-- 登录（未登录才显示） -->
              <div @click="$router.push({ path: '/login', query: { to: '/' } })" v-show="!loginStatus">登录</div>
            </el-dropdown-item>
            <el-dropdown-item v-show="loginStatus">
              <div @click="$router.push('/person')">个人信息</div>
            </el-dropdown-item>
            <el-dropdown-item v-show="loginStatus">
              <div @click="logout">退出</div>
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </el-col>

    </el-row>
  </div>
</template>

<script>
export default {
  name: "Navagation",
  props: {
    user: Object,
    loginStatus: Boolean,
    role: String,
  },
  data() {
    return {
      activeIndex: "1",   // 保留你的原字段（未使用，不影响）
      activeIndex2: "1",
      baseApi: this.$store.state.baseApi,
    };
  },
  methods: {
    // 退出账号
    logout() {
      localStorage.removeItem("user");
      this.$router.go(0);
      this.$message.success("退出成功");
    },
  },
};
</script>

<style scoped>
/* 顶栏容器：吸顶 + 细阴影 */
.navagation {
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  height: 60px;
  line-height: 60px;
  background-color: #ffffff;
  border-bottom: 1px solid #ebeef5;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
}

.nav-row {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px;
}

/* 品牌区域 */
.brand-col { display: flex; align-items: center; }
.brand-link { display: inline-flex; align-items: center; text-decoration: none; color: #303133; font-weight: 700; }
.logo { width: 32px; height: 32px; margin-right: 8px; display: inline-block; position: relative; top: 2px; }
.brand-text { font-size: 18px; }

/* 菜单 */
.menu-col { display: flex; align-items: center; }
.main-menu {
  border-bottom: none;
}

/* 调整水平菜单的间距与激活样式 */
::v-deep .el-menu--horizontal > .el-menu-item {
  padding: 0 22px;
  font-size: 14px;
  transition: color .2s ease, border-color .2s ease;
}
::v-deep .el-menu--horizontal > .el-menu-item:hover {
  color: #409EFF;
  background-color: transparent;
}
::v-deep .el-menu--horizontal > .el-menu-item.is-active {
  border-bottom: 2px solid #409EFF;
  color: #409EFF !important;
}

/* 用户区域 */
.user-col { display: flex; justify-content: flex-end; align-items: center; }
.user-dropdown { cursor: pointer; }
.user-entry { display: inline-flex; align-items: center; }
.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  margin-right: 8px;
  object-fit: cover;
}
.nickname { max-width: 110px; color: #303133; }
.caret { margin-left: 6px; color: #909399; }

/* 下拉菜单居中对齐（保持你的原意） */
.user-menu { text-align: center; }

/* 通用 */
a { text-decoration: none; }
.ellipsis { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* 窄屏优化 */
@media (max-width: 992px) {
  ::v-deep .el-menu--horizontal > .el-menu-item { padding: 0 14px; }
  .brand-text { display: none; } /* Logo 保留，节省空间 */
}
</style>
