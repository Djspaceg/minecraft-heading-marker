# Troubleshooting Guide

This guide helps you diagnose and fix common issues with the Heading Marker data pack.

## Quick Checklist

Before diving into specific issues, verify:

- [ ] Minecraft version is 1.20.2 or later (macros were added in 1.20.2)
- [ ] The `headingmarker` folder is in the correct location: `saves/[YourWorld]/datapacks/headingmarker/`
- [ ] The `pack.mcmeta` file is at: `saves/[YourWorld]/datapacks/headingmarker/pack.mcmeta`
- [ ] You have operator permissions (required to run function commands)
- [ ] You've run `/reload` after installing

## Installation Issues

### Data Pack Not Loading (No Welcome Message)

**Symptoms:**

- No message appears when joining the world
- `/datapack list` doesn't show `headingmarker`

**Causes & Solutions:**

1. **Incorrect folder location**
   - ✅ CORRECT: `saves/[YourWorld]/datapacks/headingmarker/pack.mcmeta`
   - ❌ WRONG: `saves/[YourWorld]/datapacks/pack.mcmeta` (folder not copied)
   - ❌ WRONG: `saves/[YourWorld]/headingmarker/pack.mcmeta` (not in datapacks folder) [rename to `datapacks/headingmarker`]
   - ❌ WRONG: `saves/[YourWorld]/datapacks/headingmarker/data/pack.mcmeta` (too deep)

2. **Wrong Minecraft version**
   - This data pack requires Minecraft 1.20.2 or later (for macro support)
   - Check your version in the main menu
   - Update to at least 1.20.2 if needed

3. **File permissions**
   - Ensure Minecraft can read the files
   - On Linux/Mac, check file permissions: `chmod -R 755 headingmarker`

4. **Corrupted download**
   - Re-download the data pack
   - Ensure all files copied correctly

**How to verify:**

```
/datapack list
```

### "Unknown function" Errors

**Symptoms:**

- Running `/function headingmarker:help` says "Unknown function".
- The pack appears in `/datapack list` but commands fail.

**Causes & Solutions:**

1. **Duplicate Datapacks / Namespace Conflict**
   - If you have another folder (like `hm_test` or an old backup) in `datapacks/` that also uses the `headingmarker` namespace (i.e. has `data/headingmarker`), Minecraft may attempt to merge them.
   - If the old pack has broken code, it can crash the loading of the _new_ pack.
   - **Fix:** Delete any old or duplicate folders from the `datapacks/` directory.

2. **Incompatible Pack Format**
   - If the `pack_format` in `pack.mcmeta` is newer than your game version, the game may disable the functions.
   - **Fix:** Ensure `pack.mcmeta` uses a compatible format (e.g., 48 for 1.21-1.21.1).

You should see `headingmarker` in the enabled list.

### Data Pack Shows as Incompatible

**Symptoms:**

- Data pack appears with a red ❌ in `/datapack list`
- Says "incompatible" or "made for a different version"

**Solutions:**

1. **Check Minecraft version**
   - This pack needs 1.20.2+ for macros
   - If on older version, the `pack_format: 48` won't be recognized

2. **Check pack.mcmeta**
   - File should exist at: `datapacks/headingmarker/pack.mcmeta`
   - Should contain valid JSON with `pack_format: 48`

3. **Force enable (if using older version)**
   ```
   /datapack enable "file/headingmarker"
   ```
   Note: May not work correctly on versions before 1.20.2

## Command Issues

### Commands Don't Work / Not Recognized

**Symptoms:**

- Commands show "Unknown function" error
- Tab-completion doesn't show `headingmarker:` functions

**Causes & Solutions:**

1. **Data pack not enabled**

   ```
   /datapack list
   ```

   If not shown, enable it:

   ```
   /datapack enable "file/headingmarker"
   /reload
   ```

2. **Missing operator permissions**
   - Single-player: Enabled by default
   - Multiplayer: Ask admin to run: `/op YourUsername`

3. **Typo in command**
   - Correct: `/function headingmarker:help`
   - Wrong: `/function headingmarker:help` (hyphen)
   - Use Tab to auto-complete

4. **Namespace confusion**
   - Folder name: `headingmarker`
   - Namespace in commands: `headingmarker` (canonical)
   - This is normal! The namespace comes from the folder inside `data/`

### No Help Message When Running /function headingmarker:help

**Symptoms:**

- Command runs but no text appears
- Or shows "Unknown function"

**Solutions:**

1. **Verify data pack loaded**

   ```
   /datapack list
   ```

2. **Check for typos**

   ```
   /function headingmarker:help
   ```

   (Note: underscore not hyphen)

3. **Reload and try again**
   ```
   /reload
   /function headingmarker:help
   ```

## Marker Display Issues

### No Markers Showing on HUD

**Symptoms:**

- Markers set successfully but nothing appears on actionbar
- No distance numbers visible

**Causes & Solutions:**

1. **Markers not actually set**
   - Verify marker is active:

   ```
   /scoreboard objectives setdisplay sidebar hm.red.active
   ```

   You should see `1` if red marker is active

2. **Tick function not running**
   - The display updates every tick
   - Verify with: `/datapack list`
   - Ensure `headingmarker` is enabled

3. **In wrong dimension**
   - Markers are per-dimension
   - If you set a marker in Overworld but are in Nether, it won't show
   - Go back to the dimension where you set the marker

