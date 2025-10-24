<template>
  <div class="manage-root">
    <el-container class="frame">
      <!-- 侧边栏 -->
      <el-aside :width="sideWidth + 'px'" class="sidebar">
        <Aside :is-collapse="isCollapse" />
      </el-aside>

      <el-container>
        <el-header class="app-header">
          <Header
            :collapse-icon="collapseIcon"
            :collapse-title="collapseTitle"
            @collapse="handleCollapse"
            :user="user"
          />
        </el-header>

        <el-main class="content" :class="{ bk: $route.path === '/manage/home' }">
          <router-view @refresh="getUser" />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import Aside from "@/components/Aside";
import Header from "@/components/Header";

export default {
  name: "ManageLayout",
  components: { Aside, Header },
  data() {
    return {
      user: {},
      isCollapse: false,
      sideWidth: 250,
      collapseIcon: "el-icon-s-fold",
      collapseTitle: "收缩",
    };
  },
  created() { this.getUser(); },
  methods: {
    handleCollapse() {
      this.isCollapse = !this.isCollapse;
      if (this.isCollapse) {
        this.sideWidth = 64; this.collapseIcon = "el-icon-s-unfold"; this.collapseTitle = "展开";
      } else {
        this.sideWidth = 250; this.collapseIcon = "el-icon-s-fold"; this.collapseTitle = "收缩";
      }
    },
    getUser() {
      const username = localStorage.getItem("user")
        ? JSON.parse(localStorage.getItem("user")).username : "";
      if (!username) return;
      this.request.get("/userinfo/" + username).then(res => { this.user = res.data || {}; });
    },
  },
};
</script>

<style scoped>
.manage-root { height: 100vh; background: linear-gradient(180deg,#f5f7fb 0%,#eef2f7 100%); overflow: hidden; }
.frame { height: 100%; }

/* 侧栏：独立滚动（修复“划不下去”） */
.sidebar { height: 100vh; overflow-y: auto; overflow-x: hidden; background: #0f1115; }

.app-header { height: 60px; line-height: 60px; background: rgba(255,255,255,.9); border-bottom: 1px solid #eef0f3; backdrop-filter: blur(6px); }
.content { padding: 16px; background: transparent; }

.bk { width: 100%; background: url("@/resource/img/login_back.png") center/cover no-repeat; border-radius: 12px; min-height: calc(100vh - 60px - 32px); }

/* 可选滚动条美化 */
.sidebar::-webkit-scrollbar, .content::-webkit-scrollbar { width: 8px; }
.sidebar::-webkit-scrollbar-thumb, .content::-webkit-scrollbar-thumb { background: #2c2f36; border-radius: 6px; }
.sidebar::-webkit-scrollbar-thumb:hover, .content::-webkit-scrollbar-thumb:hover { background: #3a3f47; }
</style>
