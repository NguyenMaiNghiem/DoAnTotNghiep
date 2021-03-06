package com.nghiem.rilleyClient.ui.cart;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nghiem.rilleyClient.Adapter.MyCartAdapter;
import com.nghiem.rilleyClient.Callback.ILoadTimeFromFirebaseListener;
import com.nghiem.rilleyClient.Callback.ISearchCategoryCallbackListener;
import com.nghiem.rilleyClient.Common.Common;
import com.nghiem.rilleyClient.Common.MySwipeHelper;
import com.nghiem.rilleyClient.Database.CartDataSource;
import com.nghiem.rilleyClient.Database.CartDatabase;
import com.nghiem.rilleyClient.Database.CartItem;
import com.nghiem.rilleyClient.Database.LocalCartDataSource;
import com.nghiem.rilleyClient.EventBus.CounterCartEvent;
import com.nghiem.rilleyClient.EventBus.HideFABCart;
import com.nghiem.rilleyClient.EventBus.MenuItemBack;
import com.nghiem.rilleyClient.EventBus.UpdateItemInCart;
import com.nghiem.rilleyClient.Model.DiscountModel;
import com.nghiem.rilleyClient.Model.MilkteaLocationModel;
import com.nghiem.rilleyClient.Model.SugarModel;
import com.nghiem.rilleyClient.Model.CategoryModel;
import com.nghiem.rilleyClient.Model.FCMSendData;
import com.nghiem.rilleyClient.Model.FoodModel;
import com.nghiem.rilleyClient.Model.OrderModel;
import com.nghiem.rilleyClient.Model.SizeModel;
import com.nghiem.rilleyClient.R;
import com.nghiem.rilleyClient.Remote.IFCMService;
import com.nghiem.rilleyClient.Remote.RetrofitFCMClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nghiem.rilleyClient.ScanQRActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class CartFragment extends Fragment implements ILoadTimeFromFirebaseListener, ISearchCategoryCallbackListener{

    private static final int SCAN_QR_PERMISSION = 7171;
    //Update Cart Dialog
    private BottomSheetDialog addonBottomSheetDialog;
    private ChipGroup chip_group_addon, chip_group_user_selected_addon;
    private EditText edt_search;

    private ISearchCategoryCallbackListener searchFoodCallbackListener;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Parcelable recyclerViewState;
    private CartDataSource cartDataSource;

    ILoadTimeFromFirebaseListener listener;


    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;

    String address,comment;

    IFCMService ifcmService;

    private Place placeSelected;
    private AutocompleteSupportFragment places_fragment;
    private PlacesClient placesClient;
    private List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);


    @BindView(R.id.recycler_cart)
    RecyclerView recycler_cart;
    @BindView(R.id.txt_total_price)
    TextView txt_total_price;
    @BindView(R.id.txt_empty_cart)
    TextView txt_empty_cart;
    @BindView(R.id.group_place_holder)
    CardView group_place_holder;
    @BindView(R.id.edt_discount_code)
    EditText edt_discount_code;

    private MyCartAdapter cartAdapter;

    View view;

    private Unbinder unbinder;

    private CartViewModel cartViewModel;

    @OnClick(R.id.img_scan)
    void onScanQRcode(){
        startActivityForResult(new Intent(requireContext(), ScanQRActivity.class),SCAN_QR_PERMISSION);
    }

    @OnClick(R.id.img_check)
    void onApplyDiscount(){
        if (!TextUtils.isEmpty(edt_discount_code.getText().toString()))
        {
            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setCancelable(false)
                    .setMessage("Vui l??ng ?????i...")
                    .create();
            dialog.show();

            final DatabaseReference offsetRef = FirebaseDatabase.getInstance(Common.URL)
                    .getReference(".info/serverTimeOffset");
            final DatabaseReference discountRef = FirebaseDatabase.getInstance(Common.URL)
                    .getReference(Common.MILKTEA_REF)
                    .child(Common.currentMilktea.getUid())
                    .child(Common.DISCOUNT);
            offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long offset = snapshot.getValue(Long.class);
                    long estimatedServerTimeMs = System.currentTimeMillis() + offset;

                    discountRef.child(edt_discount_code.getText().toString().toLowerCase())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists())
                                    {
                                        DiscountModel discountModel = snapshot.getValue(DiscountModel.class);
                                        discountModel.setKey(snapshot.getKey());
                                        if (discountModel.getUntilDate() < estimatedServerTimeMs)
                                        {
                                            dialog.dismiss();
                                            listener.onLoadTimeFailed("M?? gi???m gi?? ???? h???t h???n");
                                        }
                                        else {
                                            dialog.dismiss();
                                            Common.discountApply = discountModel;
                                            sumAllItemInCart();
                                        }
                                    }
                                    else {
                                        dialog.dismiss();
                                        listener.onLoadTimeFailed("M?? gi???m gi?? kh??ng h???p l???");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    dialog.dismiss();
                                    listener.onLoadTimeFailed(error.getMessage());
                                }
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    dialog.dismiss();
                    listener.onLoadTimeFailed(error.getMessage());
                }
            });

        }
    }

    @OnClick(R.id.btn_place_order)
    void onPlaceOrderClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Th??m 1 b?????c n???a!");

        if (view == null)
            view = LayoutInflater.from(getContext()).inflate(R.layout.layout_place_order, null);


        EditText edt_address = (EditText) view.findViewById(R.id.edt_address);
        EditText edt_comment = (EditText) view.findViewById(R.id.edt_comment);
        TextView txt_address = (TextView) view.findViewById(R.id.txt_address_detail);
        RadioButton rdi_home = (RadioButton) view.findViewById(R.id.rdi_home_address);
        RadioButton rdi_other_address = (RadioButton) view.findViewById(R.id.rdi_other_address);
        RadioButton rdi_ship_to_this = (RadioButton) view.findViewById(R.id.rdi_ship_this_address);
        RadioButton rdi_cod = (RadioButton) view.findViewById(R.id.rdi_cod);

        if (places_fragment == null){
            places_fragment = (AutocompleteSupportFragment) getActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.places_autocomplete_fragment);
            places_fragment.setPlaceFields(placeFields);
            places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    placeSelected = place;
                    txt_address.setText(place.getAddress());
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(getContext(), "" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


        //Data
        txt_address.setText(Common.currentUser.getAddress()); //By Default We select, Home Address, So Users' Address Will display

        //Event
        rdi_home.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                txt_address.setText(Common.currentUser.getAddress());
                txt_address.setVisibility(View.VISIBLE);
                places_fragment.setHint(Common.currentUser.getAddress());
            }
        });

        rdi_other_address.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                txt_address.setVisibility(View.VISIBLE);
            }
        });

        rdi_ship_to_this.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                fusedLocationProviderClient.getLastLocation()
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                txt_address.setVisibility(View.GONE);
                            }
                        })
                        .addOnCompleteListener(new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                String coordinates = new StringBuilder()
                                        .append(task.getResult().getLatitude())
                                        .append("/")
                                        .append(task.getResult().getLongitude()).toString();

                                Single<String> singleAddress = Single.just(getAddressFromLatLng(task.getResult().getLatitude(),
                                        task.getResult().getLongitude()));

                                Disposable disposable = singleAddress.subscribeWith(new DisposableSingleObserver<String>() {

                                    @Override
                                    public void onSuccess(String s) {
//                                        edt_address.setText(coordinates);       //xoa
                                        txt_address.setText(s);
                                        txt_address.setVisibility(View.VISIBLE);
                                        places_fragment.setHint(s);
                                    }

                                    @Override
                                    public void onError(Throwable e) {
//                                        edt_address.setText(coordinates);   //xoa
                                        txt_address.setText(e.getMessage());
                                        txt_address.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        });
            }
        });

        builder.setView(view);
        builder.setNegativeButton("NO", (dialogInterface, which) -> dialogInterface.dismiss());
        builder.setPositiveButton("YES", (dialogInterface, which) -> {

            address = edt_address.getText().toString();
            comment = edt_comment.getText().toString();

            dialogInterface.dismiss();  //close dialog before process to remove crash
            if (rdi_cod.isChecked())
                paymentCOD(address, comment);
        });

        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialogInterface -> {
            if (places_fragment != null){
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .remove(places_fragment)
                        .commit();
            }
        });
        dialog.show();
    }

    private void paymentCOD(String address, String comment) {

        FirebaseDatabase.getInstance(Common.URL)
                .getReference(Common.MILKTEA_REF)
                .child(Common.currentMilktea.getUid())
                .child(Common.LOCATION_REF)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists())
                        {
                            MilkteaLocationModel location = snapshot.getValue(MilkteaLocationModel.class);
                            applyShippingCostByLocation(location);
                        }
                        else
                            applyShippingCostByLocation(null);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void applyShippingCostByLocation(MilkteaLocationModel location) {

        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid(),
                Common.currentMilktea.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cartItems -> {
                    //When We have all Cart items, We can have total Price
                    cartDataSource.sumPriceInCart(Common.currentUser.getUid(),
                            Common.currentMilktea.getUid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Double>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onSuccess(Double totalPrice) {
                                    double finalPrice = totalPrice; //We will modify this formula for discount late
                                    OrderModel order = new OrderModel();
                                    order.setUserId(Common.currentUser.getUid());
                                    order.setUserName(Common.currentUser.getName());
                                    order.setUserPhone(Common.currentUser.getPhone());
                                    order.setShippingAddress(address);
                                    order.setComment(comment);

                                    if (currentLocation != null) {
                                        order.setLat(currentLocation.getLatitude());
                                        order.setLng(currentLocation.getLongitude());

                                        if (location != null){
                                            order.setLat(currentLocation.getLatitude());
                                            order.setLng(currentLocation.getLongitude());

                                            if (location != null)
                                            {
                                                Location orderLocation = new Location("");
                                                orderLocation.setLatitude(currentLocation.getLatitude());
                                                orderLocation.setLongitude(currentLocation.getLongitude());

                                                Location milkteaLocation = new Location("");
                                                milkteaLocation.setLatitude(location.getLat());
                                                milkteaLocation.setLongitude(location.getLng());

                                                //Caculate fee ship
                                                float distance = orderLocation.distanceTo(milkteaLocation)/1000; //in KM
                                                if (distance * Common.SHIPPING_COST_PER_KM > Common.MAX_SHIPPING_COST)
                                                    order.setShippingCost(Common.MAX_SHIPPING_COST);
                                                else
                                                    order.setShippingCost(distance * Common.SHIPPING_COST_PER_KM);

                                            }
                                            else
                                                order.setShippingCost(0);
                                        }

                                    } else {
                                        order.setLat(-01.f);
                                        order.setLng(-01.f);

                                        //if we can't calculate shipping , we just set max shipping order
                                        order.setShippingCost(Common.MAX_SHIPPING_COST);
                                    }

                                    order.setCartItemList(cartItems);
                                    order.setTotalPayment(totalPrice);
                                    if (Common.discountApply != null)
                                        order.setDiscount(Common.discountApply.getPercent());
                                    else
                                        order.setDiscount(0);
                                    order.setFinalPayment(finalPrice);
                                    order.setCod(true);
                                    order.setTransactionId("Cash On Delivery");

                                    //Submit this order subject to Firebase
                                    syncLocalTimeWithGlobalTime(order);
//                                    writeOrderToFirebase(order);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    if (!e.getMessage().contains("Query returned empty result set"))
                                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                }, throwable -> {
                    Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));

    }

    private void  syncLocalTimeWithGlobalTime(OrderModel order) {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setCancelable(false)
                .setTitle("Ph?? Giao H??ng")
                .setMessage(new StringBuilder("?????c t??nh l?? ")
                .append(Math.round(order.getShippingCost()*100.0)/100.0)
                .append("VND cho ????n h??ng c???a b???n\nT???ng ti???n ????n h??ng c???a b???n l??: ")
                .append(Math.round((order.getFinalPayment()+order.getShippingCost())*100.0)/100.0)
                .append("VND").toString())
                .setNegativeButton("No", (dialogInterface, which) -> dialogInterface.dismiss())
                .setPositiveButton("YES", (dialogInterface, which) -> {
                    dialogInterface.dismiss();  //Fix crash
                    final DatabaseReference offsetRef =
                            FirebaseDatabase.getInstance(Common.URL).getReference(".info/serverTimeOffset");
                    offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            long offset = dataSnapshot.getValue(Long.class);
                            long estimateServerTimeMs = System.currentTimeMillis() + offset; //Offset is missing time between your local and server time.
                            SimpleDateFormat sdf = new SimpleDateFormat("MM dd,yyyy HH:mm");
                            Date resultDate = new Date(estimateServerTimeMs);
                            Log.d("TEST_DATE", "" + sdf.format(resultDate));

                            listener.onLoadTimeSuccess(order, estimateServerTimeMs);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            listener.onLoadTimeFailed(databaseError.getMessage());
                        }
                    });

                }).create();
        dialog.show();
    }

    private void writeOrderToFirebase(OrderModel order) {
        FirebaseDatabase.getInstance(Common.URL)
                .getReference(Common.MILKTEA_REF)
                .child(Common.currentMilktea.getUid())
                .child(Common.ORDER_REF)
                .child(Common.createOrderNumber()) //Create Order number with only Digit
                .setValue(order)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnCompleteListener(task -> {
                    //Write Success
                    cartDataSource.cleanCart(Common.currentUser.getUid(),
                            Common.currentMilktea.getUid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onSuccess(Integer integer) {

                                    Map<String, String> notiData = new HashMap<>();
                                    notiData.put(Common.NOTI_TITLE, "New Order Client");
                                    notiData.put(Common.NOTI_CONTENT, "You have new order from " + Common.currentUser.getPhone());

                                    FCMSendData sendData =
                                            new FCMSendData(Common.createTopicOrder(), notiData);

                                    compositeDisposable.add(ifcmService.sendNotification(sendData)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(fcmResponse -> {
                                                //Clean Success
                                                Toast.makeText(getContext(), "Order Placed Successfully", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                            }, throwable -> {
                                                Toast.makeText(getContext(), "Order was sent but failure to send notification", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                            })
                                    );


                                }

                                @Override
                                public void onError(Throwable e) {
                                    Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                });

    }

    private String getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        String result = "";
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0); //Always get first item
                StringBuilder sb = new StringBuilder(address.getAddressLine(0));
                result = sb.toString();
            } else {
                result = "Address not found";
            }


        } catch (IOException e) {
            e.printStackTrace();
            result = e.getMessage();
        }

        return result;
    }

    @SuppressLint("FragmentLiveDataObserve")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        cartViewModel =
                ViewModelProviders.of(this).get(CartViewModel.class);
        View root = inflater.inflate(R.layout.fragment_cart, container, false);

        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        listener = this;

        cartViewModel.initCartDataSource(getContext());

        cartViewModel.getMutableLiveDataCartItems().observe(this, new Observer<List<CartItem>>() {
            @Override
            public void onChanged(List<CartItem> cartItems) {
                if (cartItems.isEmpty() || cartItems == null) {
                    recycler_cart.setVisibility(View.GONE);
                    group_place_holder.setVisibility(View.GONE);
                    txt_empty_cart.setVisibility(View.VISIBLE);
                } else {
                    recycler_cart.setVisibility(View.VISIBLE);
                    group_place_holder.setVisibility(View.VISIBLE);
                    txt_empty_cart.setVisibility(View.GONE);


                    cartAdapter = new MyCartAdapter(getContext(), cartItems);
                    recycler_cart.setAdapter(cartAdapter);
                }
            }
        });

