package com.example.aditya.notesapp.data.component;

import com.example.aditya.notesapp.data.module.AppModule;
import com.example.aditya.notesapp.data.module.NetModule;

import javax.inject.Singleton;

import dagger.Component;
import retrofit2.Retrofit;

/**
 * Created by Aditya on 06-Sep-16.
 */
@Singleton
@Component(modules = {AppModule.class, NetModule.class})
public interface NetComponent {
    // downstream components need these exposed with the return type
    // method name does not really matter
    Retrofit retrofit();
}
