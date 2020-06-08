package com.lspsoftwares.minhalistadecompras.ui.anonimo_ui;

import android.content.ContentValues;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lspsoftwares.minhalistadecompras.R;
import com.lspsoftwares.minhalistadecompras.db.DBGeneric;
import com.lspsoftwares.minhalistadecompras.nucleo.adapters.lista_anonimas_adapters.RvListaAnonimo;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.Item;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.ListaCompras;
import com.lspsoftwares.minhalistadecompras.nucleo.estatico.VariaveisEstaticas;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.DialogConstrutor;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.GeradorCodigosUnicos;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.ManipuladorDataTempo;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;


public class ListaAnonimo extends Fragment {
    private ImageButton btnAddLista;
    private Resources resources;
    FirebaseDatabase database;
    DatabaseReference myRef;
    RvListaAnonimo rvListaComprasAdapter;
    private InterstitialAd interstitialAd;
    private boolean connected = false;

    //private OnFragmentInteractionListener mListener;

    public ListaAnonimo() {
        // Required empty public constructor
    }

    public static ListaAnonimo newInstance(boolean connected, InterstitialAd interstitialAd) {
        ListaAnonimo fragment = new ListaAnonimo();
        fragment.setConnected(connected);
        fragment.setInterstitialAd(interstitialAd);
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
       rvListaComprasAdapter = new RvListaAnonimo(getContext(),interstitialAd);
       rvMinhasListas.setAdapter(rvListaComprasAdapter);
       btnAddLista = v.findViewById(R.id.ibtnNovaLista);
       btnAddLista.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
                novaLista();
           }
       });
       return v;

    }

    private int setIconeLista(String nome) {
        if(nome.toLowerCase().contains("aniversario")||nome.toLowerCase().contains("niver")||nome.toLowerCase().contains("aniver"))
            return 4;
        else if(nome.toLowerCase().contains("churrasco")||nome.toLowerCase().contains("churras"))
            return 3;
        else if(nome.toLowerCase().contains("bolo"))
            return 5;
        else if(nome.toLowerCase().contains("cafe")||nome.toLowerCase().contains("café")||nome.toLowerCase().contains("lanche"))
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
        values.put("CriadorUid", VariaveisEstaticas.getUsuario().getUid());
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
    public void novaLista(){
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_add_lista, null);
        final EditText edNome = view.findViewById(R.id.edNome);
        final Spinner spPrivacidade = view.findViewById(R.id.spPrivacidade);
        spPrivacidade.setVisibility(View.GONE);
        Button btnAdd = view.findViewById(R.id.btnAdd);
        final DialogConstrutor dialogAddLista = new DialogConstrutor(getContext(),view,resources.getString(R.string.fragment_lista_dialog_add_lista_titulo),resources.getString(R.string.fragment_lista_dialog_add_lista_menssagem));
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ListaCompras lista = new ListaCompras();
                lista.setNome(edNome.getText().toString());
                lista.setCriadorUid(VariaveisEstaticas.getUsuario().getUid());
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
                VariaveisEstaticas.getListaCompras().add(lista);
                VariaveisEstaticas.getItemMap().put(lista.getuId(),new ArrayList<Item>());
                VariaveisEstaticas.getVisiveis().add(1);
                rvListaComprasAdapter.notifyDataSetChanged();
                addListaDB(lista);
                dialogAddLista.fechar();
            }
        });
    }
}
