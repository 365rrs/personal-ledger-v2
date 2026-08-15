# Project Structure

## Repository Layout

```
personal-ledger-v2/
├── .amazonq/rules/          # Amazon Q 编码规范和开发规则
│   └── memory-bank/         # 开发记忆库
├── .kiro/steering/          # Kiro AI 助手引导文档
│   ├── frontend.md          # 前端开发指南
│   ├── language.md          # 语言规则
│   ├── product.md           # 产品概述
│   ├── structure.md         # 项目结构（本文件）
│   └── tech.md              # 技术栈说明
├── docs/                    # 产品需求文档和使用说明
│   ├── qianji/              # 钱迹导出相关文件
│   ├── templete/            # 模板文件
│   ├── 00-09 系列 PRD       # 功能需求文档
│   └── 开发完成系列文档      # 开发总结文档
├── personal-ledger-backend/ # 后端 Spring Boot 应用
└── personal-ledger-frontend/# 前端 Vue 3 应用
```

## Backend Application Structure

```
personal-ledger-backend/
├── db/                      # Database scripts
│   ├── schema.sql          # Complete database schema
│   ├── data.sql            # Initial seed data
│   ├── payment_channel_data.sql
│   ├── init_data_clean_rules.sql
│   └── *.sql               # Migration and update scripts
├── src/main/
│   ├── java/com/ledger/
│   │   ├── common/         # Common utilities and response wrappers
│   │   ├── config/         # Spring configuration classes
│   │   ├── controller/     # REST API controllers
│   │   ├── converter/      # MapStruct converters (DTO ↔ Entity)
│   │   ├── dto/            # Data Transfer Objects (request/query params)
│   │   ├── entity/         # Database entity classes
│   │   ├── enums/          # Enumeration types
│   │   ├── exception/      # Custom exceptions and global handler
│   │   ├── mapper/         # MyBatis Plus mappers (DAO layer)
│   │   ├── mybatis/        # MyBatis base classes
│   │   ├── service/        # Business logic layer
│   │   │   └── impl/       # Service implementations
│   │   ├── util/           # Utility classes
│   │   ├── vo/             # View Objects (response data)
│   │   └── LedgerApplication.java  # Spring Boot entry point
│   └── resources/
│       ├── application.yml # Application configuration
│       └── banner.txt      # Startup banner
└── pom.xml                 # Maven dependencies
```

## Frontend Application Structure

```
personal-ledger-frontend/
├── public/                  # Static assets
├── src/
│   ├── api/                # API service modules
│   │   ├── bill.js         # Bill-related APIs
│   │   ├── billImport.js   # Import-related APIs
│   │   ├── category.js     # Category APIs
│   │   ├── dataCleanRule.js# Data cleaning rule APIs
│   │   ├── paymentChannel.js# Payment channel APIs
│   │   └── tag.js          # Tag APIs
│   ├── components/         # Reusable Vue components
│   │   └── BillEditDialog.vue
│   ├── layout/             # Layout components
│   │   └── MainLayout.vue  # Main application layout
│   ├── router/             # Vue Router configuration
│   │   └── index.js        # Route definitions
│   ├── styles/             # Global styles
│   │   └── table.css       # Table-specific styles
│   ├── utils/              # Utility functions
│   │   └── request.js      # Axios HTTP client configuration
│   ├── views/              # Page components
│   │   ├── BillImport.vue  # Bill import page
│   │   ├── BillImportDetail.vue
│   │   ├── BillList.vue    # Main bill list page
│   │   ├── Category.vue    # Category management
│   │   ├── CategoryStatistics.vue
│   │   ├── CumulativeExpense.vue
│   │   ├── DailyExpense.vue
│   │   ├── Dashboard.vue   # Dashboard/Home
│   │   ├── DataCleanRule.vue
│   │   ├── Home.vue
│   │   ├── LargeExpense.vue
│   │   ├── Ledger.vue
│   │   ├── MonthlyCumulativeExpense.vue
│   │   ├── MonthlyExpense.vue
│   │   ├── PaymentChannel.vue
│   │   ├── Statistics.vue
│   │   └── TagManagement.vue
│   ├── App.vue             # Root component
│   └── main.js             # Application entry point
├── index.html              # HTML template
├── package.json            # NPM dependencies
├── vite.config.js          # Vite configuration
└── .gitignore
```

