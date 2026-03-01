# 接口开发规范

## 一、基础规范

### 1.1 URL 规范

- 【强制】使用 RESTful 风格设计接口
- 【强制】URL 使用小写字母，多个单词用中划线 `-` 分隔
- 【强制】资源名称使用复数形式
- 【强制】禁止使用路径参数，所有参数通过 Query 或 Body 传递
- 【推荐】URL 层级不超过 3 层

```
正例：
GET    /ledger?id=1     # 查询详情
POST   /ledger          # 创建
POST   /ledger/update   # 更新
POST   /ledger/delete   # 删除

反例：
GET    /ledger/{id}     # 禁止使用路径参数
DELETE /ledger/{id}     # 禁止使用路径参数
/getLedgerList
/ledger_list
/Ledger
```

### 1.2 HTTP 方法规范

- 【强制】GET：查询操作，仅用于单个简单参数查询，使用 Query 参数
- 【强制】POST：创建操作、更新操作、删除操作、复杂查询操作（多参数查询、分页查询等）
- 【禁止】不允许使用 PUT、PATCH、DELETE 方法

### 1.3 参数命名规范

- 【强制】请求参数使用驼峰命名法（camelCase）
- 【强制】响应字段使用驼峰命名法（camelCase）
- 【强制】布尔类型字段不使用 `is` 前缀

```java
// 正例
{
  "userId": 1,
  "userName": "张三",
  "deleted": false
}

// 反例
{
  "user_id": 1,
  "UserName": "张三",
  "isDeleted": false
}
```

---

## 二、请求规范

### 2.1 请求方式

- 【强制】GET 请求仅用于单个简单参数查询，使用 Query 参数
- 【强制】POST 请求用于创建、更新、删除和复杂查询操作，使用 JSON Body
- 【禁止】不允许使用路径参数（如 /{id}）

```java
// 正例：GET 用于简单查询
@GetMapping
public Response<UserVO> getById(@RequestParam Long id) { ... }

// 正例：POST 用于创建
@PostMapping
public Response<Long> create(@RequestBody UserDTO dto) { ... }

// 正例：POST 用于更新
@PostMapping("/update")
public Response<Void> update(@RequestBody UserDTO dto) { ... }

// 正例：POST 用于删除
@PostMapping("/delete")
public Response<Void> delete(@RequestBody IdDTO dto) { ... }

// 正例：POST 用于复杂查询
@PostMapping("/list")
public Response<List<UserVO>> list(@RequestBody UserQueryDTO dto) { ... }

@PostMapping("/page")
public Response<Page<UserVO>> page(@RequestBody UserQueryDTO dto) { ... }
```

### 2.2 请求头

- 【推荐】Content-Type: application/json
- 【推荐】Accept: application/json

### 2.3 分页参数

- 【强制】分页查询使用 POST 请求
- 【强制】分页参数统一命名
  - `current`：当前页码，从 1 开始
  - `size`：每页条数，默认 10

```java
// 分页查询示例
@PostMapping("/page")
public Response<Page<UserVO>> page(@RequestBody UserQueryDTO dto) {
    // dto 中包含 current、size 及其他查询条件
}
```

### 2.4 时间参数

- 【强制】时间参数使用字符串格式
- 【强制】日期时间格式：`yyyy-MM-dd HH:mm:ss`
- 【强制】日期格式：`yyyy-MM-dd`
- 【强制】时间格式：`HH:mm:ss`

---

## 三、响应规范

### 3.1 统一响应格式

- 【强制】所有接口必须返回 `Response<T>` 统一格式
- 【强制】禁止直接返回业务对象或基本类型

```java
public class Response<T> {
    private Integer code;        // 响应码
    private String message;      // 响应消息
    private T data;             // 响应数据
    private LocalDateTime timestamp; // 响应时间戳
}
```

### 3.2 成功响应

```java
// 无返回数据
@PostMapping("/delete")
public Response<Void> delete(@RequestBody IdDTO dto) {
    service.deleteById(dto.getId());
    return Response.success();
}

// 有返回数据
@GetMapping
public Response<UserVO> getById(@RequestParam Long id) {
    UserVO vo = service.getById(id);
    return Response.success(vo);
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {...},
  "timestamp": "2025-01-13 10:30:00"
}
```

### 3.3 失败响应

```java
// 使用默认错误码 500
if (param == null) {
    return Response.error("参数不能为空");
}

// 使用自定义错误码
if (user == null) {
    return Response.error(404, "用户不存在");
}

// 抛出业务异常（推荐）
if (user == null) {
    throw new BusinessException(404, "用户不存在");
}
```

