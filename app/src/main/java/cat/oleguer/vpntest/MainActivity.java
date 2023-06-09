package cat.oleguer.vpntest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import cat.oleguer.vpntest.databinding.ActivityMainBinding;
import cat.oleguer.vpntest.tools.BackgroundService;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            new AlertDialog.Builder(this)
                    .setTitle("VPN Test Tools")
                    .setMessage("Are you sure you want to exit?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            try {
                                Context context = MainActivity.super.getApplicationContext();
                                Intent stopIntent = new Intent(context, BackgroundService.class);
                                stopIntent.setAction("STOP");
                                Log.d(TAG,"Stopping BackgroundService ...");
                                context.startService(stopIntent);
                            } catch (Exception e) {
                                Log.d(TAG, "Error stopping BackgroundService: " + e.getMessage());
                            }

                            finish();
                        }
                    }).create().show();
        }
        if (id == R.id.action_main){
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            NavDestination currentDestination = navController.getCurrentDestination();
            if (currentDestination != null) {
                int fragmentId = currentDestination.getId();
                if (fragmentId == R.id.FirstFragment) {
                    Log.d(TAG,"FristFragment is loaded");
                } else if (fragmentId == R.id.FileListFragment) {
                    navController.navigate(R.id.action_FileListFragment_to_FirstFragment);
                    Log.d(TAG,"FileListFragment is loaded");
                }
            }
        }
        if (id == R.id.action_list){
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            NavDestination currentDestination = navController.getCurrentDestination();
            if (currentDestination != null) {
                int fragmentId = currentDestination.getId();
                if (fragmentId == R.id.FirstFragment) {
                    Log.d(TAG,"FristFragment is loaded");
                    navController.navigate(R.id.action_FirstFragment_to_FileListFragment);
                } else if (fragmentId == R.id.FileListFragment) {
                    Log.d(TAG,"FileListFragment is loaded");
                }
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}