## Layer Responsibilities

### Backend Layers

#### Controller Layer
- REST API endpoint definitions
- Request parameter validation (`@Valid`, `@Validated`)
- Delegates to Service layer
- Returns `Response<T>` wrapper
- **No business logic**

#### Service Layer
- Business logic implementation
- Transaction management
- Orchestrates multiple operations
- Converts between DTO/Entity/VO using Converters

#### Mapper Layer (DAO)
- Database access via MyBatis Plus
- Extends `BaseMapper<T>` for CRUD operations
- Custom SQL queries when needed

#### Entity Layer
- Database table mappings
- All entities extend `BaseEntity` (provides id, timestamps, audit fields)
- Uses Lombok annotations (`@Data`, `@EqualsAndHashCode`)
- MyBatis Plus annotations (`@TableName`, `@TableId`, `@TableLogic`)
- **重要**: BaseEntity 已定义的字段（id, createTime, updateTime, creatorCode, creatorName, updaterCode, updaterName, deleted）不应在子类中重复定义

#### DTO Layer
- Request objects (suffix: `DTO`)
- Query objects (suffix: `QueryDTO`)
- JSR-303 validation annotations
- Camel case naming

#### VO Layer
- Response objects (suffix: `VO`)
- Formatted for frontend consumption
- May include computed/joined fields not in Entity

#### Converter Layer
- MapStruct interfaces for object mapping
- Converts between Entity ↔ DTO ↔ VO
- Annotated with `@Mapper(componentModel = "spring")`

#### Common Layer
- `Response<T>`: Unified API response wrapper
- Shared utilities and constants

#### Config Layer
- `CorsConfig`: Cross-origin configuration
- `JacksonConfig`: JSON serialization settings
- `MybatisPlusConfig`: MyBatis Plus plugins (pagination, etc.)
- `SwaggerConfig`: API documentation configuration

#### Exception Layer
- `BusinessException`: Custom business exception
- `GlobalExceptionHandler`: Global exception handling with `@ControllerAdvice`

### Frontend Layers

#### API Layer (`src/api/`)
- Encapsulates backend API calls
- Uses Axios for HTTP requests
- Returns promises for async operations
- Organized by feature modules

#### Components Layer (`src/components/`)
- Reusable Vue components
- Shared across multiple views
- Props-based communication

#### Views Layer (`src/views/`)
- Page-level components
- Mapped to routes
- Composes smaller components
- Handles page-specific logic

#### Utils Layer (`src/utils/`)
- `request.js`: Axios instance with interceptors
- Request/response transformation
- Error handling
- Authentication token management

#### Router Layer (`src/router/`)
- Route definitions
- Navigation guards
- Lazy loading for code splitting

## Naming Conventions

### Backend

#### Packages
- All lowercase, singular form
- Example: `com.ledger.service`, `com.ledger.entity`

#### Classes
- Controllers: `*Controller` (e.g., `BillController`)
- Services: `*Service` interface, `*ServiceImpl` implementation
- Mappers: `*Mapper` (e.g., `BillMapper`)
- Entities: Plain names (e.g., `Bill`, `BillCategory`)
- DTOs: `*DTO` (e.g., `BillDTO`, `BillQueryDTO`)
- VOs: `*VO` (e.g., `BillVO`)
- Converters: `*Converter` (e.g., `BillConverter`)

