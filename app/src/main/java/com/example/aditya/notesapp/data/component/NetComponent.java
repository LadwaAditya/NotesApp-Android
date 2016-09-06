package com.example.aditya.notesapp.data.component;

import com.example.aditya.notesapp.AddNote;
import com.example.aditya.notesapp.MainActivity;
import com.example.aditya.notesapp.data.module.AppModule;
import com.example.aditya.notesapp.data.module.NetModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Aditya on 06-Sep-16.
 */
@Singleton
@Component(modules = {AppModule.class, NetModule.class})
public interface NetComponent {

    void inject(MainActivity activity);

    void inject(AddNote addNote);
}
