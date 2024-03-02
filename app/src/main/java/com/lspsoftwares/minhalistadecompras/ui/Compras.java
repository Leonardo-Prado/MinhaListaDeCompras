package com.lspsoftwares.minhalistadecompras.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.lspsoftwares.minhalistadecompras.R;

public class Compras extends Fragment {

    public Compras() {
        // Required empty public constructor
    }


    public static Compras newInstance() {
        return new Compras();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_compras, container, false);

        return view;
    }
}
