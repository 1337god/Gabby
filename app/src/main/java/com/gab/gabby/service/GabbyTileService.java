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

package com.gab.gabby.service;

import android.annotation.TargetApi;
import android.content.Intent;
import android.service.quicksettings.TileService;

import com.gab.gabby.ComposeActivity;

/**
 * Small Addition that adds in a QuickSettings tile that opens the Compose activity when clicked
 * Created by ztepps on 4/3/17.
 */

@TargetApi(24)
public class GabbyTileService extends TileService {
    public GabbyTileService() {
        super();
    }

    @Override
    public void onClick() {
        Intent intent = new Intent(this, ComposeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityAndCollapse(intent);
    }
}
