#  

本文档详细描述了跳蚤市场系统的各个 API 接口，包括 URL 路径、请求方法、请求参数、响应数据结构和错误码说明。

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| 40000 | 请求参数错误 |
| 40100 | 未登录 |
| 40101 | 无权限 |
| 40400 | 请求数据不存在 |
| 40300 | 禁止访问 |
| 50000 | 系统内部异常 |
| 50001 | 操作失败 |
| 422200 | 包含违禁词，多次违禁将封禁账号 |
| 50002 | 用户余额不足，无法调用 AI |

## 用户管理接口

### 用户注册

- **URL**: `/user/register`
- **方法**: POST
- **描述**: 新用户注册账号

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userAccount | string | 是 | 用户账号 |
| userPassword | string | 是 | 用户密码 |
| userName | string | 是 | 用户昵称 |
| userPhone | string | 是 | 用户联系方式 |

#### 响应数据

```json
{
  "code": 0,
  "data": 123,
  "message": "注册成功"
}
```

### 用户登录

- **URL**: `/user/login`
- **方法**: POST
- **描述**: 用户账号密码登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userAccount | string | 是 | 用户账号 |
| userPassword | string | 是 | 用户密码 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "id": 123,
    "userAccount": "test",
    "userName": "测试用户",
    "userAvatar": "avatar_url",
    "userRole": "user",
    "userStatus": 1,
    "userPhone": "13800138000",
    "point": 100.00,
    "auditTime": "2023-01-01 12:00:00",
    "createTime": "2023-01-01 12:00:00",
    "updateTime": "2023-01-01 12:00:00"
  },
  "message": "ok"
}
```

### 用户注销

- **URL**: `/user/logout`
- **方法**: POST
- **描述**: 用户退出登录

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "注销成功"
}
```

### 获取当前登录用户

- **URL**: `/user/get/login`
- **方法**: GET
- **描述**: 获取当前登录用户的详细信息
- **权限要求**: 需要登录

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "id": 123,
    "userAccount": "test",
    "userName": "测试用户",
    "userAvatar": "avatar_url",
    "userRole": "user",
    "userStatus": 1,
    "userPhone": "13800138000",
    "point": 100.00,
    "auditTime": "2023-01-01 12:00:00",
    "createTime": "2023-01-01 12:00:00",
    "updateTime": "2023-01-01 12:00:00"
  },
  "message": "ok"
}
```

### 创建用户（管理员）

- **URL**: `/user/add`
- **方法**: POST
- **描述**: 管理员创建新用户
- **权限要求**: 管理员权限

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userAccount | string | 是 | 用户账号 |
| userName | string | 是 | 用户昵称 |
| userAvatar | string | 否 | 用户头像 |
| userRole | string | 否 | 用户角色 |
| userStatus | integer | 否 | 用户状态 |
| userPhone | string | 是 | 用户联系方式 |
| point | number | 否 | 用户积分 |

#### 响应数据

```json
{
  "code": 0,
  "data": 123,
  "message": "用户创建成功"
}
```

### 删除用户（管理员）

- **URL**: `/user/delete`
- **方法**: POST
- **描述**: 管理员删除用户
- **权限要求**: 管理员权限

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | integer | 是 | 用户ID |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "用户删除成功"
}
```

### 更新用户（管理员）

- **URL**: `/user/update`
- **方法**: POST
- **描述**: 管理员更新用户信息
- **权限要求**: 管理员权限

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | integer | 是 | 用户ID |
| userAccount | string | 否 | 用户账号 |
| userName | string | 否 | 用户昵称 |
| userAvatar | string | 否 | 用户头像 |
| userRole | string | 否 | 用户角色 |
| userStatus | integer | 否 | 用户状态 |
| userPhone | string | 否 | 用户联系方式 |
| point | number | 否 | 用户积分 |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "用户更新成功"
}
```

### 根据ID获取用户（管理员）

- **URL**: `/user/get`
- **方法**: GET
- **描述**: 管理员根据用户ID获取用户详细信息
- **权限要求**: 管理员权限

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | integer | 是 | 用户ID |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "id": 123,
    "userAccount": "test",
    "userPassword": "encrypted_password",
    "userName": "测试用户",
    "userAvatar": "avatar_url",
    "userRole": "user",
    "userStatus": 1,
    "userPhone": "13800138000",
    "point": 100.00,
    "auditTime": "2023-01-01 12:00:00",
    "createTime": "2023-01-01 12:00:00",
    "updateTime": "2023-01-01 12:00:00"
  },
  "message": "ok"
}
```

### 根据ID获取用户视图

- **URL**: `/user/get/vo`
- **方法**: GET
- **描述**: 根据用户ID获取用户视图信息

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | integer | 是 | 用户ID |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "id": 123,
    "userName": "测试用户",
    "userAvatar": "avatar_url",
    "userRole": "user",
    "createTime": "2023-01-01 12:00:00"
  },
  "message": "ok"
}
```

### 分页获取用户列表（管理员）

- **URL**: `/user/list/page`
- **方法**: POST
- **描述**: 管理员分页获取用户列表
- **权限要求**: 管理员权限

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | integer | 否 | 当前页码，默认为1 |
| pageSize | integer | 否 | 每页大小，默认为10 |
| id | integer | 否 | 用户ID |
| userName | string | 否 | 用户名 |
| userRole | string | 否 | 用户角色 |
| point | integer | 否 | 用户积分 |
| sortField | string | 否 | 排序字段 |
| sortOrder | string | 否 | 排序顺序 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 123,
        "userAccount": "test",
        "userPassword": "encrypted_password",
        "userName": "测试用户",
        "userAvatar": "avatar_url",
        "userRole": "user",
        "userStatus": 1,
        "userPhone": "13800138000",
        "point": 100.00,
        "auditTime": "2023-01-01 12:00:00",
        "createTime": "2023-01-01 12:00:00",
        "updateTime": "2023-01-01 12:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

### 分页获取用户视图列表

- **URL**: `/user/list/page/vo`
- **方法**: GET
- **描述**: 分页获取用户视图信息列表

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | integer | 否 | 当前页码，默认为1 |
| size | integer | 否 | 每页大小，默认为10 |
| id | integer | 否 | 用户ID |
| userName | string | 否 | 用户名 |
| userRole | string | 否 | 用户角色 |
| point | integer | 否 | 用户积分 |
| sortField | string | 否 | 排序字段 |
| sortOrder | string | 否 | 排序顺序 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 123,
        "userName": "测试用户",
        "userAvatar": "avatar_url",
        "userRole": "user",
        "createTime": "2023-01-01 12:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

### 更新个人信息

- **URL**: `/user/update/my`
- **方法**: POST
- **描述**: 用户更新自己的个人信息
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userName | string | 否 | 用户昵称 |
| userAvatar | string | 否 | 用户头像 |
| userPhone | string | 否 | 用户联系方式 |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "个人信息更新成功"
}
```

### 审核用户（管理员）

