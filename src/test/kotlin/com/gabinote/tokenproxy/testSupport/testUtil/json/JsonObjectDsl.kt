package com.gabinote.tokenproxy.testSupport.testUtil.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode

fun jsonBuilder(block: JsonObjectDsl.() -> Unit): String =
    JsonObjectDsl().apply(block).toJsonString()

// 객체 빌더
class JsonObjectDsl {
    private val mapper = ObjectMapper()
    private val obj: ObjectNode = mapper.createObjectNode()

    /** 일반 값 추가 */
    infix fun String.to(value: Any?) {
        val element: JsonNode = when (value) {
            null -> NullNode.instance
            is JsonNode -> value
            is JsonObjectDsl -> value.build()
            is JsonArrayDsl -> value.build()
            is Number, is Boolean, is String -> mapper.valueToTree(value)
            else -> mapper.valueToTree(value)
        }
        obj.set<JsonNode>(this, element)
    }

    /** 중첩 객체 추가: "user" obj { ... } */
    fun String.obj(block: JsonObjectDsl.() -> Unit) {
        val child = JsonObjectDsl().apply(block).build()
        obj.set<JsonNode>(this, child)
    }

    /** 배열 추가: "tags" arr { +"a"; +"b" } */
    infix fun String.arr(block: JsonArrayDsl.() -> Unit) {
        val child = JsonArrayDsl().apply(block).build()
        obj.set<JsonNode>(this, child)
    }

    fun obj(block: JsonObjectDsl.() -> Unit): JsonNode =
        JsonObjectDsl().apply(block).build()

    internal fun build(): ObjectNode = obj

    fun toJsonString(): String = mapper.writeValueAsString(obj)
}
