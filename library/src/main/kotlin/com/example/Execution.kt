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
import java.time.Instant

class Execution<C : Any, P : Any, T>(
    val beforeListener: BeforeTestListener<C>,
    val afterListener: AfterTestListener<C, P>,
    val condition: C.() -> P,
    val operation: C.(P) -> T,
    val assertion: C.(P, T) -> AssertionResult,
    override val description: String
) : Check {

  fun Instant.stop(): Duration = Duration.between(this, Instant.now())

  override fun perform(): CheckResult =
      ExecutionTimeMeasuring(Instant.now())("beforeListener") {
        beforeListener()
      }("given") {
        GivenContext(it, it.condition())
      }("when") {
        it.accept(operation)
      }("then") {
        it.accept(assertion)
      }("afterListener") {
        it.accept(afterListener)
      }.mapWithStartTime { instant, assertionResult ->
        assertionResult.toCheckResult(description, instant.stop())
      }.rescue { it.second.toCheckResult(description, it.first.stop()) }
}

data class ExecutionTimeMeasuring<T : Any>(
    val start: Instant,
    val context: Either<Impediments, T>
) {

  operator fun <R : Any> invoke(step: String, function: (T) -> R): ExecutionTimeMeasuring<R> =
      ExecutionTimeMeasuring(
          start,
          context.andThen(action(step, function))
      )

  operator fun <F : Any, S : Any, R : Any> ((F, S) -> R).invoke(f: F): (S) -> R = { s -> this(f, s) }

  fun <R : Any> mapWithStartTime(mapping: (Instant, T) -> R): Either<Pair<Instant, Impediments>, R> =
      context.map(mapping(start)).errorMap { start to it }

  companion object {
    operator fun invoke(start: Instant): ExecutionTimeMeasuring<Unit> =
        ExecutionTimeMeasuring(start, Either.right(Unit))
  }
}

data class GivenContext<C : Any, P : Any>(
    val context: C,
    val provided: P
) {
  inline fun <T> accept(operation: C.(P) -> T): TestingContext<C, P, T> =
      TestingContext(context, provided, context.operation(provided))
}

data class TestingContext<C : Any, P : Any, T>(
    val context: C,
    val provided: P,
    val target: T
) {
  inline fun accept(assertion: C.(P, T) -> AssertionResult): ClosingContext<C, P> =
      ClosingContext(context, provided, context.assertion(provided, target))
}

data class ClosingContext<C : Any, P : Any>(
    val context: C,
    val provided: P,
    val result: AssertionResult
) {
  inline fun accept(afterTestListener: AfterTestListener<C, P>): AssertionResult =
      result.also { context.afterTestListener(provided) }
}

private interface Action<in T : Any, N : Any> {
  fun execute(previous: T): Either<Impediments, N>
}

private fun <T : Any, N : Any> action(step: String, function: (T) -> N): Action<T, N> = object : Action<T, N> {
  override fun execute(previous: T): Either<Impediments, N> =
      try {
        Either.right(function(previous))
      } catch (th: Throwable) {
        Either.left(Impediments(step, th))
      }
}

private infix fun <T : Any, N : Any> Either<Impediments, T>.andThen(
    action: Action<T, N>): Either<Impediments, N> = this.flatMap { action.execute(it) }
