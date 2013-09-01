#!/bin/bash
set -e

# takes a file and produces all other corresponding icons

if [[ ! -f "$1" ]]
then
    echo "Argument must be a filename: $1"
    exit 1
fi

SIZE=$(identify "$1" | awk '{print $3}')
if [[ "$SIZE" != "40x48" ]]
then
    echo "Unexpected size: $SIZE"
    exit 1
fi

filename=$(basename "$1")
directory=$(dirname "$1")
extension="${filename##*.}"
filename_without_ext="${filename%.*}"

if [[ "$(basename $directory)" != "drawable-hdpi" ]]
then
    echo "Unexpected directory: $(basename $directory)"
    exit 1
fi

dir_hdpi="$directory"
dir_mdpi="$directory/../drawable-mdpi"

convert "$dir_hdpi/$filename_without_ext.$extension" +level-colors 0x008000, "$dir_hdpi/${filename_without_ext}_updated.$extension"
convert "$dir_hdpi/$filename_without_ext.$extension" +level-colors blue, "$dir_hdpi/${filename_without_ext}_selected.$extension"

convert "$dir_hdpi/$filename_without_ext.$extension" +level-colors 0x008000, -resize 75% "$dir_mdpi/${filename_without_ext}_selected.$extension"
convert "$dir_hdpi/$filename_without_ext.$extension" +level-colors blue, -resize 75% "$dir_mdpi/${filename_without_ext}_selected.$extension"

cat  > "$dir_hdpi/${filename_without_ext}_statelist.xml" <<EOF
<?xml version="1.0" encoding="utf-8"?>

<selector xmlns:android="http://schemas.android.com/apk/res/android">

    <item
  android:state_focused="true"
  android:drawable="@drawable/${filename_without_ext}_selected" />
    <item  android:drawable="@drawable/${filename_without_ext}" />
</selector>

EOF
cp "$dir_hdpi/${filename_without_ext}_statelist.xml" "$dir_mdpi"

echo "Done!"

