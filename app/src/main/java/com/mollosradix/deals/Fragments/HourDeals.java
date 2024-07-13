package com.mollosradix.deals.Fragments;

import android.os.AsyncTask;
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

public class HourDeals extends BaseFragment {

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private DealsAdapter adapter;
    private List<DealsModel> hourDealData;
    URLFilter urlFilter = new URLFilter();

    public HourDeals() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void checkAndLoadData() {
        if (isConnectedToInternet()) {
            new HourDealsNetwork().execute();
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
        new HourDealsNetwork().execute();
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

    public void setList() {
        if (hourDealData != null && !hourDealData.isEmpty()) {
            displayData();
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getContext(), "No Offer Found", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayData() {
        adapter = new DealsAdapter(hourDealData, getActivity());
        recyclerView.setAdapter(adapter);
    }

    public class HourDealsNetwork extends AsyncTask<String, Integer, List<DealsModel>> {

        @Override
        protected List<DealsModel> doInBackground(String... strings) {
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
                Elements timeInfos = content.getElementsByClass("timeinfo");
                Elements currentPrices = content.getElementsByClass("cprice");
                Elements originalPrices = content.getElementsByClass("oprice");
                Elements shopLogos = content.getElementsByClass("divcenter1");
                Elements links = content.getElementsByClass("myButton");

                hourDealData = new ArrayList<>();

                for (int i = 0; i < imageLinks.size(); i++) {
                    String productImgLink = imageLinks.get(i).attr("data-src");
                    String productDiscription = descriptions.get(i).text();
                    String productTimeInfo = timeInfos.get(i).text();
                    String productCPrice = currentPrices.get(i).text();
                    String productSPrice = originalPrices.get(i).text();
                    String productShopLogo = StringUtils.capitalize(shopLogos.get(i).attr("store"));
                    String productMainLink = links.get(i).attr("href");

                    String productOff = 100 * (Integer.parseInt(productSPrice.replaceAll("[^0-9]", "")) -
                            Integer.parseInt(productCPrice.replaceAll("[^0-9]", ""))) /
                            Integer.parseInt(productSPrice.replaceAll("[^0-9]", "")) + "% OFF";

                    DealsModel hourDealsModel = new DealsModel();
                    hourDealsModel.setImageUrl(productImgLink);
                    hourDealsModel.setProductName(productDiscription);
                    hourDealsModel.setUpdateTime(productTimeInfo);
                    hourDealsModel.setNewPrice(productCPrice);
                    hourDealsModel.setOldPrice(productSPrice);
                    hourDealsModel.setStoreLogoUrl(productShopLogo);
                    hourDealsModel.setShopUrl(urlFilter.getOriginalURL(productMainLink));
                    hourDealsModel.setOff(productOff);
                    hourDealData.add(hourDealsModel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return hourDealData;
        }

        @Override
        protected void onPostExecute(List<DealsModel> hourDealsModels) {
            super.onPostExecute(hourDealsModels);
            progressBar.setVisibility(View.GONE);
            setList();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }
    }
}
