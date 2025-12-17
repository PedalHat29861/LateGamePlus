/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  io.netty.handler.ssl.SslContext
 *  io.netty.handler.ssl.SslContextBuilder
 *  org.slf4j.Logger
 */
package net.minecraft.server.dedicated.management;

import com.mojang.logging.LogUtils;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import org.slf4j.Logger;

public class ManagementServerEncryption {
    private static final String PASSWORD_ENVIRONMENT_VARIABLE_NAME = "MINECRAFT_MANAGEMENT_TLS_KEYSTORE_PASSWORD";
    private static final String PASSWORD_PROPERTY_NAME = "management.tls.keystore.password";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static SslContext createContext(String keystore, String password) throws Exception {
        if (keystore.isEmpty()) {
            throw new IllegalArgumentException("TLS is enabled but keystore is not configured");
        }
        File file = new File(keystore);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Supplied keystore is not a file or does not exist: '" + keystore + "'");
        }
        String string = ManagementServerEncryption.getKeystorePassword(password);
        return ManagementServerEncryption.createContext(file, string);
    }

    private static String getKeystorePassword(String fallback) {
        String string = System.getenv().get(PASSWORD_ENVIRONMENT_VARIABLE_NAME);
        if (string != null) {
            return string;
        }
        String string2 = System.getProperty(PASSWORD_PROPERTY_NAME, null);
        if (string2 != null) {
            return string2;
        }
        return fallback;
    }

    private static SslContext createContext(File keystore, String password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream inputStream = new FileInputStream(keystore);){
            keyStore.load(inputStream, password.toCharArray());
        }
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password.toCharArray());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        return SslContextBuilder.forServer((KeyManagerFactory)keyManagerFactory).trustManager(trustManagerFactory).build();
    }

    public static void logInstructions() {
        LOGGER.info("To use TLS for the management server, please follow these steps:");
        LOGGER.info("1. Set the server property 'management-server-tls-enabled' to 'true' to enable TLS");
        LOGGER.info("2. Create a keystore file of type PKCS12 containing your server certificate and private key");
        LOGGER.info("3. Set the server property 'management-server-tls-keystore' to the path of your keystore file");
        LOGGER.info("4. Set the keystore password via the environment variable 'MINECRAFT_MANAGEMENT_TLS_KEYSTORE_PASSWORD', or system property 'management.tls.keystore.password', or server property 'management-server-tls-keystore-password'");
        LOGGER.info("5. Restart the server to apply the changes.");
    }
}

