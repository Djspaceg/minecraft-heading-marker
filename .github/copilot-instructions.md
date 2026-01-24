# Copilot / AI Agent Instructions for Heading Marker 🔧

Purpose: short, actionable guidance so an AI coding agent can be productive immediately in this repository.

## Big picture overview 💡

- This repository contains a vanilla Minecraft Java Edition **data pack** + optional **resource pack** that provides per-player HUD waypoints.
- Main runtime: data pack functions run inside Minecraft using `load` (on world load) and `tick` (20x/sec) tags.
- Data lives in Minecraft storage (JSON-like persistent storage) keyed by the pack namespace and player UUIDs.
- Critical invariant: there are 5 colors × 3 dimensions × per-player marker slots. Scoreboard keys follow `hm.<color>.<x|y|z|active|dist>` and global helpers use `hm.*`.

## Key files & directories to read first 📂

- `headingmarker/pack.mcmeta` — pack metadata & `pack_format` (48).
- `headingmarker/data/headingmarker/functions/*.mcfunction` — main commands: `load.mcfunction`, `tick.mcfunction`, `set*.mcfunction`, `save_markers.mcfunction`, `load_markers.mcfunction`, `help.mcfunction`.
- `headingmarker/data/headingmarker/functions/internal/` — helper macros (color-specific set/remove/calc/append, persistence macros, dimension handling).
- `headingmarker/data/minecraft/tags/functions/{load.json,tick.json}` — which functions are auto-run by Minecraft.
- `resourcepack/assets/headingmarker/` and `resourcepack/assets/headingmarker/font/default.json` — font -> sprite mappings.
- `README.md`, `INSTALLATION.md`, and `TROUBLESHOOTING.md` — user-facing docs and common commands to validate.

## Project-specific conventions & patterns ✅

- Namespace and folder naming: canonical is `headingmarker`. Consistency is mandatory between folder names, `pack.mcmeta` namespace references, and all `namespace:function` strings.
- Scoreboards: names always start with `hm.` (e.g., `hm.red.x`, `hm.nextcolor`, `hm.dimension`). Use these exact strings when adding or referencing objectives.
- Persistence: storage key is (or should be) `headingmarker:players` (search for `storage ... players` in function files). That storage contains per-player UUID objects.
- Use `tellraw` JSON arrays for all player-facing messages; tests rely on exact structure in `help.mcfunction` and other UI functions.
- Colors map: `0=red,1=blue,2=green,3=yellow,4=purple` (used pervasively).

## Renaming / consistency checklist (high-priority) ⚠️

- Search repo for occurrences of `headingmarker` and reconcile. Use both text search and file path checks.
- Update `data/minecraft/tags/functions/*.json` values to match the actual namespace in `headingmarker/data/headingmarker/functions` (fixes "Unknown function headingmarker:..." errors in logs).
- Update `resourcepack` font file references to match the resource folder namespace if renaming resources.
- When renaming files/folders, prefer `git mv` for history; follow with a repo-wide replace of string references and update docs.

## Development, testing & debug workflow 🧪

- Quick in-game checks (Minecraft console / player chat):
  - `/datapack list` — confirm pack enabled
  - `/reload` — reload functions
  - `/function headingmarker:help` — show help (namespace must match files)
  - `/function headingmarker:save_markers` and `/function headingmarker:load_markers` — force persistence ops
  - `/data get storage headingmarker:players` — inspect saved data
  - `/scoreboard objectives setdisplay sidebar hm.red.active` — verify marker active state
- When functions are not found, check server/client `latest.log` for messages like `Unknown function headingmarker:help` — namespace mismatch is the most common cause.
- Always run `load.mcfunction` once after naming changes to re-create scoreboards or update `pack_format` if necessary.

## Example quick tasks for an AI agent (safe, discrete) 🧰

1. Find and replace all `headingmarker:` function references in `data/minecraft/tags` and docs with `headingmarker:` (run search & update files). Commit with explanatory message.
2. Ensure `resourcepack` font file references match the resource folder namespace (or update resource folder names and `pack.mcmeta`).
3. Add a short unit test / QA checklist in `TROUBLESHOOTING.md` documenting the exact commands to reproduce the "Unknown function" error and the fix.

## Non-goals and constraints ❌

- Don't change in-game user-facing messages (tellraw contents) without confirming wording preferences — maintain backward compatibility.
- Avoid changing the scoreboard naming scheme or the on-disk storage schema unless a migration plan is provided and tested in-game.

## Helpful search patterns for automation 🔎

- `git grep "headingmarker"` and `git grep "headingmarker"`
- `grep -R "hm\."` to find scoreboard usages
- Search for `Unknown function` in `latest.log` when debugging

---

If anything above is unclear or you'd like me to start with an automated rename & tests, tell me which namespace you want to be canonical (`headingmarker` recommended) and whether I should also rename resource pack folders. ✅

