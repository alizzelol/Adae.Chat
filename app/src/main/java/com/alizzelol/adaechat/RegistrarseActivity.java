package com.alizzelol.adaechat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

public class RegistrarseActivity extends AppCompatActivity {

    private EditText etNombre, etApellido, etEmail, etContraseña, etContraseña2, etTelefono;
    private Button btnRegistrarse;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Spinner spinnerRol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrarse);

        etNombre = findViewById(R.id.editTextNombre);
        etApellido = findViewById(R.id.editTextApellido);
        etEmail = findViewById(R.id.editTextEmail);
        etContraseña = findViewById(R.id.editTextPassword);
        etContraseña2 = findViewById(R.id.editTextPassword2);
        etTelefono = findViewById(R.id.editTextTelefono);
        btnRegistrarse = findViewById(R.id.buttonAñadir);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        spinnerRol = findViewById(R.id.spinnerRol);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.roles_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRol.setAdapter(adapter);

        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombre = etNombre.getText().toString();
                String apellido = etApellido.getText().toString();
                String email = etEmail.getText().toString();
                String contraseña = etContraseña.getText().toString();
                String contraseña2 = etContraseña2.getText().toString();
                String telefono = etTelefono.getText().toString();
                String rol = spinnerRol.getSelectedItem().toString();

                if (!contraseña.equals(contraseña2)) {
                    Toast.makeText(RegistrarseActivity.this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, contraseña)
                        .addOnCompleteListener(RegistrarseActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    String uid = mAuth.getCurrentUser().getUid();

                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("nombre", nombre);
                                    userData.put("apellido", apellido);
                                    userData.put("email", email);
                                    userData.put("telefono", telefono);
                                    userData.put("rol", rol);

                                    db.collection("usuarios").document(uid).set(userData)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task2) {
                                                    if (task2.isSuccessful()) {
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