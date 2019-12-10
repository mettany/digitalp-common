package ch.digitalp.common.api

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.client.HttpResponse

interface HydraService {

    fun introspectToken(token: String): (Handler<AsyncResult<HttpResponse<Buffer>>>) -> Unit
}