4. **Actionbar blocked by other data packs**
   - Some data packs may override the actionbar
   - Temporarily disable other data packs to test

### Markers Show Distance of 0 But I'm Far Away

**Symptoms:**

- Distance shows as `🔴0` even though you're not at the location

**Causes:**

1. **You ARE at the marker (within rounding error)**
   - Distance² of 0-100 means you're within 10 blocks
   - The marker works correctly!

2. **Marker coordinates are your current location**
   - You may have set the marker at your current position
   - Set it at a different location to test

### Markers Disappear After Restart

**Symptoms:**

- Markers work but don't persist between sessions
- Have to re-set markers every time

**Causes & Solutions:**

1. **Persistence system not triggering**
   - Markers should save automatically when `/reload` runs
   - Or when the world closes normally
   - Force save: `/function headingmarker:save_markers`

2. **Player UUID not matching**
   - This is rare but can happen if player data corrupts
   - Remove and rejoin the world

3. **Storage cleared**
   - Check if other data packs might be clearing storage
   - Verify with: `/data get storage headingmarker:players`

4. **Server not saving properly**
   - On servers, ensure auto-save is enabled
   - Run `/save-all` before stopping

## Multiplayer Issues

### Other Players Can't See My Markers

**This is expected behavior!**

- Markers are **per-player** and **personal**
- Other players cannot see your markers
- Each player sets their own markers independently
- To share locations, share coordinates in chat

### Markers Not Persisting on Server

**Symptoms:**

- Works in single-player but not on multiplayer server

**Solutions:**

1. **Ensure data pack is server-side**
   - Install in server's world folder, not client
   - Server needs to restart to load data pack

2. **Verify UUID-based storage**
   - Check storage: `/data get storage headingmarker:players`
   - Should show UUIDs as keys

3. **Server auto-save settings**
   - Ensure server saves player data
   - Check `server.properties` for save settings

## Performance Issues

### Game Lags With Markers Active

**Symptoms:**

- FPS drops when markers are displayed
- Game feels sluggish

**This is unlikely with this data pack (very lightweight)**

**If it happens:**

1. Reduce active markers (remove unused ones)
2. Check for other performance-heavy data packs
3. Update to latest Minecraft version

## Debugging Commands

### Confirm load tag executed (quick test)

1. Remove any duplicate packs from your world:
   - Delete `saves/[YourWorld]/datapacks/heading_marker` and `heading-marker` if present; keep only `headingmarker`.
2. Reload and watch chat (or `logs/latest.log`):
   - Run `/reload`.
   - Look for this exact debug message in chat or the log: **`[Heading Marker DEBUG] pack: headingmarker commit 8508315 loaded`** or the token **`HM-8508315-LOAD`**.
3. Test the function directly:
   - Run `/function headingmarker:debug_loaded` — it should print `debug token: HM-8508315-LOAD`.

### Check if data pack is loaded

```
/datapack list
```

### Force reload data pack

```
/reload
```

### Check marker status

```
/scoreboard objectives setdisplay sidebar hm.red.active
```

(Should show 1 if active, 0 or blank if not)

### Check marker coordinates

```
/scoreboard objectives setdisplay sidebar hm.red.x
```

(Shows X coordinate of red marker)

### View storage

```
/data get storage headingmarker:players
```

(Shows saved markers for all players)

### Force save markers

```
/function headingmarker:save_markers
```

### Force load markers

```
/function headingmarker:load_markers
```

## Still Having Issues?

If none of these solutions work:

1. **Collect information:**
   - Minecraft version
   - Single-player or multiplayer
   - Output of `/datapack list`
   - Any error messages in chat
   - Game log file (`.minecraft/logs/latest.log`)

2. **Try a clean install:**
   - Remove the data pack completely
   - Delete the `headingmarker` folder
   - Run `/reload`
   - Restart Minecraft
   - Re-install fresh copy

3. **Test in a new world:**
   - Create a new test world
   - Install data pack in test world
   - See if issue persists
   - If it works, issue may be with original world

4. **Report the bug:**
   - Open an issue on GitHub
   - Include all information from step 1
   - Describe what you expected vs what happened
   - Include steps to reproduce

## Common Misunderstandings

### "The marker doesn't point me to the location"

**This data pack shows distance only, not direction.**

- You see distance² to each waypoint
- You need to navigate using coordinates and distance
- Lower distance = you're getting closer
- Distance² increases as you move away

### "I can't see other players' markers"

**Markers are personal to each player.**

- This is by design for privacy and independence
- Share coordinates in chat if coordinating with others

### "The markers don't work in the Nether/End"

**Markers ARE dimension-specific (this is a feature!).**

- Each dimension has its own set of 5 markers
- Set different markers in each dimension
- They auto-switch when you change dimensions

### "I have to type `/function` every time"

**Yes, this is how data pack commands work.**

- Minecraft data packs use function commands
- There's no shorter slash command available
- Use tab-completion to speed up typing
- Or create command blocks for quick access

## Pro Tips

1. **Use tab-completion:** Type `/function h` then press Tab
2. **Click help examples:** The help command shows clickable examples
3. **Organize by color:** Use consistent colors (e.g., red=home, blue=mines)
4. **Command blocks:** Set up command blocks for quick marker access
5. **Coordinate sharing:** Share coordinates in team chat for coordination

---

**Need more help?** Check the main README.md or INSTALLATION.md files!
