[English](README.md) | [한국어](README.ko.md) | **简体中文** | [日本語](README.ja.md)

# Secure Spring MVC JSP Starter

Secure Spring MVC JSP Starter 是面向 Spring MVC、JSP 与 MariaDB 业务应用的安全基线。它不只是一个登录示例，还将账户锁定、会话撤销、管理员保护规则、安全审计、SQL 观测、容器加固、数据库驱动的多语言管理，以及多标签全屏工作区整合在同一套项目标准中。

当前基线使用 Java 21、Spring Boot 4.1.0 与 MariaDB 12.3.2 LTS。由于 JSP 不适用于 Spring Boot 可执行 JAR，应用以可执行 WAR 形式打包。

![Spring MVC Starter 英文首页](docs/images/home.png)

该项目适合内部系统、后台管理工具与运维门户。除了界面示例，它还提供账户与会话不变量、数据库迁移、运维日志、容器执行方式和发布前验证标准。

## 功能概览

| 领域 | 内置基线 |
|---|---|
| 服务端 | Java 21、Spring Boot、Spring MVC、JSP/JSTL、可执行 WAR |
| 数据 | MariaDB、Spring Data JPA、Flyway、HikariCP |
| 安全 | Spring Security、Argon2、CSRF、CSP、会话限制、账户锁定、审计日志 |
| 管理 | 用户账户、登录记录、活动会话、动态菜单、多语言、密码修改 |
| 业务 UI | 全屏外壳、权限导航、多标签内容、响应式布局 |
| 多语言 | 英文默认；英文、韩文、简体中文、日文完整界面；运行时语言与翻译管理 |
| 运维 | Docker Compose、健康探针、分类日志、SQL 与慢查询观测 |
| 供应链 | 第三方许可证报告、CycloneDX SBOM、自动依赖更新 |

## 内置应用标准

### 全屏业务工作区

- 登录后使用桌面式全屏布局
- 从数据库读取菜单，并按分组、角色和显示顺序过滤
- 通过同源 iframe 标签同时保留多个 JSP 业务页面
- 按用户将打开标签与活动标签保存到 `sessionStorage`，刷新后自动恢复
- 支持菜单搜索、侧栏折叠、当前标签刷新、默认标签清理与移动端导航
- 管理页面以工具栏、指标条、数据表格和编辑面板为主，而不是仅堆叠卡片
- `app.css` 保存结构行为，`theme-modern.css` 管理颜色、密度、圆角与阴影
- 活动标签不使用色带、底部强调线或下划线指示器，而通过背景、文字、字重和无障碍状态区分

业务页面地址来自数据库菜单定义。只有服务端批准的内部路径可以在标签中打开，标签状态也只保留在当前浏览器会话中。

![英文多标签业务工作区](docs/images/workspace.png)

### 数据库驱动的多语言

- 默认语言为英文，首页、登录、注册、错误页、工作区及所有管理页面完整支持英文、韩文、简体中文和日文
- 可直接在语言网格的每一行修改 BCP 47 标签、名称、顺序和启用状态，并逐行保存
- 翻译矩阵以消息键为行、注册语言为列，可同时比较和编辑多种语言
- 清空单元格即可恢复消息包文本，MariaDB 中已提交的修改会在下一次请求生效
- 翻译在事务提交后立即清理缓存，下一次请求即可生效，无需重新部署
- 首次访问会在数据库启用语言中匹配浏览器 `Accept-Language`，无匹配时使用数据库默认语言
- 语言偏好存入 `HttpOnly`、`SameSite=Lax` Cookie；生产 HTTPS 默认启用 `Secure`
- 语言切换会保留安全的内部查询参数，同时拒绝外部 URL、协议相对 URL 和路径穿越
- 动态翻译只允许纯文本，并验证控制字符与 `MessageFormat` 占位符
- 缓存大小与有效期有上限，应用外直接修改数据库时也会在 TTL 后重新读取

