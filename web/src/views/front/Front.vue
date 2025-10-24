<!--
 * 前台首页
 *
 * @Author: ShanZhu
 * @Date: 2023-11-11
-->
<template>
  <div class="app-container">
    <el-container class="main-layout">

      <el-header class="app-header">
        <Navagation
          :user="user"
          :role="role"
          :login-status="loginStatus"
        />
      </el-header>

      <el-main class="app-main">
        <div class="content-wrapper">
          <router-view />
        </div>
      </el-main>

    </el-container>
  </div>
</template>

<script>
import Navagation from "@/components/Navagation";
import request from "@/utils/request";

export default {
  name: "Front",
  components: { Navagation },
  data() {
    return {
      user: {},
      role: "user",
      loginStatus: false,
    };
  },
  methods: {
    getUser() {
      const username = localStorage.getItem("user")
        ? JSON.parse(localStorage.getItem("user")).username
        : "";
      if (username) {
        this.request.get("/userinfo/" + username).then((res) => {
          this.user = res.data;
        });
      }
    },
  },
  created() {
    if (localStorage.getItem("user")) {
      request.post("/role").then((res) => {
        if (res.code === "200") {
          this.role = res.data;
          if (localStorage.getItem("user")) {
            this.user = JSON.parse(localStorage.getItem("user"));
            this.loginStatus = true;
          }
        } else {
          this.user = { nickname: "您未登录", avatarUrl: null };
          localStorage.removeItem("user");
          this.loginStatus = false;
        }
      });
    } else {
      this.user = { nickname: "您未登录", avatarUrl: null };
      this.loginStatus = false;
    }
  },
};
</script>

<style scoped>
@import "../../resource/css/search.css";

.app-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.main-layout {
  min-height: 100vh;
  width: 100%;
}

.app-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 0;
  height: auto !important;
  position: sticky;
  top: 0;
  z-index: 1000;
  transition: all 0.3s ease;
}

.app-header:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.app-main {
  padding: 0;
  background: transparent;
  width: 100%;
  overflow-x: hidden;
}

.content-wrapper {
  min-height: calc(100vh - 80px);
  width: 100%;
  animation: fadeIn 0.3s ease-in-out;
}

/* Global styling improvements */
.image {
  width: 100%;
  display: block;
}

/* Enhanced scrollbar for webkit browsers */
::-webkit-scrollbar {
  width: 8px;
}
::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}
::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #5a67d8 0%, #6b46c1 100%);
}

/* Smooth transitions for route changes */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .app-header { position: relative; }
  .content-wrapper { min-height: calc(100vh - 60px); }
}

/* Loading state improvements */
.main-layout.loading { pointer-events: none; }
.main-layout.loading::before {
  content: '';
  position: fixed;
  inset: 0;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(4px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
