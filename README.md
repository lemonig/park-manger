# 车位租售管理系统（后台管理）

## 项目概述
本项目是一个地下车位租售管理系统，主要用于管理员管理地下车位信息，用户发布租售车位信息。管理员可以查看、编辑和删除车位信息，审核用户发布的租售信息。

## 技术栈
- **后端**：Spring Boot
- **数据库**：MySQL
- **安全**：JWT认证
- **依赖管理**：Maven
- **文档生成**：Swagger

## 功能模块
1. **用户管理**：
   - 用户注册与登录。
   - 管理员可查看、修改用户信息。
2. **车位管理**：
   - 录入车位信息：车位编号、类型、状态、业主信息等。
   - 车位状态更新：如空闲、已租、已售等。
   - 用户发布的车位信息管理（审核、删除等）。
3. **数据统计与报表**：
   - 车位租赁情况统计。
   - 收入报表。

## 系统部署

### 1. 环境要求
- **Java 21+**
- **MySQL 5.7+**
- **Maven 3.6+**

### 2. 配置数据库
在 `application.properties` 文件中配置数据库连接信息：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/parking_management
spring.datasource.username=root
spring.datasource.password=root
