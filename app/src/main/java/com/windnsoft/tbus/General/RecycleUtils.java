

/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.windnsoft.tbus.General;

import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ImageView;

/**
 * @author givenjazz
 * @modified DEV1
 */
public class RecycleUtils {
    private RecycleUtils() {
    }

    ;

    public static void RecursiveRecycle(View root) {
        if (root == null)
            return;

        try {
            root.setBackground(null);
            root.setTag(null);

            if (root instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) root;
                int count = group.getChildCount();
                for (int idx = 0; idx < count; idx++) {
                    RecursiveRecycle(group.getChildAt(idx));
                }

                if (!(root instanceof AdapterView)) {
                    group.removeAllViews();
                }
            }

            if (root instanceof ImageView) {
                ((ImageView) root).setImageDrawable(null);
            }

            if (root instanceof WebView) {
                ((WebView) root).removeAllViews();
                ((WebView) root).destroy();
            }

            root = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return;
    }
}