@echo off
setlocal enabledelayedexpansion

:: DDD 模块生成工具 - 核心配置

:: 1. 自动获取当前脚本所在目录作为项目根目录 
set "PROJECT_ROOT=%~dp0"

:: 2. 基础包名（请确保与你的 Java 包结构一致） 
set "BASE_PKG=cn.dreamtof"

:: 3. Java 源码相对路径（一般无需修改） 
set "JAVA_SRC=src\main\java"

:: 4. 内置模块数组（用空格分隔多个模块名）
set "BUILTIN_MODULES=content device media portfolio social system"

::  环境初始化
chcp 65001 >nul
if "%1"=="keep_open" goto :start
cmd /k "%~f0" keep_open
exit /b

:start
echo ==================================================
echo [ 2026 模块级 DDD 结构生成工具 ]
echo ==================================================

:: 转换 BASE_PKG 为路径格式 (点变斜杠)
set "PKG_PATH=!BASE_PKG:.=\!"

:: 最终源码包根路径
set "java_base=!PROJECT_ROOT!!JAVA_SRC!\!PKG_PATH!"

:: 验证目录是否存在
if not exist "!PROJECT_ROOT!!JAVA_SRC!" (
    echo [错误] 找不到源码目录: !PROJECT_ROOT!!JAVA_SRC!
    echo       请确保脚本放在项目根目录（与 src 文件夹同级）。
    pause
    exit /b 1
)

echo [信息] 项目根目录: !PROJECT_ROOT!
echo [信息] 基础包路径: !BASE_PKG!
echo [信息] 目标位置: !java_base!
echo [信息] 内置模块: !BUILTIN_MODULES!
echo.

:: 定义 DDD 目录结构 
set "api_pkgs=api\controller api\request api\vo"
set "app_pkgs=application\service application\dto application\assembler application\listener"
set "dom_pkgs=domain\model\valueobject domain\model\entity domain\repository domain\service domain\event domain\exception domain\factory domain\enums"
set "inf_pkgs=infrastructure\persistence\mapper infrastructure\persistence\repository infrastructure\persistence\handler infrastructure\config infrastructure\external\feign infrastructure\util"

set "all_folders=%api_pkgs% %app_pkgs% %dom_pkgs% %inf_pkgs%"

:menu_loop
echo.
echo ==================================================
echo 请选择操作模式:
echo   1. 批量生成内置模块 (!BUILTIN_MODULES!)
echo   2. 单个模块生成 (手动输入)
echo   3. 自定义批量生成 (输入多个模块名，空格分隔)
echo   4. 删除所有 package-info.java 文件
echo   5. 退出
echo ==================================================
set "choice="
set /p choice="请输入选项 (1/2/3/4/5): "

if "!choice!"=="1" goto :batch_builtin
if "!choice!"=="2" goto :single_module
if "!choice!"=="3" goto :batch_custom
if "!choice!"=="4" goto :delete_infos
if "!choice!"=="5" goto :end
echo [错误] 无效选项，请重新输入。
goto :menu_loop

:batch_builtin
echo.
echo [批量模式] 将生成以下内置模块: !BUILTIN_MODULES!
echo.
for %%m in (!BUILTIN_MODULES!) do (
    call :create_module "%%m"
)
echo.
echo [成功] 所有内置模块创建完成！
goto :menu_loop

:batch_custom
echo.
set "custom_modules="
set /p custom_modules="请输入模块名称列表（空格分隔，例如: order pay notify）: "
if "!custom_modules!"=="" goto :batch_custom

echo.
echo [批量模式] 将生成以下模块: !custom_modules!
echo.
for %%m in (!custom_modules!) do (
    call :create_module "%%m"
)
echo.
echo [成功] 所有自定义模块创建完成！
goto :menu_loop

:single_module
:input_loop
set "module_name="
set /p module_name="请输入模块名称 (例如 order, auth, song): "
if "!module_name!"=="" goto :input_loop

call :create_module "!module_name!"
echo.
echo --------------------------------------------------
set "continue="
set /p continue="是否继续创建其他模块? (y/n): "
if /i "!continue!"=="y" goto :single_module
goto :menu_loop

:: 创建模块的子程序
:create_module
set "mod_name=%~1"
echo [执行] 正在为 !mod_name! 模块创建 DDD 架构...

:: 循环创建目录和 package-info.java 
for %%f in (%all_folders%) do (
    set "target=!java_base!\!mod_name!\%%f"

    if not exist "!target!" (
        mkdir "!target!" >nul 2>&1
    )

    if not exist "!target!\package-info.java" (
        set "sub_pkg=%%f"
        set "sub_pkg=!sub_pkg:\=.!"
        set "full_pkg=!BASE_PKG!.!mod_name!.!sub_pkg!"

        (
            echo /** 
            echo  * !mod_name! module - !sub_pkg! layer 
            echo  * @author dream 
            echo  */ 
            echo package !full_pkg!; 
        ) > "!target!\package-info.java"
    )
)

echo [成功] 模块 !mod_name! 结构创建完成。
exit /b

:delete_infos
echo.
echo [清理模式] 正在删除所有 package-info.java 文件...
echo.

:: 使用 dir /s /b 查找所有 package-info.java 文件并删除
set "deleted_count=0"
for /f "delims=" %%f in ('dir /s /b "!java_base!\package-info.java" 2^>nul') do (
    del "%%f" >nul 2>&1
    echo [删除] %%f
    set /a deleted_count+=1
)

echo.
if !deleted_count! gtr 0 (
    echo [成功] 已删除 !deleted_count! 个 package-info.java 文件。
) else (
    echo [提示] 未找到任何 package-info.java 文件。
)
goto :menu_loop

:end
echo.
echo 任务结束。
pause
exit /b
