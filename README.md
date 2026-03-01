# 个人账本系统

## 项目简介
个人账本系统，用于记录个人的收入和支出信息。

## 技术栈

### 后端
- Spring Boot 2.6.13
- JDK 8
- MyBati[]()s Plus 3.5.2
- MySQL 8.0
- Lombok

### 前端
- Vue 3
- Element Plus
- Vite
- Axios

## 项目结构

```
personal-ledger-v2/
├── personal-ledger-backend/   # 后端项目
│   ├── db/                    # 数据库脚本
│   │   └── schema.sql         # 数据库表结构
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/ledger/
│   │   │   │   ├── controller/    # 控制层
│   │   │   │   ├── service/       # 业务逻辑层
│   │   │   │   ├── mapper/        # 数据访问层
│   │   │   │   ├── entity/        # 实体类
│   │   │   │   ├── dto/           # 数据传输对象
│   │   │   │   ├── vo/            # 视图对象
│   │   │   │   ├── common/        # 公共类
│   │   │   │   ├── config/        # 配置类
│   │   │   │   └── LedgerApplication.java
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   └── pom.xml
└── personal-ledger-frontend/  # 前端项目
    ├── public/
    ├── src/
    │   ├── api/               # API接口
    │   ├── components/        # 组件
    │   ├── router/            # 路由
    │   ├── utils/             # 工具类
    │   ├── views/             # 页面
    │   ├── App.vue
    │   └── main.js
    ├── index.html
    ├── package.json
    └── vite.config.js
```

## 快速开始

### 后端启动

1. 创建数据库并执行 `personal-ledger-backend/db/schema.sql`
2. 修改 `application.yml` 中的数据库配置
3. 运行 `LedgerApplication.java`
4. 访问：http://localhost:8080

### 前端启动

1. 安装依赖：`npm install`
2. 启动项目：`npm run dev`
3. 访问：http://localhost:3000

## 功能模块

- 账单管理：记录收入和支出
- 分类管理：自定义收支分类
- 统计分析：收支统计和图表展示

## 数据库设计

### t_ledger（账单表）
- id：主键
- type：类型（INCOME-收入，EXPENSE-支出）
- amount：金额
- category：分类
- description：描述
- transaction_time：交易时间
- create_time：创建时间
- update_time：更新时间
- is_deleted：逻辑删除标识
