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

public class ItemCellAdapter extends ArrayAdapter<Item> {

    Context context;
    int resource;
    ArrayList<Item> itemList;
    ShoppingActivity activity;

    public ItemCellAdapter(Context context, int resource, ArrayList<Item> itemList, ShoppingActivity activity) {
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

        View view = inflater.inflate(R.layout.itemcell, parent, false);

        TextView name = view.findViewById(R.id.lblName);
        TextView price = view.findViewById(R.id.lblPrice);
        TextView region = view.findViewById(R.id.lblRegion);
        ImageView itemImage = view.findViewById(R.id.imgItem);

        Item item = itemList.get(position);

        name.setText(item.getName());
        price.setText("$" + Double.toString(item.getPrice()));
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


        view.findViewById(R.id.btnAddToCart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Item currentItem = itemList.get(position);
                String toastText = currentItem.getName() + " added to cart";

                Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();

                if (activity.getCart().indexOf(currentItem) == -1) {
                    System.out.println("did not find the item in the cart");
                    activity.addToCart(currentItem);
                } else {
                    System.out.println("item found in cart");

                    Integer itemPositionInCart = activity.getCart().indexOf(currentItem);
                    Item itemInCart = activity.getCart().get(itemPositionInCart);

                    Integer currentAmount = itemInCart.getAmount();
                    activity.removeItemFromCart(itemInCart);

                    currentItem.setAmount(currentAmount + 1);
                    activity.addToCart(currentItem);
                }
            }
        });

        return view;
    }






}
