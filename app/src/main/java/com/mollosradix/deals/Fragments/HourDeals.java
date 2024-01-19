package com.mollosradix.deals.Fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.mollosradix.deals.CheckInternet;
import com.mollosradix.deals.DealsAdapter;
import com.mollosradix.deals.DealsModel;
import com.mollosradix.deals.R;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HourDeals extends Fragment {

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private DealsAdapter adapter;
    private String productImgLink,productDiscription,productTimeInfo,productCPrice,productSPrice,productShopLogo,productMainLink,productOff;
    private Element content,EproductImgLink,EproductDiscription,EproductTimeInfo,EproductCPrice,EproductSPrice,EproductShopLogo,EproductMainLink;
    private Elements imageLink,discription,timeInfo,cpricedata,spricedata,shoplogo,links;
    private List hourDealData ;


    public HourDeals() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hour_deals, container, false);
        progressBar = view.findViewById(R.id.progressbar);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        check(getContext());
        return view;
    }

    @Override
    public void onStart() {

        super.onStart();
    }

    public void check(Context context)
    {
        CheckInternet checkInternet = new CheckInternet(context);
        if(checkInternet.internet_connection())
        {
            new HourDealsNetwork().execute();
        }else {
            final Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content),
                    "No internet connection.",
                    Snackbar.LENGTH_INDEFINITE);
            snackbar.setActionTextColor(ContextCompat.getColor(context,
                    R.color.day_background));
            snackbar.setAction("Reconnect", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(checkInternet.internet_connection())
                    {
                        new HourDealsNetwork().execute();
                    }else {
                        check(context);
                    }
                }
            }).show();
        }
    }
    public void setList() {
        if (!hourDealData.isEmpty()&&hourDealData!=null) {
            //progressDialog.dismiss();
            displayData();
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getContext(), "No Offer Found", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayData(){
        adapter = new DealsAdapter(hourDealData,getActivity());
        recyclerView.setAdapter(adapter);

    }

    public class HourDealsNetwork extends AsyncTask<String, Integer, List<DealsModel>> {

        @Override
        protected List<DealsModel> doInBackground(String... strings) {
            Document doc;
            Connection connection;
            connection = Jsoup.connect("https://indiadesire.com/lootdeals");
            try {
                connection.execute();
                doc = connection.get();
                content = doc.getElementById("lootdeal");
                assert content != null;
                imageLink = content.getElementsByClass("lazyload");
                discription = content.getElementsByClass("anchor");
                timeInfo = content.getElementsByClass("timeinfo");
                cpricedata = content.getElementsByClass("cprice");
                spricedata = content.getElementsByClass("oprice");
                shoplogo = content.getElementsByClass("imgleft");
                links = content.getElementsByClass("myButton");
                hourDealData = new ArrayList<DealsModel>();
                for (int i = 0; i < imageLink.size(); i++) {
                    EproductImgLink = imageLink.get(i);
                    productImgLink = EproductImgLink.attr("data-src");

                    EproductDiscription = discription.get(i);
                    productDiscription = EproductDiscription.text();

                    EproductTimeInfo = timeInfo.get(i);
                    productTimeInfo = EproductTimeInfo.text();

                    EproductCPrice = cpricedata.get(i);
                    productCPrice = EproductCPrice.text();

                    EproductSPrice = spricedata.get(i);
                    productSPrice = EproductSPrice.text();

                    EproductShopLogo = shoplogo.get(i);
                    productShopLogo = EproductShopLogo.attr("src");

                    EproductMainLink = links.get(i);
                    productMainLink = EproductMainLink.attr("href");

                    productOff = 100 * (Integer.parseInt(productSPrice.replaceAll("[^0-9]", "")) - Integer.parseInt(productCPrice.replaceAll("[^0-9]", ""))) / Integer.parseInt(productSPrice.replaceAll("[^0-9]", "")) +"% OFF";


                    DealsModel hourDealsModel = new DealsModel();
                    hourDealsModel.setImageUrl(productImgLink);
                    hourDealsModel.setProductName(productDiscription);
                    hourDealsModel.setUpdateTime(productTimeInfo);
                    hourDealsModel.setNewPrice(productCPrice);
                    hourDealsModel.setOldPrice(productSPrice);
                    hourDealsModel.setStoreLogoUrl(productShopLogo);
                    hourDealsModel.setShopUrl(productMainLink);
                    hourDealsModel.setOff(productOff);
                    hourDealData.add(hourDealsModel);
//                Log.d(TAG, "doInBackground: "+productMainLink);
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