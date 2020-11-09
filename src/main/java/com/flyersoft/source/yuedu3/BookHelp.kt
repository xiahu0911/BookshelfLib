package com.flyersoft.source.yuedu3

import com.flyersoft.source.bean.BookChapterBean
import com.flyersoft.source.utils.FileUtils
import java.io.File

object BookHelp {

    fun formatBookName(name: String): String {
        return name
            .replace(AppPattern.nameRegex, "")
            .trim { it <= ' ' }
    }

    fun formatBookAuthor(author: String): String {
        return author
            .replace(AppPattern.authorRegex, "")
            .trim { it <= ' ' }
    }
}