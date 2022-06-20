package com.example.eatitv2shipper.ui.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eatitv2shipper.R;
import com.example.eatitv2shipper.adapter.MyShippingOrderAdapter;
import com.example.eatitv2shipper.common.Common;
import com.example.eatitv2shipper.databinding.FragmentHomeBinding;
import com.example.eatitv2shipper.model.ShippingOrderModel;

import java.util.List;

public class HomeFragment extends Fragment {

    RecyclerView recycler_order;

    LayoutAnimationController layoutAnimationController;
    MyShippingOrderAdapter adapter;
    List<ShippingOrderModel> shippingOrderModelList;

    private HomeViewModel homeViewModel;

    private FragmentHomeBinding binding;

    @SuppressLint("FragmentLiveDataObserve")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        recycler_order = binding.recyclerOder;
        homeViewModel.getMessageError().observe(this,s -> {
            Toast.makeText(getContext(),s , Toast.LENGTH_LONG).show();
        });
        homeViewModel.getShippingOrderMutableData(Common.currentShipperUser.getPhone())
                .observe(getViewLifecycleOwner(), shippingOrderModels -> {
                    shippingOrderModelList = shippingOrderModels;
                    adapter = new MyShippingOrderAdapter(getContext(), shippingOrderModels);
                    recycler_order.setAdapter(adapter);
                    recycler_order.setLayoutAnimation(layoutAnimationController);
                });
        initViews(root);
        return root;
    }

    private void initViews(View root) {
        ShippingOrderModel shippingOrderModel = new ShippingOrderModel();
        homeViewModel.loadOrderByShipper(Common.currentShipperUser.getPhone());
        Toast.makeText(getContext(),"adsadsadsadadsa" , Toast.LENGTH_LONG).show();
        Log.i("data",""+shippingOrderModel.toString());



        recycler_order.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_order.setLayoutManager(layoutManager);
        recycler_order.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}