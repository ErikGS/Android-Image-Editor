package me.mixal.edits.chooser;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import me.mixal.edits.R;

class GalleryAdapter extends BaseAdapter {

    private final Context context;
    private final List<GridItem> items;
    private final LayoutInflater mInflater;

    GalleryAdapter(final Context context, final List<GridItem> buckets) {
        this.items = buckets;
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        if (items.get(0) instanceof BucketItem) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.bucketitem, null);
                holder = new ViewHolder();
                holder.icon = convertView.findViewById(R.id.icon);
                holder.text = convertView.findViewById(R.id.text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            BucketItem bi = (BucketItem) items.get(position);
            holder.text.setText(bi.images > 1 ?
                    bi.name + " - " + context.getString(R.string.placeholder_amount_images, bi.images) :
                    bi.name);

            holder.icon.setImageURI(Uri.parse("file://" + bi.path));

            return convertView;
        } else {
            ImageView imageView;
            if (convertView == null) {
                imageView = (ImageView) mInflater.inflate(R.layout.image_item, null);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageURI(Uri.parse(items.get(position).path));

            return imageView;
        }
    }

    private static class ViewHolder {
        private ImageView icon;
        private TextView text;
    }

}
