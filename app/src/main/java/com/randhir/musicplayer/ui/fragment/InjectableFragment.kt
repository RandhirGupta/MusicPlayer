/*
 * Copyright 25/1/18 4:59 PM randhirgupta
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

package com.randhir.musicplayer.ui.fragment

import android.content.Context
import android.support.annotation.CallSuper
import android.support.annotation.NonNull
import android.support.v4.app.Fragment
import com.randhir.musicplayer.ui.fragment.BaseFragment

/**
 * @author randhirgupta
 *  @since 25/1/18.
 */
abstract class InjectableFragment : BaseFragment() {
    protected abstract fun inject(@NonNull fragment: Fragment): Unit

    @CallSuper
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        inject(this)
    }
}