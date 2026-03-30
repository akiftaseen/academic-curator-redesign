package com.example.deadlinedesk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
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
import android.view.View;


import com.example.deadlinedesk.R;
import com.example.deadlinedesk.data.Deadline;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private DeadlineViewModel deadlineViewModel;
    private DeadlineAdapter adapter;
    private TextView tvCalendarHeader, tvAgendaDate, tvTaskCount;
    private CalendarView calendarView;
    private Calendar selectedCalendar;
    private Calendar visibleMonthCalendar;
    private LiveData<List<Deadline>> selectedDayDeadlines;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_agenda);
        tvCalendarHeader = view.findViewById(R.id.tv_calendar_header);
        tvAgendaDate = view.findViewById(R.id.tv_agenda_date);
        tvTaskCount = view.findViewById(R.id.tv_task_count);
        ImageButton btnPrev = view.findViewById(R.id.btn_prev_month);
        View btnGoToday = view.findViewById(R.id.btn_go_today);
        ImageButton btnNext = view.findViewById(R.id.btn_next_month);
        View btnEmptyAdd = view.findViewById(R.id.btn_empty_add);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        adapter = new DeadlineAdapter(getContext());
        recyclerView.setAdapter(adapter);

        deadlineViewModel = new ViewModelProvider(this).get(DeadlineViewModel.class);

        
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

        selectedCalendar = Calendar.getInstance();
        visibleMonthCalendar = Calendar.getInstance();

        long now = System.currentTimeMillis();
        selectedCalendar.setTimeInMillis(now);
        visibleMonthCalendar.setTimeInMillis(now);
        visibleMonthCalendar.set(Calendar.DAY_OF_MONTH, 1);
        updateHeader(false);
        updateAgendaDateLabel(false);

        // Load today's agenda by default
        loadAgendaForDate(now);

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedCalendar.set(year, month, dayOfMonth);
            visibleMonthCalendar.set(year, month, 1);
            updateHeader(true);
            updateAgendaDateLabel(true);
            loadAgendaForDate(selectedCalendar.getTimeInMillis());
        });

        btnPrev.setOnClickListener(v -> {
            navigateMonth(-1);
        });

        btnGoToday.setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();
            selectedCalendar.setTimeInMillis(currentTime);
            visibleMonthCalendar.setTimeInMillis(currentTime);
            visibleMonthCalendar.set(Calendar.DAY_OF_MONTH, 1);
            calendarView.setDate(currentTime, true, true);
            updateHeader(true);
            updateAgendaDateLabel(true);
            loadAgendaForDate(currentTime);
        });

        btnNext.setOnClickListener(v -> {
            navigateMonth(1);
        });

        applyPressFeedback(btnPrev);
        applyPressFeedback(btnGoToday);
        applyPressFeedback(btnNext);

        if (btnEmptyAdd != null) {
            View tip2 = view.findViewById(R.id.tip_2);
            if (tip2 != null) {
                ((android.widget.TextView) tip2.findViewById(R.id.tip_title)).setText("Recommended Reading");
                ((android.widget.TextView) tip2.findViewById(R.id.tip_desc)).setText("Check out \"The Art of Focus\" in your curator library to boost productivity.");
            }
            applyPressFeedback(btnEmptyAdd);
            btnEmptyAdd.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AddEditDeadlineActivity.class);
                startActivity(intent);
            });
        }

        return view;
    }

    private void navigateMonth(int deltaMonth) {
        visibleMonthCalendar.add(Calendar.MONTH, deltaMonth);

        int maxDayInTargetMonth = visibleMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int adjustedDay = Math.min(selectedCalendar.get(Calendar.DAY_OF_MONTH), maxDayInTargetMonth);

        selectedCalendar.set(Calendar.YEAR, visibleMonthCalendar.get(Calendar.YEAR));
        selectedCalendar.set(Calendar.MONTH, visibleMonthCalendar.get(Calendar.MONTH));
        selectedCalendar.set(Calendar.DAY_OF_MONTH, adjustedDay);

        calendarView.setDate(selectedCalendar.getTimeInMillis(), true, true);
        updateHeader(true);
        updateAgendaDateLabel(true);
        loadAgendaForDate(selectedCalendar.getTimeInMillis());
    }

    private void updateHeader(boolean animated) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String newText = sdf.format(visibleMonthCalendar.getTime());
        if (animated) {
            animateTextSwap(tvCalendarHeader, newText);
        } else {
            tvCalendarHeader.setText(newText);
        }
    }

    private void updateAgendaDateLabel(boolean animated) {
        String agendaText;
        if (isToday(selectedCalendar)) {
            agendaText = getString(R.string.today_deadlines);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd", Locale.getDefault());
            agendaText = sdf.format(selectedCalendar.getTime()) + "'s Deadlines";
        }

        if (animated) {
            animateTextSwap(tvAgendaDate, agendaText);
        } else {
            tvAgendaDate.setText(agendaText);
        }
    }

    private boolean isToday(Calendar calendar) {
        Calendar today = Calendar.getInstance();
        return today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR);
    }

    private void animateTextSwap(TextView textView, String newText) {
        if (newText.contentEquals(textView.getText())) {
            return;
        }

        textView.animate().cancel();
        textView.animate()
                .alpha(0f)
                .translationY(-8f)
                .setDuration(110)
                .withEndAction(() -> {
                    textView.setText(newText);
                    textView.setTranslationY(8f);
                    textView.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(130)
                            .start();
                })
                .start();
    }

    private void applyPressFeedback(View view) {
        if (view == null) {
            return;
        }
        view.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(140).start();
                    break;
                default:
                    break;
            }
            return false;
        });
    }

    private void loadAgendaForDate(long timeInMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = cal.getTimeInMillis();

        if (selectedDayDeadlines != null) {
            selectedDayDeadlines.removeObservers(getViewLifecycleOwner());
        }

        selectedDayDeadlines = deadlineViewModel.getDeadlinesByDate(startOfDay, endOfDay);
        selectedDayDeadlines.observe(getViewLifecycleOwner(), deadlines -> {
            adapter.setDeadlines(deadlines);
            
            int pendingCount = 0;
            if (deadlines != null) {
                for (Deadline d : deadlines) {
                    if (!d.isDone()) pendingCount++;
                }
            }
            tvTaskCount.setText(getResources().getQuantityString(R.plurals.tasks_remaining_count, pendingCount, pendingCount));
            
            View view = getView();
            if (view != null) {
                View emptyState = view.findViewById(R.id.empty_state_view);
                RecyclerView recyclerView = view.findViewById(R.id.recycler_view_agenda);
                
                if (emptyState != null && recyclerView != null) {
                    boolean isEmpty = deadlines == null || deadlines.isEmpty();
                    emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                }
            }
        });
    }
}
