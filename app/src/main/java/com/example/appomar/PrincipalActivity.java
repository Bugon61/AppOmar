package com.example.appomar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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

        SharedPreferences preferences = getSharedPreferences("datos", Context.MODE_PRIVATE);

        /*boolean login = preferences.getBoolean("login", Boolean.parseBoolean(""));
        if(!login){
            SharedPreferences preferencias = getSharedPreferences("datos", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferencias.edit();
            editor.putString("user", "");
            editor.putBoolean("login", false);
            editor.commit();
            Intent irLogin = new Intent(this, MainActivity.class);
            startActivity(irLogin);
        }*/

        bienvenida = (TextView)findViewById(R.id.txt_bienvenida);
        nombre = (TextView) findViewById(R.id.txt_nombre);
        email = (TextView) findViewById(R.id.txt_email);
        nacion = (TextView)findViewById(R.id.txt_nacion);
        fecha = (TextView)findViewById(R.id.txt_fecha);

        nombre.setText(preferences.getString("user", ""));

        String usuario = preferences.getString("user", "");

        String endpoint = "https://omarbugon.com/datos";
        String[] credenciales = {usuario.trim(), endpoint};
        PrincipalActivity.API api = new PrincipalActivity.API();
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
}