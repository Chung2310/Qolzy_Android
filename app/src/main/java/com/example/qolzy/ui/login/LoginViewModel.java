package com.example.qolzy.ui.login;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.qolzy.data.model.LoginRequest;
import com.example.qolzy.data.model.LoginRequestFirebase;
import com.example.qolzy.data.model.User;
import com.example.qolzy.data.api.Api;
import com.example.qolzy.data.api.RetrofitClient;
import com.example.qolzy.util.Utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;

public class LoginViewModel extends AndroidViewModel {
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final Api api;
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private MutableLiveData<Integer> statusLiveData = new MutableLiveData<>();
    private MutableLiveData<String> messageLiveData = new MutableLiveData<>();

    public LoginViewModel(@NotNull Application application) {
        super(application);
        api = RetrofitClient.getInstance(Utils.BASE_URL, application).create(Api.class);
    }

    public MutableLiveData<User> getUserLiveData() {
        return userLiveData;
    }
    public MutableLiveData<Integer> getStatusLiveData() {
        return statusLiveData;
    }

    public MutableLiveData<String> getMessageLiveData() {
        return messageLiveData;
    }

    public void login(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);

        compositeDisposable.add(api.login(loginRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            statusLiveData.setValue(response.getStatus());
                            messageLiveData.setValue(response.getMessage());

                            if (response.getStatus() == 200 && response.getResult() != null) {
                                userLiveData.setValue(response.getResult());
                                Log.d("LoginViewModel", "User ID: " + response.getResult().getId());
                            }
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

    public void loginOrRegisterWithFirebase(LoginRequestFirebase loginRequestFirebase){
        Log.d("LoginViewModel", "Login Firebase: "+ loginRequestFirebase.getIdToken() );
        compositeDisposable.add(api.loginOrRegisterWithFirebase(loginRequestFirebase)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->{
                            statusLiveData.setValue(response.getStatus());
                            messageLiveData.setValue(response.getMessage());

                            if(response.getStatus() == 200 && response.getResult() != null){
                                userLiveData.setValue(response.getResult());
                            }
                        },throwable -> {
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

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
