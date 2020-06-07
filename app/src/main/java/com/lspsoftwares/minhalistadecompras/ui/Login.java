package com.lspsoftwares.minhalistadecompras.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lspsoftwares.minhalistadecompras.R;
import com.lspsoftwares.minhalistadecompras.nucleo.entidades.Usuario;
import com.lspsoftwares.minhalistadecompras.nucleo.estatico.VariaveisEstaticas;
import com.lspsoftwares.minhalistadecompras.ui.anonimo_ui.TelaPrincipalAnonimo;
import com.lspsoftwares.minhalistadecompras.ui.firebase_ui.TelaPrincipal;

public class Login  extends AppCompatActivity {
    private EditText edEmail;
    private EditText edSenha;
    private Button btnEntrar;
    private Button btnEntrarAnonimamente;
    private FirebaseAuth auth;
    private Resources resources;
    private TextView tvOfflineMsg;
    private TextView tvInfo;
    ProgressBar loading;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        loading = findViewById(R.id.loading);
        loading.setVisibility(View.GONE);
        resources = getResources();
        btnEntrarAnonimamente = findViewById(R.id.btnEntrarAnonimo);
        context = this;
        tvOfflineMsg = findViewById(R.id.tvOfflineMsg);
        tvInfo = findViewById(R.id.tvInfo);
        edEmail = findViewById(R.id.edEmail);
        edSenha = findViewById(R.id.edSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(!(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)) {
            edEmail.setVisibility(View.GONE);
            edSenha.setVisibility(View.GONE);
            btnEntrar.setVisibility(View.GONE);
            tvInfo.setVisibility(View.INVISIBLE);
            tvOfflineMsg.setVisibility(View.VISIBLE);
        }
        auth = FirebaseAuth.getInstance();
        edEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                habilitaBotao();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                habilitaBotao();
            }

            @Override
            public void afterTextChanged(Editable s) {
                habilitaBotao();
            }
        });
        edSenha.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                habilitaBotao();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                habilitaBotao();
            }

            @Override
            public void afterTextChanged(Editable s) {
                habilitaBotao();
            }
        });
        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.setVisibility(View.VISIBLE);
                Toast.makeText(context,resources.getString(R.string.login_conectando_servidor),Toast.LENGTH_LONG).show();
                logarComFirebase(edEmail.getText().toString(), edSenha.getText().toString());
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                VariaveisEstaticas.reset();

            }
        });
        btnEntrarAnonimamente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logarAnonimamente();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                VariaveisEstaticas.reset();

            }
        });
    }

    private void logarAnonimamente() {
        Toast.makeText(context,resources.getString(R.string.login_efetuando),Toast.LENGTH_LONG).show();
        loading.setVisibility(View.VISIBLE);
        Intent intent = new Intent(Login.this, TelaPrincipalAnonimo.class);
        intent.putExtra("email","anonimo@anonimo.com");
        intent.putExtra("uid","AAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        startActivity(intent);
        loading.setVisibility(View.GONE);
        finish();
    }

    private void finalizar() {
        this.finish();
    }

    private void logarComFirebase(final String email, final String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(context,resources.getString(R.string.login_efetuando),Toast.LENGTH_SHORT).show();
                            Log.d("Sucesso", "signInWithEmail:success");
                            FirebaseUser user = auth.getCurrentUser();
                            updateUI(user);

                        } else {
                            String menssagemErro = task.getException().getMessage();
                            if(menssagemErro.contains("There is no user record corresponding to this identifier. The user may have been deleted.")) {
                                criarUsuarioFirebase(email,password);

                            }else {
                                loading.setVisibility(View.GONE);
                                Log.w("falha", "signInWithEmail:failure", task.getException());
                                Toast.makeText(Login.this, resources.getString(R.string.login_activity_falha_ao_auth), Toast.LENGTH_LONG).show();
                            }
                        }

                        // ...
                    }
                });

    }

    private void criarUsuarioFirebase(String email,String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Sucesso", "createUserWithEmail:success");
                            FirebaseUser user = auth.getCurrentUser();
                            Usuario usuario = new Usuario();
                            usuario.setUid(user.getUid());
                            usuario.setEmail(user.getEmail());
                            usuario.setNome(user.getDisplayName());
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference();
                            myRef.child("Usuario").child(usuario.getUid()).setValue(usuario);
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Falha", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(Login.this, resources.getString(R.string.login_activity_falha_ao_criar_usuario), Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void habilitaBotao() {
        if(validarEmail(edEmail.getText().toString())&&validarSenha(edSenha.getText().toString()))
            btnEntrar.setEnabled(true);
        else
            btnEntrar.setEnabled(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!(auth ==null)) {
            FirebaseUser usuario = auth.getCurrentUser();
            updateUI(usuario);
        }
    }

    private void updateUI(FirebaseUser usuario) {
        if(usuario!=null)
            carregaTelaPrincipal(usuario.getEmail(),usuario.getUid());
        else
            loading.setVisibility(View.GONE);
    }

    private void carregaTelaPrincipal(String email,String uid) {
        Intent intent = new Intent(Login.this, TelaPrincipal.class);
        intent.putExtra("email",email);
        intent.putExtra("uid",uid);
        startActivity(intent);
        loading.setVisibility(View.GONE);
        finish();
    }


    private boolean validarEmail(String email) {
        String regExpn = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";
        CharSequence inputStr = email;
        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches())
            return true;
        else
            return false;
    }
    private boolean validarSenha(String senha) {
        return senha.length() >= 6;
    }
}
