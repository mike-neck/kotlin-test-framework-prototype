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

class GivenTest {

  @org.junit.jupiter.api.Test
  fun givenThenWhen() {
    val test = Given { 1 }
        .When { it + 1 }
        .Then { _, result -> result shouldBe 2 }
    val failures = test.all
        .map { it.perform() }
        .mapNotNull { it.impediments() }
    assertEquals(0,failures.size)
  }

  @org.junit.jupiter.api.Test
  fun failTest() {
    val test: Test = Given { 1 }
        .When { it + 2 }
        .Then { _, result -> result shouldBe 4 }
    val result: Iterable<CheckResult> = test.performAll()
    val failed = result.mapNotNull { it.impediments() }
    assertEquals(1, failed.size)
  }

  companion object {
    infix fun Any.shouldBe(expected: Any): AssertionResult =
        if (this == expected) Success
        else AssertFail(this, expected)
  }
}
