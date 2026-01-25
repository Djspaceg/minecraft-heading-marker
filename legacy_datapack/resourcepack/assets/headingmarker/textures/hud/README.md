# Custom Marker Sprites

This directory is for custom HUD marker sprite images.

## Required Files

Place the following 16x16 pixel PNG images in this directory:

- `marker_red.png` - Red marker sprite (mapped to unicode \uE000)
- `marker_blue.png` - Blue marker sprite (mapped to unicode \uE001)
- `marker_green.png` - Green marker sprite (mapped to unicode \uE002)
- `marker_yellow.png` - Yellow marker sprite (mapped to unicode \uE003)
- `marker_purple.png` - Purple marker sprite (mapped to unicode \uE004)

## Specifications

- **Dimensions**: 16x16 pixels (standard HUD icon size)
- **Format**: PNG with transparency
- **Style**: Design these to match your preferred aesthetic

## Usage

Once you add the PNG files here:
1. The resource pack will automatically map them to unicode characters
2. Update the datapack functions to use `\uE000` through `\uE004` instead of emoji
3. The custom sprites will appear on the actionbar instead of emoji

## Current Status

Currently using emoji placeholders (🔴🔵🟢🟡🟣) which work without any resource pack. Once you add custom sprites here, you can switch to the unicode characters for a custom look.


