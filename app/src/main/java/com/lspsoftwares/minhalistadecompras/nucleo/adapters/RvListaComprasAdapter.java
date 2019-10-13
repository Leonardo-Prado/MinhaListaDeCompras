package com.lspsoftwares.minhalistadecompras.nucleo.adapters;

import android.content.Context;
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
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.Usuario;
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
    Context context;
    List<ListaCompras> listaDeCompras;
    FirebaseDatabase database;
    DatabaseReference myRef;
    Usuario user;


    public RvListaComprasAdapter(Context context,List<ListaCompras> listaDeCompras,Usuario user) {
        this.context = context;
        this.listaDeCompras = listaDeCompras;
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        this.user = user;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.rv_lista_compras_layout,parent,false);
        ListaDeComprasViewHolder holder = new ListaDeComprasViewHolder(view,context,user);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final ListaDeComprasViewHolder viewHolder = (ListaDeComprasViewHolder) holder;
        viewHolder.listaUid = listaDeCompras.get(position).getuId();
        viewHolder.tvNome.setText(listaDeCompras.get(position).getNome());
        viewHolder.tvDescricao.setText(listaDeCompras.get(position).getDescricao());
        viewHolder.tvDataCriacao.setText(viewHolder.tvDataCriacao.getText()+"\n"+ManipuladorDataTempo.dataIntToDataString(listaDeCompras.get(position).getDataCriacao()));
        viewHolder.setListId(listaDeCompras.get(position).getuId());
        viewHolder.setListaCompras(listaDeCompras.get(position));
        viewHolder.setDatabase(database);
        viewHolder.setMyRef(myRef);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String nome =(String) dataSnapshot.child("Usuario").child(listaDeCompras.get(position).getCriadorUid()).child("nome").getValue();
                nome = nome.split("@")[0];
                viewHolder.tvCriadoPor.setText(viewHolder.tvCriadoPor.getText() + "\n" + nome);
                List<Item> itens = new ArrayList<>();
                List<String> s = new ArrayList<>();
                for (ItemLista i:listaDeCompras.get(position).getItens()) {
                    s.add(i.getItemUid());
                }
                for (String i :s
                     ) {
                   itens.add(dataSnapshot.child("Item").child(i).getValue(Item.class));
                }
                viewHolder.setItens(itens);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Falhou", "Failed to read value.", error.toException());
                viewHolder.tvCriadoPor.setText(viewHolder.tvCriadoPor.getText() + " " + "Desconhecido");
            }
        });
        listaDeCompras.get(position).getuId();


    }

    @Override
    public int getItemCount() {
        return listaDeCompras.size();
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
    String listaUid;
    Button btnNovoItem;
    RecyclerView rvItens;
    DBGeneric db;
    Usuario user;
    private String listId;
    private Boolean visivel = false;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private Context context;
    private ListaCompras listaCompras;
    private List<Item> itens = new ArrayList<>();
    private RvListaComprasItensAdapter rvListaComprasItensAdapter;
    int catId = 0;
    String[] categoriasArray;
    List<ItemLista> itemListas = new ArrayList<>();

    public ListaDeComprasViewHolder (View view, final Context context, final Usuario user) {
        super(view);
        this.context = context;
        db = new DBGeneric(context);
        this.listaUid = listaUid;
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        categoriasArray = context.getResources().getStringArray(R.array.categorias);
        imbRemover = view.findViewById(R.id.imbRemover);
        tvNome = view.findViewById(R.id.tvNome);
        tvDescricao = view.findViewById(R.id.tvDescricao);
        tvDataCriacao = view.findViewById(R.id.tvDataCriacao);
        tvCriadoPor= view.findViewById(R.id.tvCriadaPor);
        tvListaTitulo = view.findViewById(R.id.tvListaTitulo);
        imvIconeLista = view.findViewById(R.id.imvIconeLista);
        this.user = user;
        cvLista = view.findViewById(R.id.cvLista);
        btnNovoItem = view.findViewById(R.id.btnNovoItem);
        rvItens = view.findViewById(R.id.rvItens);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvItens.setLayoutManager(linearLayoutManager);
        final List<String> categorias = new ArrayList<>();
        for (int i = 0; i <categoriasArray.length; i++) {
            categorias.add(categoriasArray[i]);
        }
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ListaCompras listaCompras = dataSnapshot.child("Lista").child(listaUid).getValue(ListaCompras.class);

                for (ItemLista item:listaCompras.getItens()
                     ) {
                    itemListas.add(item);
                }
                rvListaComprasItensAdapter = new RvListaComprasItensAdapter(context,itens,categorias,listaUid,itemListas,listaCompras);
                rvItens.setAdapter(rvListaComprasItensAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        imbRemover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.getListas().remove(listaUid);
                Map<String,Object> update = new HashMap<>();
                update.put(user.getUid(),user);
                myRef.child("Usuario").updateChildren(update);
            }
        });



        cvLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(visivel){
                    rvItens.setVisibility(View.GONE);
                    btnNovoItem.setVisibility(View.GONE);
                    tvListaTitulo.setVisibility(View.GONE);
                    visivel = false;
                }else {
                    rvItens.setVisibility(View.VISIBLE);
                    btnNovoItem.setVisibility(View.VISIBLE);
                    tvListaTitulo.setVisibility(View.VISIBLE);
                    visivel = true;
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
                    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    myRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ListaCompras listaCompras = dataSnapshot.child("Lista").child(listaUid).getValue(ListaCompras.class);
                            List<ItemLista> itemListas = new ArrayList<>();
                            for (ItemLista item:listaCompras.getItens()
                            ) {
                                itemListas.add(item);
                            }
                            rvListaComprasItensAdapter = new RvListaComprasItensAdapter(context,itens,categorias,listaUid,itemListas,listaCompras);
                            rvItens.setAdapter(rvListaComprasItensAdapter);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }
        });
        btnNovoItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.dialog_novo_item, null);
                final AutoCompleteTextView acItem = view.findViewById(R.id.acItem);
                final List<List<String>> itens = db.buscar("Item",new String[]{"_id","Nome","CategoriaUid"},"Nome ASC");
                final List<String> strings = new ArrayList<>();
                for (List<String> list: itens){
                    strings.add(list.get(1));
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_dropdown_item_1line,strings);
                acItem.setAdapter(adapter);
                final EditText edDescricao = view.findViewById(R.id.edDescricao);
                final EditText edQuantidade = view.findViewById(R.id.edQuantidade);
                ArrayAdapter<String> unidadesAdapter = new ArrayAdapter<>(context,   android.R.layout.simple_list_item_1);
                String[] unidadesArray = context.getResources().getStringArray(R.array.unidades);
                for (int i = 0; i <unidadesArray.length; i++) {
                        unidadesAdapter.add(unidadesArray[i]);
                }
                final Spinner spUnidades = view.findViewById(R.id.spUnidade);
                spUnidades.setAdapter(unidadesAdapter);
                final ArrayAdapter<String> categoriaAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
                for (int i = 0; i <categoriasArray.length; i++) {
                    categoriaAdapter.add(categoriasArray[i]);
                }
                final Spinner spCategoria = view.findViewById(R.id.spCategoria);
                spCategoria.setAdapter(categoriaAdapter);
                Button btnAddNovoItem = view.findViewById(R.id.btnAddNovoItem);
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
                final DialogConstrutor dialogConstrutor = new DialogConstrutor(context,view,"Adicione um Novo Item","Adicione um novo item a sua lista de compras");
                btnAddNovoItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!edQuantidade.getText().toString().isEmpty()&&!acItem.getText().toString().isEmpty()){
                            if(strings.contains(acItem.getText().toString())) {
                                ItemLista itemLista = new ItemLista();
                                itemLista.setItemUid(itens.get(strings.indexOf(acItem.getText().toString())).get(0));
                                itemLista.setQuantidade(Double.parseDouble(edQuantidade.getText().toString()));
                                itemLista.setUnidade(spUnidades.getSelectedItem().toString());
                                listaCompras.getItens().add(itemLista);
                                Map<String, Object> update = new HashMap<>();
                                update.put(listaCompras.getuId(), listaCompras);
                                myRef.child("Lista").updateChildren(update);
                                dialogConstrutor.fechar();
                            }else{

                                Item item = new Item();
                                item.setNome(acItem.getText().toString());
                                item.setDescricao(edDescricao.getText().toString());
                                item.setQuantidade(Double.parseDouble(edQuantidade.getText().toString()));
                                catId = categoriaAdapter.getPosition(spCategoria.getSelectedItem().toString());
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
                                    item.setCriadorUid(listaCompras.getCriadorUid());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                myRef.child("Item").child(item.getuId()).setValue(item);
                                ItemLista itemLista = new ItemLista();
                                itemLista.setQuantidade(item.getQuantidade());
                                itemLista.setItemUid(item.getuId());
                                itemLista.setUnidade(spUnidades.getSelectedItem().toString());
                                listaCompras.getItens().add(itemLista);
                                Map<String, Object> update = new HashMap<>();
                                update.put(listaCompras.getuId(), listaCompras);
                                myRef.child("Lista").updateChildren(update);
                                dialogConstrutor.fechar();
                            }
                        }else{
                            new DialogConstrutor(context,"Erro, campo vazio!","Os campos nome e quantidades do item devem ser preenchidos para se adicionar um novo item!","OK");
                            dialogConstrutor.fechar();
                        }
                    }
                });
            }
        });
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public Boolean getVisivel() {
        return visivel;
    }

    public void setVisivel(Boolean visivel) {
        this.visivel = visivel;
    }

    public FirebaseDatabase getDatabase() {
        return database;
    }

    public void setDatabase(FirebaseDatabase database) {
        this.database = database;
    }

    public DatabaseReference getMyRef() {
        return myRef;
    }

    public void setMyRef(DatabaseReference myRef) {
        this.myRef = myRef;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public ListaCompras getListaCompras() {
        return listaCompras;
    }

    public void setListaCompras(ListaCompras listaCompras) {
        this.listaCompras = listaCompras;
    }

    public List<Item> getItens() {
        return itens;
    }

    public void setItens(List<Item> itens) {
        this.itens = itens;
    }
}