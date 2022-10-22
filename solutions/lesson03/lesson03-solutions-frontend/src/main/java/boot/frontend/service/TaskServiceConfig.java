package boot.frontend.service;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan
public class TaskServiceConfig {

    // tag::restTemplate[]
    @Bean
    RestTemplate restTemplate() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        return restTemplate;
    }
    // end::restTemplate[]

    private CloseableHttpClient clientAcceptingAnyValidCertificate() {
        // @formatter:off
        // tag::sslany
        CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                    .build();
        // end::sslany
        // @formatter:on
        return httpClient;
    }

    private CloseableHttpClient sslClientNoHostVerification() {
        // @formatter:off
        // tag::sslnohost[]
        CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                    .build();
        // end::sslnohost[]
        // @formatter:on
        return httpClient;
    }

    private CloseableHttpClient sslClientTrustAll()
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        // @formatter:off
        // tag::ssltrustall
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(new TrustAllStrategy()).build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLContext(sslContext)
                .build();
        // end::ssltrustall
        // @formatter:on
        return httpClient;
    }

    private CloseableHttpClient sslClientTrustSelfSigned() throws KeyManagementException, NoSuchAlgorithmException,
            KeyStoreException, CertificateException, IOException {
        // @formatter:off
        // tag::sslselfsigned[]
        KeyStore keyStore = KeyStore.getInstance(new File("mykeystore.jks"), "secret".toCharArray());
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(keyStore, new TrustSelfSignedStrategy()).build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLContext(sslContext)
                .build();
        // end::sslselfsigned[]
        // @formatter:on
        return httpClient;
    }

    private CloseableHttpClient sslClientCustomSocketFactory() throws KeyManagementException, NoSuchAlgorithmException,
            KeyStoreException, CertificateException, IOException {
        // @formatter:off
        // tag::sslsf
        KeyStore keyStore = KeyStore.getInstance(new File("mykeystore.jks"), "secret".toCharArray());
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(keyStore, new TrustSelfSignedStrategy()).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslContext,
                new String[] { "TLSv1" },
                null,
                new NoopHostnameVerifier());
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();
        // end::sslsf
        // @formatter:on
        return httpClient;
    }
}
