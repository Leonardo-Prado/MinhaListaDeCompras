package com.lspsoftwares.minhalistadecompras.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;


public class DBHelp extends SQLiteOpenHelper {
    private  String PREFERENCIAS = "preferencias";
    SharedPreferences prefs;
    public static final String DBNOME = "MinhaListaDB";
    public static final int VERSAO = 4;
    Context context;
    public DBHelp(Context context) {
        super(context,DBNOME, null, VERSAO);
        this.context = context;
        try {
            SQLiteDatabase db = this.getWritableDatabase();

        }catch (Exception e){
            Log.e("erro pegar db",e.getMessage());
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
       try {
           sqLiteDatabase.execSQL("CREATE TABLE Item(_id TEXT PRIMARY KEY,CategoriaUid TEXT NOT NULL,DataCriacao INTEGER NOT NULL,Descricao TEXT,HoraCriacao INTEGER NOT NULL, Nome TEXT NOT NULL,Quantidade REAL NOT NULL, Unidade TEXT,Sync INTEGER NOT NULL default 0)");
           sqLiteDatabase.execSQL("CREATE TABLE Usuario(_id TEXT PRIMARY KEY,nome TEXT NOT NULL,email TEXT NOT NULL,Sync INTEGER NOT NULL default 0)");
           sqLiteDatabase.execSQL("CREATE TABLE Lista(_id TEXT PRIMARY KEY, _idUsuario TEXT NOT NULL,Nome TEXT NOT NULL,Descricao TEXT,CriadorUid TEXT,DataCriacao INTEGER, HoraCriacao INTEGER,UIdCategoriaLista TEXT,Sync INTEGER NOT NULL default 0)");
           sqLiteDatabase.execSQL("CREATE TABLE ItemLista(_id INTEGER PRIMARY KEY AUTOINCREMENT, _IdLista TEXT NOT NULL,_IdItem TEXT NOT NULL,Quantidade REAL NOT NULL default 1,Unidade TEXT NOT NULL,Sync INTEGER NOT NULL default 0)");
           sqLiteDatabase.execSQL("CREATE TABLE UserLista(_id INTEGER PRIMARY KEY AUTOINCREMENT,_IdLista TEXT NOT NULL,_IdUser TEXT NOT NULL,Sync INTEGER NOT NULL default 0)");
           sqLiteDatabase.execSQL("CREATE TABLE Config(_id INTEGER PRIMARY KEY AUTOINCREMENT,ItensDownload INTEGER NOT NULL)");
       }catch (SQLException e) {
           Log.e("Erro ao criar tabela", e.getMessage());
       }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
       try{
           try { sqLiteDatabase.execSQL("drop table Item"); }catch (Exception e){}
           try { sqLiteDatabase.execSQL("drop table Usuario"); }catch (Exception e){}
           try { sqLiteDatabase.execSQL("drop table Lista"); }catch (Exception e){}
           try { sqLiteDatabase.execSQL("drop table ItemLista"); }catch (Exception e){}
           try { sqLiteDatabase.execSQL("drop table UserLista"); }catch (Exception e){}
           try { sqLiteDatabase.execSQL("drop table Config"); }catch (Exception e){}
           try {onCreate(sqLiteDatabase);}catch (Exception e){}
           prefs = context.getSharedPreferences(PREFERENCIAS, MODE_PRIVATE);
           SharedPreferences.Editor editor = prefs.edit();
           editor.putBoolean("downloadedItens", false);
           editor.commit();
       }catch (SQLException e) {
           Log.e("Erro ao atualizar db", e.getMessage());
       }

    }
    public void onDowngrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        try{
            try { sqLiteDatabase.execSQL("drop table Item"); }catch (Exception e){}
            try { sqLiteDatabase.execSQL("drop table Usuario"); }catch (Exception e){}
            try { sqLiteDatabase.execSQL("drop table Lista"); }catch (Exception e){}
            try { sqLiteDatabase.execSQL("drop table ItemLista"); }catch (Exception e){}
            try { sqLiteDatabase.execSQL("drop table UserLista"); }catch (Exception e){}
            try { sqLiteDatabase.execSQL("drop table Config"); }catch (Exception e){}
            try {onCreate(sqLiteDatabase);}catch (Exception e){}
        }catch (SQLException e) {
            Log.e("Erro ao dropar tabela", e.getMessage());
        }
    }


}
