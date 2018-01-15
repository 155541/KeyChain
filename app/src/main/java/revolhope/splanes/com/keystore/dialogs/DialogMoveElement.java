package revolhope.splanes.com.keystore.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import revolhope.splanes.com.keystore.R;
import revolhope.splanes.com.keystore.helpers.Database;
import revolhope.splanes.com.keystore.model.HeaderWrapper;
import revolhope.splanes.com.keystore.model.interfaces.IDialogCallbacks;

/**
 * Created by splanes on 8/1/18.
 **/

public class DialogMoveElement extends DialogFragment {

    private Database db;
    private int idShowing;
    private int idClicked;
    private IDialogCallbacks callback;
    private boolean isFolder;
    private Button btBack;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        db = new Database(getContext());
        idShowing = 0;

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_move_key,null,false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        final MoveKeyAdapter moveKeyAdapter = new MoveKeyAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(moveKeyAdapter);

        btBack = view.findViewById(R.id.btBackFolder);
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(idShowing != 0) {
                    // getting parent's parent
                    HeaderWrapper showing = db.getHeader(idShowing);

                    moveKeyAdapter.generateDataSet(showing.getParentId());
                    idShowing = showing.getParentId();
                }
            }
        });



        HeaderWrapper hClicked = db.getHeader(idClicked);
        String title;
        if(hClicked != null) title = "Move '" + hClicked.getName() + "' to:";
        else title = "Move to:";
        Spannable span = new SpannableString(title);
        span.setSpan(new ForegroundColorSpan(getContext().getColor(R.color.colorAccent)),0,span.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setTitle(span);
        builder.setView(view)
                .setPositiveButton("move", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if(isFolder)
                            db.updateFolderParent(idClicked,idShowing);
                        else
                            db.updateKeyParent(idClicked,idShowing);
                        callback.onMovedElement();
                    }
                });


        return builder.create();
    }

    public void setCallback(IDialogCallbacks callback) { this.callback = callback; }

    public void setIdClicked(int idClicked) { this.idClicked = idClicked; }

    public void setIsFolder(boolean isFolder) { this.isFolder = isFolder; }

    private class MoveKeyAdapter extends RecyclerView.Adapter<MoveKeyAdapter.Holder>{

        private ArrayList<JSONObject> folders;

        MoveKeyAdapter(){
            generateDataSet(0);
        }

        private void generateDataSet(int parent){

            folders = new ArrayList<>();
            for(HeaderWrapper header : db.getHeaders(parent)){
                if(header.isFolder()){

                    JSONObject json = new JSONObject();
                    try{
                        json.put("name",header.getName());
                        json.put("id",header.getId());
                        folders.add(json);

                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public long getItemId(int position) {
            if(position < folders.size()) {
                try {
                    return folders.get(position).getInt("id");
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
            return position;
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(getContext()).inflate(R.layout.holder_move_key,parent,false));
        }

        @Override
        public void onBindViewHolder(MoveKeyAdapter.Holder holder, int position) {
            if(position < folders.size()) {

                JSONObject json = folders.get(position);
                try{

                    holder.name.setText(json.getString("name"));
                    holder.id = json.getInt("id");

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }

        @Override
        public int getItemCount() {
            return folders.size();
        }

        class Holder extends RecyclerView.ViewHolder{

            private TextView name;
            private int id;

            Holder(View item){
                super(item);

                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        generateDataSet(id);
                        idShowing = id;
                        btBack.setEnabled(idShowing!=0);
                    }
                });
                name = item.findViewById(R.id.textView_idName);
            }
        }
    }

}
