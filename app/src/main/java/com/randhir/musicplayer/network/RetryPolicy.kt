/*
 * Copyright 25/1/18 4:42 PM randhirgupta
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

package com.randhir.musicplayer.network

import io.reactivex.functions.BiPredicate
import java.net.SocketTimeoutException

/**
 * @author randhirgupta
 *  @since 25/1/18.
 */
class RetryPolicy {
    companion object {
        private val HIGH: Int = 3
        private val MEDIUM: Int = 2
        private val LOW: Int = 1

        fun default(): BiPredicate<Int, Throwable> {
            return BiPredicate { retryCount, throwable -> retryCount <= LOW && throwable is SocketTimeoutException }
        }

        fun socketTimeout(): BiPredicate<Int, Throwable> {
            return BiPredicate { retryCount, throwable -> retryCount <= MEDIUM && throwable is SocketTimeoutException }
        }

        fun authorization(): BiPredicate<Int, Throwable> {
            return BiPredicate { retryCount, throwable -> retryCount <= HIGH && throwable is SocketTimeoutException }
        }

        fun none(): BiPredicate<Int, Throwable> {
            return BiPredicate { _, _ -> false }
        }
    }
}