package com.example.qolzy.data.api;

import com.example.qolzy.data.model.ChangePasswordRequest;
import com.example.qolzy.data.model.Comment;
import com.example.qolzy.data.model.CommentRequest;
import com.example.qolzy.data.model.Contact;
import com.example.qolzy.data.model.CreatePostRequest;
import com.example.qolzy.data.model.LoginRequest;
import com.example.qolzy.data.model.LoginRequestFirebase;
import com.example.qolzy.data.model.Message;
import com.example.qolzy.data.model.MessageRequest;
import com.example.qolzy.data.model.Notification;
import com.example.qolzy.data.model.Post;
import com.example.qolzy.data.model.RefreshTokenRequest;
import com.example.qolzy.data.model.RegisterRequest;
import com.example.qolzy.data.model.ResultModel;
import com.example.qolzy.data.model.Story;
import com.example.qolzy.data.model.User;
import com.example.qolzy.data.model.UserUpdateRequest;
import com.example.qolzy.ui.music.MusicResponse;
import com.example.qolzy.ui.reels.Reel;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.rxjava3.core.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Api {
    // api auth
    @POST("auth/login")
    Observable<ResultModel<User>> login(@Body LoginRequest loginRequest);
    @POST("auth/register")
    Observable<ResultModel<String>> register(@Body RegisterRequest registerRequest);
    @POST("auth/firebase")
    Observable<ResultModel<User>> loginOrRegisterWithFirebase(@Body LoginRequestFirebase loginRequestFirebase);
    @POST("auth/refreshToken")
    Observable<ResultModel<RefreshTokenRequest>> refresh(@Body RefreshTokenRequest request);

    // api user
    @POST("user/update")
    Observable<ResultModel<User>> updateUser(@Body UserUpdateRequest userUpdateRequest);
    @POST("user/changePass")
    Observable<ResultModel<String>> changePassword(@Body ChangePasswordRequest changePasswordRequest);
    @POST("user/username")
    Observable<ResultModel<String>> saveUserName(@Query("userName") String userName,
                                                 @Query("userId") Long userId);
    @Multipart
    @POST("user/uploadImage/{userId}")
    Observable<ResultModel<String>> uploadImage(
            @Path("userId") Long id,
            @Query("mode") String mode,
            @Part MultipartBody.Part image
    );


    @POST("post")
    Observable<ResultModel<String>> createPost(
            @Body CreatePostRequest createPostRequest
    );

    @Multipart
    @POST("post/upload")
    Observable<ResultModel<String>> createPostFile(
            @Query("postId") Long postId,
            @Part List<MultipartBody.Part> files

    );

    @GET("reels")
    Observable<ResultModel<List<Reel>>> getReels(
            @Query("userId") Long userId,
            @Query("page") int page,
            @Query("size") int size
    );

    //api post
    @GET("post")
    Observable<ResultModel<List<Post>>> getPosts(@Query("page") int page,
                                           @Query("size") int size,
                                           @Query("userId") int userId);
    @GET("post/{userId}")
    Observable<ResultModel<List<Post>>> getPostsHistory(@Path("userId") Long userId,
                                                        @Query("page") int page,
                                                        @Query("size") int size);


    //api story
    @GET("story")
    Observable<ResultModel<List<Story>>> getStories(@Query("userId") int userId);
    @GET("story/{userId}")
    Observable<ResultModel<List<Story>>> getStoriesHistory(@Path("userId") Long userId,
                                                           @Query("page") int page,
                                                           @Query("size") int size);

    // api like
    @PUT("like")
    Observable<ResultModel<String>> toggleLike(@Query("mode") String mode,
                                               @Query("userId") Long userId,
                                               @Query("id") Long id);
    // api comment
    @GET("comment/{postId}")
    Observable<ResultModel<List<Comment>>> loadCommentByPostId(
            @Path("postId") Long postId,
            @Query("userId") Long userId,
            @Query("page") int page,
            @Query("size") int size);

    @POST("comment")
    Observable<ResultModel<String>> createCommentParent(
            @Body CommentRequest commentRequest);

    @GET("comment/replies/{commentId}")
    Observable<ResultModel<List<Comment>>> loadCommentRepliesByCommentId(
            @Path("commentId") Long commentId,
            @Query("userId") Long userId,
            @Query("page") int page,
            @Query("size") int size);

    // lấy nhạc
    @GET("v3.0/tracks/")
    Observable<MusicResponse> getMusic(
            @Query("client_id") String clientId,
            @Query("format") String format,
            @Query("limit") int limit,
            @Query("search") String search
    );

    // lấy danh sách đã liên hệ
    @GET("contact")
    Observable<ResultModel<List<Contact>>> getContacts(
            @Query("userId") Long userId,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("contact/")
    Observable<ResultModel<List<Contact>>> getContactsByUserName(
            @Query("userName") String userName,
            @Query("page") int page,
            @Query("size") int size
    );

    // lấy danh sách tin nhắn của người dùng và liên hệ
    @GET("message")
    Observable<ResultModel<List<Message>>> getMessagesByUserIdAndContactId(
            @Query("senderId") Long senderId,
            @Query("receiverId") Long receiverId,
            @Query("page") int page,
            @Query("size") int size
    );

    @POST("follow")
    Observable<ResultModel<String>> toggleFollow(@Query("followerId") Long follower,
                                                 @Query("followingId") Long followingId);

    @GET("auth/user")
    Observable<ResultModel<User>> getUserDetail(@Query("userId") Long userId);

    @POST("message")
    Observable<ResultModel<String>> sendMessage(@Body MessageRequest messageRequest);

    @GET("user/search")
    Observable<ResultModel<List<User>>> searchUser(@Query("keySearch") String keySearch);

    @GET("notification")
    Observable<ResultModel<List<Notification>>> getNotificationByUserId(@Query("page") int page,
                                                                        @Query("size") int size,
                                                                        @Query("userId") Long userId);
    @Multipart
    @POST("story")
    Observable<ResultModel<String>> createStory(@Query("userId") Long userId,
                                                @Part MultipartBody.Part file);

    @POST("contact")
    Observable<ResultModel<String>> createContact(@Query("userId") Long userId,
                                                  @Query("contactId") Long contactId);
}
