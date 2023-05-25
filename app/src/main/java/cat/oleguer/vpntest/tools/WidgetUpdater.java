package cat.oleguer.vpntest.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

public class WidgetUpdater extends BroadcastReceiver {
    private final TextView textViewVPNStatus;
    private final TextView openPortsCountTextView;
    private final TextView closedPortsCountTextView;
    private final TextView totalConnectionsCountTextView;
    private final TextView ipTextView;

    /**
     *  Updates all TextView with received data
     *
     * @param openPortsCountTextView
     * @param closedPortsCountTextView
     * @param totalConnectionsCountTextView
     * @param ipTextView
     * @param vpnStatus
     */
    public WidgetUpdater(TextView openPortsCountTextView, TextView closedPortsCountTextView, TextView totalConnectionsCountTextView, TextView ipTextView, TextView vpnStatus) {
        this.openPortsCountTextView = openPortsCountTextView;
        this.closedPortsCountTextView = closedPortsCountTextView;
        this.totalConnectionsCountTextView = totalConnectionsCountTextView;
        this.ipTextView = ipTextView;
        this.textViewVPNStatus = vpnStatus;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("UPDATE_COUNTERS")) {
            int openPortsCount = intent.getIntExtra("openPortsCount", 0);
            int closedPortsCount = intent.getIntExtra("closedPortsCount", 0);
            int totalConnectionsCount = intent.getIntExtra("totalConnectionsCount", 0);
            String vpnStatus = intent.getStringExtra("vpnStatus");
            String ip = intent.getStringExtra("myIp");

            // Update the widget UI with the counter values
            openPortsCountTextView.setText("Open Ports: " + openPortsCount);
            closedPortsCountTextView.setText("Closed Ports: " + closedPortsCount);
            totalConnectionsCountTextView.setText("Total Connections: " + totalConnectionsCount);
            ipTextView.setText("Ip: " + ip);
            textViewVPNStatus.setText(vpnStatus);
        }
    }
}