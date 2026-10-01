# Smart Typography for OmegaT

Context-aware typographic punctuation for OmegaT 6.1.x.

## Behaviour

The plugin acts only on direct keyboard typing in OmegaT's active translation editor. Programmatic insertions such as AutoText, paste, TM insertion, glossary insertion and scripts are not reprocessed by the plugin.

Default features:

- straight double quotes become language-appropriate opening/closing double quotes
- straight apostrophes become apostrophes or language-appropriate single quotes according to context
- `--` becomes `–`
- `---` becomes `—`
- language-specific punctuation spacing is applied where the profile defines it
- `...` becomes `…` only when the Ellipsis option is enabled
- `. . .` is never collapsed

Settings are under **Preferences → Auto-completion → Typography**.

Bundled profiles in 0.1.0: English, German, German (Switzerland), French. Unsupported target languages still receive enabled language-neutral rules such as dashes and ellipsis; quote and spacing rules require a matching profile or an explicitly selected profile.

## Install

Build the plugin JAR with Gradle, then install it through OmegaT's plugin installer or place it in the OmegaT user `plugins` directory. Restart OmegaT after installation.

## Build

Requires Java 11 or later.

```sh
gradle clean test jar
```

OmegaT is a `compileOnly` dependency and is therefore not bundled into the plugin JAR.

## Contributing

The **Contribute a language** button in the preferences pane opens the repository contribution instructions. Language profiles are stored under `src/main/resources/profiles/`.

## Licence

GNU General Public License version 3 or later.
