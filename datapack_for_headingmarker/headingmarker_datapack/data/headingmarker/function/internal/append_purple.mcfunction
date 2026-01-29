# Append purple marker to display
# Add space if not first marker
execute unless data storage headingmarker:display {text:[""]} run data modify storage headingmarker:display text append value '{"text":" 🟣","color":"light_purple"}'
execute if data storage headingmarker:display {text:[""]} run data modify storage headingmarker:display text append value '{"text":"🟣","color":"light_purple"}'
data modify storage headingmarker:display text append value '{"score":{"name":"@s","objective":"hm.purple.dist"},"color":"yellow"}'


