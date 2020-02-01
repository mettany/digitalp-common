package ch.digitalp.common

import ch.digitalp.common.api.HydraService
import com.google.common.annotations.VisibleForTesting
import io.swagger.v3.parser.OpenAPIV3Parser
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.MultiMap
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import io.vertx.ext.web.api.contract.openapi3.impl.OpenApi3Utils
import io.vertx.ext.web.client.HttpResponse
import java.util.*


fun addHandlerForScopes(routerFactory: OpenAPI3RouterFactory, url: String, hydraService: HydraService) {
  val spec = OpenAPIV3Parser().readLocation(url, Collections.emptyList(), OpenApi3Utils.getParseOptions()).openAPI
  spec.components.securitySchemes["oAuth"]!!.flows.authorizationCode.scopes.forEach { scope ->
    routerFactory.addSecuritySchemaScopeValidator("oAuth", scope.key) { rc ->
      if (!rc.request().headers().contains("Authorization")) {
        rc.response().setStatusCode(401).end()
      } else {
        introspectToken(hydraService)(getBearerToken(rc.request().headers()))(handleIntrospect(rc, scope.key))
      }
    }
  }
}

@VisibleForTesting
val getBearerToken: (MultiMap) -> String = { headers ->
  headers.get("Authorization").removePrefix("Bearer ")
}

private val introspectToken: (HydraService) -> (String) -> (Handler<AsyncResult<HttpResponse<Buffer>>>) -> Unit = { hydraService ->
  { token: String -> hydraService.introspectToken(token) }
}

private val handleIntrospect: (RoutingContext, String) -> Handler<AsyncResult<HttpResponse<Buffer>>> = { rc, scope ->
  Handler { res ->
    checkHydraResponse(res, { returnUnauthorized(rc) }, { x -> checkIntrospectResult(x, rc, scope) })
  }
}

private val checkHydraResponse: (AsyncResult<HttpResponse<Buffer>>, () -> Unit, (JsonObject) -> Unit) -> Unit = { res, failed, succeed ->
  if (res.failed()) failed()
  if (res.succeeded()) succeed(res.result().bodyAsJsonObject())
}

@VisibleForTesting
val checkIntrospectResult: (JsonObject, RoutingContext, String) -> Unit = { introspect, rc, scope ->
  if (!introspect.getBoolean("active")) {
    returnUnauthorized(rc)
  } else {
    rc.data()["sub"] = introspect.getString("sub")
    val containScope = introspect.getString("scope").split(" ").contains(scope)
    val admin = introspect.getString("scope").split(" ").contains("app:admin")
    val emptyScope = scope === ""
    if (containScope || admin || emptyScope) rc.next() else returnForbidden(rc)
  }
}

@VisibleForTesting
val returnUnauthorized: (RoutingContext) -> Unit = { rc ->
  rc.response().setStatusCode(401).end()
}

@VisibleForTesting
val returnForbidden: (RoutingContext) -> Unit = { rc ->
  rc.response().setStatusCode(403).end()
}

/**
 * Util function to decode a basic authorization header base64 encoded
 * @return List<String> containing value of header separated by :
 */
val decodeAuthorizationBasic: (String) -> List<String> = { header: String ->
  String(Base64.getDecoder().decode(header.replace("Basic ", ""))).split(":")
}
