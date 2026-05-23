# Project Structure

## Repository Layout

```
personal-ledger/
├── .amazonq/rules/          # Coding standards and development rules
├── .kiro/steering/          # AI assistant steering documents
├── docs/                    # Product requirements and documentation
├── personal-ledger-backend/ # Backend Spring Boot application
├── personal-ledger-frontend/# Frontend Vue 3 application
└── LICENSE
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
  - Query count: `count*` (e.g., `countByStatus`)
  - Create: `save*` or `create*`
  - Update: `update*` or `modify*`
  - Delete: `remove*` or `delete*`

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

#### Bill Management
- Core transaction recording
- Files: `Bill.java`, `BillController.java`, `BillService.java`, etc.

#### Bill Import
- Bulk data import from CSV/Excel
- Files: `BillImportController.java`, `BillImportRecord.java`, `BillImportDetail.java`

#### Bill Category
- Category and sub-category management
- Files: `BillCategory.java`, `BillCategoryController.java`

#### Bill Tag
- Flexible tagging system
- Files: `BillTag.java`, `BillTagRelation.java`, `BillTagController.java`

#### Payment Channel
- Payment method tracking
- Files: `BillPaymentChannel.java`, `BillPaymentChannelController.java`

#### Data Cleaning
- Rule-based data normalization
- Files: `BillDataCleanRule.java`, `BillDataCleanRuleController.java`

### Frontend Modules

#### Bill Management (`BillList.vue`)
- Main bill listing and management
- Filtering, sorting, pagination
- Batch operations
- Pinyin search for categories

#### Import Management (`BillImport.vue`, `BillImportDetail.vue`)
- File upload and parsing
- Import preview and confirmation
- Import history and details

#### Category Management (`Category.vue`)
- Category CRUD operations
- Hierarchical category structure
- Category statistics

#### Statistics & Analytics
- `Dashboard.vue`: Overview and summary
- `CategoryStatistics.vue`: Category-based analysis
- `DailyExpense.vue`: Daily expense tracking
- `MonthlyExpense.vue`: Monthly summaries
- `CumulativeExpense.vue`: Cumulative trends

#### Configuration
- `PaymentChannel.vue`: Payment method management
- `TagManagement.vue`: Tag CRUD operations
- `DataCleanRule.vue`: Data cleaning rule configuration

## Documentation Location

- **PRD Documents**: `/docs/*.md` (Chinese)
- **Coding Standards**: `/.amazonq/rules/*.md`
- **Database Schema**: `/personal-ledger-backend/db/tables.md`
- **API Documentation**: Swagger UI at runtime
- **Frontend README**: `/personal-ledger-frontend/README.md` (if exists)
