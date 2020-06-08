package com.lspsoftwares.minhalistadecompras.ui.firebase_ui.Lista_Fragment;

import android.content.ContentValues;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lspsoftwares.minhalistadecompras.R;
import com.lspsoftwares.minhalistadecompras.db.DBGeneric;
import com.lspsoftwares.minhalistadecompras.nucleo.adapters.rv_lista_compras.RvListaComprasAdapterDesconn;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.Item;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.ListaCompras;
import com.lspsoftwares.minhalistadecompras.nucleo.estatico.VariaveisEstaticas;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.DialogConstrutor;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.GeradorCodigosUnicos;
import com.lspsoftwares.minhalistadecompras.objetos_auxiliares.ManipuladorDataTempo;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

public class ListaDesconn extends Fragment {
        private ImageButton btnAddLista;
        private Resources resources;
        FirebaseDatabase database;
        DatabaseReference myRef;
        RvListaComprasAdapterDesconn rvListaComprasAdapter;
        private boolean connected = false;

        //private OnFragmentInteractionListener mListener;

    public ListaDesconn() {
        // Required empty public constructor
    }

        public static ListaDesconn newInstance(boolean connected) {
        ListaDesconn fragment = new ListaDesconn();
        fragment.setConnected(connected);
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
        rvListaComprasAdapter = new RvListaComprasAdapterDesconn(getContext());
        rvMinhasListas.setAdapter(rvListaComprasAdapter);
        btnAddLista = v.findViewById(R.id.ibtnNovaLista);
        btnAddLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.dialog_add_lista, null);
                final EditText edNome = view.findViewById(R.id.edNome);
                final EditText edDescricao = view.findViewById(R.id.edDescricao);
                Button btnAdd = view.findViewById(R.id.btnAdd);
                final DialogConstrutor dialogAddLista = new DialogConstrutor(getContext(),view,resources.getString(R.string.fragment_lista_dialog_add_lista_titulo),resources.getString(R.string.fragment_lista_dialog_add_lista_menssagem));
                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListaCompras lista = new ListaCompras();
                        lista.setNome(edNome.getText().toString());
                        lista.setCriadorUid(VariaveisEstaticas.getUsuario().getUid());
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
                        VariaveisEstaticas.getUsuario().getListas().add(lista.getuId());
                        DBGeneric db = new DBGeneric(getContext());
                        ContentValues values = new ContentValues();
                        values.put("_id", lista.getuId());
                        values.put("_idUsuario",VariaveisEstaticas.getUsuario().getUid());
                        values.put("CriadorUid", lista.getCriadorUid());
                        values.put("DataCriacao", lista.getDataCriacao());
                        values.put("Descricao", lista.getDescricao());
                        values.put("HoraCriacao", lista.getHoraCriacao());
                        values.put("Nome", lista.getNome());
                        values.put("Sync",0);
                        int a = db.inserir(values, "Lista");
                        values = new ContentValues();
                        values.put("_IdLista",lista.getuId());
                        values.put("_IdUser",VariaveisEstaticas.getUsuario().getUid());
                        int b  = db.inserir(values,"UserLista");
                        VariaveisEstaticas.getListaCompras().add(lista);
                        VariaveisEstaticas.getItemMap().put(lista.getuId(),new ArrayList<Item>());
                        VariaveisEstaticas.getVisiveis().add(1);
                        rvListaComprasAdapter.notifyDataSetChanged();
                        dialogAddLista.fechar();
                        getActivity().recreate();
                    }
                });
            }
        });
        return v;

    }

        public boolean isConnected() {
        return connected;
    }

        public void setConnected(boolean connected) {
        this.connected = connected;
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
