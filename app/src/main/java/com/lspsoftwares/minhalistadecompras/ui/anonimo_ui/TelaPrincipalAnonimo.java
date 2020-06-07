package com.lspsoftwares.minhalistadecompras.ui.anonimo_ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lspsoftwares.minhalistadecompras.R;
import com.lspsoftwares.minhalistadecompras.db.DBGeneric;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.Item;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.ItemLista;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.ListaCompras;
import com.lspsoftwares.minhalistadecompras.nucleo.estatico.VariaveisEstaticas;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.DialogConstrutor;
import com.lspsoftwares.minhalistadecompras.ui.Login;
import com.lspsoftwares.minhalistadecompras.ui.firebase_ui.Lista_Fragment.Lista;
import com.lspsoftwares.minhalistadecompras.ui.firebase_ui.Lista_Fragment.ListaDesconn;

import java.util.ArrayList;
import java.util.List;

public class TelaPrincipalAnonimo extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
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
        setContentView(R.layout.activity_tela_principal_anonimo);
        resources = getResources();
        Toast.makeText(context,resources.getString(R.string.tela_principal_loading_toast),Toast.LENGTH_LONG).show();
        pbCarregandoLista = findViewById(R.id.pbCarregandoLista);
        pbCarregandoLista.setVisibility(View.VISIBLE);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId(intUId);
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
            connected = true;
        else
            connected = false;
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
        carregaUsuario(this.getIntent());
        carregaItens();
        carregaListasAnonimos();
        carregaTelaLista();
        pbCarregandoLista.setVisibility(View.GONE);
    }

    private void carregaUsuario(Intent intent) {
        VariaveisEstaticas.getInstance(context,database,myRef);
        VariaveisEstaticas.getUsuario().setNome(intent.getStringExtra("email"));
        VariaveisEstaticas.getUsuario().setUid(intent.getStringExtra("uid"));
    }
    private void carregaItens() {
        List<List<String>> itens = db.buscar("Item",new String[]{"_id","CategoriaUid","DataCriacao","Descricao","HoraCriacao","Nome","Quantidade","Unidade","Sync"},"Nome ASC");
        final List<String> nomesItens = new ArrayList<>();
        if(itens.size()>100){
            for (List<String> strings:itens) {
                Item item = strings2Item(strings);
                VariaveisEstaticas.getItens().add(item);
                nomesItens.add(item.getNome());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_dropdown_item_1line,nomesItens);
            VariaveisEstaticas.setItensAdapter(adapter);
        }else{
            if(connected){
                Query query = myRef.child("Item").orderByChild("interna").equalTo(true);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            if (dataSnapshot.exists()) {
                                for (DataSnapshot issue : dataSnapshot.getChildren()) {
                                    Item item = issue.getValue(Item.class);
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
                                    try { db.inserir(values, "Item");}catch (Exception e){}
                                    VariaveisEstaticas.getItens().add(item);
                                    nomesItens.add(item.getNome());
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_dropdown_item_1line,nomesItens);
                                VariaveisEstaticas.setItensAdapter(adapter);
                            }
                        }catch (Exception e){
                            Log.e("Error",e.getMessage());
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }
        }
    }

    private void carregaListasAnonimos() {
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
                if(!VariaveisEstaticas.getListaCompras().contains(lcompra)) {
                    VariaveisEstaticas.getListaCompras().add(lcompra);
                    VariaveisEstaticas.getVisiveis().add(0);
                    VariaveisEstaticas.getItemMap().put(lcompra.getuId(), listItens);
                }
            }
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

    private void carregaTelaLista() {
        Fragment fragmentLista;
        if(connected)
            fragmentLista = ListaAnonimo.newInstance(connected, interstitialAd);
        else
            fragmentLista = ListaDesconn.newInstance(connected);
        openFragment(fragmentLista);
        pbCarregandoLista.setVisibility(View.GONE);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.getItem(2).setVisible(false);
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
            case R.id.imLogIn:{
                VariaveisEstaticas.reset();
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_home: {
                Fragment fragmentLista;
                if (connected)
                    fragmentLista = Lista.newInstance(connected, interstitialAd);
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
                new DialogConstrutor(context, resources.getString(R.string.tela_principal_dialog_anonimo_titulo), resources.getString(R.string.tela_principal_dialog_anonimo_menssagem), resources.getString(R.string.tela_principal_dialog_anonimo_pos_btn_txt));
                break;
            }
        }
        return  true;
    }




}
