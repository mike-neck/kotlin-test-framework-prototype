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
    val before: BeforeTestListener<C>,
    val after: AfterTestListener<C, P>,
    val condition: C.() -> P
) : Test {

  private val conditionName: String =
      if (title != null) "Given $title"
      else this::class.simpleName ?: this::class.java.simpleName ?: "Given"

  private val checks: MutableList<Check> = mutableListOf()
  override val all: Iterable<Check> get() = checks.toList()


  inner class When<T>(name: String? = null, private val operation: C.(P) -> T) {

    private val operationName: String =
        if (name != null) "When $name"
        else this::class.simpleName ?: this::class.java.simpleName ?: "When"

    private val count: Array<Int> = arrayOf(1)

    private fun String?.name(): String = 
        this?.let { "Then $it" }
            ?.also { count[0]++ } 
            ?: "test-${count[0]++}" 

    @Suppress("FunctionName")
    fun Then(explain: String? = null, assertion: C.(P, T) -> AssertionResult): Given<C, P> =
        this@Given.also {
          it.checks.add(
              Execution(
                  before,
                  after,
                  condition,
                  operation,
                  assertion,
                  "$conditionName, $operationName, ${explain.name()}"))
        }
  }

  companion object {
    operator fun <P : Any> invoke(title: String? = null, condition: () -> P): Given<Unit, P> =
        Given(title, DefaultBeforeTestListener, defaultAfterListener(), { condition() })
  }
}
