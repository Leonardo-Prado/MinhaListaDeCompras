package com.lspsoftwares.minhalistadecompras.nucleo.estatico;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lspsoftwares.minhalistadecompras.R;
import com.lspsoftwares.minhalistadecompras.entidades.Item;
import com.lspsoftwares.minhalistadecompras.entidades.ListaCompras;
import com.lspsoftwares.minhalistadecompras.entidades.Usuario;
import com.lspsoftwares.minhalistadecompras.nucleo.interfaces.AoAtualizarPreco;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariaveisEstaticas {
    private static Context context;
    private static Resources resources;
    private static VariaveisEstaticas instancia;
    private static Usuario usuario;
    private static List<ListaCompras> listaCompras;
    private static Map<String,List<Item>> itemMap;
    private static boolean atualizado;
    private static List<Integer> visiveis = new ArrayList<>();
    private static ArrayAdapter<String> categorias;
    private static boolean carregaOFFLine = false;
    private static FirebaseDatabase database;
    private static DatabaseReference myRef;
    private static ArrayAdapter<String> itensAdapter;
    private static List<Item> itens = new ArrayList<>();
    private static List<AoAtualizarPreco> aoAtualizarPrecos = new ArrayList<>();


    private VariaveisEstaticas(Context context,FirebaseDatabase database,DatabaseReference myRef){
        usuario = new Usuario();
        listaCompras = new ArrayList<>();
        itemMap = new HashMap<>();
        visiveis = new ArrayList<>();
        itens = new ArrayList<>();
        aoAtualizarPrecos = new ArrayList<>();
        setCarregaOFFLine(false);
        this.database = database;
        this.myRef = myRef;
        setAtualizado(false);
        try {
            categorias = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
            String[] categoriasArray = getResources(context).getStringArray(R.array.categorias);
            for (String s:categoriasArray) {
                getCategorias().add(s);
            }
        }catch (Exception e){
            Log.e("erro:",e.getMessage());
        }
    }

    public static synchronized VariaveisEstaticas getInstance(Context context,FirebaseDatabase database,DatabaseReference myRef){
        setContext(context);
        if(instancia==null)
            instancia = new VariaveisEstaticas(context,database,myRef);
        return instancia;
    }

    public static Usuario getUsuario() {
        return usuario;
    }

    public static void setUsuario(Usuario usuario) {
        VariaveisEstaticas.usuario = usuario;
    }

    public static List<ListaCompras> getListaCompras() {
        return listaCompras;
    }

    public static void setListaCompras(List<ListaCompras> listaCompras) {
        VariaveisEstaticas.listaCompras = listaCompras;
    }

    public static Map<String, List<Item>> getItemMap() {
        return itemMap;
    }

    public static void setItemMap(Map<String, List<Item>> itemMap) {
        VariaveisEstaticas.itemMap = itemMap;
    }

    public static boolean isAtualizado() {
        return atualizado;
    }

    public static void setAtualizado(boolean atualizado) {
        VariaveisEstaticas.atualizado = atualizado;
    }

    public static List<Integer> getVisiveis() {
        return visiveis;
    }

    public static void setVisiveis(List<Integer> visiveis) {
        VariaveisEstaticas.visiveis = visiveis;
    }

    public static ArrayAdapter<String> getCategorias() {
        return categorias;
    }

    public static void setCategorias(ArrayAdapter<String> categorias) {
        VariaveisEstaticas.categorias = categorias;
    }

    public static Resources getResources(Context context) {
        if(resources == null)
            resources = context.getResources();
        return resources;
    }

    public static void setResources(Resources resources) {
        resources = resources;
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        context = context;
    }

    public static boolean isCarregaOFFLine() {
        return carregaOFFLine;
    }

    public static void setCarregaOFFLine(boolean carregaOFFLine) {
        VariaveisEstaticas.carregaOFFLine = carregaOFFLine;
    }

    public static void updateLista(int index) {
        Map<String, Object> update = new HashMap<>();
        update.put(VariaveisEstaticas.getListaCompras().get(index).getuId(), VariaveisEstaticas.getListaCompras().get(index));
        getMyRef().child("Lista").updateChildren(update);
    }

    public static FirebaseDatabase getDatabase() {
        return database;
    }

    public static void setDatabase(FirebaseDatabase database) {
        VariaveisEstaticas.database = database;
    }

    public static DatabaseReference getMyRef() {
        return myRef;
    }

    public static void setMyRef(DatabaseReference myRef) {
        VariaveisEstaticas.myRef = myRef;
    }
    public static void addAoAtualizaPrecoObservador(AoAtualizarPreco aoAtualizarPreco){
        aoAtualizarPrecos.add(aoAtualizarPreco);
    }
    public static void removeAoAtualizaPrecoObservador(AoAtualizarPreco aoAtualizarPreco){
        if(aoAtualizarPrecos.contains(aoAtualizarPreco))
            aoAtualizarPrecos.remove(aoAtualizarPreco);
    }
    public static void notificarAtualizarPrecosObservadores(){
        for (AoAtualizarPreco a:aoAtualizarPrecos) {
            a.atualizado();
        }
    }

    public static ArrayAdapter<String> getItensAdapter() {
        return itensAdapter;
    }

    public static void setItensAdapter(ArrayAdapter<String> itensAdapter) {
        VariaveisEstaticas.itensAdapter = itensAdapter;
    }

    public static List<Item> getItens() {
        return itens;
    }

    public static void setItens(List<Item> itens) {
        VariaveisEstaticas.itens = itens;
    }

    public static boolean itensContainItem(String s) {
        boolean contain = false;
        for (int i =0;i<itens.size();i++){
            if(VariaveisEstaticas.getItens().get(i).getNome().equals(s)){
                contain = true;
                break;
            }
        }
        return contain;
    }

    public static Item getItemByItemName(String nome) {
        Item item = new Item();
        for (Item i:VariaveisEstaticas.getItens()) {
            if (i.getNome().equals(nome))
                item = i;
        }
        return item;
    }

    public static Item getItemByItemId(String uid) {
        Item item = new Item();
        for (Item i:getItens()) {
            if (i.getuId().equals(uid))
                item = i;
        }
        return item;
    }
    public static void reset(){
        if(instancia!=null)
            instancia = new VariaveisEstaticas(context,database,myRef);
    }

}
