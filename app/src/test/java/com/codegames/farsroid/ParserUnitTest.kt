package com.codegames.farsroid

import com.codegames.farsroid.util.Parser
import org.jsoup.Jsoup
import org.junit.Test

class ParserUnitTest {

    @Test
    fun appList() {
        val html = Jsoup.connect("$BASEURL/cat/game/").get()

        val appList = Parser.appList(html)

        assert(appList.isNotEmpty())

        appList.forEach {
            assert(it.name.isNotBlank()) { it }
            assert(it.title.isNotBlank()) { it }
            assert(it.link.isNotBlank()) { it }
            assert(it.imageUrl.isNotBlank()) { it }
            assert(it.mode.isNotBlank()) { it }
            assert(it.type.isNotBlank()) { it }
            assert(it.downloadCount.isNotBlank()) { it }
            assert(it.lastUpdate.isNotBlank()) { it }
            assert(it.commentCount.isNotBlank()) { it }
            assert(it.version.isNotBlank()) { it }
            assert(it.likeCount.isNotBlank()) { it }
            assert(it.content.isNotBlank()) { it }
        }

    }

    @Test
    fun appPage() {
        val html = Jsoup.connect("$BASEURL/graveyard-keeper/").get()

        val app = Parser.appPage(html)

        println(app)

        assert(app.name.isNotBlank()) { app }
        assert(app.title.isNotBlank()) { app }
        assert(app.link.isNotBlank()) { app }
        assert(app.imageUrl.isNotBlank()) { app }
        assert(app.mode.isNotBlank()) { app }
        assert(app.type.isNotBlank()) { app }
        assert(app.downloadCount.isNotBlank()) { app }
        assert(app.lastUpdate.isNotBlank()) { app }
        assert(app.commentCount.isNotBlank()) { app }
        assert(app.version.isNotBlank()) { app }
        assert(app.likeCount.isNotBlank()) { app }
        assert(app.content.isNotBlank()) { app }

        assert(app.downloadLinks.isNotEmpty()) { app }
        app.downloadLinks.forEach {
            assert(it.first.isNotBlank()) { app }
            assert(it.second.isNotBlank()) { app }
        }

        assert(app.infoList.isNotEmpty()) { app }
        app.infoList.forEach {
            assert(it.isNotBlank()) { app }
        }

        assert(app.screenshotList.isNotEmpty()) { app }
        app.screenshotList.forEach {
            assert(it.isNotBlank()) { app }
        }

    }

    @Test
    fun gameCategoryTest() {

        val html = Jsoup.connect(BASEURL).get()

        val categories = Parser.gameCategoryList(html)

        assert(categories.isNotEmpty()) { categories }

        categories.forEach {
            assert(it.first.isNotBlank() && it.second.isNotBlank()) { categories }
        }

    }

    @Test
    fun programCategoryTest() {

        val html = Jsoup.connect(BASEURL).get()

        val categories = Parser.programCategoryList(html)

        assert(categories.isNotEmpty()) { categories }

        categories.forEach {
            assert(it.first.isNotBlank() && it.second.isNotBlank()) { categories }
        }

    }

    @Test
    fun mainpageGames() {

        val html = Jsoup.connect(BASEURL).get()

        val apps = Parser.mainpageGames(html)

        assert(apps.isNotEmpty()) { apps }

        apps.forEach {
            assert(it.name.isNotBlank()) { it }
            assert(it.title.isNotBlank()) { it }
            assert(it.type.isNotBlank()) { it }
            assert(it.mode.isNotBlank()) { it }
            assert(it.visitCount.isNotBlank()) { it }
            assert(it.contentLess.isNotBlank()) { it }
            assert(it.imageUrl.isNotBlank()) { it }
            assert(it.link.isNotBlank()) { it }
        }

    }

    @Test
    fun mainpagePrograms() {

        val html = Jsoup.connect(BASEURL).get()

        val apps = Parser.mainpagePrograms(html)

        assert(apps.isNotEmpty()) { apps }

        apps.forEach {
            assert(it.name.isNotBlank()) { it }
            assert(it.title.isNotBlank()) { it }
            assert(it.mode.isNotBlank()) { it }
            assert(it.visitCount.isNotBlank()) { it }
            assert(it.contentLess.isNotBlank()) { it }
            assert(it.imageUrl.isNotBlank()) { it }
            assert(it.link.isNotBlank()) { it }
        }

    }

    @Test
    fun mostDownloaded() {

        val html = Jsoup.connect("$BASEURL/most-downloaded-games-of-month").get()

        val apps = Parser.mostDownloaded(html)

        assert(apps.isNotEmpty()) { apps }

        apps.forEach {
            assert(it.name.isNotBlank()) { it }
            assert(it.imageUrl.isNotBlank()) { it }
            assert(it.link.isNotBlank()) { it }
            assert(it.downloadCount.isNotBlank()) { it }
            assert(it.rate.isNotBlank()) { it }
        }

    }

    @Test
    fun bestApps() {

        val html = Jsoup.connect("$BASEURL/top-games").get()

        val apps = Parser.bestApps(html)

        assert(apps.isNotEmpty()) { apps }

        apps.forEach {
            assert(it.name.isNotBlank()) { it }
            assert(it.imageUrl.isNotBlank()) { it }
            assert(it.link.isNotBlank()) { it }
            assert(it.rate.isNotBlank()) { it }
        }

    }

}
