/* Copyright 2017 Andrew Dawson
 *
 * This file is a part of Gabby.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Gabby is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Gabby; if not,
 * see <http://www.gnu.org/licenses>. */

package com.gab.gabby.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gab.gabby.R;
import com.gab.gabby.entity.Account;
import com.gab.gabby.interfaces.AccountActionListener;
import com.gab.gabby.util.CustomEmojiHelper;

public class BlocksAdapter extends AccountAdapter {

    public BlocksAdapter(AccountActionListener accountActionListener) {
        super(accountActionListener);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            default:
            case VIEW_TYPE_ACCOUNT: {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_blocked_user, parent, false);
                return new BlockedUserViewHolder(view);
            }
            case VIEW_TYPE_FOOTER: {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_footer, parent, false);
                return new LoadingFooterViewHolder(view);
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_ACCOUNT) {
            BlockedUserViewHolder holder = (BlockedUserViewHolder) viewHolder;
            holder.setupWithAccount(accountList.get(position));
            holder.setupActionListener(accountActionListener);
        }
    }

    static class BlockedUserViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatar;
        private TextView username;
        private TextView displayName;
        private ImageButton unblock;
        private String id;

        BlockedUserViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.blocked_user_avatar);
            username = itemView.findViewById(R.id.blocked_user_username);
            displayName = itemView.findViewById(R.id.blocked_user_display_name);
            unblock = itemView.findViewById(R.id.blocked_user_unblock);
        }

        void setupWithAccount(Account account) {
            id = account.getId();
            CharSequence emojifiedName = CustomEmojiHelper.emojifyString(account.getName(), account.getEmojis(), displayName);
            displayName.setText(emojifiedName);
            String format = username.getContext().getString(R.string.status_username_format);
            String formattedUsername = String.format(format, account.getUsername());
            username.setText(formattedUsername);
            Glide.with(avatar)
                    .asBitmap()
                    .load(account.getAvatar())
                    .placeholder(R.drawable.avatar_default)
                    .into(avatar);
        }

        void setupActionListener(final AccountActionListener listener) {
            unblock.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onBlock(false, id, position);
                }
            });
            avatar.setOnClickListener(v -> listener.onViewAccount(id));
        }
    }
}
