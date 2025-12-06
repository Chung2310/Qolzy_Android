package com.example.qolzy.ui.comment;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.qolzy.data.api.Api;
import com.example.qolzy.data.api.RetrofitClient;
import com.example.qolzy.data.model.Comment;
import com.example.qolzy.data.model.CommentReplies;
import com.example.qolzy.data.model.CommentRequest;
import com.example.qolzy.data.repository.UserRepository;
import com.example.qolzy.util.Utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;

public class CommentViewModel extends AndroidViewModel {
    private MutableLiveData<Integer> statusLiveData = new MutableLiveData<>();
    private MutableLiveData<String> messageLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Comment>> commentListLiveData = new MutableLiveData<>();

    private MutableLiveData<Map<Long, List<Comment>>> repliesMapLiveData = new MutableLiveData<>();

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Api api;


    public MutableLiveData<Map<Long, List<Comment>>> getRepliesMapLiveData() {
        return repliesMapLiveData;
    }

    public MutableLiveData<String> getMessageLiveData() {
        return messageLiveData;
    }

    public MutableLiveData<List<Comment>> getCommentListLiveData() {
        return commentListLiveData;
    }

    public CommentViewModel(@NonNull Application application) {
        super(application);
        api = RetrofitClient.getInstance(Utils.BASE_URL, application).create(Api.class);
    }

    public void toggleLike(Long commentId, Long userId){
        compositeDisposable.add(api.toggleLike("comment", userId, commentId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->{
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

    public void loadCommentParent(String mode,Long postId,Long userId ,int page, int size){
        compositeDisposable.add(api.loadCommentByPostId(postId, mode, userId, page,size)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->{
                            statusLiveData.setValue(response.getStatus());
                            messageLiveData.setValue(response.getMessage());
                            if(response.getStatus() == 200){
                                commentListLiveData.setValue(response.getResult());
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

    public void loadRepliesByParent(Long commentId,Long userId, int page, int size){
        compositeDisposable.add(api.loadCommentRepliesByCommentId(commentId, userId, page,size)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->{
                            statusLiveData.setValue(response.getStatus());
                            messageLiveData.setValue(response.getMessage());
                            if(response.getStatus() == 200){
                                Map<Long, List<Comment>> current = repliesMapLiveData.getValue();
                                if (current == null) current = new HashMap<>();
                                current.put(commentId, response.getResult());
                                repliesMapLiveData.setValue(current);

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

    public void createCommentParent(CommentRequest commentRequest){
        compositeDisposable.add(api.createCommentParent(commentRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->{
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

    public void createCommentForReel(CommentRequest commentRequest){
        compositeDisposable.add(api.createCommentForeReel(commentRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->{
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