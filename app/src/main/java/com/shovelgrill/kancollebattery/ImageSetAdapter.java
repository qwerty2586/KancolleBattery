package com.shovelgrill.kancollebattery;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shovelgrill.kancollebattery.db.ImageSet;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class ImageSetAdapter extends ArrayAdapter<ImageSet> {
    Context context;
    int resource;
    List<ImageSet> imageSets;
    public int[] progresses;
    public static final int IGNORE_PROGRESS = -1;

    public ImageSetAdapter(Context context, int resource, List<ImageSet> imageSets) {
        super(context, resource, imageSets);
        this.context = context;
        this.resource = resource;
        this.imageSets = imageSets;
        progresses = new int[imageSets.size()];
        for (int i = 0; i < progresses.length; i++) progresses[i] = IGNORE_PROGRESS;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(resource, parent, false);
        }
        TextView skin_name = (TextView) convertView.findViewById(R.id.image_set_skin_name);
        ImageView img_normal = (ImageView) convertView.findViewById(R.id.image_set_img_normal);
        ImageView img_dmg = (ImageView) convertView.findViewById(R.id.image_set_img_dmg);
        ImageSet imageSet = imageSets.get(position);
        View l_view = convertView.findViewById(R.id.image_set_progress_bar_l);
        View r_view = convertView.findViewById(R.id.image_set_progress_bar_r);

        skin_name.setText(imageSet.name);
        int progress = 0;
        if (!imageSet.downloaded) {
            skin_name.setTextColor(Color.GRAY);
            Picasso.with(context).cancelRequest(img_normal);
            img_normal.setImageDrawable(null);
            Picasso.with(context).cancelRequest(img_dmg);
            img_dmg.setImageDrawable(null);
            progress = 0;
        } else {
            skin_name.setTextColor(Color.BLACK);
            File SDDir = context.getExternalFilesDir(ImageSet.WAIFU_FOLDER);
            Picasso.with(context).load(new File(SDDir, ImageSet.getFileName(imageSet.wiki_id)[0])).into(img_normal);
            Picasso.with(context).load(new File(SDDir, ImageSet.getFileName(imageSet.wiki_id)[1])).into(img_dmg);
            progress = 100;
        }
        if (progresses[position] != IGNORE_PROGRESS) {
            progress = progresses[position];
        }
        setProgress(l_view, r_view, progress);

        return convertView;

    }

    public static void setProgress(View left_view, View right_view, int percent) {
        float part = (float) percent / 100.0f;
        LinearLayout.LayoutParams l_par = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, (1 - part));
        LinearLayout.LayoutParams r_par = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, part);
        left_view.setLayoutParams(l_par);
        right_view.setLayoutParams(r_par);
    }

}
