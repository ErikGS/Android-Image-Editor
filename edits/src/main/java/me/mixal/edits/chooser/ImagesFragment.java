/*
 * Copyright 2013 Thomas Hoffmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.mixal.edits.chooser;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import me.mixal.edits.R;


public class ImagesFragment extends Fragment {

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.gallery, null);

		assert getArguments() != null;
		Cursor cur = requireActivity().getContentResolver()
				.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
						new String[] { MediaStore.Images.Media.DATA,
								MediaStore.Images.Media.DISPLAY_NAME,
								MediaStore.Images.Media.DATE_TAKEN,MediaStore.Images.Media.SIZE },
						MediaStore.Images.Media.BUCKET_ID + " = ?",
						new String[] { String.valueOf(getArguments().getInt(
								"bucket")) },
						MediaStore.Images.Media.DATE_MODIFIED + " DESC");

		final List<GridItem> images = new ArrayList<GridItem>(cur.getCount());

		if (cur.moveToFirst()) {
			while (!cur.isAfterLast()) {
				images.add(new GridItem(cur.getString(1), cur.getString(0),cur.getString(2),cur.getLong(3)));
				cur.moveToNext();
			}
		}
		cur.close();

		GridView grid = v.findViewById(R.id.grid);
		grid.setAdapter(new GalleryAdapter(getActivity(), images));
		grid.setOnItemClickListener((parent, view, position, id)
				-> ((SelectPictureActivity) requireActivity()).imageSelected(images
				.get(position).path,images
				.get(position).imageTaken,images
				.get(position).imageSize));
		return v;
	}

}