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

import java.io.IOException;
import java.util.ArrayList;

public class AddStoryFragment extends Fragment {

    private AddStoryViewModel mViewModel;
    private FragmentAddStoryBinding binding;

    private final ActivityResultLauncher<Intent> pickLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {

                    Uri uri = result.getData().getData();
                    if (uri == null) {
                        Toast.makeText(requireContext(), "Không chọn được video", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 👉 KIỂM TRA THỜI LƯỢNG VIDEO
                    long durationMs = 0;
                    try {
                        durationMs = getVideoDuration(uri);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    long maxDuration = 10 * 1000; // 20s

                    if (durationMs == -1) {
                        Toast.makeText(requireContext(), "Không đọc được thời lượng video", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (durationMs > maxDuration) {
                        Toast.makeText(requireContext(), "Chỉ cho phép video dưới 10 giây!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ---- Video hợp lệ ----
                    ArrayList<String> uris = new ArrayList<>();
                    uris.add(uri.toString());

                    ArrayList<Boolean> isVideos = new ArrayList<>();
                    isVideos.add(true);

                    PostDetailFragment frag = PostDetailFragment.newInstance(uris, isVideos, "story");
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(((ViewGroup) requireActivity().findViewById(android.R.id.content)).getId(), frag)
                            .addToBackStack(null)
                            .commit();
                }
            });




    public static AddStoryFragment newInstance() {
        return new AddStoryFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddStoryBinding.inflate(inflater, container, false);

        // chỉ chọn video từ gallery
        binding.btnChooseFromGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            intent.setType("video/*"); // chỉ cho phép video
            pickLauncher.launch(intent);
        });


        return binding.getRoot();
    }

    private long getVideoDuration(Uri videoUri) throws IOException {
        android.media.MediaMetadataRetriever retriever = new android.media.MediaMetadataRetriever();
        try {
            retriever.setDataSource(requireContext(), videoUri);
            String time = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
            return Long.parseLong(time); // mili giây
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            retriever.release();
        }
    }

}