//        shareViewModel.getText().observe(this, new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

        unbinder = ButterKnife.bind(this, root);

        initViews();
        initLocation();


        return root;

    }

    private void initLocation() {
        buildLocationRequest();
        buildLocationCallBack();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }

    private void initViews() {

        searchFoodCallbackListener = this;

        initPlacesClient();

        setHasOptionsMenu(true);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        EventBus.getDefault().postSticky(new HideFABCart(true));

        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_cart.setLayoutManager(layoutManager);
        recycler_cart.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(), recycler_cart, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "X??a", 30, 0, Color.parseColor("#FF3C30"),
                        pos -> {
//                            Toast.makeText(getContext(), "Delete item Click!", Toast.LENGTH_SHORT).show();
                            CartItem cartItem = cartAdapter.getItemPosition(pos);
                            cartDataSource.deleteCartItem(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(Integer integer) {
                                            cartAdapter.notifyItemRemoved(pos);
                                            sumAllItemInCart(); //Update Total Price
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));   //Update FAB
                                            Toast.makeText(getContext(), "X??a s???n ph???m th??nh c??ng!", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }

                ));

                buf.add(new MyButton(getContext(), "C???p nh???t", 30, 0, Color.parseColor("#5D4037"),
                        pos -> {
                            CartItem cartItem = cartAdapter.getItemPosition(pos);
                            FirebaseDatabase.getInstance(Common.URL)
                                    .getReference(Common.MILKTEA_REF)
                                    .child(Common.currentMilktea.getUid())
                                    .child(Common.CATEGORY_REF)
                                    .child(cartItem.getCategoryId())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                CategoryModel categoryModel = dataSnapshot.getValue(CategoryModel.class);
                                                searchFoodCallbackListener.onSearchCategoryFound(categoryModel, cartItem);

                                            } else {
                                                searchFoodCallbackListener.onSearchCategoryNotfound("Kh??ng t??m th???y s???n ph???m");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            searchFoodCallbackListener.onSearchCategoryNotfound(databaseError.getMessage());
                                        }
                                    });
                        }

                ));
            }
        };

        sumAllItemInCart();

        //Addon
