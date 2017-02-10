package com.mobilenumbers;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    RecyclerView          MobileContactRecyclerview;
    Toolbar toolbar, searchToolbar;
   // AllContactsAdapter      allContactsAdapter;
    private String TAG = "AllContacts.java";
    private boolean isSearch = false;
    public boolean is = false;
    List<Map> contactList;
    public boolean isMenu = false;
    ContactListAdapter contactListAdapter;

     @Override
    protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         toolbar = (Toolbar) findViewById(R.id.toolbar);
         searchToolbar = (Toolbar) findViewById(R.id.toolbar_search);
         prepareActionBar(toolbar);
         MobileContactRecyclerview = (RecyclerView) findViewById(R.id.recycler_view);
         if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
             int permissionContacts = ContextCompat.checkSelfPermission(MainActivity.this,
                     Manifest.permission.READ_CONTACTS);
             if (permissionContacts != PackageManager.PERMISSION_GRANTED) {
                 showDialogOK("Read Contacts permission required for this operation. Go to settings and enable Read Contacts permissions.",
                         new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 switch (which) {
                                     case DialogInterface.BUTTON_POSITIVE:
                                         //checkAndRequestPermissions();
                                         finish();
                                         break;
                                 }
                             }
                         });
             }
         }
         contactList    =   new ArrayList<>();
         Log.i(TAG,"final list is"+retriveAllContact());
         contactList = retriveAllContact();
         contactListAdapter = new ContactListAdapter(contactList,this);
         MobileContactRecyclerview.setAdapter(contactListAdapter);
         LinearLayoutManager layoutManager1 = new LinearLayoutManager(this);
         MobileContactRecyclerview.setLayoutManager(layoutManager1);



    }

    public List<Map> retriveAllContact() {
        List<Map> allContacts = new ArrayList<>();

        //Log.w(TAG, "QK We are in retriveAllContact method");
        ContentResolver cr = this.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        //Log.w(TAG, "QK We are in retriveAllContact No.of Contacts: "+cur.getCount());
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                long id = cur.getLong(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String photoId;
                try {
                    photoId = cur.getString(cur.getColumnIndex(ContactsContract.Data.PHOTO_URI));
                } catch (Exception e) {
                    photoId = "";
                }

                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    List<String> phones = new ArrayList<>();
                    Cursor cursor = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id + ""}, null);

                    while (cursor.moveToNext()) {
                        String p = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        p = p.replace(" ", "");
                        p = p.replace("-", "");
                        if (!phones.contains(p)) {
                            phones.add(p);
                        }
                    }

                   HashMap hashMap = new HashMap();
                   hashMap.put("id",id);
                    hashMap.put("Name",name);
                    hashMap.put("Image",photoId);
                    hashMap.put("Phone",phones);

                    //Log.w(TAG, "QK We are in retriveAllContact Name ="+c.name+" Phones ="+c.allPhoneNumber);
                    allContacts.add(hashMap);
                    cursor.close();
                }
            }
        }
        return allContacts;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(isSearch ? R.menu.menu_search_toolbar : R.menu.menu_all_contact, menu);
        if (isSearch) {

            isMenu = true;
            //Toast.makeText(getApplicationContext(), "Search " + isSearch, Toast.LENGTH_SHORT).show();
            final SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
            search.setIconified(false);

            search.setQueryHint("Search Contacts...");

            search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    try {
                        final List<Map> result_list = new ArrayList<>(contactList.size());
                        for (int i = 0; i < contactList.size(); i++) {
                            Map map = (Map) contactList.get(i);
                            String str_title = map.get("Name").toString();
                            if (str_title.toLowerCase().contains(s)) {
                                result_list.add((Map) contactList.get(i));
                            }
                        }
                        contactListAdapter = new ContactListAdapter(result_list, getApplicationContext());
                        MobileContactRecyclerview.setAdapter(contactListAdapter);
                        contactListAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return true;
                }
            });
            search.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    closeSearch();
                    return true;
                }
            });
        }
        return super.onCreateOptionsMenu(menu);


    }


    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;
        if (isMenu) {

            if (id == android.R.id.home) {
                closeSearch();
                return true;
            }


        }


        switch (id) {



            case R.id.search: {
                isSearch = true;
                searchToolbar.setVisibility(View.VISIBLE);
                prepareActionBar(searchToolbar);
                supportInvalidateOptionsMenu();
                return true;
            }


        }
        return super.onOptionsItemSelected(item);
    }

    // Detect when the back button is pressed
    private boolean _doubleBackToExitPressedOnce = false;

    // Detect when the back button is pressed
    public void onBackPressed() {

        if (_doubleBackToExitPressedOnce) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            this._doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press again to quit", Toast.LENGTH_SHORT).show();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                _doubleBackToExitPressedOnce = false;
            }
        }, 1000);
    }


    private void prepareActionBar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
           getSupportActionBar().setTitle("Contacts");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
    }


    private void closeSearch() {
        if (isSearch) {
            isSearch = false;
            isMenu = false;
            try {

            } catch (Exception e) {
                e.printStackTrace();
            }

            prepareActionBar(toolbar);
            searchToolbar.setVisibility(View.GONE);
            supportInvalidateOptionsMenu();
        }
    }




}