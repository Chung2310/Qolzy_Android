package com.example.qolzy.ui.add_post.add_story;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.qolzy.databinding.FragmentAddStoryBinding;
import com.example.qolzy.ui.add_post.post_detail.PostDetailFragment;

import java.util.ArrayList;

public class AddStoryFragment extends Fragment {

    private AddStoryViewModel mViewModel;
    private FragmentAddStoryBinding binding;

    private final ActivityResultLauncher<Intent> pickLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
                if (res.getResultCode() == requireActivity().RESULT_OK && res.getData() != null) {
                    Uri uri = res.getData().getData();

                    if (uri != null) {
                        // Story chỉ chọn ảnh
                        ArrayList<String> uris = new ArrayList<>();
                        uris.add(uri.toString());

                        ArrayList<Boolean> isVideos = new ArrayList<>();
                        isVideos.add(false); // luôn false vì chỉ là ảnh

                        // Mở PostDetailFragment với 1 ảnh
                        PostDetailFragment frag = PostDetailFragment.newInstance(uris, isVideos);
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(((ViewGroup) requireActivity().findViewById(android.R.id.content)).getId(), frag)
                                .addToBackStack(null)
                                .commit();
                    }
                }
            });

    public static AddStoryFragment newInstance() {
        return new AddStoryFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddStoryBinding.inflate(inflater, container, false);

        // chỉ chọn ảnh từ gallery
        binding.btnChooseFromGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*"); // chỉ cho phép ảnh
            pickLauncher.launch(intent);
        });

        // TODO: mở camera để chụp story
        binding.btnCamera.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Mở camera (TODO implement)", Toast.LENGTH_SHORT).show();
        });

        return binding.getRoot();
    }
}
