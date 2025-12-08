package com.example.qolzy.ui.post;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.qolzy.data.api.Api;
import com.example.qolzy.data.api.RetrofitClient;
import com.example.qolzy.data.model.Post;
import com.example.qolzy.util.Utils;

import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class PostDetailViewModel extends AndroidViewModel {
    private MutableLiveData<List<Post>> postsLiveData = new MutableLiveData<>();
    private MutableLiveData<String> msgLiveData = new MutableLiveData<>();
    private MutableLiveData<Integer> statusLiveData = new MutableLiveData<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Api api;

    public PostDetailViewModel(@NonNull Application application) {
        super(application);
        api = RetrofitClient.getInstance(Utils.BASE_URL, application).create(Api.class);
    }


}