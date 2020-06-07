package com.lspsoftwares.minhalistadecompras.nucleo.adapters.rv_lista_compras_itens;

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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lspsoftwares.minhalistadecompras.R;
import com.lspsoftwares.minhalistadecompras.db.DBGeneric;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.Item;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.ItemLista;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.ListaCompras;
import com.lspsoftwares.minhalistadecompras.nucleo.estatico.VariaveisEstaticas;
import com.lspsoftwares.minhalistadecompras.nucleo.interfaces.AtualizarListaListener;

import java.util.ArrayList;
import java.util.List;

public class RvListaComprasItensAdapterDesconn extends RecyclerView.Adapter {
    Context context;
    FirebaseDatabase database;
    DatabaseReference myRef;
    String listaUId;

    public RvListaComprasItensAdapterDesconn(Context context,String listaUId) {
        this.context = context;
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        this.listaUId = listaUId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.rv_itens_lista_de_compras,parent,false);
        ListaDeComprasItensViewHolderDesconn holder = new ListaDeComprasItensViewHolderDesconn(view,myRef,listaUId,context);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final ListaDeComprasItensViewHolderDesconn viewHolder = (ListaDeComprasItensViewHolderDesconn) holder;
        ListaCompras listaCompras = new ListaCompras();
        for (ListaCompras l: VariaveisEstaticas.getListaCompras()
        ) {
            if(l.getuId().equals(listaUId)){
                listaCompras = l;
            }
        }
        Item item = VariaveisEstaticas.getItemMap().get(listaUId).get(position);
        viewHolder.tvNome.setText(item.getNome());
        try {
            String unidades = trataUnidade(listaCompras.getItens().get(position).getUnidade(),item.getQuantidade());
            viewHolder.tvQuantidade.setText(item.getQuantidade()+" " +unidades);
        }catch (Exception e){}
        Resources resources = context.getResources();
        TypedArray typedArray = resources.obtainTypedArray(R.array.imagemListCategorias);
        int id = drawableFind(item.getNome(),Integer.parseInt(item.getCategoriaUid()));
        Drawable drawable = typedArray.getDrawable(id);
        viewHolder.imvCategoriaIcon.setImageDrawable(drawable);
        viewHolder.position = position;
        viewHolder.addAtualizadores(new AtualizarListaListener() {
            @Override
            public void atualizarLista() {
                dataSetChange();
            }
        });

    }

    @Override
    public int getItemCount() {
        try {
            return VariaveisEstaticas.getItemMap().get(listaUId).size();
        }catch (Exception e){
            Log.e("",e.getMessage());
            return 0;
        }
    }

    public String trataUnidade(String unidade,double quantidade){
        if(unidade==null)
            unidade = "unidades";
        if (quantidade != 1 && !unidade.equals("Kg"))
            unidade += "s";

        return unidade;
    }
    public void dataSetChange(){
        this.notifyDataSetChanged();
    }

    private int drawableFind(String nome,int defDraw) {
        if(nome.toLowerCase().contains("frango"))
            return 15;
        else if (nome.toLowerCase().contains("banana"))
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

class ListaDeComprasItensViewHolderDesconn extends RecyclerView.ViewHolder{
    List<AtualizarListaListener> atualizadores = new ArrayList<>();
    TextView tvNome;
    TextView tvQuantidade;
    CheckBox cbConcluido;
    ImageView imvCategoriaIcon;
    ImageButton imbRemoveItem;
    int position;
    List<ItemLista> itemListas = new ArrayList<>();
    DatabaseReference myRef;
    String listaUId;
    ListaCompras listaCompras;
    Context context;

    public ListaDeComprasItensViewHolderDesconn(View view, final DatabaseReference myRef, final String listaUId, final Context context) {
        super(view);
        this.context = context;
        tvNome = view.findViewById(R.id.tvNome);
        tvQuantidade = view.findViewById(R.id.tvQuantidade);
        cbConcluido = view.findViewById(R.id.cbConcluido);
        imvCategoriaIcon = view.findViewById(R.id.imvCategoriaIcon);
        imbRemoveItem = view.findViewById(R.id.imbRemoverItem);
        this.myRef = myRef;
        this.listaUId = listaUId;
        for (ListaCompras l:VariaveisEstaticas.getListaCompras()
        ) {
            if(l.getuId()==listaUId)
                listaCompras = l;
        }
        try {
            imbRemoveItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DBGeneric db = new DBGeneric(context);
                    db.deletar("ItemLista","_IdItem = ? AND _IdLista = ?",new String[]{VariaveisEstaticas.getListaCompras().get(VariaveisEstaticas.getListaCompras().indexOf(listaCompras)).getItens().get(position).getItemUid(),listaUId});
                    VariaveisEstaticas.getItemMap().get(listaUId).remove(findItemByUid(VariaveisEstaticas.getListaCompras().get(VariaveisEstaticas.getListaCompras().indexOf(listaCompras)).getItens().get(position).getItemUid(),VariaveisEstaticas.getItemMap().get(listaUId)));
                    VariaveisEstaticas.getListaCompras().get(VariaveisEstaticas.getListaCompras().indexOf(listaCompras)).getItens().remove(position);
                    notificarAtualização();
                }
            });
        }catch (Exception e){
            Log.e("Erro:",e.getMessage());
        }
    }
    public Item findItemByUid(String uid,List<Item> items){
        Item item = new Item();
        for (Item i:items
        ) {
            if (i.getuId().equals(uid))
                item = i;
        }
        return item;
    }
    public  void addAtualizadores(AtualizarListaListener atualizarListaListener){
        atualizadores.add(atualizarListaListener);
    }
    public void notificarAtualização(){
        for (AtualizarListaListener a:atualizadores
             ) {
            a.atualizarLista();
        }
    }

}
