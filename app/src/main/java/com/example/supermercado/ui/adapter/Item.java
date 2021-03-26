package com.example.supermercado.ui.adapter;

import android.os.Parcel;
import android.os.Parcelable;

public class Item implements Parcelable {
    private String id;
    private String img;
    private String code;
    private String name;
    private String barcode;
    private String description;
    private String expiration;
    private String pricepayment;
    private String quantity;
    private String price;

    public Item(String id, String img, String code, String name, String barcode, String description, String expiration, String pricepayment, String quantity, String price) {
        this.id = id;
        this.img = img;
        this.code = code;
        this.name = name;
        this.barcode = barcode;
        this.description = description;
        this.expiration = expiration;
        this.pricepayment = pricepayment;
        this.quantity = quantity;
        this.price = price;
    }

    protected Item(Parcel in) {
        id = in.readString();
        img = in.readString();
        code = in.readString();
        name = in.readString();
        barcode = in.readString();
        description = in.readString();
        expiration = in.readString();
        pricepayment = in.readString();
        quantity = in.readString();
        price = in.readString();
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

    public String getId() {
        return id;
    }

    public String getImg() {
        return img;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getDescription() {
        return description;
    }

    public String getExpiration() {
        return expiration;
    }

    public String getPricepayment() {
        return pricepayment;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getPrice() {
        return price;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(img);
        dest.writeString(code);
        dest.writeString(name);
        dest.writeString(barcode);
        dest.writeString(description);
        dest.writeString(expiration);
        dest.writeString(pricepayment);
        dest.writeString(quantity);
        dest.writeString(price);
    }
}
