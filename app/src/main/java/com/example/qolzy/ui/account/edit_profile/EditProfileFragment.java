package com.example.qolzy.ui.account.edit_profile;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.qolzy.R;
import com.example.qolzy.activity.MainActivity;
import com.example.qolzy.data.model.User;
import com.example.qolzy.databinding.FragmentEditProfileBinding;
import com.example.qolzy.util.Utils;

public class EditProfileFragment extends Fragment {

    private EditProfileViewModel mViewModel;
    private FragmentEditProfileBinding binding;
    private User user;
    private static final int PICK_IMAGE_REQUEST = 101;
    private Uri selectedImageUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        user = (User) getArguments().getSerializable("USER");

        showDataUser();
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);

        binding.toolbarEditProfile.setNavigationOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        binding.btnSaveProfile.setOnClickListener(v -> {
            String newName = binding.edtName.getText().toString();
            String newBio = binding.edtBio.getText().toString();

            // Username
            if (newName != null || newBio != null) {
                mViewModel.updateUserProfile(user.getId(), newName, newBio);
            }

        });

        binding.tvChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImagePicker();
                binding.tvErrorUploadImage.setVisibility(View.GONE);
            }
        });

        // Show update message
        mViewModel.getMessageLivedata().observe(getViewLifecycleOwner(), msg -> {
            Toast.makeText(getContext(), msg +"", Toast.LENGTH_SHORT).show();
        });

        mViewModel.getStatusLivedata().observe(getViewLifecycleOwner(), status ->{
            if (status == 200) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void showDataUser() {
        if (user == null) return;

        String baseUrl = Utils.BASE_URL.replace("/api/", "");
        String avatarUrl = user.getAvatarUrl() != null
                ? (user.getAvatarUrl().contains("https")
                ? user.getAvatarUrl()
                : baseUrl + user.getAvatarUrl())
                : null;

        Glide.with(requireContext())
                .load(avatarUrl)
                .placeholder(R.drawable.ic_android_black_24dp)
                .error(R.drawable.user)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(binding.imgAvatar);

        // FULL NAME
        String fullName = (user.getFirstName() == null || user.getLastName() == null)
                ? (user.getFirstName() != null ? user.getFirstName() : user.getLastName())
                : user.getFirstName() + " " + user.getLastName();

        binding.edtName.setText(fullName);
        binding.edtBio.setText(user.getBio() != null ? user.getBio() : "");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            // Hiển thị ảnh lên ImageView tạm thời
            binding.imgAvatar.setImageURI(selectedImageUri);

            // Upload lên Firebase Storage
            mViewModel.updateUserAvatar(getContext(),user.getId(), selectedImageUri.toString());
            binding.progressBar.setVisibility(View.VISIBLE);
        }
    }


    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST);
    }

    private boolean isValidUsername(String username) {
        String regex = "^(?=.{3,30}$)(?![._])(?!.*[._]{2})[a-zA-Z0-9._]+(?<![._])$";
        return username.matches(regex);
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
