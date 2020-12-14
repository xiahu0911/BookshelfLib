package com.flyersoft.source.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.flyersoft.source.yuedu3.Coroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

abstract class BaseService : Service(), CoroutineScope by MainScope() {

    fun <T> execute(
        scope: CoroutineScope = this,
        context: CoroutineContext = Dispatchers.IO,
        block: suspend CoroutineScope.() -> T
    ) = Coroutine.async(scope, context) { block() }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}