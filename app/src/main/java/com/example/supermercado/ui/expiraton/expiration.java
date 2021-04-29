package com.example.supermercado.ui.expiraton;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.supermercado.R;
import com.example.supermercado.ip;
import com.example.supermercado.ui.adapter.AdapterProduct;
import com.example.supermercado.ui.adapter.Item;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import cz.msebera.android.httpclient.Header;

public class expiration extends Fragment implements AdapterProduct.DataIdDetails {
    Context context;
    EditText days;
    RecyclerView rec;
    AdapterProduct adp;
    LinearLayoutManager lnm;
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
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_expiration, container, false);
        days=root.findViewById(R.id.fragment_expiration_days);
        days.setText(7+"");
        rec=root.findViewById(R.id.fragment_expiration_recycler);
        lnm=new LinearLayoutManager(context);
        adp=new AdapterProduct(context,this);
        rec.setLayoutManager(lnm);
        rec.setAdapter(adp);


        days.addTextChangedListener(new TextWatcher() {
            Handler handler = new android.os.Handler();
            Runnable runnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                handler.removeCallbacks(runnable);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(runnable);
            }

            @Override
            public void afterTextChanged(Editable s) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        if(!s.equals("")){
                            skip=0;
                            adp.cleanAdapter();
                            loadExpirations(s+"");
                        }else{
                            skip=0;
                            adp.cleanAdapter();
                            days.setText(7+"");
                            loadExpirations(7+"");
                        }
                    }
                };
                handler.postDelayed(runnable, 1000);
            }
        });

        loadExpirations(days.getText().toString());

        rec.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE) {
                    if(con==0){
                        con++;
                        loadExpirations(days.getText().toString());
                    }

                }
            }
        });

        redir=root;
        return root;

    }

    private void loadExpirations(String x) {
        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        AsyncHttpClient client=new AsyncHttpClient();
        RequestParams req=new RequestParams();
        req.put("days",x);
        req.put("date",year + "-" + ((month + 1)<10?"0"+(month + 1):(month + 1)) + "-"+(day<10?"0"+day:day));
        req.put("skip",skip);
        req.put("limit",limit);
        client.post(ip.ip+"/product/productExpired",req,new JsonHttpResponseHandler(){
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
                }else{
                    Toast.makeText(context, "No existen mas productos vencidos en "+x+" dias" , Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    @Override
    public void sendid(String id) {
        Bundle b=new Bundle();
        b.putString("id",id);
        Navigation.findNavController(redir).navigate(R.id.action_nav_expiration_to_nav_edit,b);
    }
}