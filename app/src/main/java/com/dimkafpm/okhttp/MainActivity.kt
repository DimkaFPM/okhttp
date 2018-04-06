package com.dimkafpm.okhttp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView

class MainActivity : AppCompatActivity(), MainView {

    lateinit var textView: TextView
    val mainPresenter = MainPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.text)

        mainPresenter.onCreate()
    }

    override fun setText(text: CharSequence) {
        runOnUiThread { textView.text = text }
    }
}
