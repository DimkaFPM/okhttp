package com.dimkafpm.okhttp

import android.content.res.Resources

interface MainView {
    fun setText(text : CharSequence)

    fun getResources(): Resources
}