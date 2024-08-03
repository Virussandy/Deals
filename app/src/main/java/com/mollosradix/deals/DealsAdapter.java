package com.mollosradix.deals;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DealsAdapter extends RecyclerView.Adapter<DealsAdapter.HourDealsHolder> {

    private final List<DealsModel> list;
    private final Context context;

    public DealsAdapter(List<DealsModel> data, FragmentActivity application) {
        this.list = data;
        this.context = application;
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

            AssetManager assetManager = context.getAssets();
            try (InputStream inputStream = assetManager.open("send.png")) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                holder.shareImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                Log.e(TAG, "Asset Load Failed: " + e.getMessage());
            }
            Picasso.get().load(deal.getImageUrl().isEmpty() ? "https://cdn.pixabay.com/photo/2016/03/21/20/05/image-1271454_640.png" : deal.getImageUrl()).into(holder.imageView);
//                    .into(new Target() {
//                        @Override
//                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                            holder.imageView.setImageBitmap(bitmap);
//
//                            // Generate palette from bitmap
//                            Palette.from(bitmap).generate(palette -> {
//                                if (palette != null) {
//                                    // Define a set of predefined base colors
//                                    int baseCardColor = context.getResources().getColor(R.color.default_card_color, context.getTheme());
//                                    int dominantColor = getAdjustedColor(palette.getDominantColor(baseCardColor), 0.2f);
//                                    int vibrantColor = getAdjustedColor(palette.getVibrantColor(baseCardColor), 0.3f);
//                                    int mutedColor = getAdjustedColor(palette.getMutedColor(baseCardColor), 0.1f);
//
//                                    // Apply base color to main card
//                                    holder.mainCard.setCardBackgroundColor(baseCardColor);
//
//                                    // Apply dynamic colors to buttons and secondary cards
//                                    holder.shopNow.setBackgroundColor(vibrantColor);
//                                    holder.offCard.setCardBackgroundColor(vibrantColor);
//                                    holder.shareCard.setCardBackgroundColor(mutedColor);
//
//                                    // Set text colors based on background luminance
//                                    setTextColorBasedOnBackground(holder.name, baseCardColor);
//                                    setTextColorBasedOnBackground(holder.costprice, baseCardColor);
//                                    setTextColorBasedOnBackground(holder.sellprice, baseCardColor);
//                                    setTextColorBasedOnBackground(holder.updateinfo, baseCardColor);
//                                    setTextColorBasedOnBackground(holder.shopLogo, baseCardColor);
//                                    setTextColorBasedOnBackground(holder.off, baseCardColor);
//
//                                    // Set button and secondary card text colors
//                                    setTextColorBasedOnBackground(holder.shopNow, vibrantColor);
//                                    setTextColorBasedOnBackground(holder.off, vibrantColor);
//                                    setTextColorBasedOnBackground(holder.shareImage, mutedColor);
//                                }
//                            });
//                        }
//
//                        private int getAdjustedColor(int color, float adjustment) {
//                            // Adjust color brightness
//                            float[] hsl = new float[3];
//                            ColorUtils.colorToHSL(color, hsl);
//                            hsl[2] = Math.min(1.0f, hsl[2] + adjustment); // Adjust lightness
//                            return ColorUtils.HSLToColor(hsl);
//                        }
//
//                        @Override
//                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
//                            Log.e(TAG, "Bitmap Load Failed: " + e.getMessage());
//                        }
//
//                        @Override
//                        public void onPrepareLoad(Drawable placeHolderDrawable) {
//                        }
//                    });

            holder.shopLogo.setText(deal.getStoreLogoUrl());
            holder.name.setText(deal.getProductName());
            holder.off.setText(deal.getOff());
            holder.sellprice.setText(deal.getNewPrice());
            holder.costprice.setText(deal.getOldPrice());
            holder.updateinfo.setText(deal.getUpdateTime());
            holder.costprice.setPaintFlags(holder.costprice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.shopNow.setOnClickListener(view -> goToUrl(deal.getShopUrl()));
            holder.shareCard.setOnClickListener(v -> shareItem(deal.getShopUrl()));
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
        public final ImageView imageView;
        public final ImageView shareImage;
        public final TextView off;
        public final TextView name;
        public final TextView sellprice;
        public final TextView costprice;
        public final TextView updateinfo;
        public final TextView shopLogo;
        public final Button shopNow;
        public final MaterialCardView mainCard;
        public final MaterialCardView offCard;
        public final MaterialCardView shareCard;

        public HourDealsHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.productImage);
            shareImage = itemView.findViewById(R.id.share_image);
            shopLogo = itemView.findViewById(R.id.shopImage);
            off = itemView.findViewById(R.id.off);
            name = itemView.findViewById(R.id.name);
            sellprice = itemView.findViewById(R.id.sellPrice);
            costprice = itemView.findViewById(R.id.costPrice);
            updateinfo = itemView.findViewById(R.id.updateTime);
            shopNow = itemView.findViewById(R.id.button);
            mainCard = itemView.findViewById(R.id.main_card);
            offCard = itemView.findViewById(R.id.off_card);
            shareCard = itemView.findViewById(R.id.share_card);
        }
    }

//    private void setTextColorBasedOnBackground(TextView textView, int backgroundColor) {
//        boolean isDark = ColorUtils.calculateLuminance(backgroundColor) < 0.5; // Adjusted luminance threshold
//        int textColor = isDark ? Color.WHITE : Color.BLACK;
//        textView.setTextColor(textColor);
//    }
//
//    private void setTextColorBasedOnBackground(Button button, int backgroundColor) {
//        boolean isDark = ColorUtils.calculateLuminance(backgroundColor) < 0.5; // Adjusted luminance threshold
//        int textColor = isDark ? Color.WHITE : Color.BLACK;
//        button.setTextColor(textColor);
//    }
//
//    private void setTextColorBasedOnBackground(ImageView imageView, int backgroundColor) {
//        boolean isDark = ColorUtils.calculateLuminance(backgroundColor) < 0.5; // Adjusted luminance threshold
//        int iconColor = isDark ? Color.WHITE : Color.BLACK;
//        imageView.setColorFilter(iconColor);
//    }
}
