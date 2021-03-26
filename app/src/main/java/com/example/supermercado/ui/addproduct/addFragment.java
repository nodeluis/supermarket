package com.example.supermercado.ui.addproduct;

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

import com.example.supermercado.R;
import com.example.supermercado.ip;
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

import cz.msebera.android.httpclient.Header;

public class addFragment extends Fragment implements View.OnClickListener, scannerFragment.DialogData {

    DatePickerDialog picker;
    EditText code,name,expiration,barcode,description,paymentprice,quantity,price;
    ImageView img;
    Button btn;
    int CODE_PERMISSION=300;
    int IMAGE_RESULT=400;
    String path;
    Context context;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
        context=getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root=inflater.inflate(R.layout.fragment_add, container, false);

        code=root.findViewById(R.id.fragment_add_code);
        name=root.findViewById(R.id.fragment_add_name);
        expiration=root.findViewById(R.id.fragment_add_expiration);
        barcode=root.findViewById(R.id.fragment_add_barcode);
        img=root.findViewById(R.id.fragment_add_img);
        description=root.findViewById(R.id.fragment_add_description);
        paymentprice=root.findViewById(R.id.fragment_add_paymentprice);
        quantity=root.findViewById(R.id.fragment_add_quantity);
        price=root.findViewById(R.id.fragment_add_price);
        btn=root.findViewById(R.id.fragment_add_send);

        expiration.setOnClickListener(this);
        barcode.setOnClickListener(this);
        btn.setOnClickListener(this);

        if(reviewPermissions()){
            img.setOnClickListener(this);
        }
        return root;
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.fragment_add_expiration){
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
        }else if(v.getId()==R.id.fragment_add_barcode){
            scannerFragment dialog=new scannerFragment();
            dialog.setTargetFragment(addFragment.this, 1);
            dialog.setStyle(DialogFragment.STYLE_NORMAL,
                    android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
            dialog.show(getFragmentManager(), "MyCustomDialog");
        }else if(v.getId()==R.id.fragment_add_img){
            startActivityForResult(getPickImageChooserIntent(), IMAGE_RESULT);
        }else if(v.getId()==R.id.fragment_add_send){
            senddata(v);
        }
    }

    private void senddata(final View v) {
        if(path==null){
            Toast.makeText(context, "Se requiere una fotografia para enviar", Toast.LENGTH_SHORT).show();
            return;
        }
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
        client.post(ip.ip+"/product/inserted",req,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    Toast.makeText(context, ""+response.getString("message"), Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(v).navigate(R.id.action_nav_add_to_nav_home);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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
                    Bitmap selectedImage = BitmapFactory.decodeFile(filePath);
                    img.setImageBitmap(selectedImage);
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
            return path;
        }
        else {
            path=getPathFromURI(data.getData());
            return path;
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
}