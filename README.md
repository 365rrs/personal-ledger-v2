# 个人账本系统 V2

<div align="center">

![Version](https://img.shields.io/badge/version-2.0.0-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.6.13-brightgreen.svg)
![Vue](https://img.shields.io/badge/Vue-3.3.4-green.svg)
![License](https://img.shields.io/badge/license-MIT-orange.svg)

一个功能完善的个人财务管理系统，支持账单记录、数据导入、多维度统计分析和支出控制。

[功能特性](#功能特性) • [技术栈](#技术栈) • [快速开始](#快速开始) • [项目结构](#项目结构) • [功能截图](#功能截图)

</div>

---

## ✨ 功能特性

### 📝 账单管理
- **账单列表**：查看、编辑、删除账单，支持多条件筛选和分页
- **账单导入**：支持 CSV 文件批量导入，自动去重和数据清洗
- **标签管理**：为账单添加自定义标签，方便分类和检索
- **批量操作**：支持批量修改分类、标签、支付渠道等

### 📊 统计分析
- **每日支出统计**：柱状图+折线图展示每日支出趋势，点击查看明细
- **累计支出统计**：堆叠柱状图展示累计支出，分析当日占比
- **分类统计**：按月/按年统计各分类支出，支持收入/支出切换
- **大额支出列表**：自定义阈值，快速定位大额消费，控制支出

### ⚙️ 系统配置
- **分类管理**：自定义收支分类，支持二级分类
- **支付渠道管理**：管理常用支付方式
- **数据清洗规则**：自动化数据清洗，提高导入数据质量
- **标签管理**：创建和管理账单标签

### 🎯 核心亮点
- ✅ **可视化图表**：ECharts 多维度数据展示
- ✅ **智能交互**：点击图表查看明细，操作便捷
- ✅ **数据导入**：支持 CSV 批量导入，自动去重
- ✅ **支出控制**：大额预警，及时发现异常消费
- ✅ **响应式设计**：适配不同屏幕尺寸

---

## 🛠 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.6.13 | 基础框架 |
| MyBatis Plus | 3.5.2 | ORM 框架 |
| MySQL | 8.0 | 数据库 |
| Swagger | 3.0 | API 文档 |
| Lombok | - | 简化代码 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.3.4 | 前端框架 |
| Element Plus | 2.3.14 | UI 组件库 |
| ECharts | 5.4.3 | 数据可视化 |
| Axios | 1.5.0 | HTTP 客户端 |
| Vite | 4.4.9 | 构建工具 |

---

## 🚀 快速开始

### 环境要求

- JDK 8+
- Node.js 14+
- MySQL 8.0+
- Maven 3.6+

### 后端启动

1. **创建数据库**
```bash
mysql -u root -p
CREATE DATABASE personal_ledger_v2 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **执行数据库脚本**
```bash
mysql -u root -p personal_ledger_v2 < personal-ledger-backend/db/schema.sql
```

3. **修改配置文件**

编辑 `personal-ledger-backend/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/personal_ledger_v2
    username: root
    password: your_password
```

4. **启动后端服务**
```bash
cd personal-ledger-backend
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

Swagger 文档：`http://localhost:8080/swagger-ui.html`

### 前端启动

1. **安装依赖**
```bash
cd personal-ledger-frontend
npm install
```

2. **启动开发服务器**
```bash
npm run dev
```

前端应用将在 `http://localhost:3000` 启动

3. **构建生产版本**
```bash
npm run build
```

---

## 📁 项目结构

```
personal-ledger-v2/
├── personal-ledger-backend/          # 后端项目
│   ├── db/                          # 数据库脚本
│   │   └── schema.sql              # 表结构初始化
│   ├── src/main/
│   │   ├── java/com/ledger/
│   │   │   ├── controller/         # 控制层
│   │   │   ├── service/            # 业务逻辑层
│   │   │   ├── mapper/             # 数据访问层
│   │   │   ├── entity/             # 实体类
│   │   │   ├── dto/                # 数据传输对象
│   │   │   ├── vo/                 # 视图对象
│   │   │   ├── converter/          # 对象转换器
│   │   │   ├── common/             # 公共类
│   │   │   ├── config/             # 配置类
│   │   │   ├── exception/          # 异常处理
│   │   │   └── util/               # 工具类
│   │   └── resources/
│   │       ├── mapper/             # MyBatis XML
│   │       └── application.yml     # 配置文件
│   └── pom.xml
├── personal-ledger-frontend/         # 前端项目
│   ├── src/
│   │   ├── api/                    # API 接口
│   │   ├── components/             # 公共组件
│   │   ├── layout/                 # 布局组件
│   │   ├── router/                 # 路由配置
│   │   ├── utils/                  # 工具函数
│   │   ├── views/                  # 页面组件
│   │   ├── styles/                 # 样式文件
│   │   ├── App.vue                 # 根组件
│   │   └── main.js                 # 入口文件
│   ├── package.json
│   └── vite.config.js
├── docs/                            # 项目文档
└── README.md
```

---

## 💾 数据库设计

### 核心表结构

#### bill（账单表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| transaction_date | DATE | 交易日期 |
| transaction_time | TIME | 交易时间 |
| income_amount | DECIMAL(10,2) | 收入金额 |
| expense_amount | DECIMAL(10,2) | 支出金额 |
| amount_type | VARCHAR(20) | 收支类型 |
| category | VARCHAR(50) | 分类 |
| payment_channel | VARCHAR(50) | 支付渠道 |
| transaction_desc | VARCHAR(200) | 交易描述 |
| manual_remark | VARCHAR(200) | 备注 |
| include_in_statistics | VARCHAR(1) | 是否计入统计 |

#### bill_category（分类表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| category_name | VARCHAR(50) | 分类名称 |
| parent_id | BIGINT | 父分类ID |
| category_type | VARCHAR(20) | 分类类型 |

#### bill_tag（标签表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| tag_name | VARCHAR(50) | 标签名称 |
| tag_category | VARCHAR(50) | 标签分类 |
| tag_status | VARCHAR(1) | 状态 |

---

## 📸 功能截图

### 账单管理
- 账单列表：支持多条件筛选、分页、批量操作
- 账单导入：CSV 文件导入，自动去重

### 统计分析
- 每日支出：柱状图+折线图，点击查看明细
- 累计支出：堆叠柱状图，展示累计趋势
- 分类统计：按分类统计，支持按月/按年
- 大额支出：自定义阈值，快速定位

---

## 📖 开发文档

详细文档请查看 `docs/` 目录：

- [接口开发规范](docs/api-development-standard.md)
- [API 响应规范](docs/api-response-standard.md)
- [MyBatis Plus 开发规范](docs/mybatis-plus-standard.md)
- [数据库文件管理规范](docs/database-file-management.md)
- [账单数据清洗功能说明](docs/06-账单数据清洗功能使用说明.md)
- [账单统计分析功能说明](docs/07-账单统计分析功能使用说明.md)

---

## 🔧 开发规范

### 后端规范
- 遵循阿里巴巴 Java 开发手册（P3C）
- RESTful API 设计
- 统一响应格式 `Response<T>`
- 统一异常处理
- MyBatis Plus 代码生成

### 前端规范
- Vue 3 Composition API
- ESLint 代码检查
- 组件化开发
- 统一 API 封装

---

## 📝 更新日志

### v2.0.0 (2026-03-09)

**新增功能**
- ✨ 账单导入功能，支持 CSV 批量导入
- ✨ 数据清洗规则，自动化数据处理
- ✨ 标签管理，支持账单标签
- ✨ 每日支出统计，可视化图表展示
- ✨ 累计支出统计，趋势分析
- ✨ 分类统计，按月/按年统计
- ✨ 大额支出列表，支出控制

**优化改进**
- 🎨 菜单结构优化，分组更清晰
- 🎨 响应式设计，适配多种屏幕
- ⚡ 性能优化，查询速度提升

---

## 📄 许可证

MIT License

Copyright (c) 2026 Personal Ledger

---

## 📧 联系方式

如有问题或建议，欢迎通过以下方式联系：

- 提交 Issue
- 发送邮件

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给一个 Star！⭐**

Made with ❤️ by Personal Ledger Team

</div>
