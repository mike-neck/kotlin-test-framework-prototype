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

import java.time.Instant

typealias BeforeTestListener<T> = () -> T

private object DefaultBeforeTestListener : BeforeTestListener<Unit> {
  override fun invoke() = Unit
}

typealias AfterTestListener<C, P> = C.(P) -> Unit

private fun <C, P> defaultAfterListener(): AfterTestListener<C, P> = {}

class Given<C : Any, P : Any>(
    val title: String = "",
    val beforeListener: BeforeTestListener<C>,
    val afterListener: AfterTestListener<C, P>,
    val condition: C.() -> P
) : Test {
  private val checks: MutableList<Check> = mutableListOf()
  override val all: Iterable<Check> get() = checks.toList()

  inner class When<T>(val name: String, val operation: C.(P) -> T) {
    fun Then(explain: String, assertion: C.(P, T) -> AssertionResult): Given<C, P> =
        TODO()
//        this@Given.also { it.checks.add(Execution("given: $title, when: $name, then: $explain")) }
  }

  companion object {
    operator fun <P : Any> invoke(title: String = "", condition: () -> P): Given<Unit, P> =
        Given(title, DefaultBeforeTestListener, defaultAfterListener(), { condition() })
  }
}

class Execution<C: Any, P: Any, T: Any>(
    val beforeListener: BeforeTestListener<C>,
    val afterListener: AfterTestListener<C, P>,
    val condition: C.() -> P,
    val operation: C.(P) -> T,
    val assertion: C.(P, T) -> AssertionResult,
    override val description: String
) : Check {
  private operator fun <F: Any, T: Any, R: Any> Pair<F, Either<Impediments, T>>.invoke(
      step: String,
      function: (T) -> R): Pair<F, Either<Impediments, R>> = 
      this.first to this.second.andThen(action(step, function))

  override fun perform(): CheckResult = TODO()
//      (Instant.now() to Either.right<Impediments, Unit>(Unit)) {
//        beforeListener()
//      } {  }
}


private interface Action<in T: Any, N: Any> {
  fun execute(previous: T): Either<Impediments, N>
}

private fun <T: Any, N: Any> action(step: String, function: (T) -> N): Action<T, N> = object : Action<T, N> {
  override fun execute(previous: T): Either<Impediments, N> =
      try {
        Either.right(function(previous))
      } catch (th: Throwable) {
        Either.left(Impediments(step, th))
      }
}

private infix fun <T : Any, N : Any> Either<Impediments, T>.andThen(
    action: Action<T, N>): Either<Impediments, N> = this.flatMap { action.execute(it) }
