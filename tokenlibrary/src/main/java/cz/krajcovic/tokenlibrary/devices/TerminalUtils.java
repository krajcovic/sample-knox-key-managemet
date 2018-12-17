package cz.krajcovic.tokenlibrary.devices;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import cz.monetplus.smartterminallibrary.tms.TmsParameters;
import cz.monetplus.smartterminallibrary.utils.ca.CaRecords;
import org.apache.commons.io.IOUtils;

import java.io.*;

public class TerminalUtils {
    private static final String TAG = TerminalUtils.class.getName();

    public static CaRecords loadCaRecords(File filesDir, String folderName) {
        CaRecords car = new CaRecords();
//        try (InputStream inputStream = new FileInputStream(new File(this.activity.getFilesDir() + "/" + APPLICATION_FOLDER_NAME, "CAKEYS"))) {
        try (InputStream inputStream = new FileInputStream(new File(filesDir + "/" + folderName, "CAKEYS"))) {
            car.load(IOUtils.toByteArray(inputStream));
//            car.debug();
        } catch (Exception e) {
            Log.e(TAG, "Cannot load CA records", e);
        }

        return car;
    }

    public static void copyAssetsParameters(Context context, String parent) {
        AssetManager assetManager = context.getAssets();
        String[] files = null;

        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e(TAG, "Failed to get asset file list.", e);
        }

        for(String filename : files) {

            switch (filename) {
                case "webkit":
                case "images":
                    continue;
            }

            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);

                File outFile = new File(parent, filename);
                outFile.getParentFile().mkdirs();

                out = new FileOutputStream(outFile);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch(IOException e) {
                Log.e(TAG, "Failed to copy asset file: " + filename, e);
            }
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    public static TmsParameters loadParameters(Context context, File parent, String applicationFolder) {

//        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
//        Log.i(TAG, defaultSharedPreferences.getAll().toString());

        InputStream parcrdStream = null;
        InputStream paruziStream = null;
        InputStream parsysStream = null;
        InputStream paremvStream = null;

        File fParcrd = new File(parent + "/" + applicationFolder, "PARCRD");
        File fParuzi = new File(parent + "/" + applicationFolder, "PARUZI");
        File fParsys = new File(parent + "/" + applicationFolder, "PARSYS");
        File fParemv = new File(parent + "/" + applicationFolder, "PAREMV");

        if(!fParcrd.exists() || !fParuzi.exists() || !fParsys.exists() || !fParemv.exists()) {
            copyAssetsParameters(context, parent + "/" + applicationFolder);
        }

        try {
            if (fParcrd.exists()) {
                parcrdStream = new FileInputStream(fParcrd);
            }

            if (fParuzi.exists()) {
                paruziStream = new FileInputStream(fParuzi);
            }

            if (fParsys.exists()) {
                parsysStream = new FileInputStream(fParsys);
            }

            if (fParemv.exists()) {
                paremvStream = new FileInputStream(fParemv);
            }

            return new TmsParameters(parcrdStream, paruziStream, parsysStream, paremvStream);
        } catch (Exception e) {
            Log.e(TAG, "Cannot load TMS parameters.", e);
        } finally {
            IOUtils.closeQuietly(parcrdStream);
            IOUtils.closeQuietly(paruziStream);
            IOUtils.closeQuietly(parsysStream);
            IOUtils.closeQuietly(paremvStream);
        }

        return null;
    }

}