//        addonBottomSheetDialog = new BottomSheetDialog(getContext(), R.style.DialogStyle);
//        View layout_addon_display = getLayoutInflater().inflate(R.layout.layout_addon_display, null);
//        chip_group_addon = (ChipGroup) layout_addon_display.findViewById(R.id.chip_group_addon);
//        edt_search = (EditText) layout_addon_display.findViewById(R.id.edt_search);
//        addonBottomSheetDialog.setContentView(layout_addon_display);
//        addonBottomSheetDialog.setOnDismissListener(dialogInterface -> {
//            //displayUserSelectedAddon(chip_group_user_selected_addon);
//            calculateTotalPrice();
//        });
    }

//    private void displayUserSelectedAddon(ChipGroup chip_group_user_selected_addon) {
//        if (Common.selectedFood.getUserSelectedAddon() != null &&
//                Common.selectedFood.getUserSelectedAddon().size() > 0) {
//            chip_group_user_selected_addon.removeAllViews();
//            for (SugarModel sugarModel : Common.selectedFood.getUserSelectedAddon()) {
//                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon, null);
//                chip.setText(new StringBuilder(sugarModel.getName()).append("(+$")
//                        .append(sugarModel.getPrice()).append(")"));
//
////                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
////                    if (isChecked) {
////                        if (Common.selectedFood.getUserSelectedAddon() == null)
////                            Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
////                        Common.selectedFood.getUserSelectedAddon().add(sugarModel);
////                    }
////                });
//
//                chip_group_user_selected_addon.addView(chip);
//            }
//        } else
//            chip_group_user_selected_addon.removeAllViews();
//    }

    private void initPlacesClient() {
        Places.initialize(getContext(), getString(R.string.google_maps_key));
        placesClient = Places.createClient(getContext());
    }

    private void sumAllItemInCart() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid(),
                Common.currentMilktea.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double aDouble) {
                        if (Common.discountApply != null)
                        {
                            aDouble = aDouble - (aDouble * Common.discountApply.getPercent()/100);
                            txt_total_price.setText(new StringBuilder("T???ng ti???n: ").append(aDouble)
                            .append("(-")
                            .append(Common.discountApply.getPercent())
                            .append("%)"));
                        }
                        else {
                            txt_total_price.setText(new StringBuilder("T???ng ti???n: ").append(aDouble));
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!e.getMessage().contains("Query returned empty"))
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.action_settings).setVisible(false); //Hide Home menu already inflate
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.cart_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear_cart) {
            cartDataSource.cleanCart(Common.currentUser.getUid(),
                    Common.currentMilktea.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            Toast.makeText(getContext(), "Clear Cart Success!", Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().postSticky(new CounterCartEvent(true));   //Update FAB
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().removeAllStickyEvents();
        EventBus.getDefault().postSticky(new HideFABCart(false));
        EventBus.getDefault().postSticky(new CounterCartEvent(false));
        cartViewModel.onStop();
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateItemCartEvent(UpdateItemInCart event) {
        if (event.getCartItem() != null) {
            //First, Save State of Recycler View
            recyclerViewState = recycler_cart.getLayoutManager().onSaveInstanceState();

            cartDataSource.updateCartItem(event.getCartItem())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            calculateTotalPrice();
                            recycler_cart.getLayoutManager().onRestoreInstanceState(recyclerViewState); //Fix Error Refresh Recycler after Update
                        }

                        @Override
                        public void onError(Throwable e) {
                            //dang bi hien cho nay
                            //Toast.makeText(getContext(), "[UDPATE CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    private void calculateTotalPrice() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid(),
                Common.currentMilktea.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double price) {
                        txt_total_price.setText(new StringBuilder("T???ng ti???n : ")
                                .append(Common.formatPrice(price)));
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!e.getMessage().contains("Query returned empty result set"))
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }

    @Override
    public void onLoadTimeSuccess(OrderModel order, long estimateTimeInMs) {
        order.setCreateDate(estimateTimeInMs);
        order.setOrderStatus(0);
        writeOrderToFirebase(order);
    }

    @Override
    public void onLoadOnlyTimeSuccess(long estimateTimeInMs) {
        //Do nothing
    }

    @Override
    public void onLoadTimeFailed(String message) {
        Toast.makeText(getContext(), "" + message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onSearchCategoryFound(CategoryModel categoryModel, CartItem cartItem) {
        FoodModel foodModel = Common.findFoodInListById(categoryModel, cartItem.getFoodId());
        if (foodModel != null) {
            showUpdateDialog(cartItem, foodModel);
        } else
            Toast.makeText(getContext(), "Food Id not found", Toast.LENGTH_SHORT).show();
    }

    private void showUpdateDialog(CartItem cartItem, FoodModel foodModel) {
        Common.selectedFood = foodModel;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_dialog_update_cart, null);
        builder.setView(itemView);

        //View
        Button btn_ok = (Button) itemView.findViewById(R.id.btn_ok);
        Button btn_cancel = (Button) itemView.findViewById(R.id.btn_cancel);

        RadioGroup rdi_group_size = (RadioGroup) itemView.findViewById(R.id.rdi_group_size);
        RadioGroup rdi_group_sugar = (RadioGroup) itemView.findViewById(R.id.rdi_group_sugar);
//        chip_group_user_selected_addon = (ChipGroup) itemView.findViewById(R.id.chip_group_user_selected_addon);
//
//        ImageView img_add_on = (ImageView) itemView.findViewById(R.id.img_add_addon);
//        img_add_on.setOnClickListener(view -> {
//            if (foodModel.getSugar() != null) {
//                displayAddonList();
//                addonBottomSheetDialog.show();
//            }
//        });

        //Size
        if (foodModel.getSize() != null) {
            for (SizeModel sizeModel : foodModel.getSize()) {
                RadioButton radioButton = new RadioButton(getContext());
                radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked)
                            Common.selectedFood.setUserSelectedSize(sizeModel);
                        calculateTotalPrice();  //Update Price
                    }
                });

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
                radioButton.setLayoutParams(params);
                radioButton.setText(sizeModel.getName());
                radioButton.setTag(sizeModel.getPrice());

                rdi_group_size.addView(radioButton);
            }

            if (rdi_group_size.getChildCount() > 0) {
                RadioButton radioButton = (RadioButton) rdi_group_size.getChildAt(0);
                radioButton.setChecked(true); //Default First Selected
            }
        }

        //Sugar
        if (foodModel.getSugar() != null) {
            for (SugarModel sugarModel : foodModel.getSugar()) {
                RadioButton radioButton = new RadioButton(getContext());
                radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked)
                            Common.selectedFood.setUserSelectedAddon(sugarModel);
                        calculateTotalPrice();  //Update Price
                    }
                });

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
                radioButton.setLayoutParams(params);
                radioButton.setText(sugarModel.getName());
                radioButton.setTag(sugarModel.getPrice());

                rdi_group_sugar.addView(radioButton);
            }

            if (rdi_group_sugar.getChildCount() > 0) {
                RadioButton radioButton = (RadioButton) rdi_group_sugar.getChildAt(0);
                radioButton.setChecked(true); //Default First Selected
            }
        }

        //Addon
