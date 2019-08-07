package ru.jmeetup.ssldemo;

import com.ibm.websphere.ssl.SSLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.Properties;

public final class WasSSLContextBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(WasSSLContextBuilder.class);
    private static final String KEY_STORE = "com.ibm.ssl.keyStore";
    private static final String KEY_STORE_TYPE = "com.ibm.ssl.keyStoreType";
    private static final String KEY_STORE_PSWD = "com.ibm.ssl.keyStorePassword";
    private static final String TRUST_STORE_TYPE = "com.ibm.ssl.trustStoreType";
    private static final String TRUST_STORE_PSWD = "com.ibm.ssl.trustStorePassword";
    private static final String TRUST_STORE = "com.ibm.ssl.trustStore";
    private static final String SSL_LIBERTY_CONFIGURATION_NAME = "defaultSSLConfig";
    private static final String SSL_DEFAULT_CONFIGURATION_NAME = "CellDefaultSSLSettings";

    private static final String EQ_PATTERN = "{}={}";
    private static final String PASS_STARS = "*****";
    private String sslConfigName = null;

    private WasSSLContextBuilder(Builder builder) {
        this.sslConfigName = builder.sslConfigName;
    }


    public SSLContext getSSLContext() throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, KeyManagementException, IOException, CertificateException, com.ibm.websphere.ssl.SSLException {
        Security.setProperty("ssl.SocketFactory.provider", "com.ibm.jsse2.SSLSocketFactoryImpl");
        Security.setProperty("ssl.ServerSocketFactory.provider", "com.ibm.jsse2.SSLServerSocketFactoryImpl");

        Properties sslProps = getProperties();
        KeyStore trustkeyStore = KeyStore.getInstance(sslProps.getProperty(TRUST_STORE_TYPE));
        char[] tPassword = sslProps.getProperty(TRUST_STORE_PSWD).toCharArray();
        trustkeyStore.load(new FileInputStream(sslProps.getProperty(TRUST_STORE)), tPassword);
        TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(trustkeyStore);
        LOGGER.info("trustFactory.init");

        KeyStore keyStore = KeyStore.getInstance(sslProps.getProperty(KEY_STORE_TYPE));
        char[] kPassword = sslProps.getProperty(KEY_STORE_PSWD).toCharArray();
        keyStore.load(new FileInputStream(sslProps.getProperty(KEY_STORE)), kPassword);
        LOGGER.info("keyStore.load DONE");
        KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyFactory.init(keyStore, kPassword);
        LOGGER.info("keyFactory.init");

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(keyFactory.getKeyManagers(), trustFactory.getTrustManagers(), new SecureRandom());
        LOGGER.info("sc.init DONE");
        return sc;
    }

    private Properties getProperties() throws SSLException {
        LOGGER.info("Try get jsse helper");
        com.ibm.websphere.ssl.JSSEHelper jsseHelper = com.ibm.websphere.ssl.JSSEHelper.getInstance();
        Properties sslProps = null;
        if (sslConfigName != null) {
            LOGGER.info("Using jsse helper to get: {}", sslConfigName);
            sslProps = jsseHelper.getProperties(sslConfigName); //Name for was custom ssl config
        }
        if (sslProps == null) {
            LOGGER.info("Using jsse helper to get: {}", SSL_DEFAULT_CONFIGURATION_NAME);
            sslProps = jsseHelper.getProperties(SSL_DEFAULT_CONFIGURATION_NAME); //Default name for cell was ssl config
        }
        if (sslProps == null) {
            LOGGER.info("Using jsse helper to get: {}", SSL_LIBERTY_CONFIGURATION_NAME);
            sslProps = jsseHelper.getProperties(SSL_LIBERTY_CONFIGURATION_NAME); //Default for liberty profile ssl config
        }
        logSSLProps(sslProps);
        return sslProps;
    }

    private void logSSLProps(Properties sslProps) {
        Enumeration<Object> pe = sslProps.keys();
        while (pe.hasMoreElements()) { //TODO: utils
            String key = (String) pe.nextElement();
            if (key.toLowerCase().contains("Password".toLowerCase())) {
                LOGGER.debug(EQ_PATTERN, key, PASS_STARS);
            } else {
                String val = (String) sslProps.get(key);
                LOGGER.debug(EQ_PATTERN, key, val);
            }
        }
    }

    public static class Builder {
        private String sslConfigName = null;

        public Builder(String sslConfigName) {
            this.sslConfigName = sslConfigName;
        }

        public Builder() {
        }

        public Builder withWasSSLConfigName(String sslConfigName) {
            this.sslConfigName = sslConfigName;
            return this;
        }

        public WasSSLContextBuilder build() {
            return new WasSSLContextBuilder(this);
        }
    }
}