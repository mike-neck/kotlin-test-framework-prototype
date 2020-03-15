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

import org.opentest4j.AssertionFailedError
import org.opentest4j.TestAbortedException
import org.opentest4j.TestSkippedException

sealed class Impediments(
    val tag: String = "",
    open val original: Throwable) : RuntimeException(original) {

  companion object {

    fun aborted(name: String, tag: String): Impediments = invoke(tag, TestAbortedException("test[$name] aborted on $tag"))
    fun skipped(name: String, tag: String): Impediments = invoke(tag, TestSkippedException("test[$name] skipped on $tag"))
    fun failed(name: String): Impediments = invoke("test", AssertionFailedError("test[$name] failed"))
    fun failed(name: String, expected: Any, actual: Any?): Impediments = invoke("test", AssertionFailedError("test[$name] failed", expected, actual))

    operator fun invoke(tag: String, cause: Throwable): Impediments = when (cause) {
      is TestAbortedException -> AbortedException(tag, cause)
      is AbortedException -> AbortedException("$tag/${cause.tag}", cause.original)
      is TestSkippedException -> SkippedException(tag, cause)
      is SkippedException -> SkippedException("$tag/${cause.tag}", cause.original)
      is AssertionFailedError -> AssertionFailureException(tag, cause)
      is AssertionFailureException -> AssertionFailureException("$tag/${cause.tag}", cause.original)
      else -> AbortedException(tag, TestAbortedException("execution aborted on $tag", cause))
    }
  }

  class AbortedException(
      tag: String, override val original: TestAbortedException) : Impediments(tag = tag, original = original)
  class SkippedException(
      tag: String, override val original: TestSkippedException) : Impediments(tag = tag, original = original)
  class AssertionFailureException(
      tag: String, override val original: AssertionFailedError) : Impediments(tag = tag, original = original)
}
