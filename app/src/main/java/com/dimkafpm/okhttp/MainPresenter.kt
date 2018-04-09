package com.dimkafpm.okhttp

import android.support.annotation.RawRes
import android.util.Log
import okhttp3.*
import java.io.IOException
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


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
        val tlsSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .build()

        val certificatePinner = CertificatePinner.Builder()
                .add("publicobject.com", certificateSha256Fingerprint(R.raw.chls))
                .build()

        val caInput = view.getResources().openRawResource(R.raw.chls);
        val trustedManager = trustManagerForCertificates(caInput)

        return OkHttpClient.Builder()
                .sslSocketFactory(socketFactory(trustedManager), trustedManager)
                .connectionSpecs(Collections.singletonList(tlsSpec))
                .certificatePinner(certificatePinner)
                .build()
    }

    private fun certificateSha256Fingerprint(@RawRes certId: Int): String {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        var certificate: Certificate? = null

        val certStream = view.getResources().openRawResource(certId)
        certStream.use { certificate = certificateFactory.generateCertificate(certStream) }

        return CertificatePinner.pin(certificate!!)
    }

    private fun trustManagerForCertificates(inputStream: InputStream): X509TrustManager {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        var certificates: Collection<Certificate> = emptyList()
        inputStream.use {
            certificates = certificateFactory.generateCertificates(inputStream)
            if (certificates.isEmpty()) {
                throw IllegalArgumentException("expected non-empty set of trusted certificates")
            }
        }

        // Create a KeyStore containing our trusted CAs
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        certificates.forEachIndexed { index, certificate ->
            keyStore.setCertificateEntry(index.toString(), certificate)
        }

        // Create a TrustManager that trusts the CAs in our KeyStore
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        val trustManagers = trustManagerFactory.trustManagers
        if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
            throw IllegalStateException("Unexpected default trust managers:" + trustManagers.toString());
        }

        return trustManagers[0] as X509TrustManager;
    }

    private fun socketFactory(trustedManager: X509TrustManager): SSLSocketFactory {
        val context = SSLContext.getInstance("TLS");
        context.init(null, arrayOf(trustedManager), null);
        return context.socketFactory;
    }
}