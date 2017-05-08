package com.sand5.privacyscreen.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.orhanobut.logger.Logger;
import com.sand5.privacyscreen.PrivacyScreenApplication;
import com.sand5.privacyscreen.R;
import com.sand5.privacyscreen.activities.MainActivity;
import com.sand5.privacyscreen.adapters.VinylAdapter;
import com.sand5.privacyscreen.utils.ImageUrlHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class BackgroundFragment extends Fragment {

    private final int PERMISSION_WRITE_STORAGE = 501;
    @BindView(R.id.activity_vinyl_images_recyclerView)
    RecyclerView vinylImagesRecyclerView;
    Unbinder unbinder;
    FirebaseStorage firebaseStorage;
    private FirebaseAuth mAuth;
    private CallbackManager callbackManager;
    private String TAG = "VinylActivity";
    private AccessToken accessToken;
    private SharedPreferences preferences;
    private OnFragmentInteractionListener mListener;

    public BackgroundFragment() {
        // Required empty public constructor
    }


    public static BackgroundFragment newInstance(String param1, String param2) {
        BackgroundFragment fragment = new BackgroundFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PrivacyScreenApplication.getInstance().getSharedPreferences();
        firebaseStorage = FirebaseStorage.getInstance();

    }

    private void openMainActivity() {
        Intent mainActivityIntent = new Intent(getActivity(), MainActivity.class);
        startActivity(mainActivityIntent);
        getActivity().finish();
    }

    private void downloadVinylToFile(String url) {

        StorageReference storageReference = firebaseStorage.getReferenceFromUrl(url);
        try {
            File rootPath = new File(Environment.getExternalStorageDirectory(), "Privacy Shade Wallpapers");
            if (!rootPath.exists()) {
                rootPath.mkdirs();
            }
            final File localFile = new File(rootPath, "wallpaper.jpg");
            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Logger.d("Success downloading file");
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    if (localFile.exists()) localFile.delete();
                    try {
                        FileOutputStream out = new FileOutputStream(localFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.flush();
                        out.close();
                        Logger.d("Success saving file");
                        openMainActivity();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Logger.d("Failure downloading File");
                }
            });
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void downloadVinyls() {
        Logger.d("Downloading vinyls...");
        vinylImagesRecyclerView.setVisibility(View.VISIBLE);
        vinylImagesRecyclerView.setHasFixedSize(false);
        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        vinylImagesRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        final ArrayList<String> urlList = ImageUrlHelper.getImageUrls();
        VinylAdapter vinylAdapter = new VinylAdapter(getActivity(), urlList, firebaseStorage);
        vinylImagesRecyclerView.setAdapter(vinylAdapter);
        vinylAdapter.setOnItemClickListener(new VinylAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Logger.d("Clicked");
                preferences.edit().putString("background_type", "wallpaper").apply();
                preferences.edit().putString("wallpaper_url", urlList.get(position)).apply();
                downloadVinylToFile(urlList.get(position));

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_background, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        downloadVinyls();
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private void loginWithFacebook() {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
