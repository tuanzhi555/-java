<!--
 * 后台商品分类管理（UI 美化版｜仅样式与布局）
 * - 不改任何接口/字段/路由/请求逻辑
 * - 卡片化外观、展开子表更紧凑、操作区按钮分组
 * - 仅前端分页（切片展示，不影响后端数据）
 * @Author: ShanZhu
 * @Date: 2023-11-11
-->
<template>
  <div class="category-page">

    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <el-button @click="addDialogFormVisible = true" round type="success" class="btn-add">
        <i class="el-icon-circle-plus" style="padding-right:6px"></i>新增
      </el-button>
      <div class="toolbar-right">
        <span class="muted">共 {{ icons.length }} 个上级分类</span>
      </div>
    </div>

    <!-- 表格卡片 -->
    <el-card class="table-card" shadow="never" v-loading="loading">
      <el-table
        :data="displayIcons"
        stripe
        border
        highlight-current-row
        style="width: 100%"
        :header-cell-class-name="()=>'table-header'"
      >
        <!-- 下级分类展开 -->
        <el-table-column type="expand" label="下级分类" width="120">
          <template slot-scope="scope">
            <el-table
              :data="(scope.row.categories || [])"
              size="mini"
              :header-cell-style="{ background: '#f6f7fb', color: '#606266', fontWeight: 600 }"
              style="width: 100%"
            >
              <el-table-column label="分类ID" prop="id" width="120">
                <template slot-scope="child">
                  <el-tag size="small" effect="plain">{{ child.row.id }}</el-tag>
                </template>
              </el-table-column>

              <el-table-column label="分类名称" prop="name">
                <template slot-scope="child">
                  <el-tooltip effect="dark" :content="child.row.name" placement="top">
                    <span class="one-line">{{ child.row.name }}</span>
                  </el-tooltip>
                </template>
              </el-table-column>

              <el-table-column label="操作" width="200">
                <template slot-scope="child">
                  <el-button
                    type="primary"
                    size="mini"
                    @click="handleEditCategory(child.row)"
                  >修改</el-button>

                  <el-popconfirm
                    @confirm="deleteCategory(child.row)"
                    title="确定删除该下级分类？"
                    confirm-button-text="删除"
                    cancel-button-text="取消"
                  >
                    <el-button
                      type="danger"
                      size="mini"
                      slot="reference"
                    >删除</el-button>
                  </el-popconfirm>
                </template>
              </el-table-column>
            </el-table>

            <el-empty
              v-if="!scope.row.categories || scope.row.categories.length === 0"
              description="暂无下级分类"
              :image-size="80"
              class="mt-12"
            />
          </template>
        </el-table-column>

        <!-- 上级分类ID -->
        <el-table-column label="ID" prop="id" width="90">
          <template slot-scope="scope">
            <el-tag effect="plain">{{ scope.row.id }}</el-tag>
          </template>
        </el-table-column>

        <!-- 图标展示 -->
        <el-table-column label="图标" width="160">
          <template slot-scope="scope">
            <div class="icon-wrap">
              <i class="iconfont" v-html="scope.row.value"></i>
            </div>
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column fixed="right" label="操作" width="260">
          <template slot-scope="scope">
            <el-button-group>
              <el-button
                type="primary"
                icon="el-icon-edit"
                @click="handleEditIcon(scope.row)"
              >编辑图标</el-button>

              <el-button
                type="success"
                icon="el-icon-plus"
                @click="handleAddCategory(scope.row)"
              >新增下级</el-button>

              <el-popconfirm
                @confirm="deleteIcon(scope.row.id)"
                title="确定删除该上级分类？"
                confirm-button-text="删除"
                cancel-button-text="取消"
              >
                <el-button
                  type="danger"
                  icon="el-icon-delete"
                  slot="reference"
                >删除</el-button>
              </el-popconfirm>
            </el-button-group>
          </template>
        </el-table-column>

      </el-table>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && icons.length === 0"
        description="暂无上级分类"
        class="mt-16"
      />

      <!-- 分页（前端切片，不影响后端） -->
      <div class="pager" v-if="icons.length > 0">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="icons.length"
          :page-sizes="[5, 10, 20]"
          :page-size="pageSize"
          :current-page="pageNum"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- icon 修改弹窗 -->
    <el-dialog title="修改上级分类" :visible.sync="dialogFormVisible" width="520px" :close-on-click-modal="false">
      <el-form :model="icon" label-width="100px">
        <el-form-item label="当前图标">
          <div class="icon-wrap large">
            <i class="iconfont" v-html="icon.value"></i>
          </div>
        </el-form-item>

        <el-form-item label="更改图标">
          <el-select placeholder="请选择图标" v-model="icon.value" style="width: 80%">
            <el-option v-for="item in iconStore" :value="item" :key="item">
              <i class="iconfont" v-html="item"></i>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="editIcon">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 新增上级分类 -->
    <el-dialog title="新增上级分类" :visible.sync="addDialogFormVisible" width="520px" :close-on-click-modal="false">
      <el-form :model="addIcon" label-width="100px">
        <el-form-item label="当前图标">
          <div class="icon-wrap large">
            <i class="iconfont" v-html="addIcon.value"></i>
          </div>
        </el-form-item>

        <el-form-item label="更改图标">
          <el-select placeholder="请选择图标" v-model="addIcon.value" style="width: 80%">
            <el-option v-for="item in iconStore" :value="item" :key="item">
              <i class="iconfont" v-html="item"></i>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="addDialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="saveIcon">确 定</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import icons from "@/utils/icons";
