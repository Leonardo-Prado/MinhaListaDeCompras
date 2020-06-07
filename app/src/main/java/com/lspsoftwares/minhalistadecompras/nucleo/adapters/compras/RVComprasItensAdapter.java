package com.lspsoftwares.minhalistadecompras.nucleo.adapters.compras;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.lspsoftwares.minhalistadecompras.db.DBGeneric;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.Item;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.ItemLista;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.ListaCompras;
import com.lspsoftwares.minhalistadecompras.nucleo.estatico.VariaveisEstaticas;

import java.util.ArrayList;
import java.util.List;

public class RVComprasItensAdapter extends RecyclerView.Adapter {
    Context context;
    FirebaseDatabase database;
    DatabaseReference myRef;
    String listaUId;
    ListaCompras listaCompras;

    public RVComprasItensAdapter(Context context,ListaCompras listaCompras) {
        this.context = context;
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        this.listaUId = listaCompras.getuId();
        this.listaCompras = listaCompras;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.rv_itens_online_lista_de_compras,parent,false);
        ComprasItensViewHolder holder = new ComprasItensViewHolder(view,myRef,listaUId);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final ComprasItensViewHolder viewHolder = (ComprasItensViewHolder) holder;
        DBGeneric db = new DBGeneric(context);
        String uid = " ";
        String nome = " ";
        List<String> item = new ArrayList<>();
        try {
           uid = listaCompras.getItens().get(position).getItemUid();
           item = db.buscar("Item",new String[]{"Nome","CategoriaUid"},"_id = ?",new String[]{uid}).get(0);
           nome = item.get(0);
        }catch (Exception e){
            myRef.child("Item").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Item i = dataSnapshot.getValue(Item.class);
                    viewHolder.tvNome.setText(i.getNome());
                    Resources resources = context.getResources();
                    TypedArray typedArray = resources.obtainTypedArray(R.array.imagemListCategorias);
                    int id = drawableFind(i.getNome(),Integer.parseInt(i.getCategoriaUid()));
                    Drawable drawable = typedArray.getDrawable(id);
                    viewHolder.imvCategoriaIcon.setImageDrawable(drawable);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        String unidades = trataUnidade(listaCompras.getItens().get(position).getUnidade(), listaCompras.getItens().get(position).getQuantidade());
        viewHolder.tvQuantidade.setText(listaCompras.getItens().get(position).getQuantidade() + " " + unidades);
        if(item.size()>0) {
           viewHolder.tvNome.setText(item.get(0));
           Resources resources = context.getResources();
           int id = drawableFind(nome,Integer.parseInt(item.get(1)));
           TypedArray typedArray = resources.obtainTypedArray(R.array.imagemListCategorias);
           Drawable drawable = typedArray.getDrawable(id);
           viewHolder.imvCategoriaIcon.setImageDrawable(drawable);
        }
        viewHolder.position = position;
    }

    @Override
    public int getItemCount() {
        return  listaCompras.getItens().size();
    }

    public String trataUnidade(String unidade,double quantidade){
        if(unidade==null)
            unidade = "unidades";
        if (quantidade != 1 && !unidade.equals("Kg"))
            unidade += "s";

        return unidade;
    }
    private int drawableFind(String nome,int defDraw) {
        if(nome.toLowerCase().contains("frango"))
            return 15;
        else if (nome.toLowerCase().equals("banana"))
            return 16;
        else if (nome.toLowerCase().equals("maça")||nome.toLowerCase().equals("maçã"))
            return 17;
        else if (nome.toLowerCase().equals("laranja"))
            return 18;
        else if (nome.toLowerCase().contains("refrigerante"))
            return 19;
        else if (nome.toLowerCase().equals("uva"))
            return 20;
        else if (nome.toLowerCase().equals("cafe")||nome.toLowerCase().equals("café"))
            return 21;
        else if (nome.toLowerCase().contains("bolo"))
            return 22;
        else if (nome.toLowerCase().contains("queijo"))
            return 23;
        else
            return defDraw;
    }
}

class ComprasItensViewHolder extends RecyclerView.ViewHolder{
    TextView tvNome;
    TextView tvQuantidade;
    ImageView imvCategoriaIcon;
    int position;
    List<ItemLista> itemListas = new ArrayList<>();
    DatabaseReference myRef;
    String listaUId;
    ListaCompras listaCompras;

    public ComprasItensViewHolder(View view, final DatabaseReference myRef, final String listaUId) {
        super(view);
        tvNome = view.findViewById(R.id.tvNome);
        tvQuantidade = view.findViewById(R.id.tvQuantidade);
        imvCategoriaIcon = view.findViewById(R.id.imvCategoriaIcon);
        this.myRef = myRef;
        this.listaUId = listaUId;
        for (ListaCompras l:VariaveisEstaticas.getListaCompras()
        ) {
            if(l.getuId()==listaUId)
                listaCompras = l;
        }
    }
}