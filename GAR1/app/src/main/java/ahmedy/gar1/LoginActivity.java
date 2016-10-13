package ahmedy.gar1;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LoginActivity extends Activity {

    private Context mContext;
    //declare variable to store the user details
    private EditText name_text;
    private EditText phone_number_text;
    private EditText home_address_text;
    private Button save_button;
    //declare variables to get the EditeText to String.
    String name_str;
    String phone_number_str;
    String home_address_str;
    //when unvalid = 1 there is something wrong the users details.
    int unvalid = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.mContext = this;

        //getting the xml tags into variables.
        name_text = (EditText) findViewById(R.id.name);
        phone_number_text = (EditText) findViewById(R.id.phone_number);
        home_address_text = (EditText) findViewById(R.id.home_address);
        save_button = (Button) findViewById(R.id.save);

        //hide the keyborad at the laucnh of this activity
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //asking permission for accessing the device location. if the permission has not been granted, the user will be asked
        //for it again when sending report to the Garda
        LocationManager mLocationManager;
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
        try {
            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        } catch (SecurityException se) {
            Log.d("no permission ", "ask for one!");
            ActivityCompat.requestPermissions(LoginActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            Log.d("got permision","yeah");
        }

        //in case of this activity been launched through the Edit information option, fill all the fields with the existing information.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String defaultValue = ""; //getResources().getString(R.string.my_default);
        String name = sharedPref.getString(getString(R.string.full_name), defaultValue);
        String phone = sharedPref.getString(getString(R.string.phone_number), defaultValue);
        String address = sharedPref.getString(getString(R.string.home_address), defaultValue);
        //checking the the name only because if the name is not empty that's means all the other fields has value as well
        if (name != ""){
            name_text.setText(name);
            phone_number_text.setText(phone);
            home_address_text.setText(address);
        }

        //when the save button pressed
        save_button.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                // Retrieve the text entered from the EditText
                name_str = name_text.getText().toString();
                phone_number_str = phone_number_text.getText().toString();
                home_address_str = home_address_text.getText().toString();

                //set unvalid to 0, where it will be changed to (1) if there are any thing wronge with the entered information.
                unvalid = 0;
                //If the name is empty or if it has (^) in it change the filed background color to red and set unvalid to 1.
                if (name_str.matches("") || name_str.contains("^")) {
                    name_text.getBackground().setColorFilter(Color.parseColor("#ff6666"), PorterDuff.Mode.SRC_ATOP);
                    unvalid = 1;
                }
                else {
                    name_text.getBackground().setColorFilter(Color.parseColor("#b3e0ff"), PorterDuff.Mode.SRC_ATOP);
                }
                //If the phone number is empty or has any char (not number) change the filed background color to red and set unvalid to 1.
                if (!phone_number_str.matches("-?\\d+(\\.\\d+)?") || phone_number_str.length()<10 ) {
                    phone_number_text.getBackground().setColorFilter(Color.parseColor("#ff6666"), PorterDuff.Mode.SRC_ATOP);
                    unvalid = 1;
                } else {
                    phone_number_text.getBackground().setColorFilter(Color.parseColor("#b3e0ff"), PorterDuff.Mode.SRC_ATOP);
                }
                //If the address is empty or if it has (^) in it change the filed background color to red and set unvalid to 1.
                if (home_address_str.matches("") || name_str.contains("^")) {
                    home_address_text.getBackground().setColorFilter(Color.parseColor("#ff6666"), PorterDuff.Mode.SRC_ATOP);
                    unvalid = 1;
                }
                else {
                    home_address_text.getBackground().setColorFilter(Color.parseColor("#b3e0ff"), PorterDuff.Mode.SRC_ATOP);
                }
                //if unvalid is 1 show message to teh user asking to correct the red fileds.
                if (unvalid == 1){
                    Toast.makeText(mContext, "please enter valid info in the red fields", Toast.LENGTH_LONG).show();
                }
                else {
                    //storing the user details to shared preference.
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getString(R.string.full_name), name_str);
                    editor.putString(getString(R.string.phone_number), phone_number_str);
                    editor.putString(getString(R.string.home_address), home_address_str);
                    editor.commit();
                    //after saving all the user's details launch the incident activity.
                    Intent intent = new Intent(LoginActivity.this, MyActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}