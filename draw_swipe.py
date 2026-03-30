import re

with open("app/src/main/java/com/example/deadlinedesk/ui/UpcomingFragment.java", "r") as f:
    text = f.read()

import_statement = """import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.view.View;
"""
text = text.replace("import androidx.recyclerview.widget.ItemTouchHelper;\nimport androidx.recyclerview.widget.RecyclerView;\nimport com.google.android.material.snackbar.Snackbar;\n", import_statement)

swipe_code = """
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
"""
# Need to substitute the existing one
# Wait, let me just do a simpler search/replace because there's only one ItemTouchHelper
import_match = re.search(r'new ItemTouchHelper\(new ItemTouchHelper\.SimpleCallback.*?\}\)\.attachToRecyclerView\(recyclerView\);', text, re.DOTALL)
if import_match:
    text = text.replace(import_match.group(0), swipe_code.strip())

with open("app/src/main/java/com/example/deadlinedesk/ui/UpcomingFragment.java", "w") as f:
    f.write(text)

with open("app/src/main/java/com/example/deadlinedesk/ui/CalendarFragment.java", "r") as f:
    text2 = f.read()

text2 = text2.replace("import androidx.recyclerview.widget.ItemTouchHelper;\nimport androidx.recyclerview.widget.RecyclerView;\nimport com.google.android.material.snackbar.Snackbar;\n", import_statement)
import_match2 = re.search(r'new ItemTouchHelper\(new ItemTouchHelper\.SimpleCallback.*?\}\)\.attachToRecyclerView\(recyclerView\);', text2, re.DOTALL)
if import_match2:
    text2 = text2.replace(import_match2.group(0), swipe_code.strip())

with open("app/src/main/java/com/example/deadlinedesk/ui/CalendarFragment.java", "w") as f:
    f.write(text2)

