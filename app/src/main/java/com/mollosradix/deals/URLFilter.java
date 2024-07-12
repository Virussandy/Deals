package com.mollosradix.deals;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLFilter {
    private static final String TRACKING_ID = "deals026f-21";

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
                        decodedRedirect = appendAmazonTag(decodedRedirect, TRACKING_ID);
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
        switch (store) {
            case "myntra":
                return "https://www.myntra.com/" + productId;
            case "amazon":
                return "https://www.amazon.in/dp/" + productId + "?tag=" + TRACKING_ID;
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
}
