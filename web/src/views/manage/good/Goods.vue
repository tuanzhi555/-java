<!--
 * 商品管理（UI 美化版｜仅样式与布局）
 * - 不改任何接口/字段/路由/请求逻辑
 * - 顶部工具栏、卡片化表格、图片预览、文本省略+悬浮提示
 * @Author: ShanZhu
 * @Date: 2023-11-11
-->
<template>
  <div class="goods-page">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <el-input
        v-model="searchText"
        @keyup.enter.native="load"
        clearable
        placeholder="输入名称后回车搜索"
        class="search-input"
      >
        <i slot="prefix" class="el-input__icon el-icon-search"></i>
      </el-input>

      <div class="toolbar-right">
        <el-button @click="load" round type="primary">搜索</el-button>
        <el-button @click="reset" round type="info">重置</el-button>
        <el-button @click="add" round type="success">新增</el-button>
      </div>
    </div>

    <!-- 表格卡片 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="tableData"
        stripe
        border
        highlight-current-row
        style="width: 100%"
        :header-cell-class-name="()=>'table-header'"
      >
        <el-table-column prop="id" label="商品ID" width="100">
          <template slot-scope="scope">
            <el-tag effect="plain">{{ scope.row.id }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="name" label="商品名称" min-width="180">
          <template slot-scope="scope">
            <el-tooltip effect="dark" :content="scope.row.name" placement="top">
              <span class="ellipsis">{{ scope.row.name }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column label="商品图片" width="140">
          <template slot-scope="scope">
            <el-image
              :src="baseApi + scope.row.imgs"
              :preview-src-list="[baseApi + scope.row.imgs]"
              fit="cover"
              class="thumb"
              :z-index="2000"
            >
              <div slot="error" class="img-error">
                <i class="el-icon-picture-outline"></i>
              </div>
            </el-image>
          </template>
        </el-table-column>

        <el-table-column prop="description" label="商品描述" min-width="220">
          <template slot-scope="scope">
            <el-tooltip effect="dark" :content="scope.row.description" placement="top">
              <span class="ellipsis">{{ scope.row.description }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column prop="discount" label="折扣" width="100">
          <template slot-scope="scope">
            <el-tag size="small" type="info" effect="plain">{{ scope.row.discount }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="sales" label="销量" width="100">
          <template slot-scope="scope">
            <span class="mono">{{ scope.row.sales }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="saleMoney" label="销售额（元）" width="140">
          <template slot-scope="scope">
            <span class="mono">{{ scope.row.saleMoney }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="180">
          <template slot-scope="scope">
            <!-- 保持你的替换逻辑，只是用 v-html 使 &nbsp; 生效 -->
            <span v-html="scope.row.createTime.replace(' ','&nbsp;&nbsp;')"></span>
          </template>
        </el-table-column>

        <el-table-column label="推荐" width="120">
          <template slot-scope="scope">
            <el-switch
              v-model="scope.row.recommend"
              @change="handleRecommend(scope.row)"
              active-color="#13ce66"
              inactive-color="#ff4949"
            />
          </template>
        </el-table-column>

        <el-table-column fixed="right" label="操作" width="220">
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

      <!-- 分页控件 -->
      <div class="pager">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-size="pageSize"
          :page-sizes="[3, 5, 8, 10]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import API from '../../../utils/request'
const url = "/api/good/"

export default {
  name: "Goods",
  data() {
    return {
      baseApi: this.$store.state.baseApi,
      fileList: [],
      options: [],
      searchText: '',
      user: {},
      tableData: [],
      pageNum: 1,
      pageSize: 5,
      entity: {},
      total: 0,
      dialogFormVisible: false
    };
  },
  created() {
    this.user = localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {}
    this.load()
  },
  methods: {
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.load()
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.load()
    },
    handleRecommend(good){
      API.get(url + "recommend", {
        params: {
          id: good.id,
          isRecommend : good.recommend,
        }
      }).then(res => {
        if(res.code==='200'){
          this.$message.success("修改成功")
        }else{
          this.$message.error(res.msg)
        }
      })
    },
    load() {
       API.get(url + "fullPage", {
          params: {
            pageNum: this.pageNum,
            pageSize: this.pageSize,
            searchText: this.searchText
          }
       }).then(res => {
          this.tableData = res.data.records
          this.total = res.data.total
       })
    },
    reset(){
      this.searchText = '';
      this.load()
    },
    add() {
      this.$router.push("goodInfo")
    },
    edit(obj) {
      this.entity = JSON.parse(JSON.stringify(obj))
      this.$router.push({name: 'goodInfo',query:{good: JSON.stringify(this.entity)}})
    },
    handleImgSuccess(res){
      this.entity.imgs = res.data;
      API.post(url, this.entity).then(res2 => {
        if (res2.code === '200') {
          this.$message({ type: "success", message: "操作成功" })
        } else {
          this.$message({ type: "error", message: res2.msg })
        }
        this.load()
        this.dialogFormVisible = false
      })
    },
    save() {
      if(this.fileList.length!==0){
        this.$refs.upload.submit();
      }else{
        API.post(url, this.entity).then(res2 => {
          if (res2.code === '200') {
            this.$message({ type: "success", message: "操作成功" })
          } else {
            this.$message({ type: "error", message: res2.msg })
          }
          this.load()
          this.dialogFormVisible = false
        })
      }
    },
    del(id) {
      API.delete(url + id).then(res => {
        this.$message({ type: "success", message: "操作成功" })
        this.load()
      })
    },
    handleChange(file, fileList){
      this.fileList = fileList.slice(-3);
    },
  }
};
</script>

<style scoped>
.goods-page {
  padding: 12px;
}

/* 顶部工具栏 */
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}
.search-input {
  width: 320px;
}
.toolbar-right .el-button + .el-button {
  margin-left: 8px;
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

/* 图片缩略图 */
.thumb {
  width: 100px;
  height: 80px;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  overflow: hidden;
}
.img-error {
  width: 100%;
  height: 100%;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fb;
}

/* 文本省略与对齐 */
.ellipsis {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.mono {
  font-variant-numeric: tabular-nums;
}

/* 分页 */
.pager {
  display: flex;
  justify-content: flex-end;
  padding: 14px 6px;
}
</style>
