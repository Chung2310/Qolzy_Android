package com.example.qolzy.ui.message;

import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
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

import java.util.ArrayList;
import java.util.List;

public class DetailMessageFragment extends Fragment {

    private DetailMessageViewModel mViewModel;
    private AccountViewModel accountViewModel;
    private FragmentDetailMessageBinding binding;
    private Long userId;
    private User contact;
    private int page = 0, size = 20;
    private MessageAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private UserRepository userRepository;

    public static DetailMessageFragment newInstance() {
        return new DetailMessageFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailMessageBinding.inflate(inflater, container, false);

        userRepository = new UserRepository(requireContext());
        userId = (long) userRepository.getUserId();

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(DetailMessageViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        adapter = new MessageAdapter(new ArrayList<>(), getContext(), userId);

        linearLayoutManager = new LinearLayoutManager(getContext());

        binding.recyclerMessages.setLayoutManager(linearLayoutManager);

        binding.recyclerMessages.setAdapter(adapter);

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
                : fixedUrl + "avatar/" + contact.getAvatarUrl();

        Log.d("AvatarUrl", avatarUrl);
        Glide.with(requireContext())
                .load(avatarUrl)
                .placeholder(R.drawable.ic_android_black_24dp)
                .error(R.drawable.user)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(binding.imgAvatar);


        // Lấy danh sách tin nhắn ban đầu
        if (userId != null && contact.getId() != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
            mViewModel.getMessages(userId, contact.getId(), page, size);
        }

        // Quan sát LiveData để cập nhật danh sách tin nhắn
        mViewModel.getMessagesLiveData().observe(getViewLifecycleOwner(), messagesResponse -> {
            Log.d("DetailMessageFragment", ">>> Observer triggered");
            if (messagesResponse != null) {
                Log.d("DetailMessageFragment", "Nhận " + messagesResponse.size() + " tin nhắn");
                binding.progressBar.setVisibility(View.INVISIBLE);
                adapter.updateMessages(messagesResponse);
                Log.d("DetailMessageFragment", "Cập nhật adapter xong");
            } else {
                Log.w("DetailMessageFragment", "messagesResponse null!");
            }
        });




        binding.btnSend.setOnClickListener(v -> {
            String content = binding.edtMessage.getText().toString().trim();

            if (content != null && !content.isEmpty()) {
                Log.d("SendMessage", "Gửi: " + content);
                mViewModel.sendMessage(new MessageRequest(userId, contact.getId(), content));
                binding.edtMessage.setText("");
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
