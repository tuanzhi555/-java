<!--
 * 后台订单管理页面（美化版，仅前端样式与格式化）
 * @Author: ShanZhu  +  优化：ChatGPT
-->
<template>
  <div class="order-page">
    <!-- 顶部筛选工具条 -->
    <el-card shadow="never" class="toolbar-card">
      <el-form :inline="true" :model="searchForm" size="small" class="toolbar">
        <el-form-item label="订单状态">
          <el-select
            v-model="searchMode"
            placeholder="全部状态"
            clearable
            style="width: 140px"
          >
            <el-option value="已支付" label="已支付" />
            <el-option value="已发货" label="已发货" />
            <el-option value="已收货" label="已收货" />
          </el-select>
        </el-form-item>

        <el-form-item label="订单号">
          <el-input
            v-model="searchText"
            placeholder="输入订单编号回车搜索"
            clearable
            @keyup.enter.native="load"
            style="width: 240px"
          >
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="load">搜索</el-button>
          <el-button plain icon="el-icon-refresh" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格主体 -->
    <el-card shadow="never" class="table-card">
      <el-table
        :data="tableData"
        border
        stripe
        size="small"
        highlight-current-row
        style="width: 100%"
        :header-cell-style="headerStyle"
        :cell-style="cellStyle"
        empty-text="暂无订单数据"
      >
        <el-table-column prop="id" label="ID" width="70" sortable />
        <el-table-column prop="orderNo" label="订单编号" width="220" show-overflow-tooltip />
        <el-table-column
          prop="totalPrice"
          label="总价"
          width="120"
          :formatter="(row)=>currency(row.totalPrice)"
        />
        <el-table-column prop="userId" label="下单人ID" width="100" />
        <el-table-column prop="linkUser" label="联系人" width="120" />
        <el-table-column prop="linkPhone" label="联系电话" width="150" />
        <el-table-column prop="linkAddress" label="送货地址" min-width="260" show-overflow-tooltip />

        <el-table-column label="状态" width="110">
          <template slot-scope="{ row }">
            <el-tag :type="statusTagType(row.state)" effect="light">
              {{ row.state }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="下单时间" min-width="180" show-overflow-tooltip />

        <el-table-column label="操作" fixed="right" width="200">
          <template slot-scope="{ row }">
            <el-button type="primary" size="mini" @click="showDetail(row)">详情</el-button>
            <el-popconfirm
              v-if="row.state === '已支付'"
              title="确定发货吗？"
              @confirm="delivery(row)"
            >
              <el-button type="success" size="mini" slot="reference" style="margin-left: 8px">
                发货
              </el-button>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pager">
        <el-pagination
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-size="pageSize"
          :page-sizes="[3, 5, 8, 10]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>
    </el-card>

    <!-- 订单详情弹窗 -->
    <el-dialog
      title="订单详情"
      :visible.sync="dialogFormVisible"
      width="900px"
      top="5vh"
    >
      <el-table
        :data="detail"
        size="small"
        border
        :header-cell-style="headerStyle"
        :cell-style="cellStyle"
      >
        <el-table-column label="图片" width="120">
          <template slot-scope="{ row }">
            <el-image
              :src="baseApi + row.img"
              style="width: 80px; height: 80px; border-radius: 6px"
              fit="cover"
              :preview-src-list="[baseApi + row.img]"
            />
          </template>
        </el-table-column>

        <el-table-column prop="goodId" label="商品ID" width="90" />
        <el-table-column prop="goodName" label="商品名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="standard" label="规格" width="120" />
        <el-table-column prop="price" label="单价" width="100">
          <template slot-scope="{ row }">{{ currency(row.price) }}</template>
        </el-table-column>
        <el-table-column prop="discount" label="折扣" width="80" />

        <el-table-column label="实价" width="100">
          <template slot-scope="{ row }">{{ currency(row.price * row.discount) }}</template>
        </el-table-column>

        <el-table-column prop="count" label="数量" width="80" />

        <el-table-column label="小计" width="120">
          <template slot-scope="{ row }">
            {{ currency(row.price * row.discount * row.count) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="detail-summary" v-if="detail && detail.length">
        <span>合计金额：</span>
        <b>{{ currency(summaryAmount) }}</b>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import API from '../../utils/request'
const url = "/api/order/"

export default {
  name: "Order",
  data() {
    return {
      searchMode: '',
      searchText: '',
      tableData: [],
      pageNum: 1,
      pageSize: 8,
      total: 0,
      dialogFormVisible: false,
      detail: [],
      baseApi: this.$store.state.baseApi,
    };
  },
  created() {
    this.load()
  },
  computed: {
    // 订单详情合计
    summaryAmount() {
      return (this.detail || []).reduce((acc, it) => {
        return acc + (Number(it.price) * Number(it.discount) * Number(it.count))
      }, 0)
    },
  },
  methods: {
    /* ========== UI 美化用的小方法 ========== */
    headerStyle() {
      return { background: '#fafafa', color: '#333', fontWeight: 600 }
    },
    cellStyle() {
      return { color: '#444' }
    },
    currency(n) {
      const num = Number(n || 0)
      return num.toFixed(2)
    },
    statusTagType(state) {
      if (state === '已支付') return 'success'
      if (state === '已发货') return 'primary'
      if (state === '已收货') return 'info'
      return 'info'
    },

    /* ========== 交互逻辑（与原接口一致） ========== */
    handleSizeChange(val) {
      this.pageSize = val
      this.load()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.load()
    },
    reset() {
      this.searchMode = ''
      this.searchText = ''
      this.load()
    },
    load() {
      API.get(url + "/page", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          orderNo: this.searchText,
          state: this.searchMode
        }
      }).then(res => {
        this.tableData = (res.data && res.data.records) || []
        this.total = (res.data && res.data.total) || 0
      })
    },
    // 查看详情
    showDetail(row) {
      this.request.get("/api/order/orderNo/" + row.orderNo).then(res => {
        if (res.code === '200') {
          this.detail = []
          // 后端返回单条时，统一为表格数组
          this.detail.push(res.data)
          this.dialogFormVisible = true
        }
      })
    },
    // 发货
    delivery(order) {
      this.request.get("/api/order/delivery/" + order.orderNo).then(res => {
        if (res.code === '200') {
          this.$message.success("成功发货")
          order.state = '已发货'
        }
      })
    },
  },
};
</script>

<style scoped>
.order-page {
  padding: 0;
}
.toolbar-card {
  margin-bottom: 12px;
  border-radius: 10px;
}
.table-card {
  border-radius: 10px;
}
.toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}
.pager {
  margin-top: 12px;
  display: flex;
  justify-content: center;
}
.detail-summary {
  margin-top: 12px;
  text-align: right;
  font-size: 14px;
}
</style>
