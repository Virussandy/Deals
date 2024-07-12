package com.mollosradix.deals.Fragments;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.mollosradix.deals.BaseActivity;
import com.mollosradix.deals.DealsAdapter;
import com.mollosradix.deals.DealsModel;
import com.mollosradix.deals.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HotDeals extends BaseActivity {

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private DealsAdapter adapter;
    private List<DealsModel> hotDealData = new ArrayList<>();
    private String actualPrice, dealUrl;
    private int currentPage = 1; // Track current page
    private static final String TRACKING_ID = "deals026f-21";
    private static final String ARG_MESSAGE = "message";
    private String message = null;

    public HotDeals() {
        // Required empty public constructor
    }

    public static HotDeals newInstance(String query) {
        HotDeals fragment = new HotDeals();
        Bundle args = new Bundle();
        args.putString("query", query);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments() != null) {
            message = getArguments().getString(ARG_MESSAGE);
        }
        View view = inflater.inflate(R.layout.fragment_hot_deals, container, false);
        progressBar = view.findViewById(R.id.progressbar);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new DealsAdapter(hotDealData, getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(1)) {
                    // Load next page when reached to the end
                    currentPage++;
                    checkAndLoadData();
                }
            }
        });
        checkAndLoadData();
        return view;
    }

    private void checkAndLoadData() {
        if (isConnectedToInternet()) {
            new HotDealsNetwork().execute();
        } else {
            showNoConnectionSnackbar();
        }
    }

    private void showNoConnectionSnackbar() {
        Snackbar snackbar = Snackbar.make(requireActivity().findViewById(android.R.id.content),
                "No internet connection.",
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.day_background));
        snackbar.setAction("Retry", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndLoadData();
            }
        }).show();
    }

    @Override
    protected void onNetworkReconnect() {
        new HotDealsNetwork().execute();
    }

    public class HotDealsNetwork extends AsyncTask<Void, Integer, List<DealsModel>> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected List<DealsModel> doInBackground(Void... voids) {
            if (!isConnectedToInternet()) {
                return null;
            }
            List<DealsModel> data = new ArrayList<>();
            Document doc;
            try {
                if (message != null) {
                    doc = Jsoup.connect("https://www.desidime.com/selective_search?keyword=" + URLEncoder.encode(message, "UTF-8") + "&page=" + currentPage).get();
                    Elements elements = doc.select(".gridfix.cf.gb20 > li");
                    Log.d("HotDealsNetwork", "Number of elements found: " + elements.size());
                    Log.d("HotDealsNetwork", "Document fetched: " + doc.outerHtml());
                    for (Element deal : elements) {
                        String imageUrl = deal.select(".l-deal-box-image img").attr("data-src"); // Image URL
                        String title = deal.select(".l-deal-dsp a").text(); // Title
                        String price = deal.select(".l-deal-price").text(); // Price
                        String discount = deal.select(".l-deal-discount").text(); // Discount
                        String store = deal.select(".l-deal-store a").text(); // Store
                        String time = deal.select(".l-promotime").text().trim(); // Time (trim to remove leading/trailing whitespace)

                        // Extracting the URL associated with 'Get Deal' button

                        String extractedURL = extractURL(deal.select(".btn-lgetdeal").attr("data-href"));

                        // Fallback if 'data-href' is not present
                        if (extractedURL.isEmpty()) {
                            extractedURL = extractURL(deal.select(".btn-lgetdeal").attr("data-href-alt"));
                        }

                        if (!extractedURL.isEmpty()) {
                            if (extractedURL.contains("amazon.in")) {
                                dealUrl = appendTrackingId(extractedURL, TRACKING_ID);
                            } else {
                                dealUrl = extractedURL;
                            }
                        }

                        if (TextUtils.isEmpty(title)) {
                            title = "Title not available";
                        }
                        if (!TextUtils.isEmpty(price) && !TextUtils.isEmpty(discount)) {
                            actualPrice = calculateActualPrice(discount, price);
                        }
                        if (TextUtils.isEmpty(price)) {
                            actualPrice = null;
                            price = "Price not available";
                        }
                        if (TextUtils.isEmpty(discount)) {
                            actualPrice = null;
                            discount = "Discount not available";
                        }
                        if (TextUtils.isEmpty(store)) {
                            store = "Store not available";
                        }
                        if (TextUtils.isEmpty(time)) {
                            time = "Time not available";
                        }
                        if (TextUtils.isEmpty(imageUrl)) {
                            imageUrl = "Image not available";
                        }
                        if (TextUtils.isEmpty(dealUrl)) {
                            dealUrl = "Deal URL not available";
                        }

                        DealsModel dealdata = new DealsModel(imageUrl, title, price, actualPrice, time, store, dealUrl, discount);
                        data.add(dealdata);
                    }
                } else {
                    doc = Jsoup.connect("https://www.desidime.com/new?page=" + currentPage).get();
                    Elements elements = doc.select(".gridfix .l-deal-box");
                    for (Element deal : elements) {
                        String imageUrl = deal.select(".l-deal-box-image img").attr("data-src");
                        String title = deal.select(".l-deal-dsp a").text();
                        String price = deal.select(".l-deal-price").text();
                        String discount = deal.select(".l-deal-discount").text();
                        String store = deal.select(".l-deal-store").text();
                        String time = deal.select(".l-promotime").text();
                        String extractedURL = extractURL(deal.select(".btn-lgetdeal").attr("data-href"));
                        if (!extractedURL.isEmpty()) {
                            if (extractedURL.contains("amazon.in")) {
                                dealUrl = appendTrackingId(extractedURL, TRACKING_ID);
                            } else {
                                dealUrl = extractedURL;
                            }
                        }

                        if (TextUtils.isEmpty(title)) {
                            title = "Title not available";
                        }
                        if (!TextUtils.isEmpty(price) && !TextUtils.isEmpty(discount)) {
                            actualPrice = calculateActualPrice(discount, price);
                        }
                        if (TextUtils.isEmpty(price)) {
                            actualPrice = null;
                            price = "Price not available";
                        }
                        if (TextUtils.isEmpty(discount)) {
                            actualPrice = null;
                            discount = "Discount not available";
                        }
                        if (TextUtils.isEmpty(store)) {
                            store = "Store not available";
                        }
                        if (TextUtils.isEmpty(time)) {
                            time = "Time not available";
                        }
                        if (TextUtils.isEmpty(imageUrl)) {
                            imageUrl = "Image not available";
                        }
                        if (TextUtils.isEmpty(dealUrl)) {
                            dealUrl = "Deal URL not available";
                        }

                        DealsModel dealdata = new DealsModel(imageUrl, title, price, actualPrice, time, store, dealUrl, discount);
                        data.add(dealdata);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(List<DealsModel> newDeals) {
            super.onPostExecute(newDeals);
            progressBar.setVisibility(View.GONE);
            if (newDeals == null || newDeals.isEmpty()) {
                if (hotDealData.isEmpty()) {
                    Snackbar.make(requireActivity().findViewById(android.R.id.content), "No deals found.", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(requireActivity().findViewById(android.R.id.content), "No more deals found.", Snackbar.LENGTH_LONG).show();
                }
            } else {
                if (currentPage == 1) {
                    hotDealData.clear(); // Clear existing data when loading the first page
                    adapter.notifyDataSetChanged(); // Notify adapter about the dataset change
                }
                int startPosition = hotDealData.size();
                hotDealData.addAll(newDeals);
                adapter.notifyItemRangeInserted(startPosition, newDeals.size());
            }
        }

        private String calculateActualPrice(String discount, String price) {
            int length = discount.length(), aprice, cal = 0;
            float f;
            if (length == 6) {
                aprice = Integer.parseInt(discount.substring(0, 1));
                f = (float) aprice / 100;
                cal = (int) (Float.parseFloat(price) / (1 - f));
            } else if (length == 7) {
                aprice = Integer.parseInt(discount.substring(0, 2));
                f = (float) aprice / 100;
                cal = (int) (Float.parseFloat(price) / (1 - f));
            } else if (length == 8) {
                aprice = Integer.parseInt(discount.substring(0, 3));
                f = (float) aprice / 100;
                cal = (int) (Float.parseFloat(price) / (1 - f));
            }
            return String.valueOf(cal);
        }

        private String extractURL(String input) {
            try {
                // Regular expression pattern to match the URL part after 'url='
                String regex = "url=(.*)";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(input);

                // Extract and return the URL
                if (matcher.find()) {
                    String encodedURL = matcher.group(1);
                    return URLDecoder.decode(encodedURL, StandardCharsets.UTF_8.toString());
                } else {
                    return input; // Return null if no match is found
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private String appendTrackingId(String url, String trackingId) {
            if (url.contains("?")) {
                return url + "&tag=" + trackingId;
            } else {
                return url + "?tag=" + trackingId;
            }
        }
    }
}
