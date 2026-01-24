# Append blue marker to display
# Add space if not first marker
execute unless data storage heading_marker:display {text:[""]} run data modify storage heading_marker:display text append value '{"text":" 🔵","color":"blue"}'
execute if data storage heading_marker:display {text:[""]} run data modify storage heading_marker:display text append value '{"text":"🔵","color":"blue"}'
data modify storage heading_marker:display text append value '{"score":{"name":"@s","objective":"hm.blue.dist"},"color":"yellow"}'
