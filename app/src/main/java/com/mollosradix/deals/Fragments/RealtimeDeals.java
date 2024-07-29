package com.mollosradix.deals.Fragments;

import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;
import com.mollosradix.deals.BaseFragment;
import com.mollosradix.deals.DealsAdapter;
import com.mollosradix.deals.DealsModel;
import com.mollosradix.deals.R;
import com.mollosradix.deals.URLFilter;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class RealtimeDeals extends BaseFragment {

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private List<DealsModel> realTimeDealData;
    private int start = 0;
    private final URLFilter urlFilter = new URLFilter(getActivity());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public RealtimeDeals() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void checkAndLoadData() {
        if (isConnectedToInternet()) {
            loadDataInBackground();
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
        loadDataInBackground();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_realtimedeals, container, false);
        recyclerView = view.findViewById(R.id.recyclerView_realtimeDeal);
        progressBar = view.findViewById(R.id.progressbar);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        checkAndLoadData();
        return view;
    }

    private void loadDataInBackground() {
        progressBar.setVisibility(View.VISIBLE);
        executorService.submit(() -> {
            List<DealsModel> deals = fetchData();
            requireActivity().runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (deals != null && !deals.isEmpty()) {
                    realTimeDealData = deals;
                    setList();
                } else {
                    Toast.makeText(getContext(), "No Offer Found", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private List<DealsModel> fetchData() {
        if (!isConnectedToInternet()) {
            return null;
        }

        Document doc;
        Connection connection = Jsoup.connect("https://indiadesire.com/lootdeals");
        try {
            connection.execute();
            doc = connection.get();
            realTimeDealData = new ArrayList<>();
            Element content = doc.getElementById("lootdeal");
            assert content != null;
            Elements imageLink = content.getElementsByClass("lazyload");
            start = imageLink.size();

            Elements elements = doc.getElementsByClass("px-xl-2 py-xl-1 col-6 col-sm-6 col-md-4 col-lg-3 col-xxl-3 col-xl-3 p-1");

            for (Element element : elements) {
                DealsModel deal = new DealsModel();
                deal.setImageUrl(getElementAttr(element, "lazyload", "data-src"));
                deal.setProductName(getElementText(element, "anchor"));
                deal.setUpdateTime(getTimeInfo(element));
                deal.setNewPrice(getCurrentPrice(element));
                deal.setOldPrice(getOriginalPrice(element));
                deal.setStoreLogoUrl(StringUtils.capitalize(getElementAttr(element, "imgleft", "title")));
                deal.setShopUrl(urlFilter.getOriginalURL(getElementAttr(element, "myButton", "href")));
                deal.setOff(calculateDiscount(deal.getOldPrice(), deal.getNewPrice()));
                realTimeDealData.add(deal);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return realTimeDealData;
    }

    private String getElementText(Element element, String className) {
        Element el = element.getElementsByClass(className).first();
        return el != null ? el.text() : "";
    }

    private String getElementAttr(Element element, String className, String attr) {
        Element el = element.getElementsByClass(className).first();
        return el != null ? el.attr(attr) : "";
    }

    private String getTimeInfo(Element element) {
        String timeInfo = getElementText(element, "timeinfo");
        return timeInfo.isEmpty() ? "Updated recently" : timeInfo;
    }

    private String getCurrentPrice(Element element) {
        return getElementText(element, "cprice");
    }

    private String getOriginalPrice(Element element) {
        return getElementText(element, "oprice");
    }

    private String calculateDiscount(String oldPrice, String newPrice) {
        try {
            int oldPriceValue = Integer.parseInt(oldPrice.replaceAll("[^0-9]", ""));
            int newPriceValue = Integer.parseInt(newPrice.replaceAll("[^0-9]", ""));
            return (100 * (oldPriceValue - newPriceValue) / oldPriceValue) + "% OFF";
        } catch (NumberFormatException e) {
            return "N/A"; // Handle parsing error
        }
    }

    private void setList() {
        if (realTimeDealData != null && !realTimeDealData.isEmpty()) {
            // Remove hour deals
            if (start > 0) {
                realTimeDealData.subList(0, start).clear();
            }
            displayData();
        } else {
            Toast.makeText(getContext(), "No Offer Found", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayData() {
        DealsAdapter adapter = new DealsAdapter(realTimeDealData, getActivity());
        recyclerView.setAdapter(adapter);
    }
}
