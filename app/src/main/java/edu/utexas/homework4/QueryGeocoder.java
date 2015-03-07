package edu.utexas.homework4;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class QueryGeocoder extends ActionBarActivity {
    TextView textView;
    SupportMapFragment mapFragment;
    double lat = 0;
    double lng = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textView = new TextView(this);
        Intent intent = getIntent();
        String input = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        input = input.replace(' ', '+');
        String message;
        if (isConnected()) {
            message = "connected";
        }
        else {
            message = "not connected";
        }
        String url =
                "https://maps.googleapis.com/maps/api/geocode/json?address="+
                        input + "&key=AIzaSyCBsAVG9IuGrnOamjqW3y84eN6-iex7yj8";

        textView.setTextSize(40);

        // setContentView(textView);
        new HttpAsyncTask().execute(url);
    }

    public static String GET(String url) {
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if (inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public boolean isConnected() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            // textView.setText(result);
            try {
                JSONObject data = new JSONObject(result);
                JSONArray res = data.getJSONArray("results");
                if (res.length() == 0) {
                    textView.setText("no results");
                    setContentView(textView);
                }
                else {
                    JSONObject loc =
                            res.getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
                    lat = loc.getDouble("lat");
                    lng = loc.getDouble("lng");
                    mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.themap);
                    if (mapFragment == null) {
                        mapFragment = SupportMapFragment.newInstance();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.themap, mapFragment).commit();
                    }
                    mapFragment.getMapAsync(new MapCallbackTask());
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class MapCallbackTask implements OnMapReadyCallback {
        @Override
        public void onMapReady(GoogleMap map) {
            map.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title("Marker"));
            setContentView(R.layout.activity_query_geocoder);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_query_geocoder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
