package com.example.supermercado.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.supermercado.R;

import java.util.ArrayList;

public class AdapterProduct extends RecyclerView.Adapter<AdapterProduct.HolderProduct>{
    ArrayList<Item> mArray;
    Context context;
    DataIdDetails listen;

    public AdapterProduct(Context context,DataIdDetails listen) {
        this.mArray = new ArrayList<>();
        this.context = context;
        this.listen=listen;
    }

    public void add(Item it){
        mArray.add(it);
        notifyItemInserted(mArray.indexOf(it));
    }

    public void cleanAdapter(){
        mArray.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HolderProduct onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.itemproduct, parent, false);
        return new HolderProduct(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProduct holder, int position) {
        final Item it=mArray.get(position);
        Glide.with(context).applyDefaultRequestOptions(new RequestOptions()
                .placeholder(R.drawable.ic_marcablanco)
                .error(R.drawable.ic_errorload)).load(it.getImg()).into(holder.img);
        holder.code.setText(it.getCode());
        holder.name.setText(it.getName());
        holder.barcode.setText(it.getBarcode());
        holder.expiration.setText(it.getExpiration());
        holder.description.setText(it.getDescription());
        holder.paymentprice.setText(it.getPricepayment());
        holder.quantity.setText(it.getQuantity());
        holder.price.setText(it.getPrice());
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listen.sendid(it.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mArray.size();
    }

    class HolderProduct extends RecyclerView.ViewHolder {
        ImageView img;
        TextView code,name,barcode,expiration,description,paymentprice,quantity,price;
        View view;
        public HolderProduct(@NonNull View itemView) {
            super(itemView);
            img=itemView.findViewById(R.id.item_img);
            code=itemView.findViewById(R.id.item_code);
            name=itemView.findViewById(R.id.item_name);
            barcode=itemView.findViewById(R.id.item_barcode);
            expiration=itemView.findViewById(R.id.item_expiration);
            description=itemView.findViewById(R.id.item_description);
            paymentprice=itemView.findViewById(R.id.item_paymentprice);
            quantity=itemView.findViewById(R.id.item_quantity);
            price=itemView.findViewById(R.id.item_price);
            view=itemView;
        }
    }

    public interface DataIdDetails{
        void sendid(String id);
    }
}
