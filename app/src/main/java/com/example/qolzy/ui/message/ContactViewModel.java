package com.example.qolzy.ui.message;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.qolzy.data.api.Api;
import com.example.qolzy.data.api.RetrofitClient;
import com.example.qolzy.data.model.Contact;
import com.example.qolzy.util.Utils;

import org.json.JSONObject;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;

public class ContactViewModel extends AndroidViewModel {
    private MutableLiveData<List<Contact>> contactsLiveData = new MutableLiveData<>();
    private MutableLiveData<Integer> statusLiveData = new MutableLiveData<>();
    private MutableLiveData<String> messageLiveData = new MutableLiveData<>();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Api api;

    public MutableLiveData<List<Contact>> getContactsLiveData() {
        return contactsLiveData;
    }

    public MutableLiveData<Integer> getStatusLiveData() {
        return statusLiveData;
    }

    public MutableLiveData<String> getMessageLiveData() {
        return messageLiveData;
    }

    public ContactViewModel(@NonNull Application application) {
        super(application);
        api = RetrofitClient.getInstance(Utils.BASE_URL, application).create(Api.class);
    }

    public void getContacts(Long userId, int page,int size){
        compositeDisposable.add(api.getContacts(userId,page,size)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->{
                            statusLiveData.setValue(response.getStatus());
                            messageLiveData.setValue(response.getMessage());

                            if (response.getStatus() == 200 && response.getResult() != null) {
                                contactsLiveData.setValue(response.getResult());
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

    public void getContactsByUserNam(String userName, int page,int size){
        compositeDisposable.add(api.getContactsByUserName(userName,page,size)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response ->{
                            statusLiveData.setValue(response.getStatus());
                            messageLiveData.setValue(response.getMessage());

                            if (response.getStatus() == 200 && response.getResult() != null) {
                                contactsLiveData.setValue(response.getResult());
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
}