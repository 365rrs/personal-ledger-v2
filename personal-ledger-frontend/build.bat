@echo off
chcp 65001 >nul
echo ======================================
echo 开始构建前端项目
echo ======================================

cd /d "%~dp0"

echo.
echo [1/3] 清理旧的构建文件...
if exist dist (
    rmdir /s /q dist
    echo 已删除旧的 dist 目录
)

echo.
echo [2/3] 执行 npm run build...
call npm run build

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ 构建失败！请检查错误信息
    pause
    exit /b 1
)

echo.
echo [3/3] 检查构建结果...
if exist dist (
    echo ✅ 构建成功！
    echo.
    echo 构建产物位置: %~dp0dist
    echo.
    echo 请在 nginx 配置中指向该目录，例如：
    echo root E:/17_github/personal-ledger-v2/personal-ledger-frontend/dist;
) else (
    echo ❌ 未找到 dist 目录，构建可能失败
    pause
    exit /b 1
)

echo.
echo ======================================
echo 构建完成
echo ======================================
pause
