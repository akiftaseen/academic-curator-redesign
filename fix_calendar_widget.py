import re

path_themes = "app/src/main/res/values/themes.xml"
with open(path_themes, "r") as f:
    text = f.read()

# Replace any incorrect references to primary colors for the selection shape
# The design specifically calls for tertiary_container per the stitch HTML code for events.

# Also, Bottom nav indicator should be primary_fixed_dim, which is already there, but we want it to map correctly to stitch's larger indicator.
text = text.replace('<item name="android:color">@color/primary_fixed_dim</item>', 
                    '<item name="android:color">@color/surface_container_high</item>\n        <item name="android:height">32dp</item>\n        <item name="android:width">64dp</item>')

with open(path_themes, "w") as f:
    f.write(text)

