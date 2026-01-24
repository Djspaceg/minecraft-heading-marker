# Heading Marker Tutorial

Welcome! This tutorial will walk you through using the Heading Marker data pack for the first time.

## Step 1: Installation

First, make sure you've installed the data pack:

1. Copy the `datapack` folder to your world's `datapacks` directory
2. Load your world or run `/reload` if already in-game
3. You should see a welcome message in chat

If you see the message, you're ready to go! If not, check the INSTALLATION.md guide.

## Step 2: Your First Waypoint - Marking Home

Let's create your first waypoint marker at your home base!

### What You'll Need

- 1 Compass
- 1 Lodestone

### Getting the Items

If you're in creative or have operator permissions:

```
/give @s compass
/give @s lodestone
```

In survival, craft them:
- **Compass:** 4 Iron Ingots + 1 Redstone Dust
- **Lodestone:** 8 Chiseled Stone Bricks + 1 Netherite Ingot (expensive!)

### Creating the Waypoint

1. **Stand at your home base** (or wherever you want the marker)

2. **Place the lodestone** on the ground

3. **Hold the compass** in your hand

4. **Right-click the lodestone** with the compass

5. **Success!** Your compass needle now points to this location

6. **Test it:** Walk away from home. Your compass should point back!

### Naming Your Compass

For better organization:

1. **Get an anvil** or use `/give @s anvil`
2. **Place the anvil** down
3. **Put your compass** in the anvil
4. **Rename it** to "Home" or "Base"
5. **Take it out**

Now you have a labeled "Home" compass!

## Step 3: Creating Multiple Waypoints

Let's create a second waypoint at your mine:

### Scenario: You Found a Great Mining Spot

1. **Travel to your mine location**

2. **Place another lodestone** there

3. **Get a NEW compass** (each compass tracks one lodestone)
   ```
   /give @s compass
   ```

4. **Right-click the lodestone** with this new compass

5. **Rename this compass** to "Main Mine"

Now you have two compasses:
- "Home" compass → points to home base
- "Main Mine" compass → points to your mine

### Storage Tip

Create a "Compass Wall" at your base:
1. Place item frames on a wall
2. Put each labeled compass in a frame
3. Take the compass you need, use it, then return it

## Step 4: Using the Helper Commands

The data pack provides helpful commands:

### Get Instructions for Adding Markers

```
/function heading_marker:add_marker
```

This shows step-by-step instructions in chat.

### Get Instructions for Removing Markers

```
/function heading_marker:remove_marker
```

This explains how to remove or clear markers.

### Advanced: Coordinate-Based Markers

```
/function heading_marker:add_marker_coordinates
```

This shows how to place markers at exact coordinates.

## Step 5: Advanced Techniques

### Color-Coding Waypoints

Make waypoints easier to identify:

1. **Place colored carpet** on top of the lodestone
2. **Use different colors** for different waypoint types:
   - Red carpet → Home/Base
   - Blue carpet → Mines
   - Green carpet → Farms
   - Yellow carpet → Trading halls
   - Purple carpet → Nether portals

The carpet is just visual - it doesn't affect the compass. But it helps you remember what each lodestone is for!

### Waypoint at Exact Coordinates

Let's say your friend tells you about a location at coordinates X=1000, Z=-2000:

1. **Place a lodestone there remotely:**
   ```
   /execute positioned 1000 64 -2000 run setblock ~ ~ ~ lodestone
   ```

2. **Get a compass:**
   ```
   /give @s compass
   ```

3. **Teleport there to link the compass:**
   ```
   /tp @s 1000 64 -2000
   ```

4. **Right-click the lodestone** with your compass

5. **Teleport back home:**
   ```
   /tp @s <your_home_x> <your_home_y> <your_home_z>
   ```

6. **Your compass now points to that location!**

### Multiple Waypoints System

Here's a complete waypoint system:

1. **Home Base** - Red carpet on lodestone
2. **Main Mine** - Blue carpet on lodestone  
3. **Nether Portal** - Purple carpet on lodestone
4. **Village/Trading Hall** - Green carpet on lodestone
5. **End Portal** - Black carpet on lodestone
6. **Farm Complex** - Lime carpet on lodestone

Create a compass for each, label them, and store in item frames!

## Step 6: Tips and Tricks

### Always Carry a Home Compass

Keep one compass that always points home in your inventory. If you get lost, you can always find your way back!

### Use an Ender Chest

Store your important compasses in an Ender Chest:
- Access them from anywhere
- Can't lose them if you die
- Perfect for your "Home" compass

### Protect Your Lodestones

Lodestones are valuable! Protect them:
- Build a structure around important lodestones
- Use fence posts or walls to prevent accidental breaking
- In survival, memorize coordinates as backup

### Nether Navigation

Lodestone compasses work in the Nether!
- Place a lodestone at your main portal
- Carry the linked compass when exploring
- Never lose your portal again

### Multiplayer Sharing

On multiplayer servers:
- Multiple players can link to the same lodestone
- Create "community waypoints" for shared locations
- Mark spawn, shops, or community farms

## Troubleshooting

### Compass Spinning Randomly

**Problem:** Your compass is spinning instead of pointing

**Solution:** The lodestone was destroyed or is too far away (in unloaded chunks). Travel to the lodestone location to check, or the lodestone may need to be replaced.

### Can't Link Compass

**Problem:** Right-clicking lodestone doesn't work

**Solution:** 
- Make sure you're in range (not too far)
- Try in survival mode (sometimes creative has issues)
- Make sure the lodestone is fully placed (not breaking)

### Wrong Direction

**Problem:** Compass points to wrong location

**Solution:** You might have linked it to a different lodestone. The compass remembers the FIRST lodestone you clicked. Break the lodestone, place a new one, and link a fresh compass.

## What's Next?

Now that you know the basics:
- Create waypoints for all your important locations
- Share waypoints with friends on servers
- Build a compass room with all your markers
- Explore confidently knowing you can always find your way back!

## Need More Help?

- **README.md** - Full feature overview
- **USAGE.md** - Detailed usage guide with more examples
- **INSTALLATION.md** - Installation and setup help
- **QUICKREF.md** - Quick command reference

Happy exploring! 🧭✨
