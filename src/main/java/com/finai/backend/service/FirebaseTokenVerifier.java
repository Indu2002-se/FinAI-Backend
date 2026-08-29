package com.finai.backend.service;

import com.finai.backend.exception.AuthenticationException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Verifies Firebase tokens using configured credentials, environment variables,
 * well-known key file paths, or Application Default Credentials.
 */
@Slf4j
@Service
public class FirebaseTokenVerifier {

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    @Value("${firebase.project-id:finai-a6f0f}")
    private String projectId;

    private final ResourceLoader resourceLoader;
    private String lastInitError = null;

    public FirebaseTokenVerifier(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public synchronized void init() {
        tryInitialize();
    }

    private synchronized boolean tryInitialize() {
        if (!FirebaseApp.getApps().isEmpty()) {
            return true;
        }

        try {
            GoogleCredentials credentials = resolveCredentials();
            FirebaseOptions.Builder builder = FirebaseOptions.builder();

            if (credentials != null) {
                builder.setCredentials(credentials);
            }
            if (projectId != null && !projectId.isBlank()) {
                builder.setProjectId(projectId);
            }

            FirebaseApp.initializeApp(builder.build());
            log.info("Firebase Admin SDK successfully initialized (Project: {})", projectId);
            lastInitError = null;
            return true;
        } catch (Exception e) {
            lastInitError = e.getMessage();
            log.warn("Firebase Admin SDK could not be initialized at startup: {}", e.getMessage());
            return false;
        }
    }

    private GoogleCredentials resolveCredentials() throws Exception {
        // 1. Explicit property or FIREBASE_CREDENTIALS_PATH
        String configuredPath = (credentialsPath != null && !credentialsPath.isBlank())
                ? credentialsPath
                : System.getenv("FIREBASE_CREDENTIALS_PATH");

        if (configuredPath != null && !configuredPath.isBlank()) {
            InputStream stream = loadInputStreamFromLocation(configuredPath.trim());
            if (stream != null) {
                log.info("Loaded Firebase credentials from configured path: {}", configuredPath);
                return GoogleCredentials.fromStream(stream);
            }
        }

        // 2. Direct JSON content via FIREBASE_CREDENTIALS_JSON or FIREBASE_SERVICE_ACCOUNT_JSON
        String rawJson = System.getenv("FIREBASE_CREDENTIALS_JSON");
        if (rawJson == null || rawJson.isBlank()) {
            rawJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");
        }
        if (rawJson != null && !rawJson.isBlank()) {
            rawJson = rawJson.trim();
            byte[] bytes;
            if (rawJson.startsWith("{")) {
                bytes = rawJson.getBytes(StandardCharsets.UTF_8);
            } else {
                try {
                    bytes = Base64.getDecoder().decode(rawJson);
                } catch (Exception ignored) {
                    bytes = rawJson.getBytes(StandardCharsets.UTF_8);
                }
            }
            log.info("Loaded Firebase credentials from environment JSON string");
            return GoogleCredentials.fromStream(new ByteArrayInputStream(bytes));
        }

        // 3. GOOGLE_APPLICATION_CREDENTIALS environment variable
        String googleAppCreds = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (googleAppCreds != null && !googleAppCreds.isBlank()) {
            File file = new File(googleAppCreds.trim());
            if (file.exists() && file.isFile()) {
                log.info("Loaded Firebase credentials from GOOGLE_APPLICATION_CREDENTIALS: {}", googleAppCreds);
                return GoogleCredentials.fromStream(new FileInputStream(file));
            }
        }

        // 4. Well-known candidate files on disk
        String[] candidatePaths = new String[]{
                "firebase-service-account.json",
                "serviceAccountKey.json",
                "firebase-adminsdk.json",
                "firebase-key.json",
                "/app/firebase-service-account.json",
                "/app/serviceAccountKey.json",
                "/etc/finai/firebase-service-account.json",
                "/etc/finai/serviceAccountKey.json",
                "/home/ubuntu/firebase-service-account.json",
                "/home/ubuntu/serviceAccountKey.json",
                "/root/firebase-service-account.json",
                "/root/serviceAccountKey.json"
        };

        for (String path : candidatePaths) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                log.info("Loaded Firebase credentials from well-known location: {}", file.getAbsolutePath());
                return GoogleCredentials.fromStream(new FileInputStream(file));
            }
        }

        // 5. Classpath resources
        String[] classpathCandidates = new String[]{
                "classpath:firebase-service-account.json",
                "classpath:serviceAccountKey.json",
                "classpath:firebase-key.json"
        };

        for (String cpPath : classpathCandidates) {
            InputStream stream = loadInputStreamFromLocation(cpPath);
            if (stream != null) {
                log.info("Loaded Firebase credentials from classpath resource: {}", cpPath);
                return GoogleCredentials.fromStream(stream);
            }
        }

        // 6. Application Default Credentials (e.g. GCP metadata)
        try {
            log.info("Attempting Google Application Default Credentials fallback...");
            return GoogleCredentials.getApplicationDefault();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "No Firebase service account key found. Please set GOOGLE_APPLICATION_CREDENTIALS, " +
                    "FIREBASE_CREDENTIALS_PATH, or place 'firebase-service-account.json' in the application directory.", e);
        }
    }

    private InputStream loadInputStreamFromLocation(String location) {
        try {
            if (location.startsWith("classpath:") || location.startsWith("file:")) {
                Resource resource = resourceLoader.getResource(location);
                if (resource.exists()) {
                    return resource.getInputStream();
                }
            } else {
                File file = new File(location);
                if (file.exists() && file.isFile()) {
                    return new FileInputStream(file);
                }
                // Try classpath as fallback
                Resource resource = resourceLoader.getResource("classpath:" + location);
                if (resource.exists()) {
                    return resource.getInputStream();
                }
            }
        } catch (Exception e) {
            log.debug("Could not load resource from location {}: {}", location, e.getMessage());
        }
        return null;
    }

    public FirebaseToken verify(String idToken) {
        if (FirebaseApp.getApps().isEmpty()) {
            boolean initialized = tryInitialize();
            if (!initialized) {
                log.error("Google sign-in attempt failed because Firebase Admin is not initialized. Error: {}", lastInitError);
                throw new AuthenticationException(
                        "Google sign-in is not configured on the server. " +
                        (lastInitError != null ? "Reason: " + lastInitError : "Service account key missing."));
            }
        }

        try {
            return FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (FirebaseAuthException exception) {
            log.warn("Firebase ID token verification failed: {} (code: {})", exception.getMessage(), exception.getErrorCode());
            throw new AuthenticationException("Invalid or expired Google sign-in token: " + exception.getMessage());
        } catch (Exception exception) {
            log.error("Unexpected error during Firebase token verification: {}", exception.getMessage(), exception);
            throw new AuthenticationException("Failed to verify Google sign-in token: " + exception.getMessage());
        }
    }
}
