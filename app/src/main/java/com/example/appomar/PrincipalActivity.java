package com.example.appomar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class PrincipalActivity extends AppCompatActivity {

    private TextView bienvenida, nombre, email, fecha, nacion;
    private Button admin, editar, eliminar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        //Establecer los datos
        SharedPreferences preferences = getSharedPreferences("datos", Context.MODE_PRIVATE);

        bienvenida = (TextView)findViewById(R.id.txt_bienvenida);
        nombre = (TextView) findViewById(R.id.txt_nombre);
        email = (TextView) findViewById(R.id.txt_email);
        nacion = (TextView)findViewById(R.id.txt_nacion);
        fecha = (TextView)findViewById(R.id.txt_fecha);
        admin = (Button)findViewById(R.id.buttonAdmin);
        editar = (Button)findViewById(R.id.buttonEditar);
        eliminar = (Button)findViewById(R.id.buttonEliminar);

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

        Uri uri = Uri.parse("android.resource://"+this.getPackageName()+"/" + R.raw.audio);
        AudioAttributes att = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
        channel.setSound(uri, att);

        NotificationManager manager = getSystemService(NotificationManager.class);

        manager.createNotificationChannel(channel);

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