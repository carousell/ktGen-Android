package com.thecarousell.plugin.task

import com.squareup.kotlinpoet.*
import com.thecarousell.plugin.extensions.toCamelCase
import com.thecarousell.plugin.extensions.toCamelCaseCapitalized
import com.thecarousell.plugin.model.Event
import com.thecarousell.plugin.model.EventList
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class GenerateKotlinTask : DefaultTask() {

	lateinit var srcDir: File

	lateinit var outDir: File

	@TaskAction
	fun generateCode() {
		System.out.println("Parsing files: $srcDir")
		val eventParser = EventParser()

		srcDir.walk().forEach {
			if (it.name.endsWith(".yaml", true)) try {
				val eventList = eventParser.loadFromFile(it)
				generateAnalyticsClass(eventList)
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}

	fun generateAnalyticsClass(eventList: EventList) {
		val eventsFileBuilder = FileSpec.builder("com.thecarousell.analytics", "Events")
		val modelsFileBuilder = FileSpec.builder("com.thecarousell.analytics", "AnalyticsModels")
		val eventsClassBuilder = TypeSpec.classBuilder("Events")
		val companionObjectBuilder = TypeSpec.companionObjectBuilder("")

		eventList.events.forEach {
			//
			modelsFileBuilder.addType(createDataModel(it))
			//
			companionObjectBuilder.addFunction(createFunction(it))
		}
		eventsClassBuilder.addType(companionObjectBuilder.build())
		eventsFileBuilder.addType(eventsClassBuilder.build())
		writeFiles(modelsFileBuilder.build(), eventsFileBuilder.build())
	}


	private fun createDataModel(event: Event): TypeSpec {
		val dataModelBuilder = TypeSpec.classBuilder("${event.track.name}Property".toCamelCaseCapitalized()).addModifiers(KModifier.DATA)
		val constructorBuilder = FunSpec.constructorBuilder()
		event.track.properties.forEach {
			constructorBuilder.addParameter(createParameterSpec(it.name.toCamelCase(), it.type))
			dataModelBuilder.addProperty(createProperty(it.name.toCamelCase(), it.type))
		}
		return dataModelBuilder.primaryConstructor(constructorBuilder.build()).build()
	}

	private fun createFunction(event: Event): FunSpec {
		val funBuilder = FunSpec.builder(event.track.name.toCamelCase())
		funBuilder.addParameter("params", ClassName("com.thecarousell.analytics", "${event.track.name}Property".toCamelCaseCapitalized()))
				.addStatement("val map = HashMap<String, Any?>()")
		event.track.properties.forEach {
			funBuilder.addStatement("map.put(\"${it.name}\", params.${it.name.toCamelCase()})")
		}
		return funBuilder.build()
	}

	private fun createProperty(paramName: String, type: String): PropertySpec {
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

	private fun createParameterSpec(paramName: String, type: String): ParameterSpec {
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

	private fun writeFiles(vararg array: FileSpec) {
		val dir = File(outDir.absolutePath + "/com/thecarousell/analytics")
		if (!dir.exists() && !dir.mkdirs()) {
			throw IllegalStateException("Couldn't create dir: ${dir}");
		}
		val directoryFile = File(outDir.absolutePath)
		array.forEach {
			it.writeTo(directoryFile)
			System.out.println("Writting: ${it}")
		}
	}
}