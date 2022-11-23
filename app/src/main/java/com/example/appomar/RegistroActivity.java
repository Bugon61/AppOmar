package com.example.appomar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RegistroActivity extends AppCompatActivity {

    private Spinner spinnerN;
    private EditText usr, psw, psw2,eml, dt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        spinnerN = (Spinner) findViewById(R.id.spinner_nacionalidad);

        String [] naciones = {"Afganistán","Albania","Alemania","Andorra","Angola","Antigua y Barbuda","Arabia Saudita","Argelia","Argentina","Armenia","Australia","Austria","Azerbaiyán","Bahamas","Bangladés","Barbados","Baréin","Bélgica","Belice","Benín","Bielorrusia","Birmania","Bolivia","Bosnia y Herzegovina","Botsuana","Brasil","Brunéi","Bulgaria","Burkina Faso","Burundi","Bután","Cabo Verde","Camboya","Camerún","Canadá","Catar","Chad","Chile","China","Chipre","Ciudad del Vaticano","Colombia","Comoras","Corea del Norte","Corea del Sur","Costa de Marfil","Costa Rica","Croacia","Cuba","Dinamarca","Dominica","Ecuador","Egipto","El Salvador","Emiratos Árabes Unidos","Eritrea","Eslovaquia","Eslovenia","España","Estados Unidos","Estonia","Etiopía","Filipinas","Finlandia","Fiyi","Francia","Gabón","Gambia","Georgia","Ghana","Granada","Grecia","Guatemala","Guyana","Guinea","Guinea ecuatorial","Guinea-Bisáu","Haití","Honduras","Hungría","India","Indonesia","Irak","Irán","Irlanda","Islandia","Islas Marshall","Islas Salomón","Israel","Italia","Jamaica","Japón","Jordania","Kazajistán","Kenia","Kirguistán","Kiribati","Kuwait","Laos","Lesoto","Letonia","Líbano","Liberia","Libia","Liechtenstein","Lituania","Luxemburgo","Madagascar","Malasia","Malaui","Maldivas","Malí","Malta","Marruecos","Mauricio","Mauritania","México","Micronesia","Moldavia","Mónaco","Mongolia","Montenegro","Mozambique","Namibia","Nauru","Nepal","Nicaragua","Níger","Nigeria","Noruega","Nueva Zelanda","Omán","Países Bajos","Pakistán","Palaos","Palestina","Panamá","Papúa Nueva Guinea","Paraguay","Perú","Polonia","Portugal","Reino Unido","República Centroafricana","República Checa","República de Macedonia","República del Congo","República Democrática del Congo","República Dominicana","República Sudafricana","Ruanda","Rumanía","Rusia","Samoa","San Cristóbal y Nieves","San Marino","San Vicente y las Granadinas","Santa Lucía","Santo Tomé y Príncipe","Senegal","Serbia","Seychelles","Sierra Leona","Singapur","Siria","Somalia","Sri Lanka","Suazilandia","Sudán","Sudán del Sur","Suecia","Suiza","Surinam","Tailandia","Tanzania","Tayikistán","Timor Oriental","Togo","Tonga","Trinidad y Tobago","Túnez","Turkmenistán","Turquía","Tuvalu","Ucrania","Uganda","Uruguay","Uzbekistán","Vanuatu","Venezuela","Vietnam","Yemen","Yibuti","Zambia","Zimbabue"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item_omar, naciones);
        spinnerN.setAdapter(adapter);

        usr = (EditText) findViewById(R.id.registro_usuario);
        psw = (EditText) findViewById(R.id.registro_contraseña);
        psw2 = (EditText) findViewById(R.id.registro_contraseña2);
        eml = (EditText) findViewById(R.id.registro_email);
        dt = (EditText) findViewById(R.id.registro_nacimiento);

    }

    public void irLogin(View view){
        Intent irLogin = new Intent(this, MainActivity.class);
        startActivity(irLogin);
    }

    public void registrarse(View view){
        String usuario = usr.getText().toString();
        String contraseña = psw.getText().toString();
        String contraseña2 = psw2.getText().toString();
        String email = eml.getText().toString();
        String date = dt.getText().toString();
        String nacion = spinnerN.getSelectedItem().toString();


        if(usuario.length() != 0 && contraseña.length() != 0 && contraseña2.length() !=0 && email.length() != 0 && date.length() != 0){
            if(contraseña.equals(contraseña2)) {
                //Toast.makeText(this, "Bienvenido usuario " + usuario, Toast.LENGTH_SHORT).show();
                String endpoint = "https://omarbugon.com/insert";
                String[] credenciales = {usuario.trim(), contraseña.trim(), email.trim(), date.trim(), nacion.trim(), endpoint};
                //Toast.makeText(this, credenciales[4], Toast.LENGTH_SHORT).show();
                RegistroActivity.API api = new RegistroActivity.API();
                api.execute(credenciales);
            } else{
                Toast.makeText(this, "No coinciden las contraseñas", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (usuario.length() == 0) {
                Toast.makeText(this, "Introduzca usuario", Toast.LENGTH_SHORT).show();
            }
            if (contraseña.length() == 0) {
                Toast.makeText(this, "Introduzca contraseña", Toast.LENGTH_SHORT).show();
            }
            if (contraseña2.length() == 0) {
                Toast.makeText(this, "Repita la contraseña", Toast.LENGTH_SHORT).show();
            }
            if (email.length() == 0) {
                Toast.makeText(this, "Introduzca email", Toast.LENGTH_SHORT).show();
            }
            if (date.length() == 0) {
                Toast.makeText(this, "Introduzca nacimiento", Toast.LENGTH_SHORT).show();
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
            String email = credenciales[2];
            String birth = credenciales[3];
            String nation = credenciales[4];
            String endpoint = credenciales[5];
            try
            {
                URL url = new URL(endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "*/*");
                conn.setDoOutput(true);
                String payload = "{\n   \"user\" : \""+username+"\",\n   \"psw\" : \""+password+"\",\n " +
                        "\"email\" : \""+email+"\",\n  \"birth\" : \""+birth+"\",\n  \"nation\" : \""+nation+"\"\n}";
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
                Toast.makeText(RegistroActivity.this, json.getString("mensaje"), Toast.LENGTH_SHORT).show();

            }
            catch (Exception e)
            {
                Toast.makeText(RegistroActivity.this, "error", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

}