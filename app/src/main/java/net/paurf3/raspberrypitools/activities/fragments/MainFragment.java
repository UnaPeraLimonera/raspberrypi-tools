package net.paurf3.raspberrypitools.activities.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.JSchException;

import net.paurf3.raspberrypitools.R;
import net.paurf3.raspberrypitools.activities.MainActivity;
import net.paurf3.raspberrypitools.models.connection.Info;


public class MainFragment extends Fragment {
    private SwipeRefreshLayout swipeContainer;

    //Declarar TextViews
    private TextView tv_headerNavViewIP;
    //CPU
    private TextView tv_CPUTemp;
    private TextView tv_CPUMaxFreq;
    private TextView tv_CPUMinFreq;
    private TextView tv_CPUCurFreq;
    //RAM
    private TextView tv_MemTotal;
    private TextView tv_MemFree;
    private TextView tv_MemUsed;
    //NETWORK
    private TextView tv_ActiveNetworkInterface;
    private TextView tv_ActiveIP;
    //SYSTEM
    private TextView tv_Uptime;
    //SERVICES
    private TextView tv_CheckMinecraftSrv;


    private ProgressDialog fetchDataPD;
    private ProgressDialog reconnectionPD;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fetchDataPD = new ProgressDialog(getActivity());
        fetchDataPD.setCancelable(false);
        fetchDataPD.setMessage(getString(R.string.dialog_charging));
        fetchDataPD.show();
        //Al crear el fragment, se ejecuta la recogida de datos
        new AsyncTaskGetData().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_main, container, false);


        setHasOptionsMenu(true);

        //Inicializar TextViews
        //CPU
        tv_CPUTemp = (TextView) rootView.findViewById(R.id.tv_cputemp);
        tv_CPUMaxFreq = (TextView) rootView.findViewById(R.id.tv_cpumaxfreq);
        tv_CPUMinFreq = (TextView) rootView.findViewById(R.id.tv_cpuminfreq);
        tv_CPUCurFreq = (TextView) rootView.findViewById(R.id.tv_cpucurfreq);
        //RAM
        tv_MemTotal = (TextView) rootView.findViewById(R.id.tv_memtotal);
        tv_MemFree = (TextView) rootView.findViewById(R.id.tv_memfree);
        tv_MemUsed = (TextView) rootView.findViewById(R.id.tv_memused);
        //NETWORK
        tv_ActiveNetworkInterface = (TextView) rootView.findViewById(R.id.tv_active_network_interface);
        tv_ActiveIP = (TextView) rootView.findViewById(R.id.tv_active_network_ip);
        //SYSTEM
        tv_Uptime = (TextView) rootView.findViewById(R.id.tv_uptime);
        //SERVICES
        tv_CheckMinecraftSrv = (TextView) rootView.findViewById(R.id.tv_check_minecraft_srv);


        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new AsyncTaskGetData().execute();
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        return rootView;

    }


    @Override
    public void onResume() {
        super.onResume();
        //CONTROLAMOS SI SE PULSA EL BOTÓN DE ATRÁS DEL TELÉFONO, ENTONCES, IRÁ AL DIRECTORIO SUPERIOR
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    confirmExitDialog(getActivity());
                    return true;
                }
                return false;
            }
        });
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_toolbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //Si se pulsa el botón refresh del Toolbar, actualizará los datos.
            case R.id.menu_toolbar_mainfragment_refresh:
                new AsyncTaskGetData().execute();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    public void populateFields(Info info) {
        //Preparación campos Activity
        //CPU
        if(!info.getCpuTemp().equals("")){
            tv_CPUTemp.setText(info.getCpuTemp());
        } else {
            tv_CPUTemp.setText(getString(R.string.error_not_available));
        }
        if(!info.getCpuMaxFreq().equals("")){
            tv_CPUMaxFreq.setText(info.getCpuMaxFreq());
        } else {
            tv_CPUMaxFreq.setText(R.string.error_not_available);
        }
        if(!info.getCpuMinFreq().equals("")){
            tv_CPUMinFreq.setText(info.getCpuMinFreq());
        } else {
            tv_CPUMinFreq.setText(R.string.error_not_available);
        }
        if(!info.getCpuCurFreq().equals("")){
            tv_CPUCurFreq.setText(info.getCpuCurFreq());
        } else {
            tv_CPUCurFreq.setText(R.string.error_not_available);
        }

        //RAM
        if(!info.getMemTotal().equals("")){
            tv_MemTotal.setText(info.getMemTotal());
        } else{
            tv_MemTotal.setText(R.string.error_not_available);
        }
        if(!info.getMemFree().equals("")){
            tv_MemFree.setText(info.getMemFree());
         }else{
            tv_MemFree.setText(R.string.error_not_available);
        }
        if(!info.getMemUsed().equals("")){
            tv_MemUsed.setText(info.getMemUsed());
        } else{
            tv_MemUsed.setText(R.string.error_not_available);
        }

        //NETWORK
        if(!info.getActiveNetworkInterface().equals("")){
            tv_ActiveNetworkInterface.setText(info.getActiveNetworkInterface());
        } else {
            tv_ActiveNetworkInterface.setText(R.string.error_not_available);
        }
        if(!info.getActiveLocalIP().equals("")){
            tv_ActiveIP.setText(info.getActiveLocalIP());
        } else {
            tv_ActiveIP.setText(R.string.error_not_available);
        }

        //SYSTEM
        if(!info.getUptime().equals("")){
            tv_Uptime.setText(info.getUptime() + " " + getString(R.string.str_hours));
        } else {
            tv_Uptime.setText(R.string.error_not_available);
        }

        //SERVICES
        tv_CheckMinecraftSrv.setText(info.getStrCheckMinecraftSrvRunning());
        if (tv_CheckMinecraftSrv.getText().equals(getResources().getString(R.string.service_not_running))) {
            tv_CheckMinecraftSrv.setTextColor(Color.RED);
        } else if (tv_CheckMinecraftSrv.getText().equals(getResources().getString(R.string.service_running))) {
            tv_CheckMinecraftSrv.setTextColor(Color.GREEN);
        }
    }

    private void confirmExitDialog(Context context) {

        new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_confirm_exit_title)
                .setMessage(R.string.dialog_confirm_exit)
                .setIcon(R.drawable.ic_close_black_24dp)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).show();

    }


    class AsyncTaskSSHReconnect extends AsyncTask<String, Void, Boolean> {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("saved_host_settings", Context.MODE_PRIVATE);
        String user = sharedPref.getString("saved_pref_user", "");
        String password = sharedPref.getString("saved_pref_password", "");
        String host = sharedPref.getString("saved_pref_host", "");
        int port = sharedPref.getInt("saved_pref_port", 0);


        @Override
        protected void onPreExecute() {
            reconnectionPD = new ProgressDialog(getActivity());
            reconnectionPD.setCancelable(false);
            reconnectionPD.setMessage(getString(R.string.dialog_charging));
            reconnectionPD.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            //Lanzamos conexión contra la raspberry

            try {
                MainActivity.ssh.connection(user, password, host, port);
                return true;
            } catch (JSchException ex) {
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean connectionSuccesful) {
            reconnectionPD.dismiss();
            if (connectionSuccesful) {
                Toast.makeText(getActivity(), R.string.reconnected, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), R.string.error_cannot_reconnect, Toast.LENGTH_SHORT).show();
            }

        }
    }

    class AsyncTaskGetData extends AsyncTask<String, Void, Info> {

        @Override
        protected void onPreExecute() {
            MainActivity.atCompleted = false;
        }

        @Override
        protected Info doInBackground(String... params) {
            //Objeto Info que al inicializarlo con parámetro true, recoge toda la información
            //del servidor SSH en sus atributos
            try {
                Info info = new Info(true, getActivity());
                //Devolvemos el objeto info inicializado y con todos sus atributos con información
                return info;
            } catch (JSchException ex) {
                return null;
            } catch (NumberFormatException ex) {
                return null;
            } catch (NullPointerException ex) {
                return null;
            }


        }

        @Override
        protected void onPostExecute(Info info) {
            fetchDataPD.dismiss();
            swipeContainer.setRefreshing(false);

            //Rellenamos todos los campos con el método, pasando el objeto Info que contiene
            //toda la información
            if (info == null) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Error")
                        .setMessage(R.string.error_cannot_fetch_data)
                        .setPositiveButton(R.string.try_reconnect, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new AsyncTaskSSHReconnect().execute();

                            }
                        }).setNegativeButton(R.string.go_back_login, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
            } else {
                //Si la conexión y la recogida de datos se ha realizado correctamente, poblamos los campos
                populateFields(info);
                MainActivity.atCompleted = true;

            }


        }


    }
}