- **URL**: `/user/admin/audit`
- **方法**: POST
- **描述**: 管理员审核用户申请
- **权限要求**: 管理员权限

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | integer | 是 | 用户ID |
| auditStatus | integer | 是 | 审核状态（1-通过，2-拒绝） |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "用户审核成功"
}
```

### 获取待审核用户列表（管理员）

- **URL**: `/user/admin/pending`
- **方法**: GET
- **描述**: 管理员获取待审核用户列表
- **权限要求**: 管理员权限

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | integer | 否 | 当前页码，默认为1 |
| size | integer | 否 | 每页大小，默认为10 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 123,
        "userAccount": "test",
        "userPassword": "encrypted_password",
        "userName": "测试用户",
        "userAvatar": "avatar_url",
        "userRole": "user",
        "userStatus": 0,
        "userPhone": "13800138000",
        "point": 0.00,
        "auditTime": null,
        "createTime": "2023-01-01 12:00:00",
        "updateTime": "2023-01-01 12:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

### 获取已拒绝用户数量（管理员）

- **URL**: `/user/admin/rejected/count`
- **方法**: GET
- **描述**: 管理员获取已拒绝用户数量
- **权限要求**: 管理员权限

#### 响应数据

```json
{
  "code": 0,
  "data": 5,
  "message": "ok"
}
```

### 批量删除已拒绝用户（管理员）

- **URL**: `/user/admin/rejected/delete-all`
- **方法**: POST
- **描述**: 管理员一键删除所有已拒绝的用户账号
- **权限要求**: 管理员权限

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| confirm | boolean | 是 | 确认标志，必须为true |
| remark | string | 否 | 备注信息 |

#### 响应数据

```json
{
  "code": 0,
  "data": "成功删除了 5 个已拒绝用户",
  "message": "ok"
}
```

## 商品管理接口

### 添加商品

- **URL**: `/product/add`
- **方法**: POST
- **描述**: 用户添加新的商品
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| productName | string | 是 | 商品名称 |
| description | string | 否 | 商品描述 |
| price | number | 是 | 商品价格 |
| imageUrl | string | 否 | 商品图片地址 |
| categoryId | integer | 是 | 商品分类ID |
| paymentMethod | integer | 是 | 支付方式 (0-现金支付, 1-微信支付, 2-积分兑换, 3-二手物品交换) |

#### 响应数据

```json
{
  "code": 0,
  "data": 123,
  "message": "商品添加成功"
}
```

### 更新商品信息

- **URL**: `/product/update`
- **方法**: PUT
- **描述**: 用户更新自己的商品信息
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | integer | 是 | 商品ID |
| productName | string | 否 | 商品名称 |
| description | string | 否 | 商品描述 |
| price | number | 否 | 商品价格 |
| imageUrl | string | 否 | 商品图片地址 |
| categoryId | integer | 否 | 商品分类ID |
| paymentMethod | integer | 否 | 支付方式 (0-现金支付, 1-微信支付, 2-积分兑换, 3-二手物品交换) |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "商品更新成功"
}
```

### 删除商品

- **URL**: `/product/delete`
- **方法**: POST
- **描述**: 用户删除自己的商品
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | integer | 是 | 商品ID |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "商品删除成功"
}
```

### 获取商品详情

- **URL**: `/product/get/{id}`
- **方法**: GET
- **描述**: 根据商品ID获取商品详细信息（包含关联的分类和用户信息）

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | integer | 是 | 商品ID |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "id": 123,
    "productName": "测试商品",
    "description": "商品描述",
    "price": 99.99,
    "imageUrl": "image_url",
    "status": 1,
    "paymentMethod": 1,
    "categoryId": 1,
    "userId": 456,
    "category": {
      "id": 1,
      "name": "电子产品"
    },
    "user": {
      "id": 456,
      "userAccount": "seller",
      "userName": "卖家",
      "userAvatar": "avatar_url",
      "userRole": "user",
      "userStatus": 1,
      "userPhone": "13800138000",
      "point": 100.00,
      "auditTime": "2023-01-01 12:00:00",
      "createTime": "2023-01-01 12:00:00",
      "updateTime": "2023-01-01 12:00:00"
    },
    "createTime": "2023-01-01 12:00:00",
    "updateTime": "2023-01-01 12:00:00"
  },
  "message": "ok"
}
```

### 分页获取商品列表

- **URL**: `/product/list/page`
- **方法**: GET
- **描述**: 分页获取已上架的商品列表

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | integer | 否 | 当前页码，默认为1 |
| size | integer | 否 | 每页大小，默认为10 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 123,
        "productName": "测试商品",
        "description": "商品描述",
        "price": 99.99,
        "imageUrl": "image_url",
        "status": 1,
        "paymentMethod": 1,
        "category": {
          "id": 1,
          "name": "电子产品"
        },
        "user": {
          "id": 456,
          "userAccount": "seller",
          "userName": "卖家",
          "userAvatar": "avatar_url",
          "userRole": "user",
          "userStatus": 1,
          "userPhone": "13800138000",
          "point": 100.00,
          "auditTime": "2023-01-01 12:00:00",
          "createTime": "2023-01-01 12:00:00",
          "updateTime": "2023-01-01 12:00:00"
        },
        "createTime": "2023-01-01 12:00:00",
        "updateTime": "2023-01-01 12:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

### 根据分类获取商品列表

- **URL**: `/product/list/category/{categoryId}`
- **方法**: GET
- **描述**: 根据分类ID分页获取商品列表

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| categoryId | integer | 是 | 分类ID |
| current | integer | 否 | 当前页码，默认为1 |
| size | integer | 否 | 每页大小，默认为10 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 123,
        "productName": "测试商品",
        "description": "商品描述",
        "price": 99.99,
        "imageUrl": "image_url",
        "status": 1,
        "paymentMethod": 1,
        "category": {
          "id": 1,
          "name": "电子产品"
        },
        "user": {
          "id": 456,
          "userAccount": "seller",
          "userName": "卖家",
          "userAvatar": "avatar_url",
          "userRole": "user",
          "userStatus": 1,
          "userPhone": "13800138000",
          "point": 100.00,
          "auditTime": "2023-01-01 12:00:00",
          "createTime": "2023-01-01 12:00:00",
          "updateTime": "2023-01-01 12:00:00"
        },
        "createTime": "2023-01-01 12:00:00",
        "updateTime": "2023-01-01 12:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

### 搜索商品

- **URL**: `/product/search`
- **方法**: GET
- **描述**: 根据关键词搜索商品

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 是 | 搜索关键词 |
| current | integer | 否 | 当前页码，默认为1 |
| size | integer | 否 | 每页大小，默认为10 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 123,
        "productName": "测试商品",
        "description": "商品描述",
        "price": 99.99,
        "imageUrl": "image_url",
        "status": 1,
        "paymentMethod": 1,
        "category": {
          "id": 1,
          "name": "电子产品"
        },
        "user": {
          "id": 456,
          "userAccount": "seller",
          "userName": "卖家",
          "userAvatar": "avatar_url",
          "userRole": "user",
          "userStatus": 1,
          "userPhone": "13800138000",
          "point": 100.00,
          "auditTime": "2023-01-01 12:00:00",
          "createTime": "2023-01-01 12:00:00",
          "updateTime": "2023-01-01 12:00:00"
        },
        "createTime": "2023-01-01 12:00:00",
        "updateTime": "2023-01-01 12:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

### 高级搜索商品

