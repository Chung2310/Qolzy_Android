package com.example.qolzy.ui.music;

import com.google.gson.annotations.SerializedName;

public class MusicItem {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("duration")
    private int duration;

    @SerializedName("artist_id")
    private String artistId;

    @SerializedName("artist_name")
    private String artistName;

    @SerializedName("album_name")
    private String albumName;

    @SerializedName("releasedate")
    private String releaseDate;

    @SerializedName("license_ccurl")
    private String licenseUrl;

    @SerializedName("audio")
    private String audioUrl;

    @SerializedName("audiodownload")
    private String audioDownload;

    @SerializedName("audiodownload_allowed")
    private boolean audioDownloadAllowed;

    @SerializedName("image")
    private String imageUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getAudioDownload() {
        return audioDownload;
    }

    public void setAudioDownload(String audioDownload) {
        this.audioDownload = audioDownload;
    }

    public boolean isAudioDownloadAllowed() {
        return audioDownloadAllowed;
    }

    public void setAudioDownloadAllowed(boolean audioDownloadAllowed) {
        this.audioDownloadAllowed = audioDownloadAllowed;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
