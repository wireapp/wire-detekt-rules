# Repository Guidelines

## Project Structure & Module Organization

This is a single-module Kotlin/JVM project that packages custom Detekt rules. Rule implementations live in `src/main/kotlin/com/wire/detekt/rules/`; `WireRuleSetProvider.kt` assembles the published rule set. The service-loader entry under `src/main/resources/META-INF/services/` makes that provider discoverable by Detekt. Tests mirror the rules in `src/test/kotlin/com/wire/detekt/rules/`. Versioned JARs in `dist/` are release artifacts produced by CI; Gradle-generated output belongs in `build/`.

## Build, Test, and Development Commands

Use the checked-in Gradle wrapper and JDK 17:

- `./gradlew test` runs the complete unit test suite.
- `./gradlew build` compiles the rule set, runs tests, and creates build artifacts.
- `./gradlew jar` builds `build/libs/detekt-rules-<version>.jar`.
- `./gradlew jar -Pversion=local-check` builds a clearly labeled local artifact.
- `./gradlew publishToMavenLocal` publishes the library locally for integration testing in another project that declares `mavenLocal()`.

Do not edit files under `build/`. Avoid manually replacing tracked `dist/` JARs; the main-branch publishing workflow creates and commits timestamped artifacts.

## Coding Style & Naming Conventions

Follow standard Kotlin formatting with four-space indentation and trailing commas where they improve multiline diffs. Use `UpperCamelCase` for classes and suffix Detekt implementations with `Rule` (for example, `DaoFlowOnRule`). Keep rule code in `com.wire.detekt.rules`; register every new rule in `WireRuleSetProvider`. Prefer focused PSI visitors, explicit diagnostic messages, and small shared helpers in `RuleExtensions.kt`. No standalone formatter is configured, so format changed Kotlin files with the IDE before committing.

## Testing Guidelines

Tests use `kotlin.test`, JUnit Platform, and Detekt's `detekt-test` compiler helpers. Name test classes `<RuleName>Test` and use descriptive backtick test names such as ``reports when flowOn is missing``. Cover both reporting and non-reporting cases, including naming or syntax boundaries. Assert finding counts and, when useful, diagnostic text. Run `./gradlew test` before opening a pull request; the repository does not define a numeric coverage threshold.

## Commit & Pull Request Guidelines

Follow the established concise prefixes: `feat:`, `chore:`, `chore(deps):`, and `ci:`. Write imperative subjects that state the rule or infrastructure change. Reserve `[skip ci]` for automated artifact commits. Pull requests should explain the enforced behavior, include representative valid and invalid Kotlin examples, link the relevant issue, and add or update tests. Screenshots are unnecessary unless documentation rendering changes. Code owners under `@wireapp/android` review repository changes.