- **URL**: `/product/advanced-search`
- **方法**: GET
- **描述**: 多条件组合搜索商品，支持分类、价格、支付方式筛选和排序

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 否 | 搜索关键词 |
| categoryId | integer | 否 | 分类ID |
| minPrice | number | 否 | 最低价格 |
| maxPrice | number | 否 | 最高价格 |
| paymentMethod | integer | 否 | 支付方式 (0-现金, 1-微信, 2-积分, 3-交换) |
| sortField | string | 否 | 排序字段 (price/createtime/name) |
| sortOrder | string | 否 | 排序顺序 (asc/desc)，默认为desc |
| current | integer | 否 | 当前页码，默认为1 |
| size | integer | 否 | 每页大小，默认为10 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 123,
        "productName": "测试商品",
        "description": "商品描述",
        "price": 99.99,
        "imageUrl": "image_url",
        "status": 1,
        "paymentMethod": 1,
        "category": {
          "id": 1,
          "name": "电子产品"
        },
        "user": {
          "id": 456,
          "userAccount": "seller",
          "userName": "卖家",
          "userAvatar": "avatar_url",
          "userRole": "user",
          "userStatus": 1,
          "userPhone": "13800138000",
          "point": 100.00,
          "auditTime": "2023-01-01 12:00:00",
          "createTime": "2023-01-01 12:00:00",
          "updateTime": "2023-01-01 12:00:00"
        },
        "createTime": "2023-01-01 12:00:00",
        "updateTime": "2023-01-01 12:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

### 获取用户发布的商品列表

- **URL**: `/product/list/user/{userId}`
- **方法**: GET
- **描述**: 根据用户ID获取该用户发布的商品列表

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | integer | 是 | 用户ID |
| current | integer | 否 | 当前页码，默认为1 |
| size | integer | 否 | 每页大小，默认为10 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 123,
        "productName": "测试商品",
        "description": "商品描述",
        "price": 99.99,
        "imageUrl": "image_url",
        "status": 1,
        "paymentMethod": 1,
        "category": {
          "id": 1,
          "name": "电子产品"
        },
        "user": {
          "id": 456,
          "userAccount": "seller",
          "userName": "卖家",
          "userAvatar": "avatar_url",
          "userRole": "user",
          "userStatus": 1,
          "userPhone": "13800138000",
          "point": 100.00,
          "auditTime": "2023-01-01 12:00:00",
          "createTime": "2023-01-01 12:00:00",
          "updateTime": "2023-01-01 12:00:00"
        },
        "createTime": "2023-01-01 12:00:00",
        "updateTime": "2023-01-01 12:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

### 获取当前用户发布的商品列表

- **URL**: `/product/list/my`
- **方法**: GET
- **描述**: 获取当前登录用户发布的商品列表
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | integer | 否 | 当前页码，默认为1 |
| size | integer | 否 | 每页大小，默认为10 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 123,
        "productName": "测试商品",
        "description": "商品描述",
        "price": 99.99,
        "imageUrl": "image_url",
        "status": 1,
        "paymentMethod": 1,
        "category": {
          "id": 1,
          "name": "电子产品"
        },
        "user": {
          "id": 456,
          "userAccount": "seller",
          "userName": "卖家",
          "userAvatar": "avatar_url",
          "userRole": "user",
          "userStatus": 1,
          "userPhone": "13800138000",
          "point": 100.00,
          "auditTime": "2023-01-01 12:00:00",
          "createTime": "2023-01-01 12:00:00",
          "updateTime": "2023-01-01 12:00:00"
        },
        "createTime": "2023-01-01 12:00:00",
        "updateTime": "2023-01-01 12:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

### 更新商品状态

- **URL**: `/product/status/{id}`
- **方法**: PUT
- **描述**: 更新商品状态（上架/下架/售出等）
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | integer | 是 | 商品ID |
| status | integer | 是 | 商品状态 |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "商品状态更新成功"
}
```

### 获取最新商品列表

- **URL**: `/product/latest`
- **方法**: GET
- **描述**: 获取最新发布的商品列表

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| limit | integer | 否 | 限制数量，默认为10 |

#### 响应数据

```json
{
  "code": 0,
  "data": [
    {
      "id": 123,
      "productName": "测试商品",
      "description": "商品描述",
      "price": 99.99,
      "imageUrl": "image_url",
      "status": 1,
      "paymentMethod": 1,
      "category": {
        "id": 1,
        "name": "电子产品"
      },
      "user": {
        "id": 456,
        "userAccount": "seller",
        "userName": "卖家",
        "userAvatar": "avatar_url",
        "userRole": "user",
        "userStatus": 1,
        "userPhone": "13800138000",
        "point": 100.00,
        "auditTime": "2023-01-01 12:00:00",
        "createTime": "2023-01-01 12:00:00",
        "updateTime": "2023-01-01 12:00:00"
      },
      "createTime": "2023-01-01 12:00:00",
      "updateTime": "2023-01-01 12:00:00"
    }
  ],
  "message": "ok"
}
```

### 审核商品（管理员）

- **URL**: `/product/review/{id}`
- **方法**: PUT
- **描述**: 管理员审核商品（上架/拒绝）
- **权限要求**: 管理员权限

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | integer | 是 | 商品ID |
| status | integer | 是 | 审核状态 |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "商品审核成功"
}
```

### 管理员获取所有商品列表（管理员）

- **URL**: `/product/admin/list`
- **方法**: GET
- **描述**: 管理员获取所有商品列表（包括未审核的）
- **权限要求**: 管理员权限

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | integer | 否 | 当前页码，默认为1 |
| size | integer | 否 | 每页大小，默认为10 |
| keyword | string | 否 | 关键词 |
| categoryId | integer | 否 | 分类ID |
| status | integer | 否 | 商品状态 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 123,
        "productName": "测试商品",
        "description": "商品描述",
        "price": 99.99,
        "imageUrl": "image_url",
        "status": 1,
        "paymentMethod": 1,
        "category": {
          "id": 1,
          "name": "电子产品"
        },
        "user": {
          "id": 456,
          "userAccount": "seller",
          "userName": "卖家",
          "userAvatar": "avatar_url",
          "userRole": "user",
          "userStatus": 1,
          "userPhone": "13800138000",
          "point": 100.00,
          "auditTime": "2023-01-01 12:00:00",
          "createTime": "2023-01-01 12:00:00",
          "updateTime": "2023-01-01 12:00:00"
        },
        "createTime": "2023-01-01 12:00:00",
        "updateTime": "2023-01-01 12:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

## 商品分类管理接口

### 获取所有商品分类

- **URL**: `/category/list`
- **方法**: GET
- **描述**: 获取系统中所有的商品分类信息

#### 响应数据

```json
{
  "code": 0,
  "data": [
    {
      "id": 1,
      "name": "电子产品"
    },
    {
      "id": 2,
      "name": "书籍"
    }
  ],
  "message": "ok"
}
```

### 添加商品分类（管理员）

- **URL**: `/category/add`
- **方法**: POST
- **描述**: 管理员添加新的商品分类
- **权限要求**: 管理员权限

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | string | 是 | 分类名称 |

#### 响应数据

```json
{
  "code": 0,
  "data": 123,
  "message": "商品分类添加成功"
}
```

### 更新商品分类（管理员）

- **URL**: `/category/update`
- **方法**: PUT
- **描述**: 管理员更新商品分类信息
- **权限要求**: 管理员权限

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | integer | 是 | 分类ID |
| name | string | 是 | 分类名称 |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "商品分类更新成功"
}
```

### 删除商品分类（管理员）

- **URL**: `/category/delete/{id}`
- **方法**: DELETE
- **描述**: 管理员根据ID删除商品分类
- **权限要求**: 管理员权限

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | integer | 是 | 分类ID |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "商品分类删除成功"
}
```

## 订单管理接口

### 创建订单

- **URL**: `/order/create`
- **方法**: POST
- **描述**: 用户创建新的订单，自动使用商品设置的支付方式
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| productId | integer | 是 | 商品ID |

#### 响应数据

