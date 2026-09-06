@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
echo ======================================
echo 停止个人账本后端服务
echo ======================================

echo.
echo 查找运行在端口 8081 的进程...

REM 查找监听 8081 端口的进程
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING') do (
    set PID=%%a
    goto :found
)

echo ❌ 未找到运行在端口 8081 的服务
echo.
pause
exit /b 0

:found
echo ✅ 找到进程 PID: %PID%
echo.

REM 获取进程详细信息
echo 进程信息:
tasklist /FI "PID eq %PID%" | findstr %PID%
echo.

echo 是否停止该进程？(Y/N)
set /p confirm=
if /i not "%confirm%"=="Y" (
    echo 已取消操作
    pause
    exit /b 0
)

echo.
echo 正在停止服务...
taskkill /PID %PID% /F

if %ERRORLEVEL% EQU 0 (
    echo ✅ 服务已成功停止
) else (
    echo ❌ 停止服务失败
)

echo.
pause
