package com.example.appomar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class MainActivity extends AppCompatActivity {

    private EditText usr, psw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usr = (EditText) findViewById(R.id.txt_usuario);
        psw = (EditText) findViewById(R.id.txt_contraseña);

        SharedPreferences preferences = getSharedPreferences("datos", Context.MODE_PRIVATE);

        boolean login = preferences.getBoolean("login", Boolean.parseBoolean(""));
        if(login){
            irPrincipal(this);
        }

    }

    public void login (View view){

        String usuario = usr.getText().toString();
        String contraseña = psw.getText().toString();

        if(usuario.length() != 0 && contraseña.length() != 0){
            //Toast.makeText(this, "Bienvenido usuario " + usuario, Toast.LENGTH_SHORT).show();
            String endpoint = "https://omarbugon.com/login";
            String[] credenciales = {usuario.trim(),contraseña.trim(), endpoint};
            API api = new API();
            api.execute(credenciales);
        } else {
            if (usuario.length() == 0) {
                Toast.makeText(this, "Introduzca usuario", Toast.LENGTH_SHORT).show();
            }
            if (usuario.length() == 0) {
                Toast.makeText(this, "Introduzca contraseña", Toast.LENGTH_SHORT).show();
            }
        }


    }

    private class API extends AsyncTask<String, String, String>
    {
        @Override
        protected String doInBackground(String... credenciales)
        {
            String respuesta = "bien";
            String username = credenciales[0];
            String password = credenciales[1];
            String endpoint = credenciales[2];
            try
            {
                URL url = new URL(endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "*/*");
                conn.setDoOutput(true);
                String payload = "{\n   \"user\" : \""+username+"\",\n   \"psw\" : \""+password+"\"\n}";
                try (OutputStream os = conn.getOutputStream())
                {
                    byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)))
                {
                    StringBuilder resp = new StringBuilder();
                    String respLine = null;
                    while ((respLine = br.readLine()) != null)
                    {
                        resp.append(respLine.toString());
                    }
                    respuesta = resp.toString();
                }
            }
            catch (Exception e)
            {
                respuesta = "wer";
                e.printStackTrace();
            }
            return respuesta;
        }


        @Override
        protected void onPostExecute(String respuesta)
        {
            try
            {
                JSONObject json = new JSONObject(respuesta);
                Toast.makeText(MainActivity.this, json.getString("mensaje"), Toast.LENGTH_SHORT).show();
                SharedPreferences preferencias = getSharedPreferences("datos", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferencias.edit();
                if(json.getInt("codigo") == 101){
                    editor.putString("user", usr.getText().toString());
                    editor.putBoolean("login", true);
                    editor.commit();
                    finish();
                    startActivity(getIntent());
                    //Toast.makeText(MainActivity.this, usr.getText().toString(), Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e)
            {
                Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

    }


    public void irPrincipal(MainActivity view){
        Intent irPrincipal = new Intent(this, PrincipalActivity.class);
        //irPrincipal.putExtra("usuario", usr.getText().toString());
        startActivity(irPrincipal);
    }

    public void irRegistro(View view){
        Intent irRegistro = new Intent(this, RegistroActivity.class);
        startActivity(irRegistro);
    }
}