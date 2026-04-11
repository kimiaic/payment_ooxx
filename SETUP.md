# OOPay 开发环境搭建指南

## 环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 21 (LTS) | 必须使用 JDK 21 |
| Maven | 3.9.x | 构建工具 |
| MySQL | 8.0+ | 数据库 |
| Redis | 7.x | 缓存 |
| RabbitMQ | 3.12+ | 消息队列 |
| Node.js | 20.x | 前端构建（可选） |

---

## 1. Java 环境

### 1.1 安装 JDK 21

```bash
# macOS (Homebrew)
brew install openjdk@21

# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-21-jdk

# 验证
java -version
# 输出应包含: openjdk version "21"
```

### 1.2 配置环境变量

```bash
# ~/.bashrc 或 ~/.zshrc
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk  # Linux
export JAVA_HOME=/opt/homebrew/opt/openjdk@21  # macOS
export PATH=$JAVA_HOME/bin:$PATH
```

---

## 2. Maven 配置

### 2.1 安装

```bash
# macOS
brew install maven

# Ubuntu
sudo apt install maven

# 验证
mvn -v
# Apache Maven 3.9.x
```

### 2.2 配置阿里云镜像（推荐）

编辑 `~/.m2/settings.xml`：

```xml
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <name>Aliyun Maven</name>
      <url>https://maven.aliyun.com/repository/public</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
```

---

## 3. MySQL 8.0

### 3.1 Docker 安装（推荐）

```bash
docker run -d \
  --name oopay-mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=oopay123 \
  -e MYSQL_DATABASE=oopay \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci
```

### 3.2 创建数据库用户

```sql
-- 连接 MySQL
mysql -h127.0.0.1 -uroot -p

-- 创建应用用户
CREATE USER 'oopay'@'%' IDENTIFIED BY 'oopay_secret';
GRANT ALL PRIVILEGES ON oopay.* TO 'oopay'@'%';
FLUSH PRIVILEGES;
```

### 3.3 本地安装

```bash
# macOS
brew install mysql@8.0
brew services start mysql@8.0

# Ubuntu
sudo apt install mysql-server-8.0
sudo systemctl start mysql
```

---

## 4. Redis 7.x

### 4.1 Docker 安装

```bash
docker run -d \
  --name oopay-redis \
  -p 6379:6379 \
  redis:7-alpine \
  redis-server --appendonly yes
```

### 4.2 本地安装

```bash
# macOS
brew install redis
brew services start redis

# Ubuntu
sudo apt install redis-server
sudo systemctl start redis-server
```

---

## 5. RabbitMQ

### 5.1 Docker 安装（推荐）

```bash
docker run -d \
  --name oopay-rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=oopay \
  -e RABBITMQ_DEFAULT_PASS=oopay123 \
  rabbitmq:3.12-management-alpine
```

管理界面: http://localhost:15672  
账号: `oopay` / `oopay123`

---

## 6. 项目构建

### 6.1 克隆项目

```bash
git clone https://github.com/kimiaic/payment_ooxx.git
cd payment_ooxx
git checkout develop
```

### 6.2 编译

```bash
# 完整编译（含测试）
mvn clean package

# 跳过测试快速编译
mvn clean package -DskipTests

# 仅编译
mvn clean compile
```

### 6.3 验证

```bash
# 检查编译结果
ls -la oopay-gateway/target/*.jar
```

---

## 7. 数据库初始化

```bash
# 执行 Flyway 迁移
mvn flyway:migrate -Dflyway.url=jdbc:mysql://127.0.0.1:3306/oopay \
  -Dflyway.user=oopay \
  -Dflyway.password=oopay_secret
```

或在应用启动时自动执行（需配置 `application.yml`）。

---

## 8. 本地开发配置

### 8.1 复制配置文件

```bash
cp oopay-gateway/src/main/resources/application-dev.yml.example \
   oopay-gateway/src/main/resources/application-dev.yml
```

### 8.2 修改数据库连接

编辑 `application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/oopay?useUnicode=true&characterEncoding=utf-8
    username: oopay
    password: oopay_secret
  
  redis:
    host: 127.0.0.1
    port: 6379
  
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: oopay
    password: oopay123
```

---

## 9. IDE 配置

### IntelliJ IDEA

1. 导入项目: `File -> New -> Project from Existing Sources` 选择 `pom.xml`
2. 设置 JDK: `Project Structure -> SDKs -> + JDK 21`
3. 启用注解处理: `Settings -> Build -> Annotation Processors` 勾选启用
4. 安装插件: Lombok, MapStruct Support

### VS Code

推荐插件：
- Extension Pack for Java
- Spring Boot Extension Pack
- Lombok Annotations Support

---

## 10. 快速启动（Docker Compose）

创建 `docker-compose.dev.yml`：

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: oopay123
      MYSQL_DATABASE: oopay
    ports:
      - "3306:3306"
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  rabbitmq:
    image: rabbitmq:3.12-management-alpine
    environment:
      RABBITMQ_DEFAULT_USER: oopay
      RABBITMQ_DEFAULT_PASS: oopay123
    ports:
      - "5672:5672"
      - "15672:15672"
```

启动：

```bash
docker-compose -f docker-compose.dev.yml up -d
```

停止：

```bash
docker-compose -f docker-compose.dev.yml down
```

---

## 常见问题

### Q: Maven 下载依赖很慢？
A: 配置阿里云镜像（见 2.2 节）

### Q: 编译报错 "invalid source release: 21"？
A: 检查 `JAVA_HOME` 指向 JDK 21

### Q: Flyway 迁移失败？
A: 确保数据库已创建，且用户有 DDL 权限

### Q: 端口冲突？
A: 修改 Docker 端口映射，如 `-p 3307:3306`

---

## 验证清单

- [ ] `java -version` 显示 21
- [ ] `mvn -v` 显示 3.9.x
- [ ] MySQL 可连接，oopay 数据库已创建
- [ ] Redis 可连接（`redis-cli ping` 返回 PONG）
- [ ] RabbitMQ 管理界面可访问
- [ ] `mvn clean compile` 成功
- [ ] Flyway 迁移成功
