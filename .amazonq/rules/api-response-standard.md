# API 响应规范

## 统一响应格式

- 【强制】所有 Web 接口必须使用 `com.ledger.common.Response` 类进行统一封装
- 【强制】禁止直接返回业务对象或基本类型

## Response 结构

```java
public class Response<T> {
    private Integer code;        // 响应码
    private String message;      // 响应消息
    private T data;             // 响应数据
    private LocalDateTime timestamp; // 响应时间戳
}
```

## 使用规范

### 成功响应

```java
// 无返回数据
@GetMapping("/delete/{id}")
public Response<Void> delete(@PathVariable Long id) {
    ledgerService.deleteById(id);
    return Response.success();
}

// 有返回数据
@GetMapping("/{id}")
public Response<LedgerVO> getById(@PathVariable Long id) {
    LedgerVO vo = ledgerService.getById(id);
    return Response.success(vo);
}
```

### 失败响应

```java
// 使用默认错误码 500
@PostMapping
public Response<Void> create(@RequestBody LedgerDTO dto) {
    if (dto.getAmount() == null) {
        return Response.error("金额不能为空");
    }
    ledgerService.create(dto);
    return Response.success();
}

// 自定义错误码
@PostMapping
public Response<Void> create(@RequestBody LedgerDTO dto) {
    if (dto.getAmount() == null) {
        return Response.error(400, "参数错误：金额不能为空");
    }
    ledgerService.create(dto);
    return Response.success();
}
```

## 响应码规范

- 【推荐】200：成功
- 【推荐】400：客户端参数错误
- 【推荐】401：未认证
- 【推荐】403：无权限
- 【推荐】404：资源不存在
- 【推荐】500：服务器内部错误

## 响应示例

### 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "type": "INCOME",
    "amount": 1000.00
  },
  "timestamp": "2025-01-13T10:30:00"
}
```

### 失败响应

```json
{
  "code": 400,
  "message": "参数错误：金额不能为空",
  "data": null,
  "timestamp": "2025-01-13T10:30:00"
}
```

## 注意事项

- 【强制】Controller 层所有公开接口必须返回 Response 类型
- 【强制】不允许在 Controller 中直接抛出未捕获的异常
- 【推荐】使用全局异常处理器统一处理异常并返回 Response 格式
