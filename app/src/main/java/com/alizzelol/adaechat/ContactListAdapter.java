package com.alizzelol.adaechat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ContactViewHolder> {

    private List<User> contacts;
    private String username;
    private ContactListActivity activity;

    public ContactListAdapter(List<User> contacts, String username, ContactListActivity activity) {
        this.contacts = contacts;
        this.username = username;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_item, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        User contact = contacts.get(position);
        if (contact != null) {
            holder.tvContactName.setText(contact.getUsername());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String contactUsername = contact.getUsername();
                    if (contactUsername != null) {
                        Log.d("ContactListAdapter", "Contact clicked: " + contactUsername);
                        activity.startConversationActivity(contactUsername);
                    } else {
                        Log.e("ContactListAdapter", "Contact username is null");
                    }
                }
            });
        } else {
            Log.e("ContactListAdapter", "Contact is null at position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvContactName;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContactName = itemView.findViewById(R.id.tvContactName);
        }
    }
}