package com.example.qolzy.ui.account;

import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.qolzy.R;
import com.example.qolzy.activity.AuthActivity;
import com.example.qolzy.databinding.FragmentAccountBinding;
import com.example.qolzy.data.model.User;
import com.example.qolzy.data.repository.UserRepository;
import com.example.qolzy.util.Utils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class AccountFragment extends Fragment {

    private AccountViewModel mViewModel;
    private FragmentAccountBinding binding;

    private static final int REQUEST_CODE_PICK_IMAGE = 1001;

    private String currentUploadMode = "";
    private UserRepository userRepository;
    private User user;
    private FirebaseAuth mAuth;

    public static AccountFragment newInstance() {
        return new AccountFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        userRepository = new UserRepository(getContext());
        mAuth = FirebaseAuth.getInstance();

        if (getArguments() != null) {
            user = (User) getArguments().getSerializable("USER");
            binding.btnEditProfile.setVisibility(View.INVISIBLE);
            binding.btnShareProfile.setVisibility(View.INVISIBLE);
        } else {
            user = userRepository.getUser();
        }

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        showDataUser();
        setupViewPager();
        events();
    }

    private void setupViewPager() {
        AccountPagerAdapter adapter = new AccountPagerAdapter(requireActivity(), user);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Bài viết");
                            break;
                        case 1:
                            tab.setText("Story");
                            break;
                        case 2:
                            tab.setText("Reels");
                            break;
                    }
                }).attach();

        binding.viewPager.setOffscreenPageLimit(3);
    }

    public void events() {
        binding.btnSignOut.setOnClickListener(v ->{
            userRepository.clearUser();
            FirebaseAuth.getInstance().signOut();
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(
                    getContext(),
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            );
            googleSignInClient.signOut().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("LOGOUT", "Đăng xuất Google thành công");

                    Intent intent = new Intent(getContext(), AuthActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
        });
        binding.imgProfileAvatar.setOnClickListener(v -> {
            showFullImageDialog(getContext(), "avatar");
        });
    }

    public void showDataUser() {
        if (user == null) {
            Log.e("AccountFragment", "User is null, cannot show data");
            return;
        }

        String newUrl = Utils.BASE_URL.replace("/api/", "/");
        String postAvatarUrl = null;

        if (user.getAvatarUrl() != null) {
            postAvatarUrl = user.getAvatarUrl().contains("https")
                    ? user.getAvatarUrl()
                    : newUrl + "avatar/" + user.getAvatarUrl();
        }

        Glide.with(getContext())
                .load(postAvatarUrl)
                .placeholder(R.drawable.ic_android_black_24dp)
                .error(R.drawable.user)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(binding.imgProfileAvatar);

        String displayName = (user.getFirstName() == null)
                ? user.getLastName()
                : user.getFirstName() + " " + user.getLastName();

        binding.tvDisplayName.setText(displayName != null ? displayName : "Unknown");
        binding.tvUsernameToolbar.setText(user.getUserName() != null ? user.getUserName() : "username");
        binding.tvPostsCount.setText(String.valueOf(user.getPostCount()));
        binding.tvFollowersCount.setText(String.valueOf(user.getFollowersCount() != null ? user.getFollowersCount() : 0));
        binding.tvFollowingCount.setText(String.valueOf(user.getFollowingCount() != null ? user.getFollowingCount() : 0));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                // 🔥 Bạn tự viết gọi viewModel.uploadImage
                // viewModel.uploadImage(requireContext(), imageUri, currentUploadMode);
            }
        }
    }

    public void showFullImageDialog(Context context, String mode) {
        if (user == null || user.getAvatarUrl() == null) return;

        String imageUrl = Utils.BASE_URL.replace("/api/", "/");
        if (mode.equals("avatar")) {
            imageUrl = user.getAvatarUrl().contains("https")
                    ? user.getAvatarUrl()
                    : imageUrl + "avatar/" + user.getAvatarUrl();
        }

        Log.d("AvatarUrl", "Image URL: " + imageUrl);

        Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_full_image);

        ImageView imageView = dialog.findViewById(R.id.imageDialog);

        Glide.with(context)
                .load(imageUrl)
                .into(imageView);

        imageView.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
