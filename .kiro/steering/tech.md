# Technology Stack

## Backend

### Core Framework
- **Spring Boot**: 2.6.13
- **JDK**: 1.8
- **Build Tool**: Maven

### Database & ORM
- **Database**: MySQL 8.0
- **ORM**: MyBatis Plus 3.5.2
- **Connection Pool**: HikariCP (Spring Boot default)

### Key Libraries
- **Lombok**: Code generation for boilerplate reduction
- **MapStruct**: 1.5.5.Final - Object mapping between DTOs/Entities/VOs
- **EasyExcel**: 3.3.2 - Excel file processing (导入导出)
- **Apache Commons CSV**: 1.10.0 - CSV file processing (账单导入)
- **Apache Commons Lang3**: Utility functions (字符串、日期处理等)
- **Fastjson**: 1.2.83 - JSON processing
- **Spring Validation**: JSR-303 bean validation (@Valid, @Validated)

### API Documentation
- **Swagger 2**: 2.9.2 (springfox-swagger2, springfox-swagger-ui)
  - 访问地址: http://localhost:8081/swagger-ui.html
- **SpringDoc OpenAPI**: 1.6.15
  - 访问地址: http://localhost:8081/v3/api-docs
  - 提供标准 OpenAPI 3.0 规范文档

### Monitoring
- **Spring Boot Actuator**: Health checks and metrics

## Frontend

### Core Framework
- **Vue**: 3.3.4
- **Build Tool**: Vite 4.4.9
- **Node.js**: Required for development

### UI Framework
- **Element Plus**: 2.3.14 - Vue 3 UI component library

### Key Libraries
- **Vue Router**: 4.2.4 - Client-side routing (页面路由)
- **Axios**: 1.5.0 - HTTP client for API requests (后端通信)
- **ECharts**: 5.4.3 - Data visualization charts (统计图表)
- **pinyin-pro**: 3.19.6 - Pinyin conversion for search (拼音检索)
- **SortableJS**: 1.15.7 - Drag and drop functionality (拖拽排序)

### Development Tools
- **@vitejs/plugin-vue**: 4.3.4 - Vite plugin for Vue 3

## Maven Repository
- Uses Aliyun Maven mirror for faster dependency downloads in China
- 配置在 `pom.xml` 的 `<repositories>` 和 `<pluginRepositories>` 中
- Mirror URL: https://maven.aliyun.com/repository/public

## Common Commands

### Backend

#### Build & Run
```bash
# 进入后端目录
cd personal-ledger-backend

# 清理并编译
mvn clean compile

# 运行测试
mvn test

# 打包应用
mvn clean package

# 跳过测试打包（更快）
mvn clean package -DskipTests

# 运行应用
mvn spring-boot:run

# 或者运行打包后的 jar
java -jar target/personal-ledger-1.0.0.jar
```

#### Database Setup
```bash
# 从项目根目录初始化数据库
mysql -u root -p < personal-ledger-backend/db/schema.sql

# 加载初始数据
mysql -u root -p personal_ledger_v2 < personal-ledger-backend/db/data.sql

# 加载支付渠道数据
mysql -u root -p personal_ledger_v2 < personal-ledger-backend/db/payment_channel_data.sql

# 加载数据清洗规则
mysql -u root -p personal_ledger_v2 < personal-ledger-backend/db/init_data_clean_rules.sql

# 执行数据库更新脚本（如需要）
mysql -u root -p personal_ledger_v2 < personal-ledger-backend/db/update_bill_schema_20260308.sql
```

#### Troubleshooting
```bash
# 查看依赖树
mvn dependency:tree

# 强制更新依赖
mvn clean install -U

# 查看有效 POM
mvn help:effective-pom
```

### Frontend

#### Development
```bash
# 进入前端目录
cd personal-ledger-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

#### Network Access
- Development server runs on port 3000
- Configured with `host: '0.0.0.0'` for LAN access
- 本地访问: http://localhost:3000
- 局域网访问: http://<your-ip>:3000
- 需要确保防火墙允许 3000 端口访问

#### Troubleshooting
```bash
# 清理 node_modules 并重新安装
rm -rf node_modules package-lock.json
npm install

