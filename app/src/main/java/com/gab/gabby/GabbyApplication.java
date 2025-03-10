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

package com.gab.gabby;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import androidx.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import androidx.emoji.text.EmojiCompat;

import com.evernote.android.job.JobManager;
import com.gab.gabby.db.AccountManager;
import com.gab.gabby.db.AppDatabase;
import com.gab.gabby.di.AppInjector;
import com.gab.gabby.util.EmojiCompatFont;
import com.gab.gabby.util.LocaleManager;
import com.gab.gabby.util.NotificationPullJobCreator;

import org.conscrypt.Conscrypt;

import java.security.Security;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasBroadcastReceiverInjector;
import dagger.android.HasServiceInjector;

public class GabbyApplication extends Application implements HasActivityInjector, HasServiceInjector, HasBroadcastReceiverInjector {
    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;
    @Inject
    DispatchingAndroidInjector<Service> dispatchingServiceInjector;
    @Inject
    DispatchingAndroidInjector<BroadcastReceiver> dispatchingBroadcastReceiverInjector;
    @Inject
    NotificationPullJobCreator notificationPullJobCreator;

    private AppDatabase appDatabase;
    private AccountManager accountManager;

    private ServiceLocator serviceLocator;

    public static LocaleManager localeManager;

    @Override
    public void onCreate() {
        super.onCreate();

        initSecurityProvider();

        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "gabbyDB")
                .allowMainThreadQueries()
                .addMigrations(AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4, AppDatabase.MIGRATION_4_5,
                        AppDatabase.MIGRATION_5_6, AppDatabase.MIGRATION_6_7, AppDatabase.MIGRATION_7_8,
                        AppDatabase.MIGRATION_8_9, AppDatabase.MIGRATION_9_10, AppDatabase.MIGRATION_10_11,
                        AppDatabase.MIGRATION_11_12, AppDatabase.MIGRATION_12_13, AppDatabase.MIGRATION_10_13,
                        AppDatabase.MIGRATION_13_14, AppDatabase.MIGRATION_14_15, AppDatabase.MIGRATION_15_16,
                        AppDatabase.MIGRATION_16_17)
                .build();
        accountManager = new AccountManager(appDatabase);
        serviceLocator = new ServiceLocator() {
            @Override
            public <T> T get(Class<T> clazz) {
                if (clazz.equals(AccountManager.class)) {
                    //noinspection unchecked
                    return (T) accountManager;
                } else if (clazz.equals(AppDatabase.class)) {
                    //noinspection unchecked
                    return (T) appDatabase;
                } else {
                    throw new IllegalArgumentException("Unknown service " + clazz);
                }
            }
        };

        initAppInjector();
        initEmojiCompat();

        JobManager.create(this).addJobCreator(notificationPullJobCreator);

    }

    protected void initSecurityProvider() {
        Security.insertProviderAt(Conscrypt.newProvider(), 1);
    }

    @Override
    protected void attachBaseContext(Context base) {
        localeManager = new LocaleManager(base);
        super.attachBaseContext(localeManager.setLocale(base));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        localeManager.setLocale(this);
    }

    /**
     * This method will load the EmojiCompat font which has been selected.
     * If this font does not work or if the user hasn't selected one (yet), it will use a
     * fallback solution instead which won't make any visible difference to using no EmojiCompat at all.
     */
    private void initEmojiCompat() {
        int emojiSelection = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext())
                .getInt(EmojiPreference.FONT_PREFERENCE, 0);
        EmojiCompatFont font = EmojiCompatFont.byId(emojiSelection);
        // FileEmojiCompat will handle any non-existing font and provide a fallback solution.
        EmojiCompat.Config config = font.getConfig(getApplicationContext())
                // The user probably wants to get a consistent experience
                .setReplaceAll(true);
        EmojiCompat.init(config);
    }

    protected void initAppInjector() {
        AppInjector.INSTANCE.init(this);
    }

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingServiceInjector;
    }

    @Override
    public AndroidInjector<BroadcastReceiver> broadcastReceiverInjector() {
        return dispatchingBroadcastReceiverInjector;
    }

    public interface ServiceLocator {
        <T> T get(Class<T> clazz);
    }
}
