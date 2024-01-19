package com.mollosradix.deals;

public class DealsModel {
    private String imageUrl;
    private String productName;
    private String newPrice;
    private String oldPrice;
    private String UpdateTime;
    private String storeLogoUrl;
    private String shopUrl;
    private String off;

    public DealsModel(String imageUrl, String productName, String newPrice, String oldPrice, String updateTime, String storeLogoUrl, String shopUrl, String off) {
        this.imageUrl = imageUrl;
        this.productName = productName;
        this.newPrice = newPrice;
        this.oldPrice = oldPrice;
        UpdateTime = updateTime;
        this.storeLogoUrl = storeLogoUrl;
        this.shopUrl = shopUrl;
        this.off = off;
    }

    public DealsModel() {
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(String newPrice) {
        this.newPrice = newPrice;
    }

    public String getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(String oldPrice) {
        this.oldPrice = oldPrice;
    }

    public String getUpdateTime() {
        return UpdateTime;
    }

    public void setUpdateTime(String updateTime) {
        UpdateTime = updateTime;
    }

    public String getStoreLogoUrl() {
        return storeLogoUrl;
    }

    public void setStoreLogoUrl(String storeLogoUrl) {
        this.storeLogoUrl = storeLogoUrl;
    }

    public String getShopUrl() {
        return shopUrl;
    }

    public void setShopUrl(String shopUrl) {
        this.shopUrl = shopUrl;
    }

    public String getOff() {
        return off;
    }

    public void setOff(String off) {
        this.off = off;
    }
}
