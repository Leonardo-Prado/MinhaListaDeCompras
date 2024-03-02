package com.lspsoftwares.minhalistadecompras.ui;

import android.content.ContentValues;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lspsoftwares.minhalistadecompras.R;
import com.lspsoftwares.minhalistadecompras.db.DBGeneric;
import com.lspsoftwares.minhalistadecompras.entidades.Item;
import com.lspsoftwares.minhalistadecompras.entidades.ListaCompras;
import com.lspsoftwares.minhalistadecompras.nucleo.adapters.rv_lista_compras.RvListaComprasAdapter;
import com.lspsoftwares.minhalistadecompras.nucleo.estatico.VariaveisEstaticas;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.DialogConstrutor;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.GeradorCodigosUnicos;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.ManipuladorDataTempo;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class Lista extends Fragment {
    private FloatingActionButton fabAddLista;
    private Resources resources;
    FirebaseDatabase database;
    DatabaseReference myRef;
    RvListaComprasAdapter rvListaComprasAdapter;
    private InterstitialAd interstitialAd;
    private boolean connected = false;

    //private OnFragmentInteractionListener mListener;

    public Lista() {
        // Required empty public constructor
    }

    public static Lista newInstance(boolean connected,InterstitialAd interstitialAd,FloatingActionButton button) {
        Lista fragment = new Lista();
        fragment.setConnected(connected);
        fragment.setInterstitialAd(interstitialAd);
        fragment.fabAddLista = button;
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
       View v =  inflater.inflate(R.layout.fragment_lista, container, false);
       database = FirebaseDatabase.getInstance();
       myRef = database.getReference();
       resources = getResources();
       final RecyclerView rvMinhasListas = v.findViewById(R.id.rvMinhasListas);
       LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
       linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
       rvMinhasListas.setLayoutManager(linearLayoutManager);
       rvListaComprasAdapter = new RvListaComprasAdapter(getContext(),interstitialAd);
       rvMinhasListas.setAdapter(rvListaComprasAdapter);
        fabAddLista.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
               View view = inflater.inflate(R.layout.dialog_add_lista, null);
               final EditText edNome = view.findViewById(R.id.edNome);
               final EditText edDescricao = view.findViewById(R.id.edDescricao);
               Button btnAdd = view.findViewById(R.id.btnAdd);
               final DialogConstrutor dialogAddLista = new DialogConstrutor(getContext(),view,resources.getString(R.string.fragment_lista_dialog_add_lista_titulo),resources.getString(R.string.fragment_lista_dialog_add_lista_menssagem));
               btnAdd.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       final ListaCompras lista = new ListaCompras();
                       lista.setNome(edNome.getText().toString());
                       lista.setCriadorUid(VariaveisEstaticas.getUsuario().getUid());
                       lista.setDescricao(edDescricao.getText().toString());
                       try {
                           ManipuladorDataTempo dataTempo = new ManipuladorDataTempo(new Date());
                           lista.setDataCriacao(dataTempo.getDataInt());
                           lista.setHoraCriacao(dataTempo.getTempoInt());
                       } catch (ParseException e) {
                           e.printStackTrace();
                       }
                       GeradorCodigosUnicos gcu = new GeradorCodigosUnicos(10);
                       lista.setuId(gcu.gerarCodigos());
                       lista.setIcon(setIconeLista(lista.getNome()));
                       VariaveisEstaticas.getUsuario().getListas().add(lista.getuId());
                       myRef.child("Lista").child(lista.getuId()).setValue(lista);
                       Map<String, Object> update = new HashMap<>();
                       update.put(VariaveisEstaticas.getUsuario().getUid(), VariaveisEstaticas.getUsuario());
                       myRef.child("Usuario").updateChildren(update);
                       VariaveisEstaticas.getListaCompras().add(lista);
                       VariaveisEstaticas.getItemMap().put(lista.getuId(),new ArrayList<Item>());
                       VariaveisEstaticas.getVisiveis().add(1);
                       rvListaComprasAdapter.notifyDataSetChanged();
                       addListaDB(lista);
                       dialogAddLista.fechar();
                   }
               });
           }
       });

       return v;

    }

    private int setIconeLista(String nome) {
        if(nome.toLowerCase().contains("niver"))
            return 4;
        else if(nome.toLowerCase().contains("churras"))
            return 3;
        else if(nome.toLowerCase().contains("bolo"))
            return 5;
        else if(nome.toLowerCase().contains("caf")||nome.toLowerCase().contains("lanche"))
            return 6;
        else {
            Random random = new Random();
            return random.nextInt(3);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        rvListaComprasAdapter.onPause();
    }

    @Override
    public void onPause() {
        super.onPause();
        rvListaComprasAdapter.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        rvListaComprasAdapter.onPause(true);
    }

    @Override
    public void onStart(){
        super.onStart();
        rvListaComprasAdapter.notifyDataSetChanged();
    }

    private void addListaDB(ListaCompras lista) {
        DBGeneric db = new DBGeneric(getContext());
        ContentValues values = new ContentValues();
        values.put("_id", lista.getuId());
        values.put("_idUsuario",VariaveisEstaticas.getUsuario().getUid());
        values.put("CriadorUid", lista.getCriadorUid());
        values.put("DataCriacao", lista.getDataCriacao());
        values.put("Descricao", lista.getDescricao());
        values.put("HoraCriacao", lista.getHoraCriacao());
        values.put("Nome", lista.getNome());
        values.put("Sync",1);
        int a = db.inserir(values, "Lista");
        values = new ContentValues();
        values.put("_IdLista",lista.getuId());
        values.put("_IdUser",VariaveisEstaticas.getUsuario().getUid());
        values.put("Sync",1);
        int b  = db.inserir(values,"UserLista");
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public InterstitialAd getInterstitialAd() {
        return interstitialAd;
    }

    public void setInterstitialAd(InterstitialAd interstitialAd) {
        this.interstitialAd = interstitialAd;
    }
}
