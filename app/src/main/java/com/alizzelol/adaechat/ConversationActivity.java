package com.alizzelol.adaechat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.firebase.firestore.EventListener;
import java.util.*;

public class ConversationActivity extends AppCompatActivity {

    private TextView tvContactName;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private Button btnSend;
    private MessageAdapter adapter;
    private List<Mensaje> messages;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String username, contactUsername, conversationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        tvContactName = findViewById(R.id.tvContactName);
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        Intent intent = getIntent();
        if (intent != null) {
            username = intent.getStringExtra("username");
            contactUsername = intent.getStringExtra("contactUsername");

            Log.d("ConversationActivity", "Username: " + username);
            Log.d("ConversationActivity", "ContactUsername: " + contactUsername);

            if (username != null && contactUsername != null) {
                List<String> users = Arrays.asList(username, contactUsername);
                String interlocutor = "";
                for (String user : users) {
                    if (!user.equals(username)) {
                        interlocutor = user;
                        break;
                    }
                }
                tvContactName.setText(interlocutor);

                Log.d("ConversationActivity", "Users: " + users.toString());
                Log.d("ConversationActivity", "Interlocutor: " + interlocutor);

                messages = new ArrayList<>();
                adapter = new MessageAdapter(messages, username);
                rvMessages.setLayoutManager(new LinearLayoutManager(this));
                rvMessages.setAdapter(adapter);

                db = FirebaseFirestore.getInstance();
                mAuth = FirebaseAuth.getInstance();

                conversationId = generateConversationId(username, contactUsername);

                Log.d("ConversationActivity", "ConversationId: " + conversationId);

                if(conversationId != null) {
                    loadMessages();

                    btnSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String messageText = etMessage.getText().toString().trim();
                            if (!messageText.isEmpty()) {
                                sendMessage(messageText);
                                etMessage.setText("");
                            }
                        }
                    });

                    getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                        @Override
                        public void handleOnBackPressed() {
                            setResult(Activity.RESULT_OK);
                            Intent intent = new Intent(ConversationActivity.this, ChatActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            finish();
                        }
                    });

                } else {
                    Log.e("ConversationActivity", "Error: conversationId es nulo.");
                    finish();
                }

            } else {
                Log.e("ConversationActivity", "Error: Nombres de usuario nulos en Intent.");
                finish();
            }
        } else {
            Log.e("ConversationActivity", "Error: Intent nulo.");
            finish();
        }
    }

    private String generateConversationId(String username1, String username2) {
        if (username1 == null || username2 == null) {
            Log.e("ConversationActivity", "Error: Uno o ambos nombres de usuario son nulos.");
            return null;
        }

        String[] usernames = {username1, username2};
        Arrays.sort(usernames);
        String generatedId = usernames[0] + "_" + usernames[1];
        Log.d("ConversationActivity", "Generated ConversationId: " + generatedId); // Log del ID generado
        return generatedId;
    }

    private void loadMessages() {
        db.collection("chats").document(conversationId).collection("mensajes")
                .orderBy("timestamp", Query.Direction.ASCENDING) // Ordenar por timestamp
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot snapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e("ConversationActivity", "Error al cargar mensajes: " + e.getMessage());
                            return;
                        }

                        if (snapshots != null) {
                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                DocumentSnapshot document = dc.getDocument();
                                Mensaje message = document.toObject(Mensaje.class);
                                if (message != null) {
                                    switch (dc.getType()) {
                                        case ADDED:
                                            messages.add(message); // Agregar nuevos mensajes
                                            break;
                                        case MODIFIED:
                                            // Manejar mensajes modificados (si es necesario)
                                            break;
                                        case REMOVED:
                                            // Manejar mensajes eliminados (si es necesario)
                                            break;
                                    }
                                }
                            }
                            adapter.notifyDataSetChanged(); // Notificar cambios al adapter
                        }
                    }
                });
    }

    private void sendMessage(String messageText) {
        List<String> users = Arrays.asList(username, contactUsername);
        Map<String, Object> conversationData = new HashMap<>();
        conversationData.put("users", users);
        conversationData.put("lastMessage", messageText);
        conversationData.put("lastMessageTimestamp", new Date());

        db.collection("chats").document(conversationId).set(conversationData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Mensaje message = new Mensaje(username, messageText, new Date());
                    db.collection("chats").document(conversationId).collection("mensajes").add(message)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (!task.isSuccessful()) {
                                        Log.e("ConversationActivity", "Error al enviar mensaje: " + task.getException());
                                    }
                                }
                            });
                })
                .addOnFailureListener(e -> Log.e("ConversationActivity", "Error al crear/actualizar conversación: " + e.getMessage()));
    }
}