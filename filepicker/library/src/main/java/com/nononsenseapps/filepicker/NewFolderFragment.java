/*
 * Copyright (c) 2015 Jonas Kalderstam
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nononsenseapps.filepicker;


import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

public class NewFolderFragment extends NewItemFragment {

    private static final String TAG = "new_folder_fragment";

    public static void showDialog(final FragmentManager fm, final OnNewFolderListener listener) {
        NewItemFragment d = new NewFolderFragment();
        d.setListener(listener);
        d.show(fm, TAG);
    }

    @Override
    protected boolean validateName(final String itemName) {
        return !TextUtils.isEmpty(itemName)
                && !itemName.contains("/")
                && !itemName.equals(".")
                && !itemName.equals("..");
    }
}
