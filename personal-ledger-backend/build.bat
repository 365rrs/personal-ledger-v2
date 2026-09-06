@echo off
chcp 65001 >nul
echo ======================================
echo 个人账本后端项目打包脚本
echo ======================================

cd /d "%~dp0"

echo.
echo [1/4] 清理旧的构建文件...
call mvn clean

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ 清理失败！请检查错误信息
    pause
    exit /b 1
)

echo.
echo [2/4] 编译项目...
call mvn compile

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ 编译失败！请检查错误信息
    pause
    exit /b 1
)

echo.
echo [3/4] 打包 JAR 文件（跳过测试）...
call mvn package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ 打包失败！请检查错误信息
    pause
    exit /b 1
)

echo.
echo [4/4] 检查打包结果...
if exist target\personal-ledger-1.0.0.jar (
    echo ✅ 打包成功！
    echo.
    echo JAR 包位置: %~dp0target\personal-ledger-1.0.0.jar
    echo.
    echo 文件信息:
    dir target\personal-ledger-1.0.0.jar | findstr "personal-ledger"
    echo.
    echo 运行命令:
    echo java -jar target\personal-ledger-1.0.0.jar
) else (
    echo ❌ 未找到 JAR 文件，打包可能失败
    pause
    exit /b 1
)

echo.
echo ======================================
echo 打包完成
echo ======================================
pause
