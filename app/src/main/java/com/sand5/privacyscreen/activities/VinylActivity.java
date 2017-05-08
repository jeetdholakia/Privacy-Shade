package com.sand5.privacyscreen.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.orhanobut.logger.Logger;
import com.sand5.privacyscreen.PrivacyScreenApplication;
import com.sand5.privacyscreen.R;
import com.sand5.privacyscreen.fragments.BackgroundFragment;
import com.sand5.privacyscreen.fragments.ColorFragment;
import com.sand5.privacyscreen.fragments.VinylFragment;
import com.sand5.privacyscreen.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VinylActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler, BackgroundFragment.OnFragmentInteractionListener, VinylFragment.OnFragmentInteractionListener, ColorFragment.OnFragmentInteractionListener {

    private final int PERMISSION_WRITE_STORAGE = 501;
    BillingProcessor bp;
    FirebaseStorage firebaseStorage;
    @BindView(R.id.login_button)
    LoginButton loginButton;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.viewpager)
    ViewPager viewPager;
    private FirebaseAuth mAuth;
    private CallbackManager callbackManager;
    private String TAG = "VinylActivity";
    private AccessToken accessToken;
    private SharedPreferences preferences;
    private int[] tabIcons = {
            R.drawable.ic_landscape_white_24dp,
            R.drawable.ic_text_background_24dp,
    };

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
            setupViewPager(viewPager);
            tabLayout.setupWithViewPager(viewPager);
            setupTabIcons();
            //checkPermissionAndDownloadVinyl();
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
                            setupViewPager(viewPager);
                            tabLayout.setupWithViewPager(viewPager);
                            setupTabIcons();
                            //checkPermissionAndDownloadVinyl();
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

    @AfterPermissionGranted(PERMISSION_WRITE_STORAGE)
    private void checkPermissionAndDownloadVinyl() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            //downloadVinyls();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.permission_write_storage),
                    PERMISSION_WRITE_STORAGE, perms);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new BackgroundFragment(), "Black");
        adapter.addFragment(new BackgroundFragment(), "Inspiration");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        private final List<String> mFragmentTitleList = new ArrayList<>();

        private ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        private void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }
}
