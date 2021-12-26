package com.lspsoftwares.minhalistadecompras.ui.firebase_ui;

import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lspsoftwares.minhalistadecompras.R;
import com.lspsoftwares.minhalistadecompras.db.DBGeneric;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.Compras;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.Item;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.ItemLista;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.ListaCompras;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.Usuario;
import com.lspsoftwares.minhalistadecompras.nucleo.estatico.VariaveisEstaticas;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.DialogConstrutor;
import com.lspsoftwares.minhalistadecompras.ui.Login;
import com.lspsoftwares.minhalistadecompras.ui.firebase_ui.Lista_Fragment.Lista;
import com.lspsoftwares.minhalistadecompras.ui.firebase_ui.Lista_Fragment.ListaDesconn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelaPrincipal extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    //Todo: Garantir numero certo de itens em compras
    //Todo:

    private boolean back = false;
    private  String PREFERENCIAS = "preferencias";
    FirebaseDatabase database;
    DatabaseReference myRef;
    ProgressBar pbCarregandoLista;
    Context context = this;
    boolean connected = false;
    DBGeneric db;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    private InterstitialAd interstitialAd;
    String intUId = "ca-app-pub-3525661211434624/8535571153";//"ca-app-pub-3940256099942544/1033173712"; ca-app-pub-3525661211434624/8535571153
    Resources resources;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);
        resources = context.getResources();
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        /*interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId(intUId);
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });*/

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
            connected = true;
        else
            connected = false;
        Toast.makeText(context,resources.getString(R.string.tela_principal_loading_toast),Toast.LENGTH_LONG).show();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        BottomNavigationView bnvMenuInferior = findViewById(R.id.bnvMenuInferior);
        bnvMenuInferior.setOnNavigationItemSelectedListener(this);
        db = new DBGeneric(context);
        Toolbar toolbar = findViewById(R.id.toolbar2);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            toolbar.getOverflowIcon().setTint(context.getResources().getColor(R.color.branco));
        setSupportActionBar(toolbar);
        prefs = getSharedPreferences(PREFERENCIAS, MODE_PRIVATE);
        editor = prefs.edit();
        VariaveisEstaticas.getInstance(context,database,myRef).getUsuario().setDownloadListaItem(prefs.getBoolean("downloadedItens", false));
        pbCarregandoLista = findViewById(R.id.pbCarregandoLista);
        carregaUsuario(this.getIntent());
        if(!VariaveisEstaticas.getInstance(context,database,myRef).getUsuario().isDownloadListaItem()&&connected)
            downloadListaItens();
        if(connected)
            syncLista();
        carregaListasUsuario();
        //carregaListasAnonimos();
    }


    //Sincroniza as listas que foram geradas offline
    private void syncLista() {
        List<List<String>> naoSync = db.buscar("Lista",new String[]{"_id","Nome","DataCriacao","HoraCriacao","Sync"},"_idUsuario = ?",new String[]{VariaveisEstaticas.getUsuario().getUid()});
        final Usuario user = new Usuario();
        if(naoSync.size()>0){
            for (List<String> lista:naoSync) {
                if (lista.get(4).equals("0")) {
                    ListaCompras listaCompras = new ListaCompras();
                    listaCompras.setuId(lista.get(0));
                    listaCompras.setNome(lista.get(1));
                    try {
                        listaCompras.setDataCriacao(Long.parseLong(lista.get(2)));
                        listaCompras.setHoraCriacao(Long.parseLong(lista.get(3)));
                        listaCompras.setCriadorUid(VariaveisEstaticas.getUsuario().getUid());
                    } catch (Exception e) {
                        Log.e("Erro", e.getMessage());
                    }
                    List<List<String>> naoSyncItem = db.buscar("ItemLista",new String[]{"_IdItem","Quantidade","Unidade,_id"},"_IdLista = ? AND Sync = ? ",new String[]{lista.get(0),"0"});
                    if(naoSyncItem.size()>0) {
                        List<ItemLista> itensLista = new ArrayList<>();
                        for (List<String> l:naoSyncItem
                             ) {
                            ItemLista itemLista = new ItemLista();
                            itemLista.setItemUid(l.get(0));
                            itemLista.setQuantidade(Double.parseDouble(l.get(1)));
                            itemLista.setUnidade(l.get(2));
                            itensLista.add(itemLista);
                            ContentValues v = new ContentValues();
                            v.put("Sync",1);
                            db.atualizar(v,"ItemLista","_id = ?",new String[]{l.get(3)});
                        }
                        listaCompras.setItens(itensLista);
                    }
                    user.getListas().add(listaCompras.getuId());
                    myRef.child("Lista").child(listaCompras.getuId()).setValue(listaCompras);
                    ContentValues values = new ContentValues();
                    values.put("Sync", 1);
                    db.atualizar(values, "Lista", "_id = ?", new String[]{lista.get(0)});
                }
            }
            myRef.child("Usuario").child(VariaveisEstaticas.getUsuario().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        Usuario usuario = dataSnapshot.getValue(Usuario.class);
                        for (String s : user.getListas()
                        ) {
                            usuario.getListas().add(s);
                        }
                        Map<String, Object> update = new HashMap<>();
                        update.put(usuario.getUid(), usuario);
                        myRef.child("Usuario").updateChildren(update);
                    }catch (Exception ex){}
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    }


    private void downloadListaItens() {
        db = new DBGeneric(this);
        Query query = myRef.child("Item").orderByChild("interna").equalTo(true);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    if (dataSnapshot.exists()) {
                        long i = dataSnapshot.getChildrenCount();
                        long k = 0;
                        for (DataSnapshot issue : dataSnapshot.getChildren()) {
                            Item item = issue.getValue(Item.class);
                            item.isInterna();
                            ContentValues values = new ContentValues();
                            values.put("_id", item.getuId());
                            values.put("CategoriaUid", item.getCategoriaUid());
                            values.put("DataCriacao", item.getDataCriacao());
                            values.put("Descricao", item.getDescricao());
                            values.put("HoraCriacao", item.getHoraCriacao());
                            values.put("Nome", item.getNome());
                            values.put("Quantidade", item.getQuantidade());
                            values.put("Unidade", item.getUnidade());
                            values.put("Sync",1);
                            db.inserir(values, "Item");
                            k++;
                            if (k == i - 1) {
                                try {
                                    if (myRef == null)
                                        myRef = database.getReference();
                                    myRef.child("Usuario").child(VariaveisEstaticas.getUsuario().getUid()).setValue(VariaveisEstaticas.getUsuario());
                                    if (editor == null)
                                        editor = prefs.edit();
                                    editor.putBoolean("downloadedItens", true);
                                    editor.commit();
                                    boolean b = prefs.getBoolean("downloadedItens", false);
                                    VariaveisEstaticas.getUsuario().setDownloadListaItem(b);
                                }catch (Exception e){}
                            }
                        }
                    }
                }catch (Exception e){
                    Log.e("Error",e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void carregaListasUsuario() {
        if(!VariaveisEstaticas.isAtualizado()&&connected) {
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String userUid = VariaveisEstaticas.getUsuario().getUid();
                    for (DataSnapshot snap:dataSnapshot.child("Usuario").child(userUid).child("compras").getChildren()) {
                        Compras compras = snap.getValue(Compras.class);
                        VariaveisEstaticas.getUsuario().getCompras().add(compras);
                    }
                    List<String> strings = (List<String>) dataSnapshot.child("Usuario").child(VariaveisEstaticas.getUsuario().getUid()).child("listas").getValue();
                    if (!(strings == null)) {
                        VariaveisEstaticas.getUsuario().getListas().clear();
                        VariaveisEstaticas.getListaCompras().clear();
                        int a = 0;
                        for (String s : strings
                        ) {
                            VariaveisEstaticas.getUsuario().getListas().add(s);
                            //Torna o ultimo item visivel
                            if(a<strings.size()-1)
                                VariaveisEstaticas.getVisiveis().add(0);
                            else
                                VariaveisEstaticas.getVisiveis().add(1);

                            VariaveisEstaticas.getListaCompras().add(dataSnapshot.child("Lista").child(s).getValue(ListaCompras.class));
                            List<Item> items = new ArrayList<>();
                            for (ItemLista i : VariaveisEstaticas.getListaCompras().get(a).getItens()) {
                                if(i!=null) {
                                    try {
                                        items.add(dataSnapshot.child("Item").child(i.getItemUid()).getValue(Item.class));
                                    } catch (Exception e) {
                                        Log.e("Error", e.getMessage());
                                    }
                                }
                            }
                            VariaveisEstaticas.getItemMap().put(s, items);
                            a++;
                        }
                    }
                    HashMap<String,Double> cl = (HashMap<String,Double>) dataSnapshot.child("Usuario").child(VariaveisEstaticas.getUsuario().getUid()).child("listasClassificadas").getValue();
                    VariaveisEstaticas.getUsuario().setListasClassificadas(cl);
                    Fragment fragmentLista = Lista.newInstance(connected,interstitialAd);
                    openFragment(fragmentLista);
                    pbCarregandoLista.setVisibility(View.GONE);
                    VariaveisEstaticas.setAtualizado(true);
                    deletaNaoUsados();
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w("Falhou", "Failed to read value.", error.toException());
                    Fragment fragmentLista = Lista.newInstance(connected,interstitialAd);
                    openFragment(fragmentLista);
                    pbCarregandoLista.setVisibility(View.GONE);
                    new DialogConstrutor(context,resources.getString(R.string.tela_principal_dialog_erro_buscar_dados_firebase_titulo),resources.getString(R.string.tela_principal_dialog_erro_buscar_dados_firebase_menssagem),resources.getString(R.string.tela_principal_dialog_erro_buscar_dados_firebase_menssagem));
                    if(!VariaveisEstaticas.isCarregaOFFLine())
                        carregaOffLine();
                }
            });
        }else{
            Fragment fragmentLista = ListaDesconn.newInstance(connected);
            openFragment(fragmentLista);
            pbCarregandoLista.setVisibility(View.GONE);
            if(!VariaveisEstaticas.isCarregaOFFLine())
                carregaOffLine();
            if(!connected)
                new DialogConstrutor(context,resources.getString(R.string.tela_principal_dialog_offline_titulo),resources.getString(R.string.tela_principal_dialog_offline_menssagem),resources.getString(R.string.tela_principal_dialog_offline_pos_btn_txt));
        }
    }


    private void deletaNaoUsados() {
        List<List<String>> listas = db.buscar("Lista",new String[]{"_id"},"_idUsuario = ?",new String[]{VariaveisEstaticas.getUsuario().getUid()});
        for (List<String> l:listas) {
            if(!VariaveisEstaticas.getUsuario().getListas().contains(l.get(0)))
                db.deletar("Lista","_id = ?",new String[]{l.get(0)});
        }
    }

    private void carregaOffLine() {
        List<List<String>> naoSync = db.buscar("Lista",new String[]{"_id","Nome","DataCriacao","HoraCriacao","Sync"},"_idUsuario = ?",new String[]{VariaveisEstaticas.getUsuario().getUid()});
        if(naoSync.size()>0) {
        for (List<String> lista : naoSync) {
                ListaCompras listaCompras = new ListaCompras();
                listaCompras.setuId(lista.get(0));
                listaCompras.setNome(lista.get(1));
                listaCompras.setDataCriacao(Long.parseLong(lista.get(2)));
                listaCompras.setHoraCriacao(Long.parseLong(lista.get(3)));
                listaCompras.setCriadorUid(VariaveisEstaticas.getUsuario().getUid());
                List<List<String>> naoSyncItem = db.buscar("ItemLista", new String[]{"_IdItem", "Quantidade", "Unidade,_id"}, "_IdLista = ? ", new String[]{lista.get(0)});
                if (naoSyncItem.size() > 0) {
                    List<ItemLista> itensLista = new ArrayList<>();
                    for (List<String> l : naoSyncItem) {
                        ItemLista itemLista = new ItemLista();
                        itemLista.setItemUid(l.get(0));
                        itemLista.setQuantidade(Double.parseDouble(l.get(1)));
                        itemLista.setUnidade(l.get(2));
                        itensLista.add(itemLista);
                        List<List<String>> itens = db.buscar("Item", new String[]{"Nome","CategoriaUid", "DataCriacao", "Descricao","HoraCriacao"}, "_Id = ? ", new String[]{l.get(0)});
                        Item item = new Item();
                        if(itens.size()>0){
                            List<String> i = itens.get(0);
                            item.setuId(l.get(0));
                            item.setNome(i.get(0));
                            item.setCategoriaUid(i.get(1));
                            item.setDataCriacao(Long.parseLong(i.get(2)));
                            item.setDescricao(i.get(3));
                            item.setHoraCriacao(Long.parseLong(i.get(4)));
                        }
                        if(VariaveisEstaticas.getItemMap().containsKey(lista.get(0)))
                            VariaveisEstaticas.getItemMap().get(lista.get(0)).add(item);
                        else{
                            VariaveisEstaticas.getItemMap().put(lista.get(0),new ArrayList<Item>());
                            VariaveisEstaticas.getItemMap().get(lista.get(0)).add(item);
                        }
                    }
                    listaCompras.setItens(itensLista);
                }
                VariaveisEstaticas.getUsuario().getListas().add(listaCompras.getuId());
                VariaveisEstaticas.getListaCompras().add(listaCompras);
                VariaveisEstaticas.getVisiveis().add(0);
            }
        }
        VariaveisEstaticas.setCarregaOFFLine(true);
    }

    private void carregaUsuario(Intent intent) {
        VariaveisEstaticas.getUsuario().setNome(intent.getStringExtra("email"));
        VariaveisEstaticas.getUsuario().setUid(intent.getStringExtra("uid"));
    }

    private void carregaListasAnonimos() {
        boolean alterou = false;
        DBGeneric db = new DBGeneric(context);
        List<List<String>> listas = db.buscar("Lista",new String[]{"_id", "_idUsuario","Nome","CriadorUid","DataCriacao","HoraCriacao","UIdCategoriaLista","Sync"},"CriadorUid = ?",new String[]{"AAAAAAAAAAAAAAAAAAAAAAAAAAAA"});
        if(listas!=null&&listas.size()>0){
            for (List<String> strings: listas) {
                ListaCompras lcompra = strings2Lista(strings);
                List<List<String>> itens = db.buscar("ItemLista",new String[]{"_id", "_IdLista","_IdItem","Quantidade","Unidade","Sync"},"_IdLista = ?",new String[]{lcompra.getuId()});
                if(itens.size()>0){
                    List<Item> itemList = new ArrayList<>();
                    for (List<String> s:itens) {
                        ItemLista itemLista = strings2ItemLista(s);
                        lcompra.getItens().add(itemLista);
                    }
                }
                List<Item> listItens =  new ArrayList<>();
                for (ItemLista i:lcompra.getItens()) {
                    Item item = VariaveisEstaticas.getItemByItemId(i.getItemUid());
                    listItens.add(item);
                }
                lcompra.setCriadorUid(VariaveisEstaticas.getUsuario().getUid());
                if(!VariaveisEstaticas.getListaCompras().contains(lcompra)) {
                    VariaveisEstaticas.getListaCompras().add(lcompra);
                    myRef.child("Lista").child(lcompra.getuId()).setValue(lcompra);
                    VariaveisEstaticas.getVisiveis().add(0);
                    VariaveisEstaticas.getItemMap().put(lcompra.getuId(), listItens);
                    VariaveisEstaticas.getUsuario().getListas().add(lcompra.getuId());
                    alterou = true;
                }

            }
        }
        if(alterou) {
            Map<String, Object> update = new HashMap<>();
            update.put(VariaveisEstaticas.getUsuario().getUid(), VariaveisEstaticas.getUsuario());
            myRef.child("Usuario").updateChildren(update);
        }
    }

    private Item strings2Item(List<String> s){
        Item item = new Item();
        item.setuId(s.get(0));
        item.setCategoriaUid(s.get(1));
        item.setDataCriacao(Long.parseLong(s.get(2)));
        item.setDescricao(s.get(3));
        item.setHoraCriacao(Long.parseLong(s.get(4)));
        item.setNome(s.get(5));
        item.setQuantidade(Double.parseDouble(s.get(6)));
        item.setUnidade(s.get(7));
        return item;
    }

    private ListaCompras strings2Lista(List<String> strings) {
        ListaCompras listaCompras = new ListaCompras();
        listaCompras.setuId(strings.get(0));
        listaCompras.setNome(strings.get(2));
        listaCompras.setCriadorUid(strings.get(3));
        listaCompras.setDataCriacao(Long.parseLong(strings.get(4)));
        return listaCompras;
    }

    private ItemLista strings2ItemLista(List<String> s) {
        ItemLista itemLista = new ItemLista();
        itemLista.setItemUid(s.get(2));
        itemLista.setUnidade(s.get(4));
        itemLista.setQuantidade(Double.parseDouble(s.get(3)));
        return itemLista;
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frmContainerFragment, fragment);
        if(back)
            transaction.addToBackStack(null);
        back = true;
        transaction.commit();
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_home: {
                Fragment fragmentLista;
                if(connected)
                    fragmentLista = Lista.newInstance(connected,interstitialAd);
                else
                    fragmentLista = ListaDesconn.newInstance(connected);
                openFragment(fragmentLista);
                pbCarregandoLista.setVisibility(View.GONE);
                VariaveisEstaticas.setAtualizado(true);
                break;
            }
            case R.id.menu_item_dispensa: {
                break;
            }
            case R.id.menu_buscar_lista: {
                if(!connected)
                    new DialogConstrutor(context,resources.getString(R.string.tela_principal_dialog_offline_titulo),resources.getString(R.string.tela_principal_dialog_offline_menssagem),resources.getString(R.string.tela_principal_dialog_offline_pos_btn_txt));
                else {
                    Fragment fragmentListaOnLine = Online_lista.newInstance();
                    openFragment(fragmentListaOnLine);
                }
                break;
            }

          /*case R.id.menu_novo_item: {

                LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.dialog_novo_item, null);
                final EditText edItem = view.findViewById(R.id.acItem);
                final EditText edDescricao = view.findViewById(R.id.edDescricao);
                final EditText edQuantidade = view.findViewById(R.id.edQuantidade);
                ArrayAdapter<String> unidadesAdapter = new ArrayAdapter<>(context,   android.R.layout.simple_list_item_1);
                String[] unidadesArray = context.getResources().getStringArray(R.array.unidades);
                for (int i = 0; i <unidadesArray.length; i++) {
                    unidadesAdapter.add(unidadesArray[i]);
                }
                final Spinner spUnidades = view.findViewById(R.id.spUnidade);
                spUnidades.setAdapter(unidadesAdapter);
                ArrayAdapter<String> categoriaAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
                String[] categoriasArray = context.getResources().getStringArray(R.array.categorias);
                for (int i = 0; i <categoriasArray.length; i++) {
                    categoriaAdapter.add(categoriasArray[i]);
                }
                final Spinner spCategoria = view.findViewById(R.id.spCategoria);
                spCategoria.setAdapter(categoriaAdapter);
                Button btnAddNovoItem = view.findViewById(R.id.btnAddNovoItem);
                final DialogConstrutor dialogConstrutor = new DialogConstrutor(context,view,"Adicione um Novo Item","Adicione um novo item a sua lista de compras");
                btnAddNovoItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!edQuantidade.getText().toString().isEmpty()&&!edItem.getText().toString().isEmpty()){
                            final Item item = new Item();
                            item.setNome(edItem.getText().toString());
                            item.setDescricao(edDescricao.getText().toString());
                            item.setQuantidade(Double.parseDouble(edQuantidade.getText().toString()));
                            item.setUnidade(spUnidades.getSelectedItem().toString());
                            item.setInterna(true);
                            GeradorCodigosUnicos gcu = new GeradorCodigosUnicos(16);
                            item.setuId(gcu.gerarCodigos());
                            try {
                                ManipuladorDataTempo dataTempo = new ManipuladorDataTempo(new Date());
                                item.setDataCriacao(dataTempo.getDataInt());
                                item.setHoraCriacao(dataTempo.getTempoInt());
                                item.setCriadorUid("LkQeedCs8mgF1m8MTe8KdLpWYZG3");
                            } catch (ParseException e) {
                                e.printStackTrace();
                            } catch (java.text.ParseException e) {
                                e.printStackTrace();
                            }
                            Query query = myRef.child("Categorias").orderByChild("categoria").equalTo(spCategoria.getSelectedItem().toString());
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {

                                        for (DataSnapshot issue : dataSnapshot.getChildren()) {
                                            item.setCategoriaUid(issue.getKey());
                                        }
                                        myRef.child("Item").child(item.getuId()).setValue(item);
                                        Map<String,Object> update = new HashMap<>();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e("error:",databaseError.getMessage());
                                }
                            });
                        }else{
                            new DialogConstrutor(context,"Erro, campo vazio!","Os campos nome do item e quantidades devem ser preenchidos para se adicionar um novo item!","OK");
                            dialogConstrutor.fechar();
                        }
                    }
                });


                break;
            }*/
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.getItem(1).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            // case R.id.imConfiguracoes:{
            //     break;
            // }
            case R.id.imSobre:{
                final LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.dialog_sobre, null);
                TextView tvVersao = view.findViewById(R.id.tvVersao);
                String versionName = " ";
                try {
                    versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                tvVersao.setText(tvVersao.getText().toString() + " " + versionName);
                new DialogConstrutor(context,view);
                break;
            }
            case R.id.imLogOut:{
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(context, Login.class);
                startActivity(intent);
                finish();
                break;
            }
            case R.id.imSair:{
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FragmentManager fm = getFragmentManager();
 /*       if(fm.getBackStackEntryCount()==0)
            finish();*/
    }
}
