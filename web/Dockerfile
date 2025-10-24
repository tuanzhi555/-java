
# ===== 阶段 1：在 Node 镜像里完成依赖安装与打包 =====
FROM 192.168.163.135/library/node:16 AS build
# ^ 使用你 Harbor 私库里的 node:16 作为构建阶段
#   （与你项目依赖匹配即可；如果支持更高版本，可升级，以获得安全修复）

WORKDIR /app
# ^ 设置工作目录，后续 COPY/RUN 都以此为相对路径

# 先只拷贝包管理文件，利用镜像层缓存，避免每次改源码都重装依赖
COPY package*.json ./
# ^ 同时匹配 package.json 和 package-lock.json
#   如果你用 pnpm/yarn，请改成相应锁文件与命令

RUN npm install
# ^ 安装依赖；若是 CI 环境、且有 lockfile，建议改用：
#   RUN npm ci
#   好处：更快、更可重复（严格按 lockfile 安装）

# 再拷贝其余源码（这一步改变才会触发重新打包）
COPY . .
# ^ .dockerignore 里建议忽略 node_modules、dist、.git 等，减少构建上下文体积

RUN npm run build
# ^ 产物一般在 dist/（Vite、Vue CLI、React 脚手架等默认）
#   若你的输出目录不是 dist，请和下方 COPY --from=build 的路径对应起来


# ===== 阶段 2：用 Nginx 作为静态文件服务器 =====
FROM 192.168.163.135/library/nginx:latest
# ^ 运行阶段镜像：Harbor 私库里的 nginx
#   若你用的是官方 nginx:alpine，可进一步瘦身

# 拷贝你定制的 Nginx 配置（需包含 SPA 回退，否则刷新会 404）
COPY nginx.conf /etc/nginx/nginx.conf
# ^ 你的 nginx.conf 里应该包含：
#   location / { try_files $uri $uri/ /index.html; }
#   并为静态资源设置合理缓存、禁用 index.html 缓存（避免发版后老页面）

# 拷贝前端构建产物到 Nginx 默认站点目录
COPY --from=build /app/dist/ /usr/share/nginx/html/
# ^ 如果你的输出不是 dist/，请改为对应目录

EXPOSE 80
# ^ 声明容器服务端口（给文档/工具用，实际映射由 `-p` 或 K8s Service 决定）
#   例如：docker run -p 30081:80 <image>  →  宿主 30081 → 容器 80

