# 发布 nettyx 和 spring-boot-starter-nettyx

本 skill 描述从本地 `release` 分支发布 `nettyx` 及其 starter 到 Maven Central 的完整流程。

## 前置条件

- 当前必须位于 `D:\workspace\nettyx` 的 `release` 分支上。
- 发布前的所有功能改动已合并到 `release` 分支。
- `README.md` 和 `README_zh.md` 中的 `<version>` 需要与发布版本保持一致。
- Maven 配置可用：`D:\maven\apache-maven-3.9.14\bin\mvn.cmd` 和 `D:\maven\settings.xml`。
- GPG 签名和 OSSRH 凭据已配置在 `settings.xml` 中。

## 版本规则

- `nettyx` 版本：当前版本最后一位加 1。
  - 例：`2.6.25` → `2.6.26`。
- `spring-boot-starter-nettyx` 版本：由 `nettyx.version` 和 `spring-boot.version` 组合而成。
  - 例：`nettyx.version=2.6.26`，`spring-boot.version=3.5.15` → 项目版本 `2.6.26_3.5.15`。

## 发布步骤

### 1. 合并功能改动到 release

如果功能改动在 `feature` 分支，先合并到 `release`：

```bash
git checkout release
git merge --no-ff feature -m "feat(update): 2.6.26"
```

若出现冲突，通常以 feature 分支内容为准：

```bash
git checkout --theirs <conflicted-file>
git add .
git commit -m "feat(update): 2.6.26"
```

### 2. 升级 nettyx 版本

`nettyx/pom.xml` 使用 `${revision}` 属性，修改 `<revision>`：

```xml
<revision>2.6.26</revision>
```

同时更新 `README.md` 和 `README_zh.md` 中的版本号：

```xml
<version>2.6.26</version>
```

提交版本升级：

```bash
git add -A
git commit -m "chore release: nettyx 2.6.26"
```

### 3. 推送 release 到远程

```bash
git push gitee release
git push github release
```

### 4. 部署 nettyx

```bash
"D:\maven\apache-maven-3.9.14\bin\mvn.cmd" clean deploy -s "D:\maven\settings.xml" -Dmaven.repo.local=D:\maven\repository -DskipTests
```

成功后，jar、pom、sources、javadoc 会发布到 Maven Central。

### 5. 升级 spring-boot-starter-nettyx

切换到 `D:\workspace\spring-boot-starter-nettyx`，确认当前在 `main` 分支：

```bash
git -C "D:\workspace\spring-boot-starter-nettyx" branch --show-current
```

修改 `pom.xml` 中的 `nettyx.version`：

```xml
<nettyx.version>2.6.26</nettyx.version>
```

提交并推送：

```bash
git -C "D:\workspace\spring-boot-starter-nettyx" add -A
git -C "D:\workspace\spring-boot-starter-nettyx" commit -m "chore release: spring-boot-starter-nettyx 2.6.26_3.5.15"
git -C "D:\workspace\spring-boot-starter-nettyx" push gitee main
git -C "D:\workspace\spring-boot-starter-nettyx" push github main
```

### 6. 部署 spring-boot-starter-nettyx

```bash
"D:\maven\apache-maven-3.9.14\bin\mvn.cmd" -f "D:\workspace\spring-boot-starter-nettyx\pom.xml" clean deploy -s "D:\maven\settings.xml" -Dmaven.repo.local=D:\maven\repository -DskipTests
```

## 同步 main 分支

nettyx 发布完成后，还需将 `release` 分支同步到 `main`：

```bash
git checkout release
git push gitee release
git push github release

git checkout main
git merge --no-ff release -m "chore release: nettyx 2.6.26"
git push gitee main
git push github main
```

## 验证

- 检查远程仓库 `release` 和 `main` 分支的最新提交。
- 在 Maven Central 搜索 `io.github.fbbzl:nettyx:2.6.26` 和 `io.github.fbbzl:spring-boot-starter-nettyx:2.6.26_3.5.15`。

## 注意事项

- 不要在 `feature` 分支上直接执行发布。
- 发布前确保测试通过（本流程使用 `-DskipTests` 跳过测试，发布前应单独运行 `mvn test` 确认）。
- 如果 Maven Central 发布失败，检查 GPG 签名和 OSSRH 凭据。
- 如果 `release` 分支有本地未提交改动，先提交或清理。

## 历史提交信息参考

- 功能合并：`feat(update): 2.6.26`
- 版本升级：`chore release: nettyx 2.6.26`
- starter 升级：`chore release: spring-boot-starter-nettyx 2.6.26_3.5.15`
