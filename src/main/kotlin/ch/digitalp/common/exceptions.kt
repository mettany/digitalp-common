package ch.digitalp.common

import io.vertx.core.json.DecodeException
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.validation.ValidationException

data class NotFoundRestException(override val message: String) : Exception(message)
data class BadRequestRestException(override val message: String) : Exception(message)
data class InternalServerErrorRestException(override val message: String) : Exception(message)
data class ConflictRestException(override val message: String) : Exception(message)
data class InvalidFieldException(override val message: String) : Exception(message)

fun handleContextFail(router: Router) {
  val error = arrayOf(400, 404, 409, 500, 501)
  error.forEach { code ->
    router.errorHandler(code, errorHandler)
  }
}

val errorHandler = { ctx: RoutingContext ->
    ctx.response().putHeader("Content-Type", "application/json")
    validateThrowable(ctx)
}

fun validateThrowable(ctx: RoutingContext) {
    if (ctx.failure() == null) {
        when (ctx.statusCode()) {
            400 -> manageResponseException(BadRequestRestException("Bad request"), ctx)
            404 -> manageResponseException(NotFoundRestException("Not found"), ctx)
        }
    } else {
        manageResponseException(ctx.failure(), ctx)
    }
}

fun manageResponseException(e: Throwable, ctx: RoutingContext) {
  when (e) {
    is NotFoundRestException -> ctx.response().setStatusCode(404).end(Error(e.message).toStringJson())
    is BadRequestRestException -> ctx.response().setStatusCode(400).end(Error(e.message).toStringJson())
    is DecodeException -> ctx.response().setStatusCode(400).end(Error(e.message).toStringJson())
    is ConflictRestException -> ctx.response().setStatusCode(409).end(Error(e.message).toStringJson())
    is ValidationException -> ctx.response().setStatusCode(400).end(Error(e.message?.removePrefix("$.")).toStringJson())
    else -> ctx.response().setStatusCode(500).end()
  }
}
