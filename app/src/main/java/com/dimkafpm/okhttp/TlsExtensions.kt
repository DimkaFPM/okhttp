package com.dimkafpm.okhttp

import android.support.annotation.RawRes
import okhttp3.CertificatePinner
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

fun x509TrustManager(inputStream: InputStream) : X509TrustManager {
    val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    return trustManagerFactory.x509TrustManager(inputStream)
}

fun TrustManagerFactory.x509TrustManager(inputStream: InputStream): X509TrustManager {
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

    init(keyStore);
    if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
        throw IllegalStateException("Unexpected default trust managers:" + trustManagers.toString());
    }

    return trustManagers[0] as X509TrustManager;
}

fun X509TrustManager.socketFactory(): SSLSocketFactory {
    val context = SSLContext.getInstance("TLS");
    context.init(null, arrayOf(this), null);
    return context.socketFactory;
}

fun certificateSha256Fingerprint(inputStream: InputStream): String {
    val certificateFactory = CertificateFactory.getInstance("X.509")
    var certificate: Certificate? = null

    inputStream.use { certificate = certificateFactory.generateCertificate(inputStream) }

    return CertificatePinner.pin(certificate!!)
}