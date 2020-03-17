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

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import io.github.classgraph.ScanResult
import org.junit.platform.engine.EngineDiscoveryRequest
import org.junit.platform.engine.UniqueId
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

class ClasspathScanner(
    private val discoveryRequest: EngineDiscoveryRequest,
    private val uniqueId: UniqueId
) {

  companion object {
    private val logger = LoggerFactory.getLogger(ClasspathScanner::class.java)

    private fun <T : Any, R : Any> Pair<T, R?>.logIfNull(): R? =
        this.second.also { if (this.second == null) logger.info("{} is null", this.first) }

    private operator fun <R : Any, T : Any> Either<Throwable, R>.invoke(operationName: String, operation: (R) -> T): Either<Throwable, T> =
        kotlin.runCatching { this.map(operation) }
            .getOrElse { Either.left(IllegalStateException("error on $operationName", it)) }

    private fun Throwable.toIllegalStateException(): IllegalStateException =
        when (this) {
          is IllegalStateException -> this
          else -> when (val c = this.cause) {
            null -> IllegalStateException(this)
            else -> IllegalStateException(this.message, c)
          }
        }
  }

  fun scanTests(): Either<IllegalStateException, TestableCollection> =
      runCatching {
        ClassGraph()
            .enableClassInfo()
            .blacklistClasses(Test::class.java.canonicalName)
            .scan()
            .use<ScanResult, Either<IllegalStateException, TestableCollection>> { scan ->
              Either.right<Throwable, Iterable<ClassInfo>>(
                  scan.getClassesImplementing("com.example.Test")
              )("load classes") { list ->
                @Suppress("UNCHECKED_CAST")
                list.map { info -> info.loadClass().kotlin as KClass<Test> }
              }("retrieve object instance") { list ->
                list.mapNotNull { (it to it.objectInstance).logIfNull() }
              }("create Testable") { list ->
                list.map { Testable(it) }
              }("create TestableCollection") {
                TestableCollection(uniqueId, it)
              }.errorMap { it.toIllegalStateException() }
            }
      }.getOrElse {
        when (it) {
          is OutOfMemoryError -> throw it
          is IllegalStateException -> throw it
          else -> throw IllegalStateException("scan error", it)
        }
      }
}

data class Testable(val test: Test)
