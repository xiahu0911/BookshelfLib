package com.flyersoft.source.service.web.utils


import com.flyersoft.source.R
import com.flyersoft.source.SourceApplication
import com.flyersoft.source.dao.SourceController
import com.flyersoft.source.yuedu3.*
import com.flyersort.source.gen.BookSourceDao
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoWSD
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.io.IOException


class SourceDebugWebSocket(handshakeRequest: NanoHTTPD.IHTTPSession) :
    NanoWSD.WebSocket(handshakeRequest),
    CoroutineScope by MainScope(),
    Debug.Callback {


    override fun onOpen() {
        launch(IO) {
            do {
                delay(30000)
                runCatching {
                    ping(byteArrayOf("ping".toByte()))
                }
            } while (isOpen)
        }
    }

    override fun onClose(
        code: NanoWSD.WebSocketFrame.CloseCode,
        reason: String,
        initiatedByRemote: Boolean
    ) {
        cancel()
    }

    override fun onMessage(message: NanoWSD.WebSocketFrame) {
        if (!message.textPayload.isJson()) return
        kotlin.runCatching {
            val debugBean = GSON.fromJsonObject<Map<String, String>>(message.textPayload)
            if (debugBean != null) {
                val tag = debugBean["tag"]
                val key = debugBean["key"]
                if (tag.isNullOrBlank() || key.isNullOrBlank()) {
                    kotlin.runCatching {
                        send(SourceApplication.INSTANCE.getString(R.string.cannot_empty))
                        close(NanoWSD.WebSocketFrame.CloseCode.NormalClosure, "调试结束", false)
                    }
                    return
                }
                SourceController.getInstance().getBookSourceByUrl(tag)?.let {
                    Debug.callback = this
                }
            }
        }
    }

    override fun onPong(pong: NanoWSD.WebSocketFrame) {

    }

    override fun onException(exception: IOException) {
//        Debug.cancelDebug(true)
    }

    override fun printLog(state: Int, msg: String) {
        kotlin.runCatching {
            send(msg)
            if (state == -1 || state == 1000) {
//                Debug.cancelDebug(true)
            }
        }
    }

}
