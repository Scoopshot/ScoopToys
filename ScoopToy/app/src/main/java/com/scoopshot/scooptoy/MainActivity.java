package com.scoopshot.scooptoy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;

import com.scoopshot.sdk.CaptureScoopFragment;
import com.scoopshot.sdk.OpenTasksListFragment;
import com.scoopshot.sdk.SDKNotInitializedException;
import com.scoopshot.sdk.ScoopshotSDK;
import com.scoopshot.sdk.ScoopshotView;
import com.scoopshot.sdk.ScoopshotViewErrorHandler;
import com.scoopshot.sdk.ScoopshotViewLauncher;
import com.scoopshot.sdk.SendTaskNotificationToSelfFragment;

import java.util.Random;


public class MainActivity extends com.scoopshot.sdk.AbstractMainActivity
    implements ScoopshotViewErrorHandler
{
    private MainActivity me;

    private ViewGroup layout_root;
    private SendTaskNotificationToSelfFragment fNotif;
    private CaptureScoopFragment fScoop;
    private OpenTasksListFragment fTasks;

    private String email;
    private String name;

    private Random random = new Random();
    private final String chars = "abcdefghijklmnopqrstuvwxyz1234567890";

    public MainActivity() {
        setTag("ScoopToy.Main");
        setSharedPreferencesFilename("ScoopToy.sp");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        super.onCreate(savedInstanceState);

        me = this;
        final ScoopshotView ssv;
        final ScoopshotViewLauncher svl;

        setContentView(R.layout.activity_main);
        layout_root = (ViewGroup) findViewById(R.id.root);

        svl = ScoopshotSDK.getScoopshotViewLauncher(this, R.id.root);
        setScoopshotView(svl.getScoopshotView());

        /* hack, because at least email is needed so that we won't get "missing authentication field" errors */
//        email = "scooptester+be@gmail.com";
//        name = "ScoopTester Be";

        fNotif = (SendTaskNotificationToSelfFragment) getFragmentManager().findFragmentById(R.id.taskNotificationForm);

        fScoop = (CaptureScoopFragment) getFragmentManager().findFragmentById(R.id.capture_scoop);
//        fScoop.setDefaultActionOnCaptureButton();

        fTasks = (OpenTasksListFragment) getFragmentManager().findFragmentById(R.id.open_tasks);
        fTasks.setScoopshotViewContainerId(R.id.root);
        fTasks.setErrorHandler(this);

        ScoopshotSDK.setScoopshotViewParentActivity(this);
        ScoopshotSDK.setScoopshotViewContainerId(R.id.root);

        ScoopshotSDK.initialize(getApplicationContext());
        if (! ScoopshotSDK.hasAccessToken()) {
            fNotif.disable();
        }
//        ScoopshotSDK.setCameraComponentClass(MyCameraActivity.class);

        try {
            Log.i(TAG, "registered to GCM? " + String.valueOf(ScoopshotSDK.isRegisteredToGCM()));
        }
        catch (SDKNotInitializedException e) {
            e.printStackTrace();
        }
    }

    /*
     * Scoopshot SDK's AbstractMainActivity's onActivityResult is abstract so
     * trying to call it is futile.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ScoopshotSDK.actAccordingToActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
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

    private int getRandomNumber() {
        int randomInt = 0;
        randomInt = random.nextInt(chars.length());

        if (randomInt - 1 != -1) {
            randomInt -= 1;
        }

        return randomInt;
    }

    private String generateRandomString(int length) {
        StringBuffer randStr = new StringBuffer();
        char ch;
        int i = 0;

        for (; i < length; ++ i) {
            int number = getRandomNumber();
            ch = chars.charAt(number);
            randStr.append(ch);
        }

        return randStr.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleScoopshotViewError(int statusCode) {
        switch (statusCode) {
            case 401:
                /* accessToken is either null or expired
                 */
                showUnauthorizedRequestErrorDialog();
        }
    }
}