#### Methods
- Service layer:
  - Query single: `get*` (e.g., `getById`)
  - Query list: `list*` (e.g., `listByType`)
  - Query page: `page*` (e.g., `pageBills`)
  - Query count: `count*` (e.g., `countByStatus`)
  - Create: `save*` or `create*` or `add*`
  - Update: `update*` or `modify*`
  - Delete: `remove*` or `delete*` (always logical delete)
  - Batch operations: `batch*` (e.g., `batchUpdateCategory`)
  - Import operations: `import*` or `parse*`
  - Export operations: `export*` or `convert*`
  - Statistics: `statistics*` or `calculate*`

#### Database
- Tables: lowercase with underscores (e.g., `bill`, `bill_category`)
- Columns: lowercase with underscores (e.g., `transaction_date`, `payment_channel_id`)
- Indexes: `pk_*` (primary), `uk_*` (unique), `idx_*` (normal)

### Frontend

#### Files
- Components: PascalCase (e.g., `BillEditDialog.vue`)
- Views: PascalCase (e.g., `BillList.vue`)
- Utils: camelCase (e.g., `request.js`)
- API modules: camelCase (e.g., `bill.js`)

#### Variables & Functions
- camelCase for variables and functions
- PascalCase for component names
- UPPER_SNAKE_CASE for constants

## Key Design Patterns

### Backend Patterns

#### Inheritance
- All entities extend `BaseEntity` for common fields (id, audit fields, timestamps)
- **Never redefine BaseEntity fields in subclasses**

#### Logical Deletion
- All deletes are logical (soft delete)
- Uses `deleted` field with `@TableLogic` annotation
- Automatically filtered in queries

#### Audit Trail
- Auto-populated fields: `creatorCode`, `creatorName`, `updaterCode`, `updaterName`
- Timestamps: `createTime`, `updateTime`
- Handled by MyBatis Plus `MetaObjectHandler`

#### Response Wrapping
- All API responses use `Response<T>` wrapper
- Contains: `code`, `message`, `data`, `timestamp`
- Never return raw entities or primitives

#### Data Flow
```
Request → Controller (DTO) → Service (Entity) → Mapper → Database
Response ← Controller (VO) ← Service (Entity) ← Mapper ← Database
```

### Frontend Patterns

#### Component Composition
- Views compose smaller components
- Props down, events up
- Use `v-model` for two-way binding

#### State Management
- Local state with `ref()` and `reactive()`
- Computed properties for derived state
- No global state management (Vuex/Pinia) currently

#### API Integration
- Centralized API modules in `src/api/`
- Async/await for API calls
- Error handling in request interceptors

#### Routing
- Lazy loading for better performance
- Route-based code splitting
- Navigation guards for auth (if needed)

## Module Organization

### Backend Modules

#### Bill Management (账单管理)
- Core transaction recording
- 核心实体: `Bill.java`
- Controller: `BillController.java`
- Service: `BillService.java`
- 主要功能: CRUD、分页查询、批量操作、统计分析、数据导出

#### Bill Import (账单导入)
- Bulk data import from CSV/Excel
- 核心实体: `BillImportRecord.java`, `BillImportDetail.java`, `BillImportRecordData.java`
- Controller: `BillImportController.java`
- Service: `BillImportService.java`
- Util: `FileParseUtil.java`, `DataHashUtil.java`
- 主要功能: 文件解析、数据预览、指纹去重、导入确认、历史记录

#### Bill Category (账单分类)
- Category and sub-category management
- 核心实体: `BillCategory.java`
- Controller: `BillCategoryController.java`
- Service: `BillCategoryService.java`
- 主要功能: 分类 CRUD、分类统计、排序管理、分类树

#### Bill Tag (账单标签)
- Flexible tagging system
- 核心实体: `BillTag.java`, `BillTagRelation.java`
- Controller: `BillTagController.java`
- Service: `BillTagService.java`
- 主要功能: 标签 CRUD、标签关联、状态管理

#### Payment Channel (支付渠道)
- Payment method tracking
- 核心实体: `BillPaymentChannel.java`
- Controller: `BillPaymentChannelController.java`
- Service: `BillPaymentChannelService.java`
- 主要功能: 渠道 CRUD、渠道统计

