package com.thecarousell.plugin.utils

import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asTypeName

object GenerateAnalyticsUtils {

    fun createParameterSpec(paramName: String, type: String) =
            ParameterSpec.builder(paramName, getDataType(type))
                    .defaultValue(getDefaultValue(type))
                    .build()

    fun createProperty(paramName: String, type: String) =
            PropertySpec.builder(paramName, getDataType(type))
                    .initializer(paramName)
                    .build()

    private fun getDataType(type: String) = when (type) {
        "STRING" -> String::class.asTypeName().asNullable()
        "INTEGER" -> Int::class.asTypeName()
        "BOOLEAN" -> Boolean::class.asTypeName()
        else -> Int::class.asTypeName()
    }

    private fun getDefaultValue(type: String) = when (type) {
        "STRING" -> "null"
        "INTEGER" -> "0"
        "BOOLEAN" -> "false"
        else -> "null"
    }

}