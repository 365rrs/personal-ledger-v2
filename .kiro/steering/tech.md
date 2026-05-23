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
- **MapStruct**: 1.5.5.Final - Object mapping between DTOs/Entities
- **EasyExcel**: 3.3.2 - Excel file processing
- **Apache Commons CSV**: 1.10.0 - CSV file processing
- **Apache Commons Lang3**: Utility functions
- **Fastjson**: 1.2.83 - JSON processing
- **Spring Validation**: JSR-303 bean validation

### API Documentation
- **Swagger 2**: 2.9.2 (springfox-swagger2, springfox-swagger-ui)
- **SpringDoc OpenAPI**: 1.6.15

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
- **Vue Router**: 4.2.4 - Client-side routing
- **Axios**: 1.5.0 - HTTP client for API requests
- **ECharts**: 5.4.3 - Data visualization charts
- **pinyin-pro**: 3.19.6 - Pinyin conversion for search
- **SortableJS**: 1.15.7 - Drag and drop functionality

### Development Tools
- **@vitejs/plugin-vue**: 4.3.4 - Vite plugin for Vue 3

## Maven Repository
- Uses Aliyun Maven mirror for faster dependency downloads in China

## Common Commands

### Backend

#### Build & Run
```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package application
mvn clean package

# Run application
mvn spring-boot:run

# Skip tests during build
mvn clean package -DskipTests
```

#### Database Setup
```bash
# Initialize database (from project root)
mysql -u root -p < personal-ledger-backend/db/schema.sql

# Load initial data
mysql -u root -p personal_ledger_v2 < personal-ledger-backend/db/data.sql

# Load payment channel data
mysql -u root -p personal_ledger_v2 < personal-ledger-backend/db/payment_channel_data.sql

# Load data cleaning rules
mysql -u root -p personal_ledger_v2 < personal-ledger-backend/db/init_data_clean_rules.sql
```

### Frontend

#### Development
```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

#### Network Access
- Development server runs on port 3000
- Configured with `host: '0.0.0.0'` for LAN access
- Access from other devices: `http://<your-ip>:3000`

## Configuration

### Backend
- Main config: `src/main/resources/application.yml`
- Database connection settings must be updated for local environment
- Default port: 8081

### Frontend
- Vite config: `vite.config.js`
- API proxy configured to `http://localhost:8081`
- Development port: 3000

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
- **Lombok Plugin**: Required for IDE support
- **MapStruct Processor**: Configured in Maven compiler plugin

### Frontend
- **IDE**: VS Code recommended
- **Vue DevTools**: Browser extension for debugging
- **Volar**: VS Code extension for Vue 3 support
