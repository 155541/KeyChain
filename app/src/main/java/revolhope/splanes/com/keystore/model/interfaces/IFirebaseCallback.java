package revolhope.splanes.com.keystore.model.interfaces;

import java.util.ArrayList;

import revolhope.splanes.com.keystore.model.Header;

/**
 * Created by splanes on 12/1/18.
 **/

public interface IFirebaseCallback {
    void onSignIn(boolean result);
    void onSynchronized(Boolean sync);
}
