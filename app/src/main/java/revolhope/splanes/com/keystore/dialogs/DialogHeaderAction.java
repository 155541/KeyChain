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
import android.text.style.StyleSpan;

import revolhope.splanes.com.keystore.R;
import revolhope.splanes.com.keystore.model.interfaces.IDialogCallbacks;

/**
 * Created by splanes on 4/1/18.
 **/

public class DialogHeaderAction extends DialogFragment{

    private IDialogCallbacks callback;
    private static final String INFO =
            "\nDrop information:\n"
            + "If you drop a folder, all containing sub folder will be also dropped\n";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        
        final int id = getArguments().getInt("id");
        final String name = getArguments().getString("name");
        final boolean isFolder = getArguments().getBoolean("isFolder");

        Spannable title = new SpannableString("Choose an action for " + name);
        Spannable spanMessage = new SpannableString(INFO);

        title.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.colorAccent, null)), 0, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanMessage.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),0,17,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        builder.setTitle(title)
                .setNeutralButton("drop", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        callback.onDropHeaderWrapper();
                    }
                })
                .setNegativeButton("move", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DialogMoveElement dialogMoveElement = new DialogMoveElement();
                        dialogMoveElement.setArguments(getArguments());
                        dialogMoveElement.setCallback(callback);
                        dialogMoveElement.show(getFragmentManager(),"MoveDialog");

                    }
                });

        if(isFolder) {
            builder.setMessage(spanMessage)
                    .setPositiveButton("rename", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            callback.onRenameFolder(id, name);
                        }
                    });
        }

        return builder.create();
    }

    public void setCallback(IDialogCallbacks callback){ this.callback = callback; }
}