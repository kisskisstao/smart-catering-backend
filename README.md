# 智慧餐饮后端

基于 Spring Boot 的智慧餐饮后端服务，提供用户登录、菜品展示、商家菜品管理、餐桌二维码、扫码点餐、订单流转、预约管理、商家数据看板、RabbitMQ 订单事件和 WebSocket 实时推送能力。

## 技术栈

- Java 17
- Spring Boot 3.3.5
- Spring Web / WebSocket
- MyBatis-Plus 3.5.6
- MySQL 8.x
- Redis
- RabbitMQ
- JWT：`jjwt 0.12.6`
- 二维码生成：ZXing
- Lombok

## 项目结构

```text
smart-catering-backend/
  pom.xml
  src/main/java/com/nuit/yujin/smartcateringbackend/
    common/          统一响应与异常处理
    config/          MyBatis-Plus、Redis、RabbitMQ、WebSocket 配置
    controller/      REST API 控制器
    dto/             请求 DTO
    entity/          数据库实体
    enums/           订单、餐桌状态枚举
    mapper/          MyBatis-Plus Mapper
    mq/              RabbitMQ 消息、生产者、消费者
    service/         业务服务
    utils/           JWT、二维码工具
    vo/              响应视图对象
    websocket/       WebSocket 处理器
  src/main/resources/
    application.yml  应用配置
    schema.sql       数据库表结构
```

## 核心功能

- 用户端
  - 微信小程序风格登录，当前实现为 mock openid 登录。
  - JWT 登录态签发与用户信息查询、更新。
  - 菜品列表、菜品详情、关键字筛选和排序。
  - 扫桌台二维码进入点餐。
  - 创建订单、模拟支付、取消待支付订单、查看历史订单和订单详情。
  - 创建预约、查看我的预约、取消预约。

- 商家端
  - 菜品分类增删改查。
  - 菜品分页、创建、编辑、删除、上下架。
  - 餐桌分页、创建、编辑、删除、状态更新。
  - 餐桌二维码刷新和二维码图片输出。
  - 商家订单分页、详情查看、订单状态流转。
  - 预约列表和预约状态处理。
  - 数据看板：今日订单数、今日营业额、待接单数、近 7 天订单趋势、菜品销量排行、分类占比。

- 异步与实时能力
  - RabbitMQ 发布订单创建和订单状态变更事件。
  - WebSocket 推送用户订单状态、商家新订单、桌台状态变更。

## 环境要求

| 工具 | 建议版本 | 说明 |
| --- | --- | --- |
| JDK | 17 | 编译和运行后端 |
| MySQL | 8.x | 业务数据存储 |
| Redis | 6.x+ | RedisTemplate 缓存/扩展能力 |
| RabbitMQ | 3.x+ | 订单事件队列 |
| Maven | 可选 | 项目包含 Maven Wrapper |

## 配置说明

默认配置位于 `smart-catering-backend/src/main/resources/application.yml`：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smart_catering
    username: root
    password: 123456
  data:
    redis:
      host: localhost
      port: 6379
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

如果本机 MySQL、Redis 或 RabbitMQ 配置不同，请同步修改该文件。

## 数据库初始化

先创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS smart_catering
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

再执行建表脚本：

```powershell
cd smart-catering-backend
mysql -uroot -p123456 --default-character-set=utf8mb4 smart_catering < src/main/resources/schema.sql
```

`schema.sql` 当前包含以下核心表：

- `user`：用户信息
- `dish_category`：菜品分类
- `dish`：菜品
- `dish_spec`：菜品规格
- `dining_table`：餐桌与二维码信息
- `dining_order`：订单主表
- `dining_order_item`：订单明细
- `reservation`：预约记录

## 启动服务

从仓库根目录进入 Maven 项目目录：

```powershell
cd smart-catering-backend
.\mvnw.cmd spring-boot:run
```

如果使用本机 Maven：

```powershell
cd smart-catering-backend
mvn spring-boot:run
```

服务默认启动在：

```text
http://localhost:8080
```

## 测试与构建

```powershell
cd smart-catering-backend
.\mvnw.cmd test
```

打包：

```powershell
cd smart-catering-backend
.\mvnw.cmd clean package
```

## 统一响应

接口统一返回 `Result<T>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

异常由全局异常处理器捕获，返回：

```json
{
  "code": 500,
  "message": "错误信息",
  "data": null
}
```

## 认证说明

登录接口返回 JWT：

```http
POST /user/login
```

请求体支持 `code`、`username`、`account` 任一字段：

```json
{
  "code": "test001"
}
```

后续用户端接口通过请求头传递：

```http
Authorization: Bearer <token>
```

当前用户登录逻辑为 mock 实现：`openid = mock_openid_{code}`。首次登录会自动创建用户，昵称默认为 `用户_{code}`。

## REST API 概览

### 用户接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/user/login` | 用户登录，返回 token 和用户信息 |
| GET | `/user/mock-login?code=xxx` | 调试登录 |
| GET | `/user/info` | 获取当前用户信息，需要 JWT |
| PUT | `/user/info` | 更新昵称、头像、手机号，需要 JWT |

### 用户端菜品接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/dish/list` | 菜品列表，支持 `storeId`、`type`、`keyword`、`sortType` |
| GET | `/dish/detail/{id}` | 菜品详情 |

### 商家分类接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/merchant/category/list` | 分类列表 |
| POST | `/merchant/category` | 新增分类 |
| PUT | `/merchant/category/{id}` | 编辑分类 |
| DELETE | `/merchant/category/{id}` | 删除分类 |

