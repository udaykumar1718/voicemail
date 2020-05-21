package com.example.abanoub.voicebasedemailsystem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by Ahmed Hassan on 12/4/2017.
 */

public class EmailsAdapter extends BaseAdapter {
    Context context;
    ArrayList<NewEmail> emails_list;
    ArrayList<UserEmail> userEmail_list;
    String child;

    public EmailsAdapter(Context context, ArrayList<NewEmail> emails_list, ArrayList<UserEmail> userEmail_list, String child) {
        this.context = context;
        this.emails_list = emails_list;
        this.userEmail_list = userEmail_list;
        this.child = child;
    }

    @Override
    public int getCount() {
        return emails_list.size();
    }

    @Override
    public Object getItem(int position) {
        return emails_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, null);
        }

        NewEmail email = emails_list.get(position);

        if (child.equals("Sent") || email.sender.equals(Utilities.getCurrentEmail()))
            ((TextView) convertView.findViewById(R.id.sender)).setText("To: " + email.receiver);
        else
            ((TextView) convertView.findViewById(R.id.sender)).setText(email.sender);

        ((TextView) convertView.findViewById(R.id.date)).setText(email.date);
        ((TextView) convertView.findViewById(R.id.title)).setText(email.title);
        ((TextView) convertView.findViewById(R.id.body)).setText(email.body);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.image);

        if (child.equals("Inbox") || child.equals("Sent") || child.equals("Favorites")) {
            if (email.isFavorite.equals("yes"))
                ((ImageView) convertView.findViewById(R.id.star)).setImageResource(R.drawable.ic_star_24dp);
            else
                ((ImageView) convertView.findViewById(R.id.star)).setImageResource(R.drawable.ic_star_border_24dp);

        } else if (child.equals("Trash"))
            ((ImageView) convertView.findViewById(R.id.star)).setVisibility(View.GONE);

        int index = -1;
        if (userEmail_list != null) {

            for (int i = 0; i < userEmail_list.size(); i++) {
                if (child.equals("Inbox")) {
                    if (userEmail_list.get(i).profilePicture != null && userEmail_list.get(i).email.equals(email.sender))
                        index = i;

                } else if (child.equals("Sent")) {
                    if (userEmail_list.get(i).profilePicture != null && userEmail_list.get(i).email.equals(email.receiver))
                        index = i;

                } else if (child.equals("Favorites") || child.equals("Trash")) {
                    if (email.sender.equals(Utilities.getCurrentEmail())) {
                        if (userEmail_list.get(i).profilePicture != null && userEmail_list.get(i).email.equals(email.receiver))
                            index = i;

                    } else {
                        if (userEmail_list.get(i).profilePicture != null && userEmail_list.get(i).email.equals(email.sender))
                            index = i;
                    }
                }
            }
        }

        if (index != -1)
            Glide.with(context)
                    .load(userEmail_list.get(index).profilePicture)
                    .into(imageView);

        return convertView;
    }
}
