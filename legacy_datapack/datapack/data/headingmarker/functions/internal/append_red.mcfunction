# Append red marker to display
# Add space if not first marker
execute unless data storage headingmarker:display {text:[""]} run data modify storage headingmarker:display text append value '{"text":" 🔴","color":"red"}'
execute if data storage headingmarker:display {text:[""]} run data modify storage headingmarker:display text append value '{"text":"🔴","color":"red"}'
data modify storage headingmarker:display text append value '{"score":{"name":"@s","objective":"hm.red.dist"},"color":"yellow"}'


