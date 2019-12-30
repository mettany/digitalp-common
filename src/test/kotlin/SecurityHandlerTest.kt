package ch.digitalp.common

import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import kotlin.test.assertEquals

class SecurityHandlerTest {

    @Test
    fun `returnUnauthorized should set status code 401`() {
        // GIVEN
        val rc = mock(RoutingContext::class.java)
        val response = mock(HttpServerResponse::class.java)
        doReturn(response).`when`(rc).response()
        doReturn(response).`when`(response).setStatusCode(401)

        // WHEN
        returnUnauthorized(rc)

        // THEN
        verify(response).setStatusCode(401)
    }

    @Test
    fun `returnForbidden should set status code 403`() {
        // GIVEN
        val rc = mock(RoutingContext::class.java)
        val response = mock(HttpServerResponse::class.java)
        doReturn(response).`when`(rc).response()
        doReturn(response).`when`(response).setStatusCode(403)

        // WHEN
        returnForbidden(rc)

        // THEN
        verify(response).setStatusCode(403)
    }

    @Test
    fun `decodeAuthorizationBasic should decode and return string list`() {
        // GIVEN
        val toDecode = "YWRtaW46cGFzcw=="

        // WHEN
        val response = decodeAuthorizationBasic(toDecode)

        // THEN
        assertEquals(response.size, 2)
        assertEquals(response[0], "admin")
        assertEquals(response[1], "pass")
    }
}