```json
{
  "code": 0,
  "data": 123,
  "message": "订单创建成功"
}
```

### 支付订单

- **URL**: `/order/pay/{orderId}`
- **方法**: POST
- **描述**: 用户支付订单
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | integer | 是 | 订单ID |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "订单支付成功"
}
```

### 取消订单

- **URL**: `/order/cancel/{orderId}`
- **方法**: POST
- **描述**: 用户取消订单
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | integer | 是 | 订单ID |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "订单取消成功"
}
```

### 完成订单

- **URL**: `/order/complete/{orderId}`
- **方法**: POST
- **描述**: 卖家或买家确认订单完成
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | integer | 是 | 订单ID |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "订单完成成功"
}
```

### 获取订单详情

- **URL**: `/order/get/{orderId}`
- **方法**: GET
- **描述**: 根据订单ID获取订单详细信息
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | integer | 是 | 订单ID |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "id": 123,
    "productId": 456,
    "buyer": {
      "id": 789,
      "userAccount": "buyer",
      "userName": "买家",
      "userAvatar": "avatar_url",
      "userRole": "user",
      "userStatus": 1,
      "userPhone": "13800138000",
      "point": 100.00,
      "auditTime": "2023-01-01 12:00:00",
      "createTime": "2023-01-01 12:00:00",
      "updateTime": "2023-01-01 12:00:00"
    },
    "seller": {
      "id": 101,
      "userAccount": "seller",
      "userName": "卖家",
      "userAvatar": "avatar_url",
      "userRole": "user",
      "userStatus": 1,
      "userPhone": "13800138000",
      "point": 100.00,
      "auditTime": "2023-01-01 12:00:00",
      "createTime": "2023-01-01 12:00:00",
      "updateTime": "2023-01-01 12:00:00"
    },
    "amount": 99.99,
    "paymentMethod": 1,
    "status": 1,
    "paymentProof": "proof_url",
    "buyerConfirmed": true,
    "sellerConfirmed": true,
    "createTime": "2023-01-01 12:00:00",
    "finishTime": "2023-01-01 13:00:00"
  },
  "message": "ok"
}
```

### 获取买家订单列表

- **URL**: `/order/list/buyer`
- **方法**: GET
- **描述**: 获取当前登录用户的买家订单列表
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | integer | 否 | 当前页码，默认为1 |
| size | integer | 否 | 每页大小，默认为10 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 123,
        "productId": 456,
        "buyer": {
          "id": 789,
          "userAccount": "buyer",
          "userName": "买家",
          "userAvatar": "avatar_url",
          "userRole": "user",
          "userStatus": 1,
          "userPhone": "13800138000",
          "point": 100.00,
          "auditTime": "2023-01-01 12:00:00",
          "createTime": "2023-01-01 12:00:00",
          "updateTime": "2023-01-01 12:00:00"
        },
        "seller": {
          "id": 101,
          "userAccount": "seller",
          "userName": "卖家",
          "userAvatar": "avatar_url",
          "userRole": "user",
          "userStatus": 1,
          "userPhone": "13800138000",
          "point": 100.00,
          "auditTime": "2023-01-01 12:00:00",
          "createTime": "2023-01-01 12:00:00",
          "updateTime": "2023-01-01 12:00:00"
        },
        "amount": 99.99,
        "paymentMethod": 1,
        "status": 1,
        "paymentProof": "proof_url",
        "buyerConfirmed": true,
        "sellerConfirmed": true,
        "createTime": "2023-01-01 12:00:00",
        "finishTime": "2023-01-01 13:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

### 获取卖家订单列表

- **URL**: `/order/list/seller`
- **方法**: GET
- **描述**: 获取当前登录用户的卖家订单列表
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | integer | 否 | 当前页码，默认为1 |
| size | integer | 否 | 每页大小，默认为10 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 123,
        "productId": 456,
        "buyer": {
          "id": 789,
          "userAccount": "buyer",
          "userName": "买家",
          "userAvatar": "avatar_url",
          "userRole": "user",
          "userStatus": 1,
          "userPhone": "13800138000",
          "point": 100.00,
          "auditTime": "2023-01-01 12:00:00",
          "createTime": "2023-01-01 12:00:00",
          "updateTime": "2023-01-01 12:00:00"
        },
        "seller": {
          "id": 101,
          "userAccount": "seller",
          "userName": "卖家",
          "userAvatar": "avatar_url",
          "userRole": "user",
          "userStatus": 1,
          "userPhone": "13800138000",
          "point": 100.00,
          "auditTime": "2023-01-01 12:00:00",
          "createTime": "2023-01-01 12:00:00",
          "updateTime": "2023-01-01 12:00:00"
        },
        "amount": 99.99,
        "paymentMethod": 1,
        "status": 1,
        "paymentProof": "proof_url",
        "buyerConfirmed": true,
        "sellerConfirmed": true,
        "createTime": "2023-01-01 12:00:00",
        "finishTime": "2023-01-01 13:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

### 根据状态获取买家订单列表

- **URL**: `/order/list/buyer/status/{status}`
- **方法**: GET
- **描述**: 根据状态获取买家订单列表
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | integer | 是 | 订单状态 (0-待支付, 1-已支付, 2-已完成, 3-已取消) |
| current | integer | 否 | 当前页码，默认为1 |
| size | integer | 否 | 每页大小，默认为10 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 123,
        "productId": 456,
        "buyer": {
          "id": 789,
          "userAccount": "buyer",
          "userName": "买家",
          "userAvatar": "avatar_url",
          "userRole": "user",
          "userStatus": 1,
          "userPhone": "13800138000",
          "point": 100.00,
          "auditTime": "2023-01-01 12:00:00",
          "createTime": "2023-01-01 12:00:00",
          "updateTime": "2023-01-01 12:00:00"
        },
        "seller": {
          "id": 101,
          "userAccount": "seller",
          "userName": "卖家",
          "userAvatar": "avatar_url",
          "userRole": "user",
          "userStatus": 1,
          "userPhone": "13800138000",
          "point": 100.00,
          "auditTime": "2023-01-01 12:00:00",
          "createTime": "2023-01-01 12:00:00",
          "updateTime": "2023-01-01 12:00:00"
        },
        "amount": 99.99,
        "paymentMethod": 1,
        "status": 1,
        "paymentProof": "proof_url",
        "buyerConfirmed": true,
        "sellerConfirmed": true,
        "createTime": "2023-01-01 12:00:00",
        "finishTime": "2023-01-01 13:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

### 根据状态获取卖家订单列表

- **URL**: `/order/list/seller/status/{status}`
- **方法**: GET
- **描述**: 根据状态获取卖家订单列表
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | integer | 是 | 订单状态 (0-待支付, 1-已支付, 2-已完成, 3-已取消) |
| current | integer | 否 | 当前页码，默认为1 |
| size | integer | 否 | 每页大小，默认为10 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 123,
        "productId": 456,
        "buyer": {
          "id": 789,
          "userAccount": "buyer",
          "userName": "买家",
          "userAvatar": "avatar_url",
          "userRole": "user",
          "userStatus": 1,
          "userPhone": "13800138000",
          "point": 100.00,
          "auditTime": "2023-01-01 12:00:00",
          "createTime": "2023-01-01 12:00:00",
          "updateTime": "2023-01-01 12:00:00"
        },
        "seller": {
          "id": 101,
          "userAccount": "seller",
          "userName": "卖家",
          "userAvatar": "avatar_url",
          "userRole": "user",
          "userStatus": 1,
          "userPhone": "13800138000",
          "point": 100.00,
          "auditTime": "2023-01-01 12:00:00",
          "createTime": "2023-01-01 12:00:00",
          "updateTime": "2023-01-01 12:00:00"
        },
        "amount": 99.99,
        "paymentMethod": 1,
        "status": 1,
        "paymentProof": "proof_url",
        "buyerConfirmed": true,
        "sellerConfirmed": true,
        "createTime": "2023-01-01 12:00:00",
        "finishTime": "2023-01-01 13:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

