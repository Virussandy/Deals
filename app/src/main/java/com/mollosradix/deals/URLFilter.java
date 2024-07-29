package com.mollosradix.deals;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLFilter {
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference configRef;
    private String psc;
    private String linkCode;
    private String language;
    private String ref;
    private String trackingId;

    public URLFilter(Context context) {
        // Initialize Firebase Database
        FirebaseApp.initializeApp(context);
        firebaseDatabase = FirebaseDatabase.getInstance();
        configRef = firebaseDatabase.getReference("amazon_config");

        // Fetch configuration values from Firebase Realtime Database
        configRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    psc = dataSnapshot.child("psc").getValue(String.class);
                    linkCode = dataSnapshot.child("linkCode").getValue(String.class);
                    language = dataSnapshot.child("language").getValue(String.class);
                    ref = dataSnapshot.child("ref").getValue(String.class);
                    trackingId = dataSnapshot.child("trackingId").getValue(String.class); // Fetch trackingId
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Error fetching config from Firebase");
                databaseError.toException().printStackTrace();
            }
        });
    }

    public String getOriginalURL(String redirectUrl) {
        Pattern primaryPattern = Pattern.compile("redirectpid1=([^&]+)&store=([^&]+)");
        Matcher primaryMatcher = primaryPattern.matcher(redirectUrl);
        if (primaryMatcher.find()) {
            String productId = primaryMatcher.group(1);
            String store = primaryMatcher.group(2);
            return buildIndiadesireUrl(store, productId);
        }

        Pattern nestedPattern = Pattern.compile("redirect=([^&]+)");
        Matcher nestedMatcher = nestedPattern.matcher(redirectUrl);
        if (nestedMatcher.find()) {
            String nestedRedirect = nestedMatcher.group(1);
            try {
                String decodedRedirect = URLDecoder.decode(nestedRedirect, StandardCharsets.UTF_8.name());
                decodedRedirect = decodedRedirect.replaceAll("\\s", ""); // Remove all whitespace

                Pattern productIdPattern = Pattern.compile("redirectpid1=([^&]+)");
                Matcher productIdMatcher = productIdPattern.matcher(decodedRedirect);
                if (productIdMatcher.find()) {
                    String productId = productIdMatcher.group(1);
                    String store = getStoreFromUrl(decodedRedirect);
                    return buildIndiadesireUrl(store, productId);
                } else {
                    if (decodedRedirect.contains("amazon.in")) {
                        decodedRedirect = appendAmazonTag(decodedRedirect, trackingId);
                    }
                    System.out.println("Decoded redirect: " + decodedRedirect);
                    return decodedRedirect;
                }
            } catch (UnsupportedEncodingException e) {
                System.err.println("Error decoding URL");
                e.printStackTrace();
                return "Error decoding URL";
            }
        }

        System.out.println("No match found");
        return redirectUrl;
    }

    private String appendAmazonTag(String url, String tag) {
        if (url.contains("?tag=")) {
            // Replace existing tag
            return url.replaceAll("\\?tag=[^&]+", "?tag=" + tag);
        } else if (url.contains("&tag=")) {
            // Replace existing tag
            return url.replaceAll("&tag=[^&]+", "&tag=" + tag);
        } else {
            // Append tag to the URL
            if (url.contains("?")) {
                return url + "&tag=" + tag;
            } else {
                return url + "?tag=" + tag;
            }
        }
    }

    private String getStoreFromUrl(String url) {
        if (url.contains("myntra.com")) {
            return "myntra";
        } else if (url.contains("amazon.in")) {
            return "amazon";
        } else if (url.contains("flipkart.com")) {
            return "flipkart";
        } else if (url.contains("ajio.com")) {
            return "ajio";
        } else if (url.contains("nykaa.com")) {
            return "nykaa";
        } else {
            // Extract domain from the URL as a fallback store name
            Pattern pattern = Pattern.compile("https?://([^/]+)");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                return matcher.group(1);
            } else {
                return "unknown";
            }
        }
    }

    private String buildIndiadesireUrl(String store, String productId) {
        if ("amazon".equals(store)) {
            return buildAmazonUrl(productId);
        }

        switch (store) {
            case "myntra":
                return "https://www.myntra.com/" + productId;
            case "flipkart":
                return "https://www.flipkart.com/a/p/b?pid=" + productId;
            case "ajio":
                return "https://www.ajio.com/p/" + productId;
            case "nykaa":
                return "https://www.nykaa.com/a/p/" + productId;
            default:
                return "https://" + store + ".com/product/" + productId;
        }
    }

    private String buildAmazonUrl(String productId) {
        // Ensure that the configuration values are fetched before using them
        if (psc == null || linkCode == null || language == null || ref == null || trackingId == null) {
            System.err.println("Configuration values are not available yet");
            return "Error: Configuration not available";
        }

        return String.format(
                "https://www.amazon.in/gp/product/%s?psc=%s&linkCode=%s&tag=%s&language=%s&ref=%s",
                productId, psc, linkCode, trackingId, language, ref
        );
    }
}
