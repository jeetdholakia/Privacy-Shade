package com.sand5.privacyscreen.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.firebase.storage.StorageReference;
import com.sand5.privacyscreen.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jeetdholakia on 4/28/17.
 */

public class VinylAdapter extends RecyclerView.Adapter<VinylAdapter.VinylViewHolder> {

    private static final String TAG = "VinylAdapter";
    public View.OnClickListener onClickListener;
    public OnItemClickListener mItemClickListener;
    private Context context;
    private ArrayList<String> urlList;
    private StorageReference storageReference;

    public VinylAdapter(Context context, ArrayList<String> urlList, StorageReference storageReference) {
        this.context = context;
        this.urlList = urlList;
        this.storageReference = storageReference;

    }

    @Override
    public int getItemCount() {
        return urlList.size();
    }

    @Override
    public void onBindViewHolder(final VinylViewHolder vinylViewHolder, int i) {

    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    public VinylViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.layout_adapter_vinyl, viewGroup, false);

        return new VinylViewHolder(itemView);
    }


    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    class VinylViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View view;
        @BindView(R.id.imageView_vinylAdapter)
        ImageView imageView;

        VinylViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            this.view = view;
            view.setOnClickListener(this);
        }

        void setOnClickListener(View.OnClickListener onClickListener) {
            view.setOnClickListener(onClickListener);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getPosition());
            }

        }
    }
}
