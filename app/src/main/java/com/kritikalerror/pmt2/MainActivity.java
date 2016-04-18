package com.kritikalerror.pmt2;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.IntegerRes;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends ActionBarActivity {
    public final static String KEY_EXTRA_CONTACT_ID = "KEY_EXTRA_CONTACT_ID";
    private final String smsMessage = "PMT";

    private ListView listView;
    private FriendListViewAdapter mFriendAdapter;
    private ArrayList<String> dbList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.fetchContacts();
        //final ArrayAdapter listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, this.dbList);
        listView = (ListView)findViewById(R.id.listView1);
        //listView.setAdapter(listAdapter);
        mFriendAdapter = new FriendListViewAdapter(getBaseContext(), this.dbList);
        listView.setAdapter(mFriendAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> listView, View view,
                                    int position, long id) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                final int pos = position;

                alertDialogBuilder.setTitle("Are you sure? BETA, to be removed from production");
                alertDialogBuilder
                        .setMessage("Send this SMS?")
                        .setCancelable(false)
                        .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                String item = (String) MainActivity.this.listView.getItemAtPosition(pos);
                                int splitPosition = item.indexOf("\n");
                                String userNumber = item.substring(splitPosition);
                                Toast.makeText(getApplicationContext(), "Sent PMT to " + userNumber + "!", Toast.LENGTH_SHORT).show();
                                MainActivity.this.sendSMS(userNumber);
                            }
                        })
                        .setNegativeButton("No",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                // Test only, do not use in production!
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

//                String item = (String) MainActivity.this.listView.getItemAtPosition(position);
//                int splitPosition = item.indexOf("\n");
//                String userNumber = item.substring(splitPosition);
//                Toast.makeText(getApplicationContext(), "Checking in to " + userNumber + "!", Toast.LENGTH_SHORT).show();
//                MainActivity.this.sendSMS(userNumber);
            }
        });

    }

    public void sendSMS(String number){
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(number, null, this.smsMessage, null, null);
            Toast.makeText(getApplicationContext(), "Sent PMT", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "Can't send a PMT because " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public void fetchContacts() {
        String name = "";
        String number = "";
        String combine = "";

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        ContentResolver contentResolver = getContentResolver();

        Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, null);

        // Loop for every contact in the phone
        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

                if (hasPhoneNumber > 0) {
                    // Query and loop for every phone number of the contact
                    //Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);
                    Cursor phoneCursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                                    ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                            new String[]{contact_id},
                            null);
                    while (phoneCursor.moveToNext()) {
                        number = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                    }
                    phoneCursor.close();
                }

                if(!number.equals("")) {
                    combine = name + "\n" + number;
                    //Log.e("TAG", "Added to list: " + name + ", " + number);
                    this.dbList.add(combine);
                    number = "";
                    name = "";
                }
            }
        }
        // Sort the array before finishing
        Collections.sort(this.dbList, String.CASE_INSENSITIVE_ORDER);

        cursor.close();
    }

}
