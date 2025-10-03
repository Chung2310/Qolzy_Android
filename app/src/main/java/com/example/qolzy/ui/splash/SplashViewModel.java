package com.example.qolzy.ui.splash;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.qolzy.data.model.RefreshTokenRequest;
import com.example.qolzy.data.api.Api;
import com.example.qolzy.data.api.RetrofitClient;
import com.example.qolzy.util.Utils;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SplashViewModel extends AndroidViewModel {
    private MutableLiveData<RefreshTokenRequest> refreshTokenRequestMutableLiveData = new MutableLiveData<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Api api;
    private MutableLiveData<Integer> statusConnect = new MutableLiveData<>();

    public SplashViewModel(@NonNull Application application) {
        super(application);
        api = RetrofitClient.getInstance(Utils.BASE_URL, application).create(Api.class);
    }

    public void refreshToken(RefreshTokenRequest request){
        compositeDisposable.add(api.refresh(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        accessTokenModel -> {
                            Log.d("token", accessTokenModel.getResult().getAccessToken());
                            if (accessTokenModel.getStatus() == 200  ) {
                                SharedPreferences securePrefs = getApplication().getSharedPreferences("TokenAuth", Context.MODE_PRIVATE);
                                securePrefs.edit()
                                        .putString("token", accessTokenModel.getResult().getAccessToken())
                                        .putString("refreshToken", accessTokenModel.getResult().getRefreshToken())
                                        .apply();

                                refreshTokenRequestMutableLiveData.setValue(accessTokenModel.getResult());
                            } else {
                                Log.w("token", "Refresh token failed with status: " + accessTokenModel.getStatus());
                            }
                        },
                        throwable -> {
                            Log.e("token", "Refresh token error: " + throwable.getMessage(), throwable);

                        }
                ));
    }

    public MutableLiveData<RefreshTokenRequest> getRefreshTokenRequestMutableLiveData() {
        return refreshTokenRequestMutableLiveData;
    }
    public MutableLiveData<Integer> getStatusConnect() {
        return statusConnect;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
