package com.kevin.inclass03;

import android.os.Parcel;
import android.os.Parcelable;

public class Item implements Parcelable{

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

    protected Item(Parcel in) {
        discount = in.readInt();
        name = in.readString();
        photo = in.readString();
        price = in.readDouble();
        region = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(discount);
        dest.writeString(name);
        dest.writeString(photo);
        dest.writeDouble(price);
        dest.writeString(region);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

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
