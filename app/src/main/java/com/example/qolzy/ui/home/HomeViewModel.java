package com.example.qolzy.ui.home;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.qolzy.data.api.Api;
import com.example.qolzy.data.api.RetrofitClient;
import com.example.qolzy.data.model.Post;
import com.example.qolzy.data.model.Story;
import com.example.qolzy.util.Utils;

import org.json.JSONObject;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;

public class HomeViewModel extends AndroidViewModel {
    private MutableLiveData<List<Post>> postsLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Story>> storiesLiveData = new MutableLiveData<>();
    private MutableLiveData<String> messageLiveData = new MutableLiveData<>();
    private MutableLiveData<Integer> statusLiveData = new MutableLiveData<>(0);
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Api api;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        api = RetrofitClient.getInstance(Utils.BASE_URL, application).create(Api.class);
    }

    public MutableLiveData<List<Post>> getPostsLiveData() {
        return postsLiveData;
    }

    public MutableLiveData<Integer> getStatusLiveData() {
        return statusLiveData;
    }

    public MutableLiveData<String> getMessageLiveData() {
        return messageLiveData;
    }

    public MutableLiveData<List<Story>> getStoriesLiveData() {
        return storiesLiveData;
    }

    public void getPosts(int page, int size, int userId){
        compositeDisposable.add(api.getPosts(page,size,userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            statusLiveData.setValue(response.getStatus());
                            messageLiveData.setValue(response.getMessage());

                            if (response.getStatus() == 200 && response.getResult() != null) {
                                postsLiveData.setValue(response.getResult());
                                Log.d("HomeViewModel", "Size list: " + response.getResult().size());
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
                                Log.d("HomeViewModel", "Lỗi: " + throwable.getMessage() );
                            }
                        }
                ));
    }

    public void toggleLike(String mode, Long userId, Long id){
        compositeDisposable.add(api.toggleLike(mode,userId,id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        likeResult ->{
                            Log.d("Like", likeResult.getMessage()+" ");
                        },throwable -> {
                            Log.d("Like", throwable.getMessage()+" ");
                        }
                ));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }

    public void getStory(int userId) {
        compositeDisposable.add(api.getStories(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            statusLiveData.setValue(response.getStatus());
                            messageLiveData.setValue(response.getMessage());

                            if (response.getStatus() == 200 && response.getResult() != null) {
                                storiesLiveData.setValue(response.getResult());
                                Log.d("HomeViewModel", "Size list: " + response.getResult().size());
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
                                Log.d("HomeViewModel", "Lỗi: " + throwable.getMessage() );
                            }
                        }
                ));
    }

    public void toggleFollow(Long followerId,Long followingId) {
        compositeDisposable.add(api.toggleFollow(followerId,followingId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            statusLiveData.setValue(response.getStatus());
                            messageLiveData.setValue(response.getMessage());
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
}