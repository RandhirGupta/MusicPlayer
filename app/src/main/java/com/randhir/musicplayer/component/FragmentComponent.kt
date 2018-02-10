/*
 * Copyright 25/1/18 4:12 PM randhirgupta
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

package com.randhir.musicplayer.component

import com.randhir.musicplayer.FragmentModule
import com.randhir.musicplayer.annotations.UserScope
import com.randhir.musicplayer.presenter.FragmentPresenterModule
import com.randhir.musicplayer.ui.fragment.main.MainFragment
import dagger.Component

/**
 * @author randhirgupta
 *  @since 25/1/18.
 */
@UserScope
@Component(dependencies = arrayOf(AppComponent::class), modules = arrayOf(
        FragmentPresenterModule::class,
        FragmentModule::class)
)
interface FragmentComponent {
    fun inject(context: MainFragment)
}