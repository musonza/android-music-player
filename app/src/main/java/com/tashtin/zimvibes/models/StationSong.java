package com.tashtin.zimvibes.models;

import java.io.Serializable;

public class StationSong implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String url;
    private String stationId;
    private String artistName;
    private String uploadedBy;
    private String artworkUrl;
    private String liked;

    public String getDisliked() {
        return disliked;
    }

    public void setDisliked(String disliked) {
        this.disliked = disliked;
    }

    private String disliked;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getArtworkUrl() {
        return artworkUrl;
    }

    public void setArtworkUrl(String artworkUrl) {
        this.artworkUrl = artworkUrl;
    }

    public String getLiked() {
        return liked;
    }

    public void setLiked(String liked) {
        this.liked = liked;
    }
}
