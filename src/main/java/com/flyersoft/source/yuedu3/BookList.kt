package com.flyersoft.source.yuedu3

import com.flyersoft.source.bean.BookSource
import com.flyersoft.source.bean.SearchBookBean
import com.flyersoft.source.utils.Loger
import com.flyersoft.source.yuedu3.StringUtils.wordCountFormat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive

object BookList {

    @Throws(Exception::class)
    fun analyzeBookList(
        scope: CoroutineScope,
        body: String?,
        bookSource: BookSource,
        analyzeUrl: AnalyzeUrl,
        baseUrl: String,
        variableBook: SearchBookBean,
        isSearch: Boolean = true
    ): ArrayList<SearchBookBean> {
        val bookList = ArrayList<SearchBookBean>()
        Loger.H("线程：" + Thread.currentThread().name)
        body ?: throw Exception(
            "访问网站失败"
        )
        Loger.showLog(bookSource.bookSourceUrl, "≡获取成功:${analyzeUrl.ruleUrl}")
        if (!scope.isActive) throw CancellationException()
        val analyzeRule = AnalyzeRule(variableBook)
        analyzeRule.setContent(body, baseUrl)
        bookSource.ruleBookUrlPattern?.let {
            if (baseUrl.matches(it.toRegex())) {
                Loger.showLog(bookSource.bookSourceUrl, "≡链接为详情页")
                getInfoItem(scope, analyzeRule, bookSource, baseUrl, variableBook.variable)
                    ?.let { searchBook ->
                        searchBook.bookInfoHtml = body
                        bookList.add(searchBook)
                    }
                return bookList
            }
        }
        val collections: List<Any>
        var reverse = false
        val bookListRule: BookListRule = when {
            isSearch -> bookSource.bookListRuleBySearch
            bookSource.bookListRuleByFind.bookList.isNullOrBlank() -> bookSource.bookListRuleBySearch
            else -> bookSource.bookListRuleByFind
        }
        var ruleList: String = bookListRule.bookList ?: ""
        if (ruleList.startsWith("-")) {
            reverse = true
            ruleList = ruleList.substring(1)
        }
        if (ruleList.startsWith("+")) {
            ruleList = ruleList.substring(1)
        }
        Loger.showLog(bookSource.bookSourceUrl, "┌获取书籍列表")
        collections = analyzeRule.getElements(ruleList)
        if (collections.isEmpty() && bookSource.ruleBookUrlPattern.isNullOrEmpty()) {
            Loger.showLog(bookSource.bookSourceUrl, "└列表为空,按详情页解析")
            getInfoItem(scope, analyzeRule, bookSource, baseUrl, variableBook.variable)
                ?.let { searchBook ->
                    searchBook.bookInfoHtml = body
                    bookList.add(searchBook)
                }
        } else {
            val ruleName = analyzeRule.splitSourceRule(bookListRule.name)
            val ruleBookUrl = analyzeRule.splitSourceRule(bookListRule.bookUrl)
            val ruleAuthor = analyzeRule.splitSourceRule(bookListRule.author)
            val ruleCoverUrl = analyzeRule.splitSourceRule(bookListRule.coverUrl)
            val ruleIntro = analyzeRule.splitSourceRule(bookListRule.intro)
            val ruleKind = analyzeRule.splitSourceRule(bookListRule.kind)
            val ruleLastChapter = analyzeRule.splitSourceRule(bookListRule.lastChapter)
            val ruleWordCount = analyzeRule.splitSourceRule(bookListRule.wordCount)
            Loger.showLog(bookSource.bookSourceUrl, "└列表大小:${collections.size}")
            for ((index, item) in collections.withIndex()) {
                if (!scope.isActive) throw CancellationException()
                getSearchItem(
                    scope,
                    item,
                    analyzeRule,
                    bookSource,
                    baseUrl,
                    variableBook.variable,
                    index == 0,
                    ruleName = ruleName,
                    ruleBookUrl = ruleBookUrl,
                    ruleAuthor = ruleAuthor,
                    ruleCoverUrl = ruleCoverUrl,
                    ruleIntro = ruleIntro,
                    ruleKind = ruleKind,
                    ruleLastChapter = ruleLastChapter,
                    ruleWordCount = ruleWordCount
                )?.let { searchBook ->
                    if (baseUrl == searchBook.noteUrl) {
                        searchBook.bookInfoHtml = body
                    }
                    bookList.add(searchBook)
                }
            }
            if (reverse) {
                bookList.reverse()
            }
        }
        return bookList
    }

