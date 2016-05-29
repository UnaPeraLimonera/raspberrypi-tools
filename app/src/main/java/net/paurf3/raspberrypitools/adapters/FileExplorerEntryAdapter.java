package net.paurf3.raspberrypitools.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jcraft.jsch.ChannelSftp;

import net.paurf3.raspberrypitools.R;

import java.util.List;

/**
 * Created by Pau on 17/02/2016.
 */
public class FileExplorerEntryAdapter extends RecyclerView.Adapter<FileExplorerEntryAdapter.EntriesViewHolder> {
    private List<ChannelSftp.LsEntry> lsEntries;
    private ClickListener clickListener;

    private int position;

    public FileExplorerEntryAdapter(List<ChannelSftp.LsEntry> lsEntries) {
        this.lsEntries = lsEntries;
    }


    @Override
    public EntriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View entrylist = inflater.inflate(R.layout.fragment_file_explorer_rv_entry, parent, false);

        EntriesViewHolder viewHolder = new EntriesViewHolder(entrylist);

        return viewHolder;

    }

    @Override
    public void onBindViewHolder(EntriesViewHolder holder, int position) {
        ChannelSftp.LsEntry entry = lsEntries.get(position);

        ImageView iv_Icon = holder.iv_FileExplorerEntryType;
        TextView tv_EntryName = holder.tv_FileExplorerEntryName;
        TextView tv_EntryFileSize = holder.tv_FileExplorerFileSize;


        //Si es un directorio, icono carpeta i no mostrar tamaño
        //Si no, icono archivo + nombre + tamaño
        if (entry.getAttrs().isDir()) {
            iv_Icon.setImageResource(R.drawable.ic_folder_grey600_48dp);
            tv_EntryName.setText(entry.getFilename());
            tv_EntryFileSize.setText("");
        } else {
            iv_Icon.setImageResource(R.drawable.ic_file_grey600_48dp);
            tv_EntryName.setText(entry.getFilename());
            tv_EntryFileSize.setText(Float.toString(entry.getAttrs().getSize() / 1024) + "KiB");
        }

    }


    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public int getPosition(){
        return position;
    }

    public void setPosition(int position){
        this.position = position;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    @Override
    public int getItemCount() {
        return lsEntries.size();
    }


    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    class EntriesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView iv_FileExplorerEntryType;
        public TextView tv_FileExplorerEntryName;
        public TextView tv_FileExplorerFileSize;


        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public EntriesViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            itemView.setOnClickListener(this);
//            itemView.setOnLongClickListener(this);
            iv_FileExplorerEntryType = (ImageView) itemView.findViewById(R.id.iv_file_explorer_entry_type);
            tv_FileExplorerEntryName = (TextView) itemView.findViewById(R.id.tv_file_explorer_entry_name);
            tv_FileExplorerFileSize = (TextView) itemView.findViewById(R.id.tv_file_explorer_file_size);


        }


        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.itemClicked(v, getAdapterPosition());
            }
        }



    }

    public interface ClickListener {
        public void itemClicked(View view, int position);
//        public void itemLongClicked(View view, int position);

    }


}
