package com.thecarousell.plugin.model

data class EventList(
        val group: String,
        val events: List<Event>)

data class Event(
        val track: Track)

data class Track(
        val name: String,
        val type: String,
        val condition: String,
        val properties: List<Property>)

data class Property(
        val name: String,
        val description: String?,
        val type: String)