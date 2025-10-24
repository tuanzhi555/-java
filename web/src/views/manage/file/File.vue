<!--
 * 文件管理页面（并排操作栏 + 前端模糊搜索兜底）
-->
<template>
  <div class="page-wrap">
    <!-- 顶部工具栏：左侧检索，右侧操作 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="searchText"
          placeholder="请输入文件名（支持模糊搜索）"
          prefix-icon="el-icon-search"
          clearable
          @keyup.enter.native="search"
          class="w-280"
        />
        <el-button type="primary" round @click="search">搜索</el-button>
        <el-button type="info" round @click="reload">重置</el-button>
      </div>

      <div class="toolbar-right">
        <el-upload
          action="/file/upload"
          :show-file-list="false"
          :on-success="handleFileUploadSuccess"
        >
          <el-button type="warning" round>
            <i class="el-icon-upload2" style="margin-right:6px" />
            上传
          </el-button>
        </el-upload>

        <el-button type="danger" round @click="delBatch">
          <i class="el-icon-remove" style="margin-right:6px" />
          批量删除
        </el-button>
      </div>
    </div>

    <!-- 文件表格 -->
    <el-table
      stripe
      border
      fixed
      :data="displayData"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="48" />

      <!-- 预览缩略图 -->
      <el-table-column label="预览" width="120">
        <template slot-scope="{ row }">
          <el-image
            v-if="isImage(row.type)"
            :src="baseApi + row.url"
            fit="cover"
            class="thumb"
            :preview-src-list="[baseApi + row.url]"
          />
          <div v-else class="file-icon">
            <i class="el-icon-document" />
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="name" label="文件名" min-width="360" />
      <el-table-column label="文件类型" width="120">
        <template slot-scope="{ row }">
          <el-tag size="small" type="success">{{ row.type }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="size" label="文件大小" width="120" />

      <el-table-column label="操作" width="220">
        <template slot-scope="{ row }">
          <a :href="baseApi + row.url">
            <el-button size="mini" type="primary" plain>
              <i class="el-icon-download" style="margin-right:4px" />
              下载
            </el-button>
          </a>
          <el-button
            size="mini"
            type="danger"
            plain
            @click="handleDelete(row.id)"
          >
            <i class="el-icon-delete" style="margin-right:4px" />
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pager">
      <el-pagination
        :current-page="currentPage"
        :page-sizes="[5, 10, 20, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentPage"
      />
    </div>
  </div>
</template>

<script>
export default {
  name: "File",
  created() {
    this.load();
  },
  data() {
    return {
      baseApi: this.$store.state.baseApi,
      tableData: [],
      total: 0,
      pageSize: 10,
      currentPage: 1,
      searchText: "",
      multipleSelection: [],
      // 是否启用本地模糊过滤（兜底）
      localFilter: false,
      // 记住分页，便于临时放大页大小后再恢复
      _pageSizeBackup: 10,
    };
  },
  computed: {
    // 前端模糊过滤兜底
    displayData() {
      if (!this.localFilter || !this.searchText.trim()) {
        return this.tableData;
      }
      const kw = this.searchText.trim().toLowerCase();
      return this.tableData.filter((r) =>
        (r.name || "").toLowerCase().includes(kw)
      );
    },
  },
  methods: {
    isImage(type) {
      return ["png", "jpg", "jpeg", "gif", "webp"].includes(
        (type || "").toLowerCase()
      );
    },

    // 分页变化
    handleSizeChange(size) {
      this.pageSize = size;
      // 只在非本地过滤时走服务端分页
      if (!this.localFilter) this.load();
    },
    handleCurrentPage(page) {
      this.currentPage = page;
      if (!this.localFilter) this.load();
    },

    handleSelectionChange(val) {
      this.multipleSelection = val;
    },

    // 上传成功
    handleFileUploadSuccess() {
      this.$message.success("上传成功");
      this.reload();
    },

    // 页面加载
    async load(all = false) {
      // all=true 时，拉一页“大数据量”到前端做本地模糊过滤
      const params = {
        pageNum: all ? 1 : this.currentPage,
        pageSize: all ? 9999 : this.pageSize,
        fileName: this.localFilter ? "" : this.searchText, // 服务端若不支持 like，这里传空
      };
      const res = await this.request.get("/file/page", { params });
      if (res.code === "200") {
        // 统一格式化 size
        const rows = (res.data.records || []).map((s) => {
          const size = s.size * 1; // 以 Kb 为单位（根据你的后端实际而定）
          let text = "";
          if (size < 1024) text = size + " Kb";
          else if (size < 1024 * 1024)
            text = (size / 1024).toFixed(2) + " Mb";
          else text = (size / 1024 / 1024).toFixed(2) + " Gb";
          return { ...s, size: text };
        });
        this.tableData = rows;
        this.total = res.data.total;
      }
    },

    // 搜索：优先尝试后端；若后端是“等值匹配”，启用前端兜底
    async search() {
      this.currentPage = 1;

      // ——推荐后端改成 LIKE；当前兜底如下——
      this.localFilter = true; // 启用本地过滤
      this._pageSizeBackup = this.pageSize;
      await this.load(true); // 拉大页，获取尽量多的数据做本地过滤
      // 分页 UI 对本地过滤无效，这里可以选择隐藏分页，或者保持不动
    },

    // 重置
    async reload() {
      this.searchText = "";
      this.localFilter = false;
      this.pageSize = this._pageSizeBackup || 10;
      this.currentPage = 1;
      await this.load();
    },

    // 删除
    handleDelete(id) {
      this.$confirm("确认删除该文件吗?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        this.request.delete("/file/" + id).then((res) => {
          if (res.code === "200") {
            this.$message.success("删除成功");
            this.reload();
          } else {
            this.$message.error(res.msg);
          }
        });
      });
    },

    // 批量删除
    delBatch() {
      const ids = this.multipleSelection.map((v) => v.id);
      if (ids.length === 0) {
        this.$message.warning("请先选择要删除的文件");
        return;
      }
      this.$confirm("确认删除这些文件吗?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        this.request.post("/file/del/batch", ids).then((res) => {
          if (res.code === "200") {
            this.$message.success("删除成功");
            this.reload();
          } else {
            this.$message.error(res.msg);
          }
        });
      });
    },
  },
};
</script>

<style scoped>
.page-wrap {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
}

/* 顶部工具栏：左右分布 */
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}
.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.w-280 {
  width: 280px;
}

/* 缩略图样式 */
.thumb {
  width: 64px;
  height: 64px;
  border-radius: 6px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
}
.file-icon {
  width: 64px;
  height: 64px;
  border-radius: 6px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 20px;
}

.pager {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
