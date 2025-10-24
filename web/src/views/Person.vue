<!--
 * 个人信息修改页面（美化版：A11y + 统一留白/行高 + Skeleton + 提交校验 + 首错聚焦）
 * 修复：按钮与“地址”输入框间距使用 padding，避免 margin 折叠
-->
<template>
  <el-card
    class="profile-card"
    v-loading="isLoading"
    element-loading-text="加载中..."
    shadow="hover"
  >
    <!-- 标题 -->
    <header class="header" aria-label="修改个人信息">
      <h2 class="title">修改个人信息</h2>
      <p class="sub">完善资料，享受更好的服务</p>
      <div class="divider" aria-hidden="true"></div>
    </header>

    <!-- 头像 -->
    <div class="avatar-block">
      <el-upload
        class="avatar-uploader"
        :action="baseApi + '/avatar'"
        :headers="token"
        :show-file-list="false"
        :on-success="handleAvatarSuccess"
        :before-upload="beforeAvatarUpload"
        accept="image/*"
        :aria-label="'上传或更换头像'"
      >
        <div class="avatar-wrapper">
          <img
            v-if="form.avatarUrl"
            :src="baseApi + form.avatarUrl"
            class="avatar"
            alt="用户头像"
          />
          <div v-else class="avatar placeholder" aria-hidden="true">
            <i class="el-icon-user"></i>
          </div>
          <div class="avatar-mask">
            <i class="el-icon-camera"></i>
            <span>更换头像</span>
          </div>
        </div>
      </el-upload>
    </div>

    <!-- 表单 -->
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      :show-message="true"
      label-width="72px"
      class="form"
      :status-icon="true"
      :aria-label="'个人信息表单'"
    >
      <el-form-item label="昵称" prop="nickname">
        <el-input
          v-model="form.nickname"
          autocomplete="off"
          placeholder="请输入昵称"
          prefix-icon="el-icon-user"
        />
      </el-form-item>

      <el-form-item label="电话" prop="phone">
        <el-input
          v-model="form.phone"
          autocomplete="off"
          placeholder="请输入手机号"
          prefix-icon="el-icon-phone-outline"
        />
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input
          v-model="form.email"
          autocomplete="off"
          placeholder="name@example.com"
          prefix-icon="el-icon-message"
        />
      </el-form-item>

      <el-form-item label="地址" prop="address">
        <el-input
          v-model="form.address"
          autocomplete="off"
          placeholder="请输入收货地址"
          prefix-icon="el-icon-location"
        />
      </el-form-item>

      <div class="actions">
        <el-button
          type="primary"
          class="submit-btn"
          :loading="submitting"
          :disabled="submitting"
          @click="onSubmit"
          :aria-label="'保存个人信息'"
        >
          保 存
        </el-button>
      </div>
    </el-form>
  </el-card>
</template>

<script>
export default {
  name: "Person",
  data() {
    return {
      form: {},
      baseApi: this.$store.state.baseApi,
      user: localStorage.getItem("user")
        ? JSON.parse(localStorage.getItem("user"))
        : {},
      imageUrl: "",
      isLoading: true,
      submitting: false,
      rules: {
        nickname: [
          { required: true, message: "请输入昵称", trigger: "blur" },
          { min: 2, max: 20, message: "长度 2-20 个字符", trigger: "blur" },
        ],
        phone: [
          { required: true, message: "请输入手机号", trigger: "blur" },
          // 仅移动手机号：以 1 开头 11 位。如需兼容座机：/^(1\d{10}|(\d{3,4}-)?\d{7,8})$/
          { pattern: /^1\d{10}$/, message: "手机号格式不正确", trigger: ["blur","change"] },
        ],
        email: [
          { required: true, message: "请输入邮箱", trigger: "blur" },
          { type: "email", message: "邮箱格式不正确", trigger: ["blur","change"] },
        ],
        address: [{ required: true, message: "请输入地址", trigger: "blur" }],
      },
    };
  },
  created() {
    this.request
      .get("/userinfo/" + (this.user.username || ""))
      .then((res) => {
        if (res.code === "200") {
          this.form = res.data || {};
        } else {
          this.$message.error(res.msg || "获取用户信息失败");
        }
      })
      .finally(() => {
        this.isLoading = false;
      });
  },
  computed: {
    token() {
      return { token: this.user.token };
    },
  },
  methods: {
    beforeAvatarUpload(file) {
      const isImg =
        file.type === "image/jpeg" ||
        file.type === "image/png" ||
        file.type === "image/webp" ||
        file.type === "image/gif";
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isImg) this.$message.error("仅支持 JPG/PNG/WEBP/GIF 图片");
      if (!isLt2M) this.$message.error("图片大小不能超过 2MB");
      return isImg && isLt2M;
    },
    handleAvatarSuccess(res) {
      this.imageUrl = res.data;
      this.form.avatarUrl = this.imageUrl;
      this.$message.success("头像已更新");
    },
    onSubmit() {
      this.$refs.formRef.validate((valid, fields) => {
        if (valid) {
          this.submitting = true;
          this.request
            .post("/user", this.form)
            .then((res) => {
              if (res.code === "200") {
                this.$message.success("保存成功");
                // 同步本地 user
                for (var key in this.form) {
                  this.user[key] = this.form[key];
                }
                localStorage.setItem("user", JSON.stringify(this.user));
                this.$emit("refresh");
                // 如需强制刷新页面：
                // this.$router.go(0);
              } else {
                this.$message.error(res.msg || "保存失败");
              }
            })
            .catch(() => this.$message.error("网络异常，请稍后再试"))
            .finally(() => (this.submitting = false));
        } else {
          // 取第一个错误并提示 + 聚焦
          var firstKey = Object.keys(fields)[0];
          var firstMsg =
            fields[firstKey] && fields[firstKey][0] && fields[firstKey][0].message;
          this.$message.warning(firstMsg || "请先修正表单中的错误");
          this.$nextTick(() => {
            var el = this.$el.querySelector(".is-error input, .is-error textarea");
            if (el) el.focus();
          });
        }
      });
    },
  },
};
</script>

