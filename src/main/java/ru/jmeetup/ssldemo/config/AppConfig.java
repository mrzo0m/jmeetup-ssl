package ru.jmeetup.ssldemo.config;

import com.ibm.websphere.ssl.SSLException;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import ru.jmeetup.ssldemo.WasSSLContextBuilder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

@Configuration
public class AppConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);
    private static final String TLSV_1_2 = "TLSv1.2";

    @Bean
    public RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        try {
            SSLContext ctx = new WasSSLContextBuilder.Builder().build().getSSLContext();
            HttpComponentsClientHttpRequestFactory requestFactory;
            HostnameVerifier verifier;
            boolean isHostnameVerify = true; // STUB
            if (isHostnameVerify) {
                verifier = SSLConnectionSocketFactory.getDefaultHostnameVerifier();
            } else {
                verifier = NoopHostnameVerifier.INSTANCE;
            }
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                    ctx,
                    new String[]{TLSV_1_2},
                    null,
                    verifier);
            HttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(socketFactory)
                    .build();
            requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
            int httpConnectTimeout = 300000;
            requestFactory.setReadTimeout(httpConnectTimeout);
            requestFactory.setConnectionRequestTimeout(httpConnectTimeout);
            restTemplate.setRequestFactory(requestFactory);
            LOGGER.info("restTemplate DONE");

        } catch (SSLException | IOException | CertificateException
                | UnrecoverableKeyException | NoSuchAlgorithmException
                | KeyStoreException | KeyManagementException e) {
            LOGGER.error(e.getMessage());
        }
        return restTemplate;
    }
}
