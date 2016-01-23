package org.pollgram.decision.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.pollgram.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class CrashManagerActivity extends Activity {

    public static final String PAR_ERROR_MESSAGE = "PAR_ERROR_MESSAGE";
    public static final String PAR_FULL_STACKTRACE = "PAR_FULL_STACKTRACE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String errorMsg = getIntent().getExtras().getString(PAR_ERROR_MESSAGE);
        final String stackTrace = getIntent().getExtras().getString(PAR_FULL_STACKTRACE);

        final StringBuilder msgBody = new StringBuilder();
        msgBody.append(getString(R.string.emailCrashMessageBody, errorMsg));

        AlertDialog.Builder builder = new AlertDialog.Builder(CrashManagerActivity.this);
        builder.setMessage(getString(R.string.appCrashedMessage));
        builder.setPositiveButton(R.string.sendCrashEmail, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                File logAttachmentFile;
                try {
                    logAttachmentFile = extractLogToFile(CrashManagerActivity.this,stackTrace);
                } catch (Exception e) {
                    logAttachmentFile = null;
                    msgBody.append('\n');
                    msgBody.append("Error in retreving log:" + e.getMessage());
                }

                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.emailBugDestAddress)});
                i.putExtra(Intent.EXTRA_SUBJECT, "Pollgram crash report");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra(Intent.EXTRA_TEXT, msgBody.toString());
                if (logAttachmentFile != null)
                    i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logAttachmentFile));

                try {
                    startActivity(Intent.createChooser(i, "Send crash report email..."));
                    Log.d("Crash", "Send email activity started");
                } catch (android.content.ActivityNotFoundException e) {
                    Toast.makeText(CrashManagerActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
                Log.e("Crash", "Application will ends due to a crash");
                System.exit(1);
            }
        }).show();

    }

    public static File extractLogToFile(Context context, String stackTrace) throws PackageManager.NameNotFoundException, IOException {
        PackageManager manager = context.getPackageManager();
        PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
        String model = Build.MODEL;
        if (!model.startsWith(Build.MANUFACTURER))
            model = Build.MANUFACTURER + " " + model;


        // Extract to file.
        File file = File.createTempFile("crash",".log", context.getExternalFilesDir(null));
        InputStreamReader reader = null;
        FileWriter writer = null;
        try {
            // For Android 4.0 and earlier, you will get all app's log output, so filter it to
            // mostly limit it to your app's output.  In later versions, the filtering isn't needed.
            String cmd = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) ?
                    "logcat -d -v time MyApp:v dalvikvm:v System.err:v *:s" :
                    "logcat -d -v time";

            // get input stream
            Process process = Runtime.getRuntime().exec(cmd);
            reader = new InputStreamReader(process.getInputStream());

            // write output stream
            writer = new FileWriter(file);
            writer.write("Android version: " + Build.VERSION.SDK_INT + "\n");
            writer.write("Device: " + model + "\n");
            writer.write("App version: " + (info == null ? "(null)" : info.versionCode) + "\n");
            writer.write("Application crash because of: "+ stackTrace+"\n");
            writer.write("-------------------------------------------");


            char[] buffer = new char[1024];
            int n = -1;
            while((n = reader.read(buffer, 0, buffer.length)) != -1){
                writer.write(buffer, 0, n);
            }
        } finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e1) {
                }
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e1) {
                }
        }
        return file;
    }

}
