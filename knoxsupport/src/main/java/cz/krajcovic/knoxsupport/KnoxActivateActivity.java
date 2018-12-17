package cz.krajcovic.knoxsupport;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.samsung.android.knox.EnterpriseKnoxManager;
import com.samsung.android.knox.container.KnoxContainerManager;
import com.samsung.android.knox.license.KnoxEnterpriseLicenseManager;
import com.samsung.android.knox.restriction.RestrictionPolicy;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;

import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME;

public class KnoxActivateActivity extends AppCompatActivity {

    private static final String TAG = KnoxActivateActivity.class.getName();
    private static final int REQUEST_CODE_ACTIVATE_SELECT_LICENSE_FILE = 100;
    private static final int REQUEST_CODE_ACTIVATE_SELECT_LICENSE_STRING = 101;
    private static final int REQUEST_CODE_DEACTIVATE_SELECT_LICENSE_FILE = 102;

    private KnoxManager knoxManager = new KnoxManager();

    private Utils mUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_knox_activate);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView LogView = (TextView) findViewById(R.id.logview_id);
        LogView.setMovementMethod(new ScrollingMovementMethod());
        mUtils = new Utils(LogView, TAG);

        // Check if device supports Knox SDK
        mUtils.checkApiLevel(24, this);

        Button CreateAndroidProfile = (Button) findViewById(R.id.CreateAndroidProfilebtn);
        CreateAndroidProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAndroidProfile();
            }
        });
        Button ActivateLicenceFilebtn = (Button) findViewById(R.id.ActivateLicenceFilebtn);
        ActivateLicenceFilebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activateLicenceFile();
            }
        });

        Button ActivateLicenceStringbtn = (Button) findViewById(R.id.ActivateLicenceStringbtn);
        ActivateLicenceStringbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activateLicenceString();
            }
        });

        Button DeActivateLicencebtn = (Button) findViewById(R.id.DeActivateLicencebtn);
        DeActivateLicencebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deActivateLicence();
            }
        });
//        Button ToggleCamerabtn = (Button) findViewById(R.id.ToggleCamerabtn);
//        ToggleCamerabtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                toggleCameraState();
//            }
//        });

        if (knoxManager.isProfileOnwner(getApplicationContext())) {
//            ToggleCamerabtn.setEnabled(true);
            CreateAndroidProfile.setEnabled(false);
        } else {
            ActivateLicenceFilebtn.setEnabled(false);
            DeActivateLicencebtn.setEnabled(false);
//            ToggleCamerabtn.setEnabled(false);
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    // Provision an Android Profile Owner
    private void createAndroidProfile() {

        Activity provisioningActivity = this;
        // Set up the provisioning intent
        Intent provisioningIntent = new Intent("android.app.action.PROVISION_MANAGED_PROFILE");
        String packageName = getPackageName();

        provisioningIntent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME, packageName);
        //provisioningIntent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,getPackageName());
        if (provisioningIntent.resolveActivity(provisioningActivity.getPackageManager()) == null) {
            // No handler for intent! Can't provision this device.
            // Show an error message and cancel.
        } else {
            // REQUEST_PROVISION_MANAGED_PROFILE is defined
            startActivityForResult(provisioningIntent, 1);
            provisioningActivity.finish();
        }
    }

    /**
     * Note that embedding your license key in code is unsafe and is done here for
     * demonstration purposes only.
     * Please visit https://seap.samsung.com/license-keys/about. for more details about license
     * keys.
     */
    private void activateLicenceFile() {

        Intent intent = new Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select a file"), REQUEST_CODE_ACTIVATE_SELECT_LICENSE_FILE);
    }

    private void activateLicenceString() {

        Intent intent = new Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select a file"), REQUEST_CODE_ACTIVATE_SELECT_LICENSE_STRING);
    }

    private void deActivateLicence() {

        Intent intent = new Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select a file"), REQUEST_CODE_DEACTIVATE_SELECT_LICENSE_FILE);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ACTIVATE_SELECT_LICENSE_FILE && resultCode == RESULT_OK) {
            licenseManagerProgress(data, new LicenseOperation() {
                public void run(KnoxEnterpriseLicenseManager licenseManager, String key) {
                    licenseManager.activateLicense(key/*, getPackageName()*/);
                }
            });
        }

        if (requestCode == REQUEST_CODE_DEACTIVATE_SELECT_LICENSE_FILE && resultCode == RESULT_OK) {

            licenseManagerProgress(data, new LicenseOperation() {
                public void run(KnoxEnterpriseLicenseManager licenseManager, String key) {
                    licenseManager.deActivateLicense(key/*, getPackageName()*/);
                }
            });
        }
    }

    public String getPath(Uri uri) {

        String path = null;
        String[] projection = { MediaStore.Files.FileColumns.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if(cursor == null){
            path = uri.getPath();
        }
        else{
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndexOrThrow(projection[0]);
            path = cursor.getString(column_index);
            cursor.close();
        }

        return ((path == null || path.isEmpty()) ? (uri.getPath()) : path);
    }

   private void licenseManagerProgress(Intent data, LicenseOperation licenseOperation) {
        Uri selectedfile = data.getData(); //The uri with the location of the file
        //String filePath = selectedfile.getPath();

        //filePath = getPath(selectedfile);

        // Instantiate the EnterpriseLicenseManager class to use the activateLicense method
        KnoxEnterpriseLicenseManager licenseManager = KnoxEnterpriseLicenseManager.getInstance(this);


        try {
            if (selectedfile != null) {
                //String licenseKey = IOUtils.toString(new FileInputStream(filePath), "UTF-8");
                String licenseKey = IOUtils.toString(getContentResolver().openInputStream(selectedfile), "UTF-8");
                if (licenseKey != null) {
                    //licenseManager.activateLicense(Constants.LICENSE_KEY);
                    //licenseManager.activateLicense(licenseKey);
                    licenseOperation.run(licenseManager, licenseKey);
                    mUtils.log(getResources().getString(R.string.license_progress));
                } else {
                    Log.w(TAG, "URI of licenseKey cannot be converted.");
                }
            } else {
                Log.w(TAG, "Uri of license file is null");
            }

        } catch (Exception e) {
            mUtils.processException(e, TAG);
        }
    }

    /**
     * Toggle the restriction of the profile owner camera on/off. When set to disabled, the end
     * user will
     * be unable to use the device cameras.
     */
    private void toggleCameraState() {

        EnterpriseKnoxManager ekm = EnterpriseKnoxManager.getInstance(this);
        KnoxContainerManager kcm = ekm.getKnoxContainerManager(mUtils.findMyContainerId());
        RestrictionPolicy restrictionPolicy = kcm.getRestrictionPolicy();
        boolean cameraEnabled = restrictionPolicy.isCameraEnabled(false);
        try {
            // Disable camera. Other applications that use the camera cannot
            // use it.
            boolean result = restrictionPolicy.setCameraState(!cameraEnabled);

            if (result) {
                mUtils.log(getResources().getString(R.string.camera_state, !cameraEnabled));
            }
        } catch (Exception e) {
            mUtils.processException(e, TAG);
        }
    }

}
