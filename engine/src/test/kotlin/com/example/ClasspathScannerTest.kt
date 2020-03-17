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
import org.junit.platform.engine.UniqueId
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class ClasspathScannerTest {

  companion object : InvocationHandler {
    @JvmStatic
    fun main(args: Array<String>) {
      val request: EngineDiscoveryRequest = Proxy.newProxyInstance(Thread.currentThread().contextClassLoader, arrayOf(EngineDiscoveryRequest::class.java), Companion) as EngineDiscoveryRequest
      val uniqueId = UniqueId.forEngine("k-check").append("test", "test")
      val classpathScanner = ClasspathScanner(request, uniqueId)
      val scanTests = classpathScanner.scanTests()
      val res = scanTests.errorMap { e -> "${e.javaClass.simpleName} - ${e.message}/ cause: ${e.cause}" }
      println(res)
    }

    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any {
      throw UnsupportedOperationException("proxy: ${proxy?.javaClass}, method: ${method?.name}")
    }
  }
}

object MayFoundAsTest : Test
by Given("may found - given", { 1 })
    .When("may found - when", { 2 })
    .Then("may found then", { _, _ -> Success })

object ThisAlsoFoundAsTest : Test
by Given("this is also found - given", { 1 })
    .When("this is also found - when", { 2 })
    .Then("this is also found then", { _, _ -> Success })

class MayNotFound : Test
by Given("may not found - given", { 1 })
    .When("may not found - when", { 2 })
    .Then("may not found then", { _, _ -> Success })

class NotTest
object AlsoNotTest