//        displayAlreadySelectedAddon(chip_group_user_selected_addon, cartItem);

        //Show Dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        //Custom dialog
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

        //Event
        btn_ok.setOnClickListener(v -> {
            //First, Delete Item in Cart
            cartDataSource.deleteCartItem(cartItem)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            //AFter that, Update information and add new
                            //Update price and ifo
                            if (Common.selectedFood.getUserSelectedAddon() != null)
                                cartItem.setFoodAddOn(new Gson().toJson(Common.selectedFood.getUserSelectedAddon()));
                            else
                                cartItem.setFoodAddOn(" B??nh th?????ng");

                            if (Common.selectedFood.getUserSelectedSize() != null)
                                cartItem.setFoodSize(new Gson().toJson(Common.selectedFood.getUserSelectedSize()));
                            else
                                cartItem.setFoodAddOn(" B??nh th?????ng");


//                            cartItem.setFoodExtraPrice(Common.calculateExtraPrice(Common.selectedFood.getUserSelectedSize(),
//                                    Common.selectedFood.getUserSelectedAddon()));

                            //Insert New
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                        calculateTotalPrice();
                                        dialog.dismiss();
                                        Toast.makeText(getContext(), "Update Cart Success!", Toast.LENGTH_SHORT).show();
                                    }, throwable -> {
                                        Toast.makeText(getContext(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    })
                            );


                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btn_cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });


    }

