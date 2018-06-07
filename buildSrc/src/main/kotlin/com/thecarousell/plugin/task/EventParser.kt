package com.thecarousell.plugin.task

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.thecarousell.plugin.model.EventList
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class EventParser {

    private val mapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())

    fun loadFromFile(file: File): EventList {
        System.out.println("loadFromFile $file")

        val bufferedReader = BufferedReader(FileReader(file))
        val eventList = mapper.readValue(bufferedReader, EventList::class.java)
        bufferedReader.close()
        return eventList
    }

}