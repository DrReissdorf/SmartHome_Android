package home.sven.smarthome_android.tools;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import home.sven.smarthome_android.R;

public class Alerter {
    public static void alertServerNotFound(Context context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.alertServerNotFound_title)
                .setMessage(R.string.alertServerNotFound_text)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public static void alertWithOkButton(Context context, int title, int message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
