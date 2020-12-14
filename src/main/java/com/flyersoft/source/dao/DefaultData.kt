package com.flyersoft.source.dao

import com.flyersoft.source.SourceApplication
import com.flyersoft.source.bean.HttpTTS
import com.flyersoft.source.yuedu3.GSON
import com.flyersoft.source.yuedu3.fromJsonArray

object DefaultData {

    const val httpTtsFileName = "httpTTS.json"

    val httpTTS by lazy {
        val json =
            String(
                SourceApplication.INSTANCE.assets.open("$httpTtsFileName")
                    .readBytes()
            )
        val fromJsonArray = GSON.fromJsonArray<HttpTTS>(json)!!
        fromJsonArray
    }

}