package com.lspsoftwares.minhalistadecompras.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class Login  extends AppCompatActivity {
    private EditText edEmail;
    private EditText edSenha;
    private Button btnEntrar;
    private FirebaseAuth auth;
    private Resources resources;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        resources = Login.this.getResources();
        edEmail = findViewById(R.id.edEmail);
        edSenha = findViewById(R.id.edSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        auth = FirebaseAuth.getInstance();
        edEmail.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                habilitaBotao();
                return false;
            }
        });
        edSenha.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                habilitaBotao();
                return false;
            }
        });
        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logarComFirebase(edEmail.getText().toString(),edSenha.getText().toString());
            }
        });
        
        

    }

    private void logarComFirebase(final String email, final String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Sucesso", "signInWithEmail:success");
                            FirebaseUser user = auth.getCurrentUser();
                            updateUI(user);
                        } else {
                            String menssagemErro = task.getException().getMessage();
                            if(menssagemErro.contains("There is no user record corresponding to this identifier. The user may have been deleted.")) {
                                criarUsuarioFirebase(email,password);

                            }else {
                                Log.w("falha", "signInWithEmail:failure", task.getException());
                                Toast.makeText(Login.this, resources.getString(R.string.login_activity_falha_ao_auth), Toast.LENGTH_SHORT).show();
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
        FirebaseUser usuario = auth.getCurrentUser();
        updateUI(usuario);
    }

    private void updateUI(FirebaseUser usuario) {
        if(usuario!=null)
            carregaTelaPrincipal(usuario.getEmail(),usuario.getUid());
    }

    private void carregaTelaPrincipal(String email,String uid) {
        Intent intent = new Intent(Login.this, TelaPrincipal.class);
        intent.putExtra("email",email);
        intent.putExtra("uid",uid);
        startActivity(intent);
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