#### Data Cleaning (数据清洗)
- Rule-based data normalization
- 核心实体: `BillDataCleanRule.java`
- Controller: `BillDataCleanRuleController.java`
- Service: `BillDataCleanService.java`
- 主要功能: 规则 CRUD、规则匹配、自动清洗、规则测试

#### Statistics (统计分析)
- Integrated in BillService and BillController
- 主要功能:
  - 每日支出统计 (`/bill/daily-expense`)
  - 每月支出统计 (`/bill/monthly-expense`)
  - 累计支出统计 (`/bill/cumulative-expense`)
  - 分类统计分析 (`/bill-category/statistics`)
  - 大额支出查询

### Frontend Modules

#### Bill Management (`BillList.vue`)
- 账单列表展示和管理
- 功能: 
  - 分页查询、条件筛选（日期、类型、分类、标签等）
  - 拼音搜索（分类快速检索）
  - 单条编辑、批量更新、批量跳过
  - 账单导出（Excel、钱迹格式）
  - 与 BillEditDialog 组件集成

#### Import Management (`BillImport.vue`, `BillImportDetail.vue`)
- 账单导入管理
- `BillImport.vue`: 
  - 文件上传（CSV/Excel）
  - 导入预览
  - 导入确认
  - 导入历史记录
- `BillImportDetail.vue`: 
  - 导入详情查看
  - 导入数据验证结果
  - 重复数据识别

#### Category Management (`Category.vue`)
- 分类管理
- 功能:
  - 分类 CRUD 操作
  - 二级分类结构展示
  - 拖拽排序（使用 SortableJS）
  - 是否计入统计开关

#### Statistics & Analytics (统计分析模块)
- `Dashboard.vue`: 首页概览和汇总信息
- `CategoryStatistics.vue`: 分类维度统计分析
- `DailyExpense.vue`: 每日支出趋势
- `MonthlyExpense.vue`: 每月支出对比
- `MonthlyCumulativeExpense.vue`: 每月累计支出
- `CumulativeExpense.vue`: 累计支出趋势
- `LargeExpense.vue`: 大额支出分析
- 使用 ECharts 进行数据可视化

#### Configuration (配置管理模块)
- `PaymentChannel.vue`: 支付渠道配置
- `TagManagement.vue`: 标签管理（CRUD、启用/禁用）
- `DataCleanRule.vue`: 数据清洗规则配置
  - 规则 CRUD
  - 规则优先级
  - 规则测试

#### Other Pages
- `Home.vue`: 主页/欢迎页
- `Ledger.vue`: 账本视图（如有）
- `Statistics.vue`: 统计总览页

## Documentation Location

- **产品需求文档 (PRD)**: `/docs/*.md`
  - 00-账单导入功能PRD.md
  - 01-账单功能PRD.md
  - 02-账单标签功能PRD.md
  - 03-账单分类功能PRD.md
  - 04-支付渠道功能PRD.md
  - 05-账单数据清洗规则.md
  - 06-账单数据清洗功能使用说明.md
  - 07-账单统计分析功能使用说明.md
  - 08-每日结余统计功能-前端部署说明.md
  - 09-账单导入一键转换功能说明.md
- **开发完成文档**: `/docs/` 下的"开发完成"系列文档
- **编码规范**: `/.amazonq/rules/*.md`
- **数据库文档**: `/personal-ledger-backend/db/`
  - `schema.sql`: 完整数据库架构
  - `tables.md`: 数据表说明
  - 其他 SQL 脚本：数据初始化和迁移
- **API 文档**: 运行时通过 Swagger UI 访问
  - http://localhost:8081/swagger-ui.html (Swagger 2)
  - http://localhost:8081/v3/api-docs (SpringDoc OpenAPI)
- **AI 引导文档**: `/.kiro/steering/*.md`
