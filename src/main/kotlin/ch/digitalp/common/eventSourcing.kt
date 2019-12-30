package ch.digitalp.common

import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import java.time.LocalDateTime

open class Event<T>(val name: String, val payload: T, val date: LocalDateTime = LocalDateTime.now(), val transmitter: String): Models()
data class EventHandler<T, D>(val filter: String, val handler: (event: Event<T>, dependency: D) -> Unit)

inline fun <reified T, D> registerEventHandler(dependency: D, vertx: Vertx, address: String, handler: EventHandler<T, D>) {
    vertx.eventBus().consumer<JsonObject>(address, genericHandler(handler, dependency))
}

inline fun <reified T, D> genericHandler(handler: EventHandler<T, D>, dependency: D): Handler<Message<JsonObject>> {
    return Handler { message: Message<JsonObject> ->
        handler.takeIf { h -> h.filter == message.body().getString("name") }.notNull { h ->
            val payload = message.body().getJsonObject("payload").convertCC<T>()
            val event = message.body().convertCC<Event<T>>()
            val e = Event(event.name, payload, event.date, event.transmitter)
            h.handler(e, dependency)
        }
    }
}