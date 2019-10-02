package com.kevin.inclass03;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import java.util.ArrayList;

public class CartCellAdapter extends ArrayAdapter<Item> {

    Context context;
    int resource;
    ArrayList<Item> itemList;
    CartActiviy activity;

    public CartCellAdapter(Context context, int resource, ArrayList<Item> itemList, CartActiviy activity) {
        super(context, resource, itemList);

        this.context = context;
        this.resource = resource;
        this.itemList = itemList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.cartcell, parent, false);

        TextView name = view.findViewById(R.id.lblName);
        TextView price = view.findViewById(R.id.lblPrice);
        TextView region = view.findViewById(R.id.lblRegion);
        ImageView itemImage = view.findViewById(R.id.imgItem);

        Item item = itemList.get(position);

        name.setText(item.getName());
        price.setText(Double.toString(item.getPrice()));
        region.setText(item.getRegion());

        if (item.getPhoto() != "null") {
            String[] photoParts = item.getPhoto().split("\\.");
            String withoutPng = photoParts[0];

            int imgResource = context.getResources().getIdentifier(withoutPng, "drawable", context.getPackageName());
            itemImage.setImageResource(imgResource);
        } else {
            int imgResource = context.getResources().getIdentifier("not_found", "drawable", context.getPackageName());
            itemImage.setImageResource(imgResource);
        }

        return view;
    }






}
