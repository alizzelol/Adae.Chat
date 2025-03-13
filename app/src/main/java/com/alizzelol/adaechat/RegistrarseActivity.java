package com.alizzelol.adaechat;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegistrarseActivity extends AppCompatActivity {

    private EditText etUsuario, etEmail, etContraseña;
    private Button btnRegistrarse;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrarse);

        etUsuario = findViewById(R.id.etUsuario);
        etEmail = findViewById(R.id.etEmail);
        etContraseña = findViewById(R.id.etContraseña);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usuario = etUsuario.getText().toString().toLowerCase();
                String email = etEmail.getText().toString();
                String contraseña = etContraseña.getText().toString();

                mAuth.createUserWithEmailAndPassword(email, contraseña)
                        .addOnCompleteListener(RegistrarseActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    String userId = UUID.randomUUID().toString(); // Generar UUID
                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("username", usuario);
                                    userData.put("email", email);
                                    userData.put("userId", userId); // Guardar el UUID
                                    // Agregar otros campos aquí

                                    db.collection("users").document(userId).set(userData) // Usar userId como ID del documento
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(RegistrarseActivity.this, "Registro exitoso.", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(RegistrarseActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        Toast.makeText(RegistrarseActivity.this, "Error al guardar los datos del usuario.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(RegistrarseActivity.this, "Error en el registro: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}