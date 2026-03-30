import urllib.request
import os

fonts = {
    "inter_regular.ttf": "https://github.com/rsms/inter/raw/master/docs/font-files/Inter-Regular.ttf",
    "inter_medium.ttf": "https://github.com/rsms/inter/raw/master/docs/font-files/Inter-Medium.ttf",
    "inter_bold.ttf": "https://github.com/rsms/inter/raw/master/docs/font-files/Inter-Bold.ttf",
    "manrope_regular.ttf": "https://github.com/sharanda/manrope/raw/master/fonts/ttf/Manrope-Regular.ttf",
    "manrope_bold.ttf": "https://github.com/sharanda/manrope/raw/master/fonts/ttf/Manrope-Bold.ttf",
    "manrope_extrabold.ttf": "https://github.com/sharanda/manrope/raw/master/fonts/ttf/Manrope-ExtraBold.ttf"
}

os.makedirs('app/src/main/res/font', exist_ok=True)

for name, url in fonts.items():
    print(f"Downloading {name}...")
    urllib.request.urlretrieve(url, f"app/src/main/res/font/{name}")
print("Fonts downloaded.")
