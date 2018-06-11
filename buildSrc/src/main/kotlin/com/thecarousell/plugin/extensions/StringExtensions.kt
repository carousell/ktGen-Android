package com.thecarousell.plugin.extensions

fun String.toCamelCase(): String {
    return this.toCamelCaseCapitalized().decapitalize()
}

fun String.toCamelCaseCapitalized(): String {
    return this.split('_').joinToString("") {
        it.capitalize()
    }
}