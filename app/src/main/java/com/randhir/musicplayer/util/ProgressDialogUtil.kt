/*
 * Copyright 26/1/18 11:51 PM randhirgupta
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

package com.randhir.musicplayer.util

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.Window
import android.widget.TextView
import com.randhir.musicplayer.R


/**
 * @author randhirgupta
 *  @since 26/1/18.
 */
class ProgressDialogUtil {

    private var mDialog: Dialog? = null

    fun showProgressDialog(context: Context, message: String) {

        mDialog = Dialog(context)
        mDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog?.setContentView(R.layout.progress_dialog_layout)

        val textMessage = mDialog?.findViewById<TextView>(R.id.progress_text)
        textMessage?.text = message

        mDialog?.setCancelable(false)
    }

    fun dismissDialog() {
        if (mDialog != null && mDialog?.isShowing!!) {
            Log.d("MMMM", "Here here")
            mDialog?.dismiss()
            mDialog = null
        }
    }
}