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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.ReplacementSpan;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.gab.gabby.entity.Emoji;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomEmojiHelper {

    /**
     * replaces emoji shortcodes in a text with EmojiSpans
     * @param text the text containing custom emojis
     * @param emojis a list of the custom emojis (nullable for backward compatibility with old mastodon instances)
     * @param view a reference to the a view the emojis will be shown in (should be the TextView, but parents of the TextView are also acceptable)
     * @return the text with the shortcodes replaced by EmojiSpans
     */
    public static Spanned emojifyText(@NonNull Spanned text, @Nullable List<Emoji> emojis, @NonNull final View view) {

        if (emojis != null && !emojis.isEmpty()) {

            SpannableStringBuilder builder = new SpannableStringBuilder(text);
            for (Emoji emoji : emojis) {
                CharSequence pattern = new StringBuilder(":").append(emoji.getShortcode()).append(':');
                Matcher matcher = Pattern.compile(pattern.toString()).matcher(text);
                while (matcher.find()) {
                    EmojiSpan span = new EmojiSpan(view);
                    builder.setSpan(span, matcher.start(), matcher.end(), 0);
                    Glide.with(view)
                            .asBitmap()
                            .load(emoji.getUrl())
                            .into(span.getTarget());

                }
            }

            return builder;
        }

        return text;
    }

    public static Spanned emojifyString(@NonNull String string, @Nullable List<Emoji> emojis, @NonNull final View ciew) {
        return emojifyText(new SpannedString(string), emojis, ciew);
    }


    public static class EmojiSpan extends ReplacementSpan {

        private @Nullable Drawable imageDrawable;
        private WeakReference<View> viewWeakReference;

        EmojiSpan(View view) {
            this.viewWeakReference = new WeakReference<>(view);
        }

        @Override
        public int getSize(@NonNull Paint paint, CharSequence text, int start, int end,
                           @Nullable Paint.FontMetricsInt fm) {

            /* update FontMetricsInt or otherwise span does not get drawn when
               it covers the whole text */
            Paint.FontMetricsInt metrics = paint.getFontMetricsInt();
            if (fm != null) {
                fm.top = metrics.top;
                fm.ascent = metrics.ascent;
                fm.descent = metrics.descent;
                fm.bottom = metrics.bottom;
            }

            return (int) (paint.getTextSize()*1.2);
        }

        @Override
        public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x,
                         int top, int y, int bottom, @NonNull Paint paint) {
            if (imageDrawable == null) return;
            canvas.save();

            int emojiSize = (int) (paint.getTextSize() * 1.1);
            imageDrawable.setBounds(0, 0, emojiSize, emojiSize);

            int transY = bottom - imageDrawable.getBounds().bottom;
            transY -= paint.getFontMetricsInt().descent/2;
            canvas.translate(x, transY);
            imageDrawable.draw(canvas);
            canvas.restore();
        }

        Target<Bitmap> getTarget(){
            return new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    View view = viewWeakReference.get();
                    if (view != null) {
                        imageDrawable = new BitmapDrawable(view.getContext().getResources(), resource);
                        view.invalidate();
                    }
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {
                    //Do nothing on load cleared
                }
            };
        }
    }

}
