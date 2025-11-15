package com.example.qolzy.ui.account.edit_profile;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.qolzy.data.api.Api;
import com.example.qolzy.data.api.RetrofitClient;
import com.example.qolzy.util.Utils;

import org.json.JSONObject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
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


}