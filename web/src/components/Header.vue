<!--
 * 后台头部（UI 美化版｜仅样式与布局）
 * - 保留：props collapseIcon/collapseTitle/user；事件 $emit('collapse')
 * - 不改：路由/接口/数据结构
 * @Author: ShanZhu
 * @Date: 2023-11-11
-->
<template>
  <div class="header-bar">
    <!-- 左侧：折叠/返回 + 面包屑 -->
    <div class="left">
      <el-tooltip :content="collapseTitle" placement="bottom">
        <i :class="['icon-btn', collapseIcon]" @click="$emit('collapse')"></i>
      </el-tooltip>

      <el-tooltip content="返回" placement="bottom">
        <i class="icon-btn el-icon-back" @click="back"></i>
      </el-tooltip>

      <el-breadcrumb class="breadcrumb" separator-class="el-icon-arrow-right">
        <el-breadcrumb-item :to="{ path: '/manage/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item>{{ routePath }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 右侧：头像/昵称 + 下拉 -->
    <div class="right">
      <el-dropdown class="user-dropdown">
        <span class="user-entry el-dropdown-link">
          <img :src="baseApi + user.avatarUrl" class="avatar" />
          <span class="nickname ellipsis">{{ user.nickname }}</span>
          <i class="el-icon-arrow-down caret"></i>
        </span>

        <el-dropdown-menu slot="dropdown" class="user-menu">
          <el-dropdown-item>
            <div @click="$router.push('/manage/person')">个人信息</div>
          </el-dropdown-item>
          <el-dropdown-item>
            <div @click="logout">退出</div>
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
export default {
  name: "Header",
  props: {
    collapseIcon: String,
    collapseTitle: String,
    user: Object
  },
  data() {
    return {
      routePath: '',
      baseApi: this.$store.state.baseApi,
    }
  },
  created() {
    this.routePath = this.$route.meta.path;
  },
  watch: {
    '$route': function () {
      this.routePath = this.$route.meta.path;
    },
  },
  methods:{
    // 登出
    logout() {
      localStorage.removeItem("user");
      this.$router.push('/login');
      this.$message.success("退出成功");
    },
    // 返回
    back(){
      this.$router.go(-1)
    }
  }
}
</script>

<style scoped>
/* 顶栏容器：吸顶 + 细阴影 */
.header-bar{
  position: sticky;
  top: 0;
  z-index: 100;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px 0 12px;
  background: #ffffff;
  border-bottom: 1px solid #ebeef5;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
}

/* 左右区域 */
.left{ display: flex; align-items: center; min-width: 0; }
.right{ display: flex; align-items: center; }

/* 圆角图标按钮 */
.icon-btn{
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
  padding: 8px;
  margin-right: 6px;
  border-radius: 8px;
  color: #606266;
  transition: background-color .15s ease, color .15s ease;
}
.icon-btn:hover{ background: #f5f7fa; color: #409EFF; }

/* 面包屑 */
.breadcrumb{
  margin-left: 10px;
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
::v-deep .el-breadcrumb__inner{
  color: #606266;
}
::v-deep .el-breadcrumb__item:last-child .el-breadcrumb__inner{
  color: #303133;
  font-weight: 600;
}

/* 用户区域 */
.user-dropdown{ cursor: pointer; }
.user-entry{ display: inline-flex; align-items: center; }
.avatar{
  width: 30px; height: 30px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 8px;
}
.nickname{ max-width: 120px; color: #303133; }
.caret{ margin-left: 6px; color: #909399; }
.user-menu{ text-align: center; }

/* 文本省略 */
.ellipsis{ overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* 窄屏优化：收紧左右间距，隐藏过长面包屑 */
@media (max-width: 992px){
  .header-bar{ padding: 0 10px; }
  .nickname{ max-width: 72px; }
  .breadcrumb{ max-width: 45vw; }
}
</style>
