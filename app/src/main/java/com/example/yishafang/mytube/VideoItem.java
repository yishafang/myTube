package com.example.yishafang.mytube;

import com.google.api.client.util.DateTime;

import java.math.BigInteger;

/**
 * This class is to store the following information about a YouTube video.
 *
 * @author yishafang on 10/12/15.
 */
public class VideoItem {
    private String title;
    private String description;
    private String thumbnailURL;
    private String id;
    private BigInteger viewsCount;
    private DateTime publishedAt;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigInteger getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(BigInteger viewsCount) {
        this.viewsCount = viewsCount;
    }

    public DateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(DateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}
