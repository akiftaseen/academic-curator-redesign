import re

path_cal = "app/src/main/res/layout/fragment_calendar.xml"
with open(path_cal, "r") as f:
    text = f.read()

# Make the card completely borderless and white (surface_container_lowest)
text = re.sub(r'app:strokeWidth="1dp"', 'app:strokeWidth="0dp"', text)
# Need negative margins on the sides or specific padding. The padding can be increased vertically to look more like stitch.
text = text.replace('app:contentPadding="8dp"', 'app:contentPadding="16dp"')

with open(path_cal, "w") as f:
    f.write(text)

