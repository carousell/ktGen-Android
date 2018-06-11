package com.thecarousell.plugin.task

import com.squareup.kotlinpoet.FileSpec
import java.io.File

object FileUtils {
	fun writeFiles(outDir: File, packageName: String, vararg array: FileSpec) {
		val dir = File("${outDir.absolutePath}/${packageName.replace('.', '/')}")
		if (!dir.exists() && !dir.mkdirs()) {
			throw IllegalStateException("Couldn't create dir: $dir");
		}
		val directoryFile = File(outDir.absolutePath)
		array.forEach {
			it.writeTo(directoryFile)
			System.out.println("Writing: $it")
		}
	}
}