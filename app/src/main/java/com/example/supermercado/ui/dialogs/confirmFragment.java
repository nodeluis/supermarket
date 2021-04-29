package com.example.supermercado.ui.dialogs;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.supermercado.R;

public class confirmFragment extends DialogFragment implements View.OnClickListener {

    Button cancel,ok;
    Context context;
    DataConfirm listen;

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
        View root=inflater.inflate(R.layout.fragment_confirm, container, false);

        cancel=root.findViewById(R.id.fargment_confirm_cancel);
        ok=root.findViewById(R.id.fargment_confirm_ok);

        cancel.setOnClickListener(this);
        ok.setOnClickListener(this);

        return root;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listen= (DataConfirm) getTargetFragment();
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.fargment_confirm_cancel){
            listen.senddataConfirm(false);
            getDialog().dismiss();
        }else if(view.getId()==R.id.fargment_confirm_ok){
            listen.senddataConfirm(true);
            getDialog().dismiss();
        }
    }

    public interface DataConfirm{
        void senddataConfirm(Boolean data);
    }
}