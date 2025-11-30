package com.example.qolzy.ui.account;

import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.qolzy.R;
import com.example.qolzy.activity.AuthActivity;
import com.example.qolzy.databinding.FragmentAccountBinding;
import com.example.qolzy.data.model.User;
import com.example.qolzy.data.repository.UserRepository;
import com.example.qolzy.ui.account.edit_profile.EditProfileFragment;
import com.example.qolzy.ui.follow.FollowFragment;
import com.example.qolzy.ui.home.HomeViewModel;
import com.example.qolzy.ui.message.DetailMessageFragment;
import com.example.qolzy.ui.setting.SettingFragment;
import com.example.qolzy.util.Utils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.tabs.TabLayoutMediator;

public class AccountFragment extends Fragment {

    private AccountViewModel mViewModel;
    private HomeViewModel homeViewModel;
    private FragmentAccountBinding binding;

    private static final int REQUEST_CODE_PICK_IMAGE = 1001;

    private String currentUploadMode = "";
    private UserRepository userRepository;
    private User user; // currently displayed user (may be null until loaded)
    private Long targetUserId; // id của user đang xem (có thể là chính chủ hoặc người khác)
    private Long currentUserId; // id của user đăng nhập
    private FirebaseAuth mAuth;
    private Boolean followByCurrentUser;

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

        // Lấy current user id từ repository (được dùng để so sánh)
        currentUserId = (userRepository != null) ? Long.valueOf(userRepository.getUserId()) : null;

