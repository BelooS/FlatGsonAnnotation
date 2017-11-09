package com.beloo.gson

import com.google.gson.*
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException

/** mark a field as a flat field to be inserted in container object.
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

        val fields = type.rawType.getAllFields()
                .asSequence()
                .filter { it.isAnnotationPresent(Flat::class.java) }
                .onEach { it.isAccessible = true }

        return object : TypeAdapter<T>() {
            @Throws(IOException::class)
            override fun write(out: JsonWriter, value: T) {
                val element: JsonElement = defaultAdapter.toJsonTree(value)
                if (element.isJsonObject) {
                    fields.forEach {
                        val prefix: String = it.getAnnotation(Flat::class.java).prefix
                        val fieldValue = it.get(value)
                        val adapter = gson.getAdapter(fieldValue.javaClass)
                        val nestedObject = adapter.toJsonTree(fieldValue).asJsonObject
                        element.asJsonObject.addPropertiesFrom(nestedObject, prefix)
                    }
                }
                elementAdapter.write(out, element)
            }

            @Throws(IOException::class)
            override fun read(source: JsonReader): T {
                val value: T = defaultAdapter.read(source)
                fields.forEach {
                    //todo check if it is regular object
                    it.type.declaredFields.forEach {
                        val fieldName = it.getAnnotation(SerializedName::class.java)?.value ?: it.name

                    }

                    val prefix: String = it.getAnnotation(Flat::class.java).prefix
                }
                return value
            }
        }.nullSafe()
    }
}
