package com.flyersoft.source.yuedu3

import java.util.regex.Pattern

object AppPattern {
    val JS_PATTERN: Pattern = Pattern.compile("(<js>[\\w\\W]*?</js>|@js:[\\w\\W]*$)", Pattern.CASE_INSENSITIVE)
    val EXP_PATTERN: Pattern = Pattern.compile("\\{\\{([\\w\\W]*?)\\}\\}")

    val authorRegex = "作\\s*者\\s*[：:]".toRegex()
    val nameRegex = Regex("\\s+作\\s*者.*")
    val fileNameRegex = Regex("[\\\\/:*?\"<>|.]")
    val splitGroupRegex = Regex("[,;，；]")
}