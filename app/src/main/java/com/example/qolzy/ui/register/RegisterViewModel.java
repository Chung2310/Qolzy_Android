package com.example.qolzy.ui.register;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.qolzy.data.model.RegisterRequest;
import com.example.qolzy.data.api.Api;
import com.example.qolzy.data.api.RetrofitClient;
import com.example.qolzy.util.Utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;

public class RegisterViewModel extends AndroidViewModel {
    private final MutableLiveData<Integer> statusLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final Api api;

    public RegisterViewModel(@NotNull Application application) {
        super(application);
        api = RetrofitClient.getInstance(Utils.BASE_URL, application).create(Api.class);
    }

    public MutableLiveData<Integer> getStatusLiveData() {
        return statusLiveData;
    }

    public MutableLiveData<String> getMessageLiveData() {
        return messageLiveData;
    }

    public void createUser(RegisterRequest registerRequest) {
        compositeDisposable.add(api.register(registerRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        resultModel -> {
                            Log.d("RegisterViewModel", resultModel.getStatus() + "");
                            statusLiveData.setValue(resultModel.getStatus());
                            messageLiveData.setValue(resultModel.getMessage());
                        },
                        throwable -> {
                            if (throwable instanceof HttpException) {
                                HttpException httpEx = (HttpException) throwable;
                                statusLiveData.setValue(httpEx.code());

                                try {
                                    String errorBody = httpEx.response().errorBody().string();

                                    JSONObject json = new JSONObject(errorBody);
                                    String serverMessage = json.optString("message", "Lỗi không xác định");

                                    messageLiveData.setValue(serverMessage);
                                    Log.d("msg", "Error: " + serverMessage);
                                } catch (Exception e) {
                                    messageLiveData.setValue("Lỗi khi đọc message từ server");
                                    e.printStackTrace();
                                }
                            }

                        }
                ));
    }
}
