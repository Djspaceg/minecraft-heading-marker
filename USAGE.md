# Usage Guide

This guide provides detailed examples and use cases for the Heading Marker data pack.

## Quick Start

1. Get a compass: `/give @s compass`
2. Place a lodestone at your desired waypoint
3. Right-click the lodestone with your compass
4. Your compass now points to that location!

## Basic Usage

### Creating Your First Waypoint

Let's say you want to mark your home base:

1. **Craft or place a lodestone at your home base**
   - Lodestone recipe: 8 Chiseled Stone Bricks + 1 Netherite Ingot
   - Or use: `/give @s lodestone`

2. **Get a compass**
   - Craft: 4 Iron Ingots + 1 Redstone Dust
   - Or use: `/give @s compass`

3. **Link the compass to the lodestone**
   - Hold the compass in your hand
   - Right-click on the lodestone
   - The compass is now permanently linked!

4. **Test it out**
   - Walk away from your home
   - The compass will always point toward your home lodestone
   - The needle points directly at the lodestone, regardless of distance

### Creating Multiple Waypoints

You can create multiple lodestone compasses for different locations:

1. **Home Base** - Place a lodestone at your main base
2. **Mining Operation** - Place a lodestone at your main mine
3. **Trading Hall** - Place a lodestone at your villager trading area
4. **Nether Portal** - Place a lodestone at your main portal
5. **End Portal** - Place a lodestone at the stronghold

Each compass only tracks one lodestone, so you'll need multiple compasses for multiple waypoints!

## Color-Coding Waypoints

Make your waypoints easier to identify by color-coding them:

1. **Place a colored carpet on top of the lodestone** before linking
   - The carpet color doesn't affect the compass mechanically
   - Use it as a visual reminder of what the waypoint is for
   - Example: Red for home, Blue for mine, Green for farm

2. **Use named compasses** for better organization
   - Use an anvil to rename your compass before or after linking
   - Example names: "Home", "Main Mine", "Nether Hub", "End Farm"

3. **Store in organized chests** with item frames showing the purpose

## Advanced Techniques

### Waypoint at Specific Coordinates

If you know exact coordinates but can't easily travel there:

```
# Example: Place lodestone at X=1000, Y=64, Z=-500
/execute positioned 1000 64 -500 run setblock ~ ~ ~ lodestone

# Then teleport there to link your compass
/tp @s 1000 64 -500

# Right-click the lodestone with your compass, then teleport back
/tp @s <your_x> <your_y> <your_z>
```

### Temporary Waypoints

For short-term navigation that you don't want to keep:

1. Place a lodestone at your destination
2. Link a compass to it
3. Navigate to the location
4. Break the lodestone when you arrive
5. The compass becomes a regular compass again

### Shared Waypoints (Multiplayer)

On multiplayer servers, multiple players can use the same lodestone:

1. One player places the lodestone at a shared location
2. Each player brings their own compass
3. Each player right-clicks the same lodestone
4. Now everyone has a compass pointing to the same location!

Perfect for:
- Team meeting points
- Shared farms or resource areas
- Server spawn or hub locations
- Event locations

## Practical Use Cases

### 1. Never Lose Your Home

**Problem:** You venture far from home and get lost

**Solution:**
- Always keep a home compass in your inventory
- Before exploring, ensure you have a lodestone at home with a linked compass
- No matter how far you travel, you can always find your way back

### 2. Mark Resource Locations

**Problem:** You find a great mining spot but can't remember where it is

**Solution:**
- Carry spare lodestones and compasses when exploring
- When you find valuable resources, place a lodestone
- Link a compass and continue exploring
- Return later using your marked location

### 3. Nether Navigation

**Problem:** The Nether is confusing and dangerous

**Solution:**
- Place lodestones at your main nether portal
- Place lodestones at nether fortress locations
- Place lodestones at bastion remnants
- Use multiple compasses to navigate safely

**Note:** Lodestone compasses work in any dimension!

### 4. Coordinate Large Projects

**Problem:** Building something massive across multiple locations

**Solution:**
- Place lodestones at each construction site
- Label compasses with site names using an anvil
- Quickly navigate between work areas
- Share locations with team members on servers

### 5. Create a Transportation Network

**Problem:** Managing multiple important locations

**Solution:**
Create a "compass room" at your base:
- Wall of item frames, each with a labeled compass
- Each compass points to a different location
- Grab the compass you need before traveling
- Return it to the frame after use

## Command Reference

### Helper Commands

The data pack provides these helpful commands:

```
# Show instructions for adding a marker
/function heading_marker:add_marker

# Show instructions for removing a marker
/function heading_marker:remove_marker

# Show instructions for coordinate-based markers
/function heading_marker:add_marker_coordinates
```

### Useful Minecraft Commands

```
# Give yourself a compass
/give @s compass

# Give yourself a lodestone
/give @s lodestone

# Teleport to coordinates
/tp @s <x> <y> <z>

# Place a lodestone at coordinates
/execute positioned <x> <y> <z> run setblock ~ ~ ~ lodestone

# Get your current coordinates
/tp @s ~ ~ ~
```

## Tips and Tricks

### Efficient Compass Management

1. **Always carry a home compass** - Keep one compass that always points home
2. **Use an Ender Chest** - Store important compasses in an Ender Chest for universal access
3. **Shulker Boxes** - Organize sets of compasses in labeled Shulker Boxes
4. **Item Frames** - Display compasses in item frames with signs showing destinations

### Lodestone Protection

Lodestones are valuable - protect them:

1. **Build protective structures** around important lodestones
2. **Use claim protection** on servers with land claiming
3. **Memorize coordinates** of lodestones as backup
4. **Keep spare lodestones** in case one breaks

### Creative Mode Tricks

In creative mode, you can:

1. Instantly place lodestones anywhere
2. Quickly test waypoint systems
3. Create elaborate navigation networks
4. Build compass rooms with unlimited resources

## Troubleshooting

### Compass Spinning Randomly

**Cause:** The linked lodestone was destroyed or is in an unloaded chunk

**Fix:** 
- Return to the lodestone location to check if it still exists
- If broken, place a new lodestone there
- If in unloaded chunks, travel closer to load the chunks

### Compass Points to Wrong Location

**Cause:** Compass might be linked to a different lodestone than expected

**Fix:**
- Check the compass name/label
- Break and replace the correct lodestone if needed
- Create a new compass and link it properly

### Can't Link Compass to Lodestone

**Cause:** Creative mode or game bug

**Fix:**
- Make sure you're using right-click (not left-click)
- Try in survival mode
- Ensure the lodestone is fully placed (not in the process of breaking)
- Try with a fresh compass

## Best Practices

1. ✅ **Label everything** - Name your compasses and mark your lodestones
2. ✅ **Keep backups** - Always have spare compasses and lodestones
3. ✅ **Document locations** - Write down coordinates of important lodestones
4. ✅ **Protect lodestones** - Build structures to prevent accidental breaking
5. ✅ **Test before long trips** - Verify compasses work before exploring far away
6. ✅ **Share on servers** - Coordinate with teammates on shared waypoints

## Further Reading

- [Minecraft Wiki - Compass](https://minecraft.wiki/w/Compass)
- [Minecraft Wiki - Lodestone](https://minecraft.wiki/w/Lodestone)
- Main README.md for feature overview
- INSTALLATION.md for setup instructions

Happy navigating! 🧭✨
