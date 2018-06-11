package com.thecarousell.ktgen.analytics

import android.util.Log

fun sendAnalyticsEvent(name: String, type: String, properties: HashMap<String, Any?>) {
    Log.d("EventSender", "Send event \"$name\" ($type) with properties: $properties")
}