/* Copyright 2018 charlag
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

package com.gab.gabby.di

import com.gab.gabby.GabbyApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton


/**
 * Created by charlag on 3/21/18.
 */

@Singleton
@Component(modules = [
    AppModule::class,
    NetworkModule::class,
    AndroidSupportInjectionModule::class,
    ActivitiesModule::class,
    ServicesModule::class,
    BroadcastReceiverModule::class,
    ViewModelModule::class,
    RepositoryModule::class
])
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(gabbyApp: GabbyApplication): Builder

        fun build(): AppComponent
    }

    fun inject(app: GabbyApplication)
}