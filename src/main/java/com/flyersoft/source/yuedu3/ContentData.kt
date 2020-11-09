package com.flyersoft.source.yuedu3

data class ContentData<T>(
    var content: String = "",
    var nextUrl: T
)