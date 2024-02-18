package com.lspsoftwares.minhalistadecompras.nucleo.adapters.rv_lista_compras;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.Log;
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
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lspsoftwares.minhalistadecompras.R;
import com.lspsoftwares.minhalistadecompras.db.DBGeneric;
import com.lspsoftwares.minhalistadecompras.nucleo.adapters.rv_lista_compras_itens.RvListaComprasItensAdapter;
import com.lspsoftwares.minhalistadecompras.entidades.Compras;
import com.lspsoftwares.minhalistadecompras.entidades.Item;
import com.lspsoftwares.minhalistadecompras.entidades.ItemLista;
import com.lspsoftwares.minhalistadecompras.entidades.ItemListaPreco;
import com.lspsoftwares.minhalistadecompras.entidades.ListaCompras;
import com.lspsoftwares.minhalistadecompras.nucleo.estatico.VariaveisEstaticas;
import com.lspsoftwares.minhalistadecompras.nucleo.interfaces.AoAtualizarPreco;
import com.lspsoftwares.minhalistadecompras.nucleo.interfaces.AoIniciarAtividade;
import com.lspsoftwares.minhalistadecompras.nucleo.interfaces.AtualizarListaListener;
import com.lspsoftwares.minhalistadecompras.nucleo.interfaces.ListaAtualizadaListener;
import com.lspsoftwares.minhalistadecompras.nucleo.interfaces.OnPauseObserver;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.DialogConstrutor;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.GeradorCodigosUnicos;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.ManipuladorDataTempo;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RvListaComprasAdapter extends RecyclerView.Adapter {
    List<OnPauseObserver> onPauseObservers = new ArrayList<>();
    Context context;
    FirebaseDatabase database;
    DatabaseReference myRef;
    Resources resources;
    InterstitialAd interstitialAd;
    boolean emCompras = false;
    List<AoIniciarAtividade> iniciarAtividades = new ArrayList<>();


    public RvListaComprasAdapter(Context context, InterstitialAd interstitialAd) {
        this.context = context;
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        resources = context.getResources();
        this.interstitialAd = interstitialAd;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.rv_lista_compras_layout,parent,false);
        ListaDeComprasViewHolder holder = new ListaDeComprasViewHolder(view,context,interstitialAd);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final ListaDeComprasViewHolder viewHolder = (ListaDeComprasViewHolder) holder;
        viewHolder.tvNome.setText(VariaveisEstaticas.getListaCompras().get(position).getNome());
        viewHolder.tvDataCriacao.setText(resources.getString(R.string.rv_lista_de_compras_cv_tv_data_criacao)+"\n"+ManipuladorDataTempo.dataIntToDataString(VariaveisEstaticas.getListaCompras().get(position).getDataCriacao()));

        if(VariaveisEstaticas.getListaCompras().get(position).getCriadorUid().equals(VariaveisEstaticas.getUsuario().getUid()))
            viewHolder.tvCriadoPor.setText(resources.getString(R.string.rv_lista_de_compras_cv_tv_criador)+ "\n"+VariaveisEstaticas.getUsuario().getNome().split("@")[0]);
        else
            buscarCriador(VariaveisEstaticas.getListaCompras().get(position).getCriadorUid(),viewHolder.tvCriadoPor);
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
        this.addOnPauseObeservadores(new OnPauseObserver() {
            @Override
            public void onPause() {
                viewHolder.onPause();
            }

            @Override
            public void onPause(boolean b) {
                viewHolder.onPause(b);
            }
        });

    }



    @Override
    public int getItemCount() {
        return VariaveisEstaticas.getListaCompras().size();
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
            public void onCancelled(DatabaseError databaseError) {Log.e("error:",databaseError.getMessage()); }
        });

    }

    public void onPause() {
        notificarOnPauseObserver();
    }

    public void addAtividadesObserver(AoIniciarAtividade aoIniciarAtividade){
        iniciarAtividades.add(aoIniciarAtividade);
    }

    public void addOnPauseObeservadores(OnPauseObserver pauseObserver){
        onPauseObservers.add(pauseObserver);
    }
    public void notificarAtividades(){
        for (AoIniciarAtividade observer:iniciarAtividades
        ) {
            observer.iniciouAtividade();
        }
    }
    public void notificarOnPauseObserver(){
        for (OnPauseObserver observer:onPauseObservers
             ) {
            observer.onPause();
        }
    }

    public void onPause(boolean b) {
        notificarOnPauseObserver(b);
    }

    private void notificarOnPauseObserver(boolean b) {
        for (OnPauseObserver observer:onPauseObservers
        ) {
            observer.onPause(b);
        }
    }
}


