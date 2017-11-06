package com.beloo.util

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException

/** mark a field as a flat field to be insterted in container object.
 * fields of nested objects will be serialized as fields of main object with using prefix for field name if provided
 * @param prefix prefix for corresponding field name in target object. You need it for example if object contains several same nested objects */
@Target(AnnotationTarget.FIELD)
annotation class Flat(val prefix: String = "")

class FlatExclusionStrategy : ExclusionStrategy {
    override fun shouldSkipField(f: FieldAttributes): Boolean = f.getAnnotation(Flat::class.java) != null

    override fun shouldSkipClass(clazz: Class<*>): Boolean = false
}

class FlatTypeAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T> {
        val defaultAdapter = gson.getDelegateAdapter(this, type)
        val elementAdapter = gson.getAdapter(JsonElement::class.java)

        return object : TypeAdapter<T>() {
            @Throws(IOException::class)
            override fun write(out: JsonWriter, value: T) {
                val element: JsonElement = defaultAdapter.toJsonTree(value)
                if (element.isJsonObject) {
                    type.rawType.declaredFields
                            .filter { it.isAnnotationPresent(Flat::class.java) }
                            .forEach {
                                it.isAccessible = true
                                val fieldValue = it.get(value)
                                val prefix: String = it.getAnnotation(Flat::class.java).prefix
                                val adapter = gson.getAdapter(fieldValue.javaClass)
                                val nestedObject = adapter.toJsonTree(fieldValue).asJsonObject
                                element.asJsonObject.addPropertiesFrom(nestedObject, prefix)
                            }
                }
                elementAdapter.write(out, element)
            }

            @Throws(IOException::class)
            override fun read(source: JsonReader): T = defaultAdapter.read(source)
        }.nullSafe()
    }
}
