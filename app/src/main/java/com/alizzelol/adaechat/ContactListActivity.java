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
    private ActivityResultLauncher<Intent> conversationLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactlist);

        rvContacts = findViewById(R.id.rvContacts);
        username = getIntent().getStringExtra("username");

        Log.d("ContactListActivity", "Username: " + username);

        contacts = new ArrayList<>();
        adapter = new ContactListAdapter(contacts, username, this);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        rvContacts.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        conversationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
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
                        if (user != null && !user.getUsername().equals(username)) {
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

    public void startConversationActivity(String contactUsername) {
        String conversationId = generateConversationId(username, contactUsername);
        List<String> users = Arrays.asList(username, contactUsername);
        List<String> deletedBy = new ArrayList<>();

        Log.d("ContactListActivity", "Starting conversation with: " + contactUsername);
        Log.d("ContactListActivity", "Generated ConversationId: " + conversationId);

        db.collection("chats").document(conversationId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("ContactListActivity", "Conversation already exists.");
                        Intent intent = new Intent(ContactListActivity.this, ConversationActivity.class);
                        intent.putExtra("username", username);
                        intent.putExtra("contactUsername", contactUsername);
                        intent.putExtra("conversationId", conversationId);
                        conversationLauncher.launch(intent);
                    } else {
                        Log.d("ContactListActivity", "Creating new conversation.");
                        Conversation conversation = new Conversation(conversationId, contactUsername, "", new Date(), users, deletedBy);

                        db.collection("chats").document(conversationId).set(conversation)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("ContactListActivity", "Conversation created successfully.");
                                    Intent intent = new Intent(ContactListActivity.this, ConversationActivity.class);
                                    intent.putExtra("username", username);
                                    intent.putExtra("contactUsername", contactUsername);
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

    private String generateConversationId(String username1, String username2) {
        String[] usernames = {username1, username2};
        Arrays.sort(usernames);
        String generatedId = usernames[0] + "_" + usernames[1];
        Log.d("ContactListActivity", "Generated ConversationId: " + generatedId);
        return generatedId;
    }
}