//    private void displayAlreadySelectedAddon(ChipGroup chip_group_user_selected_addon, CartItem cartItem) {
//        //This function will display all addon we already seelcted before addtocart and display on layout
//        if (cartItem.getFoodAddOn() != null && !cartItem.getFoodAddOn().equals("Default")) {
//            List<SugarModel> sugarModels = new Gson().fromJson(cartItem.getFoodAddOn(), new TypeToken<List<SugarModel>>() {
//            }.getType());
//
////            Common.selectedFood.setUserSelectedAddon(sugarModels);
//
//            chip_group_user_selected_addon.removeAllViews();
//
//            for (SugarModel sugarModel : sugarModels) {
//                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon, null);
//                chip.setText(new StringBuilder(sugarModel.getName()).append("(+???")
//                        .append(sugarModel.getPrice()).append(")"));
//
//                chip.setClickable(false);
//                chip.setOnCloseIconClickListener(v -> {
//                    //Remove when user click Deelete
//                    chip_group_user_selected_addon.removeView(v);
//                    Common.selectedFood.getUserSelectedAddon().remove(sugarModel);
//                    calculateTotalPrice();
//                });
//
//                chip_group_user_selected_addon.addView(chip);
//
//            }
//
//        }
//    }

//    private void displayAddonList() {
//        if (Common.selectedFood.getSugar() != null && Common.selectedFood.getSugar().size() > 0) {
//            chip_group_addon.clearCheck();
//            chip_group_addon.removeAllViews();
//
//            edt_search.addTextChangedListener(this);
//
//            for (SugarModel sugarModel : Common.selectedFood.getSugar()) {
//                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_addon_item, null);
//                chip.setText(new StringBuilder(sugarModel.getName()).append("(+???")
//                        .append(sugarModel.getPrice()).append(")"));
//
////                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
////                    if (isChecked) {
////                        if (Common.selectedFood.getUserSelectedAddon() == null)
////                            Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
////                        Common.selectedFood.getUserSelectedAddon().add(sugarModel);
////                    }
////                });
//
//                chip_group_addon.addView(chip);
//
//            }
//
//        }
//    }

    @Override
    public void onSearchCategoryNotfound(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

//    @Override
//    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//    }
//
//    @Override
//    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
//        chip_group_addon.clearCheck();
//        chip_group_addon.removeAllViews();
//
//        for (SugarModel sugarModel : Common.selectedFood.getSugar()) {
//            if (sugarModel.getName().toLowerCase().contains(charSequence.toString().toLowerCase())) {
//                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_addon_item, null);
//                chip.setText(new StringBuilder(sugarModel.getName()).append("(+???")
//                        .append(sugarModel.getPrice()).append(")"));

//                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
//                     if (isChecked) {
//                        if (Common.selectedFood.getUserSelectedAddon() == null)
//                            Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
//                        Common.selectedFood.getUserSelectedAddon().add(sugarModel);
//                    }
//                });
//
//                chip_group_addon.addView(chip);
//            }
//        }
//
//    }
//
//    @Override
//    public void afterTextChanged(Editable s) {
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_QR_PERMISSION)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                edt_discount_code.setText(data.getStringExtra(Common.QR_CODE_TAG).toLowerCase());
            }
        }
    }
}