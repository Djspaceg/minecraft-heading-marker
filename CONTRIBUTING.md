# Contributing to Heading Marker

Thank you for considering contributing to Heading Marker! This document provides guidelines for contributing to the project.

## How to Contribute

### Reporting Bugs

If you find a bug, please create an issue with:
- A clear, descriptive title
- Steps to reproduce the bug
- Expected behavior vs actual behavior
- Minecraft version and data pack version
- Any error messages or screenshots

### Suggesting Features

Feature suggestions are welcome! Please create an issue with:
- A clear description of the feature
- Why this feature would be useful
- How it might work in Minecraft
- Any examples from other data packs or mods

### Contributing Code

1. **Fork the repository**
2. **Create a new branch** for your feature or bugfix
3. **Make your changes** following the guidelines below
4. **Test thoroughly** in Minecraft
5. **Submit a pull request** with a clear description

## Development Guidelines

### Data Pack Functions

- Use clear, descriptive function names
- Add comments explaining complex logic
- Follow Minecraft command syntax exactly
- Test functions in both single-player and multiplayer
- Use `tellraw` for user-facing messages with proper formatting

### Resource Pack Assets

- Use PNG format for textures
- Follow Minecraft's naming conventions
- Keep file sizes reasonable (optimize images)
- Test textures in-game with various settings
- Document any new texture additions

### Documentation

- Keep documentation up-to-date with code changes
- Use clear, simple language
- Provide examples for complex features
- Include screenshots where helpful
- Check spelling and grammar

### JSON Files

- Use proper JSON formatting (2-space indentation)
- Validate JSON before committing
- Follow Minecraft's data pack format specifications
- Include comments in markdown where needed

## Code Style

### MCFunction Files

```mcfunction
# Use comments to explain what the function does
# Keep commands readable with proper spacing

# Good
tellraw @s ["",{"text":"Message","color":"yellow"}]

# Also good for complex JSON
tellraw @s [
  "",
  {"text":"[Heading Marker] ","color":"gold","bold":true},
  {"text":"Your message here","color":"yellow"}
]
```

### JSON Files

```json
{
  "values": [
    "namespace:function_name"
  ]
}
```

## Testing

Before submitting a pull request:

1. **Test in a clean Minecraft world**
   - Create a new test world
   - Install only the Heading Marker data pack
   - Test all modified functions

2. **Test in survival mode**
   - Verify commands work without creative/operator permissions where applicable
   - Check resource requirements

3. **Test multiplayer** (if applicable)
   - Verify functions work for multiple players
   - Check for any synchronization issues

4. **Test compatibility**
   - Test with vanilla Minecraft (no mods)
   - Test with common data packs if possible

## Version Compatibility

Currently targeting:
- **Minecraft Java Edition:** 1.21+
- **Data Pack Format:** 48
- **Resource Pack Format:** 34

If adding features that require a different version:
- Update documentation
- Note version requirements clearly
- Consider backward compatibility

## Questions?

If you have questions about contributing:
- Check existing issues and pull requests
- Create a new issue with your question
- Reach out to maintainers

## Code of Conduct

- Be respectful and constructive
- Welcome newcomers and help them learn
- Focus on the best outcome for the project
- Accept constructive criticism gracefully

## License

By contributing to Heading Marker, you agree that your contributions will be licensed under the MIT License.

Thank you for helping make Heading Marker better! 🧭


