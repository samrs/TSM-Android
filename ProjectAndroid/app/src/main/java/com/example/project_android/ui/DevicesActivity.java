package com.example.project_android.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_android.R;
import com.example.project_android.RecyclerItemTouch;
import com.example.project_android.SMS;
import com.example.project_android.dataClases.DeviceInfo;
import com.example.project_android.services.EarthquakeLocationService;
import com.example.project_android.ui.adapter.DevicesAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class DevicesActivity extends AppCompatActivity implements RecyclerItemTouch.RecyclerItemTouchHelperListener {

    private List<DeviceInfo> listDevices = new ArrayList<>();
    private RecyclerView recyclerView;
    private DevicesAdapter devicesAdapter;
    private Button buttonEmergency;


    private final int PERMISSION_REQUEST_CODE = 4000;
    private FusedLocationProviderClient fusedLocationClient;
    private SharedPreferences sharedPref;

    private static final String TAG = "DevicesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        sharedPref = this.getSharedPreferences(
                getString(R.string.preferences_file_key), Context.MODE_PRIVATE);

        Intent intentEarthquake = new Intent(this, EarthquakeLocationService.class);
        startService(intentEarthquake);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        FloatingActionButton fabAddDevice = findViewById(R.id.fabAddDevice);
        fabAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), AddDeviceActivity.class);
                intent.putExtra("LAST_ID", listDevices.size());
                startActivity(intent);
            }
        });

        FloatingActionButton fabGetLocation = findViewById(R.id.getLocationImageButton);
        fabGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkLocationPermisson();
            }
        });

        FloatingActionButton regex = findViewById(R.id.voice);
        regex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "¿En qué te puedo ayudar");

                try {
                    startActivityForResult(intent,1000);
                }
                catch (Exception e)
                {
                    Toast.makeText(getBaseContext(), " "+e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });

        recyclerView = findViewById(R.id.rvDevicesList);

        recyclerView = findViewById(R.id.rvDevicesList);
        buttonEmergency = findViewById(R.id.contactosID);

        buttonEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getBaseContext(), SMS.class);
                startActivity(intent);

            }
        });

        ItemTouchHelper.SimpleCallback simpleCallback =
                new RecyclerItemTouch(0, ItemTouchHelper.LEFT, DevicesActivity.this);
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE){
            getLocation();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1000:
            {
                if(resultCode == RESULT_OK && null!= data)
                {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String m = result.get(0).toLowerCase();
                    String cuarto = getName(m);
                    if (!cuarto.equals("errorV"))
                    {
                        if (Pattern.matches("enciende.*|prende.*|activa.*|arranca.*|encender.*|ilumina.*", m))
                        {
                            UpdateV upV= new UpdateV("https://tsmpjgv9.000webhostapp.com/switchByName.php",cuarto,1);
                            upV.execute();
                        }
                        else
                        {
                            if (Pattern.matches("apaga.*|desactiva.*|quita.*|", m)) {
                                UpdateV upV= new UpdateV("https://tsmpjgv9.000webhostapp.com/switchByName.php",cuarto,0);
                                upV.execute();
                            }
                        }
                    }
                }
                break;
            }
        }
    }
    private String getName(String message)
    {

        if(Pattern.matches("cuarto.*|.*cuarto",message))
            return "cuarto";
        else
        {
            //Toast.makeText(getBaseContext(), "Se pasó ", Toast.LENGTH_LONG).show();
            if(Pattern.matches("cocina.*|.*cocina",message))
                return "cocina";
            else
            {
                if(Pattern.matches("patio.*|.*patio",message))
                    return "patio";
                else
                {
                    if(Pattern.matches("sala.*|.*sala",message))
                        return "sala";
                    else
                    {
                        if(Pattern.matches("terraza.*|.*terraza",message))
                            return "terraza";
                        else
                        {
                            if(Pattern.matches("radio.*|.*radio",message))
                                return "radio";
                            else
                            {
                                if(Pattern.matches("habitación.*|.*habitación",message))
                                    return "habitacion";
                                else
                                {
                                    if(Pattern.matches("estudio.*|.*estudio",message))
                                        return "estudio";
                                    else
                                    {
                                        if(Pattern.matches("exterior.*|.*exterior",message))
                                            return "exterior";
                                        else
                                        {
                                            if(Pattern.matches("jardín.*|.*jardín",message))
                                                return "jardin";
                                            else
                                            {
                                                if(Pattern.matches("recámara.*|.*recámara",message))
                                                    return "recamara";
                                                else
                                                {
                                                    if(Pattern.matches("comedor.*|.*comedor",message))
                                                        return "comedor";
                                                    else
                                                    {
                                                        if(Pattern.matches("biblioteca.*|.*biblioteca",message))
                                                            return "biblioteca";
                                                        else
                                                        {
                                                            if(Pattern.matches("cochera.*|.*cochera",message))
                                                                return "cochera";
                                                            else
                                                            {
                                                                if(Pattern.matches("garaje.*|.*garaje",message))
                                                                    return "garaje";
                                                                else
                                                                {
                                                                    return "errorV";
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public class UpdateV extends AsyncTask<Void, Void, String> {

        private final String urlWebService;
        private final String name;
        private final Integer status;

        private UpdateV(String urlWebService, String name, Integer status) {
            this.urlWebService = urlWebService;
            this.name = name;
            this.status = status;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(urlWebService);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                StringBuilder sb = new StringBuilder();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept","application/json");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("name", name);
                jsonParam.put("status", status);
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                os.writeBytes(jsonParam.toString());
                os.flush();
                os.close();
                String message = conn.getResponseMessage();
                Log.d("STATUS", String.valueOf(conn.getResponseCode()));
                Log.d("MSG" , conn.getResponseMessage());

                conn.disconnect();
                return message;
            } catch (Exception e) {
                Log.d("DevicesActivity", e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response.equals("OK")){
                if(status==1) {
                    Toast.makeText(getBaseContext(), "Se ha prendido el/la "+name, Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(getBaseContext(), "Se ha apagado el/la "+name, Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        GetDevices getDevices = new GetDevices("https://tsmpjgv9.000webhostapp.com/get_devices.php", this);
        getDevices.execute();
    }

    @Override
    public void onSwipe(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if(viewHolder instanceof DevicesAdapter.ViewHolder)
        {


            String deviceName = listDevices.get(viewHolder.getAdapterPosition()).getName();
            final DeviceInfo deviceRemove = listDevices.get(viewHolder.getAdapterPosition());

            int DeletedIntex = viewHolder.getAdapterPosition();

            devicesAdapter.removeDevice(viewHolder.getAdapterPosition());

            restoreDeviceDeleted(viewHolder, deviceName, deviceRemove, DeletedIntex);

        }
    }

    private void restoreDeviceDeleted(RecyclerView.ViewHolder viewHolder, String deviceName, final DeviceInfo deviceRemove, final int DeletedIntex)
    {
        Snackbar snackbar = Snackbar.make(((DevicesAdapter.ViewHolder)viewHolder).layoutAborrar, deviceName + "Eliminado", Snackbar.LENGTH_LONG);
        snackbar.setAction("Deshacer", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                devicesAdapter.restoreDevice(deviceRemove, DeletedIntex);

            }
        });

        snackbar.setActionTextColor(Color.GREEN);
        snackbar.show();

    }
    private void checkLocationPermisson(){
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
        else {
            getLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation(){
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(getString(R.string.preferences_actual_lat), ((Double)location.getLatitude()).toString());
                            editor.putString(getString(R.string.preferences_actual_long), ((Double) location.getLongitude()).toString());
                            editor.apply();
                            Log.d(TAG, ((Double)location.getLatitude()).toString());
                            Log.d(TAG, ((Double)location.getLongitude()).toString());
                            Toast.makeText(getBaseContext(), "Ubicación actual guardada", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    class GetDevices extends AsyncTask<Void, Void, String> {

        private final String urlWebService;
        private final Context context;

        private GetDevices(String urlWebService, Context context){
            this.urlWebService = urlWebService;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(urlWebService);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                StringBuilder sb = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String json;
                while ((json = bufferedReader.readLine()) != null) {
                    sb.append(json).append("\n");
                }
                Log.d("DevicesActivity", sb.toString().trim());
                return sb.toString().trim();
            } catch (Exception e) {
                Log.d("DevicesActivity", e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null){
                listDevices.clear();
                GsonBuilder gsonBuilder = new GsonBuilder();
                try {
                    JSONArray jsonArray = new JSONArray(response);

                    for(int i = 0; i < jsonArray.length(); i++){
                        listDevices.add(gsonBuilder.create().fromJson(jsonArray.getString(i), DeviceInfo.class));
                    }
                    if (devicesAdapter != null){
                        devicesAdapter.updateDevices(listDevices);
                    }
                    else{
                        devicesAdapter = new DevicesAdapter(context, listDevices);
                        recyclerView.setAdapter(devicesAdapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(context));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}