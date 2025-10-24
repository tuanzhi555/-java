<!--
 * 商品信息（无预览版｜仅样式与布局）
 * - 不改任何接口/字段/路由/请求逻辑
 * - 表单卡片化、区块分组、规格标签更易用
 * - 去掉图片预览，仅保留上传控件
 * @Author: ShanZhu
 * @Date: 2023-11-11
-->
<template>
  <div class="goodinfo-page">
    <el-card class="form-card" shadow="never">
      <div slot="header" class="card-header">
        <span>商品信息</span>
      </div>

      <el-form :model="good" label-width="150px" class="good-form">
        <!-- 基本信息 -->
        <el-divider content-position="left">基本信息</el-divider>

        <el-form-item label="商品名称">
          <el-input
            v-model="good.name"
            autocomplete="off"
            placeholder="请输入商品名称"
            style="width: 80%"
          />
        </el-form-item>

        <el-form-item label="商品描述">
          <el-input
            v-model="good.description"
            autocomplete="off"
            placeholder="简要描述商品特性"
            style="width: 80%"
          />
        </el-form-item>

        <el-form-item label="折扣">
          <el-input
            v-model="good.discount"
            autocomplete="off"
            placeholder="例如 1 或 0.9"
            style="width: 80%"
          />
        </el-form-item>

        <el-form-item label="分类">
          <el-select
            v-model="good.categoryId"
            placeholder="请选择"
            filterable
            style="width: 80%"
          >
            <el-option
              v-for="item in categoryItems"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <!-- 规格 -->
        <el-divider content-position="left">规格</el-divider>

        <el-form-item label="规格">
          <div class="standards">
            <el-tag
              v-for="(standard, index) in standards"
              :key="index"
              closable
              @close="handleClose(standard)"
              :disable-transitions="true"
              class="std-tag"
              @click="editStandard(index)"
            >{{ standard.value }}</el-tag>
            <el-button
              class="std-add"
              icon="el-icon-plus"
              type="primary"
              plain
              @click="addStandard"
            >添加规格</el-button>
          </div>
          <div class="hint">点击标签可编辑，关闭按钮可删除</div>
        </el-form-item>

        <!-- 图片（无预览） -->
        <el-divider content-position="left">商品图片</el-divider>

        <el-form-item label="商品图片">
          <el-upload
            class="upload-box"
            ref="upload"
            :action="baseApi + '/file/upload'"
            :file-list="fileList"
            :on-change="handleChange"
            :limit="1"
            :on-remove="handleRemove"
            :on-success="handleImgSuccess"
            :auto-upload="false"
          >
            <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
            <div slot="tip" class="el-upload__tip">只能上传一个 jpg/png 文件，且不超过 500kb</div>
          </el-upload>
        </el-form-item>

        <!-- 提交 -->
        <el-form-item label-width="150px">
          <div class="actions">
            <el-button type="success" @click="submit">提交</el-button>
            <el-button @click="$router.go(-1)">返回</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 规格弹窗 -->
    <el-dialog
      title="规格信息"
      :visible.sync="dialogFormVisible"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form :model="standard" label-width="150px">
        <el-form-item label="规格名称">
          <el-input
            v-model="standard.value"
            autocomplete="off"
            placeholder="如：500g / XL / 红色"
            style="width: 80%"
          />
        </el-form-item>
        <el-form-item label="价格">
          <el-input
            v-model="standard.price"
            autocomplete="off"
            placeholder="请输入数字"
            style="width: 80%"
          />
        </el-form-item>
        <el-form-item label="库存">
          <el-input
            v-model="standard.store"
            autocomplete="off"
            placeholder="请输入数字"
            style="width: 80%"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="saveStandard">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import API from "@/utils/request";
