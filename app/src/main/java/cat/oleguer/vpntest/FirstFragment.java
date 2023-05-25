package cat.oleguer.vpntest;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import cat.oleguer.vpntest.databinding.FragmentFirstBinding;
import cat.oleguer.vpntest.tools.BackgroundService;
import cat.oleguer.vpntest.tools.StartBackgroundService;
import cat.oleguer.vpntest.tools.WidgetUpdater;

public class FirstFragment extends Fragment {
    private static final String TAG = "FirstFragment";
    private FragmentFirstBinding binding;
    private WidgetUpdater widgetUpdater;
    //private TextView showCountTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        widgetUpdater = new WidgetUpdater(binding.openPortsCountTextView,
                                            binding.closedPortsCountTextView,
                                            binding.totalConnectionsCountTextView,
                                            binding.textviewIp,
                                            binding.textViewVPNStatus
                                            );

        IntentFilter intentFilter = new IntentFilter("UPDATE_COUNTERS");
        requireActivity().registerReceiver(widgetUpdater, intentFilter);

        binding.textViewVPNStatus.setText("Connectat: ...checking");
        binding.textViewVPNStatus.append("\nVPN: ...checking");

        startBackgroundService();

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMyServiceRunning(BackgroundService.class)){
                    Log.d(TAG,"Service already running - let's try to stop it");
                }
                Log.d(TAG,"Stop button pressed. Stopping service.");
                try {
                    Intent stopIntent = new Intent(getContext(), BackgroundService.class);
                    stopIntent.setAction("STOP");
                    getContext().startService(stopIntent);
                    Log.d(TAG,"Service stopped");
                } catch (Exception e) {
                    Log.d("Excepction",e.getMessage());
                }
            }
        });
        binding.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Log.d(TAG,"Starting Background Service");
                    startBackgroundService();
                } catch (Exception e) {
                    Log.d(TAG,"Error starting backround service" + e.getMessage());
                }
            }
        });
    }
    private void startBackgroundService(){
        try {

            if (isMyServiceRunning(BackgroundService.class)){
                Log.d(TAG,"Service already running");
            } else {
                StartBackgroundService.startBackgroundService(getContext());
            }
        } catch (Exception e) {
            Log.d("Excepction","Error starting Background Service" + e.getMessage());
        }
    }

    /*
    private void countMe(View view) throws Exception {
        // Get the value of the text view

        String countString = binding.textviewIp.getText().toString();
        Log.d("---olelog---",countString);
        // Convert value to a number and increment it
        Integer count = Integer.parseInt(countString);
        count++;
        // Display the new value in the text view.
        binding.textviewIp.setText(count.toString());

        String countstring = count.toString();
        Toast myToast = Toast.makeText(getActivity(),countstring, Toast.LENGTH_SHORT);
        myToast.show();
    }*/

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }




    /*
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction() != null) {
            if (intent.getAction().equals("UPDATE_COUNTERS")) {
                int openPortsCount = intent.getIntExtra("openPortsCount", 0);
                int closedPortsCount = intent.getIntExtra("closedPortsCount", 0);
                int totalConnectionsCount = intent.getIntExtra("totalConnectionsCount", 0);

                // Update the widget UI with the counter values
                updateWidgetUI(context, openPortsCount, closedPortsCount, totalConnectionsCount);
            }
        }
    }*/
    /*
    private void updateWidgetUI(Context context, int openPortsCount, int closedPortsCount, int totalConnectionsCount) {
        //binding.textViewOleguer.setText("Open Ports: " + openPortsCount +"\n Closed Ports: " + closedPortsCount);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.your_widget_layout);

        views.setTextViewText(R.id.openPortsCountTextView, "Open Ports: " + openPortsCount);
        views.setTextViewText(R.id.closedPortsCountTextView, "Closed Ports: " + closedPortsCount);
        views.setTextViewText(R.id.totalConnectionsCountTextView, "Total Connections: " + totalConnectionsCount);

        // Update the widget with the new RemoteViews
        AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context, YourAppWidgetProvider.class), views);
    }
    */

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        requireActivity().unregisterReceiver(widgetUpdater);
    }

}