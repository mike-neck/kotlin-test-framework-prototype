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

import org.junit.platform.engine.EngineDiscoveryRequest
import org.junit.platform.engine.ExecutionRequest
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestEngine
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.UniqueId
import org.slf4j.LoggerFactory

typealias ScannerFactory = (EngineDiscoveryRequest, UniqueId) -> ClasspathScanner

class KotlinTestEngine(val scannerFactory: ScannerFactory) : TestEngine {

  constructor() : this({ request, uniqueId -> ClasspathScanner(request, uniqueId) })

  companion object {
    private val logger = LoggerFactory.getLogger(KotlinTestEngine::class.java)

    private fun <T : Any> T?.orIllegalArgumentException(message: String): Either<IllegalArgumentException, T> =
        if (this == null) Either.left(IllegalArgumentException(message))
        else Either.right(this)

    private fun <E : Throwable> errorLog(operationName: String, error: E): Throwable =
        error.also { logger.info("error when {}", operationName, it) }

    private fun <T : Any, E : Throwable> Either<E, T>.log(operationName: String): Either<Throwable, T> =
        this.errorMap { errorLog(operationName, it) }

    private operator fun <T : Any, R : Any, E : Throwable>
        Either<Throwable, T>.invoke(operationName: String, operation: (T) -> Either<E, R>): Either<Throwable, R> =
        this.flatMap { operation(it).log(operationName) }

    private fun <T : Any> Either<Throwable, T>.throwError(): T = this.rescue { throw it }
  }

  override fun discover(discoveryRequest: EngineDiscoveryRequest?, uniqueId: UniqueId?): TestDescriptor =
      Either.right<Throwable, Unit>(Unit)("null check for uniqueId") {
        uniqueId.orIllegalArgumentException("uniqueId is null")
      }("null check for discoveryRequest") { id ->
        discoveryRequest.orIllegalArgumentException("discoveryRequest is null").map { it to id }
      }.map { scannerFactory(it.first, it.second) }("scan classpath") { scanner ->
        scanner.scanTests()
      }.throwError()

  override fun getId(): String = "k-check"

  override fun execute(request: ExecutionRequest?) =
      when (request?.rootTestDescriptor) {
        null -> throw IllegalArgumentException("request is null")
        is Executable<*> -> (request.rootTestDescriptor as Executable<*>).executeTests(
            onTestStart = { 
              request.engineExecutionListener.executionStarted(it) },
            onTestSucceeded = { 
              request.engineExecutionListener.executionFinished(it, TestExecutionResult.successful()) },
            onTestFailed = { desc, failure -> 
              request.engineExecutionListener.executionFinished(desc, TestExecutionResult.failed(failure)) },
            onTestError = { desc, error ->
              request.engineExecutionListener.executionFinished(desc, TestExecutionResult.aborted(error)) },
            onTestSkipped = { desc, skipped ->
              request.engineExecutionListener.executionSkipped(desc, "${skipped.message} on ${skipped.tag}") }
        )
        else -> Unit
      }
}
