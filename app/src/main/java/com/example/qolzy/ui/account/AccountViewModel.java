package com.example.qolzy.ui.account;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.qolzy.data.model.ChangePasswordRequest;
import com.example.qolzy.data.model.User;
import com.example.qolzy.data.model.UserUpdateRequest;
import com.example.qolzy.data.repository.UserRepository;
import com.example.qolzy.data.api.Api;
import com.example.qolzy.data.api.RetrofitClient;
import com.example.qolzy.util.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class AccountViewModel extends AndroidViewModel {
    private final MutableLiveData<User> userMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> uploadResult = new MutableLiveData<>();
    private Api api;
    private UserRepository userRepository;
    private User user;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public AccountViewModel(@NotNull Application application) {
        super(application);
        api = RetrofitClient.getInstance(Utils.BASE_URL,application).create(Api.class);
        userRepository = new UserRepository(application);
        user = userRepository.getUser();
    }

    public void updateUser(Long id,String name, String phone, String address){
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest(id,name,phone,address);

        compositeDisposable.add(api.updateUser(userUpdateRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        userModel -> {
                            if(userModel.getStatus() == 200){
                                userRepository.saveUser(userModel.getResult());
                            } else {
                                Log.d("AccountViewModel",userModel.getMessage());
                            }
                        },throwable -> {
                            Log.d("AccountViewModel",throwable.getMessage());
                        }

                ));
    }

    public void changePassword(Long id, String oldPassword, String newPassword){
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest(id,oldPassword,newPassword);
        compositeDisposable.add(api.changePassword(changePasswordRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        messageModel -> {
                            if(messageModel.getStatus() == 200){
                                Toast.makeText(getApplication(),"Đổi mật khẩu thành công!",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getApplication(),messageModel.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        },throwable -> {
                            Toast.makeText(getApplication(),throwable.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    public void uploadImage(Context context, Uri imageUri,String currentUploadMode) {
        String realPath = getRealPathFromURI(context, imageUri);
        if (realPath == null) {
            uploadResult.postValue("invalid_path");
            return;
        }

        File file = new File(realPath);
        RequestBody requestFile = RequestBody.create(MediaType.parse(currentUploadMode+"/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        compositeDisposable.add(api.uploadImage(user.getId(), currentUploadMode, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        imageModel -> {
                            if(imageModel.getStatus() == 200){
                                if(currentUploadMode == "avatar"){
                                    user.setAvatarUrl(imageModel.getResult());
                                }
                                userRepository.saveUser(user);
                            }
                            else {
                                Log.d("AccoutViewModel",imageModel.getMessage());
                            }
                        },throwable -> {
                            Log.d("AccoutViewModel",throwable.getMessage());
                        }
                ));
    }

    private String getRealPathFromURI(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) return uri.getPath();
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        String path = cursor.getString(idx);
        cursor.close();
        return path;
    }
}