package com.lspsoftwares.minhalistadecompras.nucleo.adapters.rv_lista_compras;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lspsoftwares.minhalistadecompras.R;
import com.lspsoftwares.minhalistadecompras.db.DBGeneric;
import com.lspsoftwares.minhalistadecompras.nucleo.adapters.rv_lista_compras_itens.RvListaComprasItensAdapterDesconn;
import com.lspsoftwares.minhalistadecompras.nucleo.estatico.VariaveisEstaticas;
import com.lspsoftwares.minhalistadecompras.nucleo.interfaces.ListaAtualizadaListener;
import com.lspsoftwares.minhalistadecompras.entidades.Item;
import com.lspsoftwares.minhalistadecompras.entidades.ItemLista;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.DialogConstrutor;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.GeradorCodigosUnicos;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.ManipuladorDataTempo;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RvListaComprasAdapterDesconn extends RecyclerView.Adapter {
    Context context;
    FirebaseDatabase database;
    DatabaseReference myRef;
    Resources resources;


    public RvListaComprasAdapterDesconn(Context context) {
        this.context = context;
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        resources = context.getResources();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.rv_lista_compras_layout,parent,false);
        ListaDeComprasViewHolderDesconn holder = new ListaDeComprasViewHolderDesconn(view,context);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final ListaDeComprasViewHolderDesconn viewHolder = (ListaDeComprasViewHolderDesconn) holder;
        viewHolder.tvNome.setText(VariaveisEstaticas.getListaCompras().get(position).getNome());
        viewHolder.tvDataCriacao.setText(resources.getString(R.string.rv_lista_de_compras_cv_tv_data_criacao)+"\n"+ ManipuladorDataTempo.dataIntToDataString(VariaveisEstaticas.getListaCompras().get(position).getDataCriacao()));
        viewHolder.tvCriadoPor.setText(resources.getString(R.string.rv_lista_de_compras_cv_tv_criador) + "\n"+VariaveisEstaticas.getUsuario().getNome().split("@")[0]);
        viewHolder.listaUid = VariaveisEstaticas.getListaCompras().get(position).getuId();
        viewHolder.setPosicao(position);
        viewHolder.addListaAtualizadaListener(new ListaAtualizadaListener() {
            @Override
            public void aoAtualizar() {
                notifyDataSetChanged();
            }
        });
        //Carrega icone da lista
        Resources resources = context.getResources();
        int id = VariaveisEstaticas.getListaCompras().get(position).getIcon();
        TypedArray typedArray = resources.obtainTypedArray(R.array.imgTipoLista);
        Drawable drawable = typedArray.getDrawable(id);
        viewHolder.imvIconeLista.setImageDrawable(drawable);
    }
    public int getItemCount() {
        return VariaveisEstaticas.getListaCompras().size();
    }

}


class ListaDeComprasViewHolderDesconn extends RecyclerView.ViewHolder{
    CardView cvLista;
    TextView tvNome;
    TextView tvDescricao;
    TextView tvDataCriacao;
    TextView tvCriadoPor;
    TextView tvListaTitulo;
    ImageView imvIconeLista;
    ImageButton imbRemover;
    Button btnNovoItem;
    RecyclerView rvItens;
    String listaUid;
    DBGeneric db;
    private int posicao;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private Context context;
    private RvListaComprasItensAdapterDesconn rvListaComprasItensAdapter;
    private List<ListaAtualizadaListener> listaAtualizadaListeners = new ArrayList<>();

    public ListaDeComprasViewHolderDesconn (View view, final Context context) {
        super(view);
        this.context = context;
        db = new DBGeneric(context);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        imbRemover = view.findViewById(R.id.imbRemover);
        tvNome = view.findViewById(R.id.tvNome);
        tvDescricao = view.findViewById(R.id.tvDescricao);
        tvDataCriacao = view.findViewById(R.id.tvDataCriacao);
        tvCriadoPor = view.findViewById(R.id.tvCriadaPor);
        tvListaTitulo = view.findViewById(R.id.tvListaTitulo);
        imvIconeLista = view.findViewById(R.id.imvIconeLista);
        cvLista = view.findViewById(R.id.cvLista);
        btnNovoItem = view.findViewById(R.id.btnNovoItem);
        rvItens = view.findViewById(R.id.rvItens);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvItens.setLayoutManager(linearLayoutManager);

        //Ao clicar no botão remover
        imbRemover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VariaveisEstaticas.getItemMap().remove(VariaveisEstaticas.getListaCompras().get(getPosicao()).getuId());
                VariaveisEstaticas.getUsuario().getListas().remove(listaUid);
                VariaveisEstaticas.getListaCompras().remove(getPosicao());
                VariaveisEstaticas.getVisiveis().remove(getPosicao());
                db.deletar("Lista","_id = ?",new String[]{listaUid});
                notificarAtualização();
            }
        });

        cvLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(VariaveisEstaticas.getVisiveis().get(getPosicao()).compareTo(0)==0)
                    mudaStatusVisibilidade(false);
                else
                    mudaStatusVisibilidade(true);

            }
        });

