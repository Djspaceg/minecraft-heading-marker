# Extract first UUID component and store as unique player identifier
execute store result score @s hm.uuid run data get entity @s UUID[0]
