package com.example.aditya.notesapp;

import android.app.Application;

import com.example.aditya.notesapp.data.component.NetComponent;
import com.example.aditya.notesapp.data.module.AppModule;
import com.example.aditya.notesapp.data.module.NetModule;

/**
 * Created by Aditya on 06-Sep-16.
 */

public class NoteApp extends Application{
    private NetComponent mNetComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        mNetComponent = DaggerNetComponent.builder()
                .appModule(new AppModule(this))
                .netModule(new NetModule("http://notesapp.mybluemix.net/"))
                .build();
    }

    public NetComponent getNetComponent() {
        return mNetComponent;
    }
}
