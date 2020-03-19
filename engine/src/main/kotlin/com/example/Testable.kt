package com.example

import org.junit.platform.engine.TestDescriptor

data class Testable(val test: Test) {

  fun createDescriptor(parentDescriptor: TestDescriptor): TestExecutable = TestExecutable(parentDescriptor, test)
}