        // Đọc arguments: hỗ trợ 2 kiểu truyền: USER object hoặc USER_ID
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey("USER")) {
                Object obj = args.getSerializable("USER");
                if (obj instanceof User) {
                    user = (User) obj;
                    if (user.getId() != null) {
                        targetUserId = user.getId();
                    }
                }
            }
            if (args.containsKey("USER_ID")) {
                // ưu tiên USER_ID nếu có
                long id = args.getLong("USER_ID", (currentUserId != null) ? currentUserId : -1L);
                if (id >= 0) targetUserId = id;
            }
            if (args.containsKey("followByCurrentUser")) {
                followByCurrentUser = args.getBoolean("followByCurrentUser");
            }
        }

        // Nếu không có arguments, coi là mở profile chính chủ
        if (targetUserId == null) {
            targetUserId = currentUserId;
        }

        // Set UI tạm (progress hoặc content sẽ set thật sau khi load)
        binding.layoutContent.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // load dữ liệu user theo targetUserId
        loadUserAndSetup();

        homeViewModel.getMessageLiveData().observe(getViewLifecycleOwner(), message ->{
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUserAndSetup() {
        ProgressBar progressBar = binding.progressBar;
        View layoutContent = binding.layoutContent;

        // Nếu đã có user (được truyền qua args), nhưng user.getId() khác targetUserId thì cần reload
        if (user != null && user.getId() != null && user.getId().equals(targetUserId)) {
            // có sẵn, hiển thị luôn
            progressBar.setVisibility(View.GONE);
            layoutContent.setVisibility(View.VISIBLE);

            showAppropriateLayout();
            showDataUser();
            setupViewPager();
            events();
        } else {
            // load từ API theo targetUserId
            progressBar.setVisibility(View.VISIBLE);
            layoutContent.setVisibility(View.GONE);

            if (targetUserId == null) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Không xác định được user để hiển thị", Toast.LENGTH_SHORT).show();
                return;
            }

            mViewModel.getUserDetail(targetUserId);
            // Nếu observer đã tồn tại từ lần trước, remove để tránh multiple observers (an toàn)
            mViewModel.getUserMutableLiveData().removeObservers(getViewLifecycleOwner());
            mViewModel.getUserMutableLiveData().observe(getViewLifecycleOwner(), response -> {
                if (response != null) {
                    user = response;
                    userRepository.saveUser(user);

                    progressBar.setVisibility(View.GONE);
                    layoutContent.setVisibility(View.VISIBLE);

                    showAppropriateLayout();
                    showDataUser();
                    setupViewPager();
                    events();
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Không tải được thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Hiển thị layout phù hợp: nếu targetUserId == currentUserId -> layout chính chủ
     * Ngược lại -> layout người khác
     */
    private void showAppropriateLayout() {
        boolean isCurrentUser = (currentUserId != null && targetUserId != null && currentUserId.equals(targetUserId));

        if (isCurrentUser) {
            binding.linearLayoutCurrentUser.setVisibility(View.VISIBLE);
            binding.linearLayoutOtherUser.setVisibility(View.GONE);
        } else {
            binding.linearLayoutCurrentUser.setVisibility(View.GONE);
            binding.linearLayoutOtherUser.setVisibility(View.VISIBLE);
        }
    }

    private void setupViewPager() {
        // Adapter nên xử lý user có thể null (nếu chưa load xong)
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
        // Click vào avatar để xem ảnh fullscreen
        binding.imgProfileAvatar.setOnClickListener(v -> {
            showFullImageDialog(getContext(), "avatar");
        });

        binding.btnDetailMessage.setOnClickListener(view -> {
            if (user == null || user.getId() == null || currentUserId == null) {
                Toast.makeText(getContext(), "Không thể tạo liên hệ", Toast.LENGTH_SHORT).show();
                return;
            }

            DetailMessageFragment fragment = new DetailMessageFragment();

            // createContact sử dụng currentUserId (người gửi) và user.getId() (người nhận)
            mViewModel.createContact(currentUserId, user.getId());

            Bundle args = new Bundle();
            args.putSerializable("contact", user);
            fragment.setArguments(args);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        binding.btnEditProfile.setOnClickListener(view -> {
            // Chỉ cho edit nếu đang xem chính chủ
            boolean isCurrentUser = (currentUserId != null && targetUserId != null && currentUserId.equals(targetUserId));
            if (!isCurrentUser) {
                Toast.makeText(getContext(), "Không thể chỉnh sửa profile người khác", Toast.LENGTH_SHORT).show();
                return;
            }

            EditProfileFragment fragment = new EditProfileFragment();

            // để EditProfile tiện thao tác, truyền user hiện tại
            Bundle args = new Bundle();
            args.putSerializable("USER", user);
            fragment.setArguments(args);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        binding.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homeViewModel.toggleFollow(currentUserId, targetUserId);
                if (followByCurrentUser){
                    binding.btnFollow.setText("Theo dõi");
                    binding.btnFollow.setTextColor(Color.BLACK);
                    binding.btnFollow.setBackgroundColor(Color.BLUE);
                    followByCurrentUser = false;
                }
                else {
                    binding.btnFollow.setText("Đã Theo dõi");
                    binding.btnFollow.setTextColor(Color.BLACK);
                    binding.btnFollow.setBackgroundColor(Color.WHITE);
                    followByCurrentUser = true;
                }
            }
        });

        binding.layoutFollower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFollowFragment();
            }
        });

        binding.layoutFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFollowFragment();
            }
        });

        binding.btnToolbarOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingFragment settingFragment = new SettingFragment();

                // để EditProfile tiện thao tác, truyền user hiện tại
                Bundle args = new Bundle();
                args.putSerializable("USER", user);
                settingFragment.setArguments(args);

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, settingFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    public void openFollowFragment(){
        FollowFragment fragment = new FollowFragment();

        // để EditProfile tiện thao tác, truyền user hiện tại
        Bundle args = new Bundle();
        args.putSerializable("USER", user);
        fragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void showDataUser() {
        if (user == null) {
            Log.e("AccountFragment", "User is null, cannot show data");
            return;
        }

        String newUrl = Utils.BASE_URL.replace("/api/", "");
        String postAvatarUrl = null;

        if (user.getAvatarUrl() != null) {
            postAvatarUrl = user.getAvatarUrl().contains("https")
                    ? user.getAvatarUrl()
                    : newUrl + user.getAvatarUrl();
        }

        Log.d("AccountFragment", postAvatarUrl);

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
        if (user.getBio() != null) {
            binding.tvUserBio.setVisibility(View.VISIBLE);
            binding.tvUserBio.setText(user.getBio());
        } else {
            binding.tvUserBio.setVisibility(View.GONE);
        }
        boolean isCurrentUser = (currentUserId != null && targetUserId != null && currentUserId.equals(targetUserId));
        if (!isCurrentUser) {
            if (followByCurrentUser != null && followByCurrentUser) {
                binding.btnFollow.setText("Đã Theo dõi");
                binding.btnFollow.setTextColor(Color.BLACK);
                binding.btnFollow.setBackgroundColor(Color.WHITE);
            } else {
                binding.btnFollow.setText("Theo dõi");
                binding.btnFollow.setTextColor(Color.BLACK);
                binding.btnFollow.setBackgroundColor(Color.BLUE);
            }
        }
    }

    public void showFullImageDialog(Context context, String mode) {
        if (user == null || user.getAvatarUrl() == null) return;

        String imageUrl = Utils.BASE_URL.replace("/api/", "");
        if (mode.equals("avatar")) {
            imageUrl = user.getAvatarUrl().contains("https")
                    ? user.getAvatarUrl()
                    : imageUrl + user.getAvatarUrl();
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
