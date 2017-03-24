/**
 * Created by Chris on 7/18/2015.
 */
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

        import android.os.Environment;

        import com.bumptech.glide.Glide;
        import com.nononsenseapps.filepicker.AbstractFilePickerActivity;
        import com.nononsenseapps.filepicker.AbstractFilePickerFragment;

        import java.io.File;

/**
 * All this class does is return a suitable fragment.
 */
public class ImagePickerActivity extends AbstractFilePickerActivity {
    private AbstractFilePickerActivity activity;

    public ImagePickerActivity() {
        super();
    }

    @Override
    protected AbstractFilePickerFragment<File> getFragment(
            final String startPath, final int mode, final boolean allowMultiple,
            final boolean allowCreateDir) {

        activity = this;

        Glide.get(activity).clearMemory();
        Thread thread = new Thread() {
            public void run() {
                Glide.get(activity).clearDiskCache();
            }
        };
        thread.start();

        AbstractFilePickerFragment<File> fragment = new ImagePickerFragment();
        // startPath is allowed to be null. In that case, default folder should be SD-card and not "/"
        fragment.setArgs(startPath != null ? startPath : Environment.getExternalStorageDirectory().getPath(),
                mode, allowMultiple, allowCreateDir);
        return fragment;
    }
}