### 商家菜品接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/merchant/dish/page` | 菜品分页，支持关键字、分类、状态筛选 |
| POST | `/merchant/dish` | 新增菜品 |
| PUT | `/merchant/dish/{id}` | 编辑菜品 |
| DELETE | `/merchant/dish/{id}` | 删除菜品 |
| PUT | `/merchant/dish/{id}/status` | 更新菜品状态 |

### 餐桌接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/table/list` | 餐桌列表 |
| PUT | `/table/{id}/status` | 更新餐桌状态 |
| GET | `/merchant/table/page` | 商家餐桌分页 |
| POST | `/merchant/table` | 新增餐桌 |
| PUT | `/merchant/table/{id}` | 编辑餐桌 |
| DELETE | `/merchant/table/{id}` | 删除餐桌 |
| POST | `/merchant/table/{id}/qrcode` | 刷新餐桌二维码 |
| GET | `/merchant/table/{id}/qrcode/image` | 输出餐桌二维码 PNG |
| GET | `/table/scan` | 扫码校验，参数 `tableId`、`qrToken` |

餐桌状态：

- `FREE`：空闲
- `RESERVED`：已预约
- `OCCUPIED`：占用中
- `DIRTY`：待清理

### 订单接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/order/create` | 创建订单，需要 JWT |
| POST | `/order/{orderId}/pay` | 模拟支付，需要 JWT |
| PUT | `/order/{orderId}/cancel` | 取消待支付订单，需要 JWT |
| GET | `/order/history` | 用户订单历史，需要 JWT |
| GET | `/order/detail/{orderId}` | 用户订单详情，需要 JWT |
| GET | `/merchant/order/page` | 商家订单分页 |
| GET | `/merchant/order/detail/{orderId}` | 商家订单详情 |
| PUT | `/merchant/order/{orderId}/status` | 商家更新订单状态 |

创建订单请求示例：

```json
{
  "tableId": 1,
  "items": [
    {
      "dishId": 1,
      "quantity": 2,
      "spicy": "微辣",
      "size": "大份"
    }
  ]
}
```

订单状态：

- `PENDING_PAYMENT`：待支付
- `WAIT_ACCEPT`：待接单
- `COOKING`：制作中
- `COMPLETED`：已完成
- `CANCELLED`：已取消

合法状态流转：

```text
PENDING_PAYMENT -> WAIT_ACCEPT / CANCELLED
WAIT_ACCEPT     -> COOKING / CANCELLED
COOKING         -> COMPLETED
```

业务规则：

- 创建订单会校验桌台是否可点餐。
- 创建订单会扣减菜品库存，并把餐桌标记为 `OCCUPIED`。
- 订单默认 15 分钟支付有效期，超时支付会自动取消并恢复库存。
- 模拟支付成功后订单进入 `WAIT_ACCEPT`，并发送 RabbitMQ 订单创建和状态变更消息。
- 订单完成后餐桌标记为 `DIRTY`。

### 预约接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/reservation/create` | 创建预约，需要 JWT |
| GET | `/reservation/my` | 我的预约分页，需要 JWT |
| PUT | `/reservation/{id}/cancel` | 取消我的预约，需要 JWT |
| GET | `/merchant/reservation/page` | 商家预约分页 |
| PUT | `/merchant/reservation/{id}/status` | 商家处理预约状态 |

创建预约请求示例：

```json
{
  "storeId": 1,
  "tableId": 1,
  "contactName": "张三",
  "contactPhone": "13800000000",
  "reservationDate": "2026-06-08",
  "reservationTime": "18:30:00",
  "partySize": 4,
  "remark": "靠窗"
}
```

### 商家看板

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/merchant/dashboard` | 商家数据看板，参数 `storeId` |

返回内容包含：

- `summary.todayOrders`
- `summary.todayRevenue`
- `summary.waitAcceptCount`
- `trend`
- `dishRank`
- `categoryRatio`

## RabbitMQ

订单相关队列和交换机：

| 名称 | 类型 | 说明 |
| --- | --- | --- |
| `order.exchange` | DirectExchange | 订单交换机 |
| `order.created.queue` | Queue | 新订单队列 |
| `order.status.changed.queue` | Queue | 订单状态变更队列 |
| `order.created` | Routing Key | 新订单消息路由 |
| `order.status.changed` | Routing Key | 状态变更消息路由 |

订单支付成功后会发送新订单消息；订单状态变更、取消、完成等会发送状态变更消息。

## WebSocket

当前使用原生 Spring WebSocket，不是 STOMP。

| 路径 | 说明 |
| --- | --- |
| `/ws/order/{orderId}` | 订阅指定订单状态变更 |
| `/ws/merchant/order?storeId=1` | 商家订阅新订单推送 |
| `/ws/table/status?storeId=1` | 商家订阅餐桌状态变更 |

推送消息示例：

```json
{"type":"ORDER_STATUS_CHANGE","orderId":1,"status":"COOKING"}
```

```json
{"type":"NEW_ORDER","orderId":1,"orderNo":"OD202606081200001234","storeId":1,"totalAmount":58.00}
```

```json
{"type":"TABLE_STATUS_CHANGED","storeId":1,"tableId":1,"tableNo":"A01","status":"OCCUPIED","seats":4}
```

## 常见问题

- 启动时报数据库连接失败：确认 MySQL 已启动、`smart_catering` 数据库已创建，并且 `application.yml` 中用户名密码正确。
- Redis 连接失败：确认本机 Redis 运行在 `localhost:6379`，或修改配置。
- RabbitMQ 连接失败：确认 RabbitMQ 运行在 `localhost:5672`，默认账号密码为 `guest/guest`。
- 用户接口提示 token 错误：确认请求头格式为 `Authorization: Bearer <token>`。
- 商家接口多数通过 `storeId` 请求参数区分门店，未传时默认使用 `storeId=1`。

