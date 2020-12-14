package com.flyersoft.source.yuedu3

import com.flyersoft.source.SourceApplication
import com.flyersoft.source.bean.Cache
import com.flyersoft.source.dao.CacheController

@Suppress("unused")
object CacheManager {

    private val queryTTFMap = hashMapOf<String, Pair<Long, QueryTTF>>()

    /**
     * saveTime 单位为秒
     */
    @JvmOverloads
    fun put(key: String, value: Any, saveTime: Int = 0) {
        val deadline =
            if (saveTime == 0) 0 else System.currentTimeMillis() + saveTime * 1000
        when (value) {
            is QueryTTF -> queryTTFMap[key] = Pair(deadline, value)
            is ByteArray -> ACache.get(SourceApplication.INSTANCE).put(key, value, saveTime)
            else -> {
                val cache = Cache(key, value.toString(), deadline)
                CacheController.getInstance().insert(cache)
            }
        }
    }

    fun get(key: String): String? {
        return CacheController.getInstance().get(key, System.currentTimeMillis())
    }

    fun getInt(key: String): Int? {
        return get(key)?.toIntOrNull()
    }

    fun getLong(key: String): Long? {
        return get(key)?.toLongOrNull()
    }

    fun getDouble(key: String): Double? {
        return get(key)?.toDoubleOrNull()
    }

    fun getFloat(key: String): Float? {
        return get(key)?.toFloatOrNull()
    }

    fun getByteArray(key: String): ByteArray? {
        return ACache.get(SourceApplication.INSTANCE).getAsBinary(key)
    }

    fun getQueryTTF(key: String): QueryTTF? {
        val cache = queryTTFMap[key] ?: return null
        if (cache.first == 0L || cache.first > System.currentTimeMillis()) {
            return cache.second
        }
        return null
    }
}