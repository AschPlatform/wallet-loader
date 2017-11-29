package so.asch.wallet.loader;

import java.util.ResourceBundle;

public class LanguageContant {

    public static final String KEY_UPDATEBOX_LOADING_WALLET ;
    public static final String KEY_UPDATEBOX_STARTING_WALLET ;
    public static final String KEY_UPDATEBOX_ALERT_ERROR ;
    public static final String KEY_UPDATEBOX_DIALOG_START_FAIL ;
    public static final String KEY_UPDATEBOX_PROGRESS_DOWNLOADING ;
    public static final String KEY_UPDATEBOX_PROGRESS_DOWNLOADED ;
    public static final String KEY_UPDATEBOX_PROGRESS_CHECKING_UPDATE ;
    public static final String KEY_UPDATEBOX_PROGRESS_APPLAYING_UPDATE ;
    public static final String KEY_UPDATEBOX_PROGRESS_NEED_NOT ;
    public static final String KEY_UPDATEBOX_PROGRESS_DOWNLOAD_UPDATE_FILE_FAIL ;
    public static final String KEY_UPDATEBOX_PROGRESS_APPLY_UPDATE_FAIL ;

    static {
        ResourceBundle bundle = ResourceBundle.getBundle("lang");
        KEY_UPDATEBOX_LOADING_WALLET = emptyIfNull(bundle.getString("KEY_UPDATEBOX_LOADING_WALLET = "));
        KEY_UPDATEBOX_STARTING_WALLET = emptyIfNull(bundle.getString("KEY_UPDATEBOX_STARTING_WALLET = "));
        KEY_UPDATEBOX_ALERT_ERROR = emptyIfNull(bundle.getString("KEY_UPDATEBOX_ALERT_ERROR = "));
        KEY_UPDATEBOX_DIALOG_START_FAIL = emptyIfNull(bundle.getString("KEY_UPDATEBOX_DIALOG_START_FAIL = "));
        KEY_UPDATEBOX_PROGRESS_DOWNLOADING = emptyIfNull(bundle.getString("KEY_UPDATEBOX_PROGRESS_DOWNLOADING = "));
        KEY_UPDATEBOX_PROGRESS_DOWNLOADED = emptyIfNull(bundle.getString("KEY_UPDATEBOX_PROGRESS_DOWNLOADED = "));
        KEY_UPDATEBOX_PROGRESS_CHECKING_UPDATE = emptyIfNull(bundle.getString("KEY_UPDATEBOX_PROGRESS_CHECKING_UPDATE = "));
        KEY_UPDATEBOX_PROGRESS_APPLAYING_UPDATE = emptyIfNull(bundle.getString("KEY_UPDATEBOX_PROGRESS_APPLYING_UPDATE = "));
        KEY_UPDATEBOX_PROGRESS_NEED_NOT = emptyIfNull(bundle.getString("KEY_UPDATEBOX_PROGRESS_NEED_NOT = "));
        KEY_UPDATEBOX_PROGRESS_DOWNLOAD_UPDATE_FILE_FAIL = emptyIfNull(bundle.getString("KEY_UPDATEBOX_PROGRESS_DOWNLOAD_UPDATE_FILE_FAIL = "));
        KEY_UPDATEBOX_PROGRESS_APPLY_UPDATE_FAIL = emptyIfNull(bundle.getString("KEY_UPDATEBOX_PROGRESS_APPLY_UPDATE_FAIL = "));
    }

    private static String emptyIfNull(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
}
