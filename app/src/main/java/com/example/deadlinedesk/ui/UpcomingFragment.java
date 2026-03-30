package com.example.deadlinedesk.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deadlinedesk.R;

public class UpcomingFragment extends Fragment {

    private DeadlineViewModel deadlineViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upcoming, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_upcoming);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        final DeadlineAdapter adapter = new DeadlineAdapter(getContext());
        recyclerView.setAdapter(adapter);

        deadlineViewModel = new ViewModelProvider(this).get(DeadlineViewModel.class);
        deadlineViewModel.getUpcomingDeadlines(System.currentTimeMillis()).observe(getViewLifecycleOwner(), deadlines -> {
            adapter.setDeadlines(deadlines);
            
            View emptyState = view.findViewById(R.id.empty_state_view);
            if (emptyState != null) {
                emptyState.setVisibility(deadlines == null || deadlines.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(deadlines == null || deadlines.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        adapter.setOnItemClickListener(deadline -> {
            deadlineViewModel.update(deadline);
        });

        return view;
    }
}
