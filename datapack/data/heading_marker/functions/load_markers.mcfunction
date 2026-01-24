# Load Markers from Storage
# Loads all player markers from persistent storage using UUID-based keys

# Load each player's markers using UUID-based storage
execute as @a run function heading_marker:internal/load_player_uuid