### 获取订单统计信息

- **URL**: `/order/statistics`
- **方法**: GET
- **描述**: 获取当前用户的订单统计信息
- **权限要求**: 需要登录

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "pendingPayment": 1,
    "paid": 2,
    "completed": 5,
    "cancelled": 1
  },
  "message": "ok"
}
```

### 获取所有订单（管理员）

- **URL**: `/order/admin/list`
- **方法**: GET
- **描述**: 管理员获取所有订单列表
- **权限要求**: 管理员权限

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | integer | 否 | 当前页码，默认为1 |
| size | integer | 否 | 每页大小，默认为10 |
| status | integer | 否 | 订单状态 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 123,
        "productId": 456,
        "buyer": {
          "id": 789,
          "userAccount": "buyer",
          "userName": "买家",
          "userAvatar": "avatar_url",
          "userRole": "user",
          "userStatus": 1,
          "userPhone": "13800138000",
          "point": 100.00,
          "auditTime": "2023-01-01 12:00:00",
          "createTime": "2023-01-01 12:00:00",
          "updateTime": "2023-01-01 12:00:00"
        },
        "seller": {
          "id": 101,
          "userAccount": "seller",
          "userName": "卖家",
          "userAvatar": "avatar_url",
          "userRole": "user",
          "userStatus": 1,
          "userPhone": "13800138000",
          "point": 100.00,
          "auditTime": "2023-01-01 12:00:00",
          "createTime": "2023-01-01 12:00:00",
          "updateTime": "2023-01-01 12:00:00"
        },
        "amount": 99.99,
        "paymentMethod": 1,
        "status": 1,
        "paymentProof": "proof_url",
        "buyerConfirmed": true,
        "sellerConfirmed": true,
        "createTime": "2023-01-01 12:00:00",
        "finishTime": "2023-01-01 13:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

### 提交支付凭证

- **URL**: `/order/submit/proof`
- **方法**: POST
- **描述**: 买家上传现金支付凭证
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | integer | 是 | 订单ID |
| paymentProof | string | 是 | 支付凭证URL |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "支付凭证提交成功"
}
```

### 确认订单

- **URL**: `/order/confirm`
- **方法**: POST
- **描述**: 买家确认收货
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | integer | 是 | 订单ID |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "确认收货成功"
}
```

### 模拟微信支付

- **URL**: `/order/pay/wechat/{orderId}`
- **方法**: POST
- **描述**: 模拟微信支付流程
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | integer | 是 | 订单ID |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "微信支付成功"
}
```

### 积分兑换商品

- **URL**: `/order/pay/points/{orderId}`
- **方法**: POST
- **描述**: 使用积分兑换商品
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | integer | 是 | 订单ID |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "积分兑换成功"
}
```

### 申请物品交换

- **URL**: `/order/exchange/apply/{orderId}`
- **方法**: POST
- **描述**: 买家申请物品交换
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | integer | 是 | 订单ID |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "物品交换申请成功，等待卖家确认"
}
```

### 确认物品交换

- **URL**: `/order/exchange/confirm/{orderId}`
- **方法**: POST
- **描述**: 卖家确认物品交换
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | integer | 是 | 订单ID |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "物品交换确认成功"
}
```

## 平台公告管理接口

### 分页获取新闻列表

- **URL**: `/news/list`
- **方法**: GET
- **描述**: 分页获取系统中的新闻信息

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | integer | 否 | 当前页码，默认为1 |
| size | integer | 否 | 每页大小，默认为10 |

#### 响应数据

```json
{
  "code": 0,
  "data": [
    {
      "id": 123,
      "title": "平台公告标题",
      "content": "平台公告内容",
      "imageUrl": "image_url",
      "authorId": 456,
      "author": "作者姓名",
      "createTime": "2023-01-01 12:00:00"
    }
  ],
  "message": "ok"
}
```

### 获取最新新闻

- **URL**: `/news/latest`
- **方法**: GET
- **描述**: 获取系统中最新的一条新闻信息

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "id": 123,
    "title": "平台公告标题",
    "content": "平台公告内容",
    "imageUrl": "image_url",
    "authorId": 456,
    "author": "作者姓名",
    "createTime": "2023-01-01 12:00:00"
  },
  "message": "ok"
}
```

### 获取新闻详情

- **URL**: `/news/detail/{id}`
- **方法**: GET
- **描述**: 根据ID获取新闻的详细信息

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | integer | 是 | 新闻ID |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "id": 123,
    "title": "平台公告标题",
    "content": "平台公告内容",
    "imageUrl": "image_url",
    "authorId": 456,
    "author": "作者姓名",
    "createTime": "2023-01-01 12:00:00"
  },
  "message": "ok"
}
```

### 添加新闻（管理员）

- **URL**: `/news/add`
- **方法**: POST
- **描述**: 管理员添加新的新闻
- **权限要求**: 管理员权限

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string | 是 | 新闻标题 |
| content | string | 是 | 新闻内容 |
| imageUrl | string | 否 | 新闻图片地址 |

#### 响应数据

```json
{
  "code": 0,
  "data": 123,
  "message": "新闻添加成功"
}
```

### 更新新闻（管理员）

- **URL**: `/news/update`
- **方法**: PUT
- **描述**: 管理员更新新闻信息
- **权限要求**: 管理员权限

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | integer | 是 | 新闻ID |
| title | string | 是 | 新闻标题 |
| content | string | 是 | 新闻内容 |
| imageUrl | string | 否 | 新闻图片地址 |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "新闻更新成功"
}
```

### 删除新闻（管理员）

- **URL**: `/news/delete/{id}`
- **方法**: DELETE
- **描述**: 管理员根据ID删除新闻
- **权限要求**: 管理员权限

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | integer | 是 | 新闻ID |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "新闻删除成功"
}
```

## 商品评价管理接口

### 添加评价

- **URL**: `/review/add`
- **方法**: POST
- **描述**: 用户对已完成的订单进行评价
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| productId | integer | 是 | 商品ID |
| orderId | integer | 否 | 订单ID |
| rating | integer | 是 | 评分 (1-5分) |
| content | string | 是 | 评价内容 |

#### 响应数据

```json
{
  "code": 0,
  "data": 123,
  "message": "评价添加成功"
}
```

### 更新评价信息

- **URL**: `/review/update`
- **方法**: PUT
- **描述**: 用户更新自己的评价信息
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | integer | 是 | 评价ID |
| rating | integer | 是 | 评分 (1-5分) |
| content | string | 是 | 评价内容 |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "评价更新成功"
}
```

### 删除评价

- **URL**: `/review/delete`
- **方法**: POST
- **描述**: 用户删除自己的评价
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | integer | 是 | 评价ID |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "评价删除成功"
}
```

### 获取评价详情

- **URL**: `/review/get/{id}`
- **方法**: GET
- **描述**: 根据评价ID获取评价详细信息

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | integer | 是 | 评价ID |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "id": 123,
    "productId": 456,
    "orderId": 789,
    "userId": 101,
    "rating": 5,
    "content": "很好的商品",
    "createTime": "2023-01-01 12:00:00"
  },
  "message": "ok"
}
```

