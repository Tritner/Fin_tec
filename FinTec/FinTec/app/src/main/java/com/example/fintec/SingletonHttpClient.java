package com.example.fintec;

import okhttp3.OkHttpClient;

public class SingletonHttpClient { // Singleton class for creating a single instance of OkHttpClient
    private static SingletonHttpClient instance = null;
    private OkHttpClient client;

    private SingletonHttpClient() {
        client = new OkHttpClient();
    }

    public static synchronized SingletonHttpClient getInstance() {
        if (instance == null) {
            instance = new SingletonHttpClient();
        }
        return instance;
    }

    public OkHttpClient getClient() {
        return client;
    }
}
