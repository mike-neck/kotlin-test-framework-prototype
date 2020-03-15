package com.example

interface Test {
  fun performAll(): TestResult = TestResult(all.map { it.perform() })
  val all: Iterable<Check>
}

data class TestResult(
    val results: Iterable<CheckResult>
) {
  fun success(): Boolean = results.all { it.success }

  fun failedTests(): Iterable<CheckResult> = results.filter { it.success.not() }

  fun successTests(): Iterable<CheckResult> = results.filter { it.success }
}
