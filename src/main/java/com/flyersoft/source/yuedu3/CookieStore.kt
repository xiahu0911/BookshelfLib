package com.flyersoft.source.yuedu3

import android.text.TextUtils
import com.flyersoft.source.bean.CookieBean
import com.flyersoft.source.dao.CookieController
import com.flyersoft.source.dao.DaoController
import com.franmontiel.persistentcookiejar.persistence.CookiePersistor
import com.franmontiel.persistentcookiejar.persistence.SerializableCookie


object CookieStore : CookiePersistor {

    fun setCookie(url: String, cookie: String?) {
        Coroutine.async {
            val cookieBean = Cookie(NetworkUtils.getSubDomain(url), cookie ?: "")
//            App.db.cookieDao().insert(cookieBean)
        }
    }

    fun replaceCookie(url: String, cookie: String) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(cookie)) {
            return
        }
        val oldCookie = getCookie(url)
        if (TextUtils.isEmpty(oldCookie)) {
            setCookie(url, cookie)
        } else {
            val cookieMap = cookieToMap(oldCookie)
            cookieMap.putAll(cookieToMap(cookie))
            val newCookie = mapToCookie(cookieMap)
            setCookie(url, newCookie)
        }
    }

    fun getCookie(url: String): String {
        val cookieBean = CookieController.getCookie(url)
        return cookieBean?.cookie ?: ""
    }

    fun removeCookie(url: String) {
        CookieController.del(NetworkUtils.getSubDomain(url))
    }

    fun cookieToMap(cookie: String): MutableMap<String, String> {
        val cookieMap = mutableMapOf<String, String>()
        if (cookie.isBlank()) {
            return cookieMap
        }
        val pairArray = cookie.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (pair in pairArray) {
            val pairs = pair.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (pairs.size == 1) {
                continue
            }
            val key = pairs[0].trim { it <= ' ' }
            val value = pairs[1]
            if (value.isNotBlank() || value.trim { it <= ' ' } == "null") {
                cookieMap[key] = value.trim { it <= ' ' }
            }
        }
        return cookieMap
    }

    fun mapToCookie(cookieMap: Map<String, String>?): String? {
        if (cookieMap == null || cookieMap.isEmpty()) {
            return null
        }
        val builder = StringBuilder()
        for (key in cookieMap.keys) {
            val value = cookieMap[key]
            if (value?.isNotBlank() == true) {
                builder.append(key)
                    .append("=")
                    .append(value)
                    .append(";")
            }
        }
        return builder.deleteCharAt(builder.lastIndexOf(";")).toString()
    }

    override fun loadAll(): MutableList<okhttp3.Cookie> {
        val cookies = arrayListOf<okhttp3.Cookie>()
        CookieController.getOkHttpCookies().forEach {
            val serializedCookie = it.cookie
            SerializableCookie().decode(serializedCookie)?.let { ck ->
                cookies.add(ck)
            }
        }
        return cookies
    }

    override fun saveAll(cookies: MutableCollection<okhttp3.Cookie>?) {
        val mCookies = arrayListOf<CookieBean>()
        cookies?.forEach {
            mCookies.add(CookieBean(createCookieKey(it), SerializableCookie().encode(it)))
        }
        CookieController.insertOrReplace(*mCookies.toTypedArray())
    }

    override fun removeAll(cookies: MutableCollection<okhttp3.Cookie>?) {
        cookies?.forEach {
            CookieController.del(createCookieKey(it))
        }
    }

    override fun clear() {
        CookieController.deleteOkHttp()
    }

    private fun createCookieKey(cookie: okhttp3.Cookie): String {
        return (if (cookie.secure()) "https" else "http") + "://" + cookie.domain() + cookie.path() + "|" + cookie.name()
    }
}