package com.nghiem.rilleyServer.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nghiem.rilleyServer.Callback.IRecyclerClickListener;
import com.nghiem.rilleyServer.EventBus.SelectedAddonModel;
import com.nghiem.rilleyServer.EventBus.UpdateSugarModel;
import com.nghiem.rilleyServer.Model.SugarModel;
import com.nghiem.rilleyServer.R;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MySugarAdapter extends RecyclerView.Adapter<MySugarAdapter.MyViewHolder> {

    Context context;
    List<SugarModel> sugarModels;
    UpdateSugarModel updateSugarModel;
    int editPos;

    public MySugarAdapter(Context context, List<SugarModel> sugarModels) {
        this.context = context;
        this.sugarModels = sugarModels;
        updateSugarModel = new UpdateSugarModel();
        editPos=-1;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_size_addon_display, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_name.setText(sugarModels.get(position).getName());
        holder.txt_price.setText(String.valueOf(sugarModels.get(position).getPrice()));

        holder.img_delete.setOnClickListener(v -> {
            //Delete Item
            sugarModels.remove(position);
            notifyItemRemoved(position);
            updateSugarModel.setAddonModel(sugarModels); // Set for Event
            EventBus.getDefault().postSticky(updateSugarModel); //Send for Event
        });

        holder.setListener(new IRecyclerClickListener() {
            @Override
            public void onItemClickListener(View view, int pos) {
                editPos = position;
                EventBus.getDefault().postSticky(new SelectedAddonModel(sugarModels.get(pos)));
            }
        });


    }

    @Override
    public int getItemCount() {
        if (sugarModels != null)
            return sugarModels.size();
        else
            return 0;
    }

    public void addNewAddon(SugarModel sugarModel) {
        sugarModels.add(sugarModel);
        notifyItemInserted(sugarModels.size()-1);
        updateSugarModel.setAddonModel(sugarModels);
        EventBus.getDefault().postSticky(updateSugarModel);
    }

    public void editAddon(SugarModel sugarModel) {
        if(editPos!=-1)
        {
            sugarModels.set(editPos, sugarModel);
            notifyItemChanged(editPos);
            editPos=-1; //Reset variable after success
            //Send update
            updateSugarModel.setAddonModel(sugarModels);
            EventBus.getDefault().postSticky(updateSugarModel);
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_name)
        TextView txt_name;
        @BindView(R.id.txt_price)
        TextView txt_price;
        @BindView(R.id.img_delete)
        ImageView img_delete;

        Unbinder unbinder;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(view ->
                    listener.onItemClickListener(view, getAdapterPosition()));

        }
    }
}

