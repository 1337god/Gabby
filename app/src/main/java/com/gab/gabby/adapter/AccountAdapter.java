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
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.gab.gabby.entity.Account;
import com.gab.gabby.interfaces.AccountActionListener;
import com.gab.gabby.util.ListUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AccountAdapter extends RecyclerView.Adapter {
    static final int VIEW_TYPE_ACCOUNT = 0;
    static final int VIEW_TYPE_FOOTER = 1;

    List<Account> accountList;
    AccountActionListener accountActionListener;
    private boolean bottomLoading;

    AccountAdapter(AccountActionListener accountActionListener) {
        this.accountList = new ArrayList<>();
        this.accountActionListener = accountActionListener;
        bottomLoading = false;
    }

    @Override
    public int getItemCount() {
        return accountList.size() + (bottomLoading ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == accountList.size() && bottomLoading) {
            return VIEW_TYPE_FOOTER;
        } else {
            return VIEW_TYPE_ACCOUNT;
        }
    }

    public void update(@NonNull List<Account> newAccounts) {
        accountList = ListUtils.removeDuplicates(newAccounts);
        notifyDataSetChanged();
    }

    public void addItems(@NonNull List<Account> newAccounts) {
        int end = accountList.size();
        Account last = accountList.get(end - 1);
        if (last != null && !findAccount(newAccounts, last.getId())) {
            accountList.addAll(newAccounts);
            notifyItemRangeInserted(end, newAccounts.size());
        }
    }

    public void setBottomLoading(boolean loading) {
        boolean wasLoading = bottomLoading;
        if(wasLoading == loading) {
            return;
        }
        bottomLoading = loading;
        if(loading) {
            notifyItemInserted(accountList.size());
        } else {
            notifyItemRemoved(accountList.size());
        }
    }

    private static boolean findAccount(@NonNull List<Account> accounts, String id) {
        for (Account account : accounts) {
            if (account.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public Account removeItem(int position) {
        if (position < 0 || position >= accountList.size()) {
            return null;
        }
        Account account = accountList.remove(position);
        notifyItemRemoved(position);
        return account;
    }

    public void addItem(@NonNull Account account, int position) {
        if (position < 0 || position > accountList.size()) {
            return;
        }
        accountList.add(position, account);
        notifyItemInserted(position);
    }


}
