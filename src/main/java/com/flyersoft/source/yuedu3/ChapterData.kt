package com.flyersoft.source.yuedu3

import com.flyersoft.source.bean.BookChapterBean


data class ChapterData<T>(
    var chapterList: List<BookChapterBean>? = null,
    var nextUrl: T
)