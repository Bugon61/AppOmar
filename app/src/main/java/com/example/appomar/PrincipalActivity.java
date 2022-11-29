package com.example.appomar;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class PrincipalActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, PopupMenu.OnMenuItemClickListener {


    private TextView bienvenida, nombre, email, fecha, nacion;
    private Button admin, editar, eliminar;
    private GoogleMap mMap;
    double latitud, longitud;
    private ImageView perfil;
    String foto;
    Uri uriPerfil;
    private final int GALLERY_REQ_CODE = 1000;
    //private Menu menu;
    private MenuItem Iadmin, Ieditar, Ieliminar;


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
        /*Iadmin = menu.findItem(R.id.admin);
        Ieditar = menu.findItem(R.id.editar);
        Ieliminar = menu.findItem(R.id.eliminar);*/


        nombre.setText(preferences.getString("user", ""));

        String usuario = preferences.getString("user", "");

        String endpoint = "https://omarbugon.com/datos";
        String[] credenciales = {usuario.trim(), endpoint};
        API api = new API();
        api.execute(credenciales);

        //Verificar Admin
        String endpointAdmin = "https://omarbugon.com/admin";
        String[] credencialesAdmin = {usuario.trim(), endpointAdmin};
        apiAdmin apiAdmin = new apiAdmin();
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

        //Foto
        perfil = findViewById(R.id.imagePerfil);

        /*try {
            uriPerfil = Uri.parse(preferences.getString("foto", ""));
            perfil.setImageURI(uriPerfil);
        } catch (Exception e){
            Toast.makeText(this, "errorFoto", Toast.LENGTH_SHORT).show();
        }*/
    }


    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        popup.setOnMenuItemClickListener(this);
        inflater.inflate(R.menu.main_menu, popup.getMenu());
        Iadmin = popup.getMenu().findItem(R.id.admin);
        Ieditar = popup.getMenu().findItem(R.id.editar);
        Ieliminar = popup.getMenu().findItem(R.id.eliminar);
        popup.show();
    }

    public void foto(View view){
        Intent iGallery = new Intent(Intent.ACTION_PICK);
        iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(iGallery, GALLERY_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode==RESULT_OK){
            if (requestCode==GALLERY_REQ_CODE){
                //Para la galeria
                uriPerfil = data.getData();
                //Toast.makeText(this, "Obteniendo imagen del galeriS", Toast.LENGTH_SHORT).show();
                //Toast.makeText(this, uriPerfil.toString(), Toast.LENGTH_SHORT).show();
                perfil.setImageURI(uriPerfil);

                /*SharedPreferences preferencias = getSharedPreferences("datos", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferencias.edit();

                editor.putString("foto", uriPerfil.toString());
                editor.commit();
                finish();*/
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriPerfil);

                    /*ByteArrayOutputStream baos=new  ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
                    byte [] b=baos.toByteArray();
                    String temp=Base64.encodeToString(b, Base64.DEFAULT);*/

                    ByteArrayOutputStream baos=new  ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
                    byte [] arr=baos.toByteArray();
                    String result=Base64.encodeToString(arr, Base64.DEFAULT);

                    SharedPreferences preferences = getSharedPreferences("datos", Context.MODE_PRIVATE);
                    String usuario = preferences.getString("user", "");
                    String endpointFoto = "https://omarbugon.com/fotoGuardar";
                    String[] credencialesAdmin = {usuario.trim(), result, endpointFoto};
                    apiFotoGuardar apiFotoGuardar = new apiFotoGuardar();
                    apiFotoGuardar.execute(credencialesAdmin);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //startActivity(getIntent());
            }
        }
    }

    public void cerrarSesion(PrincipalActivity view){
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
            Intent intent = getIntent();
            finish();
            startActivity(intent);
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

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.logout:
                cerrarSesion(this);
                return true;
            default:
                return false;
        }
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
                foto = json.getString("foto");

                //Toast.makeText(PrincipalActivity.this, foto, Toast.LENGTH_SHORT).show();
                perfil.setImageResource(R.mipmap.avatar);

                if(foto.length() != 0){
                    try{
                    /*byte [] encodeByte = Base64.decode(foto,Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                    perfil.setImageBitmap(bitmap);*/
                        /*byte[] decodedString = Base64.decode(foto ,Base64.DEFAULT);
                        InputStream inputStream  = new ByteArrayInputStream(decodedString);
                        Bitmap bitmap  = BitmapFactory.decodeStream(inputStream);
                        perfil.setImageBitmap(bitmap);*/

                        byte [] encodeByte=Base64.decode(foto,Base64.DEFAULT);
                        Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                        perfil.setImageBitmap(bitmap);

                        //Toast.makeText(PrincipalActivity.this, "Lo intente", Toast.LENGTH_SHORT).show();
                    }
                    catch(Exception e){
                        Toast.makeText(PrincipalActivity.this, "Error Poner la Foto", Toast.LENGTH_SHORT).show();
                    }
                } else{
                    perfil.setImageResource(R.mipmap.avatar);
                    //Toast.makeText(PrincipalActivity.this, "Imagen", Toast.LENGTH_SHORT).show();
                }


            } catch (Exception e) {
                Toast.makeText(PrincipalActivity.this, "errorDatos", Toast.LENGTH_SHORT).show();
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
                    /*Iadmin.setVisible(true);
                    Ieditar.setVisible(true);
                    Ieliminar.setVisible(true);*/

                }

            } catch (Exception e) {
                Toast.makeText(PrincipalActivity.this, "errorAdmin", Toast.LENGTH_SHORT).show();
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
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.audio);
        Ringtone r = RingtoneManager.getRingtone(PrincipalActivity.this, uri);
        r.play();

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(PrincipalActivity.this);
        managerCompat.notify(1,builder.build());

    }

    //API para guardar la foto
    private class apiFotoGuardar extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... credenciales) {
            String respuesta = "bien";
            String username = credenciales[0];
            String uri = credenciales[1];
            String endpoint = credenciales[2];
            try {
                URL url = new URL(endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "*/*");
                conn.setDoOutput(true);
                String payload = "{\n   \"user\" : \"" + username + "\", \"uri\" : \"" + uri + "\"}";
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
                //JSONObject json = new JSONObject(respuesta);

                //Toast.makeText(PrincipalActivity.this, respuesta, Toast.LENGTH_SHORT).show();


            } catch (Exception e) {
                Toast.makeText(PrincipalActivity.this, "errorFoto", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}