要在运行时添加语言，请在 **多语言管理** 的新建行中填写 BCP 47 标签，再在翻译矩阵中新出现的列中录入文本。同一消息行可同时修改多种语言并一次保存，也可在单元格中按 `Ctrl/⌘ + Enter` 保存。若新语言需要随应用提供完整、审核后的默认文本，可再添加 `messages_<语言标签>.properties`。自动测试会检查四个内置消息包的键和值是否一致且非空。

| English | 한국어 | 简体中文 | 日本語 |
|---|---|---|---|
| ![英文登录](docs/images/login-en.png) | ![韩文登录](docs/images/login.png) | ![中文登录](docs/images/login-zh.png) | ![日文登录](docs/images/login-ja.png) |

| 行内语言网格 | 多语言翻译矩阵 |
|---|---|
| ![行内运行时语言网格](docs/images/localization.png) | ![数据库多语言翻译矩阵](docs/images/translations.png) |

### 基本管理页面

| 页面 | 主要能力 | 所需角色 |
|---|---|---|
| 业务状态 | 查看账户、登录、会话指标与近期安全事件 | 用户 |
| 用户账户 | 查询账户、修改角色或状态、解除锁定 | 管理员 |
| 登录记录 | 查看成功/失败、请求 ID、时间与远程地址 | 管理员 |
| 会话管理 | 查看活动会话并撤销指定会话 | 管理员 |
| 菜单管理 | 编辑分组、顺序、内部路径、角色与可见性 | 管理员 |
| 多语言管理 | 直接编辑语言行和消息×语言翻译矩阵 | 管理员 |
| 修改密码 | 验证当前密码、应用策略并终止全部会话 | 用户 |

### 认证与账户不变量

- 用户名规范化与重复检测，注册功能默认关闭
- 使用 Argon2id 系列默认值的 `DelegatingPasswordEncoder`
- 能识别旧 bcrypt、PBKDF2、scrypt 哈希，并在成功登录后自动升级
- 校验密码长度、控制字符以及是否包含用户名或电子邮箱
- 修改密码前验证当前密码，并禁止重复使用；修改后终止所有会话
- 以用户名和 IP 为维度限制登录频率，并在数据库中保持账户锁定状态
- 对不存在、密码错误或已锁定账户统一返回同一失败消息，防止账户枚举
- 在执行高成本哈希前限制凭据长度与整个表单大小
- 支持一次性初始管理员创建
- 防止管理员停用自己、移除自己的管理员角色，以及删除最后一个管理员
- 角色或账户状态变化后撤销已有会话

### Web 安全

- Spring Security CSRF 防护，所有状态变更均使用 POST
- 登录后轮换会话 ID，限制并发会话，退出时清理会话与 Cookie
- 可配置 `HttpOnly`、`Secure`、`SameSite` 会话 Cookie
- 默认启用 CSP、Permissions-Policy、Referrer-Policy、HSTS、同源框架限制与 MIME 嗅探防护
- 认证页面禁止缓存，错误响应不暴露内部异常
- JSP 位于 `/WEB-INF/views`，并对动态输出进行转义或输入限制
- 为请求生成 ID，清理日志换行符，避免日志注入

### 数据与运维

- Flyway 版本化模式、Hibernate `validate`、关闭 Open Session in View
- HikariCP 连接、超时、生命周期与池大小均可通过环境变量配置
- 应用、安全审计、SQL 日志独立轮转
- SQL 绑定值日志默认关闭
- 本地环境启用 MariaDB 慢查询表
- 安全事件同时写入数据库与独立审计日志
- Flyway V2 提供动态菜单，V3 提供运行时语言和消息目录
- Actuator health/info 可匿名访问，其余管理端点要求管理员权限
- 支持优雅停机、存活/就绪探针、非 root 用户和只读应用容器

## 快速开始

只需要 Docker；本地 Java 和 Maven 是可选项。

### 环境要求

- Docker Engine 或 Docker Desktop，支持 Compose v2
- `make`，或直接执行 `Makefile` 中等价的 Docker Compose 命令
- 本地端口 `8080` 与 `3306`，也可在 `.env` 中修改

在仓库根目录创建本地环境文件：

```sh
make init
```

