package com.flyersoft.source.yuedu3

import android.annotation.SuppressLint
import com.flyersoft.source.bean.BaseBookBean
import com.flyersoft.source.bean.BookChapterBean
import com.flyersoft.source.bean.BookInfoBean
import java.text.SimpleDateFormat
import java.util.*

object Debug {
    private var debugSource: String? = null
    var callback: Callback? = null

    @SuppressLint("ConstantLocale")
    private val DEBUG_TIME_FORMAT = SimpleDateFormat("[mm:ss.SSS]", Locale.getDefault())
    private var startTime: Long = System.currentTimeMillis()

    @Synchronized
    fun log(
        sourceUrl: String?,
        msg: String? = "",
        print: Boolean = true,
        isHtml: Boolean = false,
        showTime: Boolean = true,
        state: Int = 1
    ) {
        if (debugSource != sourceUrl || callback == null || !print) return
        var printMsg = msg ?: ""
        if (isHtml) {
            printMsg = printMsg.htmlFormat()
        }
        if (showTime) {
            printMsg =
                "${DEBUG_TIME_FORMAT.format(Date(System.currentTimeMillis() - startTime))} $printMsg"
        }
        callback?.printLog(state, printMsg)
    }

    interface Callback {
        fun printLog(state: Int, msg: String)
    }
}