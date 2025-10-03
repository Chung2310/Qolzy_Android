package com.example.qolzy.ui.add_post.add_reels;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.app.Activity;
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

import com.example.qolzy.databinding.FragmentAddReelBinding;
import com.example.qolzy.ui.add_post.post_detail.PostDetailFragment;

import java.util.ArrayList;

public class AddReelFragment extends Fragment {

    private FragmentAddReelBinding binding;

    public static AddReelFragment newInstance() {
        return new AddReelFragment();
    }

    private final ActivityResultLauncher<Intent> pickVideoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
                if (res.getResultCode() == Activity.RESULT_OK && res.getData() != null) {
                    Intent data = res.getData();
                    ArrayList<String> uriStrings = new ArrayList<>();
                    ArrayList<Boolean> isVideos = new ArrayList<>();

                    if (data.getClipData() != null) {
                        int count = data.getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            Uri videoUri = data.getClipData().getItemAt(i).getUri();
                            uriStrings.add(videoUri.toString());
                            isVideos.add(true); // vì toàn video
                        }
                    } else if (data.getData() != null) {
                        Uri videoUri = data.getData();
                        uriStrings.add(videoUri.toString());
                        isVideos.add(true);
                    }

                    if (!uriStrings.isEmpty()) {
                        PostDetailFragment frag = PostDetailFragment.newInstance(uriStrings, isVideos);
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(((ViewGroup) requireActivity()
                                        .findViewById(android.R.id.content)).getId(), frag)
                                .addToBackStack(null)
                                .commit();
                    }
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddReelBinding.inflate(inflater, container, false);

        binding.btnChooseVideo.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // cho phép chọn nhiều video
            pickVideoLauncher.launch(intent);
        });

        binding.btnRecord.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Mở màn hình quay video (TODO)", Toast.LENGTH_SHORT).show();
            // TODO: implement video recorder (CameraX/MediaRecorder)
        });

        return binding.getRoot();
    }
}