### 分页获取评价列表

- **URL**: `/review/list/page`
- **方法**: GET
- **描述**: 分页获取所有评价列表

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | integer | 否 | 当前页码，默认为1 |
| size | integer | 否 | 每页大小，默认为10 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 123,
        "productId": 456,
        "orderId": 789,
        "userId": 101,
        "rating": 5,
        "content": "很好的商品",
        "createTime": "2023-01-01 12:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

### 根据商品ID获取评价列表

- **URL**: `/review/list/product/{productId}`
- **方法**: GET
- **描述**: 根据商品ID分页获取评价列表

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| productId | integer | 是 | 商品ID |
| current | integer | 否 | 当前页码，默认为1 |
| size | integer | 否 | 每页大小，默认为10 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 123,
        "productId": 456,
        "orderId": 789,
        "userId": 101,
        "rating": 5,
        "content": "很好的商品",
        "createTime": "2023-01-01 12:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

### 根据用户ID获取评价列表

- **URL**: `/review/list/user/{userId}`
- **方法**: GET
- **描述**: 根据用户ID分页获取评价列表

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | integer | 是 | 用户ID |
| current | integer | 否 | 当前页码，默认为1 |
| size | integer | 否 | 每页大小，默认为10 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 123,
        "productId": 456,
        "orderId": 789,
        "userId": 101,
        "rating": 5,
        "content": "很好的商品",
        "createTime": "2023-01-01 12:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

### 根据订单ID获取评价列表

- **URL**: `/review/list/order/{orderId}`
- **方法**: GET
- **描述**: 根据订单ID分页获取评价列表

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderId | integer | 是 | 订单ID |
| current | integer | 否 | 当前页码，默认为1 |
| size | integer | 否 | 每页大小，默认为10 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 123,
        "productId": 456,
        "orderId": 789,
        "userId": 101,
        "rating": 5,
        "content": "很好的商品",
        "createTime": "2023-01-01 12:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

### 获取用户对商品的评价

- **URL**: `/review/get/user/{userId}/product/{productId}`
- **方法**: GET
- **描述**: 获取指定用户对指定商品的评价

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | integer | 是 | 用户ID |
| productId | integer | 是 | 商品ID |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "id": 123,
    "productId": 456,
    "orderId": 789,
    "userId": 101,
    "rating": 5,
    "content": "很好的商品",
    "createTime": "2023-01-01 12:00:00"
  },
  "message": "ok"
}
```

### 获取当前用户对商品的评价

- **URL**: `/review/get/my/product/{productId}`
- **方法**: GET
- **描述**: 获取当前登录用户对指定商品的评价
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| productId | integer | 是 | 商品ID |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "id": 123,
    "productId": 456,
    "orderId": 789,
    "userId": 101,
    "rating": 5,
    "content": "很好的商品",
    "createTime": "2023-01-01 12:00:00"
  },
  "message": "ok"
}
```

### 获取商品平均评分

- **URL**: `/review/average/{productId}`
- **方法**: GET
- **描述**: 获取指定商品的平均评分

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| productId | integer | 是 | 商品ID |

#### 响应数据

```json
{
  "code": 0,
  "data": 4.5,
  "message": "ok"
}
```

### 获取商品评价统计信息

- **URL**: `/review/statistics/{productId}`
- **方法**: GET
- **描述**: 获取指定商品的评价统计信息

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| productId | integer | 是 | 商品ID |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "productId": 123,
    "totalReviews": 10,
    "averageRating": 4.5,
    "ratingDistribution": {
      "1": 0,
      "2": 1,
      "3": 1,
      "4": 3,
      "5": 5
    }
  },
  "message": "ok"
}
```

### 获取当前用户的评价列表

- **URL**: `/review/list/my`
- **方法**: GET
- **描述**: 获取当前登录用户的评价列表
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | integer | 否 | 当前页码，默认为1 |
| size | integer | 否 | 每页大小，默认为10 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 123,
        "productId": 456,
        "orderId": 789,
        "userId": 101,
        "rating": 5,
        "content": "很好的商品",
        "createTime": "2023-01-01 12:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

### 管理员获取所有评价列表（管理员）

- **URL**: `/review/admin/list`
- **方法**: GET
- **描述**: 管理员获取所有评价列表
- **权限要求**: 管理员权限

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | integer | 否 | 当前页码，默认为1 |
| size | integer | 否 | 每页大小，默认为10 |
| productId | integer | 否 | 商品ID |
| userId | integer | 否 | 用户ID |
| orderId | integer | 否 | 订单ID |
| minRating | integer | 否 | 最低评分 |
| maxRating | integer | 否 | 最高评分 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 123,
        "productId": 456,
        "orderId": 789,
        "userId": 101,
        "rating": 5,
        "content": "很好的商品",
        "createTime": "2023-01-01 12:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  },
  "message": "ok"
}
```

## 统计分析接口

### 获取月度交易商品排行

- **URL**: `/statistics/monthly-products`
- **方法**: GET
- **描述**: 获取指定月份的交易商品排行榜
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| month | integer | 是 | 月份 |
| year | integer | 是 | 年份 |
| limit | integer | 否 | 限制数量，默认为10 |

#### 响应数据

```json
{
  "code": 0,
  "data": [
    {
      "productId": 123,
      "productName": "测试商品",
      "transactionCount": 10,
      "totalAmount": 999.90
    }
  ],
  "message": "ok"
}
```

### 获取活跃用户排行

- **URL**: `/statistics/active-users`
- **方法**: GET
- **描述**: 获取指定时间范围内的活跃用户排行榜
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| limit | integer | 否 | 限制数量，默认为10 |
| startDate | string | 是 | 开始日期 (格式: yyyy-MM-dd) |
| endDate | string | 是 | 结束日期 (格式: yyyy-MM-dd) |

#### 响应数据

```json
{
  "code": 0,
  "data": [
    {
      "userId": 123,
      "userName": "活跃用户",
      "activityScore": 95.5
    }
  ],
  "message": "ok"
}
```

### 获取需求量大商品排行

- **URL**: `/statistics/high-demand-products`
- **方法**: GET
- **描述**: 获取需求量大的商品排行榜
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| limit | integer | 否 | 限制数量，默认为10 |

#### 响应数据

```json
{
  "code": 0,
  "data": [
    {
      "productId": 123,
      "productName": "热门商品",
      "viewCount": 1000,
      "wishlistCount": 50
    }
  ],
  "message": "ok"
}
```

### 获取闲置量大商品排行

- **URL**: `/statistics/high-inventory-products`
- **方法**: GET
- **描述**: 获取闲置量大的商品排行榜
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| limit | integer | 否 | 限制数量，默认为10 |

#### 响应数据

```json
{
  "code": 0,
  "data": [
    {
      "productId": 123,
      "productName": "库存商品",
      "daysInInventory": 30,
      "inventoryCount": 50
    }
  ],
  "message": "ok"
}
```

### 获取综合统计信息

