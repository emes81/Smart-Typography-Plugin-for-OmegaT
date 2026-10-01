# Contributing a language profile

Adding a language does not require Java changes.

1. Copy the closest file in `src/main/resources/profiles/`.
2. Rename it to the language or locale, for example `es.properties` or `fr-CA.properties`.
3. Change the values in that file.
4. Add the filename to `src/main/resources/profiles/index.txt`.
5. Submit the change as a pull request.

Profile keys:

```properties
id=xx
name=Language name
languages=xx,xx-YY
script=Latn
quotes.double.open=“
quotes.double.close=”
quotes.double.inner.open=none
quotes.double.inner.close=none
quotes.single.open=‘
quotes.single.close=’
quotes.single.inner.open=none
quotes.single.inner.close=none
apostrophe=’
spacing.before.semicolon=none
spacing.before.question=none
spacing.before.exclamation=none
spacing.before.colon=none
```

Spacing values are `none`, `space`, `nbsp`, or `nnbsp` (narrow no-break space).

Profiles are Unicode data. The engine does not require Latin characters, so profiles for other scripts can use their native punctuation directly.

A contribution should include a short source or style-guide reference for the typographic convention it implements.
