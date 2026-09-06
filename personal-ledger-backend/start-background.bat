@echo off
chcp 65001 >nul
echo ======================================
echo 启动个人账本后端服务 (后台模式)
echo ======================================

cd /d "%~dp0"

echo.
echo 检查 JAR 文件...

if not exist target\personal-ledger-1.0.0.jar (
    echo ❌ 未找到 JAR 文件: target\personal-ledger-1.0.0.jar
    echo.
    echo 请先运行 build.bat 进行打包
    pause
    exit /b 1
)

echo ✅ 找到 JAR 文件
echo.
echo 后台启动服务...

REM 创建日志目录
if not exist logs mkdir logs

REM 生成日志文件名（使用简单的时间戳）
set LOG_FILE=logs\app.log

REM 使用 javaw 后台运行
start "Personal Ledger Backend" /B javaw -jar target\personal-ledger-1.0.0.jar > %LOG_FILE% 2>&1

echo.
echo ✅ 服务已在后台启动！
echo.
echo 访问地址:
echo   - 主应用:    http://localhost:8081
echo   - Swagger:   http://localhost:8081/swagger-ui.html
echo   - API Docs:  http://localhost:8081/v3/api-docs
echo.
echo 日志文件: %LOG_FILE%
echo.
echo 提示: 使用 stop.bat 可停止服务
echo.

echo 等待服务启动...
timeout /t 5 /nobreak > nul

netstat -ano | findstr :8081 > nul
if %ERRORLEVEL% EQU 0 (
    echo ✅ 服务启动成功，端口 8081 正在监听
) else (
    echo ⚠️  服务可能启动失败，请查看日志文件
)

echo.
pause