# 清理 Vite 缓存
rm -rf node_modules/.vite

# 查看依赖版本
npm list
npm list <package-name>

# 更新依赖
npm update
```

## Configuration

### Backend
- **主配置文件**: `src/main/resources/application.yml`
- **数据库配置**: 需要根据本地环境修改连接信息
  ```yaml
  spring:
    datasource:
      url: jdbc:mysql://localhost:3306/personal_ledger_v2
      username: root
      password: your_password
  ```
- **应用端口**: 8081 (可在 `application.yml` 中修改)
- **日志配置**: Spring Boot 默认日志配置

### Frontend
- **Vite 配置**: `vite.config.js`
- **API 代理配置**: 
  ```javascript
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
  ```
- **开发端口**: 3000
- **生产构建输出**: `dist/` 目录

## Access Points

### Backend
- Application: http://localhost:8081
- Swagger UI: http://localhost:8081/swagger-ui.html
- API Docs: http://localhost:8081/v3/api-docs

### Frontend
- Development: http://localhost:3000
- LAN Access: http://<your-ip>:3000

## Development Tools

### Backend
- **IDE**: IntelliJ IDEA recommended
- **Lombok Plugin**: Required for IDE support (Lombok 注解支持)
- **MapStruct Processor**: Configured in Maven compiler plugin (自动生成转换器实现)
- **MySQL Client**: MySQL Workbench, DataGrip, or command line
- **API Testing**: Postman, Swagger UI, or curl

### Frontend
- **IDE**: VS Code recommended
- **Vue DevTools**: Browser extension for debugging (Vue 3 组件调试)
- **Volar**: VS Code extension for Vue 3 support (必装，替代 Vetur)
- **其他推荐插件**:
  - ESLint (代码检查)
  - Prettier (代码格式化)
  - Auto Import (自动导入)

## Environment Requirements

### Backend
- **JDK**: 1.8 或更高版本
- **Maven**: 3.6+ 推荐
- **MySQL**: 8.0 (必须，不兼容旧版本)
- **操作系统**: Windows / Linux / macOS

### Frontend
- **Node.js**: 16+ 推荐 (支持 ES modules)
- **npm**: 8+ (随 Node.js 安装)
- **浏览器**: Chrome / Edge / Firefox 最新版本

## Performance Considerations

### Backend
- **数据库连接池**: HikariCP (Spring Boot 默认，性能优秀)
- **分页查询**: MyBatis Plus 内置分页插件，避免全表查询
- **索引使用**: 
  - 交易日期 (`transaction_date`)
  - 分类 ID (`category_id`)
  - 支付渠道 (`payment_channel_id`)
  - 导入记录 (`import_record_id`)
  - 数据指纹 (`data_hash`)
- **批量操作**: 使用 MyBatis Plus 的 `saveBatch`, `updateBatchById` 等方法
- **缓存**: 目前未使用，可考虑引入 Redis 缓存分类、标签等元数据

### Frontend
- **懒加载**: 路由级别的代码分割
- **虚拟滚动**: 大数据量列表可考虑使用 Element Plus 的虚拟列表
- **防抖节流**: 搜索、统计等频繁操作应使用防抖
- **图表优化**: ECharts 数据量大时启用 dataZoom 和采样

## Security Notes

### Backend
- **SQL 注入防护**: MyBatis Plus 参数化查询
- **XSS 防护**: 前端输入验证 + 后端输出转义
- **CORS 配置**: `CorsConfig.java` 中配置允许的源
- **文件上传**: 
  - 文件类型验证（仅允许 .csv, .xlsx, .xls）
  - 文件大小限制
  - 文件内容验证
- **敏感信息**: 数据库密码不应提交到版本控制

### Frontend
- **API 请求**: 统一通过 Axios 拦截器处理
- **错误处理**: 全局错误提示
- **数据验证**: Element Plus Form 表单验证
- **路由守卫**: 可根据需要添加认证拦截
