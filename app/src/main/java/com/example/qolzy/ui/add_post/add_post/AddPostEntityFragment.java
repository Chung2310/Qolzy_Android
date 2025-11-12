package com.example.qolzy.ui.add_post.add_post;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.qolzy.databinding.FragmentAddPostEntityBinding;
import com.example.qolzy.ui.add_post.GalleryAdapter;
import com.example.qolzy.ui.add_post.post_detail.PostDetailFragment;

import java.util.ArrayList;
import java.util.List;

public class AddPostEntityFragment extends Fragment {
    private FragmentAddPostEntityBinding binding;
    private AddPostEntityViewModel mViewModel;

    private GalleryAdapter adapter;
    private final List<Uri> mediaUris = new ArrayList<>();
    private final List<Boolean> mediaIsVideo = new ArrayList<>();
    private final List<Uri> selectedUris = new ArrayList<>();
    private final List<Boolean> selectedIsVideos = new ArrayList<>();



    private final ActivityResultLauncher<String[]> requestMultiplePermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean granted = false;
                for (Boolean b : result.values()) {
                    if (b != null && b) {
                        granted = true;
                        break;
                    }
                }
                if (granted) loadMediaFromDevice();
                else Toast.makeText(requireContext(), "Không có quyền truy cập media", Toast.LENGTH_SHORT).show();
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddPostEntityBinding.inflate(inflater, container, false);

        binding.rvGallery.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        adapter = new GalleryAdapter(mediaUris, mediaIsVideo, (uri, isVideo, isSelected) -> {
            if (isSelected) {
                selectedUris.add(uri);
                selectedIsVideos.add(isVideo);
            } else {
                int index = selectedUris.indexOf(uri);
                if (index >= 0) {
                    selectedUris.remove(index);
                    selectedIsVideos.remove(index);
                }
            }
            // preview ảnh/video đầu tiên trong danh sách chọn
            if (!selectedUris.isEmpty()) {
                showPreview(selectedUris.get(0), selectedIsVideos.get(0));
            } else {
                binding.imgPreview.setVisibility(View.GONE);
                binding.videoPreview.setVisibility(View.GONE);
            }
        });
        binding.rvGallery.setAdapter(adapter);

        binding.btnNext.setOnClickListener(view -> {
            if (selectedUris.isEmpty()) {
                Toast.makeText(requireContext(), "Chọn ít nhất 1 ảnh hoặc video", Toast.LENGTH_SHORT).show();
                return;
            }
            // Truyền list sang PostDetailFragment
            ArrayList<String> uriStrings = new ArrayList<>();
            for (Uri u : selectedUris) uriStrings.add(u.toString());

            PostDetailFragment frag = PostDetailFragment.newInstance(uriStrings, (ArrayList<Boolean>) selectedIsVideos, "post");
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(((ViewGroup) requireActivity().findViewById(android.R.id.content)).getId(), frag)
                    .addToBackStack(null)
                    .commit();
        });

        checkPermissionAndLoad();

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AddPostEntityViewModel.class);
    }

    private void showPreview(Uri uri, boolean isVideo) {
        if (isVideo) {
            binding.imgPreview.setVisibility(View.GONE);
            binding.videoPreview.setVisibility(View.VISIBLE);
            binding.videoPreview.setVideoURI(uri);
            binding.videoPreview.start();
        } else {
            binding.videoPreview.setVisibility(View.GONE);
            binding.imgPreview.setVisibility(View.VISIBLE);
            binding.imgPreview.setImageURI(uri);
        }
    }

    private void checkPermissionAndLoad() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            List<String> permissions = new ArrayList<>();
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO);

            List<String> needRequest = new ArrayList<>();
            for (String perm : permissions) {
                if (ContextCompat.checkSelfPermission(requireContext(), perm) != PackageManager.PERMISSION_GRANTED) {
                    needRequest.add(perm);
                }
            }

            if (needRequest.isEmpty()) {
                loadMediaFromDevice();
            } else {
                requestMultiplePermissionsLauncher.launch(needRequest.toArray(new String[0]));
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                loadMediaFromDevice();
            } else {
                requestMultiplePermissionsLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
            }
        }
    }

    private void loadMediaFromDevice() {
        mediaUris.clear();
        mediaIsVideo.clear();

        // Load images
        String[] projImage = new String[]{MediaStore.Images.Media._ID};
        Cursor cursorImg = requireContext().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projImage,
                null,
                null,
                MediaStore.Images.Media.DATE_ADDED + " DESC"
        );
        if (cursorImg != null) {
            int idIdx = cursorImg.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            while (cursorImg.moveToNext()) {
                long id = cursorImg.getLong(idIdx);
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                mediaUris.add(contentUri);
                mediaIsVideo.add(false);
            }
            cursorImg.close();
        }

        // Load videos
        String[] projVideo = new String[]{MediaStore.Video.Media._ID};
        Cursor cursorVid = requireContext().getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projVideo,
                null,
                null,
                MediaStore.Video.Media.DATE_ADDED + " DESC"
        );
        if (cursorVid != null) {
            int idIdx = cursorVid.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            while (cursorVid.moveToNext()) {
                long id = cursorVid.getLong(idIdx);
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                mediaUris.add(contentUri);
                mediaIsVideo.add(true);
            }
            cursorVid.close();
        }

        adapter.notifyDataSetChanged();
    }
}
