package com.flyersoft.source.yuedu3

import org.greenrobot.greendao.annotation.Entity
import org.greenrobot.greendao.annotation.Id


@Entity
data class Cookie(
    @Id
    var url: String = "",
    var cookie: String = ""
)