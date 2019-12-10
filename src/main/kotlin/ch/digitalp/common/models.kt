package ch.digitalp.common


import com.google.gson.*
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

abstract class Models {
  fun toStringJson(): String {
    return toStringJson(this)
  }
}

fun getGsonSnakeCase(): Gson {
  return GsonBuilder()
    .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeConverter())
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
}

fun getGsonCamelCase(): Gson {
  return GsonBuilder().registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeConverter()).create()
}

fun toJson(e: Any): JsonObject {
  return JsonObject(getGsonCamelCase().toJson(e))
}

fun toStringJson(e: Any): String {
  return toJson(e).encode()
}

/**
 * Convert JsonObject with field in snake case
 */
inline fun <reified T> JsonObject.convertSC(): T {
  return getGsonSnakeCase().fromJson(this.encode(), T::class.java)
}

/**
 * Convert JsonObject with field in snake case
 */
inline fun <reified T> JsonObject.convertCC(): T {
  return getGsonCamelCase().fromJson(this.encode(), T::class.java)
}

/**
 * This method take a JsonObject and convert it with GSON to a specific type
 * GSON not having a strategy for detect unknown property, we need to re-encode it
 * to string json and assert it's not an empty json but containing valid field for
 * specified type
 */
inline fun <reified T> JsonObject.assertValidField(rc: RoutingContext): JsonObject {
    val ob = toJson(getGsonCamelCase().fromJson(this.encode(), T::class.java)!!)
    if (ob.encode() == "{}") rc.fail(BadRequestRestException("Unrecognized field"))
    return ob
}

data class Error(val message: String?) : Models()

data class Response(val result: String, val content: Any) : Models()

/**
 * LocalDateTimeConverter manage conversion between string and LocalDateTime
 */
class LocalDateTimeConverter : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

  override fun serialize(src: LocalDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement? {
    return JsonPrimitive(FORMATTER.format(src))
  }

  override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LocalDateTime? {
    return LocalDate.from(FORMATTER.parse(json!!.asString)).atStartOfDay()
  }

  companion object {
    private val FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE
  }
}

fun <T : Any> T?.notNull(f: (it: T) -> Unit): T? {
    if (this != null) f(this)
    return this
}

fun <T : Any> T?.isNull(f: () -> Unit): T? {
    if (this == null) f()
    return this
}