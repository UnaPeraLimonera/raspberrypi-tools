package net.paurf3.raspberrypitools.activities.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import net.paurf3.raspberrypitools.R;
import net.paurf3.raspberrypitools.activities.MainActivity;
import net.paurf3.raspberrypitools.activities.itemdecorators.SimpleDividerItemDecoration;
import net.paurf3.raspberrypitools.adapters.FileExplorerEntryAdapter;
import net.paurf3.raspberrypitools.models.connection.FileExplorer;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;


public class FileExplorerFragment extends Fragment implements FileExplorerEntryAdapter.ClickListener {
    public boolean atGdIsCompleted;

    TextView tv_Pwd;
    RecyclerView rv_Remote;

    ProgressDialog reconnectionPD;

    FileExplorerEntryAdapter feEntryAdapter;

    FileExplorer fileExplorer;

    Vector<ChannelSftp.LsEntry> entryList;


    public FileExplorerFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        try {
            fileExplorer = new FileExplorer();
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        }

        new AsyncTaskGetDirectory().execute();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_file_explorer, container, false);
        rv_Remote = (RecyclerView) rootView.findViewById(R.id.rv_file_explorer_entries);
        rv_Remote.setLayoutManager(new LinearLayoutManager(getActivity()));

        tv_Pwd = (TextView) rootView.findViewById(R.id.tv_file_explorer_pwd);


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
                    if (!tv_Pwd.getText().equals("/")) {
                        atGdIsCompleted = false;
                        fileExplorer.goUpperDirectory();
                        new AsyncTaskGetDirectory().execute();
                    } else {
                        confirmExitDialog(getActivity());
                    }
                    return true;
                }
                return false;
            }
        });
    }


    @Override
    public void itemClicked(View view, int position) {
        if (atGdIsCompleted) {
            ChannelSftp.LsEntry entryClicked = entryList.get(position);

            if (entryClicked.getAttrs().isDir()) {
                fileExplorer.changeDir(entryClicked.getFilename());
                new AsyncTaskGetDirectory().execute();
            }
        }
    }
//
//    @Override
//    public void itemLongClicked(View view, int position) {
//        if (atGdIsCompleted) {
//            ChannelSftp.LsEntry entryClicked = entryList.get(position);
//
//            if (entryClicked.getAttrs().isDir()) {
//
//
//            }
//        }
//    }


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

    class AsyncTaskGetDirectory extends AsyncTask<String, String, Vector> {

        @Override
        protected void onPreExecute() {
            atGdIsCompleted = false;
        }

        @Override
        protected Vector doInBackground(String... workingDir) {

            return fileExplorer.lsDirectory();

        }


        @Override
        protected void onPostExecute(Vector output) {
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
                entryList = output;
                Vector<ChannelSftp.LsEntry> folders = new Vector<ChannelSftp.LsEntry>();
                Vector<ChannelSftp.LsEntry> files = new Vector<ChannelSftp.LsEntry>();


                for (ChannelSftp.LsEntry entry : entryList) {
                    //Comprobar si es un archivo o un directorio y separarlos
                    if (entry.getAttrs().isDir()) {
                        folders.add(entry);
                    } else {
                        files.add(entry);
                    }
                }

                //Ordenamos los dos vectores (directorios y archivos)
                Collections.sort(folders, new Comparator<ChannelSftp.LsEntry>() {
                    @Override
                    public int compare(ChannelSftp.LsEntry lhs, ChannelSftp.LsEntry rhs) {
                        return lhs.compareTo(rhs);
                    }
                });


                Collections.sort(files, new Comparator<ChannelSftp.LsEntry>() {
                    @Override
                    public int compare(ChannelSftp.LsEntry lhs, ChannelSftp.LsEntry rhs) {
                        return lhs.compareTo(rhs);
                    }
                });


                entryList.clear();
                //Primero, añadimos los directorios, ya ordenados
                for (ChannelSftp.LsEntry entry : folders) {
                    entryList.add(entry);
                }


                //Segundo, los archivos, ya ordenados
                for (ChannelSftp.LsEntry entry : files) {
                    entryList.add(entry);
                }

                feEntryAdapter = new FileExplorerEntryAdapter(entryList);
                feEntryAdapter.setClickListener(FileExplorerFragment.this);

                rv_Remote.setHasFixedSize(true);
                rv_Remote.setAdapter(feEntryAdapter);
                rv_Remote.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
                tv_Pwd.setText(fileExplorer.getAbsoluteCurrentPath());
                atGdIsCompleted = true;

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
