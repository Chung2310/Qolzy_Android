package com.example.qolzy.ui.account.edit_profile;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.qolzy.data.api.Api;
import com.example.qolzy.data.api.RetrofitClient;
import com.example.qolzy.util.Utils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.HttpException;

public class EditProfileViewModel extends AndroidViewModel {
    private MutableLiveData<Integer> statusLivedata = new MutableLiveData<>();
    private MutableLiveData<String> messageLivedata = new MutableLiveData<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Api api;

    public EditProfileViewModel(@NonNull Application application) {
        super(application);
        api = RetrofitClient.getInstance(Utils.BASE_URL, application).create(Api.class);
    }

    public MutableLiveData<Integer> getStatusLivedata() {
        return statusLivedata;
    }

    public MutableLiveData<String> getMessageLivedata() {
        return messageLivedata;
    }

    public void updateUserProfile(Long id, String name, String bio) {
        compositeDisposable.add(api.updateUserProfile(id, name, bio)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            statusLivedata.setValue(response.getStatus());
                            messageLivedata.setValue(response.getMessage());
                        },
                        throwable -> {
                            if (throwable instanceof HttpException) {
                                HttpException httpEx = (HttpException) throwable;
                                statusLivedata.setValue(httpEx.code());
                                try {
                                    String errorBody = httpEx.response().errorBody().string();
                                    JSONObject json = new JSONObject(errorBody);
                                    String serverMessage = json.optString("message", "Lỗi không xác định");
                                    messageLivedata.setValue(serverMessage);
                                } catch (Exception e) {
                                    messageLivedata.setValue("Lỗi khi đọc message từ server");
                                }
                            } else {
                                messageLivedata.setValue("Lỗi: " + throwable.getMessage());
                                Log.d("EditProfileViewModel", "Lỗi: " + throwable.getMessage());
                            }
                        }
                ));
    }


    public void updateUserAvatar(Context context,Long id, String string) {
        Uri uri = Uri.parse(string);

        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType == null) mimeType = "application/octet-stream";

        // Chuyển Uri thành File tạm trong cache (an toàn với Android 10+)
        File file = uriToFile(context, uri);

        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        compositeDisposable.add(api.updateAvatarUser(id, part)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            statusLivedata.setValue(response.getStatus());
                            messageLivedata.setValue(response.getMessage());
                        },
                        throwable -> {
                            if (throwable instanceof HttpException) {
                                HttpException httpEx = (HttpException) throwable;
                                statusLivedata.setValue(httpEx.code());
                                try {
                                    String errorBody = httpEx.response().errorBody().string();
                                    JSONObject json = new JSONObject(errorBody);
                                    String serverMessage = json.optString("message", "Lỗi không xác định");
                                    messageLivedata.setValue(serverMessage);
                                } catch (Exception e) {
                                    messageLivedata.setValue("Lỗi khi đọc message từ server");
                                }
                            } else {
                                messageLivedata.setValue("Lỗi: " + throwable.getMessage());
                                Log.d("EditProfileViewModel", "Lỗi: " + throwable.getMessage());
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
}