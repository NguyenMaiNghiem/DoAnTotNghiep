package com.nghiem.rilleyClient.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nghiem.rilleyClient.Database.CartItem;
import com.nghiem.rilleyClient.Model.SugarModel;
import com.nghiem.rilleyClient.Model.SizeModel;
import com.nghiem.rilleyClient.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyOrderDetailAdapter extends RecyclerView.Adapter<MyOrderDetailAdapter.MyViewHolder> {


    private Context context;
    private List<CartItem> cartItemList;

    Gson gson;

    public MyOrderDetailAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
        gson = new Gson();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_order_detail_item, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Glide.with(context)
                .load(cartItemList.get(position).getFoodImage())
                .into(holder.img_food_image); //Load default image in Cart

        holder.txt_food_name.setText(new StringBuilder("Tên: ")
                .append(cartItemList.get(position).getFoodName()));

        holder.txt_food_quantity.setText(new StringBuilder("Số lượng: ")
                .append(cartItemList.get(position).getFoodQuantity()));

        //Size
        if(!cartItemList.get(position).getFoodSize().equals("Binh thường"))
        {
            SizeModel sizeModel = gson.fromJson(cartItemList.get(position).getFoodSize(), new TypeToken<SizeModel>(){}.getType());

            if(sizeModel!=null)
                holder.txt_size.setText(new StringBuilder("Size: ").append(sizeModel.getName()));
        }
        else
        {
            holder.txt_size.setText(new StringBuilder("Size: Binh thường"));
        }

        //Sugar
        if(!cartItemList.get(position).getFoodAddOn().equals("Binh thường"))
        {
            SugarModel sugarModel = gson.fromJson(cartItemList.get(position).getFoodAddOn(), new TypeToken<SugarModel>(){}.getType());

            if(sugarModel!=null)
                holder.txt_food_add_on.setText(new StringBuilder("Đường: ").append(sugarModel.getName()));
        }
        else
        {
            holder.txt_food_add_on.setText(new StringBuilder("Đường: Binh thường"));
        }

//        if(!cartItemList.get(position).getFoodAddOn().equals("Default"))
//        {
//            List<SugarModel> sugarModels = gson.fromJson(cartItemList.get(position).getFoodAddOn(), new TypeToken<List<SugarModel>>(){}.getType());
//
//            StringBuilder addonString = new StringBuilder();
//            if(sugarModels !=null)
//            {
//                for(SugarModel sugarModel : sugarModels)
//                    addonString.append(sugarModel.getName()).append(",");
//
//                addonString.delete(addonString.length()-1, addonString.length()); //Remove "," last
//                holder.txt_food_add_on.setText(new StringBuilder("Đường: ").append(addonString));
//            }
//        }
//        else
//        {
//            holder.txt_food_add_on.setText(new StringBuilder("Đường: Default"));
//        }

    }

    @Override
    public int getItemCount() {
        if (cartItemList != null)
            return cartItemList.size();
        else
            return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img_food_image)
        ImageView img_food_image;
        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.txt_food_add_on)
        TextView txt_food_add_on;
        @BindView(R.id.txt_size)
        TextView txt_size;
        @BindView(R.id.txt_food_quantity)
        TextView txt_food_quantity;

        private Unbinder unbinder;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            unbinder = ButterKnife.bind(this, itemView);
        }
    }
}
