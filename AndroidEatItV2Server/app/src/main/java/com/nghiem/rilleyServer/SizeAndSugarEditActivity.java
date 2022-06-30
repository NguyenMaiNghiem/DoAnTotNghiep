package com.nghiem.rilleyServer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nghiem.rilleyServer.Adapter.MySugarAdapter;
import com.nghiem.rilleyServer.Adapter.MySizeAdapter;
import com.nghiem.rilleyServer.Common.Common;
import com.nghiem.rilleyServer.EventBus.SugarSizeEditEvent;
import com.nghiem.rilleyServer.EventBus.SelectedAddonModel;
import com.nghiem.rilleyServer.EventBus.SelectedSizeModel;
import com.nghiem.rilleyServer.EventBus.UpdateSugarModel;
import com.nghiem.rilleyServer.EventBus.UpdateSizeModel;
import com.nghiem.rilleyServer.Model.SugarModel;
import com.nghiem.rilleyServer.Model.SizeModel;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SizeAndSugarEditActivity extends AppCompatActivity {

    @BindView(R.id.tooL_bar)
    Toolbar toolbar;
    @BindView(R.id.edt_name)
    EditText edt_name;
    @BindView(R.id.edt_price)
    EditText edt_price;
    @BindView(R.id.btn_create)
    Button btn_create;
    @BindView(R.id.btn_edit)
    Button btn_edit;
    @BindView(R.id.recycler_addon_size)
    RecyclerView recycler_addon_size;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.addon_size_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveData();
                break;
            case android.R.id.home:
            {
                if(needSave)
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Cancel?")
                            .setMessage("Do you really want close without saving ?")
                            .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss())
                            .setPositiveButton("OK", (dialogInterface, i) -> {
                                needSave = false;
                                closeActivity();
                            });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else
                {
                    closeActivity();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveData() {
        if(foodEditPosition!=-1)
        {
            Common.categorySelected.getFoods().set(foodEditPosition, Common.selectedFood); //Save food to Category

            Map<String, Object> updateData = new HashMap<>();
            updateData.put("foods", Common.categorySelected.getFoods());

            FirebaseDatabase.getInstance(Common.URL)
                    .getReference(Common.MILKTEA_REF)
                    .child(Common.currentServerUser.getMilktea())
                    .child(Common.CATEGORY_REF)
                    .child(Common.categorySelected.getMenu_id())
                    .updateChildren(updateData)
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(this, "Reload Success!", Toast.LENGTH_SHORT).show();
                            needSave=false;
                            edt_price.setText("");
                            edt_name.setText("");
                        }
                    });
        }
    }

    private void closeActivity() {
        edt_name.setText("");
        edt_price.setText("");
        finish();
    }

    //Variable
    MySizeAdapter adapter;
    MySugarAdapter mySugarAdapter;

    private int foodEditPosition = -1;
    private boolean needSave = false;
    private boolean isSugar = false;

    //Event
    @OnClick(R.id.btn_create)
    void onCreateNew()
    {
        if (edt_name.getText().toString().isEmpty() || edt_price.getText().toString().isEmpty()){
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_LONG).show();
        }
        else
        {
            if(!isSugar) //Size
            {
                if(adapter!=null)
                {
                    SizeModel sizeModel = new SizeModel();
                    sizeModel.setName(edt_name.getText().toString());
                    sizeModel.setPrice(Long.valueOf(edt_price.getText().toString()));
                    adapter.addNewSize(sizeModel);
                    Toast.makeText(this, "Thêm thành công", Toast.LENGTH_LONG).show();
                }
            }
            else
            {
                if(mySugarAdapter !=null)
                {
                    SugarModel sugarModel = new SugarModel();
                    sugarModel.setName(edt_name.getText().toString());
                    sugarModel.setPrice(Long.valueOf(edt_price.getText().toString()));
                    mySugarAdapter.addNewAddon(sugarModel);
                    Toast.makeText(this, "Thêm thành công", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @OnClick(R.id.btn_edit)
    void onEdit()
    {
        if (edt_name.getText().toString().isEmpty() || edt_price.getText().toString().isEmpty()){
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_LONG).show();
        }
        else
        {
            if(!isSugar) //Size
            {
                if(adapter!=null)
                {
                    SizeModel sizeModel = new SizeModel();
                    sizeModel.setName(edt_name.getText().toString());
                    sizeModel.setPrice(Long.valueOf(edt_price.getText().toString()));
                    adapter.editSize(sizeModel);
                    Toast.makeText(this, "Chỉnh sửa thành công", Toast.LENGTH_LONG).show();
                }
            }
            else
            {
                if(mySugarAdapter !=null)
                {
                    SugarModel sugarModel = new SugarModel();
                    sugarModel.setName(edt_name.getText().toString());
                    sugarModel.setPrice(Long.valueOf(edt_price.getText().toString()));
                    mySugarAdapter.editAddon(sugarModel);
                    Toast.makeText(this, "Chỉnh sửa thành công", Toast.LENGTH_LONG).show();
                }
            }
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_size_addon_edit);

        init();

    }

    private void init() {
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recycler_addon_size.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_addon_size.setLayoutManager(layoutManager);
        recycler_addon_size.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));


    }

    //Register Event


    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().removeStickyEvent(UpdateSizeModel.class);
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onStop();
    }

    //Receive Event
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onAddonSizeReceive(SugarSizeEditEvent event) {
        if (!event.isSugar()) //If Event is Size
        {
            if (Common.selectedFood.getSize() == null) //If Size is empty
                Common.selectedFood.setSize(new ArrayList<>());
                adapter = new MySizeAdapter(this, Common.selectedFood.getSize());
                foodEditPosition = event.getPos(); //Save food edit to Update
                recycler_addon_size.setAdapter(adapter);

                isSugar = event.isSugar();


        }
        else //Is Addon
        {
            if (Common.selectedFood.getSugar()  == null) //If Addon  empty
                Common.selectedFood.setSugar(new ArrayList<>());
                mySugarAdapter = new MySugarAdapter(this, Common.selectedFood.getSugar());
                foodEditPosition = event.getPos(); //Save food edit to Update
                recycler_addon_size.setAdapter(mySugarAdapter);

                isSugar = event.isSugar();

        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSizeModelUpdate(UpdateSizeModel event) {
        if (event.getSizeModelList() != null) {
            needSave = true;
            Common.selectedFood.setSize(event.getSizeModelList());
        }

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onAddonModelUpdate(UpdateSugarModel event) {
        if (event.getAddonModel() != null) {
            needSave = true;
            Common.selectedFood.setSugar(event.getAddonModel());
        }

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSelectedSizeModel(SelectedSizeModel event) {

        if(event.getSizeModel()!=null)
        {
            edt_name.setText(event.getSizeModel().getName());
            edt_price.setText(String.valueOf(event.getSizeModel().getPrice()));

            btn_edit.setEnabled(true);

        }
        else
        {
            btn_edit.setEnabled(false);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSelectedAddonModel(SelectedAddonModel event) {

        if(event.getAddonModel()!=null)
        {
            edt_name.setText(event.getAddonModel().getName());
            edt_price.setText(String.valueOf(event.getAddonModel().getPrice()));

            btn_edit.setEnabled(true);

        }
        else
        {
            btn_edit.setEnabled(false);
        }
    }
}
