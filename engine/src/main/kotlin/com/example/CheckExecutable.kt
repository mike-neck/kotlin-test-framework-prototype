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

import com.example.Executable.Companion.invoke
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestSource
import org.junit.platform.engine.TestTag
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.support.descriptor.MethodSource
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*

data class CheckExecutable(
    val parentDescriptor: TestDescriptor,
    val containerTest: Test,
    val check: Check
): TestDescriptor, Executable<CheckExecutable> {

  private val logger = LoggerFactory.getLogger(CheckExecutable::class.java)

  private inline fun CheckResult.success(action: CheckResult.(String, Duration) -> Unit): CheckResult =
      if (this.success) this.also { action(this.name, this.executionTime) } else this

  private inline fun CheckResult.assertionFailure(
      action: Impediments.AssertionFailureException.(String, Duration) -> Unit): CheckResult = 
      when (val e = this.impediments()) {
        null -> this
        is Impediments.AssertionFailureException -> this.also { e.action(this.name, this.executionTime) }
        else -> this
      }

  private inline fun CheckResult.testSkipped(action: Impediments.SkippedException.(String) -> Unit): CheckResult =
      when (val e = this.impediments()) {
        null -> this
        is Impediments.SkippedException -> this.also { e.action(this.name) }
        else -> this
      }

  private inline fun CheckResult.testAborted(action: Impediments.AbortedException.(String) -> Unit): CheckResult =
      when (val e = this.impediments()) {
        null -> this
        is Impediments.AbortedException -> this.also { e.action(this.name) }
        else -> this
      }

  override fun executeTests(
      onTestStart: (TestDescriptor) -> Unit,
      onTestSucceeded: (TestDescriptor) -> Unit,
      onTestFailed: (TestDescriptor, Impediments.AssertionFailureException) -> Unit,
      onTestSkipped: (TestDescriptor, Impediments.SkippedException) -> Unit,
      onTestError: (TestDescriptor, Throwable) -> Unit) =
      onTestStart(this) () { logger.info("check start") }
          .let { check.perform() }
          .success { name, duration -> onTestSucceeded(this@CheckExecutable).also { logger.info("test[{}] success [{}]", name, duration) } }
          .assertionFailure { name, duration -> onTestFailed(this@CheckExecutable, this).also { logger.info("test[{}] failed [{}]", name, duration) } }
          .testSkipped { name -> onTestSkipped(this@CheckExecutable, this).also { logger.info("test[{}] skipped", name) } }
          .testAborted { name -> onTestError(this@CheckExecutable, this).also { logger.info("test[{}] aborted by {} {}", name, this.javaClass.canonicalName, this.message) } }
          .let { Unit }

  override fun getSource(): Optional<TestSource> = Optional.of(MethodSource.from(containerTest.javaClass.simpleName, check.description))

  override fun removeFromHierarchy() = Unit

  override fun setParent(parent: TestDescriptor?) =
      if (parentDescriptor != parent) throw IllegalArgumentException("parent is $parentDescriptor, but not $parent")
      else Unit

  override fun getParent(): Optional<TestDescriptor> = Optional.of(parentDescriptor)

  override fun executables(): Iterable<CheckExecutable> = listOf(this)

  override fun getChildren(): MutableSet<out TestDescriptor> = mutableSetOf()

  override fun getDisplayName(): String = check.description

  override fun getType(): TestDescriptor.Type = TestDescriptor.Type.TEST

  override fun getUniqueId(): UniqueId = parentDescriptor.uniqueId.append("check", check.description)

  override fun removeChild(descriptor: TestDescriptor?) =
      if (descriptor == null) throw IllegalArgumentException("descriptor is null")
      else Unit

  override fun addChild(descriptor: TestDescriptor?) =
      if (descriptor == null) throw IllegalArgumentException("descriptor is null")
      else Unit

  override fun findByUniqueId(uniqueId: UniqueId?): Optional<out TestDescriptor> =
      when (uniqueId) {
        null -> Optional.empty()
        this.uniqueId -> Optional.of(this)
        else -> Optional.empty()
      }

  override fun getTags(): MutableSet<TestTag> = mutableSetOf()
}
