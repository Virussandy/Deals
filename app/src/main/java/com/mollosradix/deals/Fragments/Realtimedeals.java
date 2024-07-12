package com.mollosradix.deals.Fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class Realtimedeals extends BaseActivity {

    private ProgressBar progressBar;
    private View view;
    private RecyclerView recyclerView;
    private DealsAdapter adapter;
    private String productImgLink,productDiscription,productTimeInfo,productCPrice,productSPrice,productShopLogo,productMainLink,productOff;
    private List<DealsModel> realTimeDealData ;
    private int start = 0;
    private boolean isLoadingMore = false;
    URLFilter urlFilter = new URLFilter();

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
        snackbar.setAction("Retry", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndLoadData();
            }
        }).show();
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
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        checkAndLoadData();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public class RealTimeDealsNetwork extends AsyncTask<String, Integer, List<DealsModel>> {

        private static final String TAG = "";

        @Override
        protected List<DealsModel> doInBackground(String... strings) {
            if (!isConnectedToInternet()) {
                return null;
            }
            Document doc;
            Connection connection;
            connection = Jsoup.connect("https://indiadesire.com/lootdeals");
            try {
                connection.execute();
                doc = connection.get();
                realTimeDealData = new ArrayList<DealsModel>();
                Element content = doc.getElementById("lootdeal");
                assert content != null;
                Elements imageLink = content.getElementsByClass("lazyload");
                start = imageLink.size();

                Elements elements = doc.getElementsByClass("px-xl-2 py-xl-1 col-6 col-sm-6 col-md-4 col-lg-3 col-xxl-3 col-xl-3 p-1");
                for(Element element:elements) {
                    Elements imgUrl = element.getElementsByClass("lazyload");
                    for (Element url:imgUrl) {
                        productImgLink = url.attr("data-src");
//                        Log.d(TAG, "ImageUrl: "+url.attr("data-src"));
                    }
                    Elements Discription = element.getElementsByClass("anchor");
                    for (Element discription:Discription) {
                        productDiscription = discription.text();
                        //Log.d(TAG, "Discription: "+discription.text());
                    }
                    Elements timeInfo = element.getElementsByClass("timeinfo");
                    for (Element time:timeInfo) {
                        productTimeInfo = time.text();
                        if (productTimeInfo.isEmpty()) {
                            productTimeInfo = "Updated recently";
                        }
                        //Log.d(TAG, "Time: "+time.text());
                    }
                    Elements cPriceData = element.getElementsByClass("cprice");
                    for (Element cprice:cPriceData) {
                        productCPrice = cprice.text();
                        //Log.d(TAG, "C Price: "+cprice.text());
                    }
                    Elements sPriceData = element.getElementsByClass("oprice");
                    for (Element sprice:sPriceData) {
                        productSPrice = sprice.text();
                        //Log.d(TAG, "S Price: "+sprice.text());
                    }
                    Elements shopLogo = element.getElementsByClass("imgleft");
                    for (Element logo:shopLogo) {
                        //productShopLogo = logo.attr("title").substring(0,1).toUpperCase()+logo.attr("title").substring(1);
                        productShopLogo = StringUtils.capitalize(logo.attr("title"));
                        //Log.d(TAG, "Shop Logo: "+logo.attr("src"));
                    }
                    Elements productUrl = element.getElementsByClass("myButton");
                    for (Element link:productUrl) {
                        productMainLink = link.attr("href");
//                        Log.d(TAG, "Product Link: "+link.attr("href"));
                    }
                    productOff = 100 * (Integer.parseInt(productSPrice.replaceAll("[^0-9]", "")) - Integer.parseInt(productCPrice.replaceAll("[^0-9]", ""))) / Integer.parseInt(productSPrice.replaceAll("[^0-9]", "")) +"% OFF";
                    DealsModel hourDealsModel = new DealsModel();
                    hourDealsModel.setImageUrl(productImgLink);
                    hourDealsModel.setProductName(productDiscription);
                    hourDealsModel.setUpdateTime(productTimeInfo);
                    hourDealsModel.setNewPrice(productCPrice);
                    hourDealsModel.setOldPrice(productSPrice);
                    hourDealsModel.setStoreLogoUrl(productShopLogo);
                    hourDealsModel.setShopUrl(urlFilter.getOriginalURL(productMainLink));
                    hourDealsModel.setOff(productOff);
                    realTimeDealData.add(hourDealsModel);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return realTimeDealData;
        }

        public void setList() {
            if (!realTimeDealData.isEmpty()&&realTimeDealData!=null) {
                for (int i = 0; i < start; i++) {
                    realTimeDealData.remove(0);
                }
                displayData();
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "No Offer Found", Toast.LENGTH_SHORT).show();
            }
        }

        private void displayData(){
            adapter = new DealsAdapter(realTimeDealData,getActivity());
            recyclerView.setAdapter(adapter);
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