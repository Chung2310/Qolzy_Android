package com.example.qolzy.ui.message;

import static com.example.qolzy.util.NotificationHelper.showNotification;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.qolzy.R;
import com.example.qolzy.activity.MainActivity;
import com.example.qolzy.data.model.Message;
import com.example.qolzy.data.model.MessageRequest;
import com.example.qolzy.data.model.User;
import com.example.qolzy.data.repository.UserRepository;
import com.example.qolzy.databinding.FragmentDetailMessageBinding;
import com.example.qolzy.ui.account.AccountFragment;
import com.example.qolzy.ui.account.AccountViewModel;
import com.example.qolzy.util.Utils;
import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class DetailMessageFragment extends Fragment {

    private DetailMessageViewModel mViewModel;
    private AccountViewModel accountViewModel;
    private FragmentDetailMessageBinding binding;
    private Long userId;
    private User contact;
    private int page = 0, size = 20;
    private StompClient stompClient;
    private MessageAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private UserRepository userRepository;
    private static final String TAG = "WebSocket";

    public static DetailMessageFragment newInstance() {
        return new DetailMessageFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailMessageBinding.inflate(inflater, container, false);

        userRepository = new UserRepository(requireContext());
        userId = (long) userRepository.getUserId();
        connectStomp();

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(DetailMessageViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        adapter = new MessageAdapter(new ArrayList<>(), getContext(), userId);

        linearLayoutManager = new LinearLayoutManager(getContext());
//        linearLayoutManager.setStackFromEnd(true);
//        linearLayoutManager.setReverseLayout(false);
        binding.recyclerMessages.setLayoutManager(linearLayoutManager);

        binding.recyclerMessages.setAdapter(adapter);

        binding.chatToolbar.setNavigationOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        Bundle args = getArguments();
        if (args != null) {
            contact = (User) args.getSerializable("contact");
        }

        if (contact == null) {
            Log.e("DetailMessageFragment", "contact is null!");
            return;
        }

        String fullName = (contact.getFirstName() == null)
                ? contact.getLastName()
                : contact.getFirstName() + " " + contact.getLastName();
        binding.tvName.setText(fullName);

        String fixedUrl = Utils.BASE_URL.replace("/api/", "");
        String avatarUrl = contact.getAvatarUrl().contains("https")
                ? contact.getAvatarUrl()
                : fixedUrl + contact.getAvatarUrl();

        Log.d("AvatarUrl", avatarUrl);
        Glide.with(requireContext())
                .load(avatarUrl)
                .placeholder(R.drawable.ic_android_black_24dp)
                .error(R.drawable.user)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(binding.imgAvatar);

        binding.recyclerMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int first = linearLayoutManager.findFirstVisibleItemPosition();

                // Nếu chạm top -> load more
                if (first == 0) {
                    loadMoreMessages();
                }
            }
        });


        // Lấy danh sách tin nhắn ban đầu
        if (userId != null && contact.getId() != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
            mViewModel.getMessages(userId, contact.getId(), page, size);
        }

        // Quan sát LiveData để cập nhật danh sách tin nhắn
        mViewModel.getMessagesLiveData().observe(getViewLifecycleOwner(), messagesResponse -> {
            if (messagesResponse != null) {
                if(page == 0){
                    Collections.reverse(messagesResponse);
                    adapter.updateMessages(messagesResponse);
                    binding.recyclerMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
                }
                else {
                    Collections.reverse(messagesResponse);
                    adapter.addNewMessages(messagesResponse);
                }
                binding.progressBar.setVisibility(View.INVISIBLE);

            } else {
            }
        });




        binding.btnSend.setOnClickListener(v -> {
            String content = binding.edtMessage.getText().toString().trim();

            if (content != null && !content.isEmpty()) {
                Log.d("SendMessage", "Gửi: " + content);
                Message message = new Message();
                message.setContent(content);
                message.setReceiver(contact);
                message.setSender(userRepository.getUser());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    message.setCreatedAt(LocalDateTime.now().toString());
                }

                mViewModel.sendMessage(new MessageRequest(userId, contact.getId(), content));
                binding.edtMessage.setText("");
                adapter.addMessage(message);
                binding.recyclerMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
                mViewModel.getMessages(userId, contact.getId(), page, size);
            }
        });

        binding.imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAccountFragment(contact);

            }
        });
    }

    private void loadMoreMessages() {
        page++;
        mViewModel.getMessages(userId, contact.getId(),page, size);
    }

    @SuppressLint("CheckResult")
    private void connectStomp() {
        String fixedUrl = Utils.BASE_URL.replace("/api/", "");
        String fixedUrlWs = fixedUrl.replace("https", "wss");
        String wsUrl = fixedUrlWs + "/chat/websocket";

        stompClient = Stomp.over(
                Stomp.ConnectionProvider.OKHTTP,
                wsUrl
        );
        stompClient.withClientHeartbeat(10000)
                .withServerHeartbeat(10000);


        stompClient.lifecycle().subscribe(event ->{
            switch (event.getType()) {
                case OPENED:
                    Log.d(TAG, " STOMP CONNECTED");
                    subscribeMessage();
                    break;

                case ERROR:
                    Log.e(TAG, " STOMP ERROR", event.getException());
                    break;

                case CLOSED:
                    Log.w(TAG, "🔌 STOMP CLOSED");
                    break;
            }
        });

        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader("user-id", String.valueOf(userRepository.getUserId())));
        stompClient.connect(headers);
    }

    private void openAccountFragment(User user) {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        args.putSerializable("USER", user);
        fragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @SuppressLint("CheckResult")
    private void subscribeMessage() {
        stompClient.topic("/user/queue/messages")
                .subscribe(stompMessage -> {
                    String payload = stompMessage.getPayload();
                    Log.d(TAG, "📩 Received notification: " + payload);

                    Message message = new Gson().fromJson(payload, Message.class);

                    // ⚠️ Đưa UI update sang UI Thread
                    requireActivity().runOnUiThread(() -> {
                        adapter.addMessage(message);
                        binding.recyclerMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
                    });
                });
    }


    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).setBottomNavigationVisibility(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) requireActivity()).setBottomNavigationVisibility(true);
    }


}
