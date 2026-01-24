# Append yellow marker to display
# Add space if not first marker
execute unless data storage heading_marker:display {text:[""]} run data modify storage heading_marker:display text append value '{"text":" 🟡","color":"yellow"}'
execute if data storage heading_marker:display {text:[""]} run data modify storage heading_marker:display text append value '{"text":"🟡","color":"yellow"}'
data modify storage heading_marker:display text append value '{"score":{"name":"@s","objective":"hm.yellow.dist"},"color":"yellow"}'
