package com.example.aditya.notesapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.aditya.notesapp.data.Count;
import com.example.aditya.notesapp.data.Endpoint;
import com.example.aditya.notesapp.data.Note;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPush;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushException;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushNotificationListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPPushResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.push.api.MFPSimplePushNotification;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Retrofit;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener {

    private ListView mListView;
    private ArrayList<String> mList;
    private ArrayAdapter<String> mArrayAdapter;
    private List<Note> mNotes;
    private static final int PERMISSION_GET_ACCOUNT = 100;
    private MFPPush push;

    public static final String TAG = MainActivity.class.getSimpleName();


    @Inject
    Retrofit mRetrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Bind the views
        mListView = (ListView) findViewById(R.id.my_list);

        //Inject Dependency

        NoteApp.getNetComponent().inject(this);
        mList = new ArrayList<>();
        mListView.setOnItemLongClickListener(this);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddNote.class));
            }
        });

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.GET_ACCOUNTS},
                    PERMISSION_GET_ACCOUNT);

        } else {
            setUpBMSClient();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (push != null) {
            push.listen(notificationListener);
        }

        mRetrofit.create(Endpoint.class).getNotes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<List<Note>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "Completed");
                        mArrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, mList);
                        mListView.setAdapter(mArrayAdapter);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, e.toString());
                    }

                    @Override
                    public void onNext(List<Note> notes) {
                        if (mNotes != null)
                            mNotes.clear();
                        if (mList != null)
                            mList.clear();

                        for (int i = 0; i < notes.size(); i++) {
                            mNotes = notes;
                            mList.add(notes.get(i).getName());
                            Log.d(TAG, notes.get(i).getName() + notes.get(i).getId());
                        }
                    }
                });
    }

    private void setUpBMSClient() {

        try {
            BMSClient.getInstance().initialize(getApplicationContext(),
                    "http://notesapp.mybluemix.net",
                    "f54a78a7-8e23-450d-a6c6-b2553945acba",
                    BMSClient.REGION_US_SOUTH);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d(TAG, "Auth error");
        }


        push = MFPPush.getInstance();
        push.initialize(getApplicationContext(), "f54a78a7-8e23-450d-a6c6-b2553945acba");

        push.registerDevice(new MFPPushResponseListener<String>() {
            @Override
            public void onSuccess(String deviceId) {
                Log.d(TAG, "Registered for push");
            }

            @Override
            public void onFailure(MFPPushException ex) {
                Log.d(TAG, "Error registering device");
            }
        });
        Log.d(TAG, "Auth called");
    }

    MFPPushNotificationListener notificationListener = new MFPPushNotificationListener() {
        @Override
        public void onReceive(final MFPSimplePushNotification message) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this)
                    .setSmallIcon(R.drawable.common_ic_googleplayservices)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(message.getAlert());

            NotificationManagerCompat.from(MainActivity.this).notify(1,builder.build());
        }
    };


    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        final int item = i;

        new AlertDialog.Builder(this)
                .setTitle("Do you want to delete " + mNotes.get(i).getName())
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                        mRetrofit.create(Endpoint.class).deleteNote(mNotes.get(item).getId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .unsubscribeOn(Schedulers.io())
                                .subscribe(new Observer<Count>() {
                                    @Override
                                    public void onCompleted() {
                                        Log.d(TAG, "Completed");
                                        mList.remove(item);
                                        mArrayAdapter.notifyDataSetChanged();

                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.d(TAG, e.toString());
                                    }

                                    @Override
                                    public void onNext(Count count) {
                                        Log.d(TAG, String.valueOf(count));
                                    }
                                });
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create().show();

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_GET_ACCOUNT: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    setUpBMSClient();


                }

            }
        }
    }
}
