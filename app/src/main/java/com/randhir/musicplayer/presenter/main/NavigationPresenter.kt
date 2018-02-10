/*
 * Copyright 25/1/18 4:47 PM randhirgupta
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

package com.randhir.musicplayer.presenter.main

import android.support.v4.app.Fragment
import com.randhir.musicplayer.MusicPlayerApp
import com.randhir.musicplayer.component.DaggerPresenterComponent
import com.randhir.musicplayer.presenter.BaseFragmentPresenter
import com.randhir.musicplayer.presenter.PresenterView

/**
 * @author randhirgupta
 *  @since 25/1/18.
 */
class NavigationPresenter constructor(context: Fragment) : BaseFragmentPresenter<NavigationPresenter.View>() {
    override var view: View? = context as View

    init {
        DaggerPresenterComponent.builder()
                .appComponent(MusicPlayerApp.getAppComponent(context.context))
                .build().inject(this)
    }

    interface View : PresenterView
}