package com.example.deadlinedesk.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.deadlinedesk.R;

import java.util.Calendar;

public class CalendarFragment extends Fragment {

    private DeadlineViewModel deadlineViewModel;
    private DeadlineAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        CalendarView calendarView = view.findViewById(R.id.calendarView);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_agenda);
        TextView tvAgendaDate = view.findViewById(R.id.tv_agenda_date);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        adapter = new DeadlineAdapter(getContext());
        recyclerView.setAdapter(adapter);

        deadlineViewModel = new ViewModelProvider(this).get(DeadlineViewModel.class);

        adapter.setOnItemClickListener(deadline -> {
            deadlineViewModel.update(deadline);
        });

        // Load today's agenda by default
        loadAgendaForDate(System.currentTimeMillis());

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            loadAgendaForDate(calendar.getTimeInMillis());
            tvAgendaDate.setText("Agenda for " + dayOfMonth + "/" + (month + 1) + "/" + year);
        });

        return view;
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

        deadlineViewModel.getDeadlinesByDate(startOfDay, endOfDay).observe(getViewLifecycleOwner(), deadlines -> {
            adapter.setDeadlines(deadlines);
            
            View view = getView();
            if (view != null) {
                View emptyState = view.findViewById(R.id.empty_state_view);
                RecyclerView recyclerView = view.findViewById(R.id.recycler_view_agenda);
                
                if (emptyState != null && recyclerView != null) {
                    emptyState.setVisibility(deadlines == null || deadlines.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(deadlines == null || deadlines.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }
        });
    }
}
