# Personal Ledger Backend

个人账本系统后端服务

## 技术栈

- Spring Boot 2.6.13
- JDK 8
- MyBatis Plus 3.5.2
- MySQL 8.0
- Lombok
- SpringDoc OpenAPI (Swagger UI)

## 项目结构

```
personal-ledger-backend/
├── db/                         # 数据库脚本
│   └── schema.sql             # 数据库表结构
├── src/
│   ├── main/
│   │   ├── java/com/ledger/
│   │   │   ├── controller/    # 控制层
│   │   │   ├── service/       # 业务逻辑层
│   │   │   ├── mapper/        # 数据访问层
│   │   │   ├── entity/        # 实体类
│   │   │   ├── dto/           # 数据传输对象
│   │   │   ├── vo/            # 视图对象
│   │   │   ├── common/        # 公共类
│   │   │   ├── config/        # 配置类
│   │   │   └── LedgerApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── banner.txt
│   └── test/
└── pom.xml
```

## 快速开始

### 1. 数据库准备

```bash
# 创建数据库并执行初始化脚本
mysql -u root -p < db/schema.sql
```

### 2. 配置修改

修改 `src/main/resources/application.yml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/personal_ledger_v2
    username: root
    password: your_password
```

### 3. 启动项目

```bash
mvn spring-boot:run
```

### 4. 访问地址

- 本地访问：http://localhost:8080
- Swagger UI：http://localhost:8080/swagger-ui.html
- API 文档：http://localhost:8080/v3/api-docs

## 开发规范

### 接口开发规范

详细规范请查看：[接口开发规范](../.amazonq/rules/api-development-standard.md)

**核心要点：**

1. **URL 规范**
   - 禁止使用路径参数，所有参数通过 Query 或 Body 传递
   - 示例：`GET /ledger?id=1`（正确）、`GET /ledger/{id}`（错误）

2. **HTTP 方法**
   - GET：仅用于单个简单参数查询
   - POST：用于创建、更新、删除和复杂查询
   - 禁止使用 PUT、PATCH、DELETE 方法

3. **统一响应格式**
   - 所有接口必须返回 `Response<T>` 格式
   - 禁止直接返回业务对象或基本类型

### API 响应规范

【强制】所有 Web 接口必须使用 `com.ledger.common.Response` 类进行统一封装。

#### Response 结构

```java
public class Response<T> {
    private Integer code;           // 响应码
    private String message;         // 响应消息
    private T data;                // 响应数据
    private LocalDateTime timestamp; // 响应时间戳
}
```

#### 使用示例

```java
// 成功响应 - 无数据
@DeleteMapping("/{id}")
public Response<Void> delete(@PathVariable Long id) {
    ledgerService.deleteById(id);
    return Response.success();
}

// 成功响应 - 有数据
@GetMapping("/{id}")
public Response<LedgerVO> getById(@PathVariable Long id) {
    LedgerVO vo = ledgerService.getById(id);
    return Response.success(vo);
}

// 失败响应
@PostMapping
public Response<Void> create(@RequestBody LedgerDTO dto) {
    if (dto.getAmount() == null) {
        return Response.error("金额不能为空");
    }
    ledgerService.create(dto);
    return Response.success();
}
```

#### 响应示例

成功响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {...},
  "timestamp": "2025-01-13T10:30:00"
}
```

失败响应：
```json
{
  "code": 500,
  "message": "操作失败",
  "data": null,
  "timestamp": "2025-01-13T10:30:00"
}
```

### 数据库规范

- 所有 SQL 文件统一存放在 `db/` 目录下
- 建表语句必须包含 `IF NOT EXISTS` 判断
- 表必备字段：`id`、`create_time`、`update_time`

### 代码规范

- 遵循阿里巴巴 Java 开发手册（P3C）
- 使用 Lombok 简化代码
- Service 层方法命名：get/list/count/save/update/remove
- 所有类和方法必须添加 Javadoc 注释

## 功能模块

- 账单管理：记录收入和支出
- 分类管理：自定义收支分类
- 统计分析：收支统计和图表展示

## 依赖管理

项目使用阿里云 Maven 仓库加速依赖下载。

## 许可证

MIT License
