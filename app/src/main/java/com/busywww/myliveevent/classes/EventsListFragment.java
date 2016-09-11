package com.busywww.myliveevent.classes;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.busywww.myliveevent.R;
import com.busywww.myliveevent.util.CircleImage;
import com.busywww.myliveevent.util.Helper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.PlusOneButton;
import com.google.android.gms.plus.model.people.Person;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class EventsListFragment extends Fragment implements
        ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = EventsListFragment.class.getName();
    private Callbacks mCallbacks;
    private ImageLoader mImageLoader;
    private static GoogleApiClient mGoogleApiClient;
    private static GridView mGridView;
    private static View mContainerView;
    private static TextView mEmptyView;
    //private static String[] mPrivacyList = { "unlisted", "private", "public"};
    private static List<YouTubeEventData> mEvents;
    private ViewHolder mHolder;

    public static void SetLessonEvents(List<YouTubeEventData> events){mEvents = events;}

    public EventsListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Connect();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContainerView = inflater.inflate(R.layout.fragment_event_list, container,
                false);
        mGridView = (GridView) mContainerView.findViewById(R.id.grid_view);
        mEmptyView = (TextView) mContainerView.findViewById(R.id.empty);
        mEmptyView.setText("No live events found.");
        mGridView.setEmptyView(mEmptyView);

        FloatingActionButton fabAddEvent = (FloatingActionButton) mContainerView.findViewById(R.id.fabNewEvent);

        //Button startNewLiveLesson = (Button)mContainerView.findViewById(R.id.);
        fabAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallbacks.onEventAddClicked();
            }
        });

        return mContainerView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SetProfileInfo();
    }

    public void RefreshAccount() {
        Disconnect();
        Connect();
    }


    public void Disconnect() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
        }
        try {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
            mGoogleApiClient.disconnect();
        } catch (Exception e) {
            mGoogleApiClient.disconnect();
        }
    }

    public void Connect() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void SetEvents(List<YouTubeEventData> events) {
        if (!isAdded()) {
            return;
        }

        mGridView.setAdapter(new LiveEventAdapter(events));
    }


    public void SetErrorMessage(String message) {

        if (mGridView != null) {
            mGridView.setAdapter(null);
        }

        mEmptyView.setText(message);
        mGridView.setEmptyView(mEmptyView);
    }

    public void SetProfileInfo() {
        final ImageView avatar = (ImageView) getView().findViewById(R.id.avatar);
        if (!mGoogleApiClient.isConnected()
                || Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) == null) {
            avatar.setImageDrawable(null);
            ((TextView) getView().findViewById(R.id.display_name))
                    .setText("Sign in account");
        } else {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            if (currentPerson.hasImage()) {

                mImageLoader.get(currentPerson.getImage().getUrl(), new ImageLoader.ImageListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //setImageBitmap(new CircleImage(getApplicationContext()).transform(your_image_bitmap));
                        //
                        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_user_acount);
                        RoundedBitmapDrawable drawable = Helper.GetRoundBitmapDrawable(getResources(), icon);
                        ((ImageView) getView().findViewById(R.id.avatar))
                                .setImageDrawable(drawable);
//                                .setImageBitmap(Helper.GetCircleImage(
//                                        getActivity().getApplicationContext(),
//                                        icon,
//                                        getResources().getColor(R.color.bw00_60)));
                    }

                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                        if (response.getBitmap() != null) {
                            // load image into imageview
                            RoundedBitmapDrawable drawable = Helper.GetRoundBitmapDrawable(getResources(), response.getBitmap());
                            ((ImageView) getView().findViewById(R.id.avatar))
                                    .setImageDrawable(drawable);

                        }
                    }
                });


            }
            if (currentPerson.hasDisplayName()) {
                ((TextView) getView().findViewById(R.id.display_name))
                        .setText(currentPerson.getDisplayName());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        //mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (mGridView.getAdapter() != null) {
            ((LiveEventAdapter) mGridView.getAdapter())
                    .notifyDataSetChanged();
        }

       // SetProfileInfo();
        mCallbacks.onConnected(Plus.AccountApi.getAccountName(mGoogleApiClient));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            Toast.makeText(getActivity(),
                    "Connection to Play Services failed.",
                    Toast.LENGTH_SHORT).show();

            Log.e(TAG,
                    String.format(
                            "Connection to Play Services Failed, error: %d, reason: %s",
                            connectionResult.getErrorCode(),
                            connectionResult.toString()));
            try {
                connectionResult.startResolutionForResult(getActivity(), 0);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, e.toString(), e);
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new ClassCastException("Activity must implement callbacks.");
        }

        mCallbacks = (Callbacks) activity;
        mImageLoader = mCallbacks.onGetImageLoader();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
        mImageLoader = null;
    }

    public interface Callbacks {
        public ImageLoader onGetImageLoader();

        public void onEventSelected(YouTubeEventData event);
        public void onConnected(String connectedAccountName);
        public void onEventAddClicked();
        public void onEventEditClicked(YouTubeEventData event);
        public void onEventDeleteClicked(YouTubeEventData event);
        public void onEventShareClicked(YouTubeEventData event);
        public void onEventPrivacyClicked(YouTubeEventData event);
    }


    private static boolean mSkipChangeEvent = true;

    public class ViewHolder {
        public LinearLayout LayoutEvent;
        public NetworkImageView Thumbnail;
        public TextView Title;
        public ImageView EditButton;
        //public Spinner PrivacySpinner;
        public ImageView Broadcast;
        public ImageView Stream;
        public ImageView DeleteButton;
        public ImageView ShareButton;
        public ImageView PrivacyImage;
        public ImageView StartButton;
        public int Position;
    }

    private class LiveEventAdapter extends BaseAdapter {
        private List<YouTubeEventData> mEvents;
        private ViewHolder mHolder;

        private LiveEventAdapter(List<YouTubeEventData> events) {
            mEvents = events;
        }

        @Override
        public int getCount() {
            return mEvents.size();
        }

        @Override
        public Object getItem(int i) {
            return mEvents.get(i);
        }

        @Override
        public long getItemId(int i) {
            return mEvents.get(i).GetId().hashCode();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup container) {
            View v = convertView;

            mSkipChangeEvent = true;

            if (v == null) {

                v = LayoutInflater.from(getActivity()).inflate(
                        R.layout.fragment_event_list_item, container, false);
                mHolder = new ViewHolder();
                mHolder.Position = position;

                mHolder.Title = (TextView) v.findViewById(android.R.id.text1);
                mHolder.Thumbnail = (NetworkImageView) v.findViewById(R.id.thumbnail);
                mHolder.LayoutEvent = (LinearLayout) v.findViewById(R.id.layoutEvent);
                mHolder.DeleteButton = (ImageView) v.findViewById(R.id.imageViewDeleteEvent);
                mHolder.EditButton = (ImageView) v.findViewById(R.id.imageViewEditEvent);
                mHolder.ShareButton = (ImageView) v.findViewById(R.id.imageViewShareEvent);
                //mHolder.PrivacySpinner = (Spinner)v.findViewById(R.id.spinnerPrivacy);

                mHolder.Broadcast = (ImageView) v.findViewById(R.id.imageViewBroadcast);
                mHolder.Stream = (ImageView) v.findViewById(R.id.imageViewStream);
                mHolder.PrivacyImage = (ImageView) v.findViewById(R.id.imageViewPrivacy);

                mHolder.StartButton = (ImageView) v.findViewById(R.id.imageViewStart);

                v.setTag(mHolder);
            } else {
                mHolder = (ViewHolder)v.getTag();
                //mHolder.Position = position;
            }

            mHolder.Title.setText(mEvents.get(position).GetTitle());
            mHolder.Thumbnail.setImageUrl(mEvents.get(position).GetThumbUri(), mImageLoader);
            mHolder.Broadcast.setImageDrawable(mEvents.get(position).GetBroadcastRecordingStatusDrawable(getResources()));
            mHolder.Stream.setImageDrawable(mEvents.get(position).GetStreamStatusDrawable(getResources()));
            mHolder.PrivacyImage.setImageDrawable(mEvents.get(position).GetPrivacyDrawable(getResources()));

            mHolder.LayoutEvent.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mCallbacks.onEventSelected(mEvents.get(position));
                        }
                    }
            );

            mHolder.DeleteButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mCallbacks.onEventDeleteClicked(mEvents.get(position));
                        }
                    }
            );
            mHolder.EditButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mCallbacks.onEventEditClicked(mEvents.get(position));
                        }
                    }
            );
            mHolder.ShareButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mCallbacks.onEventShareClicked(mEvents.get(position));
                        }
                    }
            );
            mHolder.PrivacyImage.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mCallbacks.onEventPrivacyClicked(mEvents.get(position));
                        }
                    }
            );


            if (mGoogleApiClient.isConnected()) {
//                ((PlusOneButton) convertView.findViewById(R.id.plus_button))
//                        .initialize(event.GetWatchUri(), null);
            }

            return v;
        }
    }
//    public class PrivacyAdapter extends ArrayAdapter {
//        private String[] items;
//
//        public PrivacyAdapter(Context context, int itemLayoutId, int textViewResourceId, String[] objects) {
//            super(context, itemLayoutId, textViewResourceId, objects);
//            items = objects;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            View v = convertView;
//            if (v == null) {
//                LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                v = inflater.inflate(android.R.layout.simple_spinner_item, null);
//                String item = items[position];
//                TextView textView = (TextView) v.findViewById(android.R.id.text1);
//                textView.setText(item);
//            }
//            return v;
//        }
//
//    }

}
