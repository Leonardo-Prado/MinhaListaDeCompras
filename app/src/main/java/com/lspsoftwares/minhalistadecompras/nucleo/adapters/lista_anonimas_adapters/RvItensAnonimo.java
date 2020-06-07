package com.lspsoftwares.minhalistadecompras.nucleo.adapters.lista_anonimas_adapters;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lspsoftwares.minhalistadecompras.R;
import com.lspsoftwares.minhalistadecompras.db.DBGeneric;
import com.lspsoftwares.minhalistadecompras.nucleo.interfaces.AtualizarListaListener;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.Item;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.ItemLista;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.ListaCompras;
import com.lspsoftwares.minhalistadecompras.nucleo.estatico.VariaveisEstaticas;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.DialogConstrutor;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.GeradorCodigosUnicos;

import java.util.ArrayList;
import java.util.List;

public class RvItensAnonimo extends RecyclerView.Adapter {
    Context context;
    FirebaseDatabase database;
    DatabaseReference myRef;
    String listaUId;
    private boolean emCompras = false;
    private List<AoIniciarComprasObservador> comprasObservadores = new ArrayList<>();
    private int comprasPosicao;
    private List<AtualizarListaListener> atualizaObservadores = new ArrayList<>();

    public RvItensAnonimo(Context context,String listaUId) {
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
        ListaDeComprasItensAnonimoViewHolder holder = new ListaDeComprasItensAnonimoViewHolder(view,myRef,listaUId,context);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final  ListaDeComprasItensAnonimoViewHolder viewHolder = (ListaDeComprasItensAnonimoViewHolder) holder;
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
            viewHolder.tvQuantidade.setText(listaCompras.getItens().get(position).getQuantidade()+" " +unidades);
        }catch (Exception e){}
        final Resources resources = context.getResources();
        TypedArray typedArray = resources.obtainTypedArray(R.array.imagemListCategorias);
        int id = drawableFind(item.getNome(),Integer.parseInt(item.getCategoriaUid()));
        Drawable drawable = typedArray.getDrawable(id);
        viewHolder.imvCategoriaIcon.setImageDrawable(drawable);
        viewHolder.position = position;
        viewHolder.addAtualizadores(new AtualizarListaListener() {
            @Override
            public void atualizarLista() {
                notifyDataSetChanged();
                notificaAtualizouLista();
            }
        });
        comprasObservadores.add(new AoIniciarComprasObservador() {
            @Override
            public void iniciouCompras(boolean emCompras, final int comprasPosicao, final int listaPos) {

                if(emCompras){
                    viewHolder.listaPos = listaPos;
                    viewHolder.comprasPosicao = comprasPosicao;
                    viewHolder.cbConcluido.setVisibility(View.VISIBLE);
                    viewHolder.edPreco.setVisibility(View.VISIBLE);
                    viewHolder.imbRemoveItem.setVisibility(View.GONE);
                    viewHolder.setBackgroundColor(viewHolder.cbConcluido.isChecked());
                    viewHolder.cbConcluido.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            viewHolder.setBackgroundColor(isChecked);
                            VariaveisEstaticas.getUsuario().getCompras().get(comprasPosicao).getItemPrecos().get(position).setConcluido(isChecked);
                        }
                    });
                    try {
                        if (!viewHolder.edPreco.getText().toString().equals(""))
                            VariaveisEstaticas.getUsuario().getCompras().get(comprasPosicao).getItemPrecos().get(position).setPreco(Double.parseDouble(viewHolder.edPreco.getText().toString()));
                        else
                            VariaveisEstaticas.getUsuario().getCompras().get(comprasPosicao).getItemPrecos().get(position).setPreco(0);
                    }catch (Exception e){
                        try {
                            VariaveisEstaticas.getUsuario().getCompras().get(comprasPosicao).getItemPrecos().get(position).setPreco(0);
                            Log.e("Erro ao passar",e.getMessage());
                        }catch (Exception ex){
                            Log.e("Error:",ex.getMessage());
                        }

                    }

                }else{
                    viewHolder.cbConcluido.setVisibility(View.GONE);
                    viewHolder.edPreco.setVisibility(View.GONE);
                    viewHolder.setBackgroundColorDefault();
                    viewHolder.imbRemoveItem.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private int drawableFind(String nome,int defDraw) {
        if(nome.toLowerCase().contains("frango"))
            return 15;
        else if (nome.toLowerCase().contains("refrigerante"))
            return 19;
        else if (nome.toLowerCase().contains("banana"))
            return 16;
        else if (nome.toLowerCase().equals("maça")||nome.toLowerCase().equals("maçã"))
            return 17;
        else if (nome.toLowerCase().equals("laranja"))
            return 18;
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

    public boolean isEmCompras() {
        return emCompras;
    }

    public void setEmCompras(boolean emCompras, int comprasPosicao, int listaPos) {
        this.emCompras = emCompras;
        notificarIniciouComprasObservadores(comprasPosicao,listaPos);
    }
    public void addIniciouComprasObservadores(AoIniciarComprasObservador observador){
        comprasObservadores.add(observador);
    }
    public void notificarIniciouComprasObservadores(int comprasPosicao, int listaPos){
        for (AoIniciarComprasObservador observador:comprasObservadores
        ) {
            observador.iniciouCompras(isEmCompras(),comprasPosicao,listaPos);
        }
    }
    public void addAtualizaObservadores(AtualizarListaListener atualizarListaListener){
        if(atualizaObservadores == null)
            atualizaObservadores = new ArrayList<>();
        atualizaObservadores.add(atualizarListaListener);
    }
    public void notificaAtualizouLista(){
        for (AtualizarListaListener a:atualizaObservadores) {
            a.atualizarLista();
        }
    }

    public void setComprasPosicao(int i) {
        this.comprasPosicao = i;
    }
}

class ListaDeComprasItensAnonimoViewHolder extends RecyclerView.ViewHolder{
    public int comprasPosicao;
    public int listaPos;
    List<AtualizarListaListener> atualizadores = new ArrayList<>();
    TextView tvNome;
    TextView tvQuantidade;
    CheckBox cbConcluido;
    ImageView imvCategoriaIcon;
    ImageButton imbRemoveItem;
    EditText edPreco;
    int position;
    List<ItemLista> itemListas = new ArrayList<>();
    DatabaseReference myRef;
    String listaUId;
    ListaCompras listaCompras;
    View background;
    Resources resources;
    Context context;
    int backCor;

    public ListaDeComprasItensAnonimoViewHolder(View view, final DatabaseReference myRef, final String listaUId, final Context context) {
        super(view);
        this.context = context;
        background = view.findViewById(R.id.llAll);
        backCor = Color.parseColor("#FAFAFA");
        resources = context.getResources();
        tvNome = view.findViewById(R.id.tvNome);
        tvQuantidade = view.findViewById(R.id.tvQuantidade);
        cbConcluido = view.findViewById(R.id.cbConcluido);
        imvCategoriaIcon = view.findViewById(R.id.imvCategoriaIcon);
        imbRemoveItem = view.findViewById(R.id.imbRemoverItem);
        edPreco = view.findViewById(R.id.edPreco);
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
                    if(VariaveisEstaticas.getUsuario().getUid().equals(listaCompras.getCriadorUid())) {
                        removeItemfromDb(VariaveisEstaticas.getItemMap().get(listaUId).get(position));
                        VariaveisEstaticas.getItemMap().get(listaUId).remove(findItemByUid(VariaveisEstaticas.getListaCompras().get(VariaveisEstaticas.getListaCompras().indexOf(listaCompras)).getItens().get(position).getItemUid(), VariaveisEstaticas.getItemMap().get(listaUId)));
                        VariaveisEstaticas.getListaCompras().get(VariaveisEstaticas.getListaCompras().indexOf(listaCompras)).getItens().remove(position);
                        notificarAtualização();
                    }else{
                        String l = naoEDono();
                        removeItemfromDb(VariaveisEstaticas.getItemMap().get(listaUId).get(position));
                        VariaveisEstaticas.getItemMap().get(l).remove(position);
                        VariaveisEstaticas.getListaCompras().get(VariaveisEstaticas.getListaCompras().indexOf(listaCompras)).getItens().remove(position);
                        notificarAtualização();
                    }
                }
            });
        }catch (Exception e){
            Log.e("Erro:",e.getMessage());
        }
        tvQuantidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.dialog_editar_quantidade, null);
                final EditText edQuantidade = view.findViewById(R.id.edQuantidade);
                edQuantidade.setText(separaNum(tvQuantidade.getText().toString()));
                final Spinner spUnidades = view.findViewById(R.id.spUnidade);
                Button btnConcluir = view.findViewById(R.id.btnConcluir);
                //Adapter para spUnidades
                ArrayAdapter<String> unidadesAdapter = new ArrayAdapter<>(context,   android.R.layout.simple_list_item_1);
                String[] unidadesArray = context.getResources().getStringArray(R.array.unidades);
                for (String s : unidadesArray) {
                    unidadesAdapter.add(s);
                }
                spUnidades.setAdapter(unidadesAdapter);
                final DialogConstrutor d = new DialogConstrutor(context,view,resources.getString(R.string.rvlistacomprasitensadapter_alterar_quantidade_titulo),resources.getString(R.string.rvlistacomprasitensadapter_alterar_quantidade_menssagem));
                btnConcluir.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(VariaveisEstaticas.getUsuario().getUid().equals(listaCompras.getCriadorUid())) {
                            int a = VariaveisEstaticas.getListaCompras().indexOf(listaCompras);
                            VariaveisEstaticas.getListaCompras().get(a).getItens().get(position).setUnidade(spUnidades.getSelectedItem().toString());
                            VariaveisEstaticas.getListaCompras().get(a).getItens().get(position).setQuantidade(Double.parseDouble(edQuantidade.getText().toString()));
                            listaCompras.getItens().get(position).setQuantidade(Double.parseDouble(edQuantidade.getText().toString()));
                            listaCompras.getItens().get(position).setUnidade(spUnidades.getSelectedItem().toString());
                            notificarAtualização();
                            VariaveisEstaticas.updateLista(a);
                            d.fechar();
                        }else{
                            int a = VariaveisEstaticas.getListaCompras().indexOf(listaCompras);
                            naoEDono();
                            VariaveisEstaticas.getListaCompras().get(a).getItens().get(position).setUnidade(spUnidades.getSelectedItem().toString());
                            VariaveisEstaticas.getListaCompras().get(a).getItens().get(position).setQuantidade(Double.parseDouble(edQuantidade.getText().toString()));
                            listaCompras.getItens().get(position).setQuantidade(Double.parseDouble(edQuantidade.getText().toString()));
                            listaCompras.getItens().get(position).setUnidade(spUnidades.getSelectedItem().toString());
                            notificarAtualização();

                            VariaveisEstaticas.updateLista(a);
                            d.fechar();
                        }
                    }
                });
            }
        });
        edPreco.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    if(!cbConcluido.isChecked())
                        cbConcluido.setChecked(true);
                    if (!edPreco.getText().toString().equals(""))
                        VariaveisEstaticas.getUsuario().getCompras().get(comprasPosicao).getItemPrecos().get(position).setPreco(Double.parseDouble(s.toString()));
                    else
                        VariaveisEstaticas.getUsuario().getCompras().get(comprasPosicao).getItemPrecos().get(position).setPreco(0);
                    VariaveisEstaticas.notificarAtualizarPrecosObservadores();
                }catch (Exception e){
                    Log.e("Erro ao passar",e.getMessage());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {            }
        });

    }

    private String separaNum(String String) {
        String novaString = "";
        for (char c:String.toCharArray()
        ) {
            if((c>='0'&&c<='9')||c=='.'||c==',') {
                if(c==',')
                    c='.';
                novaString += c;
            }
        }
        return novaString;
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
    public void setBackgroundColor(boolean i){
        if(i)
            background.setBackgroundColor(resources.getColor(R.color.item_concluido));
        else
            background.setBackgroundColor(resources.getColor(R.color.item_nao_concluido));
    }
    public void setBackgroundColorDefault(){
        background.setBackgroundColor(backCor);
    }
    public String naoEDono(){
        int p = VariaveisEstaticas.getListaCompras().indexOf(listaCompras);
        ListaCompras novaListaCompras = VariaveisEstaticas.getListaCompras().get(p);
        novaListaCompras.setCriadorUid(VariaveisEstaticas.getUsuario().getUid());
        novaListaCompras.setuId(new GeradorCodigosUnicos(10).gerarCodigos());
        VariaveisEstaticas.getUsuario().getListas().set(p,novaListaCompras.getuId());
        List<Item> items = VariaveisEstaticas.getItemMap().get(listaUId);
        VariaveisEstaticas.getItemMap().remove(listaUId);
        listaUId = novaListaCompras.getuId();
        VariaveisEstaticas.getItemMap().put(listaUId,items);
        listaCompras = novaListaCompras;
        return novaListaCompras.getuId();
    }

    private void removeItemfromDb(Item item) {
        DBGeneric db = new DBGeneric(context);
        db.deletar("ItemLista","_IdLista = ? AND _IdItem = ?",new String[]{listaUId,item.getuId()});
    }
}

interface AoIniciarComprasObservador {
    public void iniciouCompras(boolean emCompras, int comprasPosicao, int listaPos);
}
