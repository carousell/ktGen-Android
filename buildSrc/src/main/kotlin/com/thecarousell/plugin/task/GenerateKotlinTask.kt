package com.thecarousell.plugin.task

import com.squareup.kotlinpoet.*
import com.thecarousell.plugin.model.Event
import com.thecarousell.plugin.model.EventList
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

fun String.toCamelCase(): String {
	return this.toCamelCaseCapitalized().decapitalize()
}

fun String.toCamelCaseCapitalized(): String {
	return this.split('_').joinToString("") {
		it.capitalize()
	}
}

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
			val dataModel = createDataModel(it)
			modelsFileBuilder.addType(dataModel)
			//

			val function = createFunction(it)

			val functionBuilder = FunSpec.builder(it.track.name + "Event")
					.addStatement("val map = HashMap<String, String>()")
			it.track.properties.forEach {
				functionBuilder
						.addParameter(it.name, String::class)
						.addStatement("map.put(\"${it.name}\", ${it.name})")
			}
			companionObjectBuilder.addFunction(functionBuilder.build())
		}


//		analyticsFileBuilder.addType(classBuilder.build()).build()
		writeFiles(modelsFileBuilder.build())
	}


	private fun createDataModel(event: Event): TypeSpec {
		val dataModelBuilder = TypeSpec.classBuilder(event.track.name.toCamelCaseCapitalized() + "Properties").addModifiers(KModifier.DATA)
		val constructorBuilder = FunSpec.constructorBuilder()
		event.track.properties.forEach {
			constructorBuilder.addParameter(createParameterSpec(it.name.toCamelCase(), it.type))
			dataModelBuilder.addProperty(createProperty(it.name.toCamelCase(), it.type))
		}
		return dataModelBuilder.primaryConstructor(constructorBuilder.build()).build()
	}

	private fun createFunction(event: Event) {

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
			"BOOLEAN"-> {
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
			"BOOLEAN"-> {
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


//	fun okButtonClicked(properties: OkButtonClickedProperties) {
//		val map: HashMap<String, Object>
//		AnalyticsSender.sendEvent(name, type, map)
//	}
//
//	data class OkButtonClickedProperties(val userId: String, val time: Int)


	//	fun generateTrial(eventList: EventList) {
//		val file = FileSpec.builder("com.thecarousell.analytics", "Analytics")
//				.addType(TypeSpec.classBuilder("Analytics")
//						.addType(TypeSpec.companionObjectBuilder("")
//								.addFunction(FunSpec.builder("create_ok_button_clickedEvent")
//										.addParameter("user_id", String::class)
//										.addParameter("time", String::class)
//										.addStatement("val map = HashMap<String, String>()")
//										.addStatement("map.put(\"user_id\", user_id)")
//										.addStatement("map.put(\"time\", time)")
//										.build()).build()
//						)
//						.build())
//				.build()
//		val dir = File(outDir.absolutePath + "/com/thecarousell/analytics")
//		if (!dir.exists() && !dir.mkdirs()) {
//			throw IllegalStateException("Couldn't create dir: " + dir);
//		}
//		val directoryFile = File(outDir.absolutePath)
//		file.writeTo(directoryFile)
//	}
	private fun writeFiles(vararg array: FileSpec) {
		val dir = File(outDir.absolutePath + "/com/thecarousell/analytics")
		if (!dir.exists() && !dir.mkdirs()) {
			throw IllegalStateException("Couldn't create dir: ${dir}");
		}
		val directoryFile = File(outDir.absolutePath)
		array.forEach {
			it.writeTo(directoryFile)
		}
	}
}