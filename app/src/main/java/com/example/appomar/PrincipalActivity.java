package com.example.appomar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.IconCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class PrincipalActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {


    private TextView bienvenida, nombre, email, fecha, nacion;
    private Button admin, editar, eliminar;
    private GoogleMap mMap;
    double latitud, longitud;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        //Establecer los datos
        SharedPreferences preferences = getSharedPreferences("datos", Context.MODE_PRIVATE);

        bienvenida = (TextView) findViewById(R.id.txt_bienvenida);
        nombre = (TextView) findViewById(R.id.txt_nombre);
        email = (TextView) findViewById(R.id.txt_email);
        nacion = (TextView) findViewById(R.id.txt_nacion);
        fecha = (TextView) findViewById(R.id.txt_fecha);
        admin = (Button) findViewById(R.id.buttonAdmin);
        editar = (Button) findViewById(R.id.buttonEditar);
        eliminar = (Button) findViewById(R.id.buttonEliminar);

        nombre.setText(preferences.getString("user", ""));

        String usuario = preferences.getString("user", "");

        String endpoint = "https://omarbugon.com/datos";
        String[] credenciales = {usuario.trim(), endpoint};
        PrincipalActivity.API api = new PrincipalActivity.API();
        api.execute(credenciales);

        //Verificar Admin
        String endpointAdmin = "https://omarbugon.com/admin";
        String[] credencialesAdmin = {usuario.trim(), endpointAdmin};
        PrincipalActivity.apiAdmin apiAdmin = new PrincipalActivity.apiAdmin();
        apiAdmin.execute(credencialesAdmin);

        //Notificacion
        NotificationChannel channel = new NotificationChannel("Notificacion", "Notificacion", NotificationManager.IMPORTANCE_DEFAULT);

        Uri uri = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.audio);
        AudioAttributes att = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
        channel.setSound(uri, att);

        NotificationManager manager = getSystemService(NotificationManager.class);

        manager.createNotificationChannel(channel);

        //MAPA
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
        mapFragment.getMapAsync(this);


    }

    public void cerrarSesion(View view){
        SharedPreferences preferencias = getSharedPreferences("datos", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putString("user", "");
        editor.putBoolean("login", false);
        editor.commit();
        Intent irLogin = new Intent(this, MainActivity.class);
        startActivity(irLogin);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            retrievedLocation();
        } else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }

        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(latitud, longitud);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Tu Ubicacion Actual"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15));

    }


    @SuppressLint("MissingPermission")
    private void retrievedLocation() {
        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5,
            this);

        Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if(location != null){
            latitud = location.getLatitude();
            longitud = location.getLongitude();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 200 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            retrievedLocation();
        } else{
            latitud = Double.parseDouble("Permission Denied");
            longitud = Double.parseDouble("Permission Denied");
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    //API par conseguir datos
    private class API extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... credenciales) {
            String respuesta = "bien";
            String username = credenciales[0];
            String endpoint = credenciales[1];
            try {
                URL url = new URL(endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "*/*");
                conn.setDoOutput(true);
                String payload = "{\n   \"user\" : \"" + username + "\"}";
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder resp = new StringBuilder();
                    String respLine = null;
                    while ((respLine = br.readLine()) != null) {
                        resp.append(respLine.toString());
                    }
                    respuesta = resp.toString();
                }
            } catch (Exception e) {
                respuesta = "wer";
                e.printStackTrace();
            }
            return respuesta;
        }


        @Override
        protected void onPostExecute(String respuesta) {
            try {
                JSONObject json = new JSONObject(respuesta);

                email.setText(json.getString("email"));
                fecha.setText(json.getString("fecha"));
                nacion.setText(json.getString("nacion"));


            } catch (Exception e) {
                Toast.makeText(PrincipalActivity.this, "error1", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    //API par verificar admin
    private class apiAdmin extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... credenciales) {
            String respuesta = "bien";
            String username = credenciales[0];
            String endpoint = credenciales[1];
            try {
                URL url = new URL(endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "*/*");
                conn.setDoOutput(true);
                String payload = "{\n   \"user\" : \"" + username + "\"}";
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder resp = new StringBuilder();
                    String respLine = null;
                    while ((respLine = br.readLine()) != null) {
                        resp.append(respLine.toString());
                    }
                    respuesta = resp.toString();
                }
            } catch (Exception e) {
                respuesta = "wer";
                e.printStackTrace();
            }
            return respuesta;
        }


        @Override
        protected void onPostExecute(String respuesta) {
            try {
                JSONObject json = new JSONObject(respuesta);

                //Toast.makeText(PrincipalActivity.this, respuesta, Toast.LENGTH_SHORT).show();
                if(json.getInt("codigo") == 411){
                    admin.setVisibility(View.VISIBLE);
                    editar.setVisibility(View.VISIBLE);
                    eliminar.setVisibility(View.VISIBLE);
                }

            } catch (Exception e) {
                Toast.makeText(PrincipalActivity.this, "errorwsx", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    public void notificacion(View view){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(PrincipalActivity.this, "Notificacion");
        builder.setContentTitle("Notificacion");
        builder.setContentText("Esto es una notificacion para AppOmar");
        builder.setSmallIcon(R.mipmap.ic_launcher_foreground);
        builder.setAutoCancel(true);


        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(PrincipalActivity.this);
        managerCompat.notify(1,builder.build());

    }
}