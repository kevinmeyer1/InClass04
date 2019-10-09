package com.kevin.inclass03;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.support.v4.app.INotificationSideChannel;
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
    CartActiviy cartActiviy;
    ShoppingActivity shoppingActivity;

    public CartCellAdapter(Context context, int resource, ArrayList<Item> itemList, CartActiviy cartActiviy, ShoppingActivity shoppingActivity) {
        super(context, resource, itemList);

        this.context = context;
        this.resource = resource;
        this.itemList = itemList;
        this.cartActiviy = cartActiviy;
        this.shoppingActivity = shoppingActivity;
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
        TextView amount = view.findViewById(R.id.lblAmount);

        final Item item = itemList.get(position);

        name.setText(item.getName());
        price.setText(Double.toString(item.getPrice()));
        region.setText(item.getRegion());
        amount.setText("Quantity: " + Integer.toString(item.getAmount()));

        if (item.getPhoto() != "null") {
            String[] photoParts = item.getPhoto().split("\\.");
            String withoutPng = photoParts[0];

            int imgResource = context.getResources().getIdentifier(withoutPng, "drawable", context.getPackageName());
            itemImage.setImageResource(imgResource);
        } else {
            int imgResource = context.getResources().getIdentifier("not_found", "drawable", context.getPackageName());
            itemImage.setImageResource(imgResource);
        }


        view.findViewById(R.id.btnAddItem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("you clicked add");
                Integer quantity = item.getAmount();
                item.setAmount(quantity + 1);

                notifyDataSetChanged();
                cartActiviy.refreshTotalPrice();
                cartActiviy.refreshTotalQuantity();
            }
        });

        view.findViewById(R.id.btnRemoveItem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer quantity = item.getAmount();

                if (quantity > 1) {
                    item.setAmount(quantity - 1);
                } else {
                    itemList.remove(item);
                }

                notifyDataSetChanged();
                cartActiviy.refreshTotalPrice();
                cartActiviy.refreshTotalQuantity();
            }
        });

        return view;
    }






}
