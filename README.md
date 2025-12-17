### 构建 
    mvn clean package

### 准备目录结构


    C:\tools\epub-count

    epub-count.jar

    epub-count.cmd


### 创建启动脚本 `epub-count.cmd`

    @echo off

    java -jar "%~dp0epub-count.jar" %*

### 将目录加入 PATH

    Win + R

    输入 sysdm.cpl

    高级 → 环境变量

    在「用户变量」中编辑 Path

    新增：C:\tools\epub-count

### 验证

在任意目录打开新的 cmd：
    
    epub-count your-book.epub

