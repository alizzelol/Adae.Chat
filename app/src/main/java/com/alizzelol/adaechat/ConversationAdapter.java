package com.alizzelol.adaechat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private List<Conversation> conversations;
    private String currentUsername;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Conversation conversation);
    }

    public ConversationAdapter(List<Conversation> conversations, String currentUsername, OnItemClickListener listener) {
        this.conversations = conversations;
        this.currentUsername = currentUsername;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_item, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);

        String contactName = "";
        if (conversation.getUsers() != null && !conversation.getUsers().isEmpty()) {
            for (String user : conversation.getUsers()) {
                if (!user.equals(currentUsername)) {
                    contactName = user;
                    break;
                }
            }
        }

        holder.tvContactName.setText(contactName);
        holder.tvLastMessage.setText(conversation.getLastMessage());
        if (conversation.getLastTimestamp() != null) {
            String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(conversation.getLastTimestamp());
            holder.tvLastTimestamp.setText(formattedDate);
        } else {
            holder.tvLastTimestamp.setText("");
        }

        holder.btnDeleteConversation.setTag(conversation.getConversationId());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(conversation));
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public static class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView tvContactName, tvLastMessage, tvLastTimestamp;
        ImageButton btnDeleteConversation;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContactName = itemView.findViewById(R.id.tvContactName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvLastTimestamp = itemView.findViewById(R.id.tvLastTimestamp);
            btnDeleteConversation = itemView.findViewById(R.id.btnDeleteConversation);
        }
    }
}
