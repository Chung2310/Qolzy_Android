package com.example.qolzy.ui.music;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.qolzy.data.api.Api;
import com.example.qolzy.data.api.RetrofitClient;
import com.example.qolzy.util.Utils;

import org.json.JSONObject;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;

public class MusicViewModel extends AndroidViewModel {
    private MutableLiveData<List<MusicItem>> musicItemsLiveData = new MutableLiveData<>();
    private MutableLiveData<Integer> statusLiveData = new MutableLiveData<>();
    private MutableLiveData<String> messageLiveData = new MutableLiveData<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Api api;
    public MusicViewModel(@NonNull Application application) {
        super(application);
        api = RetrofitClient.getInstance(Utils.JAMENDO_URL,application).create(Api.class);
    }

    public MutableLiveData<List<MusicItem>> getMusicItemsLiveData() {
        return musicItemsLiveData;
    }

    public void getMusicItems(String search, int size){

        compositeDisposable.add(api.getMusic(Utils.CLIENT_ID,"json",size,search)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            musicItemsLiveData.setValue(response.getResults());
                            Log.d("HomeViewModel", "Size list: " + response.getResults().size());

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
                                Log.d("HomeViewModel", "Lỗi: " + throwable.getMessage() );
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
