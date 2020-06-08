package com.lspsoftwares.minhalistadecompras.nucleo.adapters.lista_online_adapters;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.ManipuladorDataTempo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RVListaComprasOnlineAdapter extends RecyclerView.Adapter {
    Context context;
    FirebaseDatabase database;
    DatabaseReference myRef;
    Resources resources;
    List<ListaCompras> compras = new ArrayList<>();


    public RVListaComprasOnlineAdapter(Context context, List<ListaCompras> compras) {
        this.context = context;
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        this.compras = compras;
        resources = context.getResources();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.rv_lista_online_compras_layout, parent, false);
            ListaDeComprasOnlineViewHolder holder = new ListaDeComprasOnlineViewHolder(view, context);
            return holder;
        }catch(Exception e){
            Log.e("Error",e.getMessage());
            View view = new View(context);
            ListaDeComprasOnlineViewHolder holder = new ListaDeComprasOnlineViewHolder(view, context);
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListaDeComprasOnlineViewHolder viewHolder = (ListaDeComprasOnlineViewHolder) holder;
        viewHolder.listaCompras = compras.get(position);
        viewHolder.tvNome.setText(compras.get(position).getNome());
        viewHolder.tvDataCriacao.setText(resources.getString(R.string.rv_lista_de_compras_cv_tv_data_criacao)+"\n"+ ManipuladorDataTempo.dataIntToDataString(compras.get(position).getDataCriacao()));

        if(compras.get(position).getCriadorUid().equals(VariaveisEstaticas.getUsuario().getUid()))
            viewHolder.tvCriadoPor.setText(resources.getString(R.string.rv_lista_de_compras_cv_tv_criador) + "\n"+VariaveisEstaticas.getUsuario().getNome().split("@")[0]);
        else
            buscarCriador(compras.get(position).getCriadorUid(),viewHolder.tvCriadoPor);
        viewHolder.listaUid = compras.get(position).getuId();
        viewHolder.setPosicao(position);
        viewHolder.rbCalssificacao.setRating((float) compras.get(position).getVal());
        LayerDrawable stars = (LayerDrawable) viewHolder.rbCalssificacao.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(resources.getColor(R.color.estrela_completa), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(resources.getColor(R.color.estrela_meia), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(resources.getColor(R.color.estrela_vazia),PorterDuff.Mode.SRC_ATOP);
        viewHolder.rbCalssificacao.setProgressDrawable(stars);
        if(VariaveisEstaticas.getUsuario().getListasClassificadas()!=null) {
            double cl = 0;
            if (VariaveisEstaticas.getUsuario().getListasClassificadas().containsKey(compras.get(position).getuId())) {
                cl = Double.parseDouble(((Object)VariaveisEstaticas.getUsuario().getListasClassificadas().get(compras.get(position).getuId())).toString());
                viewHolder.rbCalssificacao.setRating((float) cl);
                viewHolder.rbCalssificacao.setEnabled(false);
            }
        }
        //Carrega icone da lista
        Resources resources = context.getResources();
        int id = compras.get(position).getIcon();
        TypedArray typedArray = resources.obtainTypedArray(R.array.imgTipoLista);
        Drawable drawable = typedArray.getDrawable(id);
        viewHolder.imvIconeLista.setImageDrawable(drawable);
    }

    private void buscarCriador(String criadorUid, final TextView tvCriadoPor) {
        Query query = myRef.child("Usuario").orderByChild("uid").equalTo(criadorUid);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        tvCriadoPor.setText(resources.getString(R.string.rv_lista_de_compras_cv_tv_criador)+"\n"+issue.child("nome").getValue().toString().split("@")[0]);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("error:",databaseError.getMessage()); }
        });

    }

    @Override
    public int getItemCount() {
        return compras.size();
    }
}
class ListaDeComprasOnlineViewHolder extends RecyclerView.ViewHolder{
    CardView cvLista;
    TextView tvNome;
    TextView tvDescricao;
    TextView tvDataCriacao;
    TextView tvCriadoPor;
    TextView tvListaTitulo;
    ImageView imvIconeLista;
    ImageButton imbAddMinhasListas;
    RatingBar rbCalssificacao;
    Button btnNovoItem;
    RecyclerView rvItens;
    ListaCompras listaCompras;
    String listaUid;
    DBGeneric db;
    private int posicao;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private Context context;
    private RVListaComprasOnlineItensAdapter rvItensAdapter;
    boolean visivel = false;
    public ListaDeComprasOnlineViewHolder(@NonNull View view, final Context context) {
        super(view);
        this.context = context;
        db = new DBGeneric(context);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        imbAddMinhasListas = view.findViewById(R.id.imbAddMinhasListas);
        tvNome = view.findViewById(R.id.tvNome);
        tvDescricao = view.findViewById(R.id.tvDescricao);
        tvDataCriacao = view.findViewById(R.id.tvDataCriacao);
        tvCriadoPor = view.findViewById(R.id.tvCriadaPor);
        tvListaTitulo = view.findViewById(R.id.tvListaTitulo);
        imvIconeLista = view.findViewById(R.id.imvIconeLista);
        cvLista = view.findViewById(R.id.cvLista);
        rbCalssificacao = view.findViewById(R.id.rbClassificacao);
        rvItens = view.findViewById(R.id.rvItens);
        if(listaCompras==null)
            listaCompras = new ListaCompras();
        rvItensAdapter = new RVListaComprasOnlineItensAdapter(context,listaCompras);
        rvItens.setAdapter(rvItensAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvItens.setLayoutManager(linearLayoutManager);
        rbCalssificacao.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if(fromUser) {
                    if(VariaveisEstaticas.getUsuario().getListasClassificadas()==null) {
                        HashMap<String, Double> cl = new HashMap<>();
                        cl.put(listaUid,(double)rating);
                        VariaveisEstaticas.getUsuario().setListasClassificadas(cl);
                    }else
                        VariaveisEstaticas.getUsuario().getListasClassificadas().put(listaUid, (double) rating);
                    Map<String, Object> update = new HashMap<>();
                    update.put(VariaveisEstaticas.getUsuario().getUid(), VariaveisEstaticas.getUsuario());
                    myRef.child("Usuario").updateChildren(update);
                    listaCompras.avaliar((double) rating);
                    update = new HashMap<>();
                    update.put(listaCompras.getuId(), listaCompras);
                    myRef.child("Lista").updateChildren(update);
                    Toast.makeText(context, context.getResources().getString(R.string.RVListaComprasOnlineAdapter_toast_classificou_lista), Toast.LENGTH_LONG).show();
                }
            }
        });
        imbAddMinhasListas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!VariaveisEstaticas.getUsuario().getListas().contains(listaUid)) {
                    VariaveisEstaticas.getUsuario().getListas().add(listaUid);
                    myRef.child("Lista").child(listaUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ListaCompras a = dataSnapshot.getValue(ListaCompras.class);
                            VariaveisEstaticas.getListaCompras().add(a);
                            final List<Item> items = new ArrayList<>();
                            for (ItemLista i : VariaveisEstaticas.getListaCompras().get(VariaveisEstaticas.getListaCompras().indexOf(a)).getItens()) {
                                myRef.child("Item").child(i.getItemUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Item item = dataSnapshot.getValue(Item.class);
                                        items.add(item);
                                        VariaveisEstaticas.getItemMap().put(listaUid,items);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                            VariaveisEstaticas.getVisiveis().add(1);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    Map<String, Object> update = new HashMap<>();
                    update.put(VariaveisEstaticas.getUsuario().getUid(), VariaveisEstaticas.getUsuario());
                    myRef.child("Usuario").updateChildren(update);
                    Toast.makeText(context, context.getResources().getString(R.string.RVListaComprasOnlineAdapter_toast_adicionou_lista), Toast.LENGTH_LONG).show();

                }
            }
        });
        cvLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Configuration config = context.getResources().getConfiguration();

                // Dont allow the default keyboard to show up
                if (config.keyboardHidden != Configuration.KEYBOARDHIDDEN_YES) {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(cvLista.getWindowToken(), 0);
                }
                if(visivel)
                    recolheLista();
                else
                    mostraLista();
                visivel = !visivel;

            }
        });
    }

    private void recolheLista() {
        rvItens.setVisibility(View.GONE);
        tvListaTitulo.setVisibility(View.GONE);
    }
    private void mostraLista() {
        tvListaTitulo.setVisibility(View.VISIBLE);
        rvItens.setVisibility(View.VISIBLE);
        rvItensAdapter = new RVListaComprasOnlineItensAdapter(context,listaCompras);
        rvItens.setAdapter(rvItensAdapter);
        rvItensAdapter.notifyDataSetChanged();
    }


    public int getPosicao() {
        return posicao;
    }

    public void setPosicao(int posicao) {
        this.posicao = posicao;
    }
}