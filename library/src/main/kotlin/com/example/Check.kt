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
  val executionTime: Duration
  fun impediments(): Impediments?
}

interface Check {
  val description: String
  fun perform(): CheckResult
}

interface AssertionResult

object Success: AssertionResult

data class AssertFail(
    val actual: Any,
    val expected: Any
): AssertionResult

interface Test {
  val all: Iterable<Check>
}
