package com.busywww.myliveevent.lecshareClasses;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.busywww.myliveevent.AppStreaming;
import com.busywww.myliveevent.R;
import com.busywww.myliveevent.classes.EventsListFragment;
import com.busywww.myliveevent.classes.YouTubeApi;
import com.busywww.myliveevent.classes.YouTubeEventData;
import com.busywww.myliveevent.util.AdService;
import com.busywww.myliveevent.util.AppShared;
import com.busywww.myliveevent.util.Helper;
import com.busywww.myliveevent.util.LessonSingelton;
import com.busywww.myliveevent.util.NetworkSingleton;
import com.busywww.myliveevent.util.UtilNetwork;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.LiveStream;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class NewLiveLessonEvent extends AppCompatActivity implements
        EventsListFragment.Callbacks {

    private static Activity mActivity;
    private static Context mContext;
    private static View mRootView;
    private static AdService.BannerFragment adFragment;
    private static InterstitialAd fullscreenAd;

    public final static HttpTransport httpTransport = AppShared.AppHttpTransport;
    public final static JsonFactory jsonFactory = AppShared.AppJsonFactory;
    public static GoogleAccountCredential credential;

    private static String mUserAccountName;
    private static EventsListFragment mEventsListFragment;
    private TextView textViewAccountName;
    private static FloatingActionButton fab;
   // private static Spinner spinnerEventList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_app_splash);
        mActivity = this;
        mContext = this;
        AppShared.gContext = this;
        mRootView = getWindow().getDecorView();
        prepareApp(savedInstanceState);
        addNewEvent();


    }


    private void prepareApp(Bundle savedInstanceState) {
        try {

            mEventsListFragment = (EventsListFragment) getFragmentManager()
                    .findFragmentById(R.id.list_fragment);

            loadUserAccount(savedInstanceState);

            textViewAccountName = (TextView) findViewById(R.id.textViewAccountName);
            if (mUserAccountName == null || mUserAccountName.length() < 1) {
                textViewAccountName.setText("Select an account.");
            } else {
                textViewAccountName.setText(mUserAccountName);
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRestart() {
        loadEventList();
        super.onRestart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case AppShared.REQUEST_GMS_ERROR_DIALOG:
                break;
            case AppShared.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK) {
                    haveGooglePlayServices();
                } else {
                    checkGooglePlayServicesAvailable();
                }
                break;
            case AppShared.REQUEST_AUTHORIZATION:
                if (resultCode != Activity.RESULT_OK) {
                    selectAccount();
                }
                break;
            case AppShared.REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null
                        && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(
                            AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mUserAccountName = accountName;

                        credential.setSelectedAccountName(accountName);
                        Helper.SaveUserAccountName(mContext, accountName);
                        textViewAccountName.setText(accountName);

                        mEventsListFragment.RefreshAccount();
                    }
                }
                break;
            case AppShared.REQUEST_STREAMER:
                if (resultCode == Activity.RESULT_OK && data != null
                        && data.getExtras() != null) {
                }
                break;

        }
    }

    private void loadUserAccount(Bundle savedInstanceState) {
        try {

            credential = GoogleAccountCredential.usingOAuth2(
                    getApplicationContext(), Arrays.asList(AppShared.SCOPES));
            // set exponential backoff policy
            credential.setBackOff(new ExponentialBackOff());

            mUserAccountName = Helper.GetSavedUserAccountName(mContext);

            credential.setSelectedAccountName(mUserAccountName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     */
    private boolean checkGooglePlayServicesAvailable() {
        final int connectionStatusCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        }

        haveGooglePlayServices();

        return true;
    }

    public void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        runOnUiThread(new Runnable() {
            public void run() {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                        connectionStatusCode, mActivity,
                        AppShared.REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }
        });
    }

    private void haveGooglePlayServices() {
        if (credential == null || credential.getSelectedAccountName() == null) {
            credential = GoogleAccountCredential.usingOAuth2(
                    getApplicationContext(), Arrays.asList(AppShared.SCOPES));
            // set exponential backoff policy
            credential.setBackOff(new ExponentialBackOff());

            selectAccount();
        } else {
            loadUserAccount(null);
        }
    }

    private void selectAccount() {
        startActivityForResult(credential.newChooseAccountIntent(), AppShared.REQUEST_ACCOUNT_PICKER);
    }

    private static ImageLoader mImageLoader;
    @Override
    public ImageLoader onGetImageLoader() {
        if (mImageLoader == null) {
            // Get the ImageLoader through your singleton class.
            mImageLoader = NetworkSingleton.getInstance(this).getImageLoader();
        }
        return mImageLoader;
    }

    @Override
    public void onEventSelected(YouTubeEventData event) {
        try {

            AppShared.SelectedEvent = event;

                // event ready to streaming
                LiveStream stream = event.GetLiveStream();
                if (stream != null) {
                    String status = stream.getStatus().getStreamStatus();
                    if (status.toLowerCase().startsWith("ready")) {
                        AppShared.AdAction = AppShared.AdActionMain;
                    } else {
                        AppShared.AdAction = AppShared.AdActionView;
                    }
                } else {
                    AppShared.AdAction = AppShared.AdActionMain;
                }

            if (fullscreenAd != null && fullscreenAd.isLoaded()) {
                fullscreenAd.show();
            } else {
                ProcessUserAction(mActivity, mContext, AppShared.AdActionMain);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void DoEvent(YouTubeEventData event)
    {
        try {

            AppShared.SelectedEvent = event;

            // event ready to streaming
            LiveStream stream = event.GetLiveStream();
            if (stream != null) {
                String status = stream.getStatus().getStreamStatus();
                if (status.toLowerCase().startsWith("ready")) {
                    AppShared.AdAction = AppShared.AdActionMain;
                } else {
                    AppShared.AdAction =  AppShared.AdActionGoMain;
                }
            } else {
                AppShared.AdAction = AppShared.AdActionMain;
            }

            if (fullscreenAd != null && fullscreenAd.isLoaded()) {
                fullscreenAd.show();
            } else {
                ProcessUserAction(mActivity, mContext,  AppShared.AdAction );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    public void onConnected(String connectedAccountName) {
        loadEventList();
    }

    @Override
    public void onEventAddClicked() {
        addNewEvent();
    }

    @Override
    public void onEventEditClicked(YouTubeEventData event) {
        ActionEditTitle(event);
    }

    @Override
    public void onEventDeleteClicked(YouTubeEventData event) {
        deleteEvent(event);
    }

    @Override
    public void onEventShareClicked(YouTubeEventData event) {
        // Helper.ActionShareLiveEvent(mContext, event);
    }

    @Override
    public void onEventPrivacyClicked(YouTubeEventData event) {
        //updatePrivacy(event, newPrivacy);
        ActionEditPrivacy(event);
    }

    private static void showJsonErrorMessage(final String json) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    if (mEventsListFragment != null) {
                        mEventsListFragment.SetErrorMessage(jsonObject.getString("message"));
                    } else {
                        Snackbar.make(fab, jsonObject.getString("message"), Snackbar.LENGTH_SHORT).show();
                        //Toast.makeText(mContext, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private static void showJsonErrorMessage(final GoogleJsonError jsonError) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mEventsListFragment != null) {
                        mEventsListFragment.SetErrorMessage(jsonError.getMessage());
                    } else {
                        Snackbar.make(fab, jsonError.getMessage(), Snackbar.LENGTH_SHORT).show();
                        //Toast.makeText(mContext, jsonError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private static void loadEventList() {
        if (mUserAccountName == null) {
            return;
        }
        new GetLiveEventsTask().execute();
    }

    private static class GetLiveEventsTask extends
            AsyncTask<Void, Void, List<YouTubeEventData>> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(mActivity, null, "Loading...");
        }

        @Override
        protected List<YouTubeEventData> doInBackground(
                Void... params) {
            YouTube youtube = new YouTube.Builder(httpTransport, jsonFactory,
                    credential).setApplicationName(AppShared.APP_NAME)
                    .build();
            try {
                return YouTubeApi.GetLiveEvents(youtube);
            } catch (UserRecoverableAuthIOException e) {
                mActivity.startActivityForResult(e.getIntent(), AppShared.REQUEST_AUTHORIZATION);
            } catch (GoogleJsonResponseException e) {
                String message = e.getDetails().getMessage();
                showJsonErrorMessage(e.getDetails());
            } catch (IOException e) {
                Log.e(AppShared.APP_NAME, "", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<YouTubeEventData> fetchedEvents) {
            if (fetchedEvents == null) {

                if (YouTubeApi.YouTubeErrorMessage != null && YouTubeApi.YouTubeErrorMessage.length() > 0) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mEventsListFragment != null) {
                                mEventsListFragment.SetErrorMessage(YouTubeApi.YouTubeErrorMessage);
                            }
                        }
                    });
                } else {
                    if (mEventsListFragment != null) {
                        mEventsListFragment.SetErrorMessage("No live events found.");
                    }
                    //Toast.makeText(mContext, "Failed to list events, please try again.", Toast.LENGTH_SHORT).show();
                    Snackbar.make(fab, "Failed to list events, please try again.", Snackbar.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
                return;
            }

            mEventsListFragment.SetEvents(fetchedEvents);
            progressDialog.dismiss();
        }
    }

    private static void addNewEvent() {
        try {
            new CreateLiveEventTask().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static class CreateLiveEventTask extends
            AsyncTask<Void, Void, List<YouTubeEventData>> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(mActivity, null, "Creating new Live Lesson...");
        }

        @Override
        protected List<YouTubeEventData> doInBackground(Void... params) {
            YouTube youtube = new YouTube.Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName(AppShared.APP_NAME)
                    .build();
            try {
                String date = new Date().toString();
                boolean success = YouTubeApi.CreateLiveEvent(youtube, "Lesson - " + date,
                        "new lesson" + date);
                if (success) {
                    AppShared.EventStatusForList = "upcoming";

                    return YouTubeApi.GetLiveEvents(youtube);
                } else {
                    return null;
                }
            } catch (UserRecoverableAuthIOException e) {
                mActivity.startActivityForResult(e.getIntent(), AppShared.REQUEST_AUTHORIZATION);
            } catch (GoogleJsonResponseException e) {
                String message = e.getDetails().getMessage();
                showJsonErrorMessage(e.getDetails());
            } catch (Exception e) {
                Log.e(AppShared.APP_NAME, "", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<YouTubeEventData> eventDataList) {
            progressDialog.dismiss();

            if (eventDataList != null) {
                //mEventsListFragment.SetEvents(eventDataList);
                mEventsListFragment.SetLessonEvents(eventDataList);
                YouTubeEventData mLessonEvent = eventDataList.get(eventDataList.size()-1);
                DoEvent(mLessonEvent);

            } else {
                if (YouTubeApi.YouTubeErrorMessage != null && YouTubeApi.YouTubeErrorMessage.length() > 0) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mEventsListFragment != null) {
                                mEventsListFragment.SetErrorMessage(YouTubeApi.YouTubeErrorMessage);
                            }
                        }
                    });
                } else {
                    if (mEventsListFragment != null) {
                        mEventsListFragment.SetErrorMessage("No lessons found.");
                    }
                    //Toast.makeText(mContext, "Failed to create new event, please try again.", Toast.LENGTH_SHORT).show();
                    Snackbar.make(fab, "Failed to create new lesson, please try again.", Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

    private static void deleteEvent(YouTubeEventData eventData) {
        new DeleteLiveEventTask().execute(eventData);
    }
    private static class DeleteLiveEventTask extends
            AsyncTask<YouTubeEventData, Void, List<YouTubeEventData>> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(mActivity, null, "Deleting event...");
        }

        @Override
        protected List<YouTubeEventData> doInBackground(YouTubeEventData... params) {
            YouTube youtube = new YouTube.Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName(AppShared.APP_NAME)
                    .build();
            try {
                String date = new Date().toString();
                boolean success = YouTubeApi.DeleteEvent(youtube, params[0]);
                if (success) {
                    return YouTubeApi.GetLiveEvents(youtube);
                } else {
                    return null;
                }
            } catch (GoogleJsonResponseException e) {
                String message = e.getDetails().getMessage();
                showJsonErrorMessage(e.getDetails());
            } catch (Exception e) {
                Log.e(AppShared.APP_NAME, "", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<YouTubeEventData> eventDataList) {

            progressDialog.dismiss();

            if (eventDataList != null) {
                mEventsListFragment.SetEvents(eventDataList);
            } else {
                //Toast.makeText(mContext, "Failed to delete event, please try again.", Toast.LENGTH_SHORT).show();
                Snackbar.make(fab, "Failed to delete lesson, please try again.", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void updatePrivacy(YouTubeEventData eventData, String newPrivacy) {
        new UpdateLiveEventPrivacyStatusTask(eventData, newPrivacy).execute();
    }
    private static class UpdateLiveEventPrivacyStatusTask extends
            AsyncTask<Void, Void, List<YouTubeEventData>> {
        private ProgressDialog progressDialog;

        private YouTubeEventData mEventData;
        private String mPrivacy;

        UpdateLiveEventPrivacyStatusTask(YouTubeEventData eventData, String newPrivacy) {
            this.mEventData = eventData;
            this.mPrivacy = newPrivacy;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(mActivity, null, "Updating event...");
        }

        @Override
        protected List<YouTubeEventData> doInBackground(Void... params) {
            YouTube youtube = new YouTube.Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName(AppShared.APP_NAME)
                    .build();
            try {
                YouTubeApi.UpdatePrivacyStatus(youtube, mEventData, mPrivacy);
                return YouTubeApi.GetLiveEvents(youtube);
            } catch (UserRecoverableAuthIOException e) {
                mActivity.startActivityForResult(e.getIntent(), AppShared.REQUEST_AUTHORIZATION);
            } catch (GoogleJsonResponseException e) {
                String message = e.getDetails().getMessage();
                showJsonErrorMessage(e.getDetails());
            } catch (Exception e) {
                Log.e(AppShared.APP_NAME, "", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<YouTubeEventData> eventDataList) {

            progressDialog.dismiss();

            if (eventDataList != null) {
                mEventsListFragment.SetEvents(eventDataList);
            } else {
                if (YouTubeApi.YouTubeErrorMessage != null && YouTubeApi.YouTubeErrorMessage.length() > 0) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mEventsListFragment != null) {
                                mEventsListFragment.SetErrorMessage(YouTubeApi.YouTubeErrorMessage);
                            }
                        }
                    });
                } else {
                    if (mEventsListFragment != null) {
                        mEventsListFragment.SetErrorMessage("No live events found.");
                    }
                    //Toast.makeText(mContext, "Failed to create new event, please try again.", Toast.LENGTH_SHORT).show();
                    Snackbar.make(fab, "Failed to update event, please try again.", Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

    private static Dialog dialogEditPrivacy = null;
    private static String newPrivacy = "";
    private static void ActionEditPrivacy(final YouTubeEventData eventData) {
        try {

            if (dialogEditPrivacy != null) {
                dialogEditPrivacy.dismiss();
                dialogEditPrivacy = null;
                //return;
            }

            dialogEditPrivacy = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
            Window window = dialogEditPrivacy.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            //window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            window.setBackgroundDrawable(new BitmapDrawable());

            dialogEditPrivacy.setCancelable(true);
            dialogEditPrivacy.setCanceledOnTouchOutside(true);
            dialogEditPrivacy.setContentView(R.layout.dialog_privacy_edit);

            RadioGroup radioGroup = (RadioGroup) window.findViewById(R.id.radioGroupPrivacy);
            final RadioButton radioButtonUnlisted = (RadioButton) window.findViewById(R.id.radioButtonUnlisted);
            final RadioButton radioButtonPrivate = (RadioButton) window.findViewById(R.id.radioButtonPrivate);
            final RadioButton radioButtonPublic = (RadioButton) window.findViewById(R.id.radioButtonPublic);

            final String status = eventData.GetPrivacyStatus();
            // newPrivacy = status;
            newPrivacy = "unlisted";
            if (status.equals("unlisted")) {
                radioGroup.check(radioButtonUnlisted.getId());
            } else if (status.equals("private")) {
                radioGroup.check(radioButtonPrivate.getId());
            } else if (status.equals("public")) {
                radioGroup.check(radioButtonPublic.getId());
            }
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == radioButtonUnlisted.getId()) {
                        newPrivacy = "unlisted";
                    } else if (checkedId == radioButtonPrivate.getId()) {
                        newPrivacy = "private";
                    } else if (checkedId == radioButtonPublic.getId()) {
                        newPrivacy = "public";
                    }
                }
            });

            Button btnOk = (Button) dialogEditPrivacy.findViewById(R.id.btnUpdate);
            btnOk.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    dialogEditPrivacy.cancel();
                    dialogEditPrivacy.dismiss();
                    dialogEditPrivacy = null;

                    if (!status.equals(newPrivacy)) {
                        new UpdateLiveEventPrivacyStatusTask(eventData, newPrivacy).execute();
                    }
                }
            });


            if (!dialogEditPrivacy.isShowing()) {
                dialogEditPrivacy.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Dialog dialogEditTitle = null;
    private static String newTitle = "";
    private static void ActionEditTitle(final YouTubeEventData eventData) {
        try {

            if (dialogEditTitle != null) {
                dialogEditTitle.dismiss();
                dialogEditTitle = null;
                //return;
            }

            dialogEditTitle = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
            Window window = dialogEditTitle.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            //window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            window.setBackgroundDrawable(new BitmapDrawable());

            dialogEditTitle.setCancelable(true);
            dialogEditTitle.setCanceledOnTouchOutside(true);
            dialogEditTitle.setContentView(R.layout.dialog_title_edit);

            final EditText editText = (EditText) dialogEditTitle.findViewById(R.id.editTextTitle);
            final String title = eventData.GetTitle();
            newTitle = title;
            editText.setText(title);

            Button btnOk = (Button) dialogEditTitle.findViewById(R.id.btnUpdate);
            btnOk.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    dialogEditTitle.cancel();
                    dialogEditTitle.dismiss();
                    dialogEditTitle = null;

                    newTitle = editText.getText().toString();

                    if (!title.equals(newTitle)) {
                        new UpdateLiveEventTitleTask(eventData, newTitle).execute();
                    }
                }
            });


            if (!dialogEditTitle.isShowing()) {
                dialogEditTitle.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static class UpdateLiveEventTitleTask extends
            AsyncTask<Void, Void, List<YouTubeEventData>> {
        private ProgressDialog progressDialog;

        private YouTubeEventData mEventData;
        private String mTitle;

        UpdateLiveEventTitleTask(YouTubeEventData eventData, String newTitle) {
            this.mEventData = eventData;
            this.mTitle = newTitle;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(mActivity, null, "Updating event...");
        }

        @Override
        protected List<YouTubeEventData> doInBackground(Void... params) {
            YouTube youtube = new YouTube.Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName(AppShared.APP_NAME)
                    .build();
            try {
                YouTubeApi.UpdateTitle(youtube, mEventData, mTitle);
                return YouTubeApi.GetLiveEvents(youtube);
            } catch (UserRecoverableAuthIOException e) {
                mActivity.startActivityForResult(e.getIntent(), AppShared.REQUEST_AUTHORIZATION);
            } catch (GoogleJsonResponseException e) {
                String message = e.getDetails().getMessage();
                showJsonErrorMessage(e.getDetails());
            } catch (Exception e) {
                Log.e(AppShared.APP_NAME, "", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<YouTubeEventData> eventDataList) {

            progressDialog.dismiss();

            if (eventDataList != null) {
                mEventsListFragment.SetEvents(eventDataList);
            } else {
                if (YouTubeApi.YouTubeErrorMessage != null && YouTubeApi.YouTubeErrorMessage.length() > 0) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mEventsListFragment != null) {
                                mEventsListFragment.SetErrorMessage(YouTubeApi.YouTubeErrorMessage);
                            }
                        }
                    });
                } else {
                    if (mEventsListFragment != null) {
                        mEventsListFragment.SetErrorMessage("No live events found.");
                    }
                    //Toast.makeText(mContext, "Failed to create new event, please try again.", Toast.LENGTH_SHORT).show();
                    Snackbar.make(fab, "Failed to update event, please try again.", Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void loadAd() {
//        try {
//            AppShared.AdAction = AppShared.AdActionNone;
//
//          //  RelativeLayout layoutContainer = (RelativeLayout) findViewById(R.id.layoutAdContainer);
//
//            if (AppShared.ShowAdView) {
//
//               // Helper.InitMyAdView(mContext, layoutContainer);
//
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//
//                        if (UtilNetwork.IsOnline(mContext)) {
//                            try {
//                                adFragment = AdService.BannerFragment.newInstance();
//                                android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//                                ft.replace(R.id.layoutAdSplash, adFragment);
//                                ft.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_NONE);
//                                ft.commit();
//                            } catch (Exception eee) {
//                                eee.printStackTrace();
//                            }
//
//
//                            fullscreenAd = new InterstitialAd(mContext);
//                            //UtilGeneralHelper.LoadAd(adFragment, getSupportFragmentManager(), R.id.layoutAdSplash, null);
//                            fullscreenAd.setAdUnitId(AppShared.FullScreenAdId);
//                            fullscreenAd.setAdListener(new AdListener() {
//                                @Override
//                                public void onAdClosed() {
//                                    super.onAdClosed();
//
//                                    // check user action...
//                                    ProcessUserAction(mActivity, mContext, AppShared.AdAction);
//
//                                    requestFullscreenAd();
//                                }
//
//                                @Override
//                                public void onAdLoaded() {
//                                    super.onAdLoaded();
//                                }
//                            });
//
//                            requestFullscreenAd();
//
//                        }
//                    }
//                }, 50);
//            } else {
//               layoutContainer.setVisibility(View.GONE);
//            }
//
//        } catch (Exception e) {
//
//        }
   }
    private void requestFullscreenAd() {
        try {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//                        .addTestDevice("E8C1A939A52F387168C48CC03EAB8BAC")	// nexus 7
//                        .addTestDevice("1EC8F452AD1838A8F2DD2DF92A40C20B")	// nexus s
//                        .addTestDevice("9986B85AAD5AE0FA3DE84B361C5810EA")	// nexus 4
                    .build();

            fullscreenAd.loadAd(adRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void ProcessUserAction(Activity activity, Context context, int actionId) {
        try {
            Intent _intent;
            switch (actionId) {
//                case AppShared.AdActionNone:
//                    break;
                case AppShared.AdActionMain:
                    AppShared.AccountCredential = credential;
                    _intent = new Intent(mContext, AppStreaming.class);
                    _intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    mContext.startActivity(_intent);
                    break;
//                case AppShared.AdActionView:
//                    try{
//                        String id = AppShared.SelectedEvent.GetEvent().getId();
//                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
//                        mActivity.startActivity(intent);
//                    }catch (ActivityNotFoundException ex){
//                        String videoUrl = AppShared.SelectedEvent.GetWatchUri();
//                        mActivity.startActivity(
//                                new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
//                        );
//                    }
//                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void temp() {
        if (fullscreenAd != null && fullscreenAd.isLoaded()) {
            fullscreenAd.show();
        } else {
            ProcessUserAction(mActivity, mContext, AppShared.AdAction);
        }
    }
}