//####################### Botão novo item ###################### //
        btnNovoItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.dialog_novo_item, null);
                final AutoCompleteTextView acItem = view.findViewById(R.id.acItem);
                final EditText edDescricao = view.findViewById(R.id.edDescricao);
                final EditText edQuantidade = view.findViewById(R.id.edQuantidade);
                final Spinner spUnidades = view.findViewById(R.id.spUnidade);
                final Spinner spCategoria = view.findViewById(R.id.spCategoria);
                Button btnAddNovoItem = view.findViewById(R.id.btnAddNovoItem);

                //Adapter para acItem
                final List<List<String>> itens = db.buscar("Item",new String[]{"_id","Nome","CategoriaUid","Unidade","Quantidade","DataCriacao","HoraCriacao","Descricao"},"Nome ASC");
                final List<String> strings = new ArrayList<>();
                for (List<String> list: itens){
                    strings.add(list.get(1));
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_dropdown_item_1line,strings);
                acItem.setAdapter(adapter);

                //Adapter para spUnidades
                ArrayAdapter<String> unidadesAdapter = new ArrayAdapter<>(context,   android.R.layout.simple_list_item_1);
                String[] unidadesArray = context.getResources().getStringArray(R.array.unidades);
                for (String s : unidadesArray) {
                    unidadesAdapter.add(s);
                }
                spUnidades.setAdapter(unidadesAdapter);

                //Adapter para categoriasfinal Spinner spUnidades = view.findViewById(R.id.spUnidade);
                spCategoria.setAdapter(VariaveisEstaticas.getCategorias());


                acItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String s = parent.getAdapter().getItem(position).toString();
                        int pos = -1;
                        for (int i =0;i<strings.size();i++){
                            if(strings.get(i).equals(s)){
                                pos = i;
                                break;
                            }
                        }
                        spCategoria.setSelection(Integer.parseInt(itens.get(pos).get(2)));
                    }
                });

                final DialogConstrutor dialogConstrutor = new DialogConstrutor(context,view,context.getResources().getString(R.string.rvlistacomprasadapter_Add_novo_item_titulo),context.getResources().getString(R.string.rvlistacomprasadapter_Add_novo_item_menssagem));
                ///###########Botão adicionar novo item ############///
                btnAddNovoItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!edQuantidade.getText().toString().isEmpty()&&!acItem.getText().toString().isEmpty()){
                            if(strings.contains(acItem.getText().toString())) {
                                ItemLista itemLista = new ItemLista();
                                itemLista.setItemUid(itens.get(strings.indexOf(acItem.getText().toString())).get(0));
                                itemLista.setQuantidade(Double.parseDouble(edQuantidade.getText().toString()));
                                itemLista.setUnidade(spUnidades.getSelectedItem().toString());
                                VariaveisEstaticas.getListaCompras().get(getPosicao()).getItens().add(itemLista);
                                ContentValues values = new ContentValues();
                                values.put("_IdLista",listaUid);
                                values.put("_IdItem",itens.get(strings.indexOf(acItem.getText().toString())).get(0));
                                values.put("Quantidade",Double.parseDouble(edQuantidade.getText().toString()));
                                values.put("Unidade",spUnidades.getSelectedItem().toString());
                                db.inserir(values,"ItemLista");
                                List<String> i = new ArrayList<>();
                                for (List<String> list:itens
                                     ) {
                                    if(list.get(0).equals(itemLista.getItemUid())) {
                                        i = list;
                                        break;
                                    }
                                }
                                Item item = new Item();
                                if(i.size()>0) {
                                    item.setuId(i.get(0));
                                    item.setNome(i.get(1));
                                    item.setCategoriaUid(i.get(2));
                                    item.setUnidade(i.get(3));
                                    item.setQuantidade(Double.parseDouble(i.get(4)));
                                    item.setDataCriacao(Long.parseLong(i.get(5)));
                                    item.setHoraCriacao(Long.parseLong(i.get(6)));
                                    item.setDescricao(i.get(7));
                                }
                                VariaveisEstaticas.getItemMap().get(listaUid).add(item);
                                rvListaComprasItensAdapter.notifyDataSetChanged();
                                dialogConstrutor.fechar();
                            }else{

                                Item item = new Item();
                                item.setNome(acItem.getText().toString());
                                item.setDescricao(edDescricao.getText().toString());
                                item.setQuantidade(Double.parseDouble(edQuantidade.getText().toString()));
                                int catId = VariaveisEstaticas.getCategorias().getPosition(spCategoria.getSelectedItem().toString());
                                if(catId<=9)
                                    item.setCategoriaUid("000"+Integer.toString(catId));
                                else
                                    item.setCategoriaUid("00"+Integer.toString(catId));
                                item.setUnidade(spUnidades.getSelectedItem().toString());
                                GeradorCodigosUnicos gcu = new GeradorCodigosUnicos(16);
                                item.setuId(gcu.gerarCodigos());
                                try {
                                    ManipuladorDataTempo dataTempo = new ManipuladorDataTempo(new Date());
                                    item.setDataCriacao(dataTempo.getDataInt());
                                    item.setHoraCriacao(dataTempo.getTempoInt());
                                    item.setCriadorUid(VariaveisEstaticas.getListaCompras().get(getPosicao()).getCriadorUid());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                ContentValues values = new ContentValues();
                                values.put("_id", item.getuId());
                                values.put("CategoriaUid", item.getCategoriaUid());
                                values.put("DataCriacao", item.getDataCriacao());
                                values.put("Descricao", item.getDescricao());
                                values.put("HoraCriacao", item.getHoraCriacao());
                                values.put("Nome", item.getNome());
                                values.put("Quantidade", item.getQuantidade());
                                values.put("Unidade", item.getUnidade());
                                values.put("Sync",0);
                                db.inserir(values, "Item");
                                ItemLista itemLista = new ItemLista();
                                itemLista.setQuantidade(item.getQuantidade());
                                itemLista.setItemUid(item.getuId());
                                itemLista.setUnidade(spUnidades.getSelectedItem().toString());
                                VariaveisEstaticas.getListaCompras().get(getPosicao()).getItens().add(itemLista);
                                values = new ContentValues();
                                values.put("_IdLista",listaUid);
                                values.put("_IdItem",itemLista.getItemUid());
                                values.put("Quantidade",Double.parseDouble(edQuantidade.getText().toString()));
                                values.put("Unidade",spUnidades.getSelectedItem().toString());
                                db.inserir(values,"ItemLista");
                                rvListaComprasItensAdapter.notifyDataSetChanged();
                                dialogConstrutor.fechar();
                            }
                        }else{
                            new DialogConstrutor(context,context.getResources().getString(R.string.rvlistacomprasadapter_campo_vazio_titulo),context.getResources().getString(R.string.rvlistacomprasadapter_campo_vazio_menssagem),context.getResources().getString(R.string.rvlistacomprasadapter_campo_vazio_pos_btn_txt));
                            dialogConstrutor.fechar();
                        }
                    }
                });
            }
        });



    }

    private void mudaStatusVisibilidade(boolean b) {
        if(b){
            rvItens.setVisibility(View.GONE);
            btnNovoItem.setVisibility(View.GONE);
            tvListaTitulo.setVisibility(View.GONE);
            VariaveisEstaticas.getVisiveis().set(getPosicao(),0);
        }else {
            rvItens.setVisibility(View.VISIBLE);
            btnNovoItem.setVisibility(View.VISIBLE);
            tvListaTitulo.setVisibility(View.VISIBLE);
            VariaveisEstaticas.getVisiveis().set(getPosicao(),1);
        }
    }

    public int getPosicao() {
        return posicao;
    }

    public void setPosicao(int posicao) {
        this.posicao = posicao;
        listaUid = VariaveisEstaticas.getListaCompras().get(getPosicao()).getuId();
        rvListaComprasItensAdapter = new RvListaComprasItensAdapterDesconn(context,listaUid);
        rvItens.setAdapter(rvListaComprasItensAdapter);
        if(VariaveisEstaticas.getVisiveis().get(getPosicao()).compareTo(0)==0)
            mudaStatusVisibilidade(true);
        else
            mudaStatusVisibilidade(false);
    }

    public void addListaAtualizadaListener(ListaAtualizadaListener listaAtualizadaListener){
        listaAtualizadaListeners.add(listaAtualizadaListener);
    }
    public void  notificarAtualização(){
        for (ListaAtualizadaListener l:listaAtualizadaListeners
             ) {
            l.aoAtualizar();
        }
    }
}

