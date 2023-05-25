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



    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        requireActivity().unregisterReceiver(widgetUpdater);
    }

}