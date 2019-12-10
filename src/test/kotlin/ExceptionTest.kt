package  ch.digitalp.common

import io.vertx.core.Handler
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.DecodeException
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.validation.ValidationException
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import java.lang.Exception

class ExceptionTest {

    @Test
    fun `handleContextFail should set handler for error`() {
        // GIVEN
        val router = mock(Router::class.java)

        // WHEN
        handleContextFail(router)

        // THEN
        verify(router, times(5)).errorHandler(ArgumentMatchers.anyInt(), ArgumentMatchers.any(Handler::class.java) as Handler<RoutingContext>?)
    }

    @Test
    fun `errorHandler should set header and response error`() {
        // GIVEN
        val ctx = mock(RoutingContext::class.java)
        val response = mock(HttpServerResponse::class.java)
        doReturn(response).`when`(ctx).response()
        doReturn(response).`when`(response).setStatusCode(ArgumentMatchers.anyInt())
        doReturn(400).`when`(ctx).statusCode()

        // WHEN
        errorHandler(ctx)

        // THEN
        verify(response).putHeader("Content-Type", "application/json")
    }

    @Test
    fun `validateThrowable when ctx not in failure with 400`() {
        // GIVEN
        val ctx = mock(RoutingContext::class.java)
        val response = mock(HttpServerResponse::class.java)
        doReturn(response).`when`(ctx).response()
        doReturn(response).`when`(response).setStatusCode(ArgumentMatchers.anyInt())
        doReturn(400).`when`(ctx).statusCode()

        // WHEN
        validateThrowable(ctx)

        // THEN
        verify(response).setStatusCode(400)
        verify(response).end("{\"message\":\"Bad request\"}")
    }

    @Test
    fun `validateThrowable when ctx not in failure with 404`() {
        // GIVEN
        val ctx = mock(RoutingContext::class.java)
        val response = mock(HttpServerResponse::class.java)
        doReturn(response).`when`(ctx).response()
        doReturn(response).`when`(response).setStatusCode(ArgumentMatchers.anyInt())
        doReturn(404).`when`(ctx).statusCode()

        // WHEN
        validateThrowable(ctx)

        // THEN
        verify(response).setStatusCode(404)
        verify(response).end("{\"message\":\"Not found\"}")
    }

    @Test
    fun `validateThrowable when ctx failure with Exception`() {
        // GIVEN
        val e = InternalServerErrorRestException("exception")
        val ctx = mock(RoutingContext::class.java)
        val response = mock(HttpServerResponse::class.java)
        doReturn(response).`when`(ctx).response()
        doReturn(response).`when`(response).setStatusCode(ArgumentMatchers.anyInt())
        doReturn(e).`when`(ctx).failure()

        // WHEN
        validateThrowable(ctx)

        // THEN
        verify(response).setStatusCode(500)
        verify(response).end()
    }

    @Test
    fun `manageResponseException when NotFoundRestException should return 404`() {
        // GIVEN
        val e = NotFoundRestException("Not found")
        val ctx = mock(RoutingContext::class.java)
        val response = mock(HttpServerResponse::class.java)
        doReturn(response).`when`(ctx).response()
        doReturn(response).`when`(response).setStatusCode(ArgumentMatchers.anyInt())

        // WHEN
        manageResponseException(e, ctx)

        // THEN
        verify(response).setStatusCode(404)
        verify(response).end("{\"message\":\"Not found\"}")
    }

    @Test
    fun `manageResponseException when BadRequestRestException should return 400`() {
        // GIVEN
        val e = BadRequestRestException("Bad request")
        val ctx = mock(RoutingContext::class.java)
        val response = mock(HttpServerResponse::class.java)
        doReturn(response).`when`(ctx).response()
        doReturn(response).`when`(response).setStatusCode(ArgumentMatchers.anyInt())

        // WHEN
        manageResponseException(e, ctx)

        // THEN
        verify(response).setStatusCode(400)
        verify(response).end("{\"message\":\"Bad request\"}")
    }

    @Test
    fun `manageResponseException when DecodeException should return 400`() {
        // GIVEN
        val e = DecodeException("Decode exception")
        val ctx = mock(RoutingContext::class.java)
        val response = mock(HttpServerResponse::class.java)
        doReturn(response).`when`(ctx).response()
        doReturn(response).`when`(response).setStatusCode(ArgumentMatchers.anyInt())

        // WHEN
        manageResponseException(e, ctx)

        // THEN
        verify(response).setStatusCode(400)
        verify(response).end("{\"message\":\"Decode exception\"}")
    }

    @Test
    fun `manageResponseException when ConflictRestException should return 409`() {
        // GIVEN
        val e = ConflictRestException("Conflict")
        val ctx = mock(RoutingContext::class.java)
        val response = mock(HttpServerResponse::class.java)
        doReturn(response).`when`(ctx).response()
        doReturn(response).`when`(response).setStatusCode(ArgumentMatchers.anyInt())

        // WHEN
        manageResponseException(e, ctx)

        // THEN
        verify(response).setStatusCode(409)
        verify(response).end("{\"message\":\"Conflict\"}")
    }

    @Test
    fun `manageResponseException when ValidationException should return 400`() {
        // GIVEN
        val e = ValidationException("Validation failed")
        val ctx = mock(RoutingContext::class.java)
        val response = mock(HttpServerResponse::class.java)
        doReturn(response).`when`(ctx).response()
        doReturn(response).`when`(response).setStatusCode(ArgumentMatchers.anyInt())

        // WHEN
        manageResponseException(e, ctx)

        // THEN
        verify(response).setStatusCode(400)
        verify(response).end("{\"message\":\"Validation failed\"}")
    }

    @Test
    fun `manageResponseException when other Exception should return 500`() {
        // GIVEN
        val e = InvalidFieldException("error")
        val ctx = mock(RoutingContext::class.java)
        val response = mock(HttpServerResponse::class.java)
        doReturn(response).`when`(ctx).response()
        doReturn(response).`when`(response).setStatusCode(ArgumentMatchers.anyInt())

        // WHEN
        manageResponseException(e, ctx)

        // THEN
        verify(response).setStatusCode(500)
        verify(response).end()
    }
}