- **URL**: `/statistics/comprehensive`
- **方法**: GET
- **描述**: 获取指定时间范围内的综合统计信息
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | string | 否 | 开始日期 (格式: yyyy-MM-dd) |
| endDate | string | 否 | 结束日期 (格式: yyyy-MM-dd) |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "totalUsers": 1000,
    "totalProducts": 5000,
    "totalOrders": 2000,
    "totalTransactions": 1500,
    "totalRevenue": 150000.00,
    "userGrowth": {
      "thisMonth": 50,
      "lastMonth": 40,
      "growthRate": 25.0
    },
    "orderTrend": {
      "thisMonth": 200,
      "lastMonth": 180,
      "growthRate": 11.1
    }
  },
  "message": "ok"
}
```

### 获取月度统计数据

- **URL**: `/statistics/monthly`
- **方法**: GET
- **描述**: 获取指定月份的统计数据
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| month | integer | 是 | 月份 |
| year | integer | 是 | 年份 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "year": 2023,
    "month": 1,
    "newUsers": 50,
    "newProducts": 200,
    "completedOrders": 150,
    "totalRevenue": 15000.00,
    "popularCategories": [
      {
        "categoryId": 1,
        "categoryName": "电子产品",
        "orderCount": 50
      }
    ]
  },
  "message": "ok"
}
```

### 获取用户交易统计

- **URL**: `/statistics/user/{userId}`
- **方法**: GET
- **描述**: 获取指定用户的交易统计数据
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | integer | 是 | 用户ID |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "userId": 123,
    "totalPurchases": 10,
    "totalSales": 5,
    "totalSpent": 1000.00,
    "totalEarned": 500.00,
    "favoriteCategory": {
      "categoryId": 1,
      "categoryName": "电子产品"
    },
    "monthlyStats": [
      {
        "year": 2023,
        "month": 1,
        "purchases": 2,
        "sales": 1,
        "spent": 200.00,
        "earned": 100.00
      }
    ]
  },
  "message": "ok"
}
```

### 获取商品交易统计

- **URL**: `/statistics/product/{productId}`
- **方法**: GET
- **描述**: 获取指定商品的交易统计数据
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| productId | integer | 是 | 商品ID |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "productId": 123,
    "totalSales": 10,
    "totalRevenue": 1000.00,
    "averageRating": 4.5,
    "viewCount": 1000,
    "conversionRate": 1.0,
    "monthlyStats": [
      {
        "year": 2023,
        "month": 1,
        "sales": 2,
        "revenue": 200.00
      }
    ]
  },
  "message": "ok"
}
```

## 图片管理接口

### 上传用户头像

- **URL**: `/image/upload/avatar`
- **方法**: POST
- **描述**: 用户上传个人头像图片
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 头像图片文件 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "originalUrl": "/api/images/avatars/2024/01/01/uuid_filename.jpg",
    "thumbnailUrls": {
      "small": "/api/images/thumbnails/avatars/2024/01/01/uuid_filename_small.jpg",
      "medium": "/api/images/thumbnails/avatars/2024/01/01/uuid_filename_medium.jpg",
      "large": "/api/images/thumbnails/avatars/2024/01/01/uuid_filename_large.jpg"
    },
    "fileName": "uuid_filename.jpg",
    "fileSize": 102400,
    "imageType": "AVATAR",
    "uploadTime": "2024-01-01 12:00:00"
  },
  "message": "图片上传成功"
}
```

### 上传商品图片

- **URL**: `/image/upload/product`
- **方法**: POST
- **描述**: 上传商品相关图片
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 商品图片文件 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "originalUrl": "/api/images/products/2024/01/01/uuid_filename.jpg",
    "thumbnailUrls": {
      "small": "/api/images/thumbnails/products/2024/01/01/uuid_filename_small.jpg",
      "medium": "/api/images/thumbnails/products/2024/01/01/uuid_filename_medium.jpg",
      "large": "/api/images/thumbnails/products/2024/01/01/uuid_filename_large.jpg"
    },
    "fileName": "uuid_filename.jpg",
    "fileSize": 102400,
    "imageType": "PRODUCT",
    "uploadTime": "2024-01-01 12:00:00"
  },
  "message": "图片上传成功"
}
```

### 上传新闻配图

- **URL**: `/image/upload/news`
- **方法**: POST
- **描述**: 上传新闻文章配图
- **权限要求**: 管理员权限

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 新闻配图文件 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "originalUrl": "/api/images/news/2024/01/01/uuid_filename.jpg",
    "thumbnailUrls": {
      "small": "/api/images/thumbnails/news/2024/01/01/uuid_filename_small.jpg",
      "medium": "/api/images/thumbnails/news/2024/01/01/uuid_filename_medium.jpg",
      "large": "/api/images/thumbnails/news/2024/01/01/uuid_filename_large.jpg"
    },
    "fileName": "uuid_filename.jpg",
    "fileSize": 102400,
    "imageType": "NEWS",
    "uploadTime": "2024-01-01 12:00:00"
  },
  "message": "图片上传成功"
}
```

### 上传横幅图片

- **URL**: `/image/upload/banner`
- **方法**: POST
- **描述**: 上传网站横幅或广告图片
- **权限要求**: 管理员权限

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 横幅图片文件 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "originalUrl": "/api/images/banners/2024/01/01/uuid_filename.jpg",
    "thumbnailUrls": {
      "small": "/api/images/thumbnails/banners/2024/01/01/uuid_filename_small.jpg",
      "medium": "/api/images/thumbnails/banners/2024/01/01/uuid_filename_medium.jpg",
      "large": "/api/images/thumbnails/banners/2024/01/01/uuid_filename_large.jpg"
    },
    "fileName": "uuid_filename.jpg",
    "fileSize": 102400,
    "imageType": "BANNER",
    "uploadTime": "2024-01-01 12:00:00"
  },
  "message": "图片上传成功"
}
```

### 通用图片上传

- **URL**: `/image/upload`
- **方法**: POST
- **描述**: 通用图片上传接口，可指定图片类型
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 图片文件 |
| type | string | 是 | 图片类型 (AVATAR, PRODUCT, NEWS, BANNER, OTHER) |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "originalUrl": "/api/images/other/2024/01/01/uuid_filename.jpg",
    "thumbnailUrls": {
      "small": "/api/images/thumbnails/other/2024/01/01/uuid_filename_small.jpg",
      "medium": "/api/images/thumbnails/other/2024/01/01/uuid_filename_medium.jpg",
      "large": "/api/images/thumbnails/other/2024/01/01/uuid_filename_large.jpg"
    },
    "fileName": "uuid_filename.jpg",
    "fileSize": 102400,
    "imageType": "OTHER",
    "uploadTime": "2024-01-01 12:00:00"
  },
  "message": "图片上传成功"
}
```

### 批量上传图片

- **URL**: `/image/upload/batch`
- **方法**: POST
- **描述**: 批量上传多张图片
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| files | file[] | 是 | 图片文件数组 |
| type | string | 是 | 图片类型 (AVATAR, PRODUCT, NEWS, BANNER, OTHER) |

#### 响应数据

```json
{
  "code": 0,
  "data": [
    {
      "originalUrl": "/api/images/other/2024/01/01/uuid_filename1.jpg",
      "thumbnailUrls": {
        "small": "/api/images/thumbnails/other/2024/01/01/uuid_filename1_small.jpg",
        "medium": "/api/images/thumbnails/other/2024/01/01/uuid_filename1_medium.jpg",
        "large": "/api/images/thumbnails/other/2024/01/01/uuid_filename1_large.jpg"
      },
      "fileName": "uuid_filename1.jpg",
      "fileSize": 102400,
      "imageType": "OTHER",
      "uploadTime": "2024-01-01 12:00:00"
    },
    {
      "originalUrl": "/api/images/other/2024/01/01/uuid_filename2.jpg",
      "thumbnailUrls": {
        "small": "/api/images/thumbnails/other/2024/01/01/uuid_filename2_small.jpg",
        "medium": "/api/images/thumbnails/other/2024/01/01/uuid_filename2_medium.jpg",
        "large": "/api/images/thumbnails/other/2024/01/01/uuid_filename2_large.jpg"
      },
      "fileName": "uuid_filename2.jpg",
      "fileSize": 204800,
      "imageType": "OTHER",
      "uploadTime": "2024-01-01 12:00:00"
    }
  ],
  "message": "批量图片上传成功"
}
```

### 删除图片

- **URL**: `/image/delete`
- **方法**: DELETE
- **描述**: 根据图片URL删除图片
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| imageUrl | string | 是 | 图片URL |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "图片删除成功"
}
```

