package com.finai.backend.service;

import com.finai.backend.exception.AuthenticationException;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;

/** Verifies Firebase tokens using Application Default Credentials. */
@Service
public class FirebaseTokenVerifier {

    public FirebaseToken verify(String idToken) {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp();
            }
            return FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (FirebaseAuthException exception) {
            throw new AuthenticationException("Invalid or expired Google sign-in token");
        } catch (Exception exception) {
            throw new AuthenticationException("Google sign-in is not configured on the server");
        }
    }
}
