@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
echo ======================================
echo 端口 8081 占用情况检查
echo ======================================

echo.
echo 检查端口 8081...

REM 查找监听 8081 端口的进程
netstat -ano | findstr :8081 | findstr LISTENING > nul
if %ERRORLEVEL% NEQ 0 (
    echo ✅ 端口 8081 未被占用，可以正常启动服务
    echo.
    pause
    exit /b 0
)

echo ⚠️  端口 8081 已被占用
echo.

REM 获取占用端口的进程信息
echo 占用端口的进程信息:
echo ----------------------------------------
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING') do (
    set PID=%%a
    echo PID: !PID!
    echo.
    tasklist /FI "PID eq !PID!" /FO LIST
    echo.
    
    REM 判断是否是 Java 进程
    tasklist /FI "PID eq !PID!" | findstr /I "java" > nul
    if !ERRORLEVEL! EQU 0 (
        echo ✅ 这是一个 Java 进程，很可能是后端服务
        set IS_JAVA=1
    ) else (
        echo ⚠️  这不是 Java 进程，可能是其他程序
        set IS_JAVA=0
    )
)
echo ----------------------------------------

echo.
echo 操作选项:
echo   1. 停止该进程并退出
echo   2. 仅查看，不做操作
echo   3. 取消

set /p choice=请选择 (1/2/3): 

if "%choice%"=="1" (
    echo.
    echo 正在停止进程 PID: %PID%...
    taskkill /PID %PID% /F
    if !ERRORLEVEL! EQU 0 (
        echo ✅ 进程已停止
        echo.
        echo 现在可以运行 start.bat 或 start-background.bat 启动服务
    ) else (
        echo ❌ 停止失败，可能需要管理员权限
    )
) else if "%choice%"=="2" (
    echo.
    echo 未做任何操作
) else (
    echo.
    echo 已取消
)

echo.
pause
