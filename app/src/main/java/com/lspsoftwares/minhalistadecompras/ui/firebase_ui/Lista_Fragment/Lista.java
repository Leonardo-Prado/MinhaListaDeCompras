package com.lspsoftwares.minhalistadecompras.ui.firebase_ui.Lista_Fragment;

import android.content.ContentValues;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.firebase.database.ValueEventListener;
import com.lspsoftwares.minhalistadecompras.R;
import com.lspsoftwares.minhalistadecompras.db.DBGeneric;
import com.lspsoftwares.minhalistadecompras.nucleo.adapters.rv_lista_compras.RvListaComprasAdapter;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.Classificacao;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.Item;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.ListaCompras;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.Tag;
import com.lspsoftwares.minhalistadecompras.nucleo.estatico.VariaveisEstaticas;
import com.lspsoftwares.minhalistadecompras.nucleo.interfaces.AoIniciarAtividade;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.DialogConstrutor;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.GeradorCodigosUnicos;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.ManipuladorDataTempo;


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

    public static Lista newInstance(boolean connected,InterstitialAd interstitialAd) {
        Lista fragment = new Lista();
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
       rvListaComprasAdapter = new RvListaComprasAdapter(getContext(),interstitialAd);
       rvMinhasListas.setAdapter(rvListaComprasAdapter);
       rvListaComprasAdapter.addAtividadesObserver(new AoIniciarAtividade() {
           @Override
           public void iniciouAtividade() {
               if(fabAddLista.getVisibility()==View.VISIBLE)
                   fabAddLista.setVisibility(View.GONE);
           }
       });
       fabAddLista = v.findViewById(R.id.fabAddLista);
       fabAddLista.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
               View view = inflater.inflate(R.layout.dialog_add_lista, null);
               final EditText edNome = view.findViewById(R.id.edNome);
               final EditText edDescricao = view.findViewById(R.id.edDescricao);
               final Spinner spPrivacidade = view.findViewById(R.id.spPrivacidade);
               ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1);
               adapter.add("Publico");
               adapter.add("Privado");
               spPrivacidade.setAdapter(adapter);
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
                       if(spPrivacidade.getSelectedItem().toString().equals("Publico")){
                           List<String> tags = pegaTags(lista.getNome());
                           tags.add(lista.getNome().toLowerCase());
                           for (final String s:tags
                                ) {
                               myRef.child("Tags").child(s).addListenerForSingleValueEvent(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                       if (dataSnapshot.exists()) {
                                           Tag tag = dataSnapshot.getValue(Tag.class);
                                           Classificacao classificacao = new Classificacao();
                                           classificacao.setuId(lista.getuId());
                                           tag.getClassificacoes().add(classificacao);
                                           Map<String, Object> update = new HashMap<>();
                                           update.put(s, tag);
                                           myRef.child("Tags").updateChildren(update);
                                           lista.getTags().add(tag.getTag());
                                           update = new HashMap<>();
                                           update.put(lista.getuId(), lista);
                                           myRef.child("Lista").updateChildren(update);

                                       } else {
                                           Tag tag = new Tag();
                                           Classificacao classificacao = new Classificacao();
                                           classificacao.setuId(lista.getuId());
                                           tag.getClassificacoes().add(classificacao);
                                           tag.setTag(s);
                                           myRef.child("Tags").child(s).setValue(tag);
                                           lista.getTags().add(tag.getTag());
                                           Map<String, Object> update = new HashMap<>();
                                           update.put(lista.getuId(), lista);
                                           myRef.child("Lista").updateChildren(update);
                                       }
                                   }

                                   @Override
                                   public void onCancelled(@NonNull DatabaseError databaseError) {

                                   }
                               });
                           }
                       }
                       dialogAddLista.fechar();
                   }
               });
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

    private List<String> pegaTags(String nome) {
        List<String> tags = new ArrayList<>();
        String[] t = nome.split(" ");
        for (String s:t
             ) {
            if((!s.toLowerCase().equals("lista")&&!s.toLowerCase().equals("listas")&&!s.toLowerCase().equals("list"))&&!contemPreposicao(s))
                tags.add(s.toLowerCase());
        }
        return  tags;
    }

    private boolean contemPreposicao(String s) {
        boolean isPrep = false;
        String[] prep = {"a", "ante", "do","após", "até", "com", "contra", "de", "desde", "em", "entre", "para", "per", "perante", "por", "sem", "sob", "sobre","tras","para","da","de","do","e","pro","pra","no","na","em","ou","à","lá","lhe","afora", "como", "conforme", "consoante", "durante", "exceto", "mediante", "menos", "salvo", "segundo", "visto" ,"etc"};
        for(String a:prep){
            if(s.toLowerCase().equals(a)) {
                isPrep = true;
                break;
            }
        }
        return isPrep;
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
