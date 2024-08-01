package com.mollosradix.deals.fragments;

import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
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

public class HourDeals extends BaseFragment {

    private RelativeLayout progressBar;
    private RecyclerView recyclerView;
    private DealsAdapter adapter;
    private List<DealsModel> hourDealData;
    final URLFilter urlFilter = new URLFilter(getActivity());

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public HourDeals() {
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
        View view = inflater.inflate(R.layout.fragment_hour_deals, container, false);
        progressBar = view.findViewById(R.id.progressbar);
        recyclerView = view.findViewById(R.id.recyclerView);
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
                    hourDealData = deals;
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
            Element content = doc.getElementById("lootdeal");
            assert content != null;

            Elements imageLinks = content.getElementsByClass("lazyload");
            Elements descriptions = content.getElementsByClass("anchor");
            Elements timeInfo = content.getElementsByClass("timeinfo");
            Elements currentPrices = content.getElementsByClass("cprice");
            Elements originalPrices = content.getElementsByClass("oprice");
            Elements shopLogos = content.getElementsByClass("divcenter1");
            Elements links = content.getElementsByClass("myButton");

            List<DealsModel> dealsList = new ArrayList<>();

            for (int i = 0; i < imageLinks.size(); i++) {
                String productImgLink = imageLinks.get(i).attr("data-src");
                String productDescription = descriptions.get(i).text();
                String productTimeInfo = timeInfo.get(i).text();
                String productCPrice = currentPrices.get(i).text();
                String productSPrice = originalPrices.get(i).text();
                String productShopLogo = StringUtils.capitalize(shopLogos.get(i).attr("store"));
                String productMainLink = links.get(i).attr("href");

                String productOff = 100 * (Integer.parseInt(productSPrice.replaceAll("[^0-9]", "")) -
                        Integer.parseInt(productCPrice.replaceAll("[^0-9]", ""))) /
                        Integer.parseInt(productSPrice.replaceAll("[^0-9]", "")) + "% OFF";

                DealsModel hourDealsModel = new DealsModel();
                hourDealsModel.setImageUrl(productImgLink);
                hourDealsModel.setProductName(productDescription);
                hourDealsModel.setUpdateTime(productTimeInfo);
                hourDealsModel.setNewPrice(productCPrice);
                hourDealsModel.setOldPrice(productSPrice);
                hourDealsModel.setStoreLogoUrl(productShopLogo);
                hourDealsModel.setShopUrl(urlFilter.getOriginalURL(productMainLink));
                hourDealsModel.setOff(productOff);
                dealsList.add(hourDealsModel);
            }
            return dealsList;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public void setList() {
        if (hourDealData != null && !hourDealData.isEmpty()) {
            displayData();
            adapter.notifyItemRangeInserted(0, hourDealData.size());
        } else {
            Toast.makeText(getContext(), "No Offer Found", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayData() {
        adapter = new DealsAdapter(hourDealData, getActivity());
        recyclerView.setAdapter(adapter);
    }
}
