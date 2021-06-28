package com.pedro.ceglia.curso.uber.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConfiguracoesFirebase {

    private static FirebaseAuth auth;
    private static DatabaseReference database;

    public static DatabaseReference getDatabaseReference(){

        if (database== null){
          database = FirebaseDatabase.getInstance().getReference();
        }
        return database;
    }

    public static FirebaseAuth getAuth(){
        if (auth == null){
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

}
