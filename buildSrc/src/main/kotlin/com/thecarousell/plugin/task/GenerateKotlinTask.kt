package com.thecarousell.plugin.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException

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
            if (it.name.endsWith(".yaml", true)) {
                try {
                    eventParser.loadFromFile(it)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

}