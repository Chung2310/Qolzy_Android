package com.example.qolzy.ui.add_post.post_detail;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.qolzy.data.api.Api;
import com.example.qolzy.data.api.RetrofitClient;
import com.example.qolzy.data.model.CreatePostRequest;
import com.example.qolzy.ui.music.MusicItem;
import com.example.qolzy.util.Utils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.HttpException;

public class PostDetailViewModel extends AndroidViewModel {
    private MutableLiveData<Integer> statusLiveData = new MutableLiveData<>();
    private MutableLiveData<String> messageLiveData = new MutableLiveData<>();
    private MutableLiveData<String> postIdLiveData = new MutableLiveData<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Api api;

    public PostDetailViewModel(@NonNull Application application) {
        super(application);
        api = RetrofitClient.getInstance(Utils.BASE_URL,application).create(Api.class);
    }

    public MutableLiveData<Integer> getStatusLiveData() {
        return statusLiveData;
    }

    public MutableLiveData<String> getMessageLiveData() {
        return messageLiveData;
    }

    public MutableLiveData<String> getPostIdLiveData() {
        return postIdLiveData;
    }

    public void createPost(String content, Long userId, MusicItem musicItem){

        compositeDisposable.add(api.createPost(new CreatePostRequest(content,musicItem,userId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    response ->{
                        postIdLiveData.setValue(response.getResult());
                    }, throwable -> {
                            if (throwable instanceof HttpException) {
                                HttpException httpEx = (HttpException) throwable;
                                statusLiveData.setValue(httpEx.code());
                                try {
                                    String errorBody = httpEx.response().errorBody().string();
                                    JSONObject json = new JSONObject(errorBody);
                                    String serverMessage = json.optString("message", "Lỗi không xác định");
                                    messageLiveData.setValue(serverMessage);
                                } catch (Exception e) {
                                    messageLiveData.setValue("Lỗi khi đọc message từ server");
                                }
                            } else {
                                messageLiveData.setValue("Lỗi: " + throwable.getMessage());
                                Log.d("LoginViewModel", "Lỗi: " + throwable.getMessage() );
                            }
                        }
                ));
    }

    public void createPostFile(Context context,Long postId ,List<String> uriStrings){

        List<MultipartBody.Part> parts = new ArrayList<>();

        for (String uriStr : uriStrings) {
            Uri uri = Uri.parse(uriStr);

            // Lấy mime type từ contentResolver
            String mimeType = context.getContentResolver().getType(uri);
            if (mimeType == null) mimeType = "application/octet-stream";

            // Chuyển Uri thành File tạm trong cache (an toàn với Android 10+)
            File file = uriToFile(context, uri);

            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("files", file.getName(), requestFile);
            parts.add(part);
        }

        compositeDisposable.add(api.createPostFile(postId, parts)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->{
                            statusLiveData.setValue(response.getStatus());
                            messageLiveData.setValue(response.getMessage());
                        }, throwable -> {
                            if (throwable instanceof HttpException) {
                                HttpException httpEx = (HttpException) throwable;
                                statusLiveData.setValue(httpEx.code());
                                try {
                                    String errorBody = httpEx.response().errorBody().string();
                                    JSONObject json = new JSONObject(errorBody);
                                    String serverMessage = json.optString("message", "Lỗi không xác định");
                                    messageLiveData.setValue(serverMessage);
                                } catch (Exception e) {
                                    messageLiveData.setValue("Lỗi khi đọc message từ server");
                                }
                            } else {
                                messageLiveData.setValue("Lỗi: " + throwable.getMessage());
                                Log.d("LoginViewModel", "Lỗi: " + throwable.getMessage() );
                            }
                        }
                ));
    }

    private File uriToFile(Context context, Uri uri) {
        File file = null;
        try {
            String fileName = "upload_" + System.currentTimeMillis();
            String ext = "";
            String mimeType = context.getContentResolver().getType(uri);
            if (mimeType != null) {
                ext = mimeType.substring(mimeType.lastIndexOf("/") + 1);
            }
            file = new File(context.getCacheDir(), fileName + "." + ext);

            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            OutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public void createStory(Context context, Long userId, String s) {
        Uri uri = Uri.parse(s);

        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType == null) mimeType = "application/octet-stream";

        // Chuyển Uri thành File tạm trong cache (an toàn với Android 10+)
        File file = uriToFile(context, uri);

        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        compositeDisposable.add(api.createStory(userId, part)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->{
                            statusLiveData.setValue(response.getStatus());
                            messageLiveData.setValue(response.getMessage());
                        }, throwable -> {
                            if (throwable instanceof HttpException) {
                                HttpException httpEx = (HttpException) throwable;
                                statusLiveData.setValue(httpEx.code());
                                try {
                                    String errorBody = httpEx.response().errorBody().string();
                                    JSONObject json = new JSONObject(errorBody);
                                    String serverMessage = json.optString("message", "Lỗi không xác định");
                                    messageLiveData.setValue(serverMessage);
                                } catch (Exception e) {
                                    messageLiveData.setValue("Lỗi khi đọc message từ server");
                                }
                            } else {
                                messageLiveData.setValue("Lỗi: " + throwable.getMessage());
                                Log.d("LoginViewModel", "Lỗi: " + throwable.getMessage() );
                            }
                        }
                ));
    }
}