package com.thecarousell.plugin.task

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
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
				generateTrial(eventList)
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}

	fun generateTrial(eventList: EventList) {
		val file = FileSpec.builder("com.thecarousell.analytics", "Analytics")
				.addType(TypeSpec.classBuilder("Analytics")
						.addType(TypeSpec.companionObjectBuilder("")
								.addFunction(FunSpec.builder("create_ok_button_clickedEvent")
										.addParameter("user_id", String::class)
										.addParameter("time", String::class)
										.addStatement("val map = HashMap<String, String>()")
										.addStatement("map.put(\"user_id\", user_id)")
										.addStatement("map.put(\"time\", time)")
										.build()).build()
						)
						.build())
				.build()
		val dir = File(outDir.absolutePath + "/com/thecarousell/analytics")
		if (!dir.exists() && !dir.mkdirs()) {
			throw IllegalStateException("Couldn't create dir: " + dir);
		}
		val directoryFile = File(outDir.absolutePath)
		file.writeTo(directoryFile)
	}

}