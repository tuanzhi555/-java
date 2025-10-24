<!--
 * 用户管理页面（美化版，仅前端样式与交互优化）
 * @Author: ShanZhu + 优化：ChatGPT
-->
<template>
  <div class="user-page">
    <!-- 筛选工具条 -->
    <el-card shadow="never" class="toolbar-card">
      <el-form :inline="true" size="small" class="toolbar">
        <el-form-item label="查询字段">
          <el-select v-model="searchMode" placeholder="请选择" style="width: 140px">
            <el-option value="id" label="用户id" />
            <el-option value="username" label="账号" />
            <el-option value="nickname" label="昵称" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="searchMode === 'id'" label="关键词">
          <el-input
            v-model="searchParams.id"
            placeholder="请输入用户 id"
            clearable
            style="width: 240px"
            @keyup.enter.native="search"
          >
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
        </el-form-item>
        <el-form-item v-if="searchMode === 'username'" label="关键词">
          <el-input
            v-model="searchParams.username"
            placeholder="请输入账号"
            clearable
            style="width: 240px"
            @keyup.enter.native="search"
          >
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
        </el-form-item>
        <el-form-item v-if="searchMode === 'nickname'" label="关键词">
          <el-input
            v-model="searchParams.nickname"
            placeholder="请输入昵称"
            clearable
            style="width: 240px"
            @keyup.enter.native="search"
          >
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="search">搜索</el-button>
          <el-button plain icon="el-icon-refresh" @click="reload">重置</el-button>
        </el-form-item>

        <el-form-item class="grow"></el-form-item>

        <el-form-item>
          <el-button type="success" icon="el-icon-circle-plus" @click="handleAdd">新增</el-button>
          <el-button type="danger" icon="el-icon-remove" @click="delBatch">批量删除</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table
        :data="tableData"
        stripe
        border
        size="small"
        highlight-current-row
        @selection-change="handleSelectionChange"
        :header-cell-style="headerStyle"
        :cell-style="cellStyle"
        empty-text="暂无用户数据"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="id" label="ID" width="70" sortable />
        <el-table-column prop="username" label="账号" width="160" show-overflow-tooltip />
        <el-table-column label="身份" width="120">
          <template slot-scope="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'info'" effect="light">
              {{ row.role === 'admin' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" width="140" show-overflow-tooltip />
        <el-table-column prop="phone" label="电话" width="160" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" width="220" show-overflow-tooltip />
        <el-table-column prop="address" label="地址" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" fixed="right" width="180">
          <template slot-scope="{ row }">
            <el-button size="mini" type="success" @click="handleEdit(row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pager">
        <el-pagination
          background
          :current-page="currentPage"
          :page-sizes="[3, 5, 8, 10]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentPage"
        />
      </div>
    </el-card>

    <!-- 新增 / 编辑 弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogFormVisible" width="520px">
      <el-form
        ref="userForm"
        :model="user"
        :rules="rules"
        label-width="70px"
        size="small"
      >
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="user.nickname" maxlength="20" />
        </el-form-item>

        <el-form-item label="身份" prop="role">
          <el-select v-model="user.role" placeholder="请选择">
            <el-option v-for="opt in roleOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="电话" prop="phone">
          <el-input v-model="user.phone" maxlength="20" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="user.email" maxlength="50" />
        </el-form-item>

        <el-form-item label="地址" prop="address">
          <el-input v-model="user.address" maxlength="100" />
        </el-form-item>
      </el-form>

      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: "User",
  data() {
    return {
      tableData: [],
      total: 0,
      pageSize: 5,
      currentPage: 1,

      // 查询
      searchMode: 'id',
      searchParams: { id: '', username: '', nickname: '' },

      // 弹窗
      dialogFormVisible: false,
      user: {},
      roleOptions: [
        { value: 'admin', label: '管理员' },
        { value: 'user', label: '用户' }
      ],
      rules: {
        nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
        role: [{ required: true, message: '请选择身份', trigger: 'change' }],
        phone: [
          { pattern: /^$|^[\d\-\+\s]{3,20}$/, message: '电话格式不正确', trigger: 'blur' }
        ],
        email: [
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
        ]
      },

      multipleSelection: []
    }
  },
  computed: {
    dialogTitle() {
      return this.user && this.user.id ? '编辑用户' : '新增用户'
    }
  },
  created() {
    this.load()
  },
  methods: {
    /* ---------- 样式辅助 ---------- */
    headerStyle() {
      return { background: '#fafafa', color: '#333', fontWeight: 600 }
    },
    cellStyle() {
      return { color: '#444' }
    },

    /* ---------- 分页 ---------- */
    handleSizeChange(size) {
      this.pageSize = size
      this.load()
    },
    handleCurrentPage(page) {
      this.currentPage = page
      this.load()
    },

    /* ---------- 表格选择 ---------- */
    handleSelectionChange(list) {
      this.multipleSelection = list
    },

    /* ---------- 数据加载 ---------- */
    load() {
      this.request.get("/user/page", {
        params: {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          id: this.searchParams.id,
          username: this.searchParams.username,
          nickname: this.searchParams.nickname
        }
      }).then(res => {
        if (res.code === '200') {
          this.tableData = res.data.records || []
          this.total = res.data.total || 0
        }
      })
    },

    /* ---------- 查询 ---------- */
    search() {
      this.currentPage = 1
      this.load()
    },
    reload() {
      this.searchParams = { id: '', username: '', nickname: '' }
      this.currentPage = 1
      this.load()
    },

    /* ---------- 新增 / 编辑 ---------- */
    handleAdd() {
      this.user = { role: 'user' }
      this.dialogFormVisible = true
      this.$nextTick(() => this.$refs.userForm && this.$refs.userForm.clearValidate())
    },
    handleEdit(row) {
      this.user = JSON.parse(JSON.stringify(row))
      this.dialogFormVisible = true
      this.$nextTick(() => this.$refs.userForm && this.$refs.userForm.clearValidate())
    },
    save() {
      this.$refs.userForm.validate(valid => {
        if (!valid) return
        this.request.post("/user", this.user).then(res => {
          if (res.code === '200') {
            this.$message.success("保存成功")
            this.dialogFormVisible = false
            this.load()
          } else {
            this.$message.error(res.msg)
          }
        })
      })
    },

    /* ---------- 删除 ---------- */
    handleDelete(id) {
      this.$confirm('确认删除该用户吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.request.delete("/user/" + id).then(res => {
          if (res.code === '200') {
            this.$message.success("删除成功")
            this.load()
          } else {
            this.$message.error(res.msg)
          }
        })
      })
    },
    delBatch() {
      const ids = this.multipleSelection.map(v => v.id)
      if (!ids.length) {
        this.$message.info('请先选择要删除的用户')
        return
      }
      this.$confirm('确认删除选中的用户吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.request.post("/user/del/batch", ids).then(res => {
          if (res.code === '200') {
            this.$message.success("删除成功")
            this.load()
          } else {
            this.$message.error(res.msg)
          }
        })
      })
    }
  }
}
</script>

<style scoped>
.user-page { }
.toolbar-card { margin-bottom: 12px; border-radius: 10px; }
.table-card { border-radius: 10px; }
.toolbar { display: flex; align-items: center; flex-wrap: wrap; }
.toolbar .grow { flex: 1 1 auto; }
.pager { margin-top: 12px; display: flex; justify-content: center; }
</style>
