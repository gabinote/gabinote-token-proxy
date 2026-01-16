package com.gabinote.tokenproxy.testSupport.testUtil.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.NullNode

// 배열 빌더
class JsonArrayDsl {
    private val mapper = ObjectMapper()
    private val arr: ArrayNode = mapper.createArrayNode()

    /** 배열 요소 추가: +value */
    operator fun Any?.unaryPlus() {
        val element: JsonNode = when (this) {
            null -> NullNode.instance
            is JsonNode -> this
            is JsonObjectDsl -> this.build()
            is JsonArrayDsl -> this.build()
            is Number, is Boolean, is String -> mapper.valueToTree(this)
            else -> mapper.valueToTree(this)
        }
        arr.add(element)
    }

    internal fun build(): ArrayNode = arr
}
