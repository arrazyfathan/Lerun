# Agent Instructions

## Verification Policy

- **Do NOT run builds**: Do not execute Gradle builds (e.g., `./gradlew build`,
  `./gradlew assembleDebug`, `./gradlew compileDebugKotlin`, etc.) to verify changes unless
  explicitly instructed by the user.
- **Do NOT run tests**: Do not run unit tests, instrumented tests, or lint checks (e.g.,
  `./gradlew test`, `./gradlew connectedAndroidTest`, `./gradlew lint`) unless explicitly requested.
- **Do NOT run on emulators or devices**: Do not launch, install, or interact with the app on any
  Android Emulator, iOS Simulator, or physical device (via `adb`, `android`, `agent-device`, etc.).

All build, test, and runtime verification will be performed manually by the user. Focus solely on
reading and writing code as requested.

## Git commit message conventions

Use Conventional Commit messages for project commits:

```text
<type>(<optional scope>)[!]: <description>

<optional body>

<optional footer>
```

The scope and breaking change indicator (`!`) are optional. Separate the
subject, body, and footer with blank lines. For example:

```sh
git commit -m "feat(auth): add email sign-in" \
  -m "Allow users to sign in with their email address." \
  -m "Closes #123"
```

### Commit types

- `feat`: Add, adjust, or remove an API or UI feature.
- `fix`: Fix an API or UI bug introduced by a previous feature commit.
- `refactor`: Rewrite or restructure code without changing API or UI behavior.
- `perf`: A specialized refactor that improves performance.
- `style`: Change code style, whitespace, or formatting without affecting
  application behavior.
- `test`: Add missing tests or correct existing tests.
- `docs`: Change documentation only.
- `build`: Change build tools, dependencies, project versions, or other
  build-related components.
- `ops`: Change infrastructure, deployment scripts, CI/CD pipelines, backups,
  monitoring, or recovery procedures.
- `chore`: Perform maintenance such as initializing the repository or updating
  `.gitignore`.

### Scope and description

- Use an optional scope to provide context about the affected area.
- Do not use issue identifiers as scopes; put issue references in the footer.
- Always provide a concise description in imperative, present tense: use
  "change", not "changed" or "changes". Read it as "This commit will...".
- Do not capitalize the first letter of the description or end it with a period.

### Body, footer, and breaking changes

- Use an optional body to explain the motivation and contrast the change with
  previous behavior. Use imperative, present tense.
- Use an optional footer for issue references such as `Closes #123` or
  `Fixes JIRA-456`.
- For breaking changes, add `!` immediately before the subject's colon and
  include a footer beginning with `BREAKING CHANGE:`.
- For a single-line breaking change explanation, add a space after
  `BREAKING CHANGE:`. For a multiline explanation, add two newlines after it.

```text
feat(api)!: remove ticket list endpoint

Refer to JIRA-1337.

BREAKING CHANGE: ticket endpoints no longer support listing all entities.
```

### Special commit formats

- Initial commit: `chore: init`.
- Merge commit: keep Git's default `Merge branch '<branch name>'` message.
- Revert commit: keep Git's default `Revert "<reverted commit subject line>"`
  message.
