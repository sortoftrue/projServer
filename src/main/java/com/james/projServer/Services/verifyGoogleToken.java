package com.james.projServer.Services;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.james.projServer.Models.GoogleUser;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

@Service
public class VerifyGoogleToken {

    public Optional<GoogleUser> verify(String googleCredential) throws GeneralSecurityException, IOException {

        HttpTransport transport = new NetHttpTransport();
        GsonFactory jsonFactory = new GsonFactory();

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections
                        .singletonList("469976009260-ut02fdoq4c4orvt2km4ksl4ufhcgdnll.apps.googleusercontent.com"))
                .build();

        GoogleIdToken idToken = verifier.verify(googleCredential);
        if (idToken != null) {
            Payload payload = idToken.getPayload();

            // Print user identifier
            String userId = payload.getSubject();
            System.out.println("User ID: " + userId);

            // Get profile information from payload
            String email = payload.getEmail();
            boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String locale = (String) payload.get("locale");
            String familyName = (String) payload.get("family_name");
            String givenName = (String) payload.get("given_name");

            GoogleUser user = new GoogleUser(name, email);
            return Optional.of(user);
            // Use or store profile information
            // ...

        } else {
            System.out.println("Invalid ID token.");
        }

        return Optional.empty();
    }

}