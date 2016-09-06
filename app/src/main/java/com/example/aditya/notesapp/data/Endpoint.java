package com.example.aditya.notesapp.data;

import java.util.List;

import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by Aditya on 06-Sep-16.
 */

public interface Endpoint {
    @GET("/posts")
    Observable<List<Note>> getNotes();
}
