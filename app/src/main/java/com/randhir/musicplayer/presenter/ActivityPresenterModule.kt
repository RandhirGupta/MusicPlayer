/*
 * Copyright 25/1/18 4:48 PM randhirgupta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.randhir.musicplayer.presenter

import android.app.Activity
import com.randhir.musicplayer.annotations.UserScope
import com.randhir.musicplayer.presenter.main.MainPresenter
import dagger.Module
import dagger.Provides

/**
 * @author randhirgupta
 *  @since 25/1/18.
 */
@Module
class ActivityPresenterModule {

    @Provides
    @UserScope
    fun provideMainPresenter(context: Activity): MainPresenter {
        return MainPresenter(context)
    }

}