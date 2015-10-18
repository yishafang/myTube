package com.example.yishafang.mytube;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.ResourceId;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import static com.example.yishafang.mytube.Constants.ACCESS_TOKEN;


/**
 * @author yishafang on 10/15/15.
 */
public class SearchFragment extends android.support.v4.app.Fragment{
    public static final String TAG = SearchFragment.class.getSimpleName();

    private String FAVORITE_LIST_TITLE = "SJSU-CMPE-277";
    // TODO shouldn't be hard coded!!
    private String FAVORITE_LIST_ID = "PLhaxd1bMGz7tmUURj5z2n25-gvERQOCLP";

    private static String accessToken;

    private EditText searchInput;
    private ListView videosFound;

    private Handler handler;

    private List<VideoItem> searchResults;

    public static SearchFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(TAG, page);
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
//        accessToken = sharedPref.getString(ACCESS_TOKEN, null);
        Intent intent = getActivity().getIntent();
        accessToken = intent.getStringExtra(ACCESS_TOKEN);

        Log.d(TAG, accessToken);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Set up search part
        searchInput = (EditText) view.findViewById(R.id.search_input);
        videosFound = (ListView) view.findViewById(R.id.videos_found);

        handler = new Handler();

        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    searchOnYoutube(v.getText().toString());
                    return false;
                }
                return true;
            }
        });

        return view;
    }

    private void searchOnYoutube(final String keywords) {
        new Thread() {
            public void run() {
                YoutubeConnector yc = new YoutubeConnector(getActivity());
                searchResults = yc.search(keywords);
                handler.post(new Runnable() {
                    public void run() {
                        updateVideosFound();
                    }
                });
            }
        }.start();
    }

    private void updateVideosFound() {
        ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getActivity().getApplicationContext(), R.layout.video_item, searchResults) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.video_item, parent, false);
                }

                final ImageView thumbnail = (ImageView) convertView.findViewById(R.id.video_thumbnail);
                thumbnail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity().getApplicationContext(), PlayerActivity.class);
                        intent.putExtra("VIDEO_ID", searchResults.get(position).getId());
                        startActivity(intent);
                    }
                });

                TextView title = (TextView) convertView.findViewById(R.id.video_title);
                TextView description = (TextView) convertView.findViewById(R.id.video_description);
                TextView publishAt = (TextView) convertView.findViewById(R.id.video_published);
                TextView viewsCount = (TextView) convertView.findViewById(R.id.video_views);

                final CheckBox checkbox = (CheckBox) convertView.findViewById(R.id.favorite);

                final VideoItem searchResult = searchResults.get(position);

                Picasso.with(getActivity().getApplicationContext()).load(searchResult.getThumbnailURL()).into(thumbnail);
                title.setText(searchResult.getTitle());
                description.setText(searchResult.getDescription());
                publishAt.setText(searchResult.getPublishedAt().toString().substring(0, 10));
                viewsCount.setText(searchResult.getViewsCount().toString());

                // set up check box
                if (searchResult.getFavorite()) {
                    checkbox.isChecked();
                }

                checkbox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (checkbox.isChecked()) {
                            searchResult.setFavorite(checkbox.isChecked());
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Video is added into your playlist.", Toast.LENGTH_LONG);
                            toast.show();
                            insertToPlaylist(FAVORITE_LIST_ID, searchResult.getId());
                            checkbox.setVisibility(View.INVISIBLE);
                        }
                    }
                });


                return convertView;
            }
        };

        videosFound.setAdapter(adapter);
    }

    private void insertToPlaylist(final String playlistId, final String videoId) {
        new Thread() {
            public void run() {
                GoogleCredential credential = new GoogleCredential.Builder().setTransport(new NetHttpTransport())
                        .setJsonFactory(new JacksonFactory()).build();
                credential.setAccessToken(accessToken);

                // This OAuth 2.0 access scope allows for full read/write access to the
                // authenticated user's account and requires requests to use an SSL connection.
//                ArrayList<String> scopes = new ArrayList<String>();
//                scopes.add("https://www.googleapis.com/auth/youtube.force-ssl");
//
//                GoogleAccountCredential credential1 = GoogleAccountCredential.usingOAuth2(
//                        getActivity().getApplicationContext(), scopes);

                // This object is used to make YouTube Data API requests.
                YouTube youtube;

                youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential)
                        .setApplicationName("MyTube")
                        .build();

                //Define a resourceId that identifies the video being added to the playlist
                ResourceId resourceId = new ResourceId();
                resourceId.setKind("youtube#video");
                resourceId.setVideoId(videoId);

                // Set fields included in the playlistItem resource's "snippet" part.
                PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
                playlistItemSnippet.setTitle(FAVORITE_LIST_TITLE);
                playlistItemSnippet.setPlaylistId(playlistId);
                playlistItemSnippet.setResourceId(resourceId);

                // Create the playlistItem resource and set its snippet to object created above.
                PlaylistItem playlistItem = new PlaylistItem();
                playlistItem.setSnippet(playlistItemSnippet);

                // Call the API to add the video to the specified playlist.
                try {

                    Log.d(TAG, "Video need to insert to playlist ");

                    YouTube.PlaylistItems.Insert InsertRequest =
                            youtube.playlistItems().insert("snippet,contentDetails", playlistItem);

                    PlaylistItem responses =
                            InsertRequest.execute();
                    Log.d(TAG, "Video is added to playlist ");

                } catch (IOException e) {
                    Log.d(TAG, "Could not add video to playlist: " + e);
                }

            }
        }.start();
    }
}
