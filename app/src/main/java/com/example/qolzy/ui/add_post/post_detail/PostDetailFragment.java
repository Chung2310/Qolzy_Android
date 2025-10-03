package com.example.qolzy.ui.add_post.post_detail;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.qolzy.R;
import com.example.qolzy.activity.MainActivity;
import com.example.qolzy.data.model.User;
import com.example.qolzy.data.repository.UserRepository;
import com.example.qolzy.databinding.FragmentPostDetailBinding;
import com.example.qolzy.music.BottomSheetMusic;
import com.example.qolzy.music.MusicItem;

import java.util.ArrayList;

public class PostDetailFragment extends Fragment {

    private PostDetailViewModel mViewModel;
    private FragmentPostDetailBinding binding;

    private static final String ARG_URIS = "arg_uris";
    private static final String ARG_IS_VIDEOS = "arg_is_videos";
    private UserRepository userRepository;
    private ArrayList<String> uriStrings;
    private ArrayList<Boolean> isVideos;

    private View bubbleView;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private MusicItem musicItem = null;

    public static PostDetailFragment newInstance(ArrayList<String> uriStrings, ArrayList<Boolean> isVideos) {
        PostDetailFragment f = new PostDetailFragment();
        Bundle b = new Bundle();
        b.putStringArrayList(ARG_URIS, uriStrings);
        b.putSerializable(ARG_IS_VIDEOS, isVideos); // dùng Serializable cho ArrayList<Boolean>
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPostDetailBinding.inflate(inflater, container, false);
        mViewModel = new ViewModelProvider(this).get(PostDetailViewModel.class);

        userRepository = new UserRepository(requireContext());
        return binding.getRoot();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            uriStrings = getArguments().getStringArrayList(ARG_URIS);
            isVideos = (ArrayList<Boolean>) getArguments().getSerializable(ARG_IS_VIDEOS);
        }

        if (uriStrings == null || uriStrings.isEmpty()) {
            Toast.makeText(requireContext(), "Không có media nào được chọn", Toast.LENGTH_SHORT).show();
            return;
        }

        // Dùng ViewPager2 để lướt qua các ảnh/video đã chọn
        DetailMediaPagerAdapter pagerAdapter = new DetailMediaPagerAdapter(uriStrings, isVideos);
        binding.viewPagerPreview.setAdapter(pagerAdapter);
        Long userId = (long) userRepository.getUserId();
        binding.btnPublish.setOnClickListener(v -> {
            String caption = binding.edtCaptionDetail.getText().toString().trim();
            // TODO: upload uriStrings + caption lên server hoặc Firebase

            mViewModel.createPost(caption, userId, musicItem);
            // Sau khi thành công:
            requireActivity().getSupportFragmentManager().popBackStack(null, 0);
        });

        mViewModel.getPostIdLiveData().observe(getViewLifecycleOwner(), postId ->{
            mViewModel.createPostFile(requireContext(), Long.parseLong(postId), uriStrings);
        });

        mViewModel.getStatusLiveData().observe(getViewLifecycleOwner(), status -> {
            if (status == 201) {
                // Xoá bubble và mediaPlayer
                if (bubbleView != null) {
                    ViewGroup rootView = (ViewGroup) requireActivity().findViewById(android.R.id.content);
                    rootView.removeView(bubbleView);
                    bubbleView = null;
                }
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    isPlaying = false;
                }
                musicItem = null;

                // 1. Dọn sạch PostDetailFragment khỏi backstack
                requireActivity().getSupportFragmentManager().popBackStack();

                // 2. Chuyển tab bottom nav về Home
                if (requireActivity() instanceof MainActivity) {
                    ((MainActivity) requireActivity()).switchToHome();
                }
            }
        });




        mViewModel.getMessageLiveData().observe(getViewLifecycleOwner(), message ->{
            Toast.makeText(requireContext(), message +"",Toast.LENGTH_SHORT).show();
        });


        // Nút thêm nhạc
        binding.btnAddMusic.setOnClickListener(v -> {
            // TODO: mở picker chọn nhạc hoặc activity trim nhạc
            BottomSheetMusic bottomSheetMusic = new BottomSheetMusic();

            bottomSheetMusic.setOnMusicSelectedListener(new BottomSheetMusic.OnMusicSelectedListener() {
                @Override
                public void onMusicSelected(MusicItem musicItem1) {
                    musicItem = musicItem1;
                    showBubble(musicItem);
                }
            });

            bottomSheetMusic.show(getChildFragmentManager(), "BottomSheetMusic");
        });

        // Nút thêm văn bản
        binding.btnAddText.setOnClickListener(v -> {
            // TODO: mở dialog nhập văn bản, overlay lên preview
            Toast.makeText(requireContext(), "Chức năng thêm văn bản", Toast.LENGTH_SHORT).show();
        });

        // Nút thêm bộ lọc
        binding.btnAddFilter.setOnClickListener(v -> {
            // TODO: mở activity chọn filter (GPUImage/PhotoEditor)
            Toast.makeText(requireContext(), "Chức năng thêm filter", Toast.LENGTH_SHORT).show();
        });


    }

    private void showBubble(MusicItem item) {
        if (bubbleView != null) return; // chỉ cho 1 bubble

        ViewGroup rootView = (ViewGroup) requireActivity().findViewById(android.R.id.content);
        bubbleView = LayoutInflater.from(getContext()).inflate(R.layout.view_music_bubble, rootView, false);

        ImageView cover = bubbleView.findViewById(R.id.bubbleCover);
        TextView title = bubbleView.findViewById(R.id.bubbleTitle);
        TextView artist = bubbleView.findViewById(R.id.bubbleArtist);
        ImageButton playPause = bubbleView.findViewById(R.id.bubblePlayPause);
        ImageButton btnClose = bubbleView.findViewById(R.id.bubbleClose); // nút X

        Glide.with(this).load(item.getImageUrl()).into(cover);
        title.setText(item.getName());
        artist.setText(item.getArtistName());

        playPause.setOnClickListener(v -> {
            if (isPlaying) {
                if (mediaPlayer != null) {
                    mediaPlayer.pause();
                }
                playPause.setImageResource(android.R.drawable.ic_media_play);
            } else {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(item.getAudioUrl());
                        mediaPlayer.prepare();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                mediaPlayer.start();
                playPause.setImageResource(android.R.drawable.ic_media_pause);
            }
            isPlaying = !isPlaying;
        });

        // Sự kiện xóa bubble
        btnClose.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                isPlaying = false;
            }
            rootView.removeView(bubbleView);
            bubbleView = null; // reset để tạo bubble mới khi cần
        });

        // Thêm bong bóng vào màn hình
        rootView.addView(bubbleView);

        // Cho phép bong bóng kéo thả
        makeDraggable(bubbleView);
    }


    private void makeDraggable(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        v.animate()
                                .x(event.getRawX() + dX)
                                .y(event.getRawY() + dY)
                                .setDuration(0)
                                .start();
                        break;
                }
                return true;
            }
        });
    }
}
