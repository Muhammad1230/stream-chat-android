package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.core.internal.InternalStreamChatApi
import io.getstream.chat.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@InternalStreamChatApi
public class CoroutineCall<T : Any>(
    private val scope: CoroutineScope,
    private val runnable: suspend () -> Result<T>
) : Call<T> {

    private var job: Job? = null

    override fun cancel() {
        job?.cancel()
    }

    override fun execute(): Result<T> {
        return runBlocking { runnable() }
    }

    override fun enqueue(callback: (Result<T>) -> Unit) {
        job = scope.launch {
            val result = runnable()
            withContext(DispatcherProvider.Main) {
                callback(result)
            }
        }
    }
}
