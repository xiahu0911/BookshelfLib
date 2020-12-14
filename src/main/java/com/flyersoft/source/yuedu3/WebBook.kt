package com.flyersoft.source.yuedu3

import com.flyersoft.source.bean.*
import com.flyersoft.source.utils.Loger
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

class WebBook(val bookSource: BookSource) {

    private val sourceUrl: String
        get() = bookSource.bookSourceUrl

    companion object {
        private val variableBookMap = hashMapOf<String, SearchBookBean>()
    }


//    fun <T> execute(
//        scope: CoroutineScope = this,
//        context: CoroutineContext = Dispatchers.Default,
//        block: suspend CoroutineScope.() -> T
//    ): Coroutine<T> {
//        return Coroutine.async(scope, context) { block() }
//    }


    private fun getVariableBook(sourceUrl: String): SearchBookBean {
        var vBook = variableBookMap[sourceUrl]
        if (vBook == null) {
            vBook = SearchBookBean()
            variableBookMap[sourceUrl] = vBook
        }
        return vBook
    }

    /**
     * 搜索
     */
    fun searchBook(
        key: String,
        page: Int? = 1,
        callback: CallBack<List<SearchBookBean>>
    ) {
        bookSource.ruleSearchUrl?.let { ruleSearchUrl ->
            val analyzeUrl = AnalyzeUrl(
                ruleUrl = ruleSearchUrl,
                key = key,
                page = page,
                baseUrl = sourceUrl,
                book = getVariableBook(bookSource.bookSourceUrl),
                headerMapF = Tools3.getHeaderMap(bookSource)
            )
            var it = arrayListOf<SearchBookBean>()
            runBlocking {
                Loger.showLog(
                    "xxxxx",
                    Thread.currentThread().id.toString() + "：" + bookSource.bookSourceName
                )
                val res = analyzeUrl.getResponseAwait(bookSource.bookSourceUrl)
                it = BookList.analyzeBookList(
                    this,
                    res.body,
                    bookSource,
                    analyzeUrl,
                    res.url,
                    getVariableBook(bookSource.bookSourceUrl),
                    true
                )
            }
            callback.callBack(it)
        } ?: callback.callBack(arrayListOf())
    }

    /**
     * 发现
     */
    fun exploreBook(
        url: String,
        page: Int? = 1,
        callback: CallBack<List<SearchBookBean>>
    ) {
        val analyzeUrl = AnalyzeUrl(
            ruleUrl = url,
            page = page,
            baseUrl = sourceUrl,
            book = getVariableBook(bookSource.bookSourceUrl),
            headerMapF = Tools3.getHeaderMap(bookSource)
        )
        runBlocking {
            val res = analyzeUrl.getResponseAwait(bookSource.bookSourceUrl)
            val it = BookList.analyzeBookList(
                this,
                res.body,
                bookSource,
                analyzeUrl,
                res.url,
                getVariableBook(bookSource.bookSourceUrl),
                false
            )
            callback.callBack(it)
        }
    }

    /**
     * 书籍信息
     */
    fun getBookInfo(
        book: BookInfoBean,
        callback: CallBack<BookInfoBean>
    ) {
        book.bookSourceType = bookSource.bookSourceType
        if (!book.bookInfoHtml.isNullOrEmpty()) {
            BookInfo.analyzeBookInfo(book, book.bookInfoHtml, bookSource, book.noteUrl, false)
            callback.callBack(book)
        } else {
            val analyzeUrl = AnalyzeUrl(
                book = book,
                ruleUrl = book.noteUrl,
                baseUrl = sourceUrl,
                headerMapF = Tools3.getHeaderMap(bookSource)
            )
            runBlocking {
                val it = analyzeUrl.getResponseAwait(bookSource.bookSourceUrl)
                BookInfo.analyzeBookInfo(book, it.body, bookSource, book.noteUrl, false)
                callback.callBack(book)
            }
        }
    }

    /**
     * 目录
     */
    fun getChapterList(
        book: BookInfoBean,
        callback: CallBack<List<BookChapterBean>>
    ) {
        runBlocking {
            book.bookSourceType = bookSource.bookSourceType
            if (book.noteUrl == book.chapterUrl && !book.chapterListHtml.isNullOrEmpty()) {
                callback.callBack(
                    BookChapterList.analyzeChapterList(
                        this,
                        book,
                        book.chapterListHtml,
                        bookSource,
                        book.chapterUrl
                    )
                )
            } else {
                val it = AnalyzeUrl(
                    book = book,
                    ruleUrl = book.chapterUrl,
                    baseUrl = book.noteUrl,
                    headerMapF = Tools3.getHeaderMap(bookSource)
                ).getResponseAwait(bookSource.bookSourceUrl);
                callback.callBack(
                    BookChapterList.analyzeChapterList(
                        this,
                        book,
                        it.body,
                        bookSource,
                        book.chapterUrl
                    )
                )
            }
        }
    }

    /**+
     * 章节内容
     */
    fun getContent(
        book: BookInfoBean,
        bookChapter: BookChapterBean,
        nextChapterUrl: String? = null,
        isTest: Boolean = false,
        callback: CallBack<String>
    ) {
        getContentSuspend(
            book, bookChapter, nextChapterUrl, isTest, callback
        )
    }

    /**
     * 章节内容
     */
    private fun getContentSuspend(
        book: BookInfoBean,
        bookChapter: BookChapterBean,
        nextChapterUrl: String? = null,
        isTest: Boolean = false,
        callback: CallBack<String>
    ) {
        runBlocking {
            if (bookSource.contentRule.content.isNullOrEmpty()) {
                Loger.H(sourceUrl + "⇒正文规则为空,使用章节链接:${bookChapter.url}")
                callback.callBack(bookChapter.url)
            }
            val body =
                if (bookChapter.url == book.noteUrl && !book.chapterListHtml.isNullOrEmpty()) {
                    book.chapterListHtml
                } else {
                    val analyzeUrl =
                        AnalyzeUrl(
                            book = book,
                            ruleUrl = bookChapter.url,
                            baseUrl = book.chapterUrl?.let { it } ?: book.noteUrl,
                            headerMapF = Tools3.getHeaderMap(bookSource)
                        )
                    analyzeUrl.getResponseAwait(
                        bookSource.bookSourceUrl,
                        jsStr = bookSource.contentRule.webJs,
                        sourceRegex = bookSource.contentRule.sourceRegex
                    ).body
                }
            callback.callBack(
                BookContent.analyzeContent(
                    this,
                    body,
                    book,
                    bookChapter,
                    bookSource,
                    bookChapter.url,
                    nextChapterUrl
                )
            )
        }
    }
}