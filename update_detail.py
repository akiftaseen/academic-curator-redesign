import re

with open("app/src/main/java/com/example/deadlinedesk/ui/DeadlineDetailActivity.java", "r") as f:
    text = f.read()

import_statement = "import android.widget.ImageButton;\nimport android.widget.TextView;\nimport android.widget.Toast;\nimport android.content.Intent;\n"
if "btn_share" not in text:
    text = text.replace("private ImageButton btnEdit;", "private ImageButton btnEdit, btnShare;")
    text = text.replace('btnEdit = findViewById(R.id.btn_edit);', 'btnEdit = findViewById(R.id.btn_edit);\n        btnShare = findViewById(R.id.btn_share);\n')

    share_code = """
        btnShare.setOnClickListener(v -> {
            if (currentDeadline != null) {
                String shareText = "Assignment: " + currentDeadline.getTitle() + "\\n" +
                        "Module: " + currentDeadline.getModule() + "\\n" +
                        "Due: " + tvDueDate.getText().toString();
                if (currentDeadline.getNotes() != null && !currentDeadline.getNotes().isEmpty()) {
                    shareText += "\\nNotes: " + currentDeadline.getNotes();
                }
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, currentDeadline.getTitle());
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(shareIntent, "Share Assignment"));
            }
        });

        btnEdit.setOnClickListener(v -> {
"""
    text = text.replace("btnEdit.setOnClickListener(v -> {", share_code)

with open("app/src/main/java/com/example/deadlinedesk/ui/DeadlineDetailActivity.java", "w") as f:
    f.write(text)
