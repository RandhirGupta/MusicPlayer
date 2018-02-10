/*
 * Copyright 25/1/18 5:05 PM randhirgupta
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

package com.randhir.musicplayer.ui.activity

import android.os.Bundle
import com.randhir.musicplayer.presenter.BasePresenter

/**
 * @author randhirgupta
 *  @since 25/1/18.
 */
abstract class PresenterActivity<T> : InjectableActivity() {

    protected abstract fun getPresenter(): BasePresenter<T>?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getPresenter()?.onCreate()
    }

    override fun onResume() {
        super.onResume()
        getPresenter()?.onResume()
    }

    override fun onPause() {
        super.onPause()
        getPresenter()?.onPause()
    }

    override fun onStop() {
        super.onStop()
        getPresenter()?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        getPresenter()?.onDestroy()
    }
}