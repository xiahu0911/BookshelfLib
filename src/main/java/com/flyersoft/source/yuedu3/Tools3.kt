package com.flyersoft.source.yuedu3

import com.flyersoft.source.bean.BookSource
import com.flyersoft.source.conf.Consts
import com.flyersoft.source.conf.Keys
import com.flyersoft.source.utils.SPUtils
import java.util.*
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.SimpleBindings

object Tools3 {
    val UA_NAME = "User-Agent"

    val SCRIPT_ENGINE: ScriptEngine by lazy {
        ScriptEngineManager().getEngineByName("rhino")
    }

    @Throws(Exception::class)
    fun getHeaderMap(bookSource: BookSource): Map<String, String> {
        val headerMap = HashMap<String, String>()
        headerMap[UA_NAME] = SPUtils.getInformain(
            Keys.SP_KEY_USER_AGENT,
            Consts.DEFAULT_USER_AGENT
        ) ?: bookSource.httpUserAgent
        bookSource.httpUserAgent?.let {
            val header1 = when {
                it.startsWith("@js:", true) ->
                    evalJS(it.substring(4)).toString()
                it.startsWith("<js>", true) ->
                    evalJS(it.substring(4, it.lastIndexOf("<"))).toString()
                else -> it
            }
            GSON.fromJsonObject<Map<String, String>>(header1)?.let { map ->
                headerMap.putAll(map)
            }
        }
        return headerMap
    }


    /**
     * 执行JS
     */
    @Throws(Exception::class)
    private fun evalJS(jsStr: String): Any {
        val bindings = SimpleBindings()
        bindings["java"] = this
        return SCRIPT_ENGINE.eval(jsStr, bindings)
    }
}