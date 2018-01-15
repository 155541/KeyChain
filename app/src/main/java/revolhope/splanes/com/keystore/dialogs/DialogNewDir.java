package revolhope.splanes.com.keystore.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import revolhope.splanes.com.keystore.R;
import revolhope.splanes.com.keystore.model.interfaces.IDialogCallbacks;

/**
 * Created by splanes on 3/1/18.
 **/

public class DialogNewDir extends DialogFragment {

    private IDialogCallbacks callback;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_new_dir,null,false);
        final EditText folderName = view.findViewById(R.id.editTextFolderName);

        Spannable title = new SpannableString("Set new folder's name");
        title.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.colorAccent,null)),0,title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.setTitle(title)
                .setView(view)
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton("create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(checkName(folderName.getText().toString())){
                            callback.onCreateFolder(folderName.getText().toString());
                        }
                    }
                });

        return builder.create();
    }

    private boolean checkName(String name){

        boolean ok = true;

        if(name == null)
            ok = false;
        else if(name.trim().equals(""))
            ok = false;
        else if(name.length() > 50)
            ok = false;
        return ok;
    }

    public void setCallback(IDialogCallbacks callback){ this.callback = callback; }
}
