package org.woltDeliveryFee

import appkt.serverkt.Server
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.net.ConnectException
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class SelfContainServerTest {
    private val host = "http://localhost:8081"
    private val client = HttpClient(CIO)
    private var runningScope: Job? = null
    @BeforeEach
    fun startServer() = runBlocking {
        println("before each 1")
         val scope = CoroutineScope(Dispatchers.IO + Job())
        runningScope = scope.launch {
            Server().deliveryFeeServerConfig(8081)
        }

        val startTime = OffsetDateTime.now()
        while (OffsetDateTime.now().isBefore(startTime.plusSeconds(5))) {
            try {
                client.get("$host/healthcheck")
                break
            } catch (e: ConnectException) {
                println("Server is not ready yet ${OffsetDateTime.now()}")
                Thread.sleep(50)
            }
        }
    }

    @AfterEach
    fun stopServer() {
        if(runningScope != null) {
            runningScope!!.cancel()
        } else {
            println("runningScope is null")
        }
    }

    @Test
    fun `Test server functionally without starting server`(): Unit = runBlocking {

        val response: HttpResponse = client.post("$host/delivery-fee") {
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
        runningScope?.cancel()
        assertEquals(response.bodyAsText(), """{"delivery_fee":500}""")
    }
}