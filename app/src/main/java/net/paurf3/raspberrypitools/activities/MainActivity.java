package net.paurf3.raspberrypitools.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import net.paurf3.raspberrypitools.R;
import net.paurf3.raspberrypitools.activities.fragments.CommandLineFragment;
import net.paurf3.raspberrypitools.activities.fragments.FileExplorerFragment;
import net.paurf3.raspberrypitools.activities.fragments.MainFragment;
import net.paurf3.raspberrypitools.models.connection.Ssh;

public class MainActivity extends AppCompatActivity {
    //Defining Variables
    private DrawerLayout drawerLayout;

    public static boolean atCompleted;

    //Fragments
    private Fragment actualFragment = null;
    private MainFragment mainFragment;
    private CommandLineFragment commandLineFragment;
    private FileExplorerFragment fileExplorerFragment;

    private static final String TAG_MAIN_FRAGMENT = "mainFragment";
    private static final String TAG_COMMANDLINE_FRAGMENT = "commandLineFragment";
    private static final String TAG_FILE_EXPLORER_FRAGMENT = "fileExplorerFragment";

    public static Ssh ssh;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = getSharedPreferences("saved_host_settings", MODE_PRIVATE);


        NavigationView navView;
        View headerLayoutNavView;

        TextView tv_HeaderNavViewIP;


        Toolbar appbar;

        //Cuando se haya conectado la activity, debe ejecutar el MainFragment.
        if (savedInstanceState == null) {
            mainFragment = new MainFragment();
            commandLineFragment = new CommandLineFragment();
            fileExplorerFragment = new FileExplorerFragment();
            actualFragment = mainFragment;
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_frame, mainFragment, TAG_MAIN_FRAGMENT)
                    .add(R.id.content_frame, commandLineFragment, TAG_COMMANDLINE_FRAGMENT)
                    .add(R.id.content_frame, fileExplorerFragment, TAG_FILE_EXPLORER_FRAGMENT)
                    .hide(commandLineFragment)
                    .hide(fileExplorerFragment)
                    .show(mainFragment)
                            //.replace(R.id.content_frame, mainFragment)
                    .commit();
        } else {
            mainFragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(TAG_MAIN_FRAGMENT);
            commandLineFragment = (CommandLineFragment) getSupportFragmentManager().findFragmentByTag(TAG_COMMANDLINE_FRAGMENT);
            fileExplorerFragment = (FileExplorerFragment) getSupportFragmentManager().findFragmentByTag(TAG_FILE_EXPLORER_FRAGMENT);
 
            /*if(mainFragment.isVisible()) fragmentActual = mainFragment;
            else fragmentActual = clFragment;
 
            getSupportFragmentManager().beginTransaction()
                    //.replace(R.id.content_frame, fragmentActual)
                    .hide(mainFragment)
                    .hide(clFragment)
                    .show(fragmentActual)
                    .commit();*/
        }


        //Código de navigation drawer
        appbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(appbar);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_nav_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navView = (NavigationView) findViewById(R.id.navview);
        //Instanciamos el header del navview y posteriormente el textview del mismo header para después poder adaptar la IP a él
        headerLayoutNavView = navView.getHeaderView(0);
        tv_HeaderNavViewIP = (TextView) headerLayoutNavView.findViewById(R.id.tv_header_navview_ip_raspberry);

        tv_HeaderNavViewIP.setText(sharedPref.getString("saved_pref_host", "IP"));

        navView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        boolean fragmentTransaction = false;

                        switch (menuItem.getItemId()) {
                            case R.id.menu_main_option:
                                if (atCompleted) {
                                    actualFragment = mainFragment;
                                    fragmentTransaction = true;
                                }
                                break;

                            case R.id.menu_commandline_option:
                                if (atCompleted) {
                                    actualFragment = commandLineFragment;
                                    fragmentTransaction = true;
                                }
                                break;

                            case R.id.menu_file_explorer_option:
                                if(atCompleted){
                                    actualFragment = fileExplorerFragment;
                                    fragmentTransaction = true;
                                }
                                break;

                            case R.id.menu_others_configuration:
                                //TODO Configuration fragment/activity
                                break;

                            case R.id.menu_others_logout:
                                //Abrimos de nuevo la LoginActivit, cerramos conexión SSH y
                                // cerramos la MainActivity
                                ssh.disconnect();
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                                break;
                        }

                        if (fragmentTransaction) {
                            getSupportFragmentManager().beginTransaction()
                                    //.replace(R.id.content_frame, fragmentActual)
                                    .hide(mainFragment)
                                    .hide(commandLineFragment)
                                    .hide(fileExplorerFragment)
                                    .show(actualFragment)
                                    .commit();

                            menuItem.setChecked(true);
                            getSupportActionBar().setTitle(menuItem.getTitle());
                        }

                        drawerLayout.closeDrawers();

                        return true;
                    }
                });


    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    //    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        if (savedInstanceState.getBoolean("mostrando_cl", false)) {
//            actualFragment = commandLineFragment;
//        } else {
//            actualFragment = mainFragment;
//        }
//        getSupportFragmentManager().beginTransaction()
//                //.replace(R.id.content_frame, fragmentActual)
//                .hide(mainFragment)
//                .hide(commandLineFragment)
//                .show(actualFragment)
//                .commit();
//
//    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        outState.putBoolean("mostrando_cl", actualFragment == commandLineFragment);
//        super.onSaveInstanceState(outState);
//    }


    @Override
    protected void onPause() {
        super.onPause();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        ssh.disconnect();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }




}