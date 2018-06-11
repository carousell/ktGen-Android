package com.thecarousell.plugin.task

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asTypeName

object GenerateAnalyticsUtils {
	fun createParameterSpec(paramName: String, type: String): ParameterSpec {
		val dataType: ClassName
		val defaultValue: String
		when (type) {
			"STRING" -> {
				dataType = String::class.asTypeName().asNullable()
				defaultValue = "null"
			}
			"INTEGER" -> {
				dataType = Int::class.asTypeName()
				defaultValue = "0"
			}
			"BOOLEAN" -> {
				dataType = Boolean::class.asTypeName()
				defaultValue = "false"
			}
			else -> {
				dataType = Int::class.asTypeName()
				defaultValue = "null"
			}
		}
		return ParameterSpec.builder(paramName, dataType)
				.defaultValue(defaultValue).build()
	}

	fun createProperty(paramName: String, type: String): PropertySpec {
		val dataType: ClassName
		when (type) {
			"STRING" -> {
				dataType = String::class.asTypeName().asNullable()
			}
			"INTEGER" -> {
				dataType = Int::class.asTypeName()
			}
			"BOOLEAN" -> {
				dataType = Boolean::class.asTypeName()
			}
			else -> {
				dataType = Int::class.asTypeName()
			}
		}
		return PropertySpec.builder(paramName, dataType)
				.initializer(paramName)
				.build()
	}
}