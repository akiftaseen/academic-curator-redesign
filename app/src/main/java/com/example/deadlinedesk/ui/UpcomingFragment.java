package com.example.deadlinedesk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deadlinedesk.R;
import com.example.deadlinedesk.data.Deadline;
import com.google.android.material.snackbar.Snackbar;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UpcomingFragment extends Fragment {

    private DeadlineViewModel deadlineViewModel;
    private UpcomingGroupedAdapter activeAdapter;
    private DeadlineAdapter completedAdapter;
    private int currentFilterPosition = 0;
    private List<Deadline> currentDeadlines = new ArrayList<>();

    // New filter states
    private String selectedPriority = "All";
    private String selectedModule = "All";
    private String selectedCompleted = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upcoming, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_upcoming);
        RecyclerView completedRecyclerView = view.findViewById(R.id.recycler_view_completed);
        TextView completedHeader = view.findViewById(R.id.tv_completed_header);
        View btnEmptyAdd = view.findViewById(R.id.btn_empty_add);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        completedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        completedRecyclerView.setHasFixedSize(true);

        activeAdapter = new UpcomingGroupedAdapter(getContext());
        completedAdapter = new DeadlineAdapter(getContext());
        recyclerView.setAdapter(activeAdapter);
        completedRecyclerView.setAdapter(completedAdapter);

        deadlineViewModel = new ViewModelProvider(this).get(DeadlineViewModel.class);
        deadlineViewModel.getUpcomingDeadlines(System.currentTimeMillis()).observe(getViewLifecycleOwner(), deadlines -> {
            if (deadlines != null) {
                currentDeadlines = deadlines;
                setupFilters(view, completedHeader);
                applyNewFilters(completedHeader, view);
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            private final Drawable icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete);
            private final ColorDrawable background = new ColorDrawable(Color.parseColor("#FF5252"));

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (!activeAdapter.isTaskPosition(viewHolder.getAdapterPosition())) {
                    return makeMovementFlags(0, 0);
                }
                return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                if (!activeAdapter.isTaskPosition(viewHolder.getAdapterPosition())) {
                    return;
                }

                View itemView = viewHolder.itemView;
                int backgroundCornerOffset = 20;

                if (dX > 0) {
                    background.setBounds(itemView.getLeft(), itemView.getTop(),
                            itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
                } else if (dX < 0) {
                    background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                            itemView.getTop(), itemView.getRight(), itemView.getBottom());
                } else {
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
                Deadline deletedDeadline = activeAdapter.getDeadlineAtAdapterPosition(viewHolder.getAdapterPosition());
                if (deletedDeadline == null) {
                    activeAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                    return;
                }

                deadlineViewModel.delete(deletedDeadline);
                Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) Snackbar.make(recyclerView, "Assignment deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", v -> deadlineViewModel.insert(deletedDeadline)).getView();
                layout.setElevation(0);
            }
        }).attachToRecyclerView(recyclerView);

        activeAdapter.setOnItemClickListener(deadline -> {
            deadlineViewModel.update(deadline);
            applyNewFilters(completedHeader, view);
        });

        completedAdapter.setOnItemClickListener(deadline -> {
            deadlineViewModel.update(deadline);
            applyNewFilters(completedHeader, view);
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

    private List<String> buildModuleOptions() {
        List<String> modules = new ArrayList<>();
        modules.add("All");
        for (Deadline d : currentDeadlines) {
            String module = d.getModule() != null && !d.getModule().isEmpty() ? d.getModule() : "No Module";
            if (!modules.contains(module)) {
                modules.add(module);
            }
        }
        return modules;
    }

    private void setupFilters(View view, TextView completedHeader) {
        // Priority filter
        android.widget.AutoCompleteTextView spinnerPriority = view.findViewById(R.id.spinner_priority);
        if (spinnerPriority != null) {
            String[] priorityOptions = {"All", "High", "Medium", "Low"};
            android.widget.ArrayAdapter<String> priorityAdapter = new android.widget.ArrayAdapter<>(requireContext(), R.layout.item_dropdown_option, priorityOptions);
            priorityAdapter.setDropDownViewResource(R.layout.item_dropdown_option);
            spinnerPriority.setAdapter(priorityAdapter);
            spinnerPriority.setText(selectedPriority, false);
            spinnerPriority.setOnItemClickListener((parent, v, position, id) -> {
                selectedPriority = (String) parent.getItemAtPosition(position);
                applyNewFilters(completedHeader, view);
            });
        }

        // Module filter - now with actual modules from data
        android.widget.AutoCompleteTextView spinnerModule = view.findViewById(R.id.spinner_module);
        if (spinnerModule != null) {
            List<String> moduleOptions = buildModuleOptions();
            android.widget.ArrayAdapter<String> moduleAdapter = new android.widget.ArrayAdapter<>(requireContext(), R.layout.item_dropdown_option, moduleOptions);
            moduleAdapter.setDropDownViewResource(R.layout.item_dropdown_option);
            spinnerModule.setAdapter(moduleAdapter);
            spinnerModule.setText(selectedModule, false);
            spinnerModule.setOnItemClickListener((parent, v, position, id) -> {
                selectedModule = (String) parent.getItemAtPosition(position);
                applyNewFilters(completedHeader, view);
            });
        }

        // Completed filter
        android.widget.AutoCompleteTextView spinnerCompleted = view.findViewById(R.id.spinner_completed);
        if (spinnerCompleted != null) {
            String[] completedOptions = {"All", "Active", "Completed"};
            android.widget.ArrayAdapter<String> completedAdapter = new android.widget.ArrayAdapter<>(requireContext(), R.layout.item_dropdown_option, completedOptions);
            completedAdapter.setDropDownViewResource(R.layout.item_dropdown_option);
            spinnerCompleted.setAdapter(completedAdapter);
            spinnerCompleted.setText(selectedCompleted, false);
            spinnerCompleted.setOnItemClickListener((parent, v, position, id) -> {
                selectedCompleted = (String) parent.getItemAtPosition(position);
                applyNewFilters(completedHeader, view);
            });
        }
    }

    private void applyNewFilters(TextView completedHeader, View view) {
        List<Deadline> activeFiltered = new ArrayList<>();
        List<Deadline> completedFiltered = new ArrayList<>();

        for (Deadline d : currentDeadlines) {
            boolean passesFilter = true;

            // Priority filter
            if (!"All".equals(selectedPriority)) {
                passesFilter = selectedPriority.equals(d.getPriority());
            }

            // Module filter
            if (passesFilter && !"All".equals(selectedModule)) {
                String module = d.getModule() != null && !d.getModule().isEmpty() ? d.getModule() : "No Module";
                passesFilter = selectedModule.equals(module);
            }

            // Completed filter
            if (passesFilter) {
                if ("Active".equals(selectedCompleted)) {
                    passesFilter = !d.isDone();
                } else if ("Completed".equals(selectedCompleted)) {
                    passesFilter = d.isDone();
                }
            }


            if (passesFilter) {
                if (d.isDone()) {
                    completedFiltered.add(d);
                } else {
                    activeFiltered.add(d);
                }
            }
        }

        Collections.sort(activeFiltered, Comparator.comparingLong(Deadline::getDueDate));
        activeAdapter.setRows(buildGroupedRows(activeFiltered));
        completedAdapter.setDeadlines(completedFiltered);
        completedHeader.setVisibility(completedFiltered.isEmpty() ? View.GONE : View.VISIBLE);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_upcoming);
        RecyclerView completedRecyclerView = view.findViewById(R.id.recycler_view_completed);
        View emptyState = view.findViewById(R.id.empty_state_view);
        if (emptyState != null && recyclerView != null) {
            boolean isEmpty = activeFiltered.isEmpty() && completedFiltered.isEmpty();
            emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            completedRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private List<UpcomingGroupedAdapter.Row> buildGroupedRows(List<Deadline> deadlines) {
        List<UpcomingGroupedAdapter.Row> rows = new ArrayList<>();
        if (deadlines.isEmpty()) {
            return rows;
        }

        LinkedHashMap<Long, List<Deadline>> grouped = new LinkedHashMap<>();
        Calendar dayCalendar = Calendar.getInstance();

        for (Deadline deadline : deadlines) {
            dayCalendar.setTimeInMillis(deadline.getDueDate());
            dayCalendar.set(Calendar.HOUR_OF_DAY, 0);
            dayCalendar.set(Calendar.MINUTE, 0);
            dayCalendar.set(Calendar.SECOND, 0);
            dayCalendar.set(Calendar.MILLISECOND, 0);
            long dayStart = dayCalendar.getTimeInMillis();

            if (!grouped.containsKey(dayStart)) {
                grouped.put(dayStart, new ArrayList<>());
            }
            grouped.get(dayStart).add(deadline);
        }

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        long todayStart = today.getTimeInMillis();

        for (Map.Entry<Long, List<Deadline>> entry : grouped.entrySet()) {
            String heading = entry.getKey() == todayStart
                    ? getString(R.string.go_to_today)
                    : dayFormat.format(entry.getKey());
            rows.add(UpcomingGroupedAdapter.Row.header(heading));
            for (Deadline deadline : entry.getValue()) {
                rows.add(UpcomingGroupedAdapter.Row.item(deadline));
            }
        }

        return rows;
    }
}
