/**
 * DISCLAIMER: PLEASE TAKE NOTE THAT THE SAMPLE APPLICATION AND
 * SOURCE CODE DESCRIBED HEREIN IS PROVIDED FOR TESTING PURPOSES ONLY.
 * <p>
 * Samsung expressly disclaims any and all warranties of any kind,
 * whether express or implied, including but not limited to the implied warranties and conditions
 * of merchantability, fitness for com.samsung.knox.example.genericvpnpolicy particular purpose and
 * non-infringement.
 * Further, Samsung does not represent or warrant that any portion of the sample application and
 * source code is free of inaccuracies, errors, bugs or interruptions, or is reliable,
 * accurate, complete, or otherwise valid. The sample application and source code is provided
 * "as is" and "as available", without any warranty of any kind from Samsung.
 * <p>
 * Your use of the sample application and source code is at its own discretion and risk,
 * and licensee will be solely responsible for any damage that results from the use of the sample
 * application and source code including, but not limited to, any damage to your computer system or
 * platform. For the purpose of clarity, the sample code is licensed “as is” and
 * licenses bears the risk of using it.
 * <p>
 * Samsung shall not be liable for any direct, indirect or consequential damages or
 * costs of any type arising out of any action taken by you or others related to the sample application
 * and source code.
 */
package cz.krajcovic.knoxsupport;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;

import com.samsung.android.knox.EnterpriseDeviceManager;
import com.samsung.android.knox.container.KnoxContainerManager;

import java.util.List;

public class Utils {

    private TextView textView;
    private String TAG;

    public Utils(TextView view, String className) {
        textView = view;
        TAG = className;
    }

    /** Check Knox API level on device, if it does not meet minimum requirement, end user
     * cannot use the applciation */
    public void checkApiLevel(int apiLevel, final Context context)
    {
        if(EnterpriseDeviceManager.getAPILevel() < apiLevel) {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(context);
            String msg = context.getResources().getString(R.string.api_level_message, EnterpriseDeviceManager.getAPILevel(),apiLevel);
            builder.setTitle(R.string.app_name)
                    .setMessage(msg)
                    .setCancelable(false)
                    .setPositiveButton("CLOSE",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    System.exit(0);
                                }
                            })
                    .show();

        } else {
            return;
        }
    }

    /** Log results to a textView in application UI */
    public void log(String text) {
        textView.append(text);
        textView.append("\n\n");
        textView.invalidate();
        Log.d(TAG,text);
    }

    /** Process the exception */
    public void processException(Exception ex, String TAG) {
        if (ex != null) {
            // present the exception message
            String msg = ex.getClass().getCanonicalName() + ": " + ex.getMessage();
            textView.append(msg);
            textView.append("\n\n");
            textView.invalidate();
            Log.e(TAG, msg);
        }
    }

    public int findMyContainerId() {

        int theId = -1;
        List<Integer> ids = KnoxContainerManager.getContainers();
        if (ids != null && !ids.isEmpty()) {
            theId = ids.get(0);
        }
        Log.d(TAG, "findMyContainerId: " + theId);
        return theId;
    }
}
