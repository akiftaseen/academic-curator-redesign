import re

path_main = "app/src/main/res/layout/activity_main.xml"
with open(path_main, "r") as f:
    text = f.read()

# Make it match exactly stitch specs instead of older layout.
text = text.replace('app:elevation="8dp"', 'app:elevation="0dp"')
text = text.replace('app:itemTextAppearanceActive="@style/TextAppearance.DeadlineDesk.LabelLarge"', 'app:itemTextAppearanceActive="@style/TextAppearance.DeadlineDesk.BottomNavActive"')
text = text.replace('app:itemTextAppearanceInactive="@style/TextAppearance.DeadlineDesk.LabelMedium"', 'app:itemTextAppearanceInactive="@style/TextAppearance.DeadlineDesk.BottomNavInactive"')
text = text.replace('android:layout_height="88dp"', 'android:layout_height="96dp"')
text = text.replace('app:itemPaddingTop="10dp"', 'app:itemPaddingTop="12dp"')
text = text.replace('app:itemPaddingBottom="12dp"', 'app:itemPaddingBottom="16dp"')

with open(path_main, "w") as f:
    f.write(text)
