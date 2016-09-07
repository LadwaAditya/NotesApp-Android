package com.example.aditya.notesapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Retrofit;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener {

    ListView mListView;
    ArrayList<String> mList;
    ArrayAdapter<String> mArrayAdapter;
    private List<Note> mNotes;

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

                        for (int i = 0; i < notes.size(); i++) {
                            mNotes = notes;
                            mList.add(notes.get(i).getName());

                            Log.d(TAG, notes.get(i).getName() + notes.get(i).getId());
                        }

                    }
                });
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        final int item = i;
        mRetrofit.create(Endpoint.class).deleteNote(mNotes.get(i).getId())
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
        return true;
    }
}
