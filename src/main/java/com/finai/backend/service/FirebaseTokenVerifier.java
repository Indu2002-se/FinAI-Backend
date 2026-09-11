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
 * Verifies Firebase ID tokens using an explicit service-account key.
 * Does not use Application Default Credentials — on non-GCP hosts (e.g. EC2)
 * ADC probes the metadata server and can block for 1–2 minutes per attempt.
 */
@Slf4j
@Service
public class FirebaseTokenVerifier {

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    @Value("${firebase.project-id:finai-a6f0f}")
    private String projectId;

    private final ResourceLoader resourceLoader;

    /** True after the single startup (or first) init attempt — never re-walk credential paths. */
    private volatile boolean initAttempted = false;
    private volatile boolean initialized = false;
    private String lastInitError = null;

    public FirebaseTokenVerifier(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public synchronized void init() {
        tryInitialize();
    }

    private synchronized boolean tryInitialize() {
        if (initialized || !FirebaseApp.getApps().isEmpty()) {
            initialized = true;
            return true;
        }

        // Fail fast on later requests — do not re-run slow credential resolution.
        if (initAttempted) {
            return false;
        }
        initAttempted = true;

        long started = System.currentTimeMillis();
        try {
            GoogleCredentials credentials = resolveCredentials();
            FirebaseOptions.Builder builder = FirebaseOptions.builder()
                    .setCredentials(credentials);
            if (projectId != null && !projectId.isBlank()) {
                builder.setProjectId(projectId);
            }

            FirebaseApp.initializeApp(builder.build());
            initialized = true;
            lastInitError = null;
            log.info("Firebase Admin SDK successfully initialized in {}ms (Project: {})",
                    System.currentTimeMillis() - started, projectId);
            return true;
        } catch (Exception e) {
            lastInitError = e.getMessage();
            log.error("Firebase Admin SDK could not be initialized ({}ms): {}",
                    System.currentTimeMillis() - started, e.getMessage());
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
            log.warn("Configured Firebase credentials path could not be loaded: {}", configuredPath);
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

        // 3. GOOGLE_APPLICATION_CREDENTIALS — must exist and be readable (no silent skip → ADC)
        String googleAppCreds = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (googleAppCreds != null && !googleAppCreds.isBlank()) {
            File file = new File(googleAppCreds.trim());
            if (!file.exists() || !file.isFile()) {
                throw new IllegalStateException(
                        "GOOGLE_APPLICATION_CREDENTIALS is set to '" + googleAppCreds
                                + "' but the file is missing. Mount firebase-service-account.json "
                                + "or set FIREBASE_CREDENTIALS_PATH.");
            }
            if (!file.canRead()) {
                throw new IllegalStateException(
                        "GOOGLE_APPLICATION_CREDENTIALS file is not readable: " + googleAppCreds
                                + " (fix permissions: chmod 644 on the host file).");
            }
            log.info("Loaded Firebase credentials from GOOGLE_APPLICATION_CREDENTIALS: {}", googleAppCreds);
            return GoogleCredentials.fromStream(new FileInputStream(file));
        }

        // 4. Well-known candidate files on disk
        String[] candidatePaths = new String[]{
                "firebase-service-account.json",
                "finai-a6f0f-firebase-adminsdk-fbsvc-0df2605496.json",
                "serviceAccountKey.json",
                "firebase-adminsdk.json",
                "firebase-key.json",
                "/app/secrets/firebase-service-account.json",
                "/app/firebase-service-account.json",
                "/app/serviceAccountKey.json",
                "/etc/finai/firebase-service-account.json",
                "/etc/finai/serviceAccountKey.json"
        };

        for (String path : candidatePaths) {
            File file = new File(path);
            if (file.exists() && file.isFile() && file.canRead()) {
                log.info("Loaded Firebase credentials from well-known location: {}", file.getAbsolutePath());
                return GoogleCredentials.fromStream(new FileInputStream(file));
            }
        }

        // 5. Classpath resources
        String[] classpathCandidates = new String[]{
                "classpath:firebase-service-account.json",
                "classpath:finai-a6f0f-firebase-adminsdk-fbsvc-0df2605496.json",
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

        // Do NOT call GoogleCredentials.getApplicationDefault() — on EC2 that hangs
        // for ~60–120s probing the GCP metadata IP (169.254.169.254).
        throw new IllegalStateException(
                "No Firebase service account key found. Set FIREBASE_CREDENTIALS_PATH or "
                        + "GOOGLE_APPLICATION_CREDENTIALS to a readable firebase-service-account.json, "
                        + "or place the key on the classpath.");
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
                    if (!file.canRead()) {
                        log.warn("Firebase credentials file exists but is not readable: {}", location);
                        return null;
                    }
                    return new FileInputStream(file);
                }
                Resource resource = resourceLoader.getResource("classpath:" + location);
                if (resource.exists()) {
                    return resource.getInputStream();
                }
            }
        } catch (Exception e) {
            log.warn("Could not load Firebase credentials from {}: {}", location, e.getMessage());
        }
        return null;
    }

    public FirebaseToken verify(String idToken) {
        if (!initialized && FirebaseApp.getApps().isEmpty()) {
            boolean ok = tryInitialize();
            if (!ok) {
                log.error("Google sign-in failed: Firebase Admin not initialized. Error: {}", lastInitError);
                throw new AuthenticationException(
                        "Google sign-in is not configured on the server. "
                                + (lastInitError != null ? "Reason: " + lastInitError : "Service account key missing."));
            }
        }

        try {
            long started = System.currentTimeMillis();
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(idToken);
            log.debug("Firebase ID token verified in {}ms", System.currentTimeMillis() - started);
            return token;
        } catch (FirebaseAuthException exception) {
            log.warn("Firebase ID token verification failed: {} (code: {})",
                    exception.getMessage(), exception.getErrorCode());
            throw new AuthenticationException(
                    "Invalid or expired Google sign-in token: " + exception.getMessage());
        } catch (Exception exception) {
            log.error("Unexpected error during Firebase token verification: {}",
                    exception.getMessage(), exception);
            throw new AuthenticationException(
                    "Failed to verify Google sign-in token: " + exception.getMessage());
        }
    }
}