export default {
  name: "Category",
  data() {
    return {
      options: [],
      searchText: "",
      user: {},
      // 从 icons.js 中引入常量 iconStore
      iconStore: icons.iconStore,
      icons: [],
      icon: {},
      addIcon: {},
      pageNum: 1,
      pageSize: 5,
      entity: {},
      total: 0,
      dialogFormVisible: false,
      addDialogFormVisible: false,
      loading: false
    };
  },
  created() {
    this.user = localStorage.getItem("user")
      ? JSON.parse(sessionStorage.getItem("user"))
      : {};
    this.load();
  },
  computed: {
    // 纯前端分页：仅切片展示，不影响后端数据
    displayIcons() {
      const list = this.icons || [];
      const start = (this.pageNum - 1) * this.pageSize;
      return list.slice(start, start + this.pageSize);
    }
  },
  methods: {
    // 加载页面
    load() {
      this.loading = true;
      this.request.get("/api/icon").then((res) => {
        this.icons = res.data || [];
      }).finally(() => {
        this.loading = false;
      });
    },
    // 分页交互（不触发后端）
    handleSizeChange(size) {
      this.pageSize = size;
      this.pageNum = 1;
    },
    handleCurrentChange(page) {
      this.pageNum = page;
    },

    // 编辑分类
    handleEditCategory(category) {
      this.$prompt("请输入修改后的名称", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
      }).then(({ value }) => {
        category.name = value;
        this.request.post("/api/category", category).then((res) => {
          if (res.code === "200") {
            this.$message.success("修改成功");
          } else {
            this.$message.error("修改失败");
          }
        });
      });
    },
    // 添加分类
    handleAddCategory(icon) {
      this.$prompt("请输入新增的下级分类名称", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
      }).then(({ value }) => {
        this.request
          .post("/api/category/add", { name: value, iconId: icon.id })
          .then((res) => {
            if (res.code === "200") {
              this.$message.success("新增成功");
              this.load();
            } else {
              this.$message.error("新增失败");
            }
          });
      });
    },
    // 编辑图标弹窗
    handleEditIcon(icon) {
      this.icon = JSON.parse(JSON.stringify(icon));
      this.dialogFormVisible = true;
    },
    // 编辑图标
    editIcon() {
      // 删除无用的属性（保持你现有逻辑）
      delete this.icon.categories;
      this.request.post("/api/icon", this.icon).then((res) => {
        if (res.code === "200") {
          this.$message.success("修改成功");
          this.dialogFormVisible = false;
        } else {
          this.$message.error("修改失败");
        }
      });
    },
    // 保存图标（新增上级分类）
    saveIcon() {
      if (this.addIcon.value == undefined) {
        this.$message.error("请选择上级分类图标");
        return;
      }
      this.request.post("/api/icon", this.addIcon).then((res) => {
        if (res.code === "200") {
          this.$message.success("新增成功");
          this.addDialogFormVisible = false;
          this.load();
        } else {
          this.$message.error("新增失败");
        }
      });
    },
    // 删除图标（上级分类）
    deleteIcon(iconId) {
      this.request.get("/api/icon/delete?id=" + iconId).then((res) => {
        if (res.code == "200") {
          this.$message.success("删除成功");
          this.load();
        } else {
          this.$message.error(res.msg);
        }
      });
    },
    // 删除分类（下级分类）
    deleteCategory(category) {
      this.request.get("/api/category/delete?id=" + category.id).then((res) => {
        if (res.code == "200") {
          this.$message.success("删除成功");
          this.load();
        } else {
          this.$message.error(res.msg);
        }
      });
    },
  },
};
</script>

<style scoped>
@import "../../../resource/css/icon.css";

.category-page {
  padding: 12px;
}

/* 顶部工具栏 */
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.btn-add {
  min-width: 120px;
}
.toolbar-right .muted {
  color: #909399;
  font-size: 13px;
}

/* 卡片与表格 */
.table-card {
  border: 1px solid #ebeef5;
  border-radius: 12px;
  overflow: hidden;
}
.table-header {
  background: #fafafa;
  color: #606266;
  font-weight: 600;
}

/* 图标外观 */
.icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  border: 1px solid #ebeef5;
  background: #f8f9fb;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.icon-wrap .iconfont {
  font-size: 22px;
  line-height: 1;
}
.icon-wrap.large {
  width: 64px;
  height: 64px;
}
.icon-wrap.large .iconfont {
  font-size: 28px;
}

/* 文本省略 */
.one-line {
  display: inline-block;
  max-width: 320px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 空状态与分页 */
.mt-12 { margin-top: 12px; }
.mt-16 { margin-top: 16px; }
.pager {
  display: flex;
  justify-content: flex-end;
  padding: 14px 6px;
}
</style>
