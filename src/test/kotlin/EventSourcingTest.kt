package ch.digitalp.common

import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import kotlin.test.assertNotNull

@Suppress("UNCHECKED_CAST")
class EventSourcingTest {

    @Test
    fun `registerEventHandler should add consumer on event bus`() {
        // GIVEN
        val vertx = mock(Vertx::class.java)
        val eventBus = mock(EventBus::class.java)
        doReturn(eventBus).`when`(vertx).eventBus()
        val dependency = "Test"
        val handler = mock(EventHandler::class.java) as EventHandler<JsonObject, String>

        // WHEN
        registerEventHandler(dependency, vertx, "address", handler)

        // THEN
        verify(eventBus).consumer<JsonObject>(eq("address"), any(Handler::class.java) as Handler<Message<JsonObject>>?)
    }

    @Test
    fun `genericHandler should return handler`() {
        // GIVEN
        val handler = mock(EventHandler::class.java) as EventHandler<JsonObject, String>
        val dependency = "Test"

        // WHEN
        val result = genericHandler(handler, dependency)

        // THEN
        assertNotNull(result)
    }

    @Test
    fun `genericHandler should filter message and not call handler`() {
        // GIVEN
        val handler = mock(EventHandler::class.java) as EventHandler<JsonObject, String>
        val dependency = "Dependency"
        val message = mock(Message::class.java) as Message<JsonObject>
        val body = JsonObject().put("name", "notsamemessage")
        doReturn(body).`when`(message).body()
        doReturn("test").`when`(handler).filter

        // WHEN
        genericHandler(handler, dependency).handle(message)

        // THEN
        verify(handler, times(0)).handler
    }

    @Test
    fun `genericHandler should filter message and call handler`() {
        // GIVEN
        val handler = mock(EventHandler::class.java) as EventHandler<JsonObject, String>
        val handleFunction = {_: Event<String>, _: String -> Unit}
        val dependency = "Dependency"
        val message = mock(Message::class.java) as Message<JsonObject>
        val payload = JsonObject().put("key", "value")
        val body = toJson(Event(name = "test", payload =  payload, transmitter = "test"))
        doReturn(body).`when`(message).body()
        doReturn("test").`when`(handler).filter
        doReturn(handleFunction).`when`(handler).handler

        // WHEN
        genericHandler(handler, dependency).handle(message)

        // THEN
        verify(handler, times(1)).handler
    }
}