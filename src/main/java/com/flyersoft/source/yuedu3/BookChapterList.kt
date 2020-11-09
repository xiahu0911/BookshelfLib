package com.flyersoft.source.yuedu3

import android.text.TextUtils
import com.flyersoft.source.bean.BookChapterBean
import com.flyersoft.source.bean.BookInfoBean
import com.flyersoft.source.bean.BookSource
import com.flyersoft.source.utils.Loger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object BookChapterList {

    suspend fun analyzeChapterList(
        coroutineScope: CoroutineScope,
        book: BookInfoBean,
        body: String?,
        bookSource: BookSource,
        baseUrl: String
    ): List<BookChapterBean> = suspendCancellableCoroutine { block ->
        try {
            val chapterList = ArrayList<BookChapterBean>()
            body ?: throw Exception(
                "访问网站失败"
            )
            Loger.showLog(bookSource.bookSourceUrl, "≡获取成功:${baseUrl}")

            val tocRule = bookSource.tocRule
            val nextUrlList = arrayListOf(baseUrl)
            var reverse = false
            var listRule = tocRule.chapterList ?: ""
            if (listRule.startsWith("-")) {
                reverse = true
                listRule = listRule.substring(1)
            }
            if (listRule.startsWith("+")) {
                listRule = listRule.substring(1)
            }
            var chapterData =
                analyzeChapterList(
                    book, baseUrl, body, tocRule, listRule, bookSource, log = true
                )
            chapterData.chapterList?.let {
                chapterList.addAll(it)
            }
            when (chapterData.nextUrl.size) {
                0 -> {
                    block.resume(finish(book, chapterList, reverse))
                }
                1 -> {
                    Coroutine.async(scope = coroutineScope) {
                        var nextUrl = chapterData.nextUrl[0]
                        while (nextUrl.isNotEmpty() && !nextUrlList.contains(nextUrl)) {
                            nextUrlList.add(nextUrl)
                            AnalyzeUrl(
                                ruleUrl = nextUrl,
                                book = book,
                                headerMapF = Tools3.getHeaderMap(bookSource)
                            ).getResponseAwait(bookSource.bookSourceUrl)
                                .body?.let { nextBody ->
                                    chapterData = analyzeChapterList(
                                        book, nextUrl, nextBody, tocRule, listRule, bookSource
                                    )
                                    nextUrl = if (chapterData.nextUrl.isNotEmpty()) {
                                        chapterData.nextUrl[0]
                                    } else ""
                                    chapterData.chapterList?.let {
                                        chapterList.addAll(it)
                                    }
                                }
                        }
                        Loger.showLog(bookSource.bookSourceUrl, "◇目录总页数:${nextUrlList.size}")
                        block.resume(finish(book, chapterList, reverse))
                    }.onError {
                        block.resumeWithException(it)
                    }
                }
                else -> {
                    val chapterDataList = arrayListOf<ChapterData<String>>()
                    for (item in chapterData.nextUrl) {
                        if (!nextUrlList.contains(item)) {
                            val data = ChapterData(nextUrl = item)
                            chapterDataList.add(data)
                            nextUrlList.add(item)
                        }
                    }
                    Loger.showLog(bookSource.bookSourceUrl, "◇目录总页数:${nextUrlList.size}")
                    for (item in chapterDataList) {
                        downloadToc(
                            coroutineScope,
                            item,
                            book,
                            bookSource,
                            tocRule,
                            listRule,
                            chapterList,
                            chapterDataList,
                            {
                                block.resume(finish(book, chapterList, reverse))
                            }, {
                                block.cancel(it)
                            })
                    }
                }
            }
        } catch (e: Exception) {
            block.resumeWithException(e)
        }
    }

    private fun downloadToc(
        coroutineScope: CoroutineScope,
        chapterData: ChapterData<String>,
        book: BookInfoBean,
        bookSource: BookSource,
        tocRule: TocRule,
        listRule: String,
        chapterList: ArrayList<BookChapterBean>,
        chapterDataList: ArrayList<ChapterData<String>>,
        onFinish: () -> Unit,
        onError: (e: Throwable) -> Unit
    ) {
        Coroutine.async(scope = coroutineScope) {
            val nextBody = AnalyzeUrl(
                ruleUrl = chapterData.nextUrl,
                book = book,
                headerMapF = Tools3.getHeaderMap(bookSource)
            ).getResponseAwait(bookSource.bookSourceUrl).body
                ?: throw Exception("${chapterData.nextUrl}, 下载失败")
            val nextChapterData = analyzeChapterList(
                book, chapterData.nextUrl, nextBody, tocRule, listRule, bookSource,
                false
            )
            synchronized(chapterDataList) {
                val isFinished = addChapterListIsFinish(
                    chapterDataList,
                    chapterData,
                    nextChapterData.chapterList
                )
                if (isFinished) {
                    chapterDataList.forEach { item ->
                        item.chapterList?.let {
                            chapterList.addAll(it)
                        }
                    }
                    onFinish()
                }
            }
        }.onError {
            onError(it)
        }
    }

    private fun addChapterListIsFinish(
        chapterDataList: ArrayList<ChapterData<String>>,
        chapterData: ChapterData<String>,
        chapterList: List<BookChapterBean>?
    ): Boolean {
        chapterData.chapterList = chapterList
        chapterDataList.forEach {
            if (it.chapterList == null) {
                return false
            }
        }
        return true
    }

    private fun finish(
        book: BookInfoBean,
        chapterList: ArrayList<BookChapterBean>,
        reverse: Boolean
    ): ArrayList<BookChapterBean> {
        //去重
        if (!reverse) {
            chapterList.reverse()
        }
        val lh = LinkedHashSet(chapterList)
        val list = ArrayList(lh)
        list.reverse()
        Loger.showLog(book.origin, "◇目录总数:${list.size}")
        for ((index, item) in list.withIndex()) {
            item.index = index
        }
        book.latestChapterTitle = list.last().title
//        book.durChapterTitle =
//            list.getOrNull(book.durChapterIndex)?.title ?: book.latestChapterTitle
//        if (book.totalChapterNum < list.size) {
//            book.lastCheckCount = list.size - book.totalChapterNum
//            book.latestChapterTime = System.currentTimeMillis()
//        }
        book.totalChapterNum = list.size
        return list
    }

    private fun analyzeChapterList(
        book: BookInfoBean,
        baseUrl: String,
        body: String,
        tocRule: TocRule,
        listRule: String,
        bookSource: BookSource,
        getNextUrl: Boolean = true,
        log: Boolean = false
    ): ChapterData<List<String>> {
        val analyzeRule = AnalyzeRule(book)
        analyzeRule.setContent(body, baseUrl)
        val chapterList = arrayListOf<BookChapterBean>()
        val nextUrlList = arrayListOf<String>()
        val nextTocRule = tocRule.nextTocUrl
        if (getNextUrl && !nextTocRule.isNullOrEmpty()) {
            Loger.showLog(bookSource.bookSourceUrl, "┌获取目录下一页列表")
            analyzeRule.getStringList(nextTocRule, true)?.let {
                for (item in it) {
                    if (item != baseUrl) {
                        nextUrlList.add(item)
                    }
                }
            }
            Loger.showLog(
                bookSource.bookSourceUrl,
                "└" + TextUtils.join("，\n", nextUrlList)
            )
        }
        Loger.showLog(bookSource.bookSourceUrl, "┌获取目录列表")
        val elements = analyzeRule.getElements(listRule)
        Loger.showLog(bookSource.bookSourceUrl, "└列表大小:${elements.size}")
        if (elements.isNotEmpty()) {
            Loger.showLog(bookSource.bookSourceUrl, "┌获取首章名称")
            val nameRule = analyzeRule.splitSourceRule(tocRule.chapterName)
            val urlRule = analyzeRule.splitSourceRule(tocRule.chapterUrl)
            val vipRule = analyzeRule.splitSourceRule(tocRule.isVip)
            val update = analyzeRule.splitSourceRule(tocRule.updateTime)
            var isVip: String?
            for (item in elements) {
                analyzeRule.setContent(item)
                val bookChapter = BookChapterBean()
                bookChapter.noteUrl = book.noteUrl
                analyzeRule.chapter = bookChapter
                bookChapter.title = analyzeRule.getString(nameRule)
                bookChapter.url = analyzeRule.getString(urlRule, true)
                bookChapter.tag = bookSource.bookSourceUrl
                isVip = analyzeRule.getString(vipRule)
                if (bookChapter.url.isEmpty()) bookChapter.url = baseUrl
                if (bookChapter.title.isNotEmpty()) {
                    if (isVip.isNotEmpty() && isVip != "null" && isVip != "false" && isVip != "0") {
                        bookChapter.title = "\uD83D\uDD12" + bookChapter.title
                    }
                    chapterList.add(bookChapter)
                }
            }
            Loger.showLog(bookSource.bookSourceUrl, "└${chapterList[0].title}")
            Loger.showLog(bookSource.bookSourceUrl, "┌获取首章链接")
            Loger.showLog(bookSource.bookSourceUrl, "└${chapterList[0].url}")
            Loger.showLog(bookSource.bookSourceUrl, "┌获取首章信息")
            Loger.showLog(bookSource.bookSourceUrl, "└${chapterList[0].tag}")
        }
        return ChapterData(chapterList, nextUrlList)
    }

}