# Append blue marker to display
# Add space if not first marker
execute unless data storage headingmarker:display {text:[""]} run data modify storage headingmarker:display text append value '{"text":" 🔵","color":"blue"}'
execute if data storage headingmarker:display {text:[""]} run data modify storage headingmarker:display text append value '{"text":"🔵","color":"blue"}'
data modify storage headingmarker:display text append value '{"score":{"name":"@s","objective":"hm.blue.dist"},"color":"yellow"}'


