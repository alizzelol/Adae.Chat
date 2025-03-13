package com.alizzelol.adaechat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements ConversationAdapter.OnItemClickListener{

    private RecyclerView rvConversations;
    private FloatingActionButton fabNewChat;
    private String username;
    private ConversationAdapter adapter;
    private List<Conversation> conversations;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ActivityResultLauncher<Intent> conversationLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        rvConversations = findViewById(R.id.rvConversations);
        fabNewChat = findViewById(R.id.fabNewChat);
        username = getIntent().getStringExtra("username");

        conversations = new ArrayList<>();
        // Pasar el username al adapter
        adapter = new ConversationAdapter(conversations, username, this);
        rvConversations.setLayoutManager(new LinearLayoutManager(this));
        rvConversations.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        conversationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        loadConversations(); // Actualizar la lista de conversaciones
                    }
                }
        );

        fabNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, ContactListActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        loadConversations();
        setupRecyclerViewListener();
    }

    private void loadConversations() {
        db.collection("chats")
                .whereArrayContains("users", username)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("ChatActivity", "Error al escuchar las conversaciones: " + e.getMessage());
                        return;
                    }

                    if (snapshots != null) {
                        conversations.clear();
                        for (DocumentSnapshot document : snapshots) {
                            Conversation conversation = document.toObject(Conversation.class);
                            if (conversation != null && (conversation.getDeletedBy() == null || !conversation.getDeletedBy().contains(username))) {
                                conversations.add(conversation);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onItemClick(Conversation conversation) {
        Intent intent = new Intent(ChatActivity.this, ConversationActivity.class);
        intent.putExtra("username", username);

        List<String> users = conversation.getUsers();
        String contactUsername = "";
        for (String user : users) {
            if (!user.equals(username)) {
                contactUsername = user;
                break;
            }
        }

        intent.putExtra("contactUsername", contactUsername);
        intent.putExtra("conversationId", conversation.getConversationId());
        conversationLauncher.launch(intent); // Usando conversationLauncher
    }

    private void setupRecyclerViewListener() {
        rvConversations.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                ImageButton btnDelete = view.findViewById(R.id.btnDeleteConversation);
                if (btnDelete != null) {
                    btnDelete.setOnClickListener(v -> {
                        String conversationId = (String) btnDelete.getTag();
                        deleteConversation(conversationId);
                    });
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
            }
        });
    }

    private void deleteConversation(String conversationId) {
        db.collection("chats").document(conversationId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Conversation conversation = documentSnapshot.toObject(Conversation.class);
                    if (conversation != null) {
                        List<String> deletedBy = conversation.getDeletedBy();
                        if (deletedBy == null) {
                            deletedBy = new ArrayList<>();
                        }
                        deletedBy.add(username);
                        db.collection("chats").document(conversationId).update("deletedBy", deletedBy)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("ChatActivity", "Conversación marcada como eliminada para el usuario.");
                                    loadConversations(); // Recargar las conversaciones después de la eliminación
                                })
                                .addOnFailureListener(e -> Log.e("ChatActivity", "Error al marcar conversación como eliminada: " + e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> Log.e("ChatActivity", "Error al obtener conversación: " + e.getMessage()));
    }
}