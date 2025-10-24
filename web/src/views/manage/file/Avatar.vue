<!--
 * 头像管理页面（左右工具栏 + 模糊搜索兜底 + 优化操作列上下布局）
-->
<template>
  <div class="page-wrap">
    <!-- 顶部工具栏：左侧检索，右侧操作 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="searchText"
          placeholder="按文件名（或链接）搜索，支持模糊匹配"
          prefix-icon="el-icon-search"
          clearable
          @keyup.enter.native="search"
          class="w-320"
        />
        <el-button type="primary" round @click="search">搜索</el-button>
        <el-button type="info" round @click="reload">重置</el-button>
      </div>

      <div class="toolbar-right">
        <el-button type="danger" round @click="delBatch">
          <i class="el-icon-remove" style="margin-right:6px" />
          批量删除
        </el-button>
      </div>
    </div>

    <!-- 表格 -->
    <el-table
      stripe
      border
      fixed
      :data="displayData"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="48" />

      <!-- 缩略图 -->
      <el-table-column label="头像" width="130">
        <template slot-scope="{ row }">
          <el-image
            :src="baseApi + row.url"
            fit="cover"
            class="thumb"
            :preview-src-list="[baseApi + row.url]"
          />
        </template>
      </el-table-column>

      <!-- 文件名 -->
      <el-table-column label="文件名" min-width="360">
        <template slot-scope="{ row }">
          <span class="mono">{{ getFileName(row.url) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="文件类型" width="120">
        <template slot-scope="{ row }">
          <el-tag size="small" type="success">{{ row.type }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="size" label="文件大小" width="120" />

      <!-- 操作：上面 下载 + 删除；下面 复制链接 -->
      <el-table-column label="操作" width="240">
        <template slot-scope="{ row }">
          <div class="op-col">
            <div class="op-row">
              <a :href="baseApi + row.url" class="no-underline">
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
            </div>
            <div class="op-row op-bottom">
              <el-button size="mini" type="info" plain @click="copyLink(row)">
                <i class="el-icon-document-copy" style="margin-right:4px" />
                复制链接
              </el-button>
            </div>
          </div>
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
  name: "Avatar",
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
      // 模糊搜索前端兜底
      localFilter: false,
      _pageSizeBackup: 10,
    };
  },
  computed: {
    displayData() {
      if (!this.localFilter || !this.searchText.trim()) {
        return this.tableData;
      }
      const kw = this.searchText.trim().toLowerCase();
      return this.tableData.filter((r) => {
        const name = this.getFileName(r.url).toLowerCase();
        const url = (r.url || "").toLowerCase();
        return name.includes(kw) || url.includes(kw);
      });
    },
  },
  methods: {
    getFileName(url = "") {
      try {
        const idx = url.lastIndexOf("/");
        return idx >= 0 ? url.slice(idx + 1) : url;
      } catch (e) {
        return url;
      }
    },

    // 复制链接
    async copyLink(row) {
      const link = this.baseApi + row.url;
      try {
        if (navigator.clipboard && window.isSecureContext) {
          await navigator.clipboard.writeText(link);
        } else {
          const input = document.createElement("input");
          input.value = link;
          document.body.appendChild(input);
          input.select();
          document.execCommand("copy");
          document.body.removeChild(input);
        }
        this.$message.success("已复制到剪贴板");
      } catch (e) {
        this.$message.error("复制失败，请手动复制");
      }
    },

    // 分页
    handleSizeChange(size) {
      this.pageSize = size;
      if (!this.localFilter) this.load();
    },
    handleCurrentPage(page) {
      this.currentPage = page;
      if (!this.localFilter) this.load();
    },

    handleSelectionChange(val) {
      this.multipleSelection = val;
    },

    // 加载（all=true 时拉大页用于本地模糊过滤）
    async load(all = false) {
      const params = {
        pageNum: all ? 1 : this.currentPage,
        pageSize: all ? 9999 : this.pageSize,
      };
      const res = await this.request.get("/avatar/page", { params });
      if (res.code === "200") {
        const rows = (res.data.records || []).map((s) => {
          const size = s.size * 1;
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

    // 搜索：启用本地兜底
    async search() {
      this.currentPage = 1;
      this.localFilter = true;
      this._pageSizeBackup = this.pageSize;
      await this.load(true);
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
      this.$confirm("确认删除该头像吗?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        this.request.delete("/avatar/" + id).then((res) => {
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
      if (!ids.length) {
        this.$message.warning("请先选择要删除的头像");
        return;
      }
      this.$confirm("确认删除已选头像吗?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        this.request.post("/avatar/del/batch", ids).then((res) => {
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
.w-320 {
  width: 320px;
}

/* 缩略图样式 */
.thumb {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
}

/* 等宽字体，文件名观感更整齐 */
.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas,
    "Liberation Mono", "Courier New", monospace;
}

/* 操作列：上下两行，横向按钮有间距 */
.op-col {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.op-row {
  display: flex;
  align-items: center;
  gap: 8px;               /* 现代浏览器的首选间距 */
}
.op-row > * {
  margin-right: 8px;      /* 兼容旧浏览器（不支持 gap） */
}
.op-row > *:last-child {
  margin-right: 0;
}
.op-bottom {
  margin-top: 8px;        /* 上下行间距 */
}

.no-underline {
  text-decoration: none;
}

.pager {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
