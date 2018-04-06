package com.dimkafpm.okhttp

import android.util.Log
import okhttp3.*
import java.io.IOException
import java.util.*


class MainPresenter(var view: MainView) {

    fun onCreate() {
        val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .build()

        val pinner = CertificatePinner.Builder()
                .add("publicobject.com", "sha256/afwiKY3RxoMmLkuRW1l7QsPZTJPwDS2pdDROQjXw8ig=")
                .build()

        val client = OkHttpClient.Builder()
                .connectionSpecs(Collections.singletonList(spec))
                .certificatePinner(pinner)
                .build()

        val request = Request.Builder()
                .url("https://publicobject.com/helloworld.txt")
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.v("error", e?.message ?: "message", e)
            }

            override fun onResponse(call: Call?, response: Response?) {
                val text = response!!.body()!!.string()
                Log.v("success", text)
                view.setText(text)
            }
        })
    }
}