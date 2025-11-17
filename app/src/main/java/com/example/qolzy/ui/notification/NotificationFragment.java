package com.example.qolzy.ui.notification;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.qolzy.activity.MainActivity;
import com.example.qolzy.data.model.Notification;
import com.example.qolzy.data.repository.UserRepository;
import com.example.qolzy.databinding.FragmentNotificationBinding;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private NotificationViewModel mViewModel;
    private FragmentNotificationBinding binding;
    private NotificationAdapter adapter;
    private List<Notification> notifications = new ArrayList<>();
    private int page = 0, size = 10;
    private Long userId ;
    private UserRepository userRepository;

    public static NotificationFragment newInstance() {
        return new NotificationFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationBinding.inflate(inflater, container, false);

        userRepository = new UserRepository(getContext());
        userId = (long) userRepository.getUserId();

        adapter = new NotificationAdapter(notifications, getContext());
        binding.recyclerNotification.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerNotification.setAdapter(adapter);

        binding.toolbarNotification.setNavigationOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);

        mViewModel.getNotificationsByUserId(page,size,userId);

        mViewModel.getNotificationsLiveData().observe(getViewLifecycleOwner(), response ->{
            if(response.size() > 0){
                binding.progressBar.setVisibility(View.INVISIBLE);
                adapter.updateNotifications(response);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Ẩn BottomNavigation khi vào Fragment này
        ((MainActivity) requireActivity()).setBottomNavigationVisibility(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Hiện lại khi rời khỏi Fragment
        ((MainActivity) requireActivity()).setBottomNavigationVisibility(true);
    }
}