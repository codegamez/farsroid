package com.codegames.farsroid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_image.view.*
import java.lang.Exception


private const val ARG_IMAGE = "image"

class ImageFragment : Fragment() {

    private var image: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            image = it.getString(ARG_IMAGE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image, container, false)

        view.ai_progressBar.show()

        Picasso.get().load(image).placeholder(R.drawable.slider_placeholder).into(view.ai_image, object : Callback {

            override fun onSuccess() {
                view.ai_progressBar.hide()
            }

            override fun onError(e: Exception?) {}
        })

        return view
    }

    companion object {

        fun newInstance(image: String) =
            ImageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_IMAGE, image)
                }
            }

    }
}
