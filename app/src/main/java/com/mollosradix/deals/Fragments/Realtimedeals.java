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

public class Realtimedeals extends BaseFragment {

    private ProgressBar progressBar;
    private View view;
    private RecyclerView recyclerView;
    private DealsAdapter adapter;
    private List<DealsModel> realTimeDealData;
    private int start = 0;
    private URLFilter urlFilter = new URLFilter();

    public Realtimedeals() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void checkAndLoadData() {
        if (isConnectedToInternet()) {
            new RealTimeDealsNetwork().execute();
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
        new RealTimeDealsNetwork().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_realtimedeals, container, false);
        recyclerView = view.findViewById(R.id.recyclerView_realtimedeal);
        progressBar = view.findViewById(R.id.progressbar);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        checkAndLoadData();
        return view;
    }

    public class RealTimeDealsNetwork extends AsyncTask<String, Integer, List<DealsModel>> {

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
                e.printStackTrace();
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

        @Override
        protected void onPostExecute(List<DealsModel> dealsModels) {
            super.onPostExecute(dealsModels);
            progressBar.setVisibility(View.GONE);
            setList();
        }

        private void setList() {
            if (realTimeDealData != null && !realTimeDealData.isEmpty()) {
                for (int i = 0; i < start; i++) {
                    realTimeDealData.remove(0); // Remove hour deals
                }
                displayData();
            } else {
                Toast.makeText(getContext(), "No Offer Found", Toast.LENGTH_SHORT).show();
            }
        }

        private void displayData() {
            adapter = new DealsAdapter(realTimeDealData, getActivity());
            recyclerView.setAdapter(adapter);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }
    }
}
