---
spec_id: configured-codec-benchmark-contract
work_unit_id: configured-codec-benchmark-v1
title: Configured codec runtime benchmark and unsafe-view contract
status: confirmed
version: 1.0
updated_at: 2026-08-23
confirmed_at: 2026-08-23
owner: dev
reference_version: b6b53e31
source_artifacts:
  - src/main/java/org/fz/nettyx/serializer/configured/ConfiguredSerializer.java
  - src/main/java/org/fz/nettyx/serializer/configured/ConfigStructView.java
  - src/test/java/org/fz/nettyx/serializer/configured/ConfiguredSerializerTest.java
evidence:
  - Current performance loop only binds a ConfigStructView and skips the frame length.
  - Actual runtime flow is XML loading at startup followed by named-struct deserialization and serialization.
confirmation_evidence:
  - User confirmed the API visibility and benchmark contract in the current conversation on 2026-08-23.
impact_chain:
  - StructConfigRegistry -> cached ConfiguredSerializer -> ConfiguredSerializer.toStruct/toByteBuf
  - ConfiguredSerializer -> ConfigStructView -> ByteBuf lifecycle
unverified_scope:
  - External consumers of the public viewIntoUnchecked API.
open_questions: []
next_action: Implement the confirmed benchmark and API visibility changes.
---

## 目标与非目标

目标：使性能基准代表实际通信期的完整编解码，且避免公开的无校验视图 API 被误用于生产代码。

非目标：不改变 XML 配置格式、字段编解码语义或现有安全的 `viewInto` API。

## 变更范围

### 新增

- 为 122 字节 `device.BenchmarkDevice` 提供完整反序列化及序列化基准。

### 修改

- 将现有百万次测试改为在 XML 已加载的前提下，分别测量完整接收与发送路径。
- JMH 使用相同的 122 字节基准结构。

### 废弃

- `ConfiguredSerializer#viewIntoUnchecked` 不再是公共 API，改为包内基准辅助能力。

## 方案、取舍与决策

- 注册表继续在启动期加载 XML；通信期通过 `toStruct` / `toByteBuf` 使用缓存的序列化器。
- 完整 Map 编解码是业务性能指标；零拷贝视图仅作为内部低层能力，不作为该指标。
- 保留 `viewInto` 的完整校验语义，以支持固定长度、按需读取场景。

## 影响链、兼容与恢复

内部调用不受影响。外部调用方若直接调用 `viewIntoUnchecked`，编译时将无法访问，需迁移到 `viewInto`。

恢复：如确认有外部消费者需要该能力，可恢复公共 API，但必须加入显式的危险标记或受控调用契约；该恢复不涉及数据迁移。

## API 契约

### 接口列表

- 保持：`toStruct`、`toByteBuf`、`viewInto`。
- 行为变化：`viewIntoUnchecked` 由 public 收窄为包内可见。

### 请求/响应模型

不涉及 HTTP 或 RPC；输入/输出仍为 `ByteBuf` 与 `Map<String, Object>`。

### 错误码

不涉及；安全 `viewInto` 仍对字节不足和视图归属不匹配抛出既有异常。

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

基准不得包含 XML 加载；接收、发送分别报告，使用相同的 122 字节结构和固定输入。

### 安全

不得向外部调用方暴露会跳过视图归属和帧完整性校验的 API。

### 兼容性

`viewIntoUnchecked` 是破坏性收窄；确认无外部消费者后执行。

### 可观测性

基准输出应明确标注“完整反序列化”或“完整序列化”，避免与视图绑定吞吐混淆。

## 任务拆分

1. 收窄 `viewIntoUnchecked` 可见性。
2. 更新百万次测试和 JMH 基准为 122 字节完整编解码。
3. 将实现交回复审与测试。

## 验收标准

- 性能循环中实际调用完整 `toStruct` 或 `toByteBuf`。
- 基准输入为 122 字节 `BenchmarkDevice`，且 XML 加载在计时外。
- 外部包不能调用 `viewIntoUnchecked`；安全 `viewInto` 保持可用。

## 风险与依赖

- 风险：外部二进制或源码消费者可能依赖 `viewIntoUnchecked`。
- 依赖：用户确认该公共 API 收窄与基准契约。
