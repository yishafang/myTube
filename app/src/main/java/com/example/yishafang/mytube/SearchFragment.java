package com.example.yishafang.mytube;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * @author yishafang on 10/15/15.
 */
public class SearchFragment extends android.support.v4.app.Fragment{
    public static final String ARG_PAGE = "ARG_PAGE";

    private String FAVORITE_LIST_TITLE = "SJSU-CMPE-277";

    private EditText searchInput;
    private ListView videosFound;

    private Handler handler;

    private List<VideoItem> searchResults;

    public static SearchFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        //addClickListener();
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
                            insertToPlaylist(, searchResult.getId());
                        }
                    }
                });

                return convertView;
            }
        };

        videosFound.setAdapter(adapter);
    }

    private void insertToPlaylist(String playlistId, String videoId) {

    }

    private void addClickListener() {
        videosFound.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity().getApplicationContext(), PlayerActivity.class);
                intent.putExtra("VIDEO_ID", searchResults.get(position).getId());
                startActivity(intent);
            }
        });
    }
}
