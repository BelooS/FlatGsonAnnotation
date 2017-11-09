package com.beloo.gson

import java.lang.reflect.Field
import kotlin.collections.*

/** recursively get all class fields from inheritance tree */
internal fun Class<*>.getAllFields(): List<Field> {
    val fields = this.declaredFields.asList().toMutableList()
    this.superclass?.getAllFields()?.let { fields.addAll(it) }
    return fields
}