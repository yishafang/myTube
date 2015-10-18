package com.example.yishafang.mytube;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.Video;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.yishafang.mytube.Constants.ACCESS_TOKEN;

/**
 * @author yishafang on 10/15/15.
 */
public class FavoriteListFragment extends Fragment {
    public static final String TAG = "FAVORITE";

    private static String accessToken;

    private String FAVORITE_LIST_TITLE = "SJSU-CMPE-277";
    // TODO shouldn't be hard coded!!
    private String FAVORITE_LIST_ID = "PLhaxd1bMGz7tmUURj5z2n25-gvERQOCLP";

    private Handler handler;

    private ListView favoriteList;
    private List<VideoItem> videoItems = new ArrayList<>();

    public static FavoriteListFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(TAG, page);
        FavoriteListFragment favoriteListFragment = new FavoriteListFragment();
        favoriteListFragment.setArguments(args);
        return favoriteListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        // Get access token
        Intent intent = getActivity().getIntent();
        accessToken = intent.getStringExtra(ACCESS_TOKEN);
        Log.d(TAG, accessToken);

        handler = new Handler();
        favoriteList = (ListView) view.findViewById(R.id.favorite_found);

        getFavoriteList(FAVORITE_LIST_ID);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_favorite_list, menu);

    }

    private void getFavoriteList(final String playListId) {
        new Thread() {
            public void run() {
                GoogleCredential credential = new GoogleCredential.Builder().setTransport(new NetHttpTransport())
                        .setJsonFactory(new JacksonFactory()).build();
                credential.setAccessToken(accessToken);

                YouTube youTube;
                youTube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential)
                        .setApplicationName("MyTube")
                        .build();

                try {
                    Log.d(TAG, "Trying to get favorite list.");

                    List<PlaylistItem> playlistItemList = new ArrayList<>();

                    YouTube.PlaylistItems.List listRequest = youTube.playlistItems().list("id,contentDetails,snippet");
                    listRequest.setPlaylistId(playListId);
                    listRequest.setFields("items(contentDetails/videoId,snippet/title,snippet/publishedAt,snippet/description,snippet/thumbnails/default/url),nextPageToken,pageInfo");

                    String nextPageToken = "";

//                    do {
                        listRequest.setPageToken(nextPageToken);

                        PlaylistItemListResponse response = listRequest.execute();
                        playlistItemList.addAll(response.getItems());

                        for (PlaylistItem item : playlistItemList) {
                            VideoItem videoItem = new VideoItem();
                            videoItem.setTitle(item.getSnippet().getTitle());
                            videoItem.setDescription(item.getSnippet().getDescription());
                            videoItem.setPublishedAt(item.getSnippet().getPublishedAt());
                            videoItem.setThumbnailURL(item.getSnippet().getThumbnails().getDefault().getUrl());

                            String videoId = item.getContentDetails().getVideoId();
                            videoItem.setId(videoId);

                            // get number of views
                            YoutubeConnector youtubeConnector = new YoutubeConnector(getActivity());
                            try {
                                YouTube.Videos.List videoListQuery = youtubeConnector.getYoutube().videos().list("snippet, statistics").setId(videoId);
                                videoListQuery.setKey(youtubeConnector.getDeveloperKey());
                                Video video = videoListQuery.execute().getItems().get(0);
                                videoItem.setViewsCount(video.getStatistics().getViewCount());
                            } catch (IOException e) {
                                Log.d(TAG, "Could not get number of views: " + e);
                            }

                            videoItems.add(videoItem);

                        }

                    //} while (nextPageToken != null);


                } catch (IOException e) {
                    Log.d(TAG, "Could not get favorite list: " + e);
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateFavoriteList(videoItems);

                    }
                });

            }
        }.start();
    }

    private void updateFavoriteList(final List<VideoItem> videoItems) {
        ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getActivity().getApplicationContext(), R.layout.video_item, videoItems) {
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
                        intent.putExtra("VIDEO_ID", videoItems.get(position).getId());
                        startActivity(intent);
                    }
                });

                TextView title = (TextView) convertView.findViewById(R.id.video_title);
                TextView description = (TextView) convertView.findViewById(R.id.video_description);
                TextView publishAt = (TextView) convertView.findViewById(R.id.video_published);
                TextView viewsCount = (TextView) convertView.findViewById(R.id.video_views);

                final CheckBox checkbox = (CheckBox) convertView.findViewById(R.id.favorite);
                checkbox.setVisibility(View.INVISIBLE);

                final VideoItem searchResult = videoItems.get(position);

                Picasso.with(getActivity().getApplicationContext()).load(searchResult.getThumbnailURL()).into(thumbnail);
                title.setText(searchResult.getTitle());
                description.setText(searchResult.getDescription());
                publishAt.setText(searchResult.getPublishedAt().toString().substring(0, 10));
                viewsCount.setText(searchResult.getViewsCount().toString());

//                // set up check box
//                if (searchResult.getFavorite()) {
//                    checkbox.isChecked();
//                }
//
//                checkbox.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                    }
//                });


                return convertView;
            }
        };

        favoriteList.setAdapter(adapter);
    }
}
