package com.ledger.controller;

import com.ledger.common.Response;
import com.ledger.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Tag(name = "系统测试")
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Response<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("message", "个人账本系统运行正常");
        data.put("timestamp", LocalDateTime.now());
        return Response.success(data);
    }

    @Operation(summary = "测试成功响应")
    @GetMapping("/success")
    public Response<String> testSuccess() {
        return Response.success("操作成功");
    }

    @Operation(summary = "测试业务异常")
    @GetMapping("/business-error")
    public Response<Void> testBusinessError() {
        throw new BusinessException("这是一个业务异常示例");
    }

    @Operation(summary = "测试系统异常")
    @GetMapping("/system-error")
    public Response<Void> testSystemError() {
        throw new RuntimeException("这是一个系统异常示例");
    }

    @Operation(summary = "测试参数校验")
    @GetMapping("/validate")
    public Response<String> testValidate(@NotBlank(message = "参数不能为空") @RequestParam String param) {
        return Response.success("参数校验成功: " + param);
    }

    @Operation(summary = "测试自定义错误码")
    @GetMapping("/custom-error")
    public Response<Void> testCustomError() {
        throw new BusinessException(404, "资源不存在");
    }
}
