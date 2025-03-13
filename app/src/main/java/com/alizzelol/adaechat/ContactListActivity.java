package com.alizzelol.adaechat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ContactListActivity extends AppCompatActivity {

    private RecyclerView rvContacts;
    private ContactListAdapter adapter;
    private List<User> contacts;
    private FirebaseFirestore db;
    private String username;
    private String userId; // Añadido userId
    private ActivityResultLauncher<Intent> conversationLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactlist);

        rvContacts = findViewById(R.id.rvContacts);
        username = getIntent().getStringExtra("username");
        userId = getIntent().getStringExtra("userId"); // Obtener userId

        Log.d("ContactListActivity", "UserId: " + userId);

        contacts = new ArrayList<>();
        adapter = new ContactListAdapter(contacts, username, this); // Pasar username al adapter
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        rvContacts.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        conversationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // No necesitamos hacer nada aquí
                }
        );

        loadContacts();
    }

    private void loadContacts() {
        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    contacts.clear();
                    for (DocumentSnapshot document : task.getResult()) {
                        User user = document.toObject(User.class);
                        if (user != null && !user.getUserId().equals(userId)) { // Usar userId para la comparación
                            contacts.add(user);
                        }
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("ContactListActivity", "Error al cargar los contactos: " + task.getException());
                    Toast.makeText(ContactListActivity.this, "Error al cargar los contactos.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void startConversationActivity(String contactUsername) { // Recibe contactUsername
        String conversationId = generateConversationId(username, contactUsername); // Usa username
        List<String> users = Arrays.asList(username, contactUsername); // Usa username
        List<String> deletedBy = new ArrayList<>();

        Log.d("ContactListActivity", "Starting conversation with: " + contactUsername);
        Log.d("ContactListActivity", "Generated ConversationId: " + conversationId);

        db.collection("chats").document(conversationId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("ContactListActivity", "Conversation already exists.");
                        Intent intent = new Intent(ContactListActivity.this, ConversationActivity.class);
                        intent.putExtra("username", username);
                        intent.putExtra("userId", userId); // Pasar userId
                        intent.putExtra("contactUsername", contactUsername); // Pasar contactUsername
                        intent.putExtra("conversationId", conversationId);
                        conversationLauncher.launch(intent);
                    } else {
                        Log.d("ContactListActivity", "Creating new conversation.");
                        Conversation conversation = new Conversation(conversationId, contactUsername, "", new Date(), users, deletedBy); // Usa contactUsername

                        db.collection("chats").document(conversationId).set(conversation)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("ContactListActivity", "Conversation created successfully.");
                                    Intent intent = new Intent(ContactListActivity.this, ConversationActivity.class);
                                    intent.putExtra("username", username);
                                    intent.putExtra("userId", userId); // Pasar userId
                                    intent.putExtra("contactUsername", contactUsername); // Pasar contactUsername
                                    intent.putExtra("conversationId", conversationId);
                                    conversationLauncher.launch(intent);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("ContactListActivity", "Error al crear la conversación: " + e.getMessage());
                                    Toast.makeText(ContactListActivity.this, "Error al crear la conversación.", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ContactListActivity", "Error al verificar la conversación: " + e.getMessage());
                    Toast.makeText(ContactListActivity.this, "Error al verificar la conversación.", Toast.LENGTH_SHORT).show();
                });
    }

    private String generateConversationId(String username1, String username2) { // Usa username
        String[] usernames = {username1, username2}; // Usa username
        Arrays.sort(usernames); // Usa username
        String generatedId = usernames[0] + "_" + usernames[1]; // Usa username
        Log.d("ContactListActivity", "Generated ConversationId: " + generatedId);
        return generatedId;
    }
}