// https://kotlinlang.org/docs/serialization.html#serialize-and-deserialize-json
package appkt.serverkt

// server
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.Serializable

// Json
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

// http
import io.ktor.http.HttpStatusCode

// validation
// import kotlinx.serialization.SerializationException
import io.ktor.server.plugins.BadRequestException
// import io.ktor.server.application.*
// import io.ktor.server.plugins.requestvalidation.*

// my modules
import appkt.reqDataVerifyKt.ReqDataVerify
import appkt.resDataVerifyKt.ResDataVerify

// for receive & respond
import io.ktor.server.request.*

// @Serializable
// data class Data(val a: Int, val b: String)

@Serializable
data class FeeCalcRequest(
    val cart_value: Int, // cent
    val delivery_distance: Int, //meter
    val number_of_items: Int,
    val time: String // UTC(ISO 8601)
)


@Serializable
data class FeeCalcResponse(
    val delivery_fee: Int
)

class Server {

    fun deliveryFeeServerConfig(port: Int = 8080) {
        embeddedServer(Netty, port = port) {

            install(ContentNegotiation){
                json(Json{ignoreUnknownKeys = false})
            }

            routing {
                post("/delivery-fee") {

                    try {
                        // verify incoming request
                        val request: FeeCalcRequest = call.receive<FeeCalcRequest>()
                        ReqDataVerify().invalidateRequest(request)

                        // Fee calculation
                        val finalFee = ResDataVerify().calculateDeliveryFee(request)

                        //Response to Clients
                        call.respond(FeeCalcResponse(finalFee))

                    } catch (e: BadRequestException) {
                        call.respond(HttpStatusCode.BadRequest, "400: Invalid request format\nType of values are wrong or invalid key included\n\nExample of expected request:\n{\"cart_value\": 10, \"delivery_distance\": 1000, \"number_of_items\": 5, \"time\": \"2024-01-01T12:00:00Z\"}")
                        // e.printStackTrace()
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "500: Internal Server Errorr")
                        // e.printStackTrace()
                    }
                }

                get("/healthcheck") {
                    call.respondText("OK")
                }

            }
        }.start(wait = true)
    }

}