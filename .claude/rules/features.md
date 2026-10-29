---
paths:
  - "src/test/resources/features/**/*.feature"
---

# Feature files
- Tag every feature `@ui` or `@api` plus an area tag (`@login`, `@cart`). `@api` scenarios get no browser.
- Mark unfinished scenarios `@wip` and known product bugs `@known-failure` (both excluded by default) instead of deleting or commenting them out.
- Refer to users by role (`"standard"`), resolved from config `user.<role>.username` / `user.password`; keep credentials in config or env vars.
- Reuse existing steps before adding new ones: grep `@(Given|When|Then)\(` in `src/test/java/com/example/e2e/steps/`.
- Tables that become objects use a `@DataTableType` in `data/DataTableTypes.java`; `random` cells are filled by Datafaker.
