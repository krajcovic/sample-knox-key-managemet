package cz.krajcovic.knoxsupport;

import com.samsung.android.knox.license.KnoxEnterpriseLicenseManager;

public interface LicenseOperation {

    void run(KnoxEnterpriseLicenseManager licenseManager, String licenseKey);
}
