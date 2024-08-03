package com.mollosradix.deals.fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mollosradix.deals.BaseFragment;
import com.mollosradix.deals.DealsAdapter;
import com.mollosradix.deals.DealsModel;
import com.mollosradix.deals.GridSpacingItemDecoration;
import com.mollosradix.deals.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HotDeals extends BaseFragment {

    private RelativeLayout progressBar;
    private DealsAdapter adapter;
    private final List<DealsModel> hotDealData = new ArrayList<>();
    private int currentPage = 1;
    private boolean isLoading = false;
    private String message;

    private String psc;
    private String linkCode;
    private String language;
    private String ref;
    private String trackingId;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4); // Configure your thread pool size

    public HotDeals() {}

    public static HotDeals newInstance(String query) {
        HotDeals fragment = new HotDeals();
        Bundle args = new Bundle();
        args.putString("message", query);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            message = getArguments().getString("message");
        }
        View view = inflater.inflate(R.layout.fragment_hot_deals, container, false);
        progressBar = view.findViewById(R.id.progressbar);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        boolean includeEdge = true;
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, includeEdge));
        adapter = new DealsAdapter(hotDealData, getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(1) && !isLoading) {
                    currentPage++;
                    checkAndLoadData();
                }
            }
        });

        // Initialize Firebase
        FirebaseApp.initializeApp(requireActivity());
        fetchFirebaseConfig();

        checkAndLoadData();
        return view;
    }


    private void fetchFirebaseConfig() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference configRef = firebaseDatabase.getReference("amazon_config");

        configRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    psc = dataSnapshot.child("psc").getValue(String.class);
                    linkCode = dataSnapshot.child("linkCode").getValue(String.class);
                    language = dataSnapshot.child("language").getValue(String.class);
                    ref = dataSnapshot.child("ref").getValue(String.class);
                    trackingId = dataSnapshot.child("trackingId").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching config from Firebase", databaseError.toException());
            }
        });
    }

    private void checkAndLoadData() {
        if (isConnectedToInternet()) {
            isLoading = true;
            progressBar.setVisibility(View.VISIBLE);
            executorService.submit(() -> {
                List<DealsModel> newDeals = fetchDeals();
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    isLoading = false;
                    if (newDeals.isEmpty()) {
                        Snackbar.make(requireActivity().findViewById(android.R.id.content),
                                hotDealData.isEmpty() ? "No deals found." : "No more deals found.",
                                Snackbar.LENGTH_LONG).show();
                    } else {
                        if (currentPage == 1) {
                            hotDealData.clear();
                            hotDealData.addAll(newDeals);
                            adapter.notifyItemRangeChanged(0, hotDealData.size());
                        } else {
                            int startPosition = hotDealData.size();
                            hotDealData.addAll(newDeals);
                            adapter.notifyItemRangeInserted(startPosition, newDeals.size());
                        }
                    }
                });
            });
        } else {
            showNoConnection();
        }
    }

    private List<DealsModel> fetchDeals() {
        List<DealsModel> data = new ArrayList<>();
        try {
            String url = message != null
                    ? "https://www.desidime.com/selective_search?keyword=" + URLEncoder.encode(message, StandardCharsets.UTF_8.name()) + "&page=" + currentPage
                    : "https://www.desidime.com/new?page=" + currentPage;

            Document doc = Jsoup.connect(url).get();
            Elements elements = message != null
                    ? doc.select(".gridfix.cf.gb20 > li")
                    : doc.select(".gridfix .l-deal-box");

            for (Element deal : elements) {
                DealsModel dealData = parseDeal(deal);
                data.add(dealData);
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error", e);
        }
        return data;
    }

    private void showNoConnection() {
        Snackbar snackbar = Snackbar.make(requireActivity().findViewById(android.R.id.content),
                "No internet connection.",
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.day_background));
        snackbar.setAction("Retry", v -> checkAndLoadData()).show();
    }

    @Override
    protected void onNetworkReconnect() {
        checkAndLoadData();
    }

    private DealsModel parseDeal(Element deal) {
        String imageUrl = deal.select(".l-deal-box-image img").attr("data-src");
        String title = deal.select(".l-deal-dsp a").text();
        String price = deal.select(".l-deal-price").text();
        String discount = deal.select(".l-deal-discount").text();
        String store = deal.select(".l-deal-store a").text();
        String time = deal.select(".l-promotime").text().trim();
        String extractedURL = extractURL(deal.select(".btn-lgetdeal").attr("data-href"));

        if (extractedURL.isEmpty()) {
            extractedURL = extractURL(deal.select(".btn-lgetdeal").attr("data-href-alt"));
        }

        final String newPrice = price.isEmpty() ? "Price not available" : "₹" + price;
        if (extractedURL.isEmpty()) {
            return new DealsModel(
                    imageUrl.isEmpty() ? "Image not available" : imageUrl,
                    title.isEmpty() ? "Title not available" : title,
                    newPrice,
                    !TextUtils.isEmpty(price) && !TextUtils.isEmpty(discount) ? "₹" + calculateActualPrice(discount, price) : null,
                    time.isEmpty() ? "Time not available" : time,
                    store.isEmpty() ? "Store not available" : store,
                    "Deal URL not available",  // Placeholder
                    discount.isEmpty() ? "Discount not available" : discount
            );
        }

        String dealUrl = extractedURL.contains("amazon.in") ? buildAmazonUrl(extractedURL) : extractedURL;
        Log.d(TAG, "parseDeal: "+dealUrl);

        return new DealsModel(
                imageUrl.isEmpty() ? "Image not available" : imageUrl,
                title.isEmpty() ? "Title not available" : title,
                newPrice,
                !TextUtils.isEmpty(price) && !TextUtils.isEmpty(discount) ? "₹" + calculateActualPrice(discount, price) : null,
                time.isEmpty() ? "Time not available" : time,
                store.isEmpty() ? "Store not available" : store,
                dealUrl.isEmpty() ? "Deal URL not available" : dealUrl,
                discount.isEmpty() ? "Discount not available" : discount
        );
    }

    private String extractURL(String url) {
        try {
            URL urlObj = new URL(url);
            String query = urlObj.getQuery();

            if (query != null) {
                // Split query parameters
                String[] queryParams = query.split("&");
                for (String param : queryParams) {
                    if (param.startsWith("url=")) {
                        // Extract the actual URL from the `url` parameter
                        String actualUrl = URLDecoder.decode(param.substring(4), "UTF-8");
                        return constructFilteredUrl(actualUrl);
                    }
                }
            }

            // If no `url` parameter is found, return the original URL without modifications
            return new URL(urlObj.getProtocol(), urlObj.getHost(), urlObj.getPort(), urlObj.getPath()).toString();
        } catch (Exception e) {
            Log.d(TAG, "extractURL: " + e.getMessage());
        }
        return url;
    }

    private String constructFilteredUrl(String actualUrl) {
        try {
            URL actualUrlObj = new URL(actualUrl);
            String actualPath = actualUrlObj.getPath();
            String actualQuery = actualUrlObj.getQuery();
            StringBuilder filteredUrl = new StringBuilder(new URL(actualUrlObj.getProtocol(), actualUrlObj.getHost(), actualUrlObj.getPort(), actualPath).toString());

            if (actualQuery != null) {
                filteredUrl.append("?").append(actualQuery);
            }

            return filteredUrl.toString();
        } catch (Exception e) {
            Log.d(TAG, "constructFilteredUrl: " + e.getMessage());
            return actualUrl;
        }
    }


    private String calculateActualPrice(String discount, String price) {
        String numericDiscount = discount.replaceAll("[^0-9]", "");
        if (TextUtils.isEmpty(numericDiscount)) {
            return price;
        }
        int aprice = Integer.parseInt(numericDiscount);
        float f = (float) aprice / 100;
        return String.valueOf((int) (Float.parseFloat(price) / (1 - f)));
    }

    private String buildAmazonUrl(String originalUrl) {
        if (TextUtils.isEmpty(originalUrl) || psc == null || linkCode == null || language == null || ref == null || trackingId == null) {
            return originalUrl; // Return the original URL if config is missing
        }
        try {
            return originalUrl + (originalUrl.contains("?") ? "&" : "?")
                    + "psc=" + URLEncoder.encode(psc, StandardCharsets.UTF_8.name())
                    + "&linkCode=" + URLEncoder.encode(linkCode, StandardCharsets.UTF_8.name())
                    + "&language=" + URLEncoder.encode(language, StandardCharsets.UTF_8.name())
                    + "&ref=" + URLEncoder.encode(ref, StandardCharsets.UTF_8.name())
                    + "&tag=" + URLEncoder.encode(trackingId, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            Log.d(TAG, "buildAmazonUrl: "+e.getMessage());
        }
        return originalUrl;
    }
}

