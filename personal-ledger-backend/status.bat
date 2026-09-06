@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
echo ======================================
echo 个人账本后端服务状态检查
echo ======================================

echo.
echo [1/3] 检查 JAR 文件...
if exist target\personal-ledger-1.0.0.jar (
    echo ✅ JAR 文件存在: target\personal-ledger-1.0.0.jar
    for %%A in (target\personal-ledger-1.0.0.jar) do (
        echo    文件大小: %%~zA 字节
        echo    修改时间: %%~tA
    )
) else (
    echo ❌ JAR 文件不存在
)

echo.
echo [2/3] 检查端口占用...
netstat -ano | findstr :8081 | findstr LISTENING > nul
if %ERRORLEVEL% EQU 0 (
    echo ✅ 端口 8081 正在监听
    echo.
    echo 进程信息:
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING') do (
        set PID=%%a
        tasklist /FI "PID eq %%a" | findstr %%a
        echo    PID: %%a
    )
) else (
    echo ❌ 端口 8081 未被占用（服务未运行）
)

echo.
echo [3/3] 检查服务可访问性...
curl -s -o nul -w "HTTP Status: %%{http_code}" http://localhost:8081/actuator/health 2>nul
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ 服务可访问
    echo.
    echo 服务地址:
    echo   - 主应用:    http://localhost:8081
    echo   - 健康检查:  http://localhost:8081/actuator/health
    echo   - Swagger:   http://localhost:8081/swagger-ui.html
    echo   - API Docs:  http://localhost:8081/v3/api-docs
) else (
    echo.
    echo ⚠️  无法访问服务 (需要 curl 命令)
    echo.
    echo 如果服务正在运行，请手动访问:
    echo   http://localhost:8081
)

echo.
echo [日志文件]
if exist logs (
    echo 最近的日志文件:
    dir /b /o-d logs\app-*.log 2>nul | findstr /n "^" | findstr "^1:"
    if %ERRORLEVEL% NEQ 0 (
        echo   无日志文件
    )
) else (
    echo   logs 目录不存在
)

echo.
echo ======================================
echo 状态检查完成
echo ======================================
pause
