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
package jp.test.example

import com.example.AssertFail
import com.example.Given
import com.example.Success
import com.example.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object AppTest : Test
by Given("1", { 1 })
    .When(" + 2", { p -> p + 2 })
    .Then(" = 3", { _, t -> if (t == 3) Success else AssertFail(t, 3) })

object ThrowingTest: Test
by Given("invalid date", { "2020-02-30" })
    .When("parsing as date", { date -> LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE) })
    .Then("invalid date", { _, v -> if (v != null) Success else AssertFail(v, Double.POSITIVE_INFINITY) })

object ListTest: Test
by Given("list with 5 items", { listOf("foo", "bar", "baz", "qux", "quux") })
    .When("take sublist from index 1 with 3 counts", { it.subList(1, 3) })
    .Then("it contains 'bar','baz','qux'.", { _, list -> if (list == listOf("bar", "baz", "qux")) Success else AssertFail(list, listOf("bar", "baz", "qux")) })
