package com.example.qolzy.ui.setting;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.qolzy.R;
import com.example.qolzy.activity.AuthActivity;
import com.example.qolzy.activity.MainActivity;
import com.example.qolzy.data.repository.UserRepository;
import com.example.qolzy.data.repository.UserSearchRepository;
import com.example.qolzy.databinding.FragmentSettingBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;

public class SettingFragment extends Fragment {

    private SettingViewModel mViewModel;
    private FragmentSettingBinding binding;
    private UserRepository userRepository;
    private UserSearchRepository userSearchRepository;

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        userRepository = new UserRepository(getContext());
        userSearchRepository = new UserSearchRepository(getContext());
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SettingViewModel.class);

        binding.btnLogout.setOnClickListener(view -> {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            GoogleSignInClient googleClient = GoogleSignIn.getClient(requireActivity(), gso);

            googleClient.signOut().addOnCompleteListener(task -> {
                // Firebase sign out
                FirebaseAuth.getInstance().signOut();
                userRepository.clearUser();
                userSearchRepository.clear();
                // Chuyển sang AuthActivity
                Intent intent = new Intent(requireActivity(), AuthActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                requireActivity().finish();
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