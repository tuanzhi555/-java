
# ===== 阶段 1：在“构建镜像”里用 Maven 编译打包 =====
FROM 192.168.163.135/library/maven:3.8.6-openjdk-8 AS build
# ↑ 基础镜像：你私有库里的 maven:3.8.6 + JDK8，作为构建阶段使用
#   取名为 build，方便后面 --from=build 拷文件

WORKDIR /src
# ↑ 设置工作目录，后续 COPY / RUN 的相对路径都以此为基准

# 先放 settings.xml，优先写入私库/代理/镜像仓库配置（如国内源）
COPY mvn-settings.xml /root/.m2/settings.xml

# 只拷贝 pom.xml（不拷贝 src），最大化利用缓存层
COPY pom.xml .

# 预下载依赖到本地仓库（go-offline），并用 BuildKit 挂载缓存目录
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -s /root/.m2/settings.xml -B -q -DskipTests dependency:go-offline
# -s 指定 settings.xml
# -B 批处理模式（无交互）
# -q 安静输出
# -DskipTests 跳过测试，加速依赖下载
# dependency:go-offline 预拉所有依赖，后续 package 更快
# --mount=type=cache 需要开启 BuildKit（DOCKER_BUILDKIT=1）

# 现在再拷贝源码；若只改源码不改 pom，前面依赖缓存层仍可命中
COPY src ./src

# 真正打包：编译 + 打包可执行 jar（或 war）
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -s /root/.m2/settings.xml -B -DskipTests package
# 产物通常在 target/*.jar

# ===== 阶段 2：精简运行镜像（只带 JRE）=====
FROM 192.168.163.135/library/openjdk:8
# ↑ 运行时镜像（JRE/JDK8）。也来自你的 Harbor 私库，减少外网依赖。
#   如果想进一步瘦身，可以考虑 temurin:8-jre、eclipse-temurin:8-jre 或自制 jre 镜像

WORKDIR /app

# 只把上一阶段产物拷进来，不带构建工具和源码
COPY --from=build /src/target/*.jar app.jar

# 运行参数（可按容器内存调优）
ENV JAVA_OPTS="-Xms256m -Xmx512m"
# ↑ 用环境变量传 JVM 参数，便于在不同环境覆盖（docker run -e / K8s env）

EXPOSE 8080
# ↑ 声明容器对外服务端口（文档性/给工具用，不做真实映射）
#   ⚠️ 确认你的应用实际监听端口；Spring Boot 默认 8080。
#   如果你的服务实际是 8888（你前面 K8s 写的是 8888），请改成 EXPOSE 8888 或改应用端口。

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
# ↑ 用 sh -c 是为了在启动时对 $JAVA_OPTS 做环境变量展开
#   若直接写 ["java","$JAVA_OPTS","-jar","/app/app.jar"] 不会展开 $JAVA_OPTS

