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
import org.slf4j.LoggerFactory
import java.util.*

data class EngineExecutable(
    private val id: UniqueId,
    val tests: Iterable<Testable>) : TestDescriptor, Executable<TestExecutable> {

  private val logger = LoggerFactory.getLogger(EngineExecutable::class.java)

  override fun executeTests(
      onTestStart: (TestDescriptor) -> Unit,
      onTestSucceeded: (TestDescriptor) -> Unit,
      onTestFailed: (TestDescriptor, Impediments.AssertionFailureException) -> Unit,
      onTestSkipped: (TestDescriptor, Impediments.SkippedException) -> Unit,
      onTestError: (TestDescriptor, Throwable) -> Unit) =
      onTestStart(this) () {
        logger.info("test execution start")
      } () {
        executables().forEach { it.executeTests(onTestStart, onTestSucceeded, onTestFailed, onTestSkipped, onTestError) }
      } () { onTestSucceeded(this) } () { logger.info("test execution finished") }

  override fun getSource(): Optional<TestSource> = Optional.empty()

  override fun removeFromHierarchy() = Unit

  override fun setParent(parent: TestDescriptor?) = Unit

  override fun getParent(): Optional<TestDescriptor> = Optional.empty()

  override fun getChildren(): MutableSet<out TestDescriptor> = executables().toMutableSet()

  override fun executables(): Iterable<TestExecutable> = tests.map { it.createDescriptor(this) }

  override fun getDisplayName(): String = "k-check"

  override fun getType(): TestDescriptor.Type = TestDescriptor.Type.CONTAINER

  override fun getUniqueId(): UniqueId = id

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

  override fun getTags(): MutableSet<TestTag> = mutableSetOf()
}