<style scoped>
/* 主题变量：间距/圆角/阴影/颜色 */
:root {
  --space-xxl: 32px;
  --space-xl: 24px;
  --space-lg: 20px;
  --space-md: 16px;
  --space-sm: 12px;

  --radius-lg: 16px;
  --radius-md: 12px;
  --radius-sm: 8px;

  --shadow-1: 0 14px 36px rgba(31,45,61,.10);
  --brand: #5b6ee1;
  --text-strong: #2c3e50;
  --text-subtle: #606266;
}

.profile-card {
  max-width: 720px;
  margin: 80px auto;
  padding: var(--space-xl) var(--space-xl) var(--space-lg);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-1);
  background: #fff;
}

/* 标题区 */
.header { text-align: center; margin-bottom: var(--space-xl); }
.title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-strong);
  line-height: 1.35;
}
.sub {
  margin: 6px 0 0;
  color: #8b95a1;
  font-size: 13px;
  line-height: 1.6;
}
.divider {
  width: 72px; height: 3px;
  margin: var(--space-sm) auto 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
}

/* 头像区 */
.avatar-block { display: flex; justify-content: center; margin-bottom: var(--space-lg); }
.avatar-uploader { position: relative; }
.avatar-wrapper {
  position: relative;
  width: 138px; height: 138px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 10px 26px rgba(0,0,0,.08);
}
.avatar {
  width: 100%; height: 100%; object-fit: cover; display: block;
}
.avatar.placeholder {
  display: grid; place-items: center; color: #c0c4cc; background: #f3f5f9;
  font-size: 40px;
}
.avatar-mask {
  position: absolute; inset: 0;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  color: #fff; gap: 6px;
  background: rgba(0,0,0,.35);
  opacity: 0; transition: opacity .25s ease;
  font-size: 12px;
}
.avatar-wrapper:hover .avatar-mask { opacity: 1; }

/* 上传器外框 */
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  overflow: hidden;
}
.avatar-uploader .el-upload:hover { border-color: var(--brand); }

/* 表单 */
.form { padding: 0 var(--space-sm); }
.el-form-item { margin-bottom: var(--space-md); }
.el-form-item__label { color: var(--text-subtle); }

/* —— 间距修复：用 padding-top，避免与上方 margin 折叠 —— */
.form .el-form-item:last-of-type {
  margin-bottom: 0;             /* 由下方 actions 的 padding 来撑开间距 */
}
.actions {
  margin-top: 0;                /* 避免与上方 margin 合并 */
  padding-top: 32px;            /* 你可调为 36/40 或变量：var(--space-xxl) */
}

/* 输入框 */
.el-input >>> .el-input__inner {
  height: 40px;
  border-radius: 10px;
}

/* 错误提示与焦点可见性 */
.profile-card :focus-visible {
  outline: 2px solid #7c90ff;
  outline-offset: 2px;
  border-radius: 6px;
}
.el-form-item__error { margin-top: 4px; }

/* 按钮 */
.actions { text-align: center; }
.submit-btn {
  min-width: 200px;
  border-radius: 10px;
  box-shadow: 0 8px 20px rgba(91,110,225,.25);
}

/* 小屏优化 */
@media (max-width: 768px) {
  .profile-card { margin: 40px auto; padding: var(--space-lg); }
  .el-form-item { margin-bottom: var(--space-sm); }
  .actions { padding-top: 18px; } /* 移动端稍微紧凑些 */
}
</style>
