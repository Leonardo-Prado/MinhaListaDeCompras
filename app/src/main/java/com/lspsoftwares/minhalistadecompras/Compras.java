package com.lspsoftwares.minhalistadecompras;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
