# BookshelfLib
BookshelfLib项目
本项目由《阅读2.0》《阅读3.0》源码剥离而来，保留了源相关功能，去掉了界面UI。

# 20201214新增：
支持整本下载书源（bookSourceType = 100）
* 例如： *
```
[
	{
		"bookSourceName": "爱下电子书",
		"bookSourceType": 100,
		"bookSourceUrl": "https://m.aixdzs.com",
		"enabled": true,
		"ruleSearch": {
			"author": "class.book-author@text",
			"bookList": "@css:ul li:has(img)",
			"bookUrl": "h3@a@href",
			"coverUrl": "tag.img@src",
			"intro": "tag.p.0@a@text",
			"kind": "class.lightgreen@text",
			"lastChapter": "tag.p.2@a@text",
			"name": "h3@a@text",
		},
		"searchUrl": "/search/?k={{key}}&page={{page}}"
	},
	{
		"bookSourceName": "掌上书苑",
		"bookSourceType": 100,
		"bookSourceUrl": "https://www.soepub.com/",
		"enabled": true,
		"ruleSearch": {
			"author": "class.book-author@text",
			"bookList": "tag.table",
			"bookUrl": "@css:[data-trigger=hover]@href@0",
			"coverUrl": "tag.img@src",
			"intro": "",
			"kind": "",
			"lastChapter": "",
			"name": "div.single-line-text@a@href",
		},
		"searchUrl": "https://www.soepub.com/search?q={{key}}&p={{page}}"
	}
]
```
书源说明：整本下载类型，bookSourceType的值必须为100；在搜索到整本下载书籍后，开发者根据类型拦截下载地址，并将书籍保存本地