该命令会生成随机数据库密码与初始管理员密码，将 `.env` 权限设置为 `600`，且不会覆盖已有文件。`.env` 包含敏感值，不要提交或共享。

启动本地服务：

```sh
make up
```

容器健康后：

1. 使用 `.env` 中的 `APP_BOOTSTRAP_ADMIN_USERNAME` 与 `APP_BOOTSTRAP_ADMIN_PASSWORD` 登录 `http://127.0.0.1:8080/login`。
2. 确认英文、韩文、中文和日文可立即切换，并在刷新后保持。
3. 立即修改临时密码；修改会终止已有会话。
4. 下次启动前将 `APP_BOOTSTRAP_ADMIN_ENABLED=false`。
5. 执行冒烟检查。

```sh
make smoke
```

默认仅绑定 `127.0.0.1`。如端口冲突，只修改 `.env` 中的 `APP_PORT` 或 `DB_PORT`。

```sh
make logs         # 跟踪应用与数据库日志
make test         # 运行完整测试
make package      # 测试并生成可执行 WAR
make public-check # 检查公开仓库安全性
make down         # 停止容器并保留数据库卷
```

安装本地 Java 21 后，也可以运行 `./mvnw test` 或 `./mvnw clean package`。WAR 输出到 `target/secure-mvc-starter.war`。

### 健康检查

| 地址 | 预期结果 |
|---|---|
| `http://127.0.0.1:8080/login` | 登录页面 |
| `http://127.0.0.1:8080/internal/actuator/health` | 应用健康状态 |
| `http://127.0.0.1:8080/internal/actuator/health/liveness` | 进程存活状态 |
| `http://127.0.0.1:8080/internal/actuator/health/readiness` | 请求就绪状态 |

`make logs` 可同时查看应用和数据库输出。`make down` 会保留数据库卷；仅在明确需要清除本地数据时才删除卷。

## 克隆后需要定制的内容

1. 修改 `pom.xml` 的 `groupId`、`artifactId`、`name` 与 `finalName`。
2. 将 `com.example.webstarter` 重命名为组织的反向域名包。
3. 修改 `messages*.properties` 的产品文案与默认菜单翻译，并通过 `theme-modern.css` 调整语义设计变量。
4. 保持现有 Flyway `V1` 到 `V3` 不变；新增业务表必须使用新的迁移版本。
5. 若权限超过 `USER` 与 `ADMIN`，应引入能力型权限并同步更新服务层 `@PreAuthorize`。
6. 部署前完成[安全检查表](docs/SECURITY-CHECKLIST.md)与[运维指南](docs/OPERATIONS.md)。

已应用迁移的校验和不能被改写。生产环境应通过后续迁移兼容演进，而不是修改旧文件。

## 项目结构

```text
src/main/java/com/example/webstarter
├── config       环境配置与 Spring Security 链
├── security     认证、密码、频率限制与会话撤销
├── user         用户领域与账户服务
├── admin        管理功能与不变量
├── audit        数据库及文件安全事件
├── bootstrap    一次性管理员创建
├── navigation   数据库菜单、安全路径与角色过滤
├── localization 运行时语言目录、翻译覆盖、校验与缓存
└── web          MVC、语言切换、请求 ID、响应头与访问日志
```

请求流程、数据模型和扩展边界请参阅[架构文档](docs/ARCHITECTURE.md)。

## 配置原则

运行环境值不写死在应用代码中。`application.yml` 的运维设置都可由环境变量覆盖。

