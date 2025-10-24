<!--
 * 后台轮播图管理（UI 美化版｜仅样式与布局）
 * - 不改任何接口/字段/路由/请求逻辑
 * - 增加更友好的表格样式与图片预览
 * - 增加本地分页（前端切片，不影响后端）
 * @Author: ShanZhu
 * @Date: 2023-11-11
-->
<template>
  <div class="carousel-page">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <el-button class="btn-add" @click="add" round type="success">
        <i class="el-icon-circle-plus" style="padding-right:6px"></i>新增
      </el-button>

      <div class="toolbar-right">
        <span class="muted">共 {{ tableData.length }} 条</span>
      </div>
    </div>

    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="displayData"
        stripe
        border
        highlight-current-row
        v-loading="loading"
        style="width: 100%"
        :header-cell-class-name="()=>'table-header'"
      >
        <!-- 商品 -->
        <el-table-column width="420" label="商品">
          <template slot-scope="scope">
            <el-tooltip effect="dark" :content="scope.row.goodName" placement="top">
              <a
                class="good-link one-line-ellipsis"
                :href="'/goodView/'+scope.row.goodId"
                :title="scope.row.goodName"
              >
                <i class="el-icon-goods icon-left"></i>{{ scope.row.goodName }}
              </a>
            </el-tooltip>
          </template>
        </el-table-column>

        <!-- 图片（可预览） -->
        <el-table-column width="380" label="图片">
          <template slot-scope="scope">
            <el-image
              class="thumb"
              :src="baseApi + scope.row.img"
              :preview-src-list="[baseApi + scope.row.img]"
              fit="cover"
              :z-index="2000"
            >
              <div slot="placeholder" class="img-placeholder">加载中...</div>
              <div slot="error" class="img-error">
                <i class="el-icon-picture-outline"></i>
                <span>无法加载</span>
              </div>
            </el-image>
          </template>
        </el-table-column>

        <!-- 轮播顺序 -->
        <el-table-column prop="showOrder" width="160" label="轮播顺序">
          <template slot-scope="scope">
            <el-tag effect="plain" type="info">{{ scope.row.showOrder }}</el-tag>
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column fixed="right" label="操作" width="280">
          <template slot-scope="scope">
            <el-button-group>
              <el-button type="primary" icon="el-icon-edit" @click="edit(scope.row)">编辑</el-button>
              <el-popconfirm
                @confirm="del(scope.row.id)"
                title="确定删除？"
                confirm-button-text="删除"
                cancel-button-text="取消"
              >
                <el-button type="danger" icon="el-icon-delete" slot="reference">删除</el-button>
              </el-popconfirm>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && tableData.length === 0"
        description="暂无轮播图"
        class="mt-16"
      ></el-empty>

      <!-- 分页（前端切片，不影响后端接口） -->
      <div class="pager" v-if="tableData.length > 0">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="tableData.length"
          :page-sizes="[5, 10, 20]"
          :page-size="pageSize"
          :current-page="pageNum"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      title="信息"
      :visible.sync="dialogFormVisible"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form :model="entity" label-width="110px">
        <el-form-item label="商品ID">
          <el-input v-model="entity.goodId" autocomplete="off" style="width: 80%"></el-input>
        </el-form-item>

        <el-form-item label="轮播顺序">
          <el-select v-model="entity.showOrder" placeholder="请选择顺序" style="width: 80%">
            <el-option
              v-for="index in tableData.length"
              :key="index"
              :label="index"
              :value="index"
            />
          </el-select>
          <div class="tip">数字越小，展示越靠前</div>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import API from '../../../utils/request'
const url = "/api/carousel/"

export default {
  name: "Carousel",
  data() {
    return {
      baseApi: this.$store.state.baseApi,
      options: [],
      searchText: '',
      user: {},
      tableData: [],
      pageNum: 1,
      pageSize: 5,
      entity: {},
      total: 0,
      dialogFormVisible: false,
      loading: false
    };
  },
  created() {
    this.load()
  },
  computed: {
    // 纯前端分页：不改后端接口
    displayData() {
      const start = (this.pageNum - 1) * this.pageSize
      return this.tableData.slice(start, start + this.pageSize)
    }
  },
  methods: {
    // 分页大小变化
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.pageNum = 1
    },
    // 翻页
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
    },
    // 加载列表
    load() {
      this.loading = true
      API.get(url).then(res => {
        this.tableData = res.data || []
      }).finally(() => {
        this.loading = false
      })
    },
    // 新增轮播图弹出
    add() {
      this.entity = {}
      // 维持你原有“length++”的逻辑以便选择顺序（不改后端）
      this.tableData.length++;
      this.dialogFormVisible = true
    },
    // 编辑
    edit(row) {
      this.entity = JSON.parse(JSON.stringify(row))
      this.dialogFormVisible = true
    },
    // 保存轮播图（不改接口）
    save() {
      if(this.entity.goodId == undefined || this.entity.goodId === "") {
        this.$message.error("商品id不能为空")
        return;
      }
      if(this.entity.showOrder == undefined) {
        this.$message.error("轮播顺序不能为空")
        return;
      }
      API.post(url, this.entity).then(res => {
        if (res.code === '200') {
          this.$message.success("保存成功")
          this.load()
          this.dialogFormVisible = false
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    // 删除轮播图（不改接口）
    del(id) {
      API.delete(url + id).then(res => {
        if(res.code==='200'){
          this.$message({ type: "success", message: "删除成功" });
          this.load();
        } else {
          this.$message.error(res.msg);
        }
      })
    }
  },
};
</script>

<style scoped>
.carousel-page {
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

/* 商品名称样式 */
.good-link {
  display: inline-flex;
  align-items: center;
  max-width: 360px;
  color: #409EFF;
  text-decoration: none;
}
.good-link:hover {
  text-decoration: underline;
}
.icon-left {
  margin-right: 6px;
}
.one-line-ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 图片缩略图 */
.thumb {
  width: 220px;
  height: 150px;
  border-radius: 10px;
  border: 1px solid #ebeef5;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  overflow: hidden;
}
.img-placeholder,
.img-error {
  width: 100%;
  height: 100%;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fb;
  gap: 6px;
}

/* 分页 */
.pager {
  display: flex;
  justify-content: flex-end;
  padding: 14px 6px;
}
.mt-16 {
  margin-top: 16px;
}

/* 弹窗小提示 */
.tip {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}
</style>