### 批量删除图片

- **URL**: `/image/delete/batch`
- **方法**: DELETE
- **描述**: 批量删除多张图片
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| imageUrls | string[] | 是 | 图片URL数组 |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "批量图片删除成功"
}
```

## 图片管理接口

### 上传用户头像

- **URL**: `/image/upload/avatar`
- **方法**: POST
- **描述**: 用户上传个人头像图片
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 头像图片文件 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "originalUrl": "/api/images/avatars/2024/01/01/uuid_filename.jpg",
    "thumbnailUrls": {
      "small": "/api/images/thumbnails/avatars/2024/01/01/uuid_filename_small.jpg",
      "medium": "/api/images/thumbnails/avatars/2024/01/01/uuid_filename_medium.jpg",
      "large": "/api/images/thumbnails/avatars/2024/01/01/uuid_filename_large.jpg"
    },
    "fileName": "uuid_filename.jpg",
    "fileSize": 102400,
    "imageType": "AVATAR",
    "uploadTime": "2024-01-01 12:00:00"
  },
  "message": "图片上传成功"
}
```

### 上传商品图片

- **URL**: `/image/upload/product`
- **方法**: POST
- **描述**: 上传商品相关图片
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 商品图片文件 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "originalUrl": "/api/images/products/2024/01/01/uuid_filename.jpg",
    "thumbnailUrls": {
      "small": "/api/images/thumbnails/products/2024/01/01/uuid_filename_small.jpg",
      "medium": "/api/images/thumbnails/products/2024/01/01/uuid_filename_medium.jpg",
      "large": "/api/images/thumbnails/products/2024/01/01/uuid_filename_large.jpg"
    },
    "fileName": "uuid_filename.jpg",
    "fileSize": 102400,
    "imageType": "PRODUCT",
    "uploadTime": "2024-01-01 12:00:00"
  },
  "message": "图片上传成功"
}
```

### 上传新闻配图

- **URL**: `/image/upload/news`
- **方法**: POST
- **描述**: 上传新闻文章配图
- **权限要求**: 管理员权限

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 新闻配图文件 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "originalUrl": "/api/images/news/2024/01/01/uuid_filename.jpg",
    "thumbnailUrls": {
      "small": "/api/images/thumbnails/news/2024/01/01/uuid_filename_small.jpg",
      "medium": "/api/images/thumbnails/news/2024/01/01/uuid_filename_medium.jpg",
      "large": "/api/images/thumbnails/news/2024/01/01/uuid_filename_large.jpg"
    },
    "fileName": "uuid_filename.jpg",
    "fileSize": 102400,
    "imageType": "NEWS",
    "uploadTime": "2024-01-01 12:00:00"
  },
  "message": "图片上传成功"
}
```

### 上传横幅图片

- **URL**: `/image/upload/banner`
- **方法**: POST
- **描述**: 上传网站横幅或广告图片
- **权限要求**: 管理员权限

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 横幅图片文件 |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "originalUrl": "/api/images/banners/2024/01/01/uuid_filename.jpg",
    "thumbnailUrls": {
      "small": "/api/images/thumbnails/banners/2024/01/01/uuid_filename_small.jpg",
      "medium": "/api/images/thumbnails/banners/2024/01/01/uuid_filename_medium.jpg",
      "large": "/api/images/thumbnails/banners/2024/01/01/uuid_filename_large.jpg"
    },
    "fileName": "uuid_filename.jpg",
    "fileSize": 102400,
    "imageType": "BANNER",
    "uploadTime": "2024-01-01 12:00:00"
  },
  "message": "图片上传成功"
}
```

### 通用图片上传

- **URL**: `/image/upload`
- **方法**: POST
- **描述**: 通用图片上传接口，可指定图片类型
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | 是 | 图片文件 |
| type | string | 是 | 图片类型 (AVATAR, PRODUCT, NEWS, BANNER, OTHER) |

#### 响应数据

```json
{
  "code": 0,
  "data": {
    "originalUrl": "/api/images/other/2024/01/01/uuid_filename.jpg",
    "thumbnailUrls": {
      "small": "/api/images/thumbnails/other/2024/01/01/uuid_filename_small.jpg",
      "medium": "/api/images/thumbnails/other/2024/01/01/uuid_filename_medium.jpg",
      "large": "/api/images/thumbnails/other/2024/01/01/uuid_filename_large.jpg"
    },
    "fileName": "uuid_filename.jpg",
    "fileSize": 102400,
    "imageType": "OTHER",
    "uploadTime": "2024-01-01 12:00:00"
  },
  "message": "图片上传成功"
}
```

### 批量上传图片

- **URL**: `/image/upload/batch`
- **方法**: POST
- **描述**: 批量上传多张图片
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| files | file[] | 是 | 图片文件数组 |
| type | string | 是 | 图片类型 (AVATAR, PRODUCT, NEWS, BANNER, OTHER) |

#### 响应数据

```json
{
  "code": 0,
  "data": [
    {
      "originalUrl": "/api/images/other/2024/01/01/uuid_filename1.jpg",
      "thumbnailUrls": {
        "small": "/api/images/thumbnails/other/2024/01/01/uuid_filename1_small.jpg",
        "medium": "/api/images/thumbnails/other/2024/01/01/uuid_filename1_medium.jpg",
        "large": "/api/images/thumbnails/other/2024/01/01/uuid_filename1_large.jpg"
      },
      "fileName": "uuid_filename1.jpg",
      "fileSize": 102400,
      "imageType": "OTHER",
      "uploadTime": "2024-01-01 12:00:00"
    },
    {
      "originalUrl": "/api/images/other/2024/01/01/uuid_filename2.jpg",
      "thumbnailUrls": {
        "small": "/api/images/thumbnails/other/2024/01/01/uuid_filename2_small.jpg",
        "medium": "/api/images/thumbnails/other/2024/01/01/uuid_filename2_medium.jpg",
        "large": "/api/images/thumbnails/other/2024/01/01/uuid_filename2_large.jpg"
      },
      "fileName": "uuid_filename2.jpg",
      "fileSize": 204800,
      "imageType": "OTHER",
      "uploadTime": "2024-01-01 12:00:00"
    }
  ],
  "message": "批量图片上传成功"
}
```

### 删除图片

- **URL**: `/image/delete`
- **方法**: DELETE
- **描述**: 根据图片URL删除图片
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| imageUrl | string | 是 | 图片URL |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "图片删除成功"
}
```

### 批量删除图片

- **URL**: `/image/delete/batch`
- **方法**: DELETE
- **描述**: 批量删除多张图片
- **权限要求**: 需要登录

#### 请求参数

| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| imageUrls | string[] | 是 | 图片URL数组 |

#### 响应数据

```json
{
  "code": 0,
  "data": true,
  "message": "批量图片删除成功"
}
```