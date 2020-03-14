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

@Component
@Import([
  RepositoryConfig::class
])
class TestConfig(val userRepository: UserRepository)

object SpringAppTest: Test
  by Given(
        title = "spring context つける",
        listenerContext = withSpringConfig(TestConfig::class),
        beforeListener = BeforeSpringUserRepositoryListener,
        afterListener = { _: TestConfig -> delete(100) },
        givenContext = { save(UserEntity(100, "Alfred")).id })
      .When("id でデータを取り出す", { id -> findById(id) })
      .Then("ユーザーが見つかる", { _, user -> all (
          { user.id becomes 100 },
          { user.name shouldBe "Alfred" }
        ) })

object BeforeSpringUserRepositoryListener: BeforeTestListener<TestConfig, UserRepository> {
  override fun prepare(testConfig: TestConfig): UserRepository = testConfig.userRepository
}
```

requirements
---

- junit-jupiter で動かせる
- ２つの形式をサポート
    - given-when-then 形式
    - property based testing も可能
- `beforeListener`/`afterListener` をつける
- `mockProvider` をつける
- Spring Framework のテストも書ける

