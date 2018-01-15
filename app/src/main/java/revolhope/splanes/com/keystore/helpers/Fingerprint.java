package revolhope.splanes.com.keystore.helpers;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.os.CancellationSignal;
import android.widget.Toast;

import revolhope.splanes.com.keystore.views.StoreActivity;

/**
 * Created by splanes on 2/1/18.
 **/

public class Fingerprint extends FingerprintManager.AuthenticationCallback {

    private CancellationSignal cancellationSignal;
    private Context context;

    public Fingerprint(Context context) {
        this.context = context;
    }

    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {

        cancellationSignal = new CancellationSignal();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context,"No permission for Fingerprint. It is needed!", Toast.LENGTH_LONG).show();
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        Toast.makeText(context, "Authentication error\n" + errString, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        Toast.makeText(context,helpString, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
        }else{
            vibrator.vibrate(300);
        }
        Firebase firebase = new Firebase(context);
        firebase.signIn();
        Intent intent = new Intent(context, StoreActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show();
    }

}
