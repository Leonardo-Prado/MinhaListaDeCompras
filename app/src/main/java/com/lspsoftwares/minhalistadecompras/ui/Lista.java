package com.lspsoftwares.minhalistadecompras.ui;

import android.content.res.Resources;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lspsoftwares.minhalistadecompras.R;
import com.lspsoftwares.minhalistadecompras.nucleo.adapters.RvListaComprasAdapter;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.ListaCompras;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.Usuario;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.DialogConstrutor;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.GeradorCodigosUnicos;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.ManipuladorDataTempo;


public class Lista extends Fragment {
    private List<ListaCompras> listaCompras = new ArrayList<>();
    private Usuario usuario;
    private FloatingActionButton fabAddLista;
    private Resources resources;
    FirebaseDatabase database;
    DatabaseReference myRef;
    RvListaComprasAdapter rvListaComprasAdapter;

    //private OnFragmentInteractionListener mListener;

    public Lista() {
        // Required empty public constructor
    }

    public static Lista newInstance(Usuario user, List<ListaCompras> listas) {
        Lista fragment = new Lista();
        fragment.setUsuario(user);
        fragment.setListaCompras(listas);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View v =  inflater.inflate(R.layout.fragment_lista, container, false);
       database = FirebaseDatabase.getInstance();
       myRef = database.getReference();
       resources = getResources();
       final RecyclerView rvMinhasListas = v.findViewById(R.id.rvMinhasListas);
       LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
       linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
       rvMinhasListas.setLayoutManager(linearLayoutManager);
       rvListaComprasAdapter = new RvListaComprasAdapter(getContext(),listaCompras,usuario);
       rvMinhasListas.setAdapter(rvListaComprasAdapter);
       fabAddLista = v.findViewById(R.id.fabAddLista);
       fabAddLista.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
               View view = inflater.inflate(R.layout.dialog_add_lista, null);
               final EditText edNome = view.findViewById(R.id.edNome);
               final EditText edDescricao = view.findViewById(R.id.edDescricao);
               Button btnAdd = view.findViewById(R.id.btnAdd);
               final DialogConstrutor dialogAddLista = new DialogConstrutor(getContext(),view,resources.getString(R.string.fragment_lista_dialog_add_lista_titulo),"");
               btnAdd.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       ListaCompras lista = new ListaCompras();
                       lista.setNome(edNome.getText().toString());
                       lista.setCriadorUid(getUsuario().getUid());
                       lista.setDescricao(edDescricao.getText().toString());
                       try {
                           ManipuladorDataTempo dataTempo = new ManipuladorDataTempo(new Date());
                           lista.setDataCriacao(dataTempo.getDataInt());
                           lista.setHoraCriacao(dataTempo.getTempoInt());
                       } catch (ParseException e) {
                           e.printStackTrace();
                       }
                       GeradorCodigosUnicos gcu = new GeradorCodigosUnicos(10);
                       lista.setuId(gcu.gerarCodigos());
                       getUsuario().getListas().add(lista.getuId());
                       myRef.child("Lista").child(lista.getuId()).setValue(lista);
                       Map<String,Object> update = new HashMap<>();
                       update.put(getUsuario().getUid(),getUsuario());
                       myRef.child("Usuario").updateChildren(update);
                       listaCompras.add(lista);
                       rvListaComprasAdapter = new RvListaComprasAdapter(getContext(),listaCompras,usuario);
                       rvMinhasListas.swapAdapter(rvListaComprasAdapter,true);
                       dialogAddLista.fechar();
                   }
               });
           }
       });
       return v;

    }

    public List<ListaCompras> getListaCompras() {
        return listaCompras;
    }

    public void setListaCompras(List<ListaCompras> listaCompras) {
        this.listaCompras = listaCompras;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    // TODO: Rename method, update argument and hook method into UI event
   /* public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

 //   @Override
    /*public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
 /*   public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/
}
