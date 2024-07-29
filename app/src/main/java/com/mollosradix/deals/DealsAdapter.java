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

public class DealsAdapter extends RecyclerView.Adapter<DealsAdapter.HourDealsHolder> {

    private final List<DealsModel> list;
    private final Context context;

    public DealsAdapter(List<DealsModel> data, FragmentActivity application) {
        this.list = data;
        this.context = application;
//        setHasStableIds(true);
    }

    @NonNull
    @Override
    public HourDealsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);
        return new HourDealsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourDealsHolder holder, int position) {
        if (list != null && !list.isEmpty() && position < list.size()) {
            DealsModel deal = list.get(position);

            Picasso.get().load(deal.getImageUrl().isEmpty() ? "https://cdn.pixabay.com/photo/2016/03/21/20/05/image-1271454_640.png" : deal.getImageUrl()).into(holder.imageView);
            holder.shopLogo.setText(deal.getStoreLogoUrl());
            holder.name.setText(deal.getProductName());
            holder.off.setText(deal.getOff());
            holder.sellprice.setText(deal.getNewPrice());
            holder.costprice.setText(deal.getOldPrice());
            holder.updateinfo.setText(deal.getUpdateTime());
            holder.costprice.setPaintFlags(holder.costprice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.shopNow.setOnClickListener(view -> goToUrl(deal.getShopUrl()));
            holder.share.setOnClickListener(v -> shareItem(deal.getShopUrl()));
        }
    }

    private void shareItem(String url) {
        if (url != null && !url.isEmpty()) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, url);
            context.startActivity(Intent.createChooser(shareIntent, "Share via"));
        } else {
            Log.e(TAG, "Invalid URL");
        }
    }

    private void goToUrl(String url) {
        if (url != null && !url.isEmpty()) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(browserIntent);
        } else {
            Log.e(TAG, "Invalid URL");
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class HourDealsHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView off, name, sellprice, costprice, updateinfo, shopLogo;
        public Button shopNow;
        public MaterialCardView share;

        public HourDealsHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.productImage);
            shopLogo = itemView.findViewById(R.id.shopImage);
            off = itemView.findViewById(R.id.off);
            name = itemView.findViewById(R.id.name);
            sellprice = itemView.findViewById(R.id.sellPrice);
            costprice = itemView.findViewById(R.id.costPrice);
            updateinfo = itemView.findViewById(R.id.updateTime);
            shopNow = itemView.findViewById(R.id.button);
            share = itemView.findViewById(R.id.share);
        }
    }
}
