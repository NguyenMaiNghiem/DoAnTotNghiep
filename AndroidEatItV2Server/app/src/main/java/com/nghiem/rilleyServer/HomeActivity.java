package com.nghiem.rilleyServer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.nghiem.rilleyServer.Adapter.PdfDocumentAdapter;
import com.nghiem.rilleyServer.Common.Common;
import com.nghiem.rilleyServer.Common.PDFUtils;
import com.nghiem.rilleyServer.EventBus.CategoryClick;
import com.nghiem.rilleyServer.EventBus.ChangeMenuClick;
import com.nghiem.rilleyServer.EventBus.PrintOrderEvent;
import com.nghiem.rilleyServer.EventBus.ToastEvent;
import com.nghiem.rilleyServer.Model.FCMSendData;
import com.nghiem.rilleyServer.Model.OrderModel;
import com.nghiem.rilleyServer.remote.IFCMService;
import com.nghiem.rilleyServer.remote.RetrofitFCMClient;
import com.google.android.gms.tasks.OnSuccessListener;

import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import butterknife.ButterKnife;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private NavController navController;
    private int menuClick = -1;

    private ImageView img_upload;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IFCMService ifcmService;
    private Uri imgUri = null;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private AlertDialog dialog;


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().removeAllStickyEvents(); //Fix eventbus always called after OnaCtivity result.
        EventBus.getDefault().unregister(this);
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);



        init();


        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_category, R.id.nav_food_list, R.id.nav_order, R.id.nav_shipper)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();
        View headerView = navigationView.getHeaderView(0);
        TextView txt_user = (TextView) headerView.findViewById(R.id.txt_user);
        Common.setSpanString("Hey,", Common.currentServerUser.getName(), txt_user);

        menuClick = R.id.nav_category; //Default

        checkIsOpenFromActivity();
    }

    private void init() {
        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        subscribeToTopic(Common.createTopicOrder());
        updateToken();

        dialog = new AlertDialog.Builder(this).setCancelable(false)
                .setMessage("Please Wait...")
                .create();
    }

    private void checkIsOpenFromActivity() {
        boolean isOpenFromNewOrder = getIntent().getBooleanExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER, false);
        if (isOpenFromNewOrder) {
            navController.popBackStack();
            navController.navigate(R.id.nav_order);
            menuClick = R.id.nav_order;
        }
    }

    private void updateToken() {
        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnFailureListener(e -> {
                    Toast.makeText(HomeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        Common.updateNewToken(HomeActivity.this,
                                instanceIdResult.getToken(),
                                true,
                                false);

                        Log.d("MYTOKEN", instanceIdResult.getToken());
                    }
                });


    }

    private void subscribeToTopic(String topicOrder) {
        FirebaseMessaging.getInstance()
                .subscribeToTopic(topicOrder)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful())
                        Toast.makeText(this, "Failed:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCategorySelected(CategoryClick event) {
        if (event.isSuccess()) {
            if (menuClick != R.id.nav_food_list) {
                navController.navigate(R.id.nav_food_list);
                menuClick = R.id.nav_food_list;
                //Toast.makeText(this, "Click To "+event.getCategoryModel().getName(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onToastEvent(ToastEvent event) {
        if (event.getAction() == Common.ACTION.CREATE) {
            Toast.makeText(this, "Create Success!", Toast.LENGTH_SHORT).show();
        }
        else if (event.getAction() == Common.ACTION.UPDATE) {
            Toast.makeText(this, "Update Success!", Toast.LENGTH_SHORT).show();
        }

        else {
            Toast.makeText(this, "Delete Success!", Toast.LENGTH_SHORT).show();
        }

        EventBus.getDefault().postSticky(new ChangeMenuClick(event.isFromFoodList()));
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onChangeMenuClick(ChangeMenuClick event) {
        if (!event.isFromFoodList()) {
            //Clear
            navController.popBackStack(R.id.nav_category, true);
            navController.navigate(R.id.nav_category);
        }
        menuClick = -1;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        menuItem.setChecked(true);
        drawer.closeDrawers();
        switch (menuItem.getItemId()) {
            case R.id.nav_category:
                if (menuItem.getItemId() != menuClick) {
                    navController.popBackStack(); //Remove all Backstack
                    navController.navigate(R.id.nav_category);
                }
                break;
            case R.id.nav_order:
                if (menuItem.getItemId() != menuClick) {
                    navController.popBackStack(); //Remove all Backstack
                    navController.navigate(R.id.nav_order);
                }
                break;
            case R.id.nav_shipper:
                if (menuItem.getItemId() != menuClick) {
                    navController.popBackStack(); //Remove all Backstack
                    navController.navigate(R.id.nav_shipper);
                }
                break;
            case R.id.nav_best_deals:
                if (menuItem.getItemId() != menuClick) {
                    navController.popBackStack(); //Remove all Backstack
                    navController.navigate(R.id.nav_best_deals);
                }
                break;
            case R.id.nav_most_popular:
                if (menuItem.getItemId() != menuClick) {
                    navController.popBackStack(); //Remove all Backstack
                    navController.navigate(R.id.nav_most_popular);
                }
                break;
            case R.id.nav_send_news:
                showNewsDialog();
                break;
            case R.id.nav_sign_out:
                signOut();
                break;
            default:
                menuClick = -1;
                break;

        }

        menuClick = menuItem.getItemId();
        return true;
    }

    private void showNewsDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("News System");
        builder.setMessage("Send news notification to all Client");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_news_system, null);

        //Views
        EditText edt_title = (EditText) itemView.findViewById(R.id.edt_title);
        EditText edt_content = (EditText) itemView.findViewById(R.id.edt_content);
        EditText edt_link = (EditText) itemView.findViewById(R.id.edt_link);
        img_upload = (ImageView) itemView.findViewById(R.id.img_upload);
        RadioButton rdi_none = (RadioButton) itemView.findViewById(R.id.rdi_none);
        RadioButton rdi_link = (RadioButton) itemView.findViewById(R.id.rdi_link);
        RadioButton rdi_upload = (RadioButton) itemView.findViewById(R.id.rdi_image);

        //Event
        rdi_none.setOnClickListener(view -> {
            edt_link.setVisibility(View.GONE);
            img_upload.setVisibility(View.GONE);
        });

        rdi_link.setOnClickListener(view -> {
            edt_link.setVisibility(View.VISIBLE);
            img_upload.setVisibility(View.GONE);
        });

        rdi_upload.setOnClickListener(view -> {
            edt_link.setVisibility(View.GONE);
            img_upload.setVisibility(View.VISIBLE);
        });

        img_upload.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PICK_IMAGE_REQUEST);
        });

        builder.setView(itemView);

        builder.setNegativeButton("CANCEL", (dialogInterface, which) -> dialogInterface.dismiss())
                .setPositiveButton("SEND", (dialogInterface, which) -> {
                    if (rdi_none.isChecked()) {
                        sendNews(edt_title.getText().toString(), edt_content.getText().toString());
                    } else if (rdi_link.isChecked()) {
                        sendNews(edt_title.getText().toString(), edt_content.getText().toString(), edt_link.getText().toString());
                    } else if (rdi_upload.isChecked()) {
                        if (imgUri != null) {
                            AlertDialog dialog = new AlertDialog.Builder(this).setMessage("Uploading...").create();
                            dialog.show();

                            String file_name = UUID.randomUUID().toString();
                            StorageReference newsImage = storageReference.child("news/" + file_name);
                            newsImage.putFile(imgUri)
                                    .addOnFailureListener(e -> {
                                        dialog.dismiss();
                                        Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnSuccessListener(taskSnapshot -> {
                                        dialog.dismiss();
                                        newsImage.getDownloadUrl().addOnSuccessListener(uri -> {
                                            sendNews(edt_title.getText().toString(), edt_content.getText().toString(), uri.toString());
                                        });
                                    }).addOnProgressListener(taskSnapshot -> {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                dialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                            });
                        }
                    }
                });

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sendNews(String title, String content, String url) {
        Map<String, String> notificationData = new HashMap<>();
        notificationData.put(Common.NOTI_TITLE, title);
        notificationData.put(Common.NOTI_CONTENT, content);
        notificationData.put(Common.IS_SEND_IMAGE, "true");
        notificationData.put(Common.IMAGE_URL, url);


        FCMSendData fcmSendData =
                new FCMSendData(Common.getNewsTopic(), notificationData);

        AlertDialog dialog = new AlertDialog.Builder(this).setMessage("Waiting....").create();
        dialog.show();

        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fcmResponse -> {
                    dialog.dismiss();
                    if (fcmResponse.getMessage_id() != 0)
                        Toast.makeText(this, "News has been Sent", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(this, "News Send failed!", Toast.LENGTH_SHORT).show();
                }, throwable -> {
                    dialog.dismiss();
                    Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                })

        );

    }

    private void sendNews(String title, String content) {
        Map<String, String> notificationData = new HashMap<>();
        notificationData.put(Common.NOTI_TITLE, title);
        notificationData.put(Common.NOTI_CONTENT, content);
        notificationData.put(Common.IS_SEND_IMAGE, "false");


        FCMSendData fcmSendData =
                new FCMSendData(Common.getNewsTopic(), notificationData);

        AlertDialog dialog = new AlertDialog.Builder(this).setMessage("Waiting....").create();
        dialog.show();

        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fcmResponse -> {
                    dialog.dismiss();
                    if (fcmResponse.getMessage_id() != 0)
                        Toast.makeText(this, "News has been Sent", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(this, "News Send failed!", Toast.LENGTH_SHORT).show();
                }, throwable -> {
                    dialog.dismiss();
                    Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                })

        );


    }

    private void signOut() {
        //Copy code from client
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Signout");
        builder.setMessage("Do you really want to sign out?");

        builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("OK", (dialog, which) -> {
            Common.selectedFood = null;
            Common.categorySelected = null;
            Common.currentServerUser = null;

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

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                imgUri = data.getData();
                img_upload.setImageURI(imgUri);

            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPrintEventListener(PrintOrderEvent event) {
        createPDFFile(event.getPath(), event.getOrderModel());
    }

    @SuppressLint("CheckResult")
    private void createPDFFile(String path, OrderModel orderModel) {
        dialog.show();

        if (new File(path).exists())
            new File(path).delete();
        try {
            Document document = new Document();

            //Save
            PdfWriter.getInstance(document, new FileOutputStream(path));

            //Open
            document.open();

            //Setting
            document.setPageSize(PageSize.A4);
            document.addCreationDate();
            document.addAuthor("Nirmal Bakery");
            document.addCreator(Common.currentServerUser.getName());

            //Font Setting
            BaseColor colorAccent = new BaseColor(0, 153, 204, 255);
            float fontsize = 20.0f;

            //Custom Font
            BaseFont fontName = BaseFont.createFont("assets/fonts/brandon_medium.otf", "UTF-8", BaseFont.EMBEDDED);

            //Create Title for Document
            Font titleFont = new Font(fontName, 36.0f, Font.NORMAL, BaseColor.BLACK);
            PDFUtils.addNewItem(document, "Order Details", Element.ALIGN_CENTER, titleFont);

            //Add More
            Font orderNumberfont = new Font(fontName, fontsize, Font.NORMAL, colorAccent);
            PDFUtils.addNewItem(document, "Order No:", Element.ALIGN_LEFT, orderNumberfont);
            Font orderNumberValuefont = new Font(fontName, 20, Font.NORMAL, BaseColor.BLACK);
            PDFUtils.addNewItem(document, orderModel.getKey(), Element.ALIGN_LEFT, orderNumberValuefont);

            PDFUtils.addLineSeparator(document);

            //Date
            PDFUtils.addNewItem(document, "Order Date:", Element.ALIGN_LEFT, orderNumberfont);
            PDFUtils.addNewItem(document, new SimpleDateFormat("dd/MM/yyyy").format(orderModel.getCreateDate()), Element.ALIGN_LEFT, orderNumberValuefont);

            PDFUtils.addLineSeparator(document);

            //Account Name
            PDFUtils.addNewItem(document, "Account Name:", Element.ALIGN_LEFT, orderNumberfont);
            PDFUtils.addNewItem(document, orderModel.getUserName(), Element.ALIGN_LEFT, orderNumberValuefont);

            PDFUtils.addLineSeparator(document);

            //Add Product and Detail
            PDFUtils.addLineSpace(document);
            PDFUtils.addNewItem(document, "Product Detail", Element.ALIGN_LEFT,  titleFont);
            PDFUtils.addLineSeparator(document);

            //Use RXJava, Fetch Image from Internet and add to PDF
            Observable.fromIterable(orderModel.getCartItemList())
                    .flatMap(cartItem -> Common.getBitmapFromUrl(HomeActivity.this, cartItem, document))
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(cartItem -> { //On Next
                        //Each item, We will add Detail
                        PDFUtils.addNewItemWithLeftAndRight(document, cartItem.getFoodName(), ("(0.0%)"), orderNumberValuefont,
                                orderNumberValuefont);

                        //Food Size and AddOn
                        PDFUtils.addNewItemWithLeftAndRight(document, "Size", Common.formatSizeJsontoString(cartItem.getFoodSize()),
                                orderNumberValuefont, orderNumberValuefont);

                        PDFUtils.addNewItemWithLeftAndRight(document, "Addon", Common.formatAddonJsontoString(cartItem.getFoodAddOn()),
                                orderNumberValuefont, orderNumberValuefont);

                        //Food Price
                        //Format Like 1*30 = 30
                        PDFUtils.addNewItemWithLeftAndRight(document,
                                new StringBuilder()
                                        .append(cartItem.getFoodQuantity())
                                        .append("*")
                                        .append(cartItem.getFoodPrice() + cartItem.getFoodExtraPrice()).toString(),
                                new StringBuilder()
                                        .append(cartItem.getFoodQuantity()*(cartItem.getFoodPrice() + cartItem.getFoodExtraPrice())).toString(),
                                orderNumberValuefont,
                                orderNumberValuefont);

                        PDFUtils.addLineSeparator(document);



                    }, throwable -> { //On Error
                        dialog.dismiss();
                        Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }, () -> { //on Complete
                        //When Complete Append Total
                        PDFUtils.addLineSpace(document);
                        PDFUtils.addLineSpace(document);

                        PDFUtils.addNewItemWithLeftAndRight(document, "Total",
                                new StringBuilder().append(orderModel.getTotalPayment()).toString(),
                                titleFont,
                                titleFont);

                        //Close
                        document.close();
                        dialog.dismiss();
                        Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();

                        printPDF();
                    });


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    private void printPDF() {
        PrintManager printManager = (PrintManager)getSystemService(Context.PRINT_SERVICE);
        try{
            PrintDocumentAdapter printDocumentAdapter = new PdfDocumentAdapter(this, new StringBuilder(Common.getAppPath(this))
                    .append(Common.FILE_PRINT).toString());
            printManager.print("Document", printDocumentAdapter, new PrintAttributes.Builder().build());

        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}

