package ahmedy.gar1;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.List;

public class MyActivity extends AppCompatActivity implements LocationListener {

    private RadioGroup radioAccidentGroup;
    private RadioButton radioAccidentButton;
    private Button sendButton;
    String receivedString ="";
    String name ;
    String phone ;
    String address ;
    LocationManager mLocationManager;
    Location loca = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //check if the user has already signed up
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String defaultValue = ""; //getResources().getString(R.string.my_default);
        name = sharedPref.getString(getString(R.string.full_name), defaultValue);
        phone = sharedPref.getString(getString(R.string.phone_number), defaultValue);
        address = sharedPref.getString(getString(R.string.home_address), defaultValue);
        if (name == "" || phone == "" || address == "") {
            // If user is anonymous, send the user to LoginActivity
            Intent intent = new Intent(MyActivity.this,
                    LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            setContentView(R.layout.activity_my);
            radioAccidentGroup=(RadioGroup)findViewById(R.id.radioGroup);
            sendButton=(Button)findViewById(R.id.button);
            //When the send button is pressed
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int selectedId=radioAccidentGroup.getCheckedRadioButtonId();
                    radioAccidentButton=(RadioButton)findViewById(selectedId);
                    //check at least one radio button is selected.
                    if (selectedId == -1){
                        Toast.makeText(MyActivity.this,"please select incident from the list", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //calling getLoca method to get the user's current location
                        loca = getLoca();
                        //if a valid loation been acheived.
                        if (loca != null) {
                            new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        //identify the user URL
                                        URL url = new URL("http://192.168.0.6:9999/form/report");
                                        URLConnection connection = url.openConnection();
                                        //adding the user datials to stringToSend prepare it to be sent with the report
                                        String stringToSend = name + "^" + phone + "^" + address + "^";
                                        //get the radio button selected to a string and add it to the stringToSend.
                                        stringToSend += radioAccidentButton.getText().toString() + "^";
                                        //adding the rorter current location to the stringToSend.
                                        stringToSend += String.valueOf(loca.getLatitude()) + "^" + String.valueOf(loca.getLongitude());

                                        //write user's details, report and location to the server
                                        connection.setDoOutput(true);
                                        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                                        out.write(stringToSend);
                                        out.close();

                                        //read the server's respond
                                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                        String returnString = "";
                                        while ((returnString = in.readLine()) != null) {
                                            receivedString = returnString;
                                        }
                                        in.close();
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                //viewing a new layout to show the resond to the user
                                                setContentView(R.layout.confirmation);
                                                EditText inputValue = null;
                                                inputValue = (EditText) findViewById(R.id.inputNum);
                                                //showing the received String to teh user
                                                inputValue.setText(receivedString);
                                            }
                                        });
                                    } catch (Exception e) {
                                        Log.d("Exception", e.toString());
                                    }
                                }
                            }).start();
                        }
                    }
                }
            });
        }
    }

    //this method return the device current location as Location instance
    public Location getLoca() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
        try {
            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            //if there are a recent location then return it.
            if (location != null && location.getTime() > Calendar.getInstance().getTimeInMillis() - 2 * 60 * 1000) {
                return location;
            } else { //get current location
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, MyActivity.this);
                return location;
            }
        //if permission has not been granted then ask for it and return null.
        } catch (SecurityException se) {
            ActivityCompat.requestPermissions(MyActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
        return null;
    }

    //onLocationChanged method
    public void onLocationChanged(Location location) {
        Log.d("at the .","onLocationChanged");
        int MY_PERMISSIONS_REQUEST_READ_CONTACTS=1;
        if (location != null) {
            Log.v("Location Changed", location.getLatitude() + " and " + location.getLongitude());
            try{
                mLocationManager.removeUpdates(this);
            }catch (SecurityException se){
                ActivityCompat.requestPermissions(MyActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        }
    }

    // Required by implementing interface functions
    public void onProviderDisabled(String arg0) {}
    public void onProviderEnabled(String arg0) {}
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my, menu);
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
            Log.d("the shared main ", "name");
            Intent intent = new Intent(MyActivity.this,
                    LoginActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}