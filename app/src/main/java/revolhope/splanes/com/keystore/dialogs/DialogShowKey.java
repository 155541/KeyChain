package revolhope.splanes.com.keystore.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import revolhope.splanes.com.keystore.R;
import revolhope.splanes.com.keystore.model.Content;
import revolhope.splanes.com.keystore.views.UpdateKeyActivity;

/**
 * Created by splanes on 7/1/18.
 **/

public class DialogShowKey extends DialogFragment {

    private Content content;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_show_key,null,false);

        TextView name = view.findViewById(R.id.textView_idName);
        TextView user = view.findViewById(R.id.textView_user);
        TextView brief = view.findViewById(R.id.textView_brief);

        TextView key = view.findViewById(R.id.textView_key);
        Button copy = view.findViewById(R.id.btCopyKey);

        if(content != null){

            name.setText(content.getPlainName());
            user.setText(content.getPlainUser());
            brief.setText(content.getPlainBrief());
            key.setText(content.getPlainKey());

        }else{
            Toast.makeText(getContext(),"Ouch.. Some getting the key went wrong",Toast.LENGTH_LONG).show();
        }

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Content",content.getPlainKey());
                clipboardManager.setPrimaryClip(clip);
                Toast.makeText(getContext(),"Key copied to clipboard",Toast.LENGTH_SHORT).show();
            }
        });

        Spannable title = new SpannableString("Key Entry");
        title.setSpan(new ForegroundColorSpan(getContext().getColor(R.color.colorAccent)),0,title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        builder.setTitle(title)
                .setView(view)
                .setNegativeButton("update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(getContext(), UpdateKeyActivity.class);
                        intent.putExtra("oldContent",content);
                        startActivity(intent);
                    }
                });

        return builder.create();
    }

    public void setKeyData(Content content) { this.content = content; }
    public void setCallback(){} // todo: implement!
}
