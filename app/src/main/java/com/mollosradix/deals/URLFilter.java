package com.mollosradix.deals;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLFilter {
    private static final String TRACKING_ID = "deals026f-21";

    public String getOriginalURL(String redirectUrl) {
        Pattern pattern = Pattern.compile("redirectpid1=([^&]+)&store=([^&]+)");
        Matcher matcher = pattern.matcher(redirectUrl);
        if (matcher.find()) {
            String productId = matcher.group(1);
            String store = matcher.group(2);
            return buildIndiadesireUrl(store, productId);
        }
        return "Invalid URL";
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
