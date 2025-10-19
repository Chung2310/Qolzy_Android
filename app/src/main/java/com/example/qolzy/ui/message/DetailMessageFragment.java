package com.example.qolzy.ui.message;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.qolzy.R;
import com.example.qolzy.data.model.Message;
import com.example.qolzy.data.model.MessageRequest;
import com.example.qolzy.data.model.User;
import com.example.qolzy.data.repository.UserRepository;
import com.example.qolzy.databinding.FragmentDetailMessageBinding;
import com.example.qolzy.ui.account.AccountFragment;

import java.util.ArrayList;
import java.util.List;

public class DetailMessageFragment extends Fragment {

    private DetailMessageViewModel mViewModel;
    private FragmentDetailMessageBinding binding;
    private Long contactId, userId;
    private int page =0, size = 20;
    private MessageAdapter adapter;
    private List<Message> messages = new ArrayList<>();
    private UserRepository userRepository;

    public static DetailMessageFragment newInstance() {
        return new DetailMessageFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetailMessageBinding.inflate(inflater,container,false);

        userRepository = new UserRepository(requireContext());
        userId = (long) userRepository.getUserId();

        binding.recyclerMessages.setLayoutManager( new LinearLayoutManager(requireContext()));
        adapter = new MessageAdapter(messages, requireContext(), userId);
        binding.recyclerMessages.setAdapter(adapter);

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(DetailMessageViewModel.class);

        Bundle args = getArguments();
        if (args != null) {
            contactId = args.getLong("contactId");
        }

        if(contactId != null && userId != null){
            mViewModel.getMessages(userId, contactId, page, size);
        }

        mViewModel.getMessagesLiveData().observe(getViewLifecycleOwner(), messagesResponse ->{
            adapter.updateMessages(messagesResponse);
        });

        adapter.setListener(new MessageAdapter.OnActionMessageListener() {
            @Override
            public void onClickAvatar(User user) {
                openAccountFragment(user);
            }

            @Override
            public void onLongClickMessage(Long messageId) {

            }
        });

        binding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = binding.edtMessage.getText().toString();

                if(content != null || content.length() > 0){
                    mViewModel.sendMessage(new MessageRequest(userId, contactId, content));
                }
            }
        });
    }

    public void openAccountFragment(User user){
        AccountFragment fragment = new AccountFragment();

// truyền userId qua Bundle
        Bundle args = new Bundle();
        args.putSerializable("USER", user);
        fragment.setArguments(args);

// mở fragment
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment) // fragment_container là id FrameLayout chứa fragment
                .addToBackStack(null)
                .commit();

    }
}