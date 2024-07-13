package com.mollosradix.deals.Fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import com.google.android.material.snackbar.Snackbar;
import com.mollosradix.deals.BaseFragment;
import com.mollosradix.deals.DealsAdapter;
import com.mollosradix.deals.DealsModel;
import com.mollosradix.deals.MainActivity;
import com.mollosradix.deals.R;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HotDeals extends BaseFragment {

    private ProgressBar progressBar;
//    private GridLayout skeletonLayout;
    private RecyclerView recyclerView;
    private DealsAdapter adapter;
    private List<DealsModel> hotDealData = new ArrayList<>();
    private int currentPage = 1;
    private static final String TRACKING_ID = "deals026f-21";
    private static final String ARG_MESSAGE = "message";
    private String message = null;

    public HotDeals() {}

    public static HotDeals newInstance(String query) {
        HotDeals fragment = new HotDeals();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, query);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            message = getArguments().getString(ARG_MESSAGE);
        }
        View view = inflater.inflate(R.layout.fragment_hot_deals, container, false);
        progressBar = view.findViewById(R.id.progressbar);
//        skeletonLayout = view.findViewById(R.id.skeleton_layout);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new DealsAdapter(hotDealData, getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(1)) {
                    currentPage++;
                    checkAndLoadData();
                }
            }
        });
//        showSkeletonLoading();
        checkAndLoadData();
        return view;
    }

//    private void showSkeletonLoading() {
//        skeletonLayout.setVisibility(View.VISIBLE);
//        recyclerView.setVisibility(View.GONE);
//    }
//
//    private void hideSkeletonLoading() {
//        skeletonLayout.setVisibility(View.GONE);
//        recyclerView.setVisibility(View.VISIBLE);
//    }
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
        snackbar.setAction("Retry", v -> checkAndLoadData()).show();
    }

    @Override
    protected void onNetworkReconnect() {
        new HotDealsNetwork().execute();
    }

    private class HotDealsNetwork extends AsyncTask<Void, Void, List<DealsModel>> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<DealsModel> doInBackground(Void... voids) {
            List<DealsModel> data = new ArrayList<>();
            try {
                String url = message != null
                        ? "https://www.desidime.com/selective_search?keyword=" + URLEncoder.encode(message, "UTF-8") + "&page=" + currentPage
                        : "https://www.desidime.com/new?page=" + currentPage;

                Document doc = Jsoup.connect(url).get();
                Elements elements = message != null
                        ? doc.select(".gridfix.cf.gb20 > li")
                        : doc.select(".gridfix .l-deal-box");

                for (Element deal : elements) {
                    DealsModel dealData = parseDeal(deal);
                    if (dealData != null) {
                        data.add(dealData);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(List<DealsModel> newDeals) {
            progressBar.setVisibility(View.GONE);
            if (newDeals.isEmpty()) {
                Snackbar.make(requireActivity().findViewById(android.R.id.content),
                        hotDealData.isEmpty() ? "No deals found." : "No more deals found.",
                        Snackbar.LENGTH_LONG).show();
            } else {
                if (currentPage == 1) {
                    hotDealData.clear();
                }
                int startPosition = hotDealData.size();
                hotDealData.addAll(newDeals);
                adapter.notifyItemRangeInserted(startPosition, newDeals.size());
            }
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

            // Return the original URL if both are empty
            if (extractedURL.isEmpty()) {
                return new DealsModel(
                        imageUrl.isEmpty() ? "Image not available" : imageUrl,
                        title.isEmpty() ? "Title not available" : title,
                        price.isEmpty() ? "Price not available" : price,
                        !TextUtils.isEmpty(price) && !TextUtils.isEmpty(discount) ? calculateActualPrice(discount, price) : null,
                        time.isEmpty() ? "Time not available" : time,
                        store.isEmpty() ? "Store not available" : store,
                        "Deal URL not available",  // Placeholder
                        discount.isEmpty() ? "Discount not available" : discount
                );
            }

            String dealUrl = extractedURL.contains("amazon.in") ? appendTrackingId(extractedURL, TRACKING_ID) : extractedURL;

            return new DealsModel(
                    imageUrl.isEmpty() ? "Image not available" : imageUrl,
                    title.isEmpty() ? "Title not available" : title,
                    price.isEmpty() ? "Price not available" : price,
                    !TextUtils.isEmpty(price) && !TextUtils.isEmpty(discount) ? calculateActualPrice(discount, price) : null,
                    time.isEmpty() ? "Time not available" : time,
                    store.isEmpty() ? "Store not available" : store,
                    dealUrl,
                    discount.isEmpty() ? "Discount not available" : discount
            );
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

        private String extractURL(String input) {
            try {
                String regex = "url=(.*)";
                Matcher matcher = Pattern.compile(regex).matcher(input);
                if (matcher.find()) {
                    String encodedURL = matcher.group(1);
                    return URLDecoder.decode(encodedURL, StandardCharsets.UTF_8.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return input;  // Return original input if no match found
        }

        private String appendTrackingId(String url, String trackingId) {
            return url.contains("?") ? url + "&tag=" + trackingId : url + "?tag=" + trackingId;
        }
    }
}
