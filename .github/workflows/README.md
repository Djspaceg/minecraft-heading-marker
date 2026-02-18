# GitHub Actions Workflows

This directory contains GitHub Actions workflows for continuous integration and releases.

## Workflows

### ci.yml - Minecraft Mod CI
**Triggers:** Push and pull requests to `main` and `develop` branches, manual dispatch

**Jobs:**
1. **unit-test** - Runs JUnit tests with Java 21
   - Uploads test results as artifacts
   
2. **build** - Builds the Minecraft mod JAR
   - Depends on unit-test passing
   - Uploads mod JAR as artifact
   
3. **status-report** - Posts build status to pull requests
   - Only runs on PRs
   - Links to artifacts for easy download

**Artifacts:**
- `test-results` - Test reports (14 day retention)
- `headingmarker-mod` - Built mod JARs (30 day retention)

### release.yml - Build Release Mod
**Triggers:** Version tags (`v*`), manual dispatch

**Jobs:**
1. **build-release** - Builds and releases the mod
   - Builds the mod JAR
   - Creates GitHub release with JAR files attached
   - Includes installation instructions

**Artifacts:**
- `headingmarker-{version}` - Release mod JARs (90 day retention)

## Requirements

Both workflows require:
- Java 21 (Temurin distribution)
- Gradle wrapper (gradle-wrapper.jar must be committed)
- Fabric Loom plugin (configured in build.gradle)

## Local Testing

To test builds locally:
```bash
# Run tests
./gradlew test

# Build mod
./gradlew build

# Check build outputs
ls -la build/libs/
```

## Notes

- The workflows use Java 21 to match the project's requirements
- Gradle caching is enabled to speed up builds
- All builds use `--stacktrace` for better error diagnostics
- The gradle-wrapper.jar is committed to the repository (exception in .gitignore)
