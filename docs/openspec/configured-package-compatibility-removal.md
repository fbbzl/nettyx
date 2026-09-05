---
spec_id: configured-package-compatibility-removal
work_unit_id: configured-package-compatibility-removal-v1
title: Remove deprecated configured serializer compatibility facades
status: confirmed
version: 1.0
updated_at: 2026-08-24
confirmed_at: 2026-08-24
owner: dev
reference_version: c7bb97f6
source_artifacts:
  - src/main/java/org/fz/nettyx/serializer/configured/BasicTypeResolver.java
  - src/main/java/org/fz/nettyx/serializer/configured/XmlStructConfigParser.java
evidence:
  - Both root-package classes are deprecated facades over the type and parser packages.
confirmation_evidence:
  - User explicitly requested that the @Deprecated classes be deleted in the current conversation on 2026-08-24.
impact_chain:
  - External source consumers using the root-package facades must migrate to configured.type.BasicTypeResolver or configured.parser.XmlStructConfigParser.
  - Internal production and test sources already import the new packages.
unverified_scope:
  - Downstream projects compiled against the removed root-package classes.
open_questions: []
next_action: Remove both compatibility facades and verify repository references.
---

## 目标与非目标

本文记录 `reference_version` 对应的历史变更，旧路径按当时版本保留。后续包迁移将 `serializer.configured` 改为 `serializer.schema`；当前替代类位于 `schema.type.BasicTypeResolver` 和 `schema.parser.XmlStructConfigParser`。

目标：删除已废弃的根包兼容类，完成 `configured` 包结构迁移。

非目标：不改变实际类型解析、XML 解析和编解码行为。

## 变更范围

### 新增

- 不涉及。

### 修改

- 不涉及；内部调用已使用新包路径。

### 废弃

- 删除 `org.fz.nettyx.serializer.configured.BasicTypeResolver`。
- 删除 `org.fz.nettyx.serializer.configured.XmlStructConfigParser`。

## 方案、取舍与决策

不保留二次转发层。用户明确选择立即移除兼容 API；保留会使扁平包继续暴露实现细节。

## 影响链、兼容与恢复

这是源码和二进制不兼容变更。消费者分别迁移至 `configured.type.BasicTypeResolver` 与 `configured.parser.XmlStructConfigParser`。

恢复：从版本控制恢复两个 facade 类即可，无数据迁移。

## API 契约

### 接口列表

- 删除两个已废弃的根包公共类；替代 API 已存在于子包。

### 请求/响应模型

不涉及 HTTP 或 RPC。

### 错误码

不涉及。

### 幂等性

不涉及。

### 权限

不涉及。

## 数据库变更

### 表结构

不涉及。

### 索引

不涉及。

### 迁移脚本

不涉及。

### 回滚方案

不涉及。

### 数据校验

不涉及。

## 非功能要求

### 性能

不涉及；移除 facade 不改变运行时编解码路径。

### 安全

不涉及。

### 兼容性

外部调用旧包路径将于编译或运行链接时失败，需迁移到子包路径。

### 可观测性

不涉及。

## 任务拆分

1. 删除两个 facade 类。
2. 检查仓库内旧包路径引用并执行构建验证。

## 验收标准

- 根包不再包含已废弃的 facade 类。
- 仓库内不再引用其完整旧包路径。
- Maven 构建成功。

## 风险与依赖

- 风险：未在本仓库构建的下游消费者需要同步迁移。
- 依赖：用户已确认立即删除兼容 API。
