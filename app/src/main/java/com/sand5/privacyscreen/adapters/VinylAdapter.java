package com.sand5.privacyscreen.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.orhanobut.logger.Logger;
import com.sand5.privacyscreen.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jeetdholakia on 4/28/17.
 */

public class VinylAdapter extends RecyclerView.Adapter<VinylAdapter.VinylViewHolder> {

    private static final String TAG = "VinylAdapter";
    private View.OnClickListener onClickListener;
    private OnItemClickListener mItemClickListener;
    private Context context;
    private ArrayList<String> urlList;
    private FirebaseStorage firebaseStorage;


    public VinylAdapter(Context context, ArrayList<String> urlList, FirebaseStorage firebaseStorage) {
        this.context = context;
        this.urlList = urlList;
        this.firebaseStorage = firebaseStorage;
    }

    @Override
    public int getItemCount() {
        return urlList.size();
    }

    @Override
    public void onBindViewHolder(final VinylViewHolder vinylViewHolder, int i) {

        StorageReference storageReference = firebaseStorage.getReferenceFromUrl(urlList.get(i));
        final long ONE_MEGABYTE = 1024 * 1024;
        storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Logger.d("Download successful");
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
                vinylViewHolder.vinylImageView.setImageBitmap(mutableBitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Logger.e("Download not successful");
            }
        });
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
        ImageView vinylImageView;

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
