# Append purple marker to display
# Add space if not first marker
execute unless data storage heading_marker:display {text:[""]} run data modify storage heading_marker:display text append value '{"text":" 🟣","color":"light_purple"}'
execute if data storage heading_marker:display {text:[""]} run data modify storage heading_marker:display text append value '{"text":"🟣","color":"light_purple"}'
data modify storage heading_marker:display text append value '{"score":{"name":"@s","objective":"hm.purple.dist"},"color":"yellow"}'
