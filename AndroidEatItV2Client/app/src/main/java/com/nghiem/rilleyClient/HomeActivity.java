package com.nghiem.rilleyClient;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.andremion.counterfab.CounterFab;
import com.nghiem.rilleyClient.EventBus.MenuInflateEvent;
import com.nghiem.rilleyClient.EventBus.MenuItemEvent;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nghiem.rilleyClient.Common.Common;
import com.nghiem.rilleyClient.Database.CartDataSource;
import com.nghiem.rilleyClient.Database.CartDatabase;
import com.nghiem.rilleyClient.Database.LocalCartDataSource;
import com.nghiem.rilleyClient.EventBus.BestDealItemClick;
import com.nghiem.rilleyClient.EventBus.CategoryClick;
import com.nghiem.rilleyClient.EventBus.CounterCartEvent;
import com.nghiem.rilleyClient.EventBus.FoodItemClick;
import com.nghiem.rilleyClient.EventBus.HideFABCart;
import com.nghiem.rilleyClient.EventBus.MenuItemBack;
import com.nghiem.rilleyClient.EventBus.PopularCategoryClick;
import com.nghiem.rilleyClient.Model.CategoryModel;
import com.nghiem.rilleyClient.Model.FoodModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavController navController;
    private NavigationView navigationView;

    private CartDataSource cartDataSource;

    private Place placeSelected;
    private AutocompleteSupportFragment places_fragment;
    private PlacesClient placesClient;
    private List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

    android.app.AlertDialog dialog;

    int menuClick = -1;

    @BindView(R.id.fab)
    CounterFab fab;
    @BindView(R.id.fab_chat)
    CounterFab fab_chat;

    @Override
    protected void onResume() {
        super.onResume();
        //countCartItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initPlacesClient();

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        ButterKnife.bind(this);

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            navController.navigate(R.id.nav_cart);
        });
        fab_chat.setOnClickListener(v -> {
            startActivity(new Intent(this,ChatActivity.class));
        });
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_milktea,
                R.id.nav_home, R.id.nav_menu, R.id.nav_food_detail,
                R.id.nav_view_orders, R.id.nav_cart, R.id.nav_food_list)
                .setDrawerLayout(drawer)
                .build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront(); //Fixed

        View headerView = navigationView.getHeaderView(0);
        TextView txt_user = (TextView) headerView.findViewById(R.id.txt_user);
        Common.setSpanString("Hey, ", Common.currentUser.getName(), txt_user);

        //Hide FAB because in Restaurant list , we can't show cart
        EventBus.getDefault().postSticky(new HideFABCart(true));
        //countCartItem();

        menuClick = R.id.nav_home;  //Default

        checkIsOpenFromActivity();
    }

    private void checkIsOpenFromActivity() {
        boolean isOpenFromNewOrder = getIntent().getBooleanExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER, false);
        if (isOpenFromNewOrder) {
            navController.popBackStack();
            navController.navigate(R.id.nav_view_orders);
            menuClick = R.id.nav_view_orders;
        }
    }

    private void initPlacesClient() {
        Places.initialize(this, getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        //Called when Click on ==
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        menuItem.setChecked(true);
        drawer.closeDrawers();
        switch (menuItem.getItemId()) {
            case R.id.nav_milktea:
                if (menuItem.getItemId() != menuClick)
                    navController.navigate(R.id.nav_milktea);
                    EventBus.getDefault().postSticky(new MenuInflateEvent(true));
                break;
            case R.id.nav_home:
                if (menuItem.getItemId() != menuClick)
                {
                    //Display detail menu of restaurant
                    navController.navigate(R.id.nav_home);
                    EventBus.getDefault().postSticky(new MenuInflateEvent(true));
                }
                break;
            case R.id.nav_menu:
                if (menuItem.getItemId() != menuClick) {
                    navController.navigate(R.id.nav_menu);
                    EventBus.getDefault().postSticky(new MenuInflateEvent(true));
                }
                break;
            case R.id.nav_cart:
                if (menuItem.getItemId() != menuClick) {
                    navController.navigate(R.id.nav_cart);
                    EventBus.getDefault().postSticky(new MenuInflateEvent(true));
                }
                break;
            case R.id.nav_view_orders:
                if (menuItem.getItemId() != menuClick){
                    navController.navigate(R.id.nav_view_orders);
                    EventBus.getDefault().postSticky(new MenuInflateEvent(true));
                }
                break;
            case R.id.nav_update_info:
                showUpdateInfoDialog();
                break;
            case R.id.nav_news:
                showSubscribeNews();
                break;
            case R.id.nav_sign_out:
                signOut();
                break;


        }
        menuClick = menuItem.getItemId();
        return true;
    }

    private void showSubscribeNews() {

        Paper.init(this);

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Th??ng B??o");
        builder.setMessage("B???n c?? mu???n ????ng k?? ????? nh???n nh???ng th??ng b??o v??? c??c m?? gi???m gi?? t??? c???a h??ng kh??ng?");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_subscribe_news, null);
        CheckBox ckb_news = (CheckBox)itemView.findViewById(R.id.ckb_subscribe_news);
        boolean isSubscribeNews = Paper.book().read(Common.currentMilktea.getUid(), false);

        if(isSubscribeNews)
            ckb_news.setChecked(true);

        builder.setNegativeButton("H???y", (dialogInterface, which) -> {
            dialogInterface.dismiss();
        }).setPositiveButton("G???i", (dialogInterface, which) -> {
            if(ckb_news.isChecked())
            {
                Paper.book().write(Common.currentMilktea.getUid(), true);
                FirebaseMessaging.getInstance()
                        .subscribeToTopic(Common.createTopicNews())
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        })
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Subscribe Successuly!", Toast.LENGTH_SHORT).show();
                        });
            }
            else
            {
                Paper.book().delete(Common.currentMilktea.getUid());
                FirebaseMessaging.getInstance()
                        .unsubscribeFromTopic(Common.createTopicNews())
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        })
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Unsubscribe Successuly!", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        builder.setView(itemView);

        //Show Dialog
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void showUpdateInfoDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("C???p nh???t th??ng tin");
        builder.setMessage("Vui l??ng ??i???n ?????y ????? th??ng tin");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null);
        EditText edt_name = (EditText) itemView.findViewById(R.id.edt_name);
        EditText edt_address = (EditText) itemView.findViewById(R.id.edt_address);
        EditText edt_phone = (EditText) itemView.findViewById(R.id.edt_phone);

        places_fragment = (AutocompleteSupportFragment) getSupportFragmentManager()
                .findFragmentById(R.id.places_autocomplete_fragment);
        places_fragment.setPlaceFields(placeFields);
        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                placeSelected = place;
                edt_address.setText(place.getAddress());
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(HomeActivity.this, "" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //Set Data
        edt_phone.setText(Common.currentUser.getPhone());
        edt_name.setText(Common.currentUser.getName());
        edt_address.setText(Common.currentUser.getAddress());

        builder.setNegativeButton("H???y", (dialogInterface, which) -> dialog.dismiss());
        builder.setPositiveButton("C???p nh???t", (dialogInterface, which) -> {
            if (placeSelected != null) {
                if (TextUtils.isEmpty(edt_name.getText().toString())) {
                    Toast.makeText(HomeActivity.this, "Vui l??ng nh???p t??n", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> update_data = new HashMap<>();
                update_data.put("name", edt_name.getText().toString());
                update_data.put("address", edt_address.getText().toString());
                update_data.put("lat", placeSelected.getLatLng().latitude);
                update_data.put("lng", placeSelected.getLatLng().longitude);

                FirebaseDatabase.getInstance(Common.URL)
                        .getReference(Common.USER_REFERENCES)
                        .child(Common.currentUser.getUid())
                        .updateChildren(update_data)
                        .addOnFailureListener(e -> {
                            dialogInterface.dismiss();
                            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        })
                        .addOnSuccessListener(aVoid -> {
                            dialogInterface.dismiss();
                            Toast.makeText(this, "C???p nh???t th??ng tin th??nh c??ng", Toast.LENGTH_SHORT).show();
                            Common.currentUser.setName(update_data.get("name").toString());
                            Common.currentUser.setAddress(update_data.get("address").toString());
                            Common.currentUser.setLat(Double.parseDouble(update_data.get("lat").toString()));
                            Common.currentUser.setLng(Double.parseDouble(update_data.get("lng").toString()));
                        });


            } else {
                Toast.makeText(this, "Please Select Address!", Toast.LENGTH_SHORT).show();
            }

        });

        builder.setView(itemView);

        //Show Dialog
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(dialogInterface -> {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.remove(places_fragment);
            fragmentTransaction.commit();
        });
        dialog.show();
    }

    private void signOut() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("????ng Xu???t");
        builder.setMessage("B???n c?? mu???n ????ng xu???t kh??ng ?");

        builder.setNegativeButton("H???y", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("OK", (dialog, which) -> {
            Common.selectedFood = null;
            Common.categorySelected = null;
            Common.currentUser = null;

            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        //Show Dialog
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    //EventBus

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().removeAllStickyEvents(); //Fix eventbus always called after OnaCtivity result.
        EventBus.getDefault().unregister(this);
        //compositeDisposable.clear();
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCategorySelected(CategoryClick event) {
        if (event.isSuccess()) {
            navController.navigate(R.id.nav_food_list);
            //Toast.makeText(this, "Click To "+event.getCategoryModel().getName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onFoodItemClick(FoodItemClick event) {
        if (event.isSuccess()) {
            navController.navigate(R.id.nav_food_detail);
//            Toast.makeText(this, "Click To "+event.getFoodModel().getName(), Toast.LENGTH_SHORT).show();

        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCartCounter(CounterCartEvent event) {
//        if (event.isSuccess()) {
//            countCartItem();
//        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPopularItemClick(PopularCategoryClick event) {
        if (event.getPopularCategoryModel() != null) {
            dialog.show();

            FirebaseDatabase.getInstance(Common.URL)
                    .getReference(Common.MILKTEA_REF)
                    .child(Common.currentMilktea.getUid())
                    .child(Common.CATEGORY_REF)
                    .child(event.getPopularCategoryModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Common.categorySelected = dataSnapshot.getValue(CategoryModel.class);
                                Common.categorySelected.setMenu_id(dataSnapshot.getKey());

                                //Load Food
                                FirebaseDatabase.getInstance(Common.URL)
                                        .getReference(Common.MILKTEA_REF)
                                        .child(Common.currentMilktea.getUid())
                                        .child(Common.CATEGORY_REF)
                                        .child(event.getPopularCategoryModel().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .equalTo(event.getPopularCategoryModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                                                        Common.selectedFood = itemSnapshot.getValue(FoodModel.class);
                                                        Common.selectedFood.setKey(itemSnapshot.getKey());
                                                    }
                                                    navController.navigate(R.id.nav_food_detail);
                                                } else {

                                                    Toast.makeText(HomeActivity.this, "Item doesn't exist!", Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                dialog.dismiss();
                                                Toast.makeText(HomeActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                dialog.dismiss();
                                Toast.makeText(HomeActivity.this, "Item doesn't exist!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(HomeActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onBestDealItemClick(BestDealItemClick event) {
        if (event.getBestDealModel() != null) {
            dialog.show();

            FirebaseDatabase.getInstance(Common.URL)
                    .getReference(Common.MILKTEA_REF)
                    .child(Common.currentMilktea.getUid())
                    .child(Common.CATEGORY_REF)
                    .child(event.getBestDealModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Common.categorySelected = dataSnapshot.getValue(CategoryModel.class);
                                Common.categorySelected.setMenu_id(dataSnapshot.getKey());

                                //Load Food
                                FirebaseDatabase.getInstance(Common.URL)
                                        .getReference(Common.MILKTEA_REF)
                                        .child(Common.currentMilktea.getUid())
                                        .child(Common.CATEGORY_REF)
                                        .child(event.getBestDealModel().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .equalTo(event.getBestDealModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                                                        Common.selectedFood = itemSnapshot.getValue(FoodModel.class);
                                                        Common.selectedFood.setKey(itemSnapshot.getKey());
                                                    }
                                                    navController.navigate(R.id.nav_food_detail);
                                                } else {

                                                    Toast.makeText(HomeActivity.this, "Item doesn't exist!", Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                dialog.dismiss();
                                                Toast.makeText(HomeActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                dialog.dismiss();
                                Toast.makeText(HomeActivity.this, "Item doesn't exist!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(HomeActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onHideFABEvent(HideFABCart event) {
        if (event.isHidden()) {
            fab.hide();
            fab_chat.hide();
        } else
        {
            fab.show();
            fab_chat.show();
            countCartItem();
        }
    }

    private void countCartItem() {
        cartDataSource.countItemInCart(Common.currentUser.getUid(),
                Common.currentMilktea.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        fab.setCount(integer);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!e.getMessage().contains("Query returned empty"))
                            Toast.makeText(HomeActivity.this, "[COUNT CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        else
                            fab.setCount(0);
                    }
                });
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void countCartAgain(CounterCartEvent event)
    {
        if (event.isSuccess())
            if (Common.currentMilktea != null)
                countCartItem();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMenuItemBack(MenuItemBack event) {
        menuClick = -1;
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMilkteaClick(MenuItemEvent event) {
        Bundle bundle = new Bundle();
        bundle.putString("milktea",event.getMilkTeaModel().getUid());
        navController.navigate(R.id.nav_home,bundle);
        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.milktea_detail_menu);
        EventBus.getDefault().postSticky(new MenuInflateEvent(true));
        EventBus.getDefault().postSticky(new HideFABCart(false));   //Show CART button when user click select restaurant
        countCartItem();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onInflateMenu(MenuInflateEvent event) {
        navigationView.getMenu().clear();
        if (event.isShowDetail())
            navigationView.inflateMenu(R.menu.milktea_detail_menu);
        else
            navigationView.inflateMenu(R.menu.activity_home_drawer);

    }
}
