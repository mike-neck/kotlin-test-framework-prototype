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

import java.time.Duration

interface CheckResult {
  val name: String
  val executionTime: Duration
  fun impediments(): Impediments?
  val success: Boolean get() = impediments() == null
}

interface Check {
  val description: String
  fun perform(): CheckResult
}

interface AssertionResult {
  fun toCheckResult(name: String, executionTime: Duration): CheckResult
}

object Success: AssertionResult {
  override fun toCheckResult(name: String, executionTime: Duration): CheckResult =
      object : CheckResult {
        override val name: String = name 
        override fun toString(): String = "CheckResult[$name=SUCCESS]"
        override val executionTime: Duration get() = executionTime
        override fun impediments(): Impediments? = null
      }
}

data class AssertFail(
    val actual: Any?,
    val expected: Any
): AssertionResult {
  override fun toCheckResult(name: String, executionTime: Duration): CheckResult =
      object : CheckResult {
        override val name: String = name
        override fun toString(): String = "CheckResult[$name=FAILURE]"
        override val executionTime: Duration get() = executionTime
        override fun impediments(): Impediments? = Impediments.failed(name, expected, actual)
      }
}

fun Impediments.toCheckResult(name: String, executionTime: Duration): CheckResult =
    object : CheckResult {
      override val name: String = name
      override fun toString(): String = "CheckResult[$name=ERROR(${this@toCheckResult::class.simpleName})]"
      override val executionTime: Duration get() = executionTime
      override fun impediments(): Impediments? = this@toCheckResult
    }

fun Impediments.asAssertionResult(): AssertionResult = object : AssertionResult {
  override fun toCheckResult(name: String, executionTime: Duration): CheckResult = this@asAssertionResult.toCheckResult(name, executionTime)
}
