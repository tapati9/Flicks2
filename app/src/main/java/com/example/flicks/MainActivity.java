package com.example.flicks;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.example.flicks.Model.Config;
import com.example.flicks.Model.Movie;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {

    //Constants
    //The base URL for the API
    public final static String API_BASE_URL = "https://api.themoviedb.org/3";
    //The parameter name for the API key
    public final static String API_KEY_PARAM = "api_key";
    //The API key --TODO move to a secure location
    public final static String API_KEY = "api_key";
    //tag for logging from this activity
    public final static String Tag = "MainActivity";

    // instance fields
    AsyncHttpClient client;
    // the list of currently playing movie
    ArrayList<Movie> movies;
    // the recycle view
    RecyclerView rvMovies;
    // the adapter wired to the recycler view
    MovieAdapter adapter;
    // image config
    Config config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // initialize the client
        client = new AsyncHttpClient();
        // initialize the list of movies
        movies = new ArrayList<com.example.flicks.Model.Movie>();
        // initialize the adapter -- movies array cannot be reinitialized after this point
        adapter = new MovieAdapter(movies);

        // resolve the recycle view and connect a layout manager and the adapter
        rvMovies = (RecyclerView) findViewById(R.id.rvMovies);
        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        rvMovies.setAdapter(adapter);

        // get the configuration on app creation
        getConfiguration();
        // get the now playing movie list
        // getNowPlaying();
    }
    // get the list of currently playing movies from the API
    private void getNowPlaying(){
        // create the URL
        String url = API_BASE_URL + "/movie/now_playing";
        // set the request parameters
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key)); // API KEY always required
                // getString(R.string.api_key)); // API KEY always required
        // executive a Get request expecting a JSON object response
        client.get(url, params, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // super.onSuccess(statusCode, headers, response);
                //load the results into movies list
                try {
                    JSONArray results = response.getJSONArray("results");
                    // iterate thought result set and create Movie object
                    for (int i = 0; i < results.length(); i++){
                        com.example.flicks.Model.Movie movie = new com.example.flicks.Model.Movie(results.getJSONObject(i));
                      //  Movie movie = new Movie(results.getJSONObject(i));
                        movies.add(movie);
                        // notify adapter that a row was added
                        adapter.notifyItemInserted(movies.size() - 1);

                    }

                    Log.i(Tag, String.format("loaded %s movies", results.length()));

                } catch (JSONException e) {
                    // e.printStackTrace();
                    logError("Failed to parse now playing movies", e,true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                // super.onFailure(statusCode, headers, responseString, throwable);
                logError("Failed to get data from now_playing endpoint", throwable, true);

            }
        });
    }

    // get the configuration from the API
    private void getConfiguration(){
        // create the URL
        String url = API_BASE_URL + "/configuration";
        // set the request parameters
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key)); // API KEY always required
        // executive a Get request expecting a JSON object response
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    config = new Config(response);
                    Log.i(Tag, String.format("Loaded configuration with imageBaseUrl %s and posterSize %s",
                            config.getImageBaseUrl(),
                            config.getPosterSize()));
                    // pass config to adapter
                    adapter.setConfig(config);
                    // get the now playing movie list
                    getNowPlaying();
                } catch (JSONException e) {
                    //e.printStackTrace();
                    logError("Failed parsing configuration", e,true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                //super.onFailure(statusCode, headers, responseString, throwable);
                logError("Failed getting configuration", throwable, true);
            }
        });
    }

    // handle errors, log and alert user
    private void logError(String message, Throwable error, boolean alertUser) {
        // always log the error
        Log.e(Tag, message, error);
        // alert the user to void silent errors
        if (alertUser){
            // show a long toast whith the error message
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

        }
    }

}
