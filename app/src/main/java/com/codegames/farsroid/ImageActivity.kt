package com.codegames.farsroid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.codegames.farsroid.util.ViewPagerAdapter
import kotlinx.android.synthetic.main.activity_image.*


class ImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val images = intent?.extras?.getStringArray("images") ?: throw Throwable("put images")
        val position = intent?.extras?.getInt("position") ?: throw Throwable("put position")

        ai_back.setOnClickListener {
            onBackPressed()
        }

        ai_viewPager.adapter = ViewPagerAdapter(supportFragmentManager).apply {
            images.forEach {
                addFragment(ImageFragment.newInstance(it), "")
            }
        }

        ai_viewPager.setCurrentItem(position, false)

    }

}
