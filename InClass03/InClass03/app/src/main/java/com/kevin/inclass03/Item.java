package com.kevin.inclass03;

public class Item {

    private int discount;
    private String name;
    private String photo;
    private double price;
    private String region;

    public Item(int discount, String name, String photo, double price, String region) {
        this.discount = discount;
        this.name = name;
        this.photo = photo;
        this.price = price;
        this.region = region;
    }

    public int getDiscount() {
        return discount;
    }

    public String getName() {
        return name;
    }

    public String getPhoto() {
        return photo;
    }

    public double getPrice() {
        return price;
    }

    public String getRegion() {
        return region;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setRegion(String region) {
        this.region = region;
    }


}
