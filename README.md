kotlin-test-framework-prototype
---

テストフレームワークのプロトタイプ

概要
---

こんな感じのテストフレームワークを作ろうとしてる

```kotlin
object AddTest: Test
  by Given("1 に", { 1 })
      .When("1 を足すと", { it + 1 })
      .Then("2 になる", { _, result -> result becomes 2 })
      .When("2 を足すと", { it + 2 })
      .Then("3 になる", { _, result -> result becomes 3 })
```

requirements
---

- junit-jupiter で動かせる
- ２つの形式をサポート
    - given-when-then 形式
    - property based testing も可能
- Spring Framework のテストも書ける

