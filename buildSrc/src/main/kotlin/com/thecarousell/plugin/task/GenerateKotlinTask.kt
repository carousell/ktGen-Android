package com.thecarousell.plugin.task

import com.thecarousell.plugin.task.generator.AnalyticsGenerator
import com.thecarousell.plugin.task.parser.EventParser
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

		val analyticsGenerator = AnalyticsGenerator(outDir, packageName)

		srcDir.walk().forEach {
			if (it.name.endsWith(".yaml", true)) try {
				val eventList = EventParser.loadFromFile(it)
				analyticsGenerator.generateAnalyticsClass(eventList)
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}
}