package com.codegames.farsroid.util

import com.codegames.farsroid.model.App
import lib.codegames.extension.fanum
import org.jsoup.nodes.Document

object Parser {

    fun appList(doc: Document): Array<App> {

        val appDocuments = doc.select("article.single_app")
        val appList = mutableListOf<App>()

        appDocuments.forEach {

            val name = it.selectFirst(".title h1").attr("data-header_title").trim()
            val title = it.selectFirst(".title a").attr("title")
            val link = it.selectFirst(".title a").attr("href")
            val imageUrl = it.selectFirst(".thumb img").attr("src")
            val mode = it.selectFirst(".p_online small").html()
            val type = it.selectFirst(".p_online span").html()
            val downloadCount = it.selectFirst(".p_downcount em").html()
            val lastUpdate = it.selectFirst(".p_publishdate span").html()
            val commentCount = it.selectFirst(".p_comment span a").html()
            val version = it.selectFirst(".p_version span").html()
            val likeCount = it.selectFirst(".p_like .count").html()
            val content = it.selectFirst(".app_body p").html()

            val app = App(
                title = title,
                name = name,
                link = link.removeBaseUrl(),
                imageUrl = imageUrl,
                mode = mode,
                type = type,
                downloadCount = downloadCount,
                lastUpdate = lastUpdate,
                commentCount = commentCount,
                version = version,
                likeCount = likeCount,
                content = content,
                contentLess = content
            )

            appList.add(app)
        }

        return appList.toTypedArray()
    }

    fun appPage(doc: Document): App {


        val name = doc.selectFirst(".title h1").attr("data-header_title").trim()
        val title = doc.selectFirst(".title a").attr("title")
        val link = doc.selectFirst(".title a").attr("href")
        val imageUrl = doc.selectFirst(".thumb img").attr("src")
        val mode = doc.selectFirst(".p_online small").html()
        val type = doc.selectFirst(".p_online span").html()
        val downloadCount = doc.selectFirst(".p_downcount em").html()
        val lastUpdate = doc.selectFirst(".p_publishdate span").html()
        val commentCount = doc.selectFirst(".p_comment span a").html()
        val version = doc.selectFirst(".p_version span").html()
        val likeCount = doc.selectFirst(".p_like .count").html()
        val content = doc.selectFirst("#article_body").html()
        val contentLess =
            doc.selectFirst("#article_body p[style*='text-align: justify', style*='text-align:justify']")?.html()
                ?: doc.selectFirst("#article_body p:not([style*='text-align: center'])")?.html() ?: content

        val categoryLink = doc.selectFirst(".p_category a").attr("href")
        val categoryName = doc.selectFirst(".p_category a").html()

        val downloadLinks = mutableListOf<Pair<String, String>>()
        doc.select(".download .dlbox_dl_links ul li a").forEach {
            downloadLinks.add(it.attr("title").trim() to it.attr("href"))
        }

        val infoList = mutableListOf<String>()
        doc.select(".download .info-links ul li").forEach {
            infoList.add(it.html().removeTag("i").trim())
        }

        val screenshotList = mutableListOf<String>()
        doc.select(".single-article-slider a img").forEach {
            screenshotList.add(it.attr("src"))
        }

        var rate = 0
        doc.select(".post_des .star_rating .stars img").forEach { star ->
            takeIf { star.attr("src").trim().getName() == "rating_off.png" } ?: rate++
        }

        return App(
            title = title,
            name = name,
            link = link.removeBaseUrl(),
            imageUrl = imageUrl,
            mode = mode,
            type = type,
            downloadCount = downloadCount,
            lastUpdate = lastUpdate,
            commentCount = commentCount,
            version = version,
            likeCount = likeCount,
            content = content,
            rate = rate.toString(),
            contentLess = contentLess,
            categoryLink = categoryLink.removeBaseUrl(),
            categoryName = categoryName,
            downloadLinks = downloadLinks.toTypedArray(),
            infoList = infoList.toTypedArray(),
            screenshotList = screenshotList.toTypedArray()
        )
    }

    fun gameCategoryList(doc: Document): Array<Pair<String, String>> {

        val catEls = doc.select(".categories_list.blue ul li a")
        val categories = mutableListOf<Pair<String, String>>()

        catEls.forEach {
            val name = it.html().trim()
            val link = it.attr("href")
            categories.add(name to link.removeBaseUrl())
        }

        return categories.toTypedArray()
    }

