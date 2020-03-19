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

import com.example.Executable.Companion.optional
import com.example.Executable.Companion.invoke
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestSource
import org.junit.platform.engine.TestTag
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.support.descriptor.ClassSource
import org.slf4j.LoggerFactory
import java.util.*

data class TestExecutable(
    val parentDescriptor: TestDescriptor,
    val test: Test
) : TestDescriptor, Executable<CheckExecutable> {

  private val logger = LoggerFactory.getLogger(TestExecutable::class.java)

  override fun executeTests(
      onTestStart: (TestDescriptor) -> Unit,
      onTestSucceeded: (TestDescriptor) -> Unit,
      onTestFailed: (TestDescriptor, Impediments.AssertionFailureException) -> Unit,
      onTestSkipped: (TestDescriptor, Impediments.SkippedException) -> Unit,
      onTestError: (TestDescriptor, Throwable) -> Unit) = 
      onTestStart(this) () { logger.info("test start") } () {
        executables().forEach { executable ->
          executable.executeTests(onTestStart, onTestSucceeded, onTestFailed, onTestSkipped, onTestError)
        }
      } () { onTestSucceeded(this) } () { logger.info("test finished") }

  override fun getSource(): Optional<TestSource> = Optional.of(ClassSource.from(test.javaClass))

  override fun removeFromHierarchy() = throw UnsupportedOperationException("removeFromHierarchy not supported")

  override fun setParent(parent: TestDescriptor?) =
      if (parentDescriptor != parent) throw IllegalArgumentException("parent is $parentDescriptor, but not $parent")
      else Unit

  override fun getParent(): Optional<TestDescriptor> = Optional.of(parentDescriptor)

  override fun executables(): Iterable<CheckExecutable> = test.all.map { CheckExecutable(this, test, it) }

  override fun getChildren(): MutableSet<out TestDescriptor> = executables().toMutableSet()

  override fun getDisplayName(): String = test::class.simpleName ?: test.javaClass.simpleName

  override fun getType(): TestDescriptor.Type = TestDescriptor.Type.CONTAINER

  override fun getUniqueId(): UniqueId = parentDescriptor.uniqueId.append("test", test.javaClass.canonicalName)

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
        else -> children.find { it.uniqueId == uniqueId }.optional
      }

  override fun getTags(): MutableSet<TestTag> = emptySet<TestTag>().toMutableSet()
}