    @Throws(Exception::class)
    private fun getInfoItem(
        scope: CoroutineScope,
        analyzeRule: AnalyzeRule,
        bookSource: BookSource,
        baseUrl: String,
        variable: String?
    ): SearchBookBean? {
        val searchBook = SearchBookBean()
        searchBook.variable = variable
        searchBook.noteUrl = baseUrl
        searchBook.origin = bookSource.bookSourceName
//        searchBook.originName = bookSource.bookSourceName
//        searchBook.originOrder = bookSource.customOrder
//        searchBook.type = bookSource.bookSourceType
        searchBook.tag = bookSource.bookSourceUrl
        analyzeRule.book = searchBook
        with(bookSource.getBookInfoRule()) {
            init?.let {
                if (it.isNotEmpty()) {
                    if (!scope.isActive) throw CancellationException()
                    Loger.showLog(bookSource.bookSourceUrl, "≡执行详情页初始化规则")
                    analyzeRule.setContent(analyzeRule.getElement(it))
                }
            }
            if (!scope.isActive) throw CancellationException()
            Loger.showLog(bookSource.bookSourceUrl, "┌获取书名")
            searchBook.name = BookHelp.formatBookName(analyzeRule.getString(name))
            Loger.showLog(bookSource.bookSourceUrl, "└${searchBook.name}")
            if (searchBook.name.isNotEmpty()) {
                if (!scope.isActive) throw CancellationException()
                Loger.showLog(bookSource.bookSourceUrl, "┌获取作者")
                searchBook.author = BookHelp.formatBookAuthor(analyzeRule.getString(author))
                Loger.showLog(bookSource.bookSourceUrl, "└${searchBook.author}")
                if (!scope.isActive) throw CancellationException()
                Loger.showLog(bookSource.bookSourceUrl, "┌获取分类")
                searchBook.kind = analyzeRule.getStringList(kind)?.joinToString(",")
                Loger.showLog(bookSource.bookSourceUrl, "└${searchBook.kind}")
                if (!scope.isActive) throw CancellationException()
                Loger.showLog(bookSource.bookSourceUrl, "┌获取字数")
                searchBook.wordCount = wordCountFormat(analyzeRule.getString(wordCount))
                Loger.showLog(bookSource.bookSourceUrl, "└${searchBook.wordCount}")
                if (!scope.isActive) throw CancellationException()
                Loger.showLog(bookSource.bookSourceUrl, "┌获取最新章节")
                searchBook.lastChapter = analyzeRule.getString(lastChapter)
                Loger.showLog(bookSource.bookSourceUrl, "└${searchBook.lastChapter}")
                if (!scope.isActive) throw CancellationException()
                Loger.showLog(bookSource.bookSourceUrl, "┌获取简介")
                searchBook.introduce = analyzeRule.getString(intro).htmlFormat()
                Loger.showLog(bookSource.bookSourceUrl, "└${searchBook.introduce}", true)
                if (!scope.isActive) throw CancellationException()
                Loger.showLog(bookSource.bookSourceUrl, "┌获取封面链接")
                searchBook.coverUrl = analyzeRule.getString(coverUrl, true)
                Loger.showLog(bookSource.bookSourceUrl, "└${searchBook.coverUrl}")
                return searchBook
            }
        }
        return null
    }

