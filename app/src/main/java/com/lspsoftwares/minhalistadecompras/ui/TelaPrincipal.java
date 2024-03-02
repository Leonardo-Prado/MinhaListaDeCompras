package com.lspsoftwares.minhalistadecompras.ui;

import static android.content.ContentValues.TAG;

import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lspsoftwares.minhalistadecompras.R;
import com.lspsoftwares.minhalistadecompras.ads.AppAdsView;
import com.lspsoftwares.minhalistadecompras.conn.ConnManager;
import com.lspsoftwares.minhalistadecompras.db.DBGeneric;
import com.lspsoftwares.minhalistadecompras.entidades.Compras;
import com.lspsoftwares.minhalistadecompras.entidades.Item;
import com.lspsoftwares.minhalistadecompras.entidades.ItemLista;
import com.lspsoftwares.minhalistadecompras.entidades.ListaCompras;
import com.lspsoftwares.minhalistadecompras.nucleo.estatico.VariaveisEstaticas;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.DialogConstrutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TelaPrincipal extends AppCompatActivity  {
    private boolean back = false;
    private FloatingActionButton fabAddLista;
    private  String PREFERENCIAS = "preferencias";
    FirebaseDatabase database;
    DatabaseReference myRef;
    ProgressBar pbCarregandoLista;
    Context context = this;
    boolean connected = false;
    DBGeneric db;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    private InterstitialAd mInterstitialAd;
    String intUId = "ca-app-pub-3525661211434624/8535571153";//"ca-app-pub-3940256099942544/1033173712"; ca-app-pub-3525661211434624/8535571153
    Resources resources;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);
        fabAddLista = findViewById(R.id.fabAddList);
        resources = context.getResources();
        //region Ads Initialization
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AppAdsView view = new AppAdsView((AdView) findViewById(R.id.adView));
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this,intUId, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });

        //endregion
        //region Check Connection
        ConnManager connManager = new ConnManager(getSystemService(Context.CONNECTIVITY_SERVICE));
        connected = connManager.isConnected();
        //endregion
        Toast.makeText(context,resources.getString(R.string.tela_principal_loading_toast),Toast.LENGTH_LONG).show();
        //region DB and Firebase Initialization
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        db = new DBGeneric(context);
        //endregion
        //region Toolbar Initialization
        Toolbar toolbar = findViewById(R.id.toolbar2);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            toolbar.getOverflowIcon().setTint(context.getResources().getColor(R.color.branco));
        setSupportActionBar(toolbar);
        //endregion
        prefs = getSharedPreferences(PREFERENCIAS, MODE_PRIVATE);
        editor = prefs.edit();
        VariaveisEstaticas.getInstance(context,database,myRef).getUsuario().setDownloadListaItem(prefs.getBoolean("downloadedItens", false));
        pbCarregandoLista = findViewById(R.id.pbCarregandoLista);
        carregaUsuario(this.getIntent());
        if(!VariaveisEstaticas.getInstance(context,database,myRef).getUsuario().isDownloadListaItem()&&connected)
            downloadListaItens();
        carregaListasUsuario();
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
            public void onCancelled(@NonNull DatabaseError databaseError) { }
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
                    Fragment fragmentLista = Lista.newInstance(connected, mInterstitialAd,fabAddLista);
                    openFragment(fragmentLista);
                    pbCarregandoLista.setVisibility(View.GONE);
                    VariaveisEstaticas.setAtualizado(true);
                    deletaNaoUsados();
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    Log.w("Falhou", "Failed to read value.", error.toException());
                    Fragment fragmentLista = Lista.newInstance(connected, mInterstitialAd,fabAddLista);
                    openFragment(fragmentLista);
                    pbCarregandoLista.setVisibility(View.GONE);
                    new DialogConstrutor(context,resources.getString(R.string.tela_principal_dialog_erro_buscar_dados_firebase_titulo),resources.getString(R.string.tela_principal_dialog_erro_buscar_dados_firebase_menssagem),resources.getString(R.string.tela_principal_dialog_erro_buscar_dados_firebase_menssagem));
                }
            });
        }else{
            pbCarregandoLista.setVisibility(View.GONE);
            if(!connected)
                new DialogConstrutor(context,resources.getString(R.string.tela_principal_dialog_offline_titulo),
                        resources.getString(R.string.tela_principal_dialog_offline_menssagem),
                        resources.getString(R.string.tela_principal_dialog_offline_pos_btn_txt)
                );
            Fragment fragmentLista = Lista.newInstance(connected, mInterstitialAd,fabAddLista);
            openFragment(fragmentLista);
        }
    }
    private void deletaNaoUsados() {
        List<List<String>> listas = db.buscar("Lista",new String[]{"_id"},"_idUsuario = ?",new String[]{VariaveisEstaticas.getUsuario().getUid()});
        for (List<String> l:listas) {
            if(!VariaveisEstaticas.getUsuario().getListas().contains(l.get(0)))
                db.deletar("Lista","_id = ?",new String[]{l.get(0)});
        }
    }
    private void carregaUsuario(Intent intent) {
        VariaveisEstaticas.getUsuario().setNome(intent.getStringExtra("email"));
        VariaveisEstaticas.getUsuario().setUid(intent.getStringExtra("uid"));
    }
    private void openFragment(Fragment fragment) {
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frmContainerFragment, fragment);
            if (back)
                transaction.addToBackStack(null);
            back = true;
            transaction.commit();
        }catch (Exception e){
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG);
        }
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
    }
}
