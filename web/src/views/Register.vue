
<!--
 * 用户注册页面
 *
 * @Author: ShanZhu
 * @Date: 2023-11-11
-->
<template>
<div class="wrapper">
  <div class="register-container">
    <div class="register-card">
      <div class="title">
        <h2 class="title-text">创建账户</h2>
        <p class="subtitle">加入我们的购物商城</p>
      </div>

      <el-form class="register-form" label-width="0">
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

        <el-form-item class="form-item">
          <el-input
            v-model.trim="user.confirmPassword"
            placeholder="请确认密码"
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
            class="register-btn"
            @click="onSubmit">
            注册
          </el-button>
          <el-button
            size="large"
            class="back-btn"
            @click="$router.push('/login')">
            返回登录
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
  name: "Register",
  data(){
    return {
      user: {},
    }
  },
  methods: {
    onSubmit() {
      if(this.user.username==='' || this.user.password==='' ||this.user.confirmPassword===''){
        this.$message.error("账号或密码不能为空")
        return false;
      }
      if(this.user.password!==this.user.confirmPassword){
        this.$message.error("两次密码不一致")
        return false;
      }
      this.user.password = md5(this.user.password);
      this.request.post("/register",this.user).then(res=>{
        if(res.code==='200'){
          this.$message.success("注册成功")
          this.$router.push("/login")
        }else{
          this.$message.error(res.msg)
        }
      })
    }
  }
}
</script>

<style scoped>
.wrapper {
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: auto;
}

.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
}

.register-card {
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

.title-text {
  color: #2c3e50;
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
  letter-spacing: 0.5px;
}

.subtitle {
  color: #64748b;
  font-size: 14px;
  margin: 0;
  font-weight: 400;
}

.register-form {
  margin-top: 0;
}

.form-item {
  margin-bottom: 24px;
}

.form-item:last-child {
  margin-bottom: 0;
}

/* Custom Element UI styling */
.register-form >>> .el-input__inner {
  border-radius: 12px;
  border: 1px solid #e0e6ed;
  height: 48px;
  font-size: 16px;
  padding-left: 45px;
  transition: all 0.3s ease;
}

.register-form >>> .el-input__inner:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.register-form >>> .el-input__prefix {
  left: 15px;
}

.register-form >>> .el-input__prefix .el-icon-user,
.register-form >>> .el-input__prefix .el-icon-lock {
  color: #8a9eb2;
  font-size: 18px;
}

.button-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 32px;
}

.register-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  width: 100%;
  transition: all 0.3s ease;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.back-btn {
  border: 1px solid #e0e6ed;
  background: #ffffff;
  color: #64748b;
  border-radius: 12px;
  height: 48px;
  font-size: 16px;
  width: 100%;
  transition: all 0.3s ease;
}

.back-btn:hover {
  border-color: #667eea;
  color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

/* Mobile responsiveness */
@media (max-width: 480px) {
  .register-card {
    width: 90%;
    padding: 24px;
    margin: 20px;
  }

  .title-text {
    font-size: 20px;
  }
}</style>
