package com.example.eatitv2server.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eatitv2server.Callback.IRecyclerClickListener;
import com.example.eatitv2server.Model.ShipperModel;
import com.example.eatitv2server.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyShipperSelectedAdapter extends RecyclerView.Adapter<MyShipperSelectedAdapter.MyViewHolder> {

    private Context context;
    List<ShipperModel> shipperModelList;

    private ImageView lastCheckedImageView = null;
    private ShipperModel selectedShipper = null;

    public MyShipperSelectedAdapter(Context context, List<ShipperModel> shipperModelList) {
        this.context = context;
        this.shipperModelList = shipperModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_shipper_selected, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_name.setText(new StringBuilder(shipperModelList.get(position).getName()));
        holder.txt_phone.setText(new StringBuilder(shipperModelList.get(position).getName()));
        holder.setiRecyclerClickListener((view, pos) -> {
            if (lastCheckedImageView != null)
                lastCheckedImageView.setImageResource(0);
            holder.img_checked.setImageResource(R.drawable.ic_check_black_24dp);
            lastCheckedImageView = holder.img_checked;
            selectedShipper = shipperModelList.get(pos);
        });

    }

    public ShipperModel getSelectedShipper() {
        return selectedShipper;
    }

    @Override
    public int getItemCount() {
        return shipperModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Unbinder unbinder;

        @BindView(R.id.txt_name)
        TextView txt_name;
        @BindView(R.id.txt_phone)
        TextView txt_phone;
        @BindView(R.id.img_checked)
        ImageView img_checked;

        IRecyclerClickListener iRecyclerClickListener;

        public void setiRecyclerClickListener(IRecyclerClickListener iRecyclerClickListener) {
            this.iRecyclerClickListener = iRecyclerClickListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);


        }

        @Override
        public void onClick(View view) {
            iRecyclerClickListener.onItemClickListener(view, getAdapterPosition());
        }
    }
}
