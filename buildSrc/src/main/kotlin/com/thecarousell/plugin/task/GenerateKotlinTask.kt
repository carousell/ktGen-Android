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

	lateinit var packageName: String

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

	private fun generateAnalyticsClass(eventList: EventList) {
		val eventsFileBuilder = FileSpec.builder(packageName,
				"${eventList.group.toCamelCaseCapitalized()}Events")
		val eventsObjectBuilder = TypeSpec.objectBuilder(
				"${eventList.group.toCamelCaseCapitalized()}Events")
		val modelsFileBuilder = FileSpec.builder(packageName, "AnalyticsModels")

		eventList.events.forEach {
			modelsFileBuilder.addType(createDataModel(it))
			eventsObjectBuilder.addFunction(createFunction(it))
		}
		eventsFileBuilder.addType(eventsObjectBuilder.build())
		FileUtils.writeFiles(outDir, packageName, modelsFileBuilder.build(), eventsFileBuilder.build())
	}

	private fun createDataModel(event: Event): TypeSpec {
		val dataModelBuilder = TypeSpec.classBuilder("${event.track.name}Properties".toCamelCaseCapitalized()).addModifiers(KModifier.DATA)
		val constructorBuilder = FunSpec.constructorBuilder()
		event.track.properties.forEach {
			constructorBuilder.addParameter(GenerateAnalyticsUtils.createParameterSpec(it.name.toCamelCase(), it.type))
			dataModelBuilder.addProperty(GenerateAnalyticsUtils.createProperty(it.name.toCamelCase(), it.type))
		}
		return dataModelBuilder.primaryConstructor(constructorBuilder.build()).build()
	}

	private fun createFunction(event: Event): FunSpec {
		val funBuilder = FunSpec.builder(event.track.name.toCamelCase())
		funBuilder.addParameter("properties", ClassName(packageName, "${event.track.name}Properties".toCamelCaseCapitalized()))
				.addStatement("val map = HashMap<String, Any?>()")
		event.track.properties.forEach {
			funBuilder.addStatement("map.put(\"${it.name}\", properties.${it.name.toCamelCase()})")
		}
		funBuilder.addStatement("sendAnalyticsEvent(\"${event.track.name}\", \"${event.track.type}\", map)")
		return funBuilder.build()
	}
}