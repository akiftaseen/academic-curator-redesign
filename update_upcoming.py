import re

with open("app/src/main/java/com/example/deadlinedesk/ui/UpcomingFragment.java", "r") as f:
    text = f.read()

import_statement = "import androidx.recyclerview.widget.ItemTouchHelper;\nimport androidx.recyclerview.widget.RecyclerView;\nimport com.google.android.material.snackbar.Snackbar;\n"
text = text.replace("import androidx.recyclerview.widget.RecyclerView;", import_statement)

swipe_code = """
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
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
"""

text = text.replace("adapter.setOnItemClickListener(deadline -> {", swipe_code)

with open("app/src/main/java/com/example/deadlinedesk/ui/UpcomingFragment.java", "w") as f:
    f.write(text)
