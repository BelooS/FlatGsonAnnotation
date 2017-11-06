package com.beloo.util

import com.google.gson.JsonObject

internal fun JsonObject.addPropertiesFrom(jsonObject: JsonObject?): JsonObject {
    jsonObject?.entrySet()?.forEach { this.add(it.key, it.value) }
    return this
}

internal fun JsonObject.addPropertiesFrom(jsonObject: JsonObject?, keyPrefix: String): JsonObject {
    jsonObject?.entrySet()?.forEach { this.add("$keyPrefix${it.key}", it.value) }
    return this
}