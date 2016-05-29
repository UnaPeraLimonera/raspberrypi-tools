package net.paurf3.raspberrypitools.activities.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.JSchException;

import net.paurf3.raspberrypitools.R;
import net.paurf3.raspberrypitools.activities.MainActivity;

/**
 *
 */
public class CommandLineFragment extends Fragment {
    //Declarar TextViews y otros componentes
    private ScrollView sv_CommandLine;
    private TextView tv_CommandLine;
    private EditText et_Command;
    private Button btn_SendCommand;

    private ProgressDialog reconnectionPD;

    private String prompt;


    public CommandLineFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        new GetPrompt().execute();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_command_line, container, false);

        sv_CommandLine = (ScrollView) rootView.findViewById(R.id.sv_commandline);
        tv_CommandLine = (TextView) rootView.findViewById(R.id.tv_commandline);
        et_Command = (EditText) rootView.findViewById(R.id.et_command);
        btn_SendCommand = (Button) rootView.findViewById(R.id.btn_send_command);

        btn_SendCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Al hacer onclick al botón, si el campo de comando está vacío, no se enviará
                if (!et_Command.getText().toString().equals("")) {
                    if (et_Command.getText().toString().equals("clear")) {
                        tv_CommandLine.setText(prompt + " ");

                    } else {
                        new AsyncTaskSendCommand().execute(et_Command.getText().toString());
                    }
                    et_Command.setText("");
                }//TODO Al else Implementar que salte un aviso.

            }
        });

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


    class GetPrompt extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                return MainActivity.ssh.getUserAndHostPrompt();
            } catch (JSchException ex) {
                return null;
            }

        }

        @Override
        protected void onPostExecute(String output) {
            if (output == null) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.error)
                        .setMessage(R.string.error_connection_lost)
                        .setPositiveButton(R.string.try_reconnect, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                new AsyncTaskSSHReconnect().execute();
                            }
                        })
                        .setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //No hacer nada
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else {
                tv_CommandLine.append(output + " ");
                prompt = output + " ";
            }

        }
    }

    class AsyncTaskSendCommand extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            tv_CommandLine.append(et_Command.getText());
        }

        @Override
        protected String doInBackground(String... command) {
            try {
                return MainActivity.ssh.execCommand(command[0]);
            } catch (JSchException ex) {
                return null;
            }

        }

        @Override
        protected void onPostExecute(String output) {
            if (output == null) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Error")
                        .setMessage("No s'ha pogut connectar, reintentar?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                new AsyncTaskSendCommand().execute();
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
                //Añadimos el output del comando al final del TextView y, en línea a parte,
                //Volvemos a escribir el prompt.
                //Al final, hacemos scroll hacia abajo en el Scrollview;
                tv_CommandLine.append("\n" + output + prompt);
                sv_CommandLine.fullScroll(ScrollView.FOCUS_DOWN);
            }

        }
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
}
