package com.iodroid.beamit;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.iodroid.beamit.R;

public class MainActivity extends Activity implements
        CreateNdefMessageCallback, OnNdefPushCompleteCallback{

    TextView longi,latt,lattout,longout;
    String cord;
    double latitude,longitude,lat,lng;
    NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getLocationPermission();
        super.onCreate(savedInstanceState);
      //  setContentView(R.layout.activity_main);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(nfcAdapter==null){
//            Toast.makeText(MainActivity.this,
//                    "Error: No NFC adapter",
//                    Toast.LENGTH_LONG).show();
        }else{
            nfcAdapter.setNdefPushMessageCallback(this, this);
            nfcAdapter.setOnNdefPushCompleteCallback(this, this);
        }
    }
    private void getLocationPermission() {
    /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        //PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION
                , Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NFC, Manifest.permission.READ_EXTERNAL_STORAGE};

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        latt = (TextView) findViewById(R.id.lattin);
        longi = (TextView) findViewById(R.id.longin);
        longout = (TextView) findViewById(R.id.longout);
        lattout = (TextView) findViewById(R.id.lattout);
        try {

            com.iodroid.beamit.GPSTracker gpsTracker = new com.iodroid.beamit.GPSTracker(getApplicationContext());
          /*  mLocation = gpsTracker.getLocation();

            latitude = mLocation.getLatitude();
            longitude = mLocation.getLongitude();*/
            lat = gpsTracker.getLocation().getLatitude();
            lng = gpsTracker.getLocation().getLongitude();

            cord = String.valueOf(lat) + "#" + String.valueOf(lng);
            latt.setText("Lattitude:" + String.valueOf(lat));
            longi.setText("Longitude:" + String.valueOf(lng));

        } catch (Exception e) {
        }
        Intent intent = getIntent();
        String action = intent.getAction();
        if (action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            Parcelable[] parcelables =
                    intent.getParcelableArrayExtra(
                            NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage inNdefMessage = (NdefMessage) parcelables[0];
            NdefRecord[] inNdefRecords = inNdefMessage.getRecords();
            NdefRecord NdefRecord_0 = inNdefRecords[0];
            String inMsg = new String(NdefRecord_0.getPayload());
            String[] parts = inMsg.split("#");
            lattout.setText("Lattitide: "+parts[0]);
            longout.setText("Longitude:"+parts[1]);
            Toast.makeText(MainActivity.this,"Bundle 555 successful",Toast.LENGTH_LONG).show();
            try {
                Intent intent1 = new Intent(MainActivity.this, mapActivity.class);
                Bundle bundle = new Bundle();
                bundle.putDouble("lattitude", Double.parseDouble(parts[0]));
                bundle.putDouble("longitude", Double.parseDouble(parts[1]));
                intent1.putExtras(bundle);
                Toast.makeText(MainActivity.this,"Bundle successful",Toast.LENGTH_LONG).show();
              //  startActivity(intent1);
            }
            catch(Exception ee)
            {
                Toast.makeText(MainActivity.this,"Bundle not successful",Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {

        final String eventString = "onNdefPushComplete\n" + event.toString();
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        eventString,
                        Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {

        String stringOut = cord;
        byte[] bytesOut = stringOut.getBytes();

        NdefRecord ndefRecordOut = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA,
                "text/plain".getBytes(),
                new byte[] {},
                bytesOut);

        NdefMessage ndefMessageout = new NdefMessage(ndefRecordOut);
        return ndefMessageout;
    }

}