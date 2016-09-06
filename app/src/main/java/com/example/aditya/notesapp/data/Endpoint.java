package com.example.aditya.notesapp.data;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by Aditya on 06-Sep-16.
 */

public interface Endpoint {
    @GET("notes")
    Observable<List<Note>> getNotes();

    @POST("notes")
    Observable<Note> addNote(@Body Note note);
}
