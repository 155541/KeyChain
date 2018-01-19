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
import revolhope.splanes.com.keystore.model.Folder;
import revolhope.splanes.com.keystore.model.interfaces.IDialogCallbacks;

/**
 * Created by splanes on 8/1/18.
 **/

public class DialogRenameFolder extends DialogFragment {

    private IDialogCallbacks callback;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_new_dir,null,false);
        final EditText newName = view.findViewById(R.id.editTextFolderName);
        final int id = getArguments().getInt("id");
        final String oldName = getArguments().getString("oldName");
        final int idParent = getArguments().getInt("idParent");

        Spannable title = new SpannableString("Set new name for '" + oldName + "' folder");
        title.setSpan(new ForegroundColorSpan(getContext().getColor(R.color.colorAccent)),0,title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.setTitle(title)
                .setView(view)
                .setNeutralButton("dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .setPositiveButton("rename", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(callback != null && checkField(newName)){

                            Folder folder = new Folder();
                            folder.setParentId(idParent);
                            folder.setPlainName(newName.getText().toString());
                            folder.setId(id);
                            callback.onRenamedFolder(folder);
                        }
                    }
                });


        return builder.create();
    }

    private boolean checkField(EditText edit){
        boolean ok = true;
        if(edit == null) ok = false;
        else if(edit.getText().toString().isEmpty()) ok = false;
        return ok;
    }

    public void setCallback(IDialogCallbacks callback) { this.callback = callback; }
}
