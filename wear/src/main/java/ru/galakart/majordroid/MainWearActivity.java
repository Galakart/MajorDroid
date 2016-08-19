package ru.galakart.majordroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MainWearActivity extends Activity implements WearableListView.ClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, MessageApi.MessageListener {


    private static final int SPEECH_REQUEST_CODE = 0;
    private static final int GOOGLE_API_RESOLVE = 1;

    private static final String TAG = "MainWearActivity";
    private BoxInsetLayout mContainerView;
    private WearableListView listView;
    private ProgressBar progressBar;
    private View retryView;

    private GoogleApiClient googleApiClient;

    private Node mNode; // the connected device to send the message to

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);


        // Get the list component from the layout of the activity
        listView = (WearableListView) findViewById(R.id.wearable_list);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        retryView = findViewById(R.id.retryView);

        // Assign an adapter to the list
        //listView.setAdapter(new Adapter(this, elements));

        // Set a click listener
        listView.setClickListener(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Wearable.MessageApi.removeListener(googleApiClient, this);
        googleApiClient.disconnect();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            final String spokenText = results.get(0);

            if (spokenText != null && spokenText.length() > 0) {
                if (googleApiClient.isConnected()) {
                    Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                        @Override
                        public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodes) {
                            for (Node node : nodes.getNodes()) {
                                Log.d("TAG", node.getDisplayName() + "|" + node.getId() + "|" + node.isNearby());
                                Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), "/sendCommand", spokenText.getBytes()).setResultCallback(resultCallback);
                            }
                        }
                    });
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(final WearableListView.ViewHolder viewHolder) {
        if (googleApiClient.isConnected()) {
            System.out.println(viewHolder.getItemId());
            Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodes) {
                    for (Node node : nodes.getNodes()) {
                        Log.d("TAG", node.getDisplayName() + "|" + node.getId() + "|" + node.isNearby());
                        Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), "/sendClick", new byte[]{(byte) viewHolder.getItemId()}).setResultCallback(resultCallback);
                    }
                }
            });
        }
    }

    @Override
    public void onTopEmptyRegionClick() {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "GoogleAPI Connected");
        Wearable.MessageApi.addListener(googleApiClient, this);
        onRetryClick(null);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "GoogleAPI onConnectionSuspended:" + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "GoogleAPI onConnectionFailed:" + connectionResult);
        String errorMessage = connectionResult.getErrorMessage();
        if (errorMessage != null && errorMessage.length() > 0) Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();

        if (connectionResult.getResolution() != null) {
            try {
                connectionResult.startResolutionForResult(this, GOOGLE_API_RESOLVE);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived " + messageEvent);
        progressBar.setVisibility(View.GONE);
        switch (messageEvent.getPath()) {
            case "/jsonResult":
                String json = new String(messageEvent.getData());
                if (json.length() > 0) {
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        JSONArray jsonArray = jsonObject.getJSONArray("items");
                        MenuBean[] dataset = new MenuBean[jsonArray.length()];
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonItem = jsonArray.getJSONObject(i);
                            MenuBean menuBean = new MenuBean();
                            menuBean.setId(jsonItem.getInt("ID"));
                            menuBean.setTitle(jsonItem.getString("TITLE"));
                            menuBean.setSubtitle(jsonItem.getString("SUBTITLE"));
                            dataset[i] = menuBean;
                        }

                        listView.setAdapter(new Adapter(this, dataset));
                        listView.setVisibility(View.VISIBLE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        retryView.setVisibility(View.VISIBLE);
                    }

                } else {
                    retryView.setVisibility(View.VISIBLE);
                }
                break;


            //{"RESULT":"OK"}
            case "/commandResult":
                String jsonResponse = new String(messageEvent.getData());
                String serverResponse = "No server response";
                boolean success = false;
                if (jsonResponse.length() > 0) {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        serverResponse = jsonObject.getString("RESULT");
                        success = "OK".equalsIgnoreCase(jsonObject.getString("RESULT"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (success) {
                    Intent intent = new Intent(MainWearActivity.this, ConfirmationActivity.class);
                    intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainWearActivity.this, ConfirmationActivity.class);
                    intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION);
                    intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, serverResponse);
                    startActivity(intent);
                }
                break;
        }
    }

    private ResultCallback<MessageApi.SendMessageResult> resultCallbackOnLoad = new ResultCallback<MessageApi.SendMessageResult>() {

        @Override
        public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
            Log.d(TAG, "SendMessageResult:" + sendMessageResult.getStatus());
            if (!sendMessageResult.getStatus().isSuccess()) {
                Log.d(TAG, "Failed to send message with status code: " + sendMessageResult.getStatus().getStatusCode());
//                Toast.makeText(MainWearActivity.this, WearableStatusCodes.getStatusCodeString(sendMessageResult.getStatus().getStatusCode()), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainWearActivity.this, ConfirmationActivity.class);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION);
                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Not connected to device");
                startActivity(intent);
                retryView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                listView.setVisibility(View.INVISIBLE);
            }
        }
    };

    private ResultCallback<MessageApi.SendMessageResult> resultCallback = new ResultCallback<MessageApi.SendMessageResult>() {

        @Override
        public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
            Log.d(TAG, "SendMessageResult:" + sendMessageResult.getStatus());
            if (!sendMessageResult.getStatus().isSuccess()) {
                Log.d(TAG, "Failed to send message with status code: " + sendMessageResult.getStatus().getStatusCode());
//                Toast.makeText(MainWearActivity.this, WearableStatusCodes.getStatusCodeString(sendMessageResult.getStatus().getStatusCode()), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainWearActivity.this, ConfirmationActivity.class);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION);
                intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Not connected to device");
                startActivity(intent);
            }else{
                progressBar.setVisibility(View.VISIBLE);
            }
        }
    };

    public void onRetryClick(View view) {
        retryView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        if (googleApiClient.isConnected()) {
            Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodes) {
                    if (!nodes.getStatus().isSuccess() || nodes.getNodes() == null || nodes.getNodes().size() == 0) {
                        Intent intent = new Intent(MainWearActivity.this, ConfirmationActivity.class);
                        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION);
                        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Not connected to device");
                        startActivity(intent);
                        retryView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    } else {
                        for (Node node : nodes.getNodes()) {
                            Log.d("TAG", node.getDisplayName() + "|" + node.getId() + "|" + node.isNearby());
                            Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), "/getJSON", null).setResultCallback(resultCallbackOnLoad);
                        }
                    }
                }
            });
        } else {
            retryView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    public void onSpeakClick(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Произнесите команду");
// Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    private static final class Adapter extends WearableListView.Adapter {
        private MenuBean[] mDataset;
        private final Context mContext;
        private final LayoutInflater mInflater;

        // Provide a suitable constructor (depends on the kind of dataset)
        public Adapter(Context context, MenuBean[] dataset) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mDataset = dataset;
            setHasStableIds(true);
        }

        // Provide a reference to the type of views you're using
        public static class ItemViewHolder extends WearableListView.ViewHolder {
            private TextView name;
            private TextView description;

            public ItemViewHolder(View itemView) {
                super(itemView);
                // find the text view within the custom item's layout
                name = (TextView) itemView.findViewById(R.id.name);
                description = (TextView) itemView.findViewById(R.id.description);
            }
        }

        // Create new views for list items
        // (invoked by the WearableListView's layout manager)
        @Override
        public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Inflate our custom layout for list items
            return new ItemViewHolder(mInflater.inflate(R.layout.list_item, parent, false));
        }

        @Override
        public long getItemId(int position) {
            return mDataset[position].getId();
        }


        // Replace the contents of a list item
        // Instead of creating new views, the list tries to recycle existing ones
        // (invoked by the WearableListView's layout manager)
        @Override
        public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
            // retrieve the text view
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            // replace text contents
            MenuBean menuBean = mDataset[position];
            itemHolder.name.setText(menuBean.getTitle());
            itemHolder.description.setText(menuBean.getSubtitle());
            // replace list item's metadata
            holder.itemView.setTag(position);
        }

        // Return the size of your dataset
        // (invoked by the WearableListView's layout manager)
        @Override
        public int getItemCount() {
            return mDataset.length;
        }
    }


}
