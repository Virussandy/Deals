package com.mollosradix.deals;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DealsAdapter extends RecyclerView.Adapter<DealsAdapter.HourDealsHolder> {

    List<DealsModel> list;
    Context context;

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
        Picasso.get().load(list.get(position).getImageUrl().isEmpty()?"https://cdn.pixabay.com/photo/2016/03/21/20/05/image-1271454_640.png":list.get(position).getImageUrl()).into(holder.imageView);
        Picasso.get().load(list.get(position).getStoreLogoUrl()).into(holder.shopLogo);
        holder.name.setText(list.get(position).getProductName());
        holder.off.setText(list.get(position).getOff());
        holder.sellprice.setText(list.get(position).getNewPrice());
        holder.costprice.setText(list.get(position).getOldPrice());
        holder.updateinfo.setText(list.get(position).getUpdateTime());
        holder.shopNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirect(holder);
            }
        });
    }

    private void redirect(HourDealsHolder holder) {
        String url = list.get(holder.getAdapterPosition()).getShopUrl();
        String pattern = "redirectpid1=([^&]+)&store=([^&]+)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(url);

        if (m.find()) {
            String productId = m.group(1);
            String store = m.group(2);

            if (store != null) {
                String storeUrl = null;

                switch (store) {
                    case "amazon":
                        storeUrl = "https://www.amazon.in/dp/" + productId;
                        break;
                    case "myntra":
                        storeUrl = "https://www.myntra.com/" + productId;
                        break;
                    case "flipkart":
                        storeUrl = "https://www.flipkart.com/a/p/b?pid=" + productId;
                        break;
                    case "ajio":
                        storeUrl = "https://www.ajio.com/p/" + productId;
                        break;
                }

                if (storeUrl != null) {
                    goToUrl(storeUrl);
                    return;
                }
            }
        }

        goToUrl(url);
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
        public ImageView imageView,shopLogo;
        public TextView off,name,sellprice, costprice, updateinfo;
        public Button shopNow;
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
        }
    }
}
