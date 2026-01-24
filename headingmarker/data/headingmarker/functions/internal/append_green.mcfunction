# Append green marker to display
# Add space if not first marker
execute unless data storage headingmarker:display {text:[""]} run data modify storage headingmarker:display text append value '{"text":" 🟢","color":"green"}'
execute if data storage headingmarker:display {text:[""]} run data modify storage headingmarker:display text append value '{"text":"🟢","color":"green"}'
data modify storage headingmarker:display text append value '{"score":{"name":"@s","objective":"hm.green.dist"},"color":"yellow"}'


