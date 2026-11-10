#!/usr/bin/env python3
"""Static localization checks that do not require an Android SDK."""

from __future__ import annotations

import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


PROJECT = Path(__file__).resolve().parents[1]
MAIN = PROJECT / "app" / "src" / "main"
BASE_XML = MAIN / "res" / "values" / "strings.xml"
RU_XML = MAIN / "res" / "values-ru" / "strings.xml"
SUPPORTED_CATALOGS = {
    "ru": MAIN / "res" / "values-ru" / "strings.xml",
    "de": MAIN / "res" / "values-de" / "strings.xml",
    "fr": MAIN / "res" / "values-fr" / "strings.xml",
    "es": MAIN / "res" / "values-es" / "strings.xml",
    "pt-BR": MAIN / "res" / "values-pt-rBR" / "strings.xml",
    "ja": MAIN / "res" / "values-ja" / "strings.xml",
    "zh-CN": MAIN / "res" / "values-zh-rCN" / "strings.xml",
}
KOTLIN_ROOT = MAIN / "java"
GRADLE = PROJECT / "app" / "build.gradle.kts"
RESOURCE_PROPERTIES = MAIN / "res" / "resources.properties"
LANGUAGE_SOURCE = KOTLIN_ROOT / "org" / "collegiumdesideriorum" / "e_e_launcher" / "LauncherLanguage.kt"
FORMAT_TOKEN = re.compile(r"%(?:(\d+)\$)?([a-zA-Z%])")
RESOURCE_REF = re.compile(r"R\.string\.([A-Za-z0-9_]+)")
CYRILLIC = re.compile(r"[\u0400-\u04ff]")
CONCATENATED_RESOURCES = re.compile(r"l10n\([^)]*\)\s*\+\s*l10n\(", re.DOTALL)
STATIC_ENUM_RESOURCE = re.compile(r"^\s*[A-Z][A-Z0-9_]*\s*\([^\n]*l10n\(", re.MULTILINE)
TRANSLATION_MARKER = re.compile(r"__[A-Z_]{3,}__")


def read_catalog(path: Path) -> tuple[dict[str, str], set[str]]:
    root = ET.parse(path).getroot()
    values: dict[str, str] = {}
    non_translatable: set[str] = set()
    for entry in root.findall("string"):
        name = entry.attrib["name"]
        if name in values:
            raise ValueError(f"duplicate string resource {name!r} in {path}")
        values[name] = "".join(entry.itertext())
        if entry.attrib.get("translatable") == "false":
            non_translatable.add(name)
    return values, non_translatable


def placeholders(value: str) -> list[tuple[str | None, str]]:
    return [match.groups() for match in FORMAT_TOKEN.finditer(value) if match.group(2) != "%"]


def fail(errors: list[str], message: str) -> None:
    errors.append(message)


def main() -> int:
    errors: list[str] = []
    base, non_translatable = read_catalog(BASE_XML)
    expected_translated = set(base) - non_translatable
    catalog_sizes: dict[str, int] = {}
    for language_tag, path in SUPPORTED_CATALOGS.items():
        translated, _ = read_catalog(path)
        catalog_sizes[language_tag] = len(translated)
        missing = sorted(expected_translated - set(translated))
        extra = sorted(set(translated) - set(base))
        if missing:
            fail(errors, f"{language_tag} catalog is missing: {', '.join(missing)}")
        if extra:
            fail(errors, f"{language_tag} catalog has unknown entries: {', '.join(extra)}")
        for name in sorted(expected_translated & set(translated)):
            if placeholders(base[name]) != placeholders(translated[name]):
                fail(
                    errors,
                    f"{language_tag} placeholder mismatch for {name}: "
                    f"{placeholders(base[name])} != {placeholders(translated[name])}",
                )
            if TRANSLATION_MARKER.search(translated[name]):
                fail(errors, f"{language_tag} contains an unfinished marker in {name}")

    kotlin_sources = list(KOTLIN_ROOT.rglob("*.kt"))
    all_kotlin = "\n".join(path.read_text(encoding="utf-8") for path in kotlin_sources)
    missing_refs = sorted(set(RESOURCE_REF.findall(all_kotlin)) - set(base))
    if missing_refs:
        fail(errors, f"Kotlin references missing resources: {', '.join(missing_refs)}")
    if CYRILLIC.search(all_kotlin):
        fail(errors, "Cyrillic UI text remains in Kotlin sources")
    if CONCATENATED_RESOURCES.search(all_kotlin):
        fail(errors, "A user-facing sentence is assembled by concatenating l10n() calls")
    if STATIC_ENUM_RESOURCE.search(all_kotlin):
        fail(errors, "An enum caches a translated l10n() value at process initialization")

    gradle_text = GRADLE.read_text(encoding="utf-8")
    for setting in ("isPseudoLocalesEnabled = true", "generateLocaleConfig = true"):
        if setting not in gradle_text:
            fail(errors, f"Missing Gradle localization setting: {setting}")
    if not RESOURCE_PROPERTIES.exists() or "unqualifiedResLocale=en" not in RESOURCE_PROPERTIES.read_text(encoding="utf-8"):
        fail(errors, "The unqualified English resource locale is not declared")

    expected_directories = {path.parent.name for path in SUPPORTED_CATALOGS.values()}
    actual_directories = {
        path.parent.name for path in (MAIN / "res").glob("values-*/strings.xml")
    }
    if actual_directories != expected_directories:
        fail(
            errors,
            "Unexpected release locale directories: "
            f"expected {sorted(expected_directories)}, got {sorted(actual_directories)}",
        )

    language_source = LANGUAGE_SOURCE.read_text(encoding="utf-8")
    for language_tag in ("en", *SUPPORTED_CATALOGS):
        if f'"{language_tag}"' not in language_source:
            fail(errors, f"The in-launcher selector is missing {language_tag}")

    if errors:
        for error in errors:
            print(f"ERROR: {error}", file=sys.stderr)
        return 1

    longest = max(len(value) for value in base.values())
    print(
        "LOCALIZATION_VALIDATION_OK "
        f"base={len(base)} translated={len(expected_translated)} "
        f"catalogs={len(SUPPORTED_CATALOGS)} kotlin_files={len(kotlin_sources)} "
        f"longest_base_string={longest} pseudo_locales=en-XA,ar-XB "
        f"sizes={catalog_sizes}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
