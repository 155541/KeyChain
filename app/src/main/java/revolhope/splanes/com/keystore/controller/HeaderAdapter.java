package revolhope.splanes.com.keystore.controller;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import revolhope.splanes.com.keystore.R;
import revolhope.splanes.com.keystore.model.Content;
import revolhope.splanes.com.keystore.model.Folder;
import revolhope.splanes.com.keystore.model.Header;
import revolhope.splanes.com.keystore.model.interfaces.HeaderVisitor;
import revolhope.splanes.com.keystore.model.interfaces.IOnClickCallback;

/**
 * Created by splanes on 3/1/18.
 **/

public class HeaderAdapter extends RecyclerView.Adapter<HeaderAdapter.HeaderHolder> {

    private IOnClickCallback onClickCallback;
    private ArrayList<Header> dataSet;
    private Context context;

    public HeaderAdapter(Context context, IOnClickCallback onClickCallback) {
        this.context = context;
        this.onClickCallback = onClickCallback;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }


    @Override
    public HeaderAdapter.HeaderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HeaderHolder(LayoutInflater.from(context).inflate(R.layout.holder_header,parent,false));
    }


    @Override
    public void onBindViewHolder(HeaderAdapter.HeaderHolder holder, int position) {


        if(position >= dataSet.size()) return;
        Header header = dataSet.get(position);
        if(header == null) return;

        boolean isFolder = header.checkClass(new HeaderVisitor<Boolean>() {
            @Override
            public Boolean isFolder(Folder folder) {
                return Boolean.TRUE;
            }

            @Override
            public Boolean isContent(Content content) {
                return Boolean.FALSE;
            }
        });

        holder.textViewName.setText(header.getPlainName());
        holder.ivIcon.setBackground(
                isFolder ?
                        context.getDrawable(R.drawable.ic_folder_black_24dp) :
                        context.getDrawable(R.drawable.ic_vpn_key_black_24dp)
        );

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void setDataSet(ArrayList<Header> dataSet){
        if(dataSet == null) this.dataSet = new ArrayList<>();
        else this.dataSet = dataSet;
    }

    class HeaderHolder extends RecyclerView.ViewHolder{

        private TextView textViewName;
        private ImageView ivIcon;

        HeaderHolder (View view){
            super(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickCallback.onHeaderHolderClick(dataSet.get(getAdapterPosition()));
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    onClickCallback.onHeaderHolderLongClick(dataSet.get(getAdapterPosition()));
                    return true;
                }
            });

            this.textViewName = view.findViewById(R.id.textViewName);
            this.ivIcon = view.findViewById(R.id.ivIcon);
        }
    }
}