const url = "/api/good";
export default {
  name: "GoodInfo",
  data() {
    return {
      baseApi: this.$store.state.baseApi,
      good: {
        discount: "1",
      },
      standards: [],
      index: 0,
      standard: {},
      fileList: [],
      dialogFormVisible: false,
      categoryItems: [],
      checkedCategory: {},
    };
  },
  created() {
    this.request.get("/api/category").then((res) => {
      if (res.code === "200") {
        this.categoryItems = res.data;
      }
      if (this.$route.query.good) {
        this.good = JSON.parse(this.$route.query.good);
        this.fileList = [{ name: "原始图片", url: this.good.imgs }];
        this.checkedCategory = this.categoryItems[this.good.categoryId];
        //从服务器获取商品规格信息
        this.request.get(url + "/standard/" + this.good.id).then((res) => {
          if (res.code === "200") {
            let standards = JSON.parse(res.data);
            this.standards = standards;
          } else {
            //没有规格
          }
        });
      }
    });
  },
  methods: {
    /* 规格操作（保持原逻辑） */
    editStandard(index) {
      this.standard = this.standards[index];
      this.index = index;
      this.dialogFormVisible = true;
    },
    addStandard() {
      this.standard = {};
      this.index = this.standards.length;
      this.dialogFormVisible = true;
    },
    saveStandard() {
      if (this.standard.value == undefined || this.standard.value == "") {
        this.$message({ type: "error", message: "请输入规格名称", showClose: true });
        return;
      }
      if (this.standard.price == undefined || this.standard.price == "") {
        this.$message({ type: "error", message: "请输入价格", showClose: true });
        return;
      }
      if (this.standard.store == undefined || this.standard.store == "") {
        this.$message({ type: "error", message: "请输入库存", showClose: true });
        return;
      }
      this.standards[this.index] = this.standard;
      this.dialogFormVisible = false;
    },
    handleClose(standard) {
      this.standards.splice(this.standards.indexOf(standard), 1);
    },

    /* 上传相关（保持原逻辑） */
    handleImgSuccess(res) {
      this.good.imgs = res.data;
      this.save();
    },
    handleChange(file, fileList) {
      this.fileList = fileList.slice(-3);
    },
    handleRemove() {
      this.fileList.pop();
    },

    /* 提交校验与保存（保持原接口） */
    submit() {
      if (this.good.name == undefined || this.good.name.trim() == "") {
        this.$message({ type: "error", message: "请输入商品名称", showClose: true });
        return false;
      }
      if (this.good.description == undefined || this.good.description.trim() == "") {
        this.$message({ type: "error", message: "请输入商品描述", showClose: true });
        return false;
      }
      if (this.standards.length === 0) {
        this.$message({ type: "error", message: "请至少添加一个规格", showClose: true });
        return false;
      }
      if (this.good.discount == undefined || this.good.discount === "") {
        this.$message({ type: "error", message: "请输入商品折扣", showClose: true });
        return false;
      }
      if (this.good.categoryId == undefined) {
        this.$message({ type: "error", message: "请选择商品分类", showClose: true });
        return false;
      }
      if (this.fileList.length === 0) {
        this.$message({ type: "error", message: "请上传图片", showClose: true });
        return false;
      }
      // 先上传图片再保存数据
      if (this.fileList[0].status === "ready") {
        this.$refs.upload.submit();
      } else if (this.fileList[0].status === "success") {
        // 未修改图片但需要保存时，沿用你的原逻辑
        this.save();
      }
    },
    save() {
      API.post(url, this.good).then((res2) => {
        if (res2.code === "200") {
          this.good.id = res2.data;
          this.request
            .post(url + "/standard?goodId=" + this.good.id, this.standards)
            .then((res) => {
              if (res.code === "200") {
                this.$message({ type: "success", message: "操作成功", showClose: true });
                this.$router.go(-1);
              } else {
                this.$message({ type: "error", message: "操作失败", showClose: true });
              }
            });
        } else {
          this.$message({ type: "error", message: res2.msg });
        }
      });
    },
  },
};
</script>

<style scoped>
.goodinfo-page {
  max-width: 1000px;
  margin: 24px auto;
  padding: 0 8px;
}
.form-card {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  overflow: hidden;
}
.card-header {
  font-weight: 600;
  color: #303133;
}
.good-form .el-divider__text {
  font-size: 13px;
  color: #909399;
}

/* 规格区域 */
.standards {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.std-tag {
  cursor: pointer;
}
.std-add {
  height: 32px;
  align-self: center;
}
.hint {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}

/* 上传 */
.upload-box {
  min-width: 300px;
}

/* 按钮区 */
.actions {
  display: flex;
  gap: 8px;
}
</style>
