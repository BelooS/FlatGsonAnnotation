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
                .registerTypeAdapterFactory (FlatTypeAdapterFactory())
                .create()
    }

    @Test
    fun toJson_WithSeveralFieldsAndFlatPrefixAnnotation_NestedFieldsSerialized() {
        val model = TestModelPrefix("test", Nested("one"), Nested("two"))
        val json: String = gson.toJson(model)
        val result = gson.fromJson<ResultModelPrefix>(json, ResultModelPrefix::class.java)
        assertEquals("test", result.test)
        assertEquals("one", result.nested)
        assertEquals("two", result.nestedSecond)
    }

    @Test
    fun toJson_WithOneFieldAndFlatAnnotation_NestedFieldSerialized() {
        val model = TestModel("test", Nested("one"))
        val json: String = gson.toJson(model)
        val result = gson.fromJson<ResultModel>(json, ResultModel::class.java)
        assertEquals("test", result.test)
        assertEquals("one", result.nested)
    }

}

data class ResultModelPrefix(
        @SerializedName("test")
        val test: String,
        @SerializedName("first_nested")
        val nested: String,
        @SerializedName("second_nested")
        val nestedSecond: String
)

data class TestModelPrefix(
        @SerializedName("test")
        val test: String,
        @Flat(prefix = "first_")
        val nested: Nested,
        @Flat(prefix = "second_")
        val nestedSecond: Nested
)

data class TestModel(
        @SerializedName("test")
        val test: String,
        @Flat
        val nested: Nested
)

data class ResultModel(
        @SerializedName("test")
        val test: String,
        @SerializedName("nested")
        val nested: String
)

data class Nested(
        @SerializedName("nested")
        val nested: String
)