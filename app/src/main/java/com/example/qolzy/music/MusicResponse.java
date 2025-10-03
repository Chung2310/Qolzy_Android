package com.example.qolzy.music;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MusicResponse {
    @SerializedName("headers")
    private Headers headers;

    @SerializedName("results")
    private List<MusicItem> results;

    public Headers getHeaders() {
        return headers;
    }

    public List<MusicItem> getResults() {
        return results;
    }

    public static class Headers {
        @SerializedName("status")
        private String status;

        @SerializedName("code")
        private int code;

        @SerializedName("error_message")
        private String errorMessage;

        @SerializedName("results_count")
        private int resultsCount;

        public String getStatus() {
            return status;
        }

        public int getCode() {
            return code;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public int getResultsCount() {
            return resultsCount;
        }
    }
}
