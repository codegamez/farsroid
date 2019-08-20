package com.codegames.farsroid

import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_download_help.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class DownloadHelpDialog : AppCompatActivity(), GLSurfaceView.Renderer {

    private var glSurfaceView: GLSurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_help)

        glSurfaceView = GLSurfaceView(this)
        glSurfaceView?.setRenderer(this)
        rootView.addView(glSurfaceView)

        @Suppress("DEPRECATION")
        val processor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            var a = ""
            Build.SUPPORTED_ABIS.forEach { if (it.isNotBlank()) a += "${it.trim()}\n" }
            a.trim('\n')
        } else {
            "${Build.CPU_ABI}\n${Build.CPU_ABI2}"
        }

        adh_processor.text = processor

    }

    override fun onDrawFrame(gl: GL10?) {
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val renderer = gl?.glGetString(GL10.GL_RENDERER)
        runOnUiThread {
            adh_graphic.text = renderer?.trim()
            rootView.removeView(glSurfaceView)
        }
    }

}