    @Throws(Exception::class)
    private fun getSearchItem(
        scope: CoroutineScope,
        item: Any,
        analyzeRule: AnalyzeRule,
        bookSource: BookSource,
        baseUrl: String,
        variable: String?,
        log: Boolean,
        ruleName: List<AnalyzeRule.SourceRule>,
        ruleBookUrl: List<AnalyzeRule.SourceRule>,
        ruleAuthor: List<AnalyzeRule.SourceRule>,
        ruleKind: List<AnalyzeRule.SourceRule>,
        ruleCoverUrl: List<AnalyzeRule.SourceRule>,
        ruleWordCount: List<AnalyzeRule.SourceRule>,
        ruleIntro: List<AnalyzeRule.SourceRule>,
        ruleLastChapter: List<AnalyzeRule.SourceRule>
    ): SearchBookBean? {
        val searchBook = SearchBookBean()
        searchBook.variable = variable
        searchBook.origin = bookSource.bookSourceName
//        searchBook.originName = bookSource.bookSourceName
//        searchBook.type = bookSource.bookSourceType
//        searchBook.originOrder = bookSource.customOrder
        searchBook.tag = bookSource.bookSourceUrl
        analyzeRule.book = searchBook
        analyzeRule.setContent(item)
        if (!scope.isActive) throw CancellationException()
        Loger.showLog(bookSource.bookSourceUrl, "┌获取书名", log)
        searchBook.name = BookHelp.formatBookName(analyzeRule.getString(ruleName))
        Loger.showLog(bookSource.bookSourceUrl, "└${searchBook.name}", log)
        if (searchBook.name.isNotEmpty()) {
            if (!scope.isActive) throw CancellationException()
            Loger.showLog(bookSource.bookSourceUrl, "┌获取作者", log)
            searchBook.author = BookHelp.formatBookAuthor(analyzeRule.getString(ruleAuthor))
            Loger.showLog(bookSource.bookSourceUrl, "└${searchBook.author}", log)
            if (!scope.isActive) throw CancellationException()
            Loger.showLog(bookSource.bookSourceUrl, "┌获取分类", log)
            searchBook.kind = analyzeRule.getStringList(ruleKind)?.joinToString(",")
            Loger.showLog(bookSource.bookSourceUrl, "└${searchBook.kind}", log)
            if (!scope.isActive) throw CancellationException()
            Loger.showLog(bookSource.bookSourceUrl, "┌获取字数", log)
            searchBook.wordCount = wordCountFormat(analyzeRule.getString(ruleWordCount))
            Loger.showLog(bookSource.bookSourceUrl, "└${searchBook.wordCount}", log)
            if (!scope.isActive) throw CancellationException()
            Loger.showLog(bookSource.bookSourceUrl, "┌获取最新章节", log)
            searchBook.lastChapter = analyzeRule.getString(ruleLastChapter)
            Loger.showLog(bookSource.bookSourceUrl, "└${searchBook.lastChapter}", log)
            if (!scope.isActive) throw CancellationException()
            Loger.showLog(bookSource.bookSourceUrl, "┌获取简介", log)
            searchBook.introduce = analyzeRule.getString(ruleIntro).htmlFormat()
            Loger.showLog(bookSource.bookSourceUrl, "└${searchBook.introduce}", log)
            if (!scope.isActive) throw CancellationException()
            Loger.showLog(bookSource.bookSourceUrl, "┌获取封面链接", log)
            analyzeRule.getString(ruleCoverUrl).let {
                if (it.isNotEmpty()) searchBook.coverUrl = NetworkUtils.getAbsoluteURL(baseUrl, it)
            }
            Loger.showLog(bookSource.bookSourceUrl, "└${searchBook.coverUrl}", log)
            if (!scope.isActive) throw CancellationException()
            Loger.showLog(bookSource.bookSourceUrl, "┌获取详情页链接", log)
            searchBook.noteUrl = analyzeRule.getString(ruleBookUrl, true)
            if (searchBook.noteUrl.isEmpty()) {
                searchBook.noteUrl = baseUrl
            }
            Loger.showLog(bookSource.bookSourceUrl, "└${searchBook.noteUrl}", log)
            return searchBook
        }
        return null
    }

}