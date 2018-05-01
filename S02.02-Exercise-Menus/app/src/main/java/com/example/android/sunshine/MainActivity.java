/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    final  static String LOG_TAG = MainActivity.class.getSimpleName();

    private TextView mWeatherTextView;

    private ProgressBar mProgressBarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        /*
         * Using findViewById, we get a reference to our TextView from xml. This allows us to
         * do things like set the text of the TextView.
         */
        mWeatherTextView = (TextView) findViewById(R.id.tv_weather_data);
        // Get a reference to the ProgressBar using findViewById
        mProgressBarView = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        /* Once all of our views are setup, we can load the weather data. */
        loadWeatherData();
    }

    /**
     * This method will get the user's preferred location for weather, and then tell some
     * background method to get the weather data in the background.
     */
    private void loadWeatherData() {
        //String location ="Athens,gr";
        String location = SunshinePreferences.getPreferredWeatherLocation(this);
        new FetchWeatherTask().execute(location);
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBarView.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[] doInBackground(String... params) {

            /* If there's no input param, there's nothing to look up. */
            if (params.length == 0) {
                return null;
            }

            String location = params[0];
            URL searchURL = NetworkUtils.buildUrl(location);
            Log.i(LOG_TAG, "URL: " + searchURL.toString());
            String[] simpleJsonWeatherData = new String[0];
            try {
                String weatherSearchResults = NetworkUtils.getResponseFromHttpUrl(searchURL);
                Log.i(LOG_TAG, "json data: " + weatherSearchResults);
                simpleJsonWeatherData = OpenWeatherJsonUtils
                        .getSimpleWeatherStringsFromJson(MainActivity.this, weatherSearchResults);

            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException"+e);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "JSONException"+e);
            } finally {
                return simpleJsonWeatherData;
            }
        }

        @Override
        protected void onPostExecute(String[] s) {
            mProgressBarView.setVisibility(View.INVISIBLE);
            if (s != null && !(s.length == 0) ) {
                /*
                 * Iterate through the array and append the Strings to the TextView. The reason why we add
                 * the "\n\n\n" after the String is to give visual separation between each String in the
                 * TextView. Later, we'll learn about a better way to display lists of data.
                 */
                for (String weatherString : s) {
                    mWeatherTextView.append((weatherString) + "\n\n\n");
                }
            }
        }
    }

    // (2) Create a menu resource in res/menu/ called forecast.xml
    // (3) Add one item to the menu with an ID of action_refresh
    // (4) Set the title of the menu item to "Refresh" using strings.xml

    // (5) Override onCreateOptionsMenu to inflate the menu for this Activity
    // (6) Return true to display the menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.forecast, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Search" menu option
            case R.id.action_refresh:
                //clear the screen
                mWeatherTextView.setText("");
                // show a Toast message, that the refresh option was selected
                Context context = getApplicationContext();
                CharSequence text = "Refresh item selected: " + R.id.action_refresh;
                Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                toast.show();
                // re-load the weather data
                loadWeatherData();
                return true;
            default:
                /* an unknown menu item Id was passed */
                return super.onOptionsItemSelected(item);
        }

    }

    // (7) Override onOptionsItemSelected to handle clicks on the refresh button
}