**响应示例：**
```json
{
  "code": 404,
  "message": "用户不存在",
  "data": null,
  "timestamp": "2025-01-13 10:30:00"
}
```

### 3.4 分页响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],      // 数据列表
    "total": 100,         // 总记录数
    "current": 1,         // 当前页码
    "size": 10,           // 每页条数
    "pages": 10           // 总页数
  },
  "timestamp": "2025-01-13 10:30:00"
}
```

---

## 四、状态码规范

### 4.1 HTTP 状态码

- 【强制】所有接口统一返回 HTTP 200
- 【强制】通过 Response.code 区分业务状态

### 4.2 业务状态码

- 【推荐】200：成功
- 【推荐】400：客户端参数错误
- 【推荐】401：未认证
- 【推荐】403：无权限
- 【推荐】404：资源不存在
- 【推荐】500：服务器内部错误

---

## 五、数据类型规范

### 5.1 时间类型

- 【强制】使用 `LocalDateTime` 表示日期时间
- 【强制】使用 `LocalDate` 表示日期
- 【强制】使用 `LocalTime` 表示时间
- 【强制】响应时自动格式化为字符串

### 5.2 金额类型

- 【强制】使用 `BigDecimal` 类型
- 【强制】保留两位小数
- 【禁止】使用 `float` 或 `double`

```java
// 正例
private BigDecimal amount;

// 反例
private double amount;
```

### 5.3 布尔类型

- 【强制】使用 `Boolean` 或 `boolean`
- 【强制】字段名不使用 `is` 前缀
- 【推荐】使用 0/1 存储到数据库

```java
// 正例
private Boolean deleted;

// 反例
private Boolean isDeleted;
```

---

## 六、参数校验规范

### 6.1 使用注解校验

- 【强制】Controller 层使用 `@Valid` 或 `@Validated` 注解
- 【强制】DTO 类使用 JSR-303 校验注解

```java
@PostMapping
public Response<Void> create(@Valid @RequestBody UserDTO dto) {
    service.create(dto);
    return Response.success();
}
```

### 6.2 常用校验注解

```java
public class UserDTO {
    @NotNull(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度为2-20个字符")
    private String username;
    
    @NotNull(message = "年龄不能为空")
    @Min(value = 1, message = "年龄必须大于0")
    @Max(value = 150, message = "年龄必须小于150")
    private Integer age;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;
}
```

---

## 七、异常处理规范

### 7.1 业务异常

- 【强制】使用 `BusinessException` 抛出业务异常
- 【强制】异常会被全局异常处理器捕获并返回统一格式

```java
// 抛出业务异常
if (user == null) {
    throw new BusinessException("用户不存在");
}

// 自定义错误码
if (balance < amount) {
    throw new BusinessException(400, "余额不足");
}
```

### 7.2 全局异常处理

- 【强制】所有异常由 `GlobalExceptionHandler` 统一处理
- 【强制】不允许在 Controller 中直接抛出未捕获的异常

---

## 八、接口文档规范

### 8.1 Swagger 注解

- 【强制】Controller 类使用 `@Tag` 注解
- 【强制】接口方法使用 `@Operation` 注解
- 【推荐】参数使用 `@Parameter` 注解

```java
@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {
    
    @Operation(summary = "创建用户")
    @PostMapping
    public Response<Long> create(@RequestBody UserDTO dto) {
        Long id = userService.create(dto);
        return Response.success(id);
    }
    
    @Operation(summary = "查询用户详情")
    @GetMapping
    public Response<UserVO> getById(
            @Parameter(description = "用户ID") @RequestParam Long id) {
        UserVO vo = userService.getById(id);
        return Response.success(vo);
    }
}
```

---

## 九、DTO/VO 规范

### 9.1 命名规范

- 【强制】请求对象使用 `DTO` 后缀（Data Transfer Object）
- 【强制】响应对象使用 `VO` 后缀（View Object）
- 【强制】数据库实体使用 `Entity` 或不加后缀

### 9.2 使用场景

```
Controller  <--DTO-->  Service  <--Entity-->  Mapper
Controller  <--VO---   Service
```

- DTO：接收前端请求参数
- VO：返回给前端的数据
- Entity：数据库实体对象

### 9.3 对象转换

- 【推荐】使用工具类进行对象转换（如 BeanUtils）
- 【禁止】直接返回 Entity 对象给前端

---

## 十、注意事项

1. 【强制】Controller 层只做参数校验和转发，不写业务逻辑
2. 【强制】所有接口必须添加 Swagger 注解
3. 【强制】删除操作使用逻辑删除，不物理删除数据
4. 【推荐】接口支持幂等性设计
5. 【推荐】敏感信息不在日志中输出
6. 【推荐】接口响应时间控制在 1 秒以内
