package com.beloo.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test

class FlatGsonAnnotationIntegrationTest {

    private lateinit var gson: Gson

    @Before
    fun setUp() {
        gson = GsonBuilder()
                .addSerializationExclusionStrategy(FlatExclusionStrategy())
                .addDeserializationExclusionStrategy(FlatExclusionStrategy())
                .registerTypeAdapterFactory (FlatTypeAdapterFactory())
                .create()
    }

    @Test
    fun toJson_WithSeveralFieldsAndFlatPrefixAnnotation_NestedFieldsSerialized() {
        val model = ContainsNestedPrefix("test", Nested("one", "two"), Nested("three", "four"))
        val json: String = gson.toJson(model)
        val result = gson.fromJson<FlatObjectPrefix>(json, FlatObjectPrefix::class.java)
        assertEquals("test", result.test)
        assertEquals("one", result.firstField1)
        assertEquals("two", result.firstField2)
        assertEquals("three", result.secondField1)
        assertEquals("four", result.secondField2)
    }

    @Test
    fun toJson_WithOneFieldAndFlatAnnotation_NestedFieldSerialized() {
        val model = ContainsNested("test", Nested("one", "two"))
        val json: String = gson.toJson(model)
        val result = gson.fromJson<FlatObject>(json, FlatObject::class.java)
        assertEquals("test", result.test)
        assertEquals("one", result.field1)
        assertEquals("two", result.field2)
    }

    @Test
    fun toJson_ChildOfObjectWithNested_NestedFieldSerialized() {
        val model = Child("test", Nested("one", "two"), 5)
        val json: String = gson.toJson(model)
        val result = gson.fromJson<FlatObject>(json, FlatObject::class.java)
        assertEquals("test", result.test)
        assertEquals("one", result.field1)
        assertEquals("two", result.field2)
        assertEquals(5, result.value)
    }

    @Test
    fun fromJson_ToObjectWithOneNestedObject_NestedObjectDeserialized() {
        val flatObject = FlatObject("test", "one", "two", 5)
        val json: String = gson.toJson(flatObject)
        val result = gson.fromJson<ContainsNested>(json, ContainsNested::class.java)
        assertNotNull(result.nested1)
        assertEquals("test", result.test)
        assertEquals("one", result.nested1.field1)
        assertEquals("two", result.nested1.field2)
    }

    @Test
    fun fromJson_ToObjectWithSeveralNestedObjects_NestedObjectsDeserialized() {
        val flatObject = FlatObjectPrefix("test", "one", "two", "three", "four")
        val json: String = gson.toJson(flatObject)
        val result = gson.fromJson<ContainsNestedPrefix>(json, ContainsNestedPrefix::class.java)
        assertNotNull(result.nested1)
        assertNotNull(result.nested2)
        assertEquals("test", result.test)
        assertEquals("one", result.nested1.field1)
        assertEquals("two", result.nested1.field2)
        assertEquals("three", result.nested2.field1)
        assertEquals("four", result.nested2.field2)
    }

}

data class FlatObjectPrefix(
        @SerializedName("test")
        val test: String,
        @SerializedName("first_field1")
        val firstField1: String,
        @SerializedName("first_field2")
        val firstField2: String,
        @SerializedName("second_field1")
        val secondField1: String,
        @SerializedName("second_field2")
        val secondField2: String
)

data class ContainsNestedPrefix(
        @SerializedName("test")
        val test: String,
        @Flat(prefix = "first_")
        val nested1: Nested,
        @Flat(prefix = "second_")
        val nested2: Nested
)

class Child(
        test: String,
        nested1: Nested,
        @SerializedName("value")
        val value: Int) : ContainsNested(test, nested1)

open class ContainsNested(
        @SerializedName("test")
        val test: String,
        @Flat
        val nested1: Nested
)

data class FlatObject(
        @SerializedName("test")
        val test: String,
        @SerializedName("field1")
        val field1: String,
        @SerializedName("field2")
        val field2: String,
        @SerializedName("value")
        val value: Int
)

data class Nested(
        @SerializedName("field1")
        val field1: String,
        @SerializedName("field2")
        val field2: String
)