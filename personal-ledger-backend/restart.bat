@echo off
chcp 65001 >nul
echo ======================================
echo 重启个人账本后端服务
echo ======================================

cd /d "%~dp0"

echo.
echo [1/2] 停止现有服务...

REM 查找并停止现有服务
for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr :8081 ^| findstr LISTENING') do (
    echo 找到进程 PID: %%a
    taskkill /PID %%a /F >nul 2>&1
    echo 已停止服务
    timeout /t 2 /nobreak > nul
    goto :start
)

echo 未找到运行中的服务

:start
echo.
echo [2/2] 启动新服务...

if not exist target\personal-ledger-1.0.0.jar (
    echo ❌ 未找到 JAR 文件
    pause
    exit /b 1
)

REM 创建日志目录
if not exist logs mkdir logs

REM 后台启动服务
start "Personal Ledger Backend" /B javaw -jar target\personal-ledger-1.0.0.jar > logs\app.log 2>&1

echo ✅ 服务正在启动...
echo.
echo 等待服务启动...
timeout /t 5 /nobreak > nul

netstat -ano | findstr :8081 > nul
if %ERRORLEVEL% EQU 0 (
    echo ✅ 服务重启成功！
    echo.
    echo 访问地址: http://localhost:8081
) else (
    echo ⚠️  服务可能启动失败，请查看日志: logs\app.log
)

echo.
pause
