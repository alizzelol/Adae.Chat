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
import com.google.firebase.Timestamp;
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

            if (username != null && contactUsername != null) {
                tvContactName.setText(contactUsername);

                messages = new ArrayList<>();
                adapter = new MessageAdapter(messages, username);
                rvMessages.setLayoutManager(new LinearLayoutManager(this));
                rvMessages.setAdapter(adapter);

                db = FirebaseFirestore.getInstance();
                mAuth = FirebaseAuth.getInstance();

                conversationId = generateConversationId(username, contactUsername);

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
                            setResult(Activity.RESULT_OK); // Indicar que la conversación se ha abierto
                            Intent intent = new Intent(ConversationActivity.this, ChatActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // Asegurar que ChatActivity se inicie correctamente
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
        return usernames[0] + "_" + usernames[1];
    }


    private void loadMessages() {
        db.collection("chats").document(conversationId).collection("mensajes")
                .orderBy("timestamp")
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
                                switch (dc.getType()) {
                                    case ADDED:
                                        messages.add(message);
                                        break;
                                    case MODIFIED:
                                        // Handle modified document
                                        break;
                                    case REMOVED:
                                        // Handle removed document
                                        break;
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void sendMessage(String messageText) {
        List<String> users = Arrays.asList(username, contactUsername); // Crea el array de usuarios
        Map<String, Object> conversationData = new HashMap<>();
        conversationData.put("users", users); // Agrega el array al mapa de datos
        conversationData.put("lastMessage", messageText);
        conversationData.put("lastMessageTimestamp", new Date());

        db.collection("chats").document(conversationId).set(conversationData, SetOptions.merge()) // Crea o actualiza el documento de conversación
                .addOnSuccessListener(aVoid -> {
                    Mensaje message = new Mensaje(username, contactUsername, messageText, new Date());
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