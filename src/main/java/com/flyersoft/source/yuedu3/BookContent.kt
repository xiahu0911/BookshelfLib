package com.flyersoft.source.yuedu3

import com.flyersoft.source.bean.BaseBookBean
import com.flyersoft.source.bean.BookChapterBean
import com.flyersoft.source.bean.BookInfoBean
import com.flyersoft.source.bean.BookSource
import com.flyersoft.source.utils.Loger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext


object BookContent {

    @Throws(Exception::class)
    suspend fun analyzeContent(
        coroutineScope: CoroutineScope,
        body: String?,
        book: BookInfoBean,
        bookChapter: BookChapterBean,
        bookSource: BookSource,
        baseUrl: String,
        nextChapterUrlF: String? = null
    ): String {
        body ?: throw Exception(
            "访问网站失败"
        )
        Loger.H(bookSource.bookSourceUrl + "≡获取成功:${baseUrl}")
        val content = StringBuilder()
        val nextUrlList = arrayListOf(baseUrl)
        val contentRule = bookSource.getContentRule()
        var contentData = analyzeContent(
            book, baseUrl, body, contentRule, bookChapter, bookSource
        )
        content.append(contentData.content).append("\n")

        if (contentData.nextUrl.size == 1) {
            var nextUrl = contentData.nextUrl[0]
            val nextChapterUrl = if (!nextChapterUrlF.isNullOrEmpty())
                nextChapterUrlF
            else
                ""
            while (nextUrl.isNotEmpty() && !nextUrlList.contains(nextUrl)) {
                if (!nextChapterUrl.isNullOrEmpty()
                    && NetworkUtils.getAbsoluteURL(baseUrl, nextUrl)
                    == NetworkUtils.getAbsoluteURL(baseUrl, nextChapterUrl)
                ) break
                nextUrlList.add(nextUrl)
                AnalyzeUrl(
                    ruleUrl = nextUrl,
                    book = book,
                    headerMapF = Tools3.getHeaderMap(bookSource)
                ).getResponseAwait(bookSource.bookSourceUrl)
                    .body?.let { nextBody ->
                        contentData =
                            analyzeContent(
                                book, nextUrl, nextBody, contentRule, bookChapter, bookSource, false
                            )
                        nextUrl =
                            if (contentData.nextUrl.isNotEmpty()) contentData.nextUrl[0] else ""
                        content.append(contentData.content).append("\n")
                    }
            }
            Loger.H(bookSource.bookSourceUrl+ "◇本章总页数:${nextUrlList.size}")
        } else if (contentData.nextUrl.size > 1) {
            val contentDataList = arrayListOf<ContentData<String>>()
            for (item in contentData.nextUrl) {
                if (!nextUrlList.contains(item))
                    contentDataList.add(ContentData(nextUrl = item))
            }
            for (item in contentDataList) {
                withContext(coroutineScope.coroutineContext) {
                    AnalyzeUrl(
                        ruleUrl = item.nextUrl,
                        book = book,
                        headerMapF = Tools3.getHeaderMap(bookSource)
                    ).getResponseAwait(bookSource.bookSourceUrl)
                        .body?.let {
                            contentData =
                                analyzeContent(
                                    book, item.nextUrl, it, contentRule, bookChapter, bookSource, false
                                )
                            item.content = contentData.content
                        }
                }
            }
            for (item in contentDataList) {
                content.append(item.content).append("\n")
            }
        }
        content.deleteCharAt(content.length - 1)
        var contentStr = content.toString().htmlFormat()
        val replaceRegex = bookSource.ruleBookContentReplaceRegex
        if (!replaceRegex.isNullOrEmpty()) {
            val analyzeRule = AnalyzeRule(book)
            analyzeRule.setContent(contentStr, baseUrl)
            analyzeRule.chapter = bookChapter
            contentStr = analyzeRule.getString(replaceRegex)
        }
        Loger.H(bookSource.bookSourceUrl+ "┌获取章节名称")
        Loger.H(bookSource.bookSourceUrl+ "└${bookChapter.title}")
        Loger.H(bookSource.bookSourceUrl+ "┌获取正文内容")
        Loger.H(bookSource.bookSourceUrl+ "└\n$contentStr")
        return contentStr
    }

    @Throws(Throwable::class)
    private fun analyzeContent(
        book: BookInfoBean,
        baseUrl: String,
        body: String,
        contentRule: ContentRule,
        chapter: BookChapterBean,
        bookSource: BookSource,
        printLog: Boolean = true
    ): ContentData<List<String>> {
        val analyzeRule = AnalyzeRule(book)
        analyzeRule.setContent(body, baseUrl)
        val nextUrlList = arrayListOf<String>()
        analyzeRule.chapter = chapter
        val nextUrlRule = contentRule.nextContentUrl
        if (!nextUrlRule.isNullOrEmpty()) {
            Loger.H(bookSource.bookSourceUrl + "┌获取正文下一页链接")
            analyzeRule.getStringList(nextUrlRule, true)?.let {
                nextUrlList.addAll(it)
            }
            Loger.H(bookSource.bookSourceUrl + "└" + nextUrlList.joinToString("，"))
        }
        val content = analyzeRule.getString(contentRule.content)
        return ContentData(content, nextUrlList)
    }
}