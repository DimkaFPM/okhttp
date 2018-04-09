package com.dimkafpm.okhttp

import android.util.Log
import okhttp3.*
import java.io.IOException
import java.util.*


class MainPresenter(var view: MainView) {

    fun onCreate() {

        val client = httpClient()

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

    private fun httpClient(): OkHttpClient {
        val caInput = view.getResources().openRawResource(R.raw.chls);
        val trustedManager = x509TrustManager(caInput)

        val tlsSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .build()

        val fingerprintInput = view.getResources().openRawResource(R.raw.chls);
        val certificatePinner = CertificatePinner.Builder()
                .add("publicobject.com", certificateSha256Fingerprint(fingerprintInput))
                .build()

        return OkHttpClient.Builder()
                .sslSocketFactory(trustedManager.socketFactory(), trustedManager)
                .connectionSpecs(Collections.singletonList(tlsSpec))
                .certificatePinner(certificatePinner)
                .build()
    }
}