class ListaDeComprasViewHolder extends RecyclerView.ViewHolder{
    CardView cvLista;
    TextView tvNome;
    TextView tvDescricao;
    TextView tvDataCriacao;
    TextView tvCriadoPor;
    TextView tvListaTitulo;
    ImageView imvIconeLista;
    ImageButton imbRemover;
    Button btnNovoItem;
    Button btnIniciarCompras;
    RecyclerView rvItens;
    String listaUid;
    DBGeneric db;
    String tvTituloTexto;
    private int posicao;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private Context context;
    private RvListaComprasItensAdapter rvListaComprasItensAdapter;
    private List<ListaAtualizadaListener> listaAtualizadaListeners = new ArrayList<>();
    boolean emCompras = false;
    Resources resources;
    int comprasPosicao = 0;
    InterstitialAd interstitialAd;

    public ListaDeComprasViewHolder(View view, final Context context, InterstitialAd interstitialAd) {

        super(view);
        this.context = context;
        this.interstitialAd = interstitialAd;
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
        btnIniciarCompras = view.findViewById(R.id.btnIniciarCompras);
        if(VariaveisEstaticas.getListaCompras().get(posicao).getItens().size()>0)
            btnIniciarCompras.setVisibility(View.VISIBLE);
        else
            btnIniciarCompras.setVisibility(View.GONE);
        rvItens = view.findViewById(R.id.rvItens);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvItens.setLayoutManager(linearLayoutManager);
        resources = context.getResources();
        tvTituloTexto = resources.getString(R.string.rv_lista_de_compras_tv_itens_titulo);
        //Ao clicar no botão iniciar compras
        btnIniciarCompras.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                emCompras = !emCompras;
                gerenciaCompras(emCompras);
                mudaBotaoEmCompras(emCompras);
                    //torna os itens de compras visiveis e habilita as compras
                setItensVisiveis(emCompras);
            }
        });
        //Ao clicar no botão remover
        imbRemover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VariaveisEstaticas.getItemMap().remove(VariaveisEstaticas.getListaCompras().get(getPosicao()).getuId());
                VariaveisEstaticas.getUsuario().getListas().remove(listaUid);
                VariaveisEstaticas.getListaCompras().remove(getPosicao());
                VariaveisEstaticas.getVisiveis().remove(getPosicao());
                Map<String,Object> update = new HashMap<>();
                update.put(VariaveisEstaticas.getUsuario().getUid(),VariaveisEstaticas.getUsuario());
                myRef.child("Usuario").updateChildren(update);
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
                final List<List<String>> itens = db.buscar("Item",new String[]{"_id","Nome","CategoriaUid"},"Nome ASC");
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

                final DialogConstrutor dialogConstrutor = new DialogConstrutor(context,view,resources.getString(R.string.rvlistacomprasadapter_Add_novo_item_titulo),resources.getString(R.string.rvlistacomprasadapter_Add_novo_item_menssagem));
                ///###########Botão adicionar novo item ############///
                btnAddNovoItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!edQuantidade.getText().toString().isEmpty()&&!acItem.getText().toString().isEmpty()){
                            if(strings.contains(acItem.getText().toString()))
                                itemExistente(dialogConstrutor,acItem,edQuantidade,spUnidades,itens,strings,naoEDono(posicao));
                            else
                                itemInexistente(dialogConstrutor,acItem,edQuantidade,spUnidades,itens,strings,spCategoria,naoEDono(posicao));
                        }else{
                            new DialogConstrutor(context,resources.getString(R.string.rvlistacomprasadapter_campo_vazio_titulo),resources.getString(R.string.rvlistacomprasadapter_campo_vazio_menssagem),resources.getString(R.string.rvlistacomprasadapter_campo_vazio_pos_btn_txt));
                            dialogConstrutor.fechar();
                        }
                    }
                });
            }
        });



    }

    private void iniciarAd() {
       // interstitialAd.show();

    }

    private void gerenciaCompras(boolean emCompras) {
        if(emCompras){
            iniciarAd();
            boolean p = false;
            int i = 0;
            for (Compras compras:VariaveisEstaticas.getUsuario().getCompras()) {
               if(!compras.isConcluida()) {
                   p = true;
                   break;
               }
               i++;
            }
            Compras compras;
            if(!p){
                compras = new Compras();
                compras.setConcluida(false);
                compras.setListaUid(listaUid);
                compras.setCompraUid(new GeradorCodigosUnicos(10).gerarCodigos());
                List<ItemListaPreco> itemListaPrecos = new ArrayList<>();
                for (ItemLista item:VariaveisEstaticas.getListaCompras().get(posicao).getItens()) {
                    ItemListaPreco itemListaPreco = new ItemListaPreco();
                    itemListaPreco.setItemUid(item.getItemUid());
                    itemListaPrecos.add(itemListaPreco);
                }
                compras.setItemPrecos(itemListaPrecos);
                i = VariaveisEstaticas.getUsuario().getCompras().size();
                VariaveisEstaticas.getUsuario().getCompras().add(compras);
                atualizaFBCompras();
            }else{
                if(VariaveisEstaticas.getUsuario().getCompras().get(i).getItemPrecos().size()!=VariaveisEstaticas.getListaCompras().get(posicao).getItens().size()){
                    atualizaItensCompras(posicao,i);
                }
            }
            comprasPosicao = i;
            rvListaComprasItensAdapter.setComprasPosicao(i);

        }else {
            totalPreco();
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.dialog_concluir_compra, null);
            final RadioButton rbConcluir = view.findViewById(R.id.rbConcluir);
            Button btnConfirmar = view.findViewById(R.id.btnConfirmar);
            final DialogConstrutor dialogConstrutor = new DialogConstrutor(context,view,resources.getString(R.string.rvlistacomprasadapter_concluir_compra_titulo),resources.getString(R.string.rvlistacomprasadapter_concluir_compra_menssagem));
            btnConfirmar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(rbConcluir.isChecked()) {
                        if(!todosOsPrecos()){
                            LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
                            View view = inflater.inflate(R.layout.dialog_confirmar_concluir, null);
                            Button btnConfirmar = view.findViewById(R.id.btnConfirmar);
                            Button btnCancelar = view.findViewById(R.id.btnCancelar);
                            TextView tvCompraPreco = view.findViewById(R.id.tvCompraPreco);
                            tvCompraPreco.setText(tvCompraPreco.getText().toString() + totalPreco());
                            final DialogConstrutor d = new DialogConstrutor(context,view,resources.getString(R.string.rvlistacomprasadapter_confirmar_concluir_titulo),resources.getString(R.string.rvlistacomprasadapter_confirmar_concluir_menssagem));
                            btnCancelar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    d.fechar();
                                }
                            });
                            btnConfirmar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    d.fechar();
                                    VariaveisEstaticas.getUsuario().getCompras().get(comprasPosicao).setConcluida(true);
                                }
                            });
                        }else {
                            VariaveisEstaticas.getUsuario().getCompras().get(comprasPosicao).setConcluida(true);
                        }
                    }
                    onPause();
                    iniciarAd();
                    dialogConstrutor.fechar();
                }
            });

        }
    }

    private void atualizaItensCompras(int posicao, int i) {
        ListaCompras lista = VariaveisEstaticas.getListaCompras().get(posicao);
        Compras compras = VariaveisEstaticas.getUsuario().getCompras().get(i);
        for (ItemLista item: lista.getItens()) {
            boolean existe = false;
            for (ItemListaPreco preco:compras.getItemPrecos()) {
                if(preco.getItemUid().equals(item.getItemUid())){
                    existe = !existe;
                    break;
                }
            }
            if(!existe){
                ItemListaPreco a = new ItemListaPreco();
                a.setItemUid(item.getItemUid());
                compras.getItemPrecos().add(a);
            }
        }
        List<ItemListaPreco> toRemove = new ArrayList<>();
        for (ItemListaPreco itemListaPreco: compras.getItemPrecos()) {
            boolean existe = false;
            for (ItemLista item:lista.getItens()) {
                if(item.getItemUid().equals(itemListaPreco.getItemUid())){
                    existe = !existe;
                    break;
                }
            }
            if(!existe)
                toRemove.add(itemListaPreco);
        }
        for (ItemListaPreco itemListaPreco:toRemove) {
            compras.getItemPrecos().remove(itemListaPreco);
        }
        VariaveisEstaticas.getUsuario().getCompras().set(i,compras);
        atualizaFBCompras();
    }

    private String totalPreco() {
        try {
            double preco = 0;
            for (ItemListaPreco i : VariaveisEstaticas.getUsuario().getCompras().get(comprasPosicao).getItemPrecos()) {
                if (i.isConcluido()) {
                    preco += i.getPreco() * VariaveisEstaticas.getListaCompras().get(posicao).getItens().get(posicao).getQuantidade();
                }
            }
            VariaveisEstaticas.getUsuario().getCompras().get(comprasPosicao).setPrecoTotal(preco);
            return VariaveisEstaticas.getUsuario().getCompras().get(comprasPosicao).getPrecoTotalString();
        }catch (Exception e){
            return "0,00";
        }
    }

    private boolean todosOsPrecos() {
        boolean todos = true;
        for (ItemListaPreco i:VariaveisEstaticas.getUsuario().getCompras().get(comprasPosicao).getItemPrecos()) {
            if(i.getPreco()==0 ||!i.isConcluido()){
                todos = false;
                break;
            }
        }
        return todos;
    }


    private void mudaBotaoEmCompras(boolean emCompras) {
        Drawable[] drawables = btnIniciarCompras.getCompoundDrawables();
        // get left drawable.

        Drawable img = drawables[0];
        String txt;
        if(emCompras){
            img = resources.getDrawable(R.drawable.ic_pausar_compras);
            txt = resources.getString(R.string.rv_lista_de_compras_cv_btn_pausar_compras);
        }else{
            img = resources.getDrawable(R.drawable.ic_iniciar_compras);
            txt = resources.getString(R.string.rv_lista_de_compras_cv_btn_iniciar_compras);
        }
        img.setBounds(drawables[0].getBounds());
        btnIniciarCompras.setCompoundDrawables(img,null,null,null);
        btnIniciarCompras.setText(txt);
    }

    private void setItensVisiveis(final boolean emCompras) {
        AoAtualizarPreco atualizarPreco = new AoAtualizarPreco() {
            @Override
            public void atualizado() {
                if(emCompras)
                    tvListaTitulo.setText("R$ "+ totalPreco());
                else
                    tvListaTitulo.setText(tvTituloTexto);
            }
        };
        if(emCompras) {
            btnNovoItem.setVisibility(View.INVISIBLE);
            VariaveisEstaticas.addAoAtualizaPrecoObservador(atualizarPreco);
            VariaveisEstaticas.notificarAtualizarPrecosObservadores();
        }else {
            btnNovoItem.setVisibility(View.VISIBLE);
            VariaveisEstaticas.removeAoAtualizaPrecoObservador(atualizarPreco);
            tvListaTitulo.setText(tvTituloTexto);
        }
        rvListaComprasItensAdapter.setEmCompras(emCompras,comprasPosicao,posicao);
    }

    private void addNovoItemDB(Item item) {
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
    }

    private void addItemDb(String listaUid, String s, double d, String ts) {
        ContentValues values = new ContentValues();
        values.put("_IdLista",listaUid);
        values.put("_IdItem",s);
        values.put("Quantidade",d);
        values.put("Unidade",ts);
        db.inserir(values,"ItemLista");
    }

    private void mudaStatusVisibilidade(boolean b) {
        if(b){
            rvItens.setVisibility(View.GONE);
            btnNovoItem.setVisibility(View.GONE);
            tvListaTitulo.setVisibility(View.GONE);
            btnIniciarCompras.setVisibility(View.GONE);
            VariaveisEstaticas.getVisiveis().set(getPosicao(),0);
        }else {
            rvItens.setVisibility(View.VISIBLE);
            btnNovoItem.setVisibility(View.VISIBLE);
            tvListaTitulo.setVisibility(View.VISIBLE);
            if(VariaveisEstaticas.getListaCompras().get(posicao).getItens().size()>0)
                btnIniciarCompras.setVisibility(View.VISIBLE);
            else
                btnIniciarCompras.setVisibility(View.INVISIBLE);
            VariaveisEstaticas.getVisiveis().set(getPosicao(),1);
        }
    }

    public int getPosicao() {
        return posicao;
    }

    public void setPosicao(int posicao) {
        this.posicao = posicao;
        listaUid = VariaveisEstaticas.getListaCompras().get(getPosicao()).getuId();
        rvListaComprasItensAdapter = new RvListaComprasItensAdapter(context,listaUid);
        rvItens.setAdapter(rvListaComprasItensAdapter);
        rvListaComprasItensAdapter.addAtualizaObservadores(new AtualizarListaListener() {
            @Override
            public void atualizarLista() {
                notificarAtualização();
            }
        });
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
    public void onPause(){
        if(emCompras){
            mudaBotaoEmCompras(emCompras);
            setItensVisiveis(emCompras);
        }else
            atualizaFBCompras();
    }
    public void atualizaFBCompras(){
        Map<String, Object> update = new HashMap<>();
        update.put(VariaveisEstaticas.getUsuario().getUid(), VariaveisEstaticas.getUsuario());
        myRef.child("Usuario").updateChildren(update);
    }

    public void onPause(boolean b) {
        if(b)
            emCompras= !emCompras;
        if(emCompras){
            mudaBotaoEmCompras(emCompras);
            setItensVisiveis(emCompras);
        }else
            atualizaFBCompras();
    }
    public String naoEDono(int p){
        if(VariaveisEstaticas.getUsuario().getUid().equals(VariaveisEstaticas.getListaCompras().get(posicao).getCriadorUid())) {
            return listaUid;
        }else {
            ListaCompras novaListaCompras = VariaveisEstaticas.getListaCompras().get(p);
            novaListaCompras.setCriadorUid(VariaveisEstaticas.getUsuario().getUid());
            novaListaCompras.setuId(new GeradorCodigosUnicos(10).gerarCodigos());
            myRef.child("Lista").child(novaListaCompras.getuId()).setValue(novaListaCompras);
            VariaveisEstaticas.getUsuario().getListas().set(p, novaListaCompras.getuId());
            HashMap<String, Object> update = new HashMap<>();
            update.put(VariaveisEstaticas.getUsuario().getUid(), VariaveisEstaticas.getUsuario());
            myRef.child("Usuario").updateChildren(update);
            List<Item> items = VariaveisEstaticas.getItemMap().get(listaUid);
            VariaveisEstaticas.getItemMap().remove(listaUid);
            listaUid = novaListaCompras.getuId();
            VariaveisEstaticas.getItemMap().put(listaUid, items);
            return novaListaCompras.getuId();
        }
    }
    public void itemExistente(DialogConstrutor dialogConstrutor, AutoCompleteTextView acItem, EditText edQuantidade, Spinner spUnidades, List<List<String>> itens, List<String> strings, String b){
        listaUid = b;
        ItemLista itemLista = new ItemLista();
        itemLista.setItemUid(itens.get(strings.indexOf(acItem.getText().toString())).get(0));
        itemLista.setQuantidade(Double.parseDouble(edQuantidade.getText().toString()));
        itemLista.setUnidade(spUnidades.getSelectedItem().toString());
        VariaveisEstaticas.getListaCompras().get(getPosicao()).getItens().add(itemLista);
        myRef.child("Item").child(itemLista.getItemUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Item item = dataSnapshot.getValue(Item.class);
                VariaveisEstaticas.getItemMap().get(listaUid).add(item);
                rvListaComprasItensAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Map<String, Object> update = new HashMap<>();
        update.put(VariaveisEstaticas.getListaCompras().get(getPosicao()).getuId(), VariaveisEstaticas.getListaCompras().get(getPosicao()));
        myRef.child("Lista").updateChildren(update);
        for (Compras compras : VariaveisEstaticas.getUsuario().getCompras()) {
            ItemListaPreco itemListaPreco = new ItemListaPreco();
            itemListaPreco.setItemUid(itemLista.getItemUid());
            compras.getItemPrecos().add(itemListaPreco);
        }
        atualizaFBCompras();
        addItemDb(listaUid, itens.get(strings.indexOf(acItem.getText().toString())).get(0), Double.parseDouble(edQuantidade.getText().toString()), spUnidades.getSelectedItem().toString());
        rvListaComprasItensAdapter.notifyDataSetChanged();
        if (btnIniciarCompras.getVisibility() == View.INVISIBLE)
            btnIniciarCompras.setVisibility(View.VISIBLE);
        notificarAtualização();
        dialogConstrutor.fechar();
    }
    public void itemInexistente(DialogConstrutor dialogConstrutor, AutoCompleteTextView acItem, EditText edQuantidade, Spinner spUnidades, List<List<String>> itens, List<String> strings, Spinner spCategoria, String b){
        listaUid = b;
        Item item = new Item();
        item.setNome(acItem.getText().toString());
        item.setQuantidade(Double.parseDouble(edQuantidade.getText().toString()));
        int catId = VariaveisEstaticas.getCategorias().getPosition(spCategoria.getSelectedItem().toString());
        if (catId <= 9)
            item.setCategoriaUid("000" + Integer.toString(catId));
        else
            item.setCategoriaUid("00" + Integer.toString(catId));
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
        myRef.child("Item").child(item.getuId()).setValue(item);
        addNovoItemDB(item);
        ItemLista itemLista = new ItemLista();
        itemLista.setQuantidade(item.getQuantidade());
        itemLista.setItemUid(item.getuId());
        itemLista.setUnidade(spUnidades.getSelectedItem().toString());
        VariaveisEstaticas.getListaCompras().get(getPosicao()).getItens().add(itemLista);
        Map<String, Object> update = new HashMap<>();
        update.put(VariaveisEstaticas.getListaCompras().get(getPosicao()).getuId(), VariaveisEstaticas.getListaCompras().get(getPosicao()));
        myRef.child("Lista").updateChildren(update);
        addItemDb(listaUid, itemLista.getItemUid(), Double.parseDouble(edQuantidade.getText().toString()), spUnidades.getSelectedItem().toString());
        VariaveisEstaticas.getItemMap().get(listaUid).add(item);
        for (Compras compras : VariaveisEstaticas.getUsuario().getCompras()) {
            ItemListaPreco itemListaPreco = new ItemListaPreco();
            itemListaPreco.setItemUid(itemLista.getItemUid());
            compras.getItemPrecos().add(itemListaPreco);
        }
        atualizaFBCompras();
        notificarAtualização();
        rvListaComprasItensAdapter.notifyDataSetChanged();
        if (btnIniciarCompras.getVisibility() == View.INVISIBLE)
            btnIniciarCompras.setVisibility(View.VISIBLE);
        dialogConstrutor.fechar();
    }
}

