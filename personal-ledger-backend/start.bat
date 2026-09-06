@echo off
chcp 65001 >nul
echo ======================================
echo 启动个人账本后端服务 (控制台模式)
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
echo ======================================
echo 服务启动中...
echo ======================================
echo.
echo 访问地址:
echo   - 主应用:    http://localhost:8081
echo   - Swagger:   http://localhost:8081/swagger-ui.html
echo   - API Docs:  http://localhost:8081/v3/api-docs
echo.
echo 提示: 按 Ctrl+C 可停止服务
echo ======================================
echo.

java -Dfile.encoding=UTF-8 -jar target\personal-ledger-1.0.0.jar

echo.
echo 服务已停止
pause
