package com.sand5.privacyscreen.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.orhanobut.logger.Logger;
import com.sand5.privacyscreen.PrivacyScreenApplication;
import com.sand5.privacyscreen.R;
import com.sand5.privacyscreen.adapters.VinylAdapter;
import com.sand5.privacyscreen.fragments.BackgroundFragment;
import com.sand5.privacyscreen.fragments.ColorFragment;
import com.sand5.privacyscreen.fragments.VinylFragment;
import com.sand5.privacyscreen.utils.Constants;
import com.sand5.privacyscreen.utils.ImageUrlHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VinylActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler, BackgroundFragment.OnFragmentInteractionListener, VinylFragment.OnFragmentInteractionListener, ColorFragment.OnFragmentInteractionListener {

    BillingProcessor bp;
    FirebaseStorage firebaseStorage;

    @BindView(R.id.test_imageView)
    ImageView testImageView;

    @BindView(R.id.login_button)
    LoginButton loginButton;

    @BindView(R.id.activity_vinyl_images_recyclerView)
    RecyclerView vinylImagesRecyclerView;

    private FirebaseAuth mAuth;
    private CallbackManager callbackManager;
    private String TAG = "VinylActivity";
    private AccessToken accessToken;
    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vinyl);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();
        preferences = PrivacyScreenApplication.getInstance().getSharedPreferences();
        bp = new BillingProcessor(this, Constants.billingID, this);
        firebaseStorage = FirebaseStorage.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loginButton.setVisibility(View.GONE);
            downloadVinyls();
        }

        callbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Logger.d("Successfully  logged in!");
                        accessToken = loginResult.getAccessToken();
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "Login Cancelled");
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        exception.printStackTrace();
                        Toast.makeText(VinylActivity.this, getResources().getString(R.string.error_facebook_login), Toast.LENGTH_SHORT).show();
                    }
                });

        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                accessToken = currentAccessToken;
            }
        };
        checkAccessTokenAndLogin();
    }


    private void downloadVinyls() {
        Logger.d("Downloading vinyls...");
        vinylImagesRecyclerView.setVisibility(View.VISIBLE);
        vinylImagesRecyclerView.setHasFixedSize(false);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        vinylImagesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        final ArrayList<String> urlList = ImageUrlHelper.getImageUrls();
        VinylAdapter vinylAdapter = new VinylAdapter(this, urlList, firebaseStorage);
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
    public void onBackPressed() {
        openMainActivity();
    }

    private void openMainActivity() {
        Intent mainActivityIntent = new Intent(VinylActivity.this, MainActivity.class);
        startActivity(mainActivityIntent);
        finish();
    }

    private void checkAccessTokenAndLogin() {
        accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            Logger.d("Access token is not null");
        } else {
            Logger.d("Access token is null,logging in again");
            LoginManager.getInstance().logInWithReadPermissions(VinylActivity.this, Arrays.asList("email", "public_profile"));
        }
    }


    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            loginButton.setVisibility(View.GONE);
                            downloadVinyls();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(VinylActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }

    // TODO: 5/2/17 Check file storage access permission here
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


    // IBillingHandler implementation
    @Override
    public void onBillingInitialized() {
        /*
         * Called when BillingProcessor was initialized and it's ready to purchase
		 */

        Logger.d("On billing initialized");
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        /*
         * Called when requested PRODUCT ID was successfully purchased
		 */
        Logger.d("On product purchased");
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
		/*
		 * Called when some error occurred. See Constants class for more details
		 *
		 * Note - this includes handling the case where the user canceled the buy dialog:
		 * errorCode = Constants.BILLING_RESPONSE_RESULT_USER_CANCELED
		 */
        Logger.d("On Billing error");
    }

    @Override
    public void onPurchaseHistoryRestored() {
		/*
		 * Called when purchase history was restored and the list of all owned PRODUCT ID's
		 * was loaded from Google Play
		 */
        Logger.d("On purchase history restored");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (bp.handleActivityResult(requestCode, resultCode, data)) {
            //Handle billing
            Logger.d("BillingHandler.OnActivityResult");
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
