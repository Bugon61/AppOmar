package com.example.appomar;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class PrincipalActivity extends AppCompatActivity {

    private TextView bienvenida, nombre, email, fecha, nacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        createNotificationChannel();

        //Establecer los datos del usuario
        SharedPreferences preferences = getSharedPreferences("datos", Context.MODE_PRIVATE);

        bienvenida = (TextView)findViewById(R.id.txt_bienvenida);
        nombre = (TextView) findViewById(R.id.txt_nombre);
        email = (TextView) findViewById(R.id.txt_email);
        nacion = (TextView)findViewById(R.id.txt_nacion);
        fecha = (TextView)findViewById(R.id.txt_fecha);

        nombre.setText(preferences.getString("user", ""));

        String usuario = preferences.getString("user", "");

        String endpoint = "https://omarbugon.com/datos";
        String[] credenciales = {usuario.trim(), endpoint};
        API api = new API();
        api.execute(credenciales);
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
                Toast.makeText(PrincipalActivity.this, "error", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "canal";
            String description = "descripcion";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("12345", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void notificacion(View view){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "12345")
                .setSmallIcon(R.drawable.buttonshapewhitebg)
                .setContentTitle("Notificacion AppOmar")
                .setContentText("Esta es una notificacion de AppOmar")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(12345, builder.build());
    }
}