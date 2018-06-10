package com.thecarousell.plugin.task

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files

open class GenerateKotlinTask : DefaultTask() {

	lateinit var srcDir: File

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
				generateTrial()
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}

	fun generateTrial() {
//		val analyticsClass = ClassName("", "Analytics")
		val file = FileSpec.builder("", "Analytics")
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
		val dir = File("analytics/build/generated/source/generatedAnalytics/")
		Files.createDirectories(dir.toPath());
		val classFile = File("analytics/build/generated/source/generatedAnalytics/")
		file.writeTo(classFile)
	}

}