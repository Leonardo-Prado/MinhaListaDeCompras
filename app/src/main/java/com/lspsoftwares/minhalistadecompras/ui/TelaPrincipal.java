package com.lspsoftwares.minhalistadecompras.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lspsoftwares.minhalistadecompras.R;
import com.lspsoftwares.minhalistadecompras.db.DBGeneric;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.Item;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.ListaCompras;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.Usuario;
import com.lspsoftwares.minhalistadecompras.nucleo.estatico.VariaveisEstaticas;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.DialogConstrutor;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.GeradorCodigosUnicos;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.ManipuladorDataTempo;

public class TelaPrincipal extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private boolean back = false;
    private  String PREFERENCIAS = "preferencias";
    FirebaseDatabase database;
    DatabaseReference myRef;
    Usuario user = new Usuario();
    List<ListaCompras> listas = new ArrayList<>();
    ProgressBar pbCarregandoLista;
    Context context = this;
    DBGeneric db;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    VariaveisEstaticas variaveisEstaticas;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        BottomNavigationView bnvMenuInferior = findViewById(R.id.bnvMenuInferior);
        bnvMenuInferior.setOnNavigationItemSelectedListener(this);
        db = new DBGeneric(context);
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        prefs = getSharedPreferences(PREFERENCIAS, MODE_PRIVATE);
        editor = prefs.edit();
        VariaveisEstaticas.getInstance().getUsuario().setDownloadListaItem(prefs.getBoolean("downloadedItens", false));
        pbCarregandoLista = findViewById(R.id.pbCarregandoLista);
        carregaUsuario(VariaveisEstaticas.getInstance().getUsuario(),this.getIntent());
        if(!VariaveisEstaticas.getInstance().getUsuario().isDownloadListaItem())
            downloadListaItens();
        carregaListasUsuario(VariaveisEstaticas.getInstance().getUsuario());
    }


    private void downloadListaItens() {
        db = new DBGeneric(this);
        Query query = myRef.child("Item").orderByChild("interna").equalTo(true);
        query.addValueEventListener(new ValueEventListener() {
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
                            db.inserir(values, "Item");
                            k++;
                            if (k == i - 1) {

                                myRef.child("Usuario").child(user.getUid()).setValue(user);
                                editor.putBoolean("downloadedItens", true);
                                editor.commit();
                                boolean b = prefs.getBoolean("downloadedItens", false);
                                user.setDownloadListaItem( b);
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

    private void carregaListasUsuario(final Usuario user) {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                listas = new ArrayList<>();
                List<String> strings = (List<String>)dataSnapshot.child("Usuario").child(user.getUid()).child("listas").getValue();
                if(!(strings==null)) {
                    user.getListas().clear();
                    for (String s : strings
                    ) {
                        listas.add(dataSnapshot.child("Lista").child(s).getValue(ListaCompras.class));
                        user.getListas().add(s);
                    }
                }
                Fragment fragmentLista = Lista.newInstance(user,listas);
                openFragment(fragmentLista);
                pbCarregandoLista.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Falhou", "Failed to read value.", error.toException());
                listas = new ArrayList<>();
                Fragment fragmentLista = Lista.newInstance(user,listas);
                openFragment(fragmentLista);
                pbCarregandoLista.setVisibility(View.GONE);
            }
        });
    }

    private void carregaUsuario(Usuario user, Intent intent) {
        user.setNome(intent.getStringExtra("email"));
        user.setUid(intent.getStringExtra("uid"));
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
                break;
            }
            case R.id.menu_item_dispensa: {
                break;
            }
          /*  case R.id.menu_novo_item: {

                LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.dialog_novo_item, null);
                final EditText edItem = view.findViewById(R.id.edItem);
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
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
