package com.mollosradix.deals;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DealsAdapter extends RecyclerView.Adapter<DealsAdapter.HourDealsHolder> {

    List<DealsModel> list;
    Context context;
    URLFilter urlFilter = new URLFilter();

    public DealsAdapter(List<DealsModel> data, FragmentActivity application) {
        this.list = data;
        this.context = application;
    }

    @NonNull
    @Override
    public HourDealsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View viewitem = inflater.inflate(R.layout.row,parent,false);
        HourDealsHolder holder = new HourDealsHolder(viewitem);
        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull HourDealsHolder holder, int position) {
        if (list != null && !list.isEmpty() && holder.getAdapterPosition() < list.size()) {
            Picasso.get().load(list.get(holder.getAdapterPosition()).getImageUrl().isEmpty()?"https://cdn.pixabay.com/photo/2016/03/21/20/05/image-1271454_640.png":list.get(position).getImageUrl()).into(holder.imageView);
            //Picasso.get().load(list.get(holder.getAdapterPosition()).getStoreLogoUrl()).into(holder.shopLogo);
            holder.shopLogo.setText(list.get(holder.getAdapterPosition()).getStoreLogoUrl());
            holder.name.setText(list.get(holder.getAdapterPosition()).getProductName());
            holder.off.setText(list.get(holder.getAdapterPosition()).getOff());
            holder.sellprice.setText(list.get(holder.getAdapterPosition()).getNewPrice());
            holder.costprice.setText(list.get(holder.getAdapterPosition()).getOldPrice());
            holder.updateinfo.setText(list.get(holder.getAdapterPosition()).getUpdateTime());

            Log.d(TAG, "onBindViewHolder: "+list.get(holder.getAdapterPosition()).getShopUrl());
            holder.shopNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    goToUrl(urlFilter.getOriginalURL(list.get(holder.getAdapterPosition()).getShopUrl()));
                    goToUrl(list.get(holder.getAdapterPosition()).getShopUrl());
                }
            });
            holder.share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    shareItem(urlFilter.getOriginalURL(list.get(holder.getAdapterPosition()).getShopUrl()));
                    shareItem(list.get(holder.getAdapterPosition()).getShopUrl());
                }
            });
        }
    }

    private void shareItem(String url) {
        Log.d(TAG, "shareItem: "+url);
        if (url != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, url);
            context.startActivity(Intent.createChooser(shareIntent, "Share via"));
        }
    }

    private void goToUrl(String url)
    {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(browserIntent);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class HourDealsHolder extends RecyclerView.ViewHolder{
        public ImageView imageView;
        public TextView off,name,sellprice, costprice, updateinfo,shopLogo;
        public Button shopNow;
        public MaterialCardView share;
        public HourDealsHolder(@NonNull View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.productImage);
            this.shopLogo = itemView.findViewById(R.id.shopImage);
            this.off = itemView.findViewById(R.id.off);
            this.name = itemView.findViewById(R.id.name);
            this.sellprice = itemView.findViewById(R.id.sellPrice);
            this.costprice = itemView.findViewById(R.id.costPrice);
            costprice.setPaintFlags(costprice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            this.updateinfo = itemView.findViewById(R.id.updateTime);
            this.shopNow = itemView.findViewById(R.id.button);
            this.share = itemView.findViewById(R.id.share);
        }
    }
}
