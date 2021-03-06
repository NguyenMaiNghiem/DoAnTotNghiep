package com.nghiem.rilleyClient.ui.fooddetail;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.nghiem.rilleyClient.Common.Common;
import com.nghiem.rilleyClient.Database.CartDataSource;
import com.nghiem.rilleyClient.Database.CartDatabase;
import com.nghiem.rilleyClient.Database.CartItem;
import com.nghiem.rilleyClient.Database.LocalCartDataSource;
import com.nghiem.rilleyClient.EventBus.CounterCartEvent;
import com.nghiem.rilleyClient.EventBus.MenuItemBack;
import com.nghiem.rilleyClient.Model.CommentModel;
import com.nghiem.rilleyClient.Model.FoodModel;
import com.nghiem.rilleyClient.Model.SizeModel;
import com.nghiem.rilleyClient.Model.SugarModel;
import com.nghiem.rilleyClient.R;
import com.nghiem.rilleyClient.ui.comments.CommentFragment;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FoodDetailFragment extends Fragment {

    android.app.AlertDialog waitingDialog;
    //View Need Inflate
    ChipGroup chip_group_addon;
    EditText edt_search;
    @BindView(R.id.img_food)
    ImageView img_food;
    @BindView(R.id.btnCart)
    CounterFab btnCart;
    @BindView(R.id.btn_rating)
    FloatingActionButton btn_rating;
    @BindView(R.id.food_name)
    TextView food_name;
    @BindView(R.id.food_description)
    TextView food_description;
    @BindView(R.id.food_price)
    TextView food_price;
    @BindView(R.id.number_button)
    ElegantNumberButton number_button;
    @BindView(R.id.ratingBar)
    RatingBar ratingBar;
    @BindView(R.id.btnShowComment)
    Button btnShowComment;
    @BindView(R.id.rdi_group_size)
    RadioGroup rdi_group_size;
    @BindView(R.id.rdi_group_sugar)
    RadioGroup rdi_group_sugar;
//    @BindView(R.id.img_add_on)
//    ImageView img_add_on;
//    @BindView(R.id.chip_group_user_selected_addon)
//    ChipGroup chip_group_user_selected_addon;
    private CartDataSource cartDataSource;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private FoodDetailViewModel foodDetailViewModel;
    private BottomSheetDialog addonBottomSheetDialog;
    private Unbinder unbinder;

    @OnClick(R.id.btn_rating)
    void onRatingButtonClick() {
        showDialogRating();
    }

    @OnClick(R.id.btnShowComment)
    void onShowCommentButtonClick() {
        CommentFragment commentFragment = CommentFragment.getInstance();
        commentFragment.show(getActivity().getSupportFragmentManager(), "CommentFragment");
    }

//    @OnClick(R.id.img_add_on)
//    void onAddonClick()
//    {
//        if(Common.selectedFood.getSugar() != null) {
//            displayAddOnList(); //Show All AddonOption
//            addonBottomSheetDialog.show();
//        }
//
//    }

    @OnClick(R.id.btnCart)
    void onCartItemAdd() {
        CartItem cartItem = new CartItem();
        cartItem.setMilkteaId(Common.currentMilktea.getUid());
        cartItem.setUid(Common.currentUser.getUid());
        cartItem.setUserPhone(Common.currentUser.getPhone());
        cartItem.setCategoryId(Common.categorySelected.getMenu_id());
        cartItem.setFoodId(Common.selectedFood.getId());
        cartItem.setFoodName(Common.selectedFood.getName());
        cartItem.setFoodImage(Common.selectedFood.getImage());
        cartItem.setFoodPrice(Double.valueOf(String.valueOf(Common.selectedFood.getPrice())));
        cartItem.setFoodQuantity(Integer.valueOf(number_button.getNumber()));
        //cartItem.setFoodExtraPrice(Common.calculateExtraPrice(Common.selectedFood.getUserSelectedSize(), Common.selectedFood.getUserSelectedAddon())); //Because Default we do not choose Size + Addon
        cartItem.setFoodExtraPrice(Common.calculateExtraPrice(Common.selectedFood.getUserSelectedSize(), Common.selectedFood.getUserSelectedAddon())); //Because Default we do not choose Size + Addon
        if (Common.selectedFood.getUserSelectedAddon() != null)
            cartItem.setFoodAddOn(new Gson().toJson(Common.selectedFood.getUserSelectedAddon()));
        else
            cartItem.setFoodAddOn(" B??nh th?????ng");

        if (Common.selectedFood.getUserSelectedSize() != null)
            cartItem.setFoodSize(new Gson().toJson(Common.selectedFood.getUserSelectedSize()));
        else
            cartItem.setFoodSize(" B??nh th?????ng");

        cartDataSource.getItemWithAllOptionsInCart(Common.currentUser.getUid(),
                Common.categorySelected.getMenu_id(),
                cartItem.getFoodId(),
                cartItem.getFoodSize(),
                cartItem.getFoodAddOn(),
                Common.currentMilktea.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<CartItem>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(CartItem cartItemFromDB) {
                        if (cartItemFromDB.equals(cartItem)) {
                            //Already in Database, Just Update
                            cartItemFromDB.setFoodExtraPrice(cartItem.getFoodExtraPrice());
                            cartItemFromDB.setFoodAddOn(cartItem.getFoodAddOn());
                            cartItemFromDB.setFoodSize(cartItem.getFoodSize());
                            cartItemFromDB.setFoodQuantity(cartItemFromDB.getFoodQuantity() + cartItem.getFoodQuantity());

                            cartDataSource.updateCartItem(cartItemFromDB)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(Integer integer) {
                                            Toast.makeText(getContext(), "C???p nh???t gi??? h??ng th??nh c??ng!", Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Toast.makeText(getContext(), "[UPDATE CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            //Item not available in Cart before, Insert new
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                        Toast.makeText(getContext(), "S???n ph???m ???? ???????c th??m v??o gi??? h??ng!", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    }, throwable -> {
                                        Toast.makeText(getContext(), "[Cart Error]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e.getMessage().contains("empty")) {
                            //Default this code will fired
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                        Toast.makeText(getContext(), "S???n ph???m ???? ???????c th??m v??o gi??? h??ng!", Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                    }, throwable -> {
                                        Toast.makeText(getContext(), "[Cart Error]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }));
                        }

//                        Toast.makeText(getContext(), "[GET CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }


//    private void displayAddOnList() {
//        if(Common.selectedFood.getSugar().size() > 0)
//        {
//            chip_group_addon.clearCheck();
//            chip_group_addon.removeAllViews();
//
//            edt_search.addTextChangedListener(this);
//
//            //Add on All Views
//            for(SugarModel sugarModel :  Common.selectedFood.getSugar())
//            {
//                Chip chip = (Chip)getLayoutInflater().inflate(R.layout.layout_addon_item, null);
//                chip.setText(new StringBuilder(sugarModel.getName()).append("(+$")
//                        .append(sugarModel.getPrice()).append(")"));
//
//                chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        if(isChecked)
//                        {
//                            if(Common.selectedFood.getUserSelectedAddon() == null)
//                                Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
//                            Common.selectedFood.getUserSelectedAddon().add(sugarModel);
//                        }
//                    }
//                });
//
//                chip_group_addon.addView(chip);
//
//            }
//        }
//    }


    private void showDialogRating() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("????nh gi?? s???n ph???m");
        builder.setMessage("Vui l??ng ??i???n ?????y ????? th??ng tin");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_rating, null);

        RatingBar ratingBar = (RatingBar) itemView.findViewById(R.id.rating_bar);
        EditText edt_comment = (EditText) itemView.findViewById(R.id.edt_comment);

        builder.setView(itemView);
        builder.setNegativeButton("H???y", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CommentModel commentModel = new CommentModel();
                commentModel.setName(Common.currentUser.getName());
                commentModel.setUid(Common.currentUser.getUid());
                commentModel.setComment(edt_comment.getText().toString());
                commentModel.setRatingValue(ratingBar.getRating());

                //Time Stamp
                Map<String, Object> serverTimeStamp = new HashMap<>();
                serverTimeStamp.put("timeStamp", ServerValue.TIMESTAMP);
                commentModel.setCommentTimeStamp(serverTimeStamp);

                foodDetailViewModel.setCommentModel(commentModel);

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }


    @SuppressLint("FragmentLiveDataObserve")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodDetailViewModel =
                ViewModelProviders.of(this).get(FoodDetailViewModel.class);
        View root = inflater.inflate(R.layout.fragment_food_detail, container, false);

        unbinder = ButterKnife.bind(this, root);

        //Code Added - Manually
        number_button.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                calculateTotalPrice();
            }
        });
        //Code - End

        initViews();

        foodDetailViewModel.getMutableLiveDataFood().observe(this, foodModel -> {
            displayInfo(foodModel);
        });
        foodDetailViewModel.getMutableLiveDataComment().observe(this, commentModel -> {
            submitRatingToFirebase(commentModel);
        });

        return root;
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    private void initViews() {

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        waitingDialog = new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();

        addonBottomSheetDialog = new BottomSheetDialog(getContext(), R.style.DialogStyle);
        View layout_addon_display = getLayoutInflater().inflate(R.layout.layout_addon_display, null);
        chip_group_addon = (ChipGroup) layout_addon_display.findViewById(R.id.chip_group_addon);
        edt_search = (EditText) layout_addon_display.findViewById(R.id.edt_search);

        addonBottomSheetDialog.setContentView(layout_addon_display);

        addonBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //displayUserSelectedAddon();
                calculateTotalPrice();
            }
        });
    }

//    private void displayUserSelectedAddon() {
//        if (Common.selectedFood.getUserSelectedAddon() != null &&
//                Common.selectedFood.getUserSelectedAddon().size() > 0) {
//            chip_group_user_selected_addon.removeAllViews();
//            for (SugarModel sugarModel : Common.selectedFood.getUserSelectedAddon()) {
//                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon, null);
//                chip.setText(new StringBuilder(sugarModel.getName()).append("(+$")
//                        .append(sugarModel.getPrice()).append(")"));
//
//                chip.setClickable(false);
//                chip.setOnCloseIconClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        //Remove when User Click Delete
//                        chip_group_user_selected_addon.removeView(view);
//                        Common.selectedFood.getUserSelectedAddon().remove(sugarModel);
//                        calculateTotalPrice();
//                    }
//                });
//
//                chip_group_user_selected_addon.addView(chip);
//            }
//        } else
//            chip_group_user_selected_addon.removeAllViews();
//
//    }

    private void submitRatingToFirebase(CommentModel commentModel) {

        waitingDialog.show();

        //First, We will submit to Comment Ref.
        FirebaseDatabase.getInstance(Common.URL)
                .getReference(Common.MILKTEA_REF)
                .child(Common.currentMilktea.getUid())
                .child(Common.COMMENT_REF)
                .child(Common.selectedFood.getId())
                .push()
                .setValue(commentModel)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //After Submit Rating to CommentRef, We will udpate value Average in Food
                            addRatingToFood(commentModel.getRatingValue());
                        }
                        waitingDialog.dismiss();
                    }
                });
    }

    private void addRatingToFood(float ratingValue) {
        FirebaseDatabase.getInstance(Common.URL)
                .getReference(Common.MILKTEA_REF)
                .child(Common.currentMilktea.getUid())
                .child(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id()) //Select Category
                .child("foods") //Select Arrya list of category
                .child(Common.selectedFood.getKey()) //Because food item is array list and key is index of array
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            FoodModel foodModel = dataSnapshot.getValue(FoodModel.class);
                            foodModel.setKey(Common.selectedFood.getKey()); //Set Key

                            //Apply Rating
                            if (foodModel.getRatingValue() == null)
                                foodModel.setRatingValue(0d);

                            if (foodModel.getRatingCount() == null)
                                foodModel.setRatingCount(0l);

                            double sumRating = foodModel.getRatingValue() + ratingValue;
                            long ratingCount = foodModel.getRatingCount() + 1;

                            double result = sumRating / ratingCount;

                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("ratingValue", sumRating);
                            updateData.put("ratingCount", ratingCount);

                            //Update Food Model
                            foodModel.setRatingValue(sumRating);
                            foodModel.setRatingCount(ratingCount);

                            dataSnapshot.getRef()
                                    .updateChildren(updateData)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getContext(), "Thank you!", Toast.LENGTH_SHORT).show();
                                                Common.selectedFood = foodModel;
                                                foodDetailViewModel.setFoodModel(foodModel);
                                            }
                                        }
                                    });

                        } else {
                            waitingDialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        waitingDialog.dismiss();
                        Toast.makeText(getContext(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void displayInfo(FoodModel foodModel) {
        Glide.with(getContext()).load(foodModel.getImage()).into(img_food);
        food_name.setText(new StringBuilder(foodModel.getName()));
        food_description.setText(new StringBuilder(foodModel.getDescription()));
        food_price.setText(new StringBuilder(foodModel.getPrice().toString()));

        if (foodModel.getRatingValue() != null)
            ratingBar.setRating(foodModel.getRatingValue().floatValue() / foodModel.getRatingCount());

        ((AppCompatActivity) getActivity())
                .getSupportActionBar()
                .setTitle(Common.selectedFood.getName());

        //Size
        if (Common.selectedFood.getSize() != null) {
            for (SizeModel sizeModel : Common.selectedFood.getSize()) {
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
        }

        //Sugar
        if (Common.selectedFood.getSugar() != null) {
            for (SugarModel sugarModel : Common.selectedFood.getSugar()) {
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
        }


        if (rdi_group_size.getChildCount() > 0) {
            RadioButton radioButton = (RadioButton) rdi_group_size.getChildAt(0);
            radioButton.setChecked(true); //Default First Selected
        }

        if (rdi_group_sugar.getChildCount() > 0) {
            RadioButton radioButton = (RadioButton) rdi_group_sugar.getChildAt(0);
            radioButton.setChecked(true); //Default First Selected
        }

        calculateTotalPrice();

    }

    private void calculateTotalPrice() {
        double totalPrice = Double.parseDouble(Common.selectedFood.getPrice().toString()), displayPrice = 0.0;

        //Addon
//        if(Common.selectedFood.getUserSelectedAddon() != null && Common.selectedFood.getUserSelectedAddon().size() > 0)
//            for(SugarModel sugarModel : Common.selectedFood.getUserSelectedAddon())
//                totalPrice += Double.parseDouble(sugarModel.getPrice().toString());
        if (Common.selectedFood.getUserSelectedAddon() != null)
            totalPrice += Double.parseDouble(Common.selectedFood.getUserSelectedAddon().getPrice().toString());


        //Size
        if (Common.selectedFood.getUserSelectedSize() != null)
            totalPrice += Double.parseDouble(Common.selectedFood.getUserSelectedSize().getPrice().toString());

        displayPrice = totalPrice * (Integer.parseInt(number_button.getNumber()));
        displayPrice = Math.round(displayPrice * 100.0 / 100.0);

        food_price.setText(""+displayPrice);

//        food_price.setText(new StringBuilder("").append(Common.formatPrice(displayPrice)).toString());

    }

//    @Override
//    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//        //Nothing
//    }

//    @Override
//    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
//        chip_group_addon.clearCheck();
//        chip_group_addon.removeAllViews();
//
//        for(SugarModel sugarModel :  Common.selectedFood.getSugar())
//        {
//            if(sugarModel.getName().toLowerCase().contains(charSequence.toString().toLowerCase()))
//            {
//                Chip chip = (Chip)getLayoutInflater().inflate(R.layout.layout_addon_item, null);
//                chip.setText(new StringBuilder(sugarModel.getName()).append("(+$")
//                        .append(sugarModel.getPrice()).append(")"));
//
//                chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        if(isChecked)
//                        {
//                            if(Common.selectedFood.getUserSelectedAddon() == null)
//                                Common.selectedFood.setUserSelectedAddon(new ArrayList<>());
//                            Common.selectedFood.getUserSelectedAddon().add(sugarModel);
//                        }
//                    }
//                });
//
//                chip_group_addon.addView(chip);
//            }
//        }
//    }

//    @Override
//    public void afterTextChanged(Editable s) {
//
//    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}