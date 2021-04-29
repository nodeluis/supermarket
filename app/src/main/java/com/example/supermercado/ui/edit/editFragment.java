package com.example.supermercado.ui.edit;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.supermercado.R;
import com.example.supermercado.ip;
import com.example.supermercado.ui.addproduct.addFragment;
import com.example.supermercado.ui.dialogs.confirmFragment;
import com.example.supermercado.ui.dialogs.scannerFragment;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import cz.msebera.android.httpclient.Header;

public class editFragment extends Fragment implements View.OnClickListener, scannerFragment.DialogData,confirmFragment.DataConfirm{

    DatePickerDialog picker;
    EditText code,name,expiration,barcode,description,paymentprice,quantity,price;
    String id;
    ImageView img;
    Button btn,delete;
    int CODE_PERMISSION=301;
    int IMAGE_RESULT=401;
    String path;
    Context context;
    View redir;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            id=getArguments().getString("id");
        }
        context=getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root=inflater.inflate(R.layout.fragment_edit, container, false);

        code=root.findViewById(R.id.fragment_edit_code);
        name=root.findViewById(R.id.fragment_edit_name);
        expiration=root.findViewById(R.id.fragment_edit_expiration);
        barcode=root.findViewById(R.id.fragment_edit_barcode);
        img=root.findViewById(R.id.fragment_edit_img);
        description=root.findViewById(R.id.fragment_edit_description);
        paymentprice=root.findViewById(R.id.fragment_edit_paymentprice);
        quantity=root.findViewById(R.id.fragment_edit_quantity);
        price=root.findViewById(R.id.fragment_edit_price);
        btn=root.findViewById(R.id.fragment_edit_send);
        delete=root.findViewById(R.id.fragment_edit_delete);

        expiration.setOnClickListener(this);
        barcode.setOnClickListener(this);
        btn.setOnClickListener(this);
        delete.setOnClickListener(this);

        loaddata();

        if(reviewPermissions()){
            img.setOnClickListener(this);
        }

        redir=root;
        return root;
    }

    private void loaddata() {
        AsyncHttpClient client=new AsyncHttpClient();
        client.get(ip.ip+"/product/filterprod/"+id,null,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    Glide.with(context).applyDefaultRequestOptions(new RequestOptions()
                            .placeholder(R.drawable.ic_marcablanco)
                            .error(R.drawable.ic_errorload)).load(response.getString("photo")).into(img);
                    name.setText(response.getString("name"));
                    code.setText(response.getString("code"));
                    expiration.setText(response.getString("vencimient"));
                    barcode.setText(response.getString("barcode"));
                    description.setText(response.getString("description"));
                    paymentprice.setText(response.getString("paymentPrice"));
                    quantity.setText(response.getString("quantity"));
                    price.setText(response.getString("price"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.fragment_edit_expiration){
            final Calendar cldr = Calendar.getInstance();
            int day = cldr.get(Calendar.DAY_OF_MONTH);
            int month = cldr.get(Calendar.MONTH);
            int year = cldr.get(Calendar.YEAR);
            // date picker dialog
            picker = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    expiration.setText(year + "-" + ((month + 1)<10?"0"+(month + 1):(month + 1)) + "-"+(dayOfMonth<10?"0"+dayOfMonth:dayOfMonth));
                }
            }, year, month, day);
            picker.show();
        }else if(v.getId()==R.id.fragment_edit_barcode){
            scannerFragment dialog=new scannerFragment();
            dialog.setTargetFragment(editFragment.this, 1);
            dialog.setStyle(DialogFragment.STYLE_NORMAL,
                    android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
            dialog.show(getFragmentManager(), "MyCustomDialog");
        }else if(v.getId()==R.id.fragment_edit_img){
            startActivityForResult(getPickImageChooserIntent(), IMAGE_RESULT);
        }else if(v.getId()==R.id.fragment_edit_send){
            senddata(v);
        }else if(v.getId()==R.id.fragment_edit_delete){
            confirmFragment  dialog=new confirmFragment();
            dialog.setTargetFragment(editFragment.this, 1);
            dialog.setStyle(DialogFragment.STYLE_NORMAL,
                    android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
            dialog.show(getFragmentManager(), "MyCustomDialog");
        }
    }

    private void senddata(final View v) {
        if(path==null){
            AsyncHttpClient client=new AsyncHttpClient();
            RequestParams req=new RequestParams();
            req.put("name",name.getText().toString());
            req.put("code",code.getText().toString());
            req.put("barcode",barcode.getText().toString());
            req.put("vencimient",expiration.getText().toString());
            req.put("description",description.getText().toString());
            req.put("paymentPrice",paymentprice.getText().toString());
            req.put("quantity",quantity.getText().toString());
            req.put("price",price.getText().toString());
            req.put("id",id);
            client.patch(ip.ip+"/product/updateoutfile",req,new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    try {
                        Toast.makeText(context, ""+response.getString("message"), Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(v).navigate(R.id.action_nav_edit_to_nav_home);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }else{
            File file=new File(path);
            AsyncHttpClient client=new AsyncHttpClient();
            RequestParams req=new RequestParams();
            req.put("name",name.getText().toString());
            req.put("code",code.getText().toString());
            req.put("barcode",barcode.getText().toString());
            try {
                req.put("img",file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            req.put("vencimient",expiration.getText().toString());
            req.put("description",description.getText().toString());
            req.put("paymentPrice",paymentprice.getText().toString());
            req.put("quantity",quantity.getText().toString());
            req.put("price",price.getText().toString());
            req.put("id",id);
            client.patch(ip.ip+"/product/update",req,new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    try {
                        Toast.makeText(context, ""+response.getString("message"), Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(v).navigate(R.id.action_nav_edit_to_nav_home);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    @Override
    public void sendDataCode(String data) {
        barcode.setText(data);
    }

    private boolean reviewPermissions() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return true;
        }

        if(getActivity().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED&&
                getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        this.requestPermissions(new String [] {Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,},
                CODE_PERMISSION);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (CODE_PERMISSION == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                img.setOnClickListener(this);
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==getActivity().RESULT_OK){
            if (requestCode == IMAGE_RESULT) {
                String filePath = getImageFilePath(data);
                if (filePath != null) {
                    Bitmap selectedImage = null;
                    Glide.with(context)
                            .load(new File(filePath)).diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true).into(img);

                }
            }
        }
    }

    public Intent getPickImageChooserIntent() {

        Uri outputFileUri = getCaptureImageOutputUri();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getActivity().getPackageManager();

        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }


    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getActivity().getExternalFilesDir("");
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "imagen_supermercado.png"));
        }
        return outputFileUri;
    }

    private String getImageFromFilePath(Intent data) {
        boolean isCamera = data == null || data.getData() == null;
        if (isCamera) {
            path=getCaptureImageOutputUri().getPath();
            return getCaptureImageOutputUri().getPath();
        }
        else {
            path=getPathFromURI(data.getData());
            return getPathFromURI(data.getData());
        }

    }

    public String getImageFilePath(Intent data) {
        return getImageFromFilePath(data);
    }

    private String getPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public void senddataConfirm(Boolean data) {
        if (data){
            deleteProduct();
        }
    }

    private void deleteProduct() {
        AsyncHttpClient client=new AsyncHttpClient();
        client.delete(ip.ip+"/product/product/"+id,null,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    Toast.makeText(context, ""+response.getString("message"), Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(redir).navigate(R.id.action_nav_edit_to_nav_home);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}