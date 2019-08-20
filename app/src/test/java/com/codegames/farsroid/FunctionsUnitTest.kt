package com.codegames.farsroid

import com.codegames.farsroid.util.makeShort
import com.codegames.farsroid.util.removeTag
import org.junit.Test

import org.junit.Assert.*

class FunctionsUnitTest {

    @Test
    fun removeTag() {
        val text = "<i class='icon icon-info'></i>text"
        val textTagRemoved = text.removeTag("i")

        assertEquals(textTagRemoved, textTagRemoved, "text")

    }

    @Test
    fun makeShort() {

        val text = "دانلود Simple Habit Meditation Full 1.34.5 B-276 - برنامه مدیتیشن و کاهش استرس اندروید"
        val shortText = text.makeShort()

        println(shortText)

    }

}
