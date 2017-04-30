package com.sand5.privacyscreen.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.orhanobut.logger.Logger;
import com.sand5.privacyscreen.R;
import com.sand5.privacyscreen.utils.Constants;
import com.sand5.privacyscreen.utils.ImageUrlHelper;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VinylActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    BillingProcessor bp;
    FirebaseStorage storage;
    StorageReference storageRef;
    @BindView(R.id.test_imageView)
    ImageView testImageView;
    @BindView(R.id.login_button)
    LoginButton loginButton;
    private FirebaseAuth mAuth;
    private CallbackManager callbackManager;
    private String TAG = "VinylActivity";
    private AccessToken accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vinyl);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();
        bp = new BillingProcessor(this, Constants.billingID, this);
        storage = FirebaseStorage.getInstance();
        //backgroundsRef = storageRef.child("backgrounds");
        //imageRef = backgroundsRef.child("Image01.jpg");
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
        ArrayList<String> urlList = ImageUrlHelper.getImageUrls();
        storageRef = storage.getReferenceFromUrl(urlList.get(0));

        final long ONE_MEGABYTE = 1024 * 1024;
        storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Logger.d("Download successful");
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
                testImageView.setImageBitmap(mutableBitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Logger.e("Download not successful");
            }
        });
    }

    private void checkAccessTokenAndLogin() {
        accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            Logger.d("Access token is not null");
            //getLoggedPerson();
            //sendLoginCredentialsToDatabase(accessToken);

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
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(VinylActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
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
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
}
