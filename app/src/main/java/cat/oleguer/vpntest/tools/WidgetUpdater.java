package cat.oleguer.vpntest.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

public class WidgetUpdater extends BroadcastReceiver {
    private TextView textViewVPNStatus;
    private TextView openPortsCountTextView;
    private TextView closedPortsCountTextView;
    private TextView totalConnectionsCountTextView;
    private TextView ipTextView;

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