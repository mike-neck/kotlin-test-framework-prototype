/*
 * Copyright 2020 Shinya Mochida
 * 
 * Licensed under the Apache License,Version2.0(the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,software
 * Distributed under the License is distributed on an"AS IS"BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse

class GivenTest {

  @org.junit.jupiter.api.Test
  fun givenThenWhen() {
    val test = Given { 1 }
        .When { it + 1 }
        .Then { _, result -> result shouldBe 2 }

    val testResult = test.performAll()

    assertEquals(emptyList<CheckResult>(), testResult.failedTests())
    assertEquals(
        "Given/When/test-1",
        testResult.successTests().first().name)
  }

  @org.junit.jupiter.api.Test
  fun failTest() {
    val test: Test = Given { 1 }
        .When { it + 2 }
        .Then { _, result -> result shouldBe 4 }

    val result = test.performAll()

    assertFalse(result.success())
    assertEquals(1, result.failedTests().toList().size)
  }

  @org.junit.jupiter.api.Test
  fun beforeFilter() {
    val test: Test =
        Given(title = "map(one:1,two:2).get(one)?100",
            before = { mapOf("one" to 1, "two" to 2) }, 
            after = {}, condition = { this["one"] ?: 10 })
            .When(" + get(two)?200") { p -> p + (get("two") ?: 200) }
            .Then(" = 3") { _, t -> t shouldBe 3 }
            .When(" + get(three)") { p -> get("three")?.plus(p) }
            .Then("= 4") { _, t -> t shouldBe 4 }

    val result = test.performAll()
    println(result)
    assertFalse(result.success())
    assertEquals(1, result.failedTests().toList().size)
  }
  
  companion object {
    infix fun Any?.shouldBe(expected: Any): AssertionResult =
        if (this == expected) Success
        else AssertFail(this, expected)

    fun Any?.shouldBeNull(): AssertionResult =
        if (this == null) Success
        else Impediments.failed("expected nul but notNull($this)").asAssertionResult()
  }
}
