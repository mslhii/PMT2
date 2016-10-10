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

        this.fetchContactsWrapper();
        this.fetchSMSWrapper();
    }

    /**
     * fetchContactsWrapper ensures Marshmallow support
     * This goes in version 1.0.2
     * @return
     */
    //TODO: please fix! split methods?
    private void fetchContactsWrapper() {
        Log.i("TAG", "Show camera button pressed. Checking permission.");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Camera permission has not been granted.
            this.requestContactsPermissions();
        } else {

            // Camera permissions is already available, show the camera preview.
            Log.i("TAG", "Fetching contacts.");
            this.fetchContacts();
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

                    Log.v("TAG", "Position clicked: " + String.valueOf(pos));

                    // Need to prevent NullPointerException for ads here
                    if (pos > 0) {
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

    private void requestContactsPermissions() {
        // BEGIN_INCLUDE(contacts_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS)) {

            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
            Log.i("TAG",
                    "Displaying contacts permission rationale to provide additional context.");

            // Display a SnackBar with an explanation and a button to trigger the request.
            showOKAlertMessage("You need to allow app to show contacts",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_ASK_PERMISSIONS);
                        }
                    });
        } else {
            // Contact permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    REQUEST_CODE_ASK_PERMISSIONS);
        }
        // END_INCLUDE(contacts_permission_request)
    }

    private void fetchSMSWrapper() {
        Log.i("TAG", "Show camera button pressed. Checking permission.");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // SMS permission has not been granted.
            //this.requestSMSPermissions();
        } else {

            // Camera permissions is already available, show the camera preview.
            Log.i("TAG", "Fetching contacts.");

        }
    }

//    private void requestSMSPermissions() {
//        // BEGIN_INCLUDE(contacts_permission_request)
//        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                Manifest.permission.SEND_SMS)) {
//
//            // Provide an additional rationale to the user if the permission was not granted
//            // and the user would benefit from additional context for the use of the permission.
//            // For example, if the request has been denied previously.
//            Log.i("TAG",
//                    "Displaying contacts permission rationale to provide additional context.");
//
//            // Display a SnackBar with an explanation and a button to trigger the request.
//            showOKAlertMessage("You need to allow app to send SMS",
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_CODE_ASK_PERMISSIONS);
//                        }
//                    });
//        } else {
//            // Contact permissions have not been granted yet. Request them directly.
//            ActivityCompat.requestPermissions(MainActivity.this,
//                    new String[]{Manifest.permission.SEND_SMS},
//                    REQUEST_CODE_ASK_PERMISSIONS);
//        }
//        // END_INCLUDE(contacts_permission_request)
//    }

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
            //Toast.makeText(getApplicationContext(), "Sent PMT", Toast.LENGTH_SHORT).show();
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
