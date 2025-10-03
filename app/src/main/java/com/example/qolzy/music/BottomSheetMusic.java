package com.example.qolzy.music;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.qolzy.databinding.LayoutBottomsheetMusicBinding;
import com.example.qolzy.ui.home.HomeViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BottomSheetMusic extends BottomSheetDialogFragment {
    private LayoutBottomsheetMusicBinding binding;
    private MusicAdapter adapter;
    private List<MusicItem> musicItems;
    private MusicViewModel viewModel;
    private int size = 20;

    public interface OnMusicSelectedListener {
        void onMusicSelected(MusicItem musicItem);
    }

    private OnMusicSelectedListener listener;

    public void setOnMusicSelectedListener(OnMusicSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutBottomsheetMusicBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(MusicViewModel.class);

        binding.rvMusicList.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MusicAdapter(new ArrayList<>());
        binding.rvMusicList.setAdapter(adapter);

        adapter.setOnMusicActionListener(new MusicAdapter.OnMusicActionListener() {
            @Override
            public void onMusicClick(MusicItem item) {
                if (listener != null){
                    listener.onMusicSelected(item);
                }
            }
        });

        binding.edtMusicTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.getMusicItems( s.toString(), size);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        viewModel.getMusicItems("", size);

        viewModel.getMusicItemsLiveData().observe(getViewLifecycleOwner(), musics ->{
            adapter.updateMusics(musics);
        });

        return binding.getRoot();
    }
}
