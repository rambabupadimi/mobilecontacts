package com.mobilenumbers;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

import static com.mobilenumbers.R.drawable.default_user_men_icon;

/**
 * Created by ramu on 10/02/17.
 */

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ContactViewHolder> {

    private List list;
    private Context contextM;


    public ContactListAdapter(List list, Context context) {
        this.list = list;
        this.contextM=context;
    }

    @Override
    public int getItemCount() {
        if(list!=null)
            return list.size();//list.size();
        else
            return 0;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_record, parent, false);
        return new ContactViewHolder(itemView);
    }


    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(contextM)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }


    @Override
    public void onBindViewHolder(final ContactViewHolder holder, int position) {
        final Map m = (Map)list.get(position);
        holder.nameTextView.setText(m.get("Name").toString());
        holder.mobileTextView.setText(m.get("Phone").toString());

        if(m.get("Image")!=null) {
            Picasso.with(contextM).load(m.get("Image").toString())
                    .transform(new CircleTransform())
                    .into(holder.imageView);
        }
        else
        {
            holder.imageView.setImageDrawable(contextM.getResources().getDrawable(R.drawable.default_user_men_icon));
        }



        holder.callImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    int permissionContacts = ContextCompat.checkSelfPermission(contextM,
                            Manifest.permission.CALL_PHONE);
                    if (permissionContacts != PackageManager.PERMISSION_GRANTED) {
                        showDialogOK("Call Phone permission required for this operation. Please Go to settings and grant the permission.",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                break;
                                        }
                                    }
                                });
                    }
                    else
                    {
                        try {
                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                            callIntent.setData(Uri.parse("tel:" + m.get("Phone").toString()));
                            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            contextM.startActivity(callIntent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                else
                {

                    try {
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" +  m.get("Phone").toString()));
                        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        contextM.startActivity(callIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            }
        });

    }





    public void reload(List list){
        this.list = list;
        notifyDataSetChanged();
    }


    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView mobileTextView;
        ImageView imageView,callImage;

        public ContactViewHolder(final View itemView) {
            super(itemView);
            nameTextView        = (TextView)itemView.findViewById(R.id.contact_name);
            mobileTextView      = (TextView)itemView.findViewById(R.id.contact_number);
            imageView           =   (ImageView) itemView.findViewById(R.id.imageview);
            callImage           =   (ImageView) itemView.findViewById(R.id.call);
        }
    }


    public Bitmap getCircleBitmap(Bitmap bitmap) {

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Bitmap output = null;



        try {


            int radius = Math.min(h / 2, w / 2);
            output = Bitmap.createBitmap(w + 8, h + 8, Bitmap.Config.ARGB_8888);


            Paint p = new Paint();
            p.setAntiAlias(true);

            Canvas c = new Canvas(output);
            c.drawARGB(0, 0, 0, 0);
            p.setStyle(Paint.Style.FILL);

            c.drawCircle((w / 2) + 4, (h / 2) + 4, radius, p);

            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            c.drawBitmap(bitmap, 4, 4, p);
            p.setXfermode(null);
            p.setStyle(Paint.Style.STROKE);
            p.setColor(Color.WHITE);
            p.setStrokeWidth(3);
            c.drawCircle((w / 2) + 4, (h / 2) + 4, radius, p);
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return output;
    }


}