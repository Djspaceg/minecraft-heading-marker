# Append yellow marker to display
# Add space if not first marker
execute unless data storage headingmarker:display {text:[""]} run data modify storage headingmarker:display text append value '{"text":" 🟡","color":"yellow"}'
execute if data storage headingmarker:display {text:[""]} run data modify storage headingmarker:display text append value '{"text":"🟡","color":"yellow"}'
data modify storage headingmarker:display text append value '{"score":{"name":"@s","objective":"hm.yellow.dist"},"color":"yellow"}'


