<!-- src/views/Login.vue -->
<template>
  <div class="login-index" :style="backgroundDiv">
    <div class="login-container">
      <div class="login-card">
        <div class="title">
          <img src="../resource/logo.png" class="logo" alt="Logo">
          <h2 class="title-text">好物集商城</h2>
        </div>

        <el-form class="login-form" label-width="0">
          <el-form-item class="form-item">
            <el-input
              v-model.trim="user.username"
              placeholder="请输入用户名"
              prefix-icon="el-icon-user"
              size="large"
              aria-required="true">
            </el-input>
          </el-form-item>

          <el-form-item class="form-item">
            <el-input
              v-model.trim="user.password"
              placeholder="请输入密码"
              prefix-icon="el-icon-lock"
              show-password
              size="large"
              aria-required="true">
            </el-input>
          </el-form-item>

          <el-form-item class="form-item button-group">
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              @click="onSubmit">
              登录
            </el-button>
            <el-button
              size="large"
              class="register-btn"
              @click="$router.push('/register')">
              注册账户
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import md5 from "js-md5";

export default {
  name: "Login",
  data() {
    return {
      to: '/',
      user: {},
      backgroundDiv: {
        backgroundImage: "url(" + require("@/resource/img/login_back.png") + ")",
        backgroundRepeat: "no-repeat",
        backgroundSize: "100% 100%",
      },
    }
  },
  created() {
    this.to = this.$route.query.to ? this.$route.query.to : "/"
  },
  methods: {
    onSubmit() {
      if (!this.user.username || !this.user.password) {
        this.$message.error("账号或密码不能为空")
        return
      }

      // ★ 登录时也做 md5，和注册保持一致
      this.user.password = md5(this.user.password)

      this.request.post("/login", this.user).then(res => {
        if (res.code === '200') {
          this.$message.success({ message: "登陆成功", showClose: true })
          localStorage.setItem("user", JSON.stringify(res.data))
          this.$router.push(this.to)
        } else {
          this.$message.error(res.msg)
        }
      })
    }
  }
}
</script>

<style scoped>
.login-index {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 40px;
  width: 420px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.title {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  width: 48px;
  height: 48px;
  margin-bottom: 16px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(231, 92, 9, 0.2);
}

.title-text {
  color: #2c3e50;
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  letter-spacing: 0.5px;
}

.login-form {
  margin-top: 0;
}

.form-item {
  margin-bottom: 24px;
}

.form-item:last-child {
  margin-bottom: 0;
}

/* Custom Element UI styling */
.login-form >>> .el-input__inner {
  border-radius: 12px;
  border: 1px solid #e0e6ed;
  height: 48px;
  font-size: 16px;
  padding-left: 45px;
  transition: all 0.3s ease;
}

.login-form >>> .el-input__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.login-form >>> .el-input__prefix {
  left: 15px;
}

.login-form >>> .el-input__prefix .el-icon-user,
.login-form >>> .el-input__prefix .el-icon-lock {
  color: #8a9eb2;
  font-size: 18px;
}

.button-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 32px;
}

.login-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  width: 100%;
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.register-btn {
  border: 1px solid #e0e6ed;
  background: #ffffff;
  color: #64748b;
  border-radius: 12px;
  height: 48px;
  font-size: 16px;
  width: 100%;
  transition: all 0.3s ease;
}

.register-btn:hover {
  border-color: #667eea;
  color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

/* Mobile responsiveness */
@media (max-width: 480px) {
  .login-card {
    width: 90%;
    padding: 24px;
    margin: 20px;
  }

  .title-text {
    font-size: 20px;
  }
}
</style>
