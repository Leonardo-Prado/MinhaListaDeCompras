package com.lspsoftwares.minhalistadecompras.nucleo.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lspsoftwares.minhalistadecompras.R;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.Item;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.ItemLista;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.ListaCompras;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.ManipuladorDataTempo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RvListaComprasItensAdapter extends RecyclerView.Adapter {
    Context context;
    List<Item> listaDeComprasItens;
    List<String> categorias;
    List<ItemLista> itemListas = new ArrayList<>();
    FirebaseDatabase database;
    DatabaseReference myRef;
    String listaUId;
    ListaCompras listaCompras;

    public RvListaComprasItensAdapter(Context context,List<Item> listaDeComprasItens,List<String> categorias,String listaUId,List<ItemLista> itemListas,ListaCompras listaCompras ) {
        this.context = context;
        this.listaDeComprasItens = listaDeComprasItens;
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        this.categorias = categorias;
        this.listaUId = listaUId;
        this.itemListas = itemListas;
        this.listaCompras = listaCompras;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.rv_itens_lista_de_compras,parent,false);
        ListaDeComprasItensViewHolder holder = new ListaDeComprasItensViewHolder(view,itemListas,myRef,listaUId,listaCompras);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final ListaDeComprasItensViewHolder viewHolder = (ListaDeComprasItensViewHolder) holder;
        viewHolder.position = position;
        viewHolder.tvNome.setText(listaDeComprasItens.get(position).getNome());
        viewHolder.tvDescricao.setText(listaDeComprasItens.get(position).getDescricao());
        viewHolder.tvCategoria.setText(categorias.get(Integer.parseInt(listaDeComprasItens.get(position).getCategoriaUid())));
        String unidades = trataUnidade(itemListas.get(position).getUnidade(),itemListas.get(position).getQuantidade());
        viewHolder.tvQuantidade.setText(itemListas.get(position).getQuantidade()+" " +unidades);
        Resources resources = context.getResources();
        TypedArray typedArray = resources.obtainTypedArray(R.array.imagemListCategorias);
        Drawable drawable = typedArray.getDrawable(Integer.parseInt(listaDeComprasItens.get(position).getCategoriaUid()));
        viewHolder.imvCategoriaIcon.setImageDrawable(drawable);
    }

    @Override
    public int getItemCount() {
        return listaDeComprasItens.size();
    }

    public String trataUnidade(String unidade,double quantidade){
        if(unidade==null)
            unidade = "unidades";
        if (quantidade != 1 && !unidade.equals("Kg"))
            unidade += "s";

        return unidade;
    }
}

class ListaDeComprasItensViewHolder extends RecyclerView.ViewHolder{
    TextView tvNome;
    TextView tvDescricao;
    TextView tvQuantidade;
    TextView tvCategoria;
    CheckBox cbConcluido;
    ImageView imvCategoriaIcon;
    ImageButton imbRemoveItem;
    int position;
    List<ItemLista> itemListas = new ArrayList<>();
    DatabaseReference myRef;
    String listaUId;
    ListaCompras listaCompras;

    public ListaDeComprasItensViewHolder(View view, final List<ItemLista> itemListas, final DatabaseReference myRef, final String listaUId, final ListaCompras listaCompras) {
        super(view);
        tvNome = view.findViewById(R.id.tvNome);
        tvDescricao = view.findViewById(R.id.tvDescricao);
        tvCategoria = view.findViewById(R.id.tvCategoria);
        tvCategoria.setVisibility(View.GONE);
        tvQuantidade = view.findViewById(R.id.tvQuantidade);
        cbConcluido = view.findViewById(R.id.cbConcluido);
        imvCategoriaIcon = view.findViewById(R.id.imvCategoriaIcon);
        imbRemoveItem = view.findViewById(R.id.imbRemoverItem);
        this.listaCompras = listaCompras;
        this.itemListas = itemListas;
        this.myRef = myRef;
        this.listaUId = listaUId;
        try {
            imbRemoveItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemListas.remove(position);
                    listaCompras.getItens().remove(position);
                    Map<String, Object> update = new HashMap<>();
                    update.put(listaCompras.getuId(), listaCompras);
                    myRef.child("Lista").updateChildren(update);
                }
            });
        }catch (Exception e){
            Log.e("Erro:",e.getMessage());
        }

    }
}