    fun programCategoryList(doc: Document): Array<Pair<String, String>> {

        val catEls = doc.select(".categories_list.green ul li a")
        val categories = mutableListOf<Pair<String, String>>()

        catEls.forEach {
            val name = it.html().trim()
            val link = it.attr("href")
            categories.add(name to link.removeBaseUrl())
        }

        return categories.toTypedArray()
    }

    fun mainpageGames(doc: Document): Array<App> {

        val elements = doc.select(".main_content .single_category_post_games article")
        val apps = mutableListOf<App>()

        elements.forEach {

            val link = it.select("header .title").attr("href").trim()
            val title = it.select("header .title").attr("title").trim()
            val name = it.select(".thumb img").attr("alt").trim()
                .takeIf { it.isNotBlank() } ?: title.makeShort()
            val imageUrl = it.select(".thumb img").attr("src").trim()
            val contentLess = it.select("p.with_after").html().trim()
            val lastUpdate = it.select(".details li:nth-child(1)").html().removeTag("i").trim()
            val visitCount = it.select(".details li:nth-child(2)").html().removeTag("i").trim()
            val type = it.select(".type span:nth-child(1)").html().trim()
            val mode = it.select(".type span:nth-child(2)").html().trim()

            val app = App(
                link = link.removeBaseUrl(),
                title = title,
                name = name,
                imageUrl = imageUrl,
                contentLess = contentLess,
                lastUpdate = lastUpdate,
                visitCount = visitCount,
                type = type,
                mode = mode
            )

            apps.add(app)
        }

        return apps.toTypedArray()
    }

    fun mainpagePrograms(doc: Document): Array<App> {

        val elements = doc.select(".main_content .single_category_post.green article")
        val apps = mutableListOf<App>()

        elements.forEach {

            val link = it.select("header .title").attr("href").trim()
            val title = it.select("header .title").attr("title").trim()
            val name = it.select(".thumb img").attr("alt").trim()
                .takeIf { it.isNotBlank() } ?: title.makeShort()
            val imageUrl = it.select(".thumb img").attr("src").trim()
            val contentLess = it.select("p.with_after").html().trim()
            val lastUpdate = it.select(".details li:nth-child(1)").html().removeTag("i").trim()
            val visitCount = it.select(".details li:nth-child(2)").html().removeTag("i").trim()
            val type = it.select(".type span:nth-child(1)").html().trim()
            val mode = it.select(".type span:nth-child(2)").html().trim()

            val app = App(
                link = link.removeBaseUrl(),
                title = title,
                name = name,
                imageUrl = imageUrl,
                contentLess = contentLess,
                lastUpdate = lastUpdate,
                visitCount = visitCount,
                type = type,
                mode = mode
            )

            apps.add(app)
        }

        return apps.toTypedArray()
    }

    fun mostDownloaded(doc: Document): Array<App> {

        val apps = mutableListOf<App>()
        val articles = doc.select(".bilder_content.p30 article")

        articles.forEach {

            val imageUrl = it.select(".top_side .thumb img").attr("src").trim()
            val name = it.select("h1 a").html().trim().fanum()
            val link = it.select("h1 a").attr("href").trim()
            val downloadCount = it.select(".star_rating h5").html().trim().fanum()
            var rate = 0

            it.select(".star_rating .stars img").forEach { star ->
                takeIf { star.attr("src").trim().getName() == "rating_off.png" } ?: rate++
            }

            val app = App(
                imageUrl = imageUrl,
                name = name,
                link = link.removeBaseUrl(),
                downloadCount = downloadCount,
                rate = rate.toString()
            )

            apps.add(app)

        }

        return apps.toTypedArray()
    }

    fun bestApps(doc: Document): Array<App> {

        val apps = mutableListOf<App>()
        val articles = doc.select(".bilder_content.p30 article")

        articles.forEach {

            val imageUrl = it.select(".top_side .thumb img").attr("src").trim()
            val name = it.select("h1 a").html().trim().fanum()
            val link = it.select("h1 a").attr("href").trim()
            var rate = 0

            it.select(".star_rating .stars img").forEach { star ->
                takeIf { star.attr("src").trim().getName() == "rating_off.png" } ?: rate++
            }

            val app = App(
                imageUrl = imageUrl,
                name = name,
                link = link.removeBaseUrl(),
                rate = rate.toString()
            )

            apps.add(app)

        }

        return apps.toTypedArray()
    }

}