package com.example.aditya.notesapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.aditya.notesapp.data.Endpoint;
import com.example.aditya.notesapp.data.Note;

import javax.inject.Inject;

import retrofit2.Retrofit;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AddNote extends AppCompatActivity implements View.OnClickListener {


    private EditText mEditText;
    private Button mButton;
    private final static String TAG = AddNote.class.getSimpleName();
    @Inject
    Retrofit mRetrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NoteApp.getNetComponent().inject(this);

        mEditText = (EditText) findViewById(R.id.edittext_note);
        mButton = (Button) findViewById(R.id.button_submit);

        mButton.setOnClickListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_submit) {
            Log.d(TAG, "Clicked");

            Note note = new Note();
            note.setName(mEditText.getText().toString());
            mRetrofit.create(Endpoint.class).addNote(note)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribe(new Observer<Note>() {
                        @Override
                        public void onCompleted() {
                            Log.d(TAG, "Complete");
                            finish();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, e.toString());
                        }

                        @Override
                        public void onNext(Note note) {
                            Log.d(TAG, note.getName());
                        }
                    });
        }
    }
}
