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

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.gab.gabby.R;
import com.gab.gabby.interfaces.StatusActionListener;

public final class PlaceholderViewHolder extends RecyclerView.ViewHolder {

    private Button loadMoreButton;
    private ProgressBar progressBar;

    PlaceholderViewHolder(View itemView) {
        super(itemView);
        loadMoreButton = itemView.findViewById(R.id.button_load_more);
        progressBar = itemView.findViewById(R.id.progressBar);
    }

    public void setup(final StatusActionListener listener, boolean progress) {
        loadMoreButton.setVisibility(progress ? View.GONE : View.VISIBLE);
        progressBar.setVisibility(progress ? View.VISIBLE : View.GONE);

        loadMoreButton.setEnabled(true);
        loadMoreButton.setOnClickListener(v -> {
            loadMoreButton.setEnabled(false);
            listener.onLoadMore(getAdapterPosition());
        });

    }

}