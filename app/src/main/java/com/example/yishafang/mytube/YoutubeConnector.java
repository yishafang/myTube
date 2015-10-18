package com.example.yishafang.mytube;

import android.content.Context;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Avoid having to deal with the YouTube Data API directly in our Activity.
 *
 * An instance of the YouTube class that will be used for communicating with the YouTube API
 * An instance of YouTube.Search.List to represent a search query
 * The YouTube API key as a static String
 *
 * @author yishafang on 10/12/15.
 */
public class YoutubeConnector {
    private YouTube youtube;
    private YouTube.Search.List query;

    // Developer Key
    public static final String KEY = "AIzaSyCglFHAsH2HLqkBIMMWBWlZ8iRCp3Za07o";

    private static final long NUMBER_OF_VIDEOS_RETURNED = 25;

    public YoutubeConnector(Context context) {
        youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest) throws IOException {}
        }).setApplicationName(context.getString(R.string.app_name)).build();

//        youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, credential).setApplicationName(
//                "youtube-cmdline-channelbulletin-sample").build();
        //credential要加到里面再build，然后设置accesstoken

//        Credential credential = Auth.authorize(scopes, "channelbulletin");
//        credential.setAccessToken()


        try {
            query = youtube.search().list("id, snippet");
            query.setKey(KEY);
            query.setType("video");
            query.setFields("items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url,snippet/publishedAt)");
            query.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

        } catch (IOException e) {
            Log.d("YC", "Could not initialize: " + e);
        }
    }

    public List<VideoItem> search(String keywords) {
        query.setQ(keywords);

        try {
            SearchListResponse response = query.execute();
            List<SearchResult> results = response.getItems();

            List<VideoItem> items = new ArrayList<>();
            for (SearchResult result : results) {
                VideoItem item = new VideoItem();
                item.setTitle(result.getSnippet().getTitle());
                item.setDescription(result.getSnippet().getDescription());
                item.setThumbnailURL(result.getSnippet().getThumbnails().getDefault().getUrl());
                item.setPublishedAt(result.getSnippet().getPublishedAt());

                String videoId = result.getId().getVideoId();
                item.setId(videoId);

                // get number of views
                YouTube.Videos.List videoListQuery = youtube.videos().list("snippet, statistics").setId(videoId);
                videoListQuery.setKey(KEY);
                Video video = videoListQuery.execute().getItems().get(0);
                item.setViewsCount(video.getStatistics().getViewCount());

                items.add(item);
            }

            return items;
        } catch (IOException e) {
            Log.d("YC", "Could not search: " + e);
            return null;
        }
    }

}

