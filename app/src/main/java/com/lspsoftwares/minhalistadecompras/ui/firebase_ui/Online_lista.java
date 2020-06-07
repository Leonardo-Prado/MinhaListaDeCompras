package com.lspsoftwares.minhalistadecompras.ui.firebase_ui;


import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lspsoftwares.minhalistadecompras.R;
import com.lspsoftwares.minhalistadecompras.nucleo.adapters.lista_online_adapters.RVListaComprasOnlineAdapter;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.ListaCompras;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
/**
 * A simple {@link Fragment} subclass.
 */
public class Online_lista extends Fragment {

    EditText edBuscarOnline;
    RecyclerView rvListaOnline;
    FirebaseDatabase database;
    DatabaseReference myRef;
    private Resources resources;
    List<ListaCompras> listaComprasOnline = new ArrayList<>();
    RVListaComprasOnlineAdapter rvListaComprasAdapter;
    List<String> listasIds;
    boolean podeLimpar = true;

    public Online_lista() {
        // Required empty public constructor
    }

    public static Online_lista newInstance() {
        Online_lista fragment = new Online_lista();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentLayout = inflater.inflate(R.layout.fragment_online_lista, container, false);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        resources = getResources();
        edBuscarOnline = fragmentLayout.findViewById(R.id.edBuscarOnline);
        rvListaOnline = fragmentLayout.findViewById(R.id.rvListasOnline);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvListaOnline.setLayoutManager(linearLayoutManager);
        rvListaComprasAdapter = new RVListaComprasOnlineAdapter(getContext(),listaComprasOnline);
        rvListaOnline.setAdapter(rvListaComprasAdapter);
        edBuscarOnline.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(podeLimpar) {
                    listasIds = new ArrayList<>();
                    podeLimpar = false;
                    listaComprasOnline.clear();
                    rvListaComprasAdapter.notifyDataSetChanged();
                }
                if(edBuscarOnline.getText().toString().length()>=2){
                    String[] tags = edBuscarOnline.getText().toString().split(" ");
                    for (String tag:tags) {
                        buscarListasOnline(tag);
                    }
                    buscarListasOnline(edBuscarOnline.getText().toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return fragmentLayout;
    }

    private void buscarListasOnline(String tag) {
        listaComprasOnline.clear();
        rvListaComprasAdapter.notifyDataSetChanged();
        Query query = myRef.child("Tags").orderByKey().startAt(tag).endAt(tag+"zzzzzzzzzzzzzzzzz").limitToFirst(10);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    listaComprasOnline.clear();
                    rvListaComprasAdapter.notifyDataSetChanged();
                    int i = 0;
                    for (DataSnapshot snapshot:dataSnapshot.getChildren()) {
                        if(i>10)
                            break;
                        int j = 0;
                        List<HashMap<String, Object>> list = (List<HashMap<String, Object>>) snapshot.child("classificacoes").getValue();
                        List<String> listOrdenada = ordenaLista(list);

                        for (String s:listOrdenada) {
                            if(j>15)
                                break;
                            if(!listasIds.contains(s)) {
                                myRef.child("Lista").child(s).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        ListaCompras listaCompras = dataSnapshot.getValue(ListaCompras.class);
                                        listaComprasOnline.add(listaCompras);
                                        rvListaComprasAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                j++;
                                listasIds.add(s);
                            }
                            podeLimpar = true;

                        }
                        i++;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private List<String> ordenaLista(List<HashMap<String, Object>> list) {
        List<String> ordenada = new ArrayList<>();
        try {
            if (list != null) {
                List<HashMap<String, Object>> porVal = list;
                List<HashMap<String, Object>> porAval = list;
                try {
                    Collections.sort(porVal, new Comparator<HashMap<String, Object>>() {
                        @Override
                        public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
                            return (int) (1000 * (Double.parseDouble(o1.get("val").toString()) - Double.parseDouble(o2.get("val").toString())));
                        }
                    });
                }catch (Exception e){}
                try {
                    Collections.sort(porAval, new Comparator<HashMap<String, Object>>() {
                        @Override
                        public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
                            return ((int)o2.get("aval")) - ((int) o1.get("aval"));
                        }
                    });
                }catch (Exception ex){}
                for (int i = 0; i < list.size(); i++) {
                    HashMap<String,Object> a = list.get(i);
                    String luid =a.get("uId").toString();
                    if (i % 2 == 0 && i > 0)
                        luid = porAval.get(i).get("uId").toString() ;
                    else
                        luid = porVal.get(i).get("uId").toString();
                    ordenada.add(luid);
                }
            }
        }catch (Exception e){
            Log.e("Erro",e.getMessage());
        }
        return ordenada;
    }

}