| 领域 | 代表变量 | 默认方向 |
|---|---|---|
| 数据库 | `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`DB_POOL_MAX_SIZE` | 外部注入密钥，限制连接池 |
| 会话 | `SESSION_COOKIE_SECURE`、`SESSION_COOKIE_SAME_SITE`、`SESSION_TIMEOUT` | 生产 HTTPS、30 分钟、Lax |
| 多语言 | `APP_I18N_DEFAULT_LOCALE`、`APP_I18N_SUPPORTED_LOCALES`、`APP_I18N_COOKIE_*`、`APP_I18N_CACHE_*` | 数据库目录、英文后备、安全 Cookie 与缓存上限 |
| 密码哈希 | `APP_PASSWORD_ALGORITHM`、`APP_ARGON2_*` | Argon2，启动时验证 |
| 登录保护 | `APP_LOGIN_*`、`APP_ACCOUNT_LOCK_*` | IP/标识符限制与账户锁定 |
| 响应头 | `APP_CONTENT_SECURITY_POLICY`、`APP_FRAME_OPTIONS`、`APP_HSTS_*` | 禁止外部框架，允许同源工作标签 |
| 日志 | `APP_SQL_LOG_LEVEL`、`APP_SQL_BIND_LOG_LEVEL`、`APP_LOG_*` | 生产 SQL 关闭，绑定值关闭 |
| 管理 | `MANAGEMENT_ENDPOINTS`、`APP_ADMIN_PAGE_SIZE` | 最小端点与分页上限 |
| HTTP 输入 | `SERVER_MAX_FORM_POST_SIZE`、`SERVER_MAX_PARAMETER_COUNT` | 尽早拒绝过大请求 |
| 工作区 | `APP_WORKSPACE_MAX_TABS`、`APP_WORKSPACE_DEFAULT_MENU_KEY` | 标签上限与默认页面 |

完整变量与部署示例见[运维指南](docs/OPERATIONS.md)。

## SQL 与慢查询观测

本地配置将 Hibernate SQL 写入 `/app/logs/sql.log`。绑定值可能含密码、令牌或个人信息，因此 `APP_SQL_BIND_LOG_LEVEL=OFF` 是默认值，也是生产建议值。

```sh
docker compose --env-file .env exec app sh -c 'tail -f /app/logs/sql.log'
docker compose --env-file .env exec db mariadb -uroot -p
```

在数据库控制台查看慢查询：

```sql
SELECT start_time, query_time, rows_examined, sql_text
FROM mysql.slow_log
ORDER BY start_time DESC
LIMIT 50;
```

生产环境必须设置慢日志保留策略。高流量系统应使用文件输出或集中式可观测平台。

## 有意保留的边界

本仓库是安全起点，而不是完整身份产品。邮箱所有权验证、密码重置邮件、MFA/通行密钥、SSO/OIDC、CAPTCHA 与个人数据保留策略依赖具体业务基础设施，因此没有用虚假示例模拟。

面向互联网开放注册前，至少应增加邮箱验证与账户恢复。多实例部署时，应将内存登录限制与 `SessionRegistry` 替换为共享限流器和 Spring Session，并为数据库迁移与运行时访问使用不同账户。

## 许可证与供应链

项目源码采用 [Apache License 2.0](LICENSE)。第三方许可证和再分发说明记录在 [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md)。

`clean package` 会在 `target/generated-sources/license/THIRD-PARTY.txt` 生成直接与传递依赖的许可证报告。缺失许可证元数据会导致构建失败，CycloneDX SBOM 会打包到 `META-INF/sbom`。

```sh
make license-report
make package
```

## 公开仓库安全措施

[AGENTS.md](AGENTS.md) 规定了供应商中立表述、工作站信息与密钥排除、截图检查和依赖许可证审查。克隆后启用 Git hooks：

```sh
git config core.hooksPath .githooks
./scripts/public-release-check.sh repository
```

扫描器会拒绝环境文件、密钥、证书、日志、构建产物、过大文件、绝对用户路径和高可信密钥模式。个人阻止词应仅放在未跟踪的 `.git/info/public-release-deny-patterns` 中。

贡献流程见 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 参考标准

- [Spring Boot Servlet/JSP 文档](https://docs.spring.io/spring-boot/4.0/reference/web/servlet.html)
- [Spring Boot traditional WAR 部署](https://docs.spring.io/spring-boot/how-to/deployment/traditional-deployment.html)
- [Spring Security CSRF](https://docs.spring.io/spring-security/reference/7.0/servlet/exploits/csrf.html)
- [Spring Security 安全响应头](https://docs.spring.io/spring-security/reference/features/exploits/headers.html)
- [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [MariaDB Connector/J](https://mariadb.com/docs/connectors/mariadb-connector-j/about-mariadb-connector-j)
