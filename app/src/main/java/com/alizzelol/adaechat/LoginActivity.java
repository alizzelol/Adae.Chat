package com.alizzelol.adaechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etContrasena = findViewById(R.id.etContrasena);
        Button btnLogin = findViewById(R.id.btnLogin);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> iniciarSesionUsuario(etEmail, etContrasena, mAuth, db));
    }

    private void iniciarSesionUsuario(EditText etEmail, EditText etContrasena, FirebaseAuth mAuth, FirebaseFirestore db) {
        String email = etEmail.getText().toString();
        String contrasena = etContrasena.getText().toString();

        mAuth.signInWithEmailAndPassword(email, contrasena)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String usuario = email.substring(0, email.indexOf("@"));

                        db.collection("users").document(usuario).get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful() && task1.getResult() != null) {
                                        DocumentSnapshot document = task1.getResult();
                                        if (document.exists()) {
                                            Map<String, Object> userData = document.getData();
                                            if (userData != null) { // Agregamos esta verificación
                                                String username = (String) userData.get("username");

                                                Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
                                                intent.putExtra("username", username);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(LoginActivity.this, "Error: Datos del usuario vacíos.", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Error al obtener los datos del usuario.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Error al obtener los datos del usuario.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        if (task.getException() != null) {
                            Toast.makeText(LoginActivity.this, "Error en el inicio de sesión: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Error en el inicio de sesión.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
