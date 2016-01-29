package com.scoopshot.scooptoy;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.scoopshot.sdk.NoFragmentFoundException;
import com.scoopshot.sdk.SDKNotInitializedException;
import com.scoopshot.sdk.ScoopshotContentErrorHandler;
import com.scoopshot.sdk.ScoopshotSDK;
import com.scoopshot.sdk.UserExistsAlreadyException;
import com.scoopshot.sdk.UserNotAccessibleException;
import com.scoopshot.sdk.fragment.CaptureScoopFragment;
import com.scoopshot.sdk.fragment.OpenTasksListFragment;
import com.scoopshot.sdk.fragment.ScoopshotContentFragment;
import com.scoopshot.sdk.fragment.SendTaskNotificationToSelfFragment;
import com.scoopshot.sdk.fragment.UserDataFragment;


public class MainActivity
    extends com.scoopshot.sdk.activity.ScoopshotAbstractMainActivity
//    implements ScoopshotContentErrorHandler
{
    private MainActivity me;

    private ViewGroup layout_root;
    private SendTaskNotificationToSelfFragment fNotif;
    private CaptureScoopFragment fScoop;
    private OpenTasksListFragment fTasks;
    private UserDataFragment fUser;

    private String email;
    private String name;

    public MainActivity() {
        setTag("ScoopToy.Main");
        setSharedPreferencesFilename("ScoopToy.sp");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        requestWindowFeature(Window.FEATURE_ACTION_BAR);
//        requestWindowFeature(Window.FEATURE_CONTEXT_MENU);

        super.onCreate(savedInstanceState);

        me = this;

        setContentView(R.layout.activity_main);
        layout_root = (ViewGroup) findViewById(R.id.root);

        ScoopshotSDK.initialize(getApplicationContext());
        ScoopshotSDK.setScoopshotActivity(this);
        ScoopshotSDK.setScoopshotContainerId(R.id.root);
        ScoopshotSDK.setApplicationId(BuildConfig.scoopshotSdkApplicationId);
        ScoopshotSDK.setClientKey(BuildConfig.scoopshotSdkClientKey);

        fNotif = (SendTaskNotificationToSelfFragment) getFragmentManager().findFragmentById(R.id.taskNotificationForm);

        try {
            ScoopshotSDK.createUser("", "", new Runnable() {
                @Override
                public void run() {
                    if (ScoopshotSDK.hasAccessToken()) {
                        fNotif.enable();
                    }
                }
            }, null);
        }
        catch (UserExistsAlreadyException e) {
            e.printStackTrace();
        }
        catch (SDKNotInitializedException e) {
            e.printStackTrace();
        }


        /* hack, because at least email is needed so that we won't get "missing authentication field" errors */
//        email = "scooptester+be@gmail.com";
//        name = "ScoopTester Be";

        fUser = (UserDataFragment) getFragmentManager().findFragmentById(R.id.userData);

        fTasks = (OpenTasksListFragment) getFragmentManager().findFragmentById(R.id.open_tasks);
//        fTasks.setScoopshotContentContainerId(R.id.root);
//        fTasks.setErrorHandler(this);
        fTasks.setArguments(R.id.root, this);

        fScoop = (CaptureScoopFragment) getFragmentManager().findFragmentById(R.id.capture_scoop);
        fScoop.setArguments(R.id.root);
//        fScoop.setDefaultActionOnCaptureButton();

        if (! ScoopshotSDK.hasAccessToken()) {
            fNotif.disable();
        }
//        ScoopshotSDK.setCameraComponentClass(MyCameraActivity.class);

        try {
            ScoopshotSDK.registerToGCM();
        }
        catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }

//        Boolean registeredToC2DM = null;
//        try {
//            registeredToC2DM = ScoopshotSDK.isRegisteredToGCM();
//        }
//        catch (SDKNotInitializedException e) {
//            e.printStackTrace();
//        }
//
//        Log.i(TAG, "registered to GCM? " + String.valueOf(registeredToC2DM));
    }

    /*
     * ScoopshotAbstractMainActivity#onActivityResult() is abstract so
     * trying to call it is futile.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        _onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        final Intent intent;

        if (ScoopshotSDK.hasAccessToken()) {
            fNotif.enable();
        }

        intent = getIntent();

        if (intent.hasExtra("consumed") && intent.getBooleanExtra("consumed", false) == false) {
            ScoopshotSDK.actAccordingToIntentOnResume(this, intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        final int finishAt = 1;
        final int bsec;
        final FragmentManager fm;

        Log.i(TAG, "MainActivity#onBackPressed()");

        fm = this.getFragmentManager();
        bsec = fm.getBackStackEntryCount();

        if (bsec > finishAt) {
            try {
                goBack();
                fm.popBackStack();
            }
            catch (NoFragmentFoundException e) {
                e.printStackTrace();
                fm.popBackStack();
            }
        }
        else if (bsec == finishAt) {
            fm.popBackStack();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else {
            /* when bsec == 0 */
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void openSettings() {
        try {
            ScoopshotContentFragment.getInstance(this, this, R.id.root).openSettings();
        } catch (UserNotAccessibleException e) {
            e.printStackTrace(); /* shouldn't happen */
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if (ScoopshotSDK.hasAccessToken()) {
                openSettings();
            }
            else {
//                Toast.makeText(this, "You are not registered yet -- do it by opening the Tasks list or capture a Scoop", Toast.LENGTH_LONG).show();
                Toast.makeText(this, "Registering you as a new user .. try again in a couple seconds", Toast.LENGTH_LONG).show();
                try {
                    ScoopshotSDK.createUser(fUser.getEmail(), fUser.getName());
                }
                catch (UserExistsAlreadyException e) {
                    e.printStackTrace();
                }
                catch (SDKNotInitializedException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleScoopshotContentError(int statusCode, String message) {
        switch (statusCode) {
            case 401:
                /* accessToken is either null or expired
                 */
                showUnauthorizedRequestErrorDialog(ScoopshotContentFragment.getInstance());
        }
    }
}
