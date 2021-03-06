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
package com.example.android.makemyvoiceheard;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView senator1NameTV;
    private TextView senator2NameTV;
    private TextView representativeNameTV;
    private TextView yourCongressmanLabelTV;
    private TextView yourSenatorLabelTV;
    private TextView loadingErrorTV;
    private ImageView senator1ImageIV;
    private ImageView senator2ImageIV;
    private ImageView representativeImageIV;

    public static final Integer SENATOR_ONE = 1;
    public static final Integer SENATOR_TWO = 2;
    public static final Integer REPRESENTATIVE = 3;
    public static final String IMAGE_SELECTION = "SELECTION";
    public static final String OFFICIAL_NAME = "OFFICIAL_NAME";
    public static final String OFFICIAL_PARTY = "OFFICIAL_PARTY";
    public static final String OFFICIAL_TYPE  = "OFFICIAL_TYPE";
    public static final String OFFICIAL_ADDRESS_LINE1 = "OFFICIAL_LINE1";
    public static final String OFFICIAL_ADDRESS_LINE2 = "OFFICIAL_LINE2";
    public static final String OFFICIAL_URL = "OFFICIAL_URL";
    public static final String OFFICIAL_PHOTO_URL = "OFFICIAL_PHOTO_URL";
    public static final String OFFICIAL_PHONE = "OFFICIAL_PHONE";

    GPSTracker gps;

    // data used for layout files
    private String senator1Name;
    private String senator1URL;
    private String senator1PhotoURL;
    private String senator1AddressLine1;
    private String senator1AddressLine2;
    private String senator1Party;
    private String senator1Phone;

    private String senator2Name;
    private String senator2URL;
    private String senator2PhotoURL;
    private String senator2AddressLine1;
    private String senator2AddressLine2;
    private String senator2Party;
    private String senator2Phone;

    private String representativeName;
    private String representativeURL;
    private String representativePhotoURL;
    private String representativeAddressLine1;
    private String representativeAddressLine2;
    private String representativeParty;
    private String representativePhone;

    // shared preferences member variables
    private static SharedPreferences mPreferences;
    private static String sharedPrefFile = "com.example.android.makemyvoiceheardprefs";
    private final static String DATA_STORED_KEY       = "DATA_STORED";

    private final static String SENATOR_1_NAME_KEY       = "SENATOR_1_NAME";
    private final static String SENATOR_1_LINE1_KEY      = "SENATOR_1_LINE1";
    private final static String SENATOR_1_LINE2_KEY      = "SENATOR_1_LINE2";
    private final static String SENATOR_1_PHOTO_URL_KEY  = "SENATOR_1_PHOTO_URL";
    private final static String SENATOR_1_URL_KEY        = "SENATOR_1_URL";
    private final static String SENATOR_1_PARTY_KEY      = "SENATOR_1_PARTY";
    private final static String SENATOR_1_PHONE_KEY      = "SENATOR_1_PHONE";

    private final static String SENATOR_2_NAME_KEY       = "SENATOR_2_NAME";
    private final static String SENATOR_2_LINE1_KEY      = "SENATOR_2_LINE1";
    private final static String SENATOR_2_LINE2_KEY      = "SENATOR_2_LINE2";
    private final static String SENATOR_2_PHOTO_URL_KEY  = "SENATOR_2_PHOTO_URL";
    private final static String SENATOR_2_URL_KEY        = "SENATOR_2_URL";
    private final static String SENATOR_2_PARTY_KEY      = "SENATOR_2_PARTY";
    private final static String SENATOR_2_PHONE_KEY      = "SENATOR_2_PHONE";

    private final static String REPRESENTATIVE_NAME_KEY       = "REPRESENTATIVE_NAME";
    private final static String REPRESENTATIVE_LINE1_KEY      = "REPRESENTATIVE_LINE1";
    private final static String REPRESENTATIVE_LINE2_KEY      = "REPRESENTATIVE_LINE2";
    private final static String REPRESENTATIVE_PHOTO_URL_KEY  = "REPRESENTATIVE_PHOTO_URL";
    private final static String REPRESENTATIVE_URL_KEY        = "REPRESENTATIVE_URL";
    private final static String REPRESENTATIVE_PARTY_KEY      = "REPRESENTATIVE_PARTY";
    private final static String REPRESENTATIVE_PHONE_KEY      = "REPRESENTATIVE_PHONE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        String mCallPermission = Manifest.permission.CALL_PHONE;
        final int REQUEST_CODE_PERMISSION = 2;
        String encodeAddress;
        String encodedAddress = "";
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        senator1NameTV         = (TextView) findViewById(R.id.senator_1_name);
        senator2NameTV         = (TextView) findViewById(R.id.senator_2_name);
        representativeNameTV   = (TextView) findViewById(R.id.representative_name);
        yourCongressmanLabelTV = (TextView) findViewById(R.id.congressman_label);
        yourSenatorLabelTV     = (TextView) findViewById(R.id.your_senators_label);
        loadingErrorTV         = (TextView) findViewById(R.id.loading_error_message);
        senator1ImageIV        = (ImageView) findViewById(R.id.senator_1_label);
        senator2ImageIV        = (ImageView) findViewById(R.id.senator_2_label);
        representativeImageIV  = (ImageView) findViewById(R.id.representative_image);
        showLoadingMessage();

        try {
            if ((ContextCompat.checkSelfPermission(this, mPermission)
                    != PackageManager.PERMISSION_GRANTED) ||  (ContextCompat.checkSelfPermission(this, mCallPermission)
                    != PackageManager.PERMISSION_GRANTED)) {
                Log.d("WWD", "requesting permission");
                requestPermissions(new String[]{mPermission,mCallPermission}, REQUEST_CODE_PERMISSION);

                // If any permission above not allowed by user, this condition will execute every time, else your else part will work
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        gps = new GPSTracker(MainActivity.this);

        // check if GPS enabled
        if(gps.canGetLocation()){
            Log.d("WWD", "canGetLocation returned true");
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            Log.d("WWD", " ---------- latitude is " + latitude);
            Log.d("WWD", " ---------- longitude is " + longitude);
            // \n is for new line
            /* Toast.makeText(getApplicationContext(), "Your Location is - \nLat: "
                    + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show(); */
            Geocoder geocoder= new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                List<Address> listAddress = geocoder.getFromLocation(latitude, longitude, 1);
                if (listAddress != null && listAddress.size() > 0) {
                    String address = "";
                    address += listAddress.get(0).getAddressLine(0);
                    Log.d("WWD", " -------------  address line is " + address);
                    encodeAddress = URLEncoder.encode(address, "UTF-8");
                    Log.d("WWD", "-------------- the the encoded address is " + encodeAddress);
                    encodedAddress = encodeAddress.replace("+", "%20");
                    Log.d("WWD", " -------------- the encoded address afer replacment is " + encodedAddress);

                   // URL url = new URL(address);
                    //Log.d("WWD", "-------------- first url is " + url.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
        new CivicQueryTask().execute(encodedAddress);
        /* try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } */

    }

    public void onClickSenator1(View view) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(OFFICIAL_NAME, senator1Name);
        intent.putExtra(OFFICIAL_TYPE, "Your Senator");
        intent.putExtra(OFFICIAL_ADDRESS_LINE1, senator1AddressLine1);
        intent.putExtra(OFFICIAL_ADDRESS_LINE2, senator1AddressLine2);
        intent.putExtra(OFFICIAL_URL, senator1URL);
        intent.putExtra(OFFICIAL_PHOTO_URL, senator1PhotoURL);
        intent.putExtra(OFFICIAL_PHONE, senator1Phone);
        startActivity(intent);
    }

    public void onClickSenator2(View view) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(IMAGE_SELECTION, SENATOR_TWO);
        intent.putExtra(OFFICIAL_NAME, senator2Name);
        intent.putExtra(OFFICIAL_TYPE, "Your Senator");
        intent.putExtra(OFFICIAL_ADDRESS_LINE1, senator2AddressLine1);
        intent.putExtra(OFFICIAL_ADDRESS_LINE2, senator2AddressLine2);
        intent.putExtra(OFFICIAL_URL, senator2URL);
        intent.putExtra(OFFICIAL_PHOTO_URL, senator2PhotoURL);
        intent.putExtra(OFFICIAL_PHONE, senator2Phone);
        startActivity(intent);
    }

    public void onClickRepresentative(View view) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(OFFICIAL_NAME, representativeName);
        intent.putExtra(OFFICIAL_TYPE, "Your Representative");
        intent.putExtra(OFFICIAL_ADDRESS_LINE1, representativeAddressLine1);
        intent.putExtra(OFFICIAL_ADDRESS_LINE2, representativeAddressLine2);
        intent.putExtra(OFFICIAL_URL, representativeURL);
        intent.putExtra(OFFICIAL_PHOTO_URL, representativePhotoURL);
        intent.putExtra(OFFICIAL_PHONE, representativePhone);
        startActivity(intent);
    }

    private void storePreferences() {
        //private SharedPreferences mPreferences;
        // private String sharedPrefFile = "com.example.android.hellosharedprefs";
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();

        // store senator 1 data
        preferencesEditor.putString(SENATOR_1_NAME_KEY,      JsonUtil.getSenator1Name());
        preferencesEditor.putString(SENATOR_1_LINE1_KEY,     JsonUtil.getSenator1AddressLine1());
        preferencesEditor.putString(SENATOR_1_LINE2_KEY,     JsonUtil.getSenator1AddressLine2());
        preferencesEditor.putString(SENATOR_1_URL_KEY,       JsonUtil.getSenator1URL());
        preferencesEditor.putString(SENATOR_1_PHOTO_URL_KEY, JsonUtil.getSenator1PhotoURL());
        preferencesEditor.putString(SENATOR_1_PARTY_KEY,     JsonUtil.getSenator1Party());
        preferencesEditor.putString(SENATOR_1_PHONE_KEY,      JsonUtil.getSenator1Phone());

        // store senator 2 data
        preferencesEditor.putString(SENATOR_2_NAME_KEY,      JsonUtil.getSenator2Name());
        preferencesEditor.putString(SENATOR_2_LINE1_KEY,     JsonUtil.getSenator2AddressLine1());
        preferencesEditor.putString(SENATOR_2_LINE2_KEY,     JsonUtil.getSenator2AddressLine2());
        preferencesEditor.putString(SENATOR_2_URL_KEY,       JsonUtil.getSenator2URL());
        preferencesEditor.putString(SENATOR_2_PHOTO_URL_KEY, JsonUtil.getSenator2PhotoURL());
        preferencesEditor.putString(SENATOR_2_PARTY_KEY,     JsonUtil.getSenator2Party());
        preferencesEditor.putString(SENATOR_2_PHONE_KEY,      JsonUtil.getSenator2Phone());

        // store representative data
        preferencesEditor.putString(REPRESENTATIVE_NAME_KEY,      JsonUtil.getRepresentativeName());
        preferencesEditor.putString(REPRESENTATIVE_LINE1_KEY,     JsonUtil.getRepresentativeAddressLine1());
        preferencesEditor.putString(REPRESENTATIVE_LINE2_KEY,     JsonUtil.getRepresentativeAddressLine2());
        preferencesEditor.putString(REPRESENTATIVE_URL_KEY,       JsonUtil.getRepresentativeURL());
        preferencesEditor.putString(REPRESENTATIVE_PHOTO_URL_KEY, JsonUtil.getRepresentativePhotoURL());
        preferencesEditor.putString(REPRESENTATIVE_PARTY_KEY,     JsonUtil.getRepresentativeParty());
        preferencesEditor.putString(REPRESENTATIVE_PHONE_KEY,      JsonUtil.getRepresentativePhone());
        preferencesEditor.putBoolean(DATA_STORED_KEY, true);
    }

    private  Boolean isStoredPreferencesAvailable() {
        Boolean dataAvailable  = mPreferences.getBoolean(DATA_STORED_KEY, false);
        Log.d("WWD", "in isStoredPreferencesAvailable dataAvailable is " + dataAvailable);
        return dataAvailable;
    }

    public void readPreferences() {
        senator1Name         = mPreferences.getString(SENATOR_1_NAME_KEY, "");
        senator1URL          = mPreferences.getString(SENATOR_1_URL_KEY, "");
        senator1PhotoURL     = mPreferences.getString(SENATOR_1_PHOTO_URL_KEY, "");
        senator1AddressLine1 = mPreferences.getString(SENATOR_1_LINE1_KEY, "");
        senator1AddressLine2 = mPreferences.getString(SENATOR_1_LINE2_KEY, "");
        senator1Party        = mPreferences.getString(SENATOR_1_PARTY_KEY, "");
        senator1Phone        = mPreferences.getString(SENATOR_1_PHONE_KEY, "");

        senator2Name         = mPreferences.getString(SENATOR_2_NAME_KEY, "");
        senator2URL          = mPreferences.getString(SENATOR_2_URL_KEY, "");
        senator2PhotoURL     = mPreferences.getString(SENATOR_2_PHOTO_URL_KEY, "");
        senator2AddressLine1 = mPreferences.getString(SENATOR_2_LINE1_KEY, "");
        senator2AddressLine2 = mPreferences.getString(SENATOR_2_LINE2_KEY, "");
        senator2Party        = mPreferences.getString(SENATOR_2_PARTY_KEY, "");
        senator2Phone        = mPreferences.getString(SENATOR_2_PHONE_KEY, "");

        representativeName          = mPreferences.getString(REPRESENTATIVE_NAME_KEY, "");
        representativeURL           = mPreferences.getString(REPRESENTATIVE_URL_KEY, "");
        representativePhotoURL      = mPreferences.getString(REPRESENTATIVE_PHOTO_URL_KEY, "");
        representativeAddressLine1  = mPreferences.getString(REPRESENTATIVE_LINE1_KEY, "");
        representativeAddressLine2  = mPreferences.getString(REPRESENTATIVE_LINE2_KEY, "");
        representativeParty         = mPreferences.getString(REPRESENTATIVE_PARTY_KEY, "");
        representativePhone         = mPreferences.getString(REPRESENTATIVE_PHONE_KEY, "");
    }

        /* senator1NameTV       = (TextView) findViewById(R.id.senator_1_name);
        senator2NameTV       = (TextView) findViewById(R.id.senator_2_name);
        representativeNameTV = (TextView) findViewById(R.id.representative_name);
         yourCongressmanLabelTV = (TextView) findViewById(R.id.congressman_label);
        yourSenatorLabelTV     = (TextView) findViewById(R.id.your_senators_label);
        senator1Image        = (ImageView) findViewById(R.id.senator_1_label);
        senator2Image        = (ImageView) findViewById(R.id.senator_2_label);
        representativeImage  = (ImageView) findViewById(R.id.representative_image); */

    private void turnOffMainViews() {
        senator1NameTV.setVisibility(View.INVISIBLE);
        senator2NameTV.setVisibility(View.VISIBLE);
        representativeNameTV.setVisibility(View.INVISIBLE);
        senator1ImageIV.setVisibility(View.INVISIBLE);
        senator2ImageIV.setVisibility(View.INVISIBLE);
        representativeImageIV.setVisibility(View.INVISIBLE);
        yourCongressmanLabelTV.setVisibility(View.INVISIBLE);
        yourSenatorLabelTV.setVisibility(View.INVISIBLE);
    }

    private void turnOnMainViews() {
        senator1NameTV.setVisibility(View.VISIBLE);
        senator2NameTV.setVisibility(View.VISIBLE);
        representativeNameTV.setVisibility(View.VISIBLE);
        senator1ImageIV.setVisibility(View.VISIBLE);
        senator2ImageIV.setVisibility(View.VISIBLE);
        representativeImageIV.setVisibility(View.VISIBLE);
        yourCongressmanLabelTV.setVisibility(View.VISIBLE);
        yourSenatorLabelTV.setVisibility(View.VISIBLE);
    }

    private void showLoadingMessage() {
        turnOffMainViews();
        loadingErrorTV.setText("LOADING PLEASE WAIT");
        loadingErrorTV.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        turnOffMainViews();
        loadingErrorTV.setText("NETWORK ERROR CHECK WIFI");
        loadingErrorTV.setVisibility(View.VISIBLE);
    }

    private void turnOffLoadingErrorMessage() {
        loadingErrorTV.setVisibility(View.GONE);
        turnOnMainViews();
    }


    public class CivicQueryTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String address = params[0];
            String civicResults = null;
          //  Log.d("WWD", "in doInBackground");
            try {
            //    Log.d("WWD", "call network utils");
                civicResults = NetworkUtils.getResponseFromHttpUrl(address);
                //Log.d("WWD", "civicResults):" + civicResults);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return civicResults;
        }

        @Override
        protected void onPostExecute(String civicSearchResults) {
            if (NetworkUtils.getNetworkConnected() && (civicSearchResults != null && !civicSearchResults.equals(""))) {
                // ---------------------------------------------------------------------
                // first set the names to the text views
                // ---------------------------------------------------------------------
                JsonUtil.parseCivicsJson(civicSearchResults);

                senator1Name         = JsonUtil.getSenator1Name();
                senator1URL          = JsonUtil.getSenator1URL();
                senator1PhotoURL     = JsonUtil.getSenator1PhotoURL();
                senator1AddressLine1 = JsonUtil.getSenator1AddressLine1();
                senator1AddressLine2 = JsonUtil.getSenator1AddressLine2();
                senator1Party        = JsonUtil.getSenator1Party();
                senator1Phone        = JsonUtil.getSenator1Phone();

                senator2Name         = JsonUtil.getSenator2Name();
                senator2URL          = JsonUtil.getSenator1URL();
                senator2PhotoURL     = JsonUtil.getSenator2PhotoURL();
                senator2AddressLine1 = JsonUtil.getSenator2AddressLine1();
                senator2AddressLine2 = JsonUtil.getSenator2AddressLine2();
                senator2Party        = JsonUtil.getSenator2Party();
                senator2Phone        = JsonUtil.getSenator2Phone();

                representativeName          = JsonUtil.getRepresentativeName();
                representativeURL           = JsonUtil.getRepresentativeURL();
                representativePhotoURL      = JsonUtil.getRepresentativePhotoURL();
                representativeAddressLine1  = JsonUtil.getRepresentativeAddressLine1();
                representativeAddressLine2  = JsonUtil.getRepresentativeAddressLine2();
                representativeParty         = JsonUtil.getRepresentativeParty();
                representativePhone         = JsonUtil.getRepresentativePhone();

                senator1NameTV.setText(senator1Name);
                senator2NameTV.setText(senator2Name);
                representativeNameTV.setText(representativeName);

                // ---------------------------------------------------------------------
                // next set the images using picasso
                // ---------------------------------------------------------------------

                if (senator1PhotoURL.length() == 0) {
                    senator1ImageIV.setImageResource(R.drawable.nophoto);
                } else {
                    Picasso.get().load(senator1PhotoURL).into(senator1ImageIV);
                }

                if (senator2PhotoURL.length() == 0) {
                    senator2ImageIV.setImageResource(R.drawable.nophoto);
                } else {
                    Picasso.get().load(senator2PhotoURL).into(senator2ImageIV);
                }

                if (representativePhotoURL.length() == 0) {
                    representativeImageIV.setImageResource(R.drawable.nophoto);
                } else {
                    Picasso.get().load(representativePhotoURL).into(representativeImageIV);
                }
                turnOffLoadingErrorMessage();
                // store data for future use in case of network failure
                storePreferences();
            } else if (isStoredPreferencesAvailable()){
                readPreferences();
                Log.d("WWD", "read data from preferences");
                //showErrorMessage();
            } else {
                showErrorMessage();
            }
        }
    }
}