package com.kevin.inclass03;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import java.util.ArrayList;

public class ItemCellAdapter extends ArrayAdapter<Item> {

    Context context;
    int resource;
    ArrayList<Item> itemList;

    public ItemCellAdapter(Context context, int resource, ArrayList<Item> itemList) {
        super(context, resource, itemList);

        this.context = context;
        this.resource = resource;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(resource, null);

        TextView name = view.findViewById(R.id.lblName);
        TextView price = view.findViewById(R.id.lblPrice);
        TextView region = view.findViewById(R.id.lblRegion);
        ImageView itemImage = view.findViewById(R.id.imgItem);

        Item item = itemList.get(position);

        name.setText(item.getName());
        price.setText(Double.toString(item.getPrice()));
        region.setText(item.getRegion());

        if (item.getPhoto() != "null") {
            int imgResource = context.getResources().getIdentifier(item.getPhoto(), "drawable", context.getPackageName());
            itemImage.setImageResource(imgResource);
        } else {
            System.out.println(position);
        }

        view.findViewById(R.id.btnAddToCart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Item getItem(int i) {
        return itemList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }





}
