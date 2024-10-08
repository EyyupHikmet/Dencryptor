package com.rdiykru.dencryptor.core.extensions

import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope

suspend fun <T> sendEvent(channel: Channel<T>, event: T, tag: String) {
	coroutineScope {
		launch {
			channel.send(event)
			Log.d(tag, "EVENT SENT: $event")
		}
	}
}
