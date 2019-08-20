package com.codegames.farsroid.util

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.codegames.farsroid.BASEURL
import lib.codegames.debug.LogCG
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Response
import java.util.*
import java.util.regex.Pattern


fun String.removeTag(tagName: String): String {

    val startTag = this.indexOf("<$tagName", 0)
    val endTag = this.indexOf("</$tagName>", 0)

    if (startTag < 0 || endTag < 0 || endTag < startTag)
        return this

    return this.removeRange(startTag, endTag + tagName.length + 3).removeTag(tagName)
}

class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
    val mFragmentList = ArrayList<Fragment>()
    private val mFragmentTitleList = ArrayList<String>()

    override fun getItem(position: Int): Fragment {
        return mFragmentList.get(position)
    }

    override fun getCount(): Int {
        return mFragmentList.size
    }

    fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mFragmentTitleList[position]
    }
}

class SwipelessViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {
    override fun onTouchEvent(event: MotionEvent): Boolean = false
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean = false
}

fun calculateNoOfColumns(context: Context, columnWidthDp: Float): Int {
    val displayMetrics = context.resources.displayMetrics
    val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
    return (screenWidthDp / columnWidthDp + 0.5).toInt()
}

fun String.justNumber(): String {

    var result = ""

    this.toCharArray().forEach {
        when (it) {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> result += it
            '۰' -> result += '0'
            '۱' -> result += '1'
            '۲' -> result += '2'
            '۳' -> result += '3'
            '۴' -> result += '4'
            '۵' -> result += '5'
            '۶' -> result += '6'
            '۷' -> result += '7'
            '۸' -> result += '8'
            '۹' -> result += '9'
        }
    }

    return result
}

fun String.toGoodDate(): String {

    val pattern = Pattern.compile(".+([0-9۰-۹]+) (.+) ([0-9۰-۹]+)")
    val matcher = pattern.matcher(this)

    return if (matcher.find()) {

        val year = matcher.group(3)
        val month = matcher.group(2).let {
            when (it) {
                "فروردین" -> "01"
                "اردیبهشت" -> "02"
                "خرداد" -> "03"
                "تیر" -> "04"
                "مرداد" -> "05"
                "شهریور" -> "06"
                "مهر" -> "07"
                "آبان" -> "08"
                "آذر" -> "09"
                "دی" -> "10"
                "بهمن" -> "11"
                "اسفند" -> "12"
                else -> "--"
            }
        }
        val day = matcher.group(1).let { day ->
            "0$day".let { it.substring(it.length - 2, it.length) }
        }

        "$year/$month/$day"
    } else
        this
}

fun String.makeShort(): String {

    var result = this

    val dashIndex = result.indexOfLast { it == '-' }

    if (dashIndex >= 0)
        result = result.substring(0, dashIndex)

    result = result.replace("دانلود", "")

    return result.trim()
}

fun String.getName(): String {
    val index = this.trim('/').lastIndexOf('/')
    return when {
        index < 0 -> this
        index + 1 >= length -> this
        else -> this.substring(index + 1, this.length)
    }
}

fun Response<ResponseBody>.document(): Document {
    return Jsoup.parse(body()?.string())
}

fun String.removeBaseUrl(): String = replace(BASEURL, "")
    .replace(BASEURL.replaceFirst("https", "http"), "")
    .trim('/')

fun String.toPrettyNumber(): String {

    val factor = when {
        indexOf('K') >= 0 || indexOf('k') >= 0 -> 1_000
        indexOf('M') >= 0 || indexOf('m') >= 0 -> 1_000_000
        else -> 1
    }

    var num = (justNumber().toIntOrNull() ?: 0) * factor

    LogCG.d("$this -> $num")

    val factorSign = when {
        num > 1_000 -> {
            num /= 1_000
            "K"
        }
        num > 1_000_000 -> {
            num /= 1_000_000
            "M"
        }
        else -> ""
    }

    return "$num$factorSign"
}

fun View.assetFont(path: String) {

    val typeface = Typeface.createFromAsset(this.context.assets, path)

    val stack = Stack<View>()
    stack.add(this)

    while (stack.isNotEmpty()) {
        (this as? TextView)?.typeface = typeface
        (this as? ViewGroup)?.children?.forEach {
            stack.add(it)
        }
    }

}

fun now() = Date().time