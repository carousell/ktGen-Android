package com.thecarousell.plugin.task.generator

import com.squareup.kotlinpoet.*
import com.thecarousell.plugin.extensions.toCamelCase
import com.thecarousell.plugin.extensions.toCamelCaseCapitalized
import com.thecarousell.plugin.model.Event
import com.thecarousell.plugin.model.EventList
import com.thecarousell.plugin.utils.FileUtils
import com.thecarousell.plugin.utils.GenerateAnalyticsUtils
import java.io.File

class AnalyticsGenerator(private val outDir: File, private val packageName: String) {

    fun generateAnalyticsClasses(eventList: EventList) {
        val objectName = "${eventList.group.toCamelCaseCapitalized()}Events"
        val eventsFileBuilder = FileSpec.builder(packageName, objectName)
        val eventsObjectBuilder = TypeSpec.objectBuilder(objectName)
        val modelsFileBuilder = FileSpec.builder(packageName, "AnalyticsModels")

        eventList.events.forEach {
            modelsFileBuilder.addType(createDataModel(it))
            eventsObjectBuilder.addFunction(createFunction(it))
        }
        eventsFileBuilder.addType(eventsObjectBuilder.build())

        FileUtils.writeFiles(outDir, packageName, modelsFileBuilder.build(), eventsFileBuilder.build())
    }

    private fun createDataModel(event: Event): TypeSpec {
        val dataModelBuilder = TypeSpec
                .classBuilder("${event.track.name.toCamelCaseCapitalized()}Properties")
                .addModifiers(KModifier.DATA)
                .addKdoc("Properties of \"${event.track.name}\" event \n\n")
        val constructorBuilder = FunSpec.constructorBuilder()

        event.track.properties.forEach {
            val propertyName = it.name.toCamelCase()
            constructorBuilder
                    .addParameter(GenerateAnalyticsUtils.createParameterSpec(propertyName, it.type))
            dataModelBuilder
                    .addProperty(GenerateAnalyticsUtils.createProperty(propertyName, it.type))
                    .addKdoc("@property $propertyName ${it.description}\n")
        }

        return dataModelBuilder.primaryConstructor(constructorBuilder.build()).build()
    }

    private fun createFunction(event: Event): FunSpec {
        val funBuilder = FunSpec.builder(event.track.name.toCamelCase())
        funBuilder
                .addKdoc(event.track.condition)
                .addKdoc("\n")
                .addParameter("properties",
                        ClassName(packageName, "${event.track.name.toCamelCaseCapitalized()}Properties"))
                .addStatement("val map = HashMap<String, Any?>()")

        event.track.properties.forEach {
            funBuilder.addStatement("map.put(\"${it.name}\", properties.${it.name.toCamelCase()})")
        }
        funBuilder.addStatement("sendAnalyticsEvent(\"${event.track.name}\", \"${event.track.type}\", map)")

        return funBuilder.build()
    }

}