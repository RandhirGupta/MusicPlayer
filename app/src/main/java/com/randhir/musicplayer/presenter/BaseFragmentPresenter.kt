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

import android.support.annotation.CallSuper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.net.SocketTimeoutException

/**
 * @author randhirgupta
 *  @since 25/1/18.
 */
abstract class BaseFragmentPresenter<T> {

    private var disposables: CompositeDisposable? = CompositeDisposable()
    abstract var view: T?

    @CallSuper
    open fun onCreate() {
    }

    @CallSuper
    open fun onResume() {
    }

    @CallSuper
    open fun onPause() {
    }

    @CallSuper
    open fun onStop() {
    }

    @CallSuper
    open fun onDestroyView() {
        disposables?.clear()
        disposables = null
        view = null
    }

    protected fun isTimeoutError(throwable: Throwable): Boolean {
        return throwable is SocketTimeoutException
    }

    protected fun addToDisposable(disposable: Disposable) {
        disposables?.add(disposable)
    }
}