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

package com.gab.gabby.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import android.provider.Settings;
import android.util.TypedValue;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Provides runtime compatibility to obtain theme information and re-theme views, especially where
 * the ability to do so is not supported in resource files.
 */
@Singleton
public class ThemeUtils {

    @Inject
    public ThemeUtils(){}

    public static final String APP_THEME_DEFAULT = ThemeUtils.THEME_NIGHT;

    private static final String THEME_NIGHT = "night";
    private static final String THEME_DAY = "day";
    private static final String THEME_BLACK = "black";
    private static final String THEME_AUTO = "auto";
    public static final String THEME_SYSTEM = "auto_system";

    public static Drawable getDrawable(@NonNull Context context, @AttrRes int attribute,
            @DrawableRes int fallbackDrawable) {
        TypedValue value = new TypedValue();
        @DrawableRes int resourceId;
        if (context.getTheme().resolveAttribute(attribute, value, true)) {
            resourceId = value.resourceId;
        } else {
            resourceId = fallbackDrawable;
        }
        return context.getDrawable(resourceId);
    }

    public static @DrawableRes int getDrawableId(@NonNull Context context, @AttrRes int attribute,
            @DrawableRes int fallbackDrawableId) {
        TypedValue value = new TypedValue();
        if (context.getTheme().resolveAttribute(attribute, value, true)) {
            return value.resourceId;
        } else {
            return fallbackDrawableId;
        }
    }

    public static @ColorInt int getColor(@NonNull Context context, @AttrRes int attribute) {
        TypedValue value = new TypedValue();
        if (context.getTheme().resolveAttribute(attribute, value, true)) {
            return value.data;
        } else {
            return Color.BLACK;
        }
    }

    public static @ColorRes int getColorId(@NonNull Context context, @AttrRes int attribute) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attribute, value, true);
        return value.resourceId;
    }

    /** this can be replaced with drawableTint in xml once minSdkVersion >= 23 */
    public static @Nullable Drawable getTintedDrawable(@NonNull Context context, @DrawableRes int drawableId, @AttrRes int colorAttr) {
        Drawable drawable = context.getDrawable(drawableId);
        if(drawable == null) {
            return null;
        }
        setDrawableTint(context, drawable, colorAttr);
        return drawable;
    }

    public static void setDrawableTint(Context context, Drawable drawable, @AttrRes int attribute) {
        drawable.setColorFilter(getColor(context, attribute), PorterDuff.Mode.SRC_IN);
    }

    public void setAppNightMode(String flavor, Context context) {
        switch (flavor) {
            default:
            case THEME_NIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_DAY:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_BLACK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_AUTO:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
                break;
            case THEME_SYSTEM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

                //stupid workaround to make MODE_NIGHT_FOLLOW_SYSTEM work :(
                if((Settings.System.getInt(context.getContentResolver(), "display_night_theme", 0) == 1)) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else if ((Settings.System.getInt(context.getContentResolver(), "display_night_theme", 0) == 0)) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                break;
        }
    }
}
