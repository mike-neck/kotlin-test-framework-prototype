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

typealias BeforeTestListener<T> = () -> T

private object DefaultBeforeTestListener : BeforeTestListener<Unit> {
  override fun invoke() = Unit
}

typealias AfterTestListener<C, P> = C.(P) -> Unit

private fun <C, P> defaultAfterListener(): AfterTestListener<C, P> = {}

class Given<C : Any, P : Any>(
    title: String? = null,
    val beforeListener: BeforeTestListener<C>,
    val afterListener: AfterTestListener<C, P>,
    val condition: C.() -> P
) : Test {

  val conditionName: String = title ?: this::class.simpleName ?: this::class.java.simpleName

  private val checks: MutableList<Check> = mutableListOf()
  override val all: Iterable<Check> get() = checks.toList()

  inner class When<T>(name: String? = null, private val operation: C.(P) -> T) {

    val operationName: String = name ?: this::class.simpleName ?: this::class.java.simpleName

    @Suppress("FunctionName")
    fun Then(explain: String? = null, assertion: C.(P, T) -> AssertionResult): Given<C, P> =
        this@Given.also {
          it.checks.add(
              Execution(
                  beforeListener,
                  afterListener,
                  condition,
                  operation,
                  assertion,
                  "given: $conditionName, when: $operationName, then: $explain"))
        }
  }

  companion object {
    operator fun <P : Any> invoke(title: String = "", condition: () -> P): Given<Unit, P> =
        Given(title, DefaultBeforeTestListener, defaultAfterListener(), { condition() })
  }
}
