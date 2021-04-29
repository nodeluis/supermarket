package com.example.supermercado.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.supermercado.R;
import com.example.supermercado.ip;
import com.example.supermercado.ui.adapter.AdapterProduct;
import com.example.supermercado.ui.adapter.Item;
import com.example.supermercado.ui.addproduct.addFragment;
import com.example.supermercado.ui.dialogs.scannerFragment;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class HomeFragment extends Fragment implements AdapterProduct.DataIdDetails,scannerFragment.DialogData{
    RecyclerView rec;
    AdapterProduct adp;
    LinearLayoutManager lnm;
    Context context;
    SearchView searchView;
    String search;
    EditText salto;

    int skip;
    int limit;
    int con;
    View redir;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=getActivity();
        skip=0;
        limit=20;
        con=0;
        search="";
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        searchView=root.findViewById(R.id.HomesearchView);
        salto=root.findViewById(R.id.home_skip);
        rec=root.findViewById(R.id.HomeRecycler);
        lnm=new LinearLayoutManager(context);
        adp=new AdapterProduct(context,this);
        rec.setLayoutManager(lnm);
        rec.setAdapter(adp);
        load();

        salto.addTextChangedListener(new TextWatcher() {
            Handler handler = new android.os.Handler();
            Runnable runnable;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                handler.removeCallbacks(runnable);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        if(!s.equals("")){
                            try {
                                skip=Integer.parseInt(s+"");
                            }catch (Exception e){
                                skip=0;
                            }
                            adp.cleanAdapter();
                            load();
                        }else{
                            skip=0;
                            adp.cleanAdapter();
                            load();
                        }
                    }
                };
                handler.postDelayed(runnable, 1000);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!query.equals("")){
                    skip=0;
                    search=query;
                    adp.cleanAdapter();
                    searchText(query);
                }else{
                    try {
                        skip=Integer.parseInt(salto.getText().toString()+"");
                    }catch (Exception e){
                        skip=0;
                    }
                    search="";
                    adp.cleanAdapter();
                    load();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.equals("")){
                    try {
                        skip=Integer.parseInt(salto.getText().toString()+"");
                    }catch (Exception e){
                        skip=0;
                    }
                    search="";
                    adp.cleanAdapter();
                    load();
                }
                return false;
            }
        });

        rec.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE) {
                    if(con==0&&search.equals("")){
                        con++;
                        load();
                    }else if(con==0){
                        con++;
                        searchText(search);
                    }

                }
            }
        });
        redir=root;
        return root;
    }
    private void searchText(String x) {
        AsyncHttpClient client=new AsyncHttpClient();
        client.get(ip.ip+"/product/search?skip="+skip+"&limit="+limit+"&search="+x,null,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                for (int i=0;i<response.length();i++){
                    try {
                        JSONObject ob=response.getJSONObject(i);
                        Item it=new Item(ob.getString("_id")+"",
                                ob.getString("photo")+"",
                                ob.getString("code")+"",
                                ob.getString("name")+"",
                                ob.getString("barcode")+"",
                                ob.getString("description")+"",
                                ob.getString("vencimient")+"",
                                ob.getString("paymentPrice")+"",
                                ob.getInt("quantity")+"",
                                ob.getString("price")+"");
                        adp.add(it);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if(response.length()>0){
                    con=0;
                    skip+=20;
                }

            }
        });
    }

    private void load() {
        AsyncHttpClient client=new AsyncHttpClient();
        client.get(ip.ip+"/product/home?skip="+skip+"&limit="+limit,null,new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                for (int i=0;i<response.length();i++){
                    try {
                        JSONObject ob=response.getJSONObject(i);
                        Item it=new Item(ob.getString("_id")+"",
                                ob.getString("photo")+"",
                                ob.getString("code")+"",
                                ob.getString("name")+"",
                                ob.getString("barcode")+"",
                                ob.getString("description")+"",
                                ob.getString("vencimient")+"",
                                ob.getString("paymentPrice")+"",
                                ob.getInt("quantity")+"",
                                ob.getString("price")+"");
                        adp.add(it);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if(response.length()>0){
                    con=0;
                    skip+=20;
                }
            }
        });
    }

    @Override
    public void sendid(String id) {
        Bundle b=new Bundle();
        b.putString("id",id);
        Navigation.findNavController(redir).navigate(R.id.action_nav_home_to_nav_edit,b);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        //inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.scanner_search:
                scannerFragment dialog=new scannerFragment();
                dialog.setTargetFragment(HomeFragment.this, 1);
                dialog.setStyle(DialogFragment.STYLE_NORMAL,
                        android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
                dialog.show(getFragmentManager(), "MyCustomDialog");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void sendDataCode(String data) {
        search=data;
        searchView.setQuery(data,true);
    }
}