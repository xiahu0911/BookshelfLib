package com.flyersoft.source.yuedu3

import com.flyersoft.source.bean.BaseBookBean
import com.flyersoft.source.bean.BookInfoBean
import com.flyersoft.source.bean.BookSource
import com.flyersoft.source.utils.Loger
import com.flyersoft.source.yuedu3.StringUtils.wordCountFormat
import kotlin.jvm.Throws

object BookInfo {

    @Throws(Exception::class)
    fun analyzeBookInfo(
        book: BookInfoBean,
        body: String?,
        bookSource: BookSource,
        baseUrl: String,
        canReName: Boolean
    ) {
        body ?: throw Exception(
            "访问网站失败"
        )
        Debug.log(bookSource.bookSourceUrl, "≡获取成功:${baseUrl}")
        val infoRule = bookSource.getBookInfoRule()
        val analyzeRule = AnalyzeRule(book)
        analyzeRule.setContent(body).setBaseUrl(baseUrl)
        infoRule.init?.let {
            if (it.isNotBlank()) {
                Debug.log(bookSource.bookSourceUrl, "≡执行详情页初始化规则")
                analyzeRule.setContent(analyzeRule.getElement(it))
            }
        }
        Debug.log(bookSource.bookSourceUrl, "┌获取书名")
        BookHelp.formatBookName(analyzeRule.getString(infoRule.name)).let {
            if (it.isNotEmpty() && (canReName || book.name.isEmpty())) {
                book.name = it
            }
        }
        Debug.log(bookSource.bookSourceUrl, "└${book.name}")
        Debug.log(bookSource.bookSourceUrl, "┌获取作者")
        BookHelp.formatBookAuthor(analyzeRule.getString(infoRule.author)).let {
            if (it.isNotEmpty() && (canReName || book.name.isEmpty())) {
                book.author = it
            }
        }
        Debug.log(bookSource.bookSourceUrl, "└${book.author}")
        Debug.log(bookSource.bookSourceUrl, "┌获取分类")
        analyzeRule.getStringList(infoRule.kind)
            ?.joinToString(",")
            ?.let {
                if (it.isNotEmpty()) book.kind = it
            }
        Debug.log(bookSource.bookSourceUrl, "└${book.kind}")
        Debug.log(bookSource.bookSourceUrl, "┌获取字数")
        wordCountFormat(analyzeRule.getString(infoRule.wordCount)).let {
            if (it.isNotEmpty()) book.wordCount = it
        }
        Debug.log(bookSource.bookSourceUrl, "└${book.wordCount}")
        Debug.log(bookSource.bookSourceUrl, "┌获取最新章节")
        analyzeRule.getString(infoRule.lastChapter).let {
            if (it.isNotEmpty()) book.latestChapterTitle = it
        }
        Debug.log(bookSource.bookSourceUrl, "└${book.latestChapterTitle}")
        Debug.log(bookSource.bookSourceUrl, "┌获取简介")
        analyzeRule.getString(infoRule.intro).let {
            if (it.isNotEmpty()) book.introduce = it.htmlFormat()
        }
        Debug.log(bookSource.bookSourceUrl, "└${book.introduce}", isHtml = true)

        Debug.log(bookSource.bookSourceUrl, "┌获取封面链接")
        analyzeRule.getString(infoRule.coverUrl).let {
            if (it.isNotEmpty()) book.coverUrl = NetworkUtils.getAbsoluteURL(baseUrl, it)
        }
        Debug.log(bookSource.bookSourceUrl, "└${book.coverUrl}")
        Debug.log(bookSource.bookSourceUrl, "┌获取目录链接")
        book.chapterUrl = analyzeRule.getString(infoRule.tocUrl, true)
        if (book.chapterUrl.isEmpty()) book.chapterUrl = baseUrl
        if (book.chapterUrl == baseUrl) {
            book.chapterListHtml = body
        }
        Debug.log(bookSource.bookSourceUrl, "└${book.chapterUrl}")
    }

}