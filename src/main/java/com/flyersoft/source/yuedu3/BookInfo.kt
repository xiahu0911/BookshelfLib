package com.flyersoft.source.yuedu3

import com.flyersoft.source.bean.BaseBookBean
import com.flyersoft.source.bean.BookInfoBean
import com.flyersoft.source.bean.BookSource
import com.flyersoft.source.utils.Loger
import kotlin.jvm.Throws

object BookInfo {

    @Throws(Exception::class)
    fun analyzeBookInfo(
        book: BookInfoBean,
        body: String?,
        bookSource: BookSource,
        baseUrl: String
    ) {
        body ?: throw Exception(
            "访问网站失败"
        )
        Loger.H(bookSource.bookSourceUrl + "≡获取成功:${baseUrl}")
        val infoRule = bookSource.bookInfoRule
        val analyzeRule = AnalyzeRule(book)
        analyzeRule.setContent(body, baseUrl)
        infoRule.init?.let {
            if (it.isNotEmpty()) {
                Loger.H(bookSource.bookSourceUrl + "≡执行详情页初始化规则")
                analyzeRule.setContent(analyzeRule.getElement(it))
            }
        }
        Loger.H(bookSource.bookSourceUrl + "┌获取书名")
        analyzeRule.getString(infoRule.name).let {
            if (it.isNotEmpty()) book.name = it
        }
        Loger.H(bookSource.bookSourceUrl + "└${book.name}")
        Loger.H(bookSource.bookSourceUrl + "┌获取作者")
        analyzeRule.getString(infoRule.author).let {
            if (it.isNotEmpty()) book.author = it.replace(AppPattern.authorRegex, "")
        }
        Loger.H(bookSource.bookSourceUrl + "└${book.author}")
        Loger.H(bookSource.bookSourceUrl + "┌获取分类")
        analyzeRule.getStringList(infoRule.kind)
            ?.joinToString(",")
            ?.let {
                if (it.isNotEmpty()) book.kind = it
            }
        Loger.H(bookSource.bookSourceUrl + "└${book.kind}")
        Loger.H(bookSource.bookSourceUrl + "┌获取字数")
        analyzeRule.getString(infoRule.wordCount).let {
            if (it.isNotEmpty()) book.wordCount = it
        }
        Loger.H(bookSource.bookSourceUrl + "└${book.wordCount}")
        Loger.H(bookSource.bookSourceUrl + "┌获取最新章节")
        analyzeRule.getString(infoRule.lastChapter).let {
            if (it.isNotEmpty()) book.latestChapterTitle = it
        }
        Loger.H(bookSource.bookSourceUrl + "└${book.latestChapterTitle}")
        Loger.H(bookSource.bookSourceUrl + "┌获取简介")
        analyzeRule.getString(infoRule.intro).let {
            if (it.isNotEmpty()) book.introduce = it.htmlFormat()
        }
        Loger.H(bookSource.bookSourceUrl + "└${book.introduce}")

        Loger.H(bookSource.bookSourceUrl + "┌获取封面链接")
        analyzeRule.getString(infoRule.coverUrl).let {
            if (it.isNotEmpty()) book.coverUrl = NetworkUtils.getAbsoluteURL(baseUrl, it)
        }
        Loger.H(bookSource.bookSourceUrl + "└${book.coverUrl}")
        Loger.H(bookSource.bookSourceUrl + "┌获取目录链接")
        book.chapterUrl = analyzeRule.getString(infoRule.tocUrl, true)
        if (book.chapterUrl.isEmpty()) book.chapterUrl = baseUrl
        if (book.chapterUrl == baseUrl) {
            book.chapterListHtml = body
        }
        Loger.H(bookSource.bookSourceUrl + "└${book.chapterUrl}")
    }

}