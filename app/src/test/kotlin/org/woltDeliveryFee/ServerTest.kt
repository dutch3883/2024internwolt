package org.woltDeliveryFee

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ServerTest {
    @Test
    fun `Test server functionally required server started`(): Unit = runBlocking {
        val client = HttpClient(CIO)
        val response: HttpResponse = client.post("http://localhost:8080/delivery-fee") {
            contentType(ContentType.Application.Json)
            setBody(
                """
            {
              "cart_value": 2000,
              "delivery_distance": 2235,
              "number_of_items": 4,
              "time": "2024-01-15T13:00:00Z"
            }
            """.trimIndent()
            )
        }
        client.close()
        assertEquals(response.bodyAsText(), """{"delivery_fee":500}""")

    }
}