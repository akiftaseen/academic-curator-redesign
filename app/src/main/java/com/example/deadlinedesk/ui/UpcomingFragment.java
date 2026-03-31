package com.example.deadlinedesk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;


import com.example.deadlinedesk.R;

public class UpcomingFragment extends Fragment {

    private DeadlineViewModel deadlineViewModel;
    private int currentFilterPosition = 0;
    private java.util.List<com.example.deadlinedesk.data.Deadline> currentDeadlines = new java.util.ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upcoming, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_upcoming);
        View btnEmptyAdd = view.findViewById(R.id.btn_empty_add);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        final DeadlineAdapter adapter = new DeadlineAdapter(getContext());
        recyclerView.setAdapter(adapter);

        deadlineViewModel = new ViewModelProvider(this).get(DeadlineViewModel.class);
        deadlineViewModel.getUpcomingDeadlines(System.currentTimeMillis()).observe(getViewLifecycleOwner(), deadlines -> {
            if (deadlines != null) {
                currentDeadlines = deadlines;
                applyFilter(adapter, view, currentFilterPosition);
            }
        });

        android.widget.AutoCompleteTextView spinnerFilter = view.findViewById(R.id.spinner_filter);
        if (spinnerFilter != null) {
            String[] filterOptions = getResources().getStringArray(R.array.filter_options);
            android.widget.ArrayAdapter<String> arrayAdapter = new android.widget.ArrayAdapter<>(requireContext(), R.layout.item_dropdown_option, filterOptions);
            arrayAdapter.setDropDownViewResource(R.layout.item_dropdown_option);
            spinnerFilter.setAdapter(arrayAdapter);
            spinnerFilter.setText(filterOptions[currentFilterPosition], false);
            spinnerFilter.setOnItemClickListener((parent, v, position, id) -> {
                String selectedText = (String) parent.getItemAtPosition(position);
                for (int i = 0; i < filterOptions.length; i++) {
                    if (filterOptions[i].equals(selectedText)) {
                        currentFilterPosition = i;
                        break;
                    }
                }
                applyFilter(adapter, view, currentFilterPosition);
            });
        }

        
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            private Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_delete);
            private ColorDrawable background = new ColorDrawable(Color.parseColor("#FF5252"));

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                View itemView = viewHolder.itemView;
                int backgroundCornerOffset = 20;

                if (dX > 0) { // Swiping to the right
                    background.setBounds(itemView.getLeft(), itemView.getTop(),
                            itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
                } else if (dX < 0) { // Swiping to the left
                    background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                            itemView.getTop(), itemView.getRight(), itemView.getBottom());
                } else { // view is unSwiped
                    background.setBounds(0, 0, 0, 0);
                }
                background.draw(c);

                if (icon != null) {
                    int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                    int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                    int iconBottom = iconTop + icon.getIntrinsicHeight();

                    if (dX > 0) {
                        int iconLeft = itemView.getLeft() + iconMargin;
                        int iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        icon.draw(c);
                    } else if (dX < 0) {
                        int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                        int iconRight = itemView.getRight() - iconMargin;
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        icon.draw(c);
                    }
                }
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                com.example.deadlinedesk.data.Deadline deletedDeadline = adapter.getDeadlineAt(viewHolder.getAdapterPosition());
                deadlineViewModel.delete(deletedDeadline);
                Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) Snackbar.make(recyclerView, "Assignment deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> deadlineViewModel.insert(deletedDeadline)).getView();
                layout.setElevation(0);
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(deadline -> {

            deadlineViewModel.update(deadline);
        });

        if (btnEmptyAdd != null) {
            View tip2 = view.findViewById(R.id.tip_2);
            if (tip2 != null) {
                ((android.widget.TextView) tip2.findViewById(R.id.tip_title)).setText("Recommended Reading");
                ((android.widget.TextView) tip2.findViewById(R.id.tip_desc)).setText("Check out \"The Art of Focus\" in your curator library to boost productivity.");
            }
            btnEmptyAdd.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AddEditDeadlineActivity.class);
                startActivity(intent);
            });
        }

        return view;
    }

    private void applyFilter(DeadlineAdapter adapter, View view, int position) {
        java.util.List<com.example.deadlinedesk.data.Deadline> filtered = new java.util.ArrayList<>();
        for (com.example.deadlinedesk.data.Deadline d : currentDeadlines) {
            if (position == 1) { // Urgent Only (High Priority)
                if ("High".equals(d.getPriority())) filtered.add(d);
            } else if (position == 2) { // Hide Completed
                if (!d.isDone()) filtered.add(d);
            } else {
                filtered.add(d); // All
            }
        }
        adapter.setDeadlines(filtered);
        
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_upcoming);
        View emptyState = view.findViewById(R.id.empty_state_view);
        if (emptyState != null && recyclerView != null) {
            boolean isEmpty = filtered.isEmpty();
            emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }
}
