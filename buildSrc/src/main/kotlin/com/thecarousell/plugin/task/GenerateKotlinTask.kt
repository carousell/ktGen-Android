package com.thecarousell.plugin.task

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class GenerateKotlinTask : DefaultTask() {

	lateinit var srcDir: File

	lateinit var outDir: File

	@TaskAction
	fun generateCode() {
		parseYaml(srcDir)
	}

	private fun parseYaml(directory: File) {
		System.out.println("Parsing files: $directory")

		val eventParser = EventParser()

		directory.walk().forEach {
			if (it.name.endsWith(".yaml", true)) try {
				eventParser.loadFromFile(it)
				generateTrial(outDir)
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}

	fun generateTrial(outDirectory: File) {
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
		val dir = File(outDirectory.absolutePath + "/com/thecarousell/analytics")
		if (!dir.exists() && !dir.mkdirs()) {
			throw IllegalStateException("Couldn't create dir: " + dir);
		}
		val classFile = File(outDirectory.absolutePath)
		file.writeTo(classFile)
	}

}