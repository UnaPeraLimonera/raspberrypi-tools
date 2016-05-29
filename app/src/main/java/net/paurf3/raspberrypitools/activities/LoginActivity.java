package net.paurf3.raspberrypitools.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;


import com.jcraft.jsch.JSchException;

import net.paurf3.raspberrypitools.R;
import net.paurf3.raspberrypitools.models.connection.Ssh;

public class LoginActivity extends AppCompatActivity {

    EditText etUserLogin;
    EditText etPasswordLogin;
    EditText etHostLogin;
    EditText etPortLogin;


    private ProgressDialog connectionPD;
    public Ssh ssh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SharedPreferences sharedPref = LoginActivity.this.getSharedPreferences("saved_host_settings", MODE_PRIVATE);
        final SharedPreferences.Editor sharedPrefEditor = sharedPref.edit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //Inicializamos los EditText
        etUserLogin = (EditText) findViewById(R.id.et_user_login);
        etPasswordLogin = (EditText) findViewById(R.id.et_password_login);
        etHostLogin = (EditText) findViewById(R.id.et_host_login);
        etPortLogin = (EditText) findViewById(R.id.et_port_login);

        //TODO PARA DESARROLLO: Ponemos los datos del servidor en los campos
        etUserLogin.setText("root");
        etPasswordLogin.setText("raspbian");
        etHostLogin.setText("192.168.1.4");
        etPortLogin.setText("22");


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_connect_login);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Guardamos las credenciales en opciones
                sharedPrefEditor.putString("saved_pref_user",etUserLogin.getText().toString());
                sharedPrefEditor.putString("saved_pref_password",etPasswordLogin.getText().toString());
                sharedPrefEditor.putString("saved_pref_host",etHostLogin.getText().toString());
                sharedPrefEditor.putInt("saved_pref_port", Integer.parseInt(etPortLogin.getText().toString()));
                sharedPrefEditor.commit();

                //Al hacer click al Floating Button de connect, lanzamos la AsyncTask de conectar
                new AsyncTaskSSHConnect().execute();

            }
        });
    }



    class AsyncTaskSSHConnect extends AsyncTask<String, Void, Ssh> {
        //DATOS CONEXIÓN, falta hacer una Activity para conectar
        String user = etUserLogin.getText().toString();
        String password = etPasswordLogin.getText().toString();
        String host = etHostLogin.getText().toString();
        int port = Integer.parseInt(etPortLogin.getText().toString());


        @Override
        protected void onPreExecute() {
            ssh = new Ssh(user, password, host, port);
            connectionPD = new ProgressDialog(LoginActivity.this);
            connectionPD.setCancelable(false);
            connectionPD.setMessage(getString(R.string.dialog_charging));
            connectionPD.show();
        }

        @Override
        protected Ssh doInBackground(String... params) {
            //Lanzamos conexión contra la raspberry

            try {
                ssh.connection(user, password, host, port);
                return ssh;
            } catch (JSchException ex) {
                return null;
            }

        }

        @Override
        protected void onPostExecute(Ssh ssh) {
            connectionPD.dismiss();

            if (ssh == null) {
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle(R.string.error)
                        .setMessage(R.string.error_cannot_connect)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                new AsyncTaskSSHConnect().execute();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //No hacer nada
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else {
                //Si se ha conectado correctamente, abrir MainActivity
                MainActivity.ssh = ssh;

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);

                //Cerramos la activity Login
                finish();

            }

        }
    }

}
