package com.kritikalerror.pmt2;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends ActionBarActivity {
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private final String smsMessage = "PMT";

    private ListView listView;
    private FriendListViewAdapter mFriendAdapter;
    private ArrayList<String> dbList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        // User warning
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle("WARNING!");
        alertDialogBuilder
                .setMessage("This app sends SMS which costs money. If you do not want to use this app, please click \"Get me outta here!\"")
                .setCancelable(false)
                .setPositiveButton("Proceed!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Get me outta here!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        boolean hasContacts = this.fetchContactsWrapper();
        if(hasContacts) {
            listView = (ListView) findViewById(R.id.listView1);
            mFriendAdapter = new FriendListViewAdapter(getBaseContext(), this.dbList);
            listView.setAdapter(mFriendAdapter);
            listView.setFastScrollEnabled(true);
            listView.setFastScrollAlwaysVisible(true);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> listView, View view,
                                        int position, long id) {
                    final int pos = position;

                    // Need to prevent NullPointerException for ads here
                    if(pos > 0) {
                        String item = (String) MainActivity.this.listView.getItemAtPosition(pos);
                        int splitPosition = item.indexOf("\n");
                        String userNumber = item.substring(splitPosition);
                        String userName = item.substring(0, (splitPosition - 1));
                        Toast.makeText(getApplicationContext(), "Sent PMT to " + userName + ": " + userNumber + "!", Toast.LENGTH_SHORT).show();
                        MainActivity.this.sendSMS(userNumber);
                    }
                }
            });
        }
    }

    /**
     * fetchContactsWrapper ensures Marshmallow support
     * @return
     */
    private boolean fetchContactsWrapper() {
        int hasSMSPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.SEND_SMS);
        if (hasSMSPermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.SEND_SMS)) {
                showOKAlertMessage("You need to allow app to send SMS",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.SEND_SMS},
                                        REQUEST_CODE_ASK_PERMISSIONS);
                            }
                        });
            }
            else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.SEND_SMS},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }

            // Do another check here
            int hasSMSPermissionAgain = ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.SEND_SMS);
            if (hasSMSPermissionAgain != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        int hasReadContactsPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_CONTACTS);
        if (hasReadContactsPermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_CONTACTS)) {
                showOKAlertMessage("You need to allow access to Contacts",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.READ_CONTACTS},
                                        REQUEST_CODE_ASK_PERMISSIONS);
                            }
                        });
            }
            else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
            // Do another check here
            int hasReadContactsPermissionAgain = ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.READ_CONTACTS);
            if (hasReadContactsPermissionAgain != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        this.fetchContacts();
        return true;
    }

    private void showOKAlertMessage(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
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
                    Cursor phoneCursor = getContentResolver().query(PhoneCONTENT_URI,
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
                    Log.v("TAG", "Added to list: " + name + ", " + number);
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
