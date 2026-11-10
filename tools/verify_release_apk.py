#!/usr/bin/env python3
"""Fail-closed, local checks for e_e launcher GitHub Release APK candidates.

Requires Android SDK build-tools (aapt and apksigner). Does not upload files,
inspect secret signing passwords, or replace an on-device install check.
"""
from __future__ import annotations

import argparse
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys

ROOT = Path(__file__).resolve().parent.parent
BUILD = ROOT / "app" / "build.gradle.kts"


def sdk_tool(name: str) -> str:
    executable = shutil.which(name)
    if executable:
        return executable
    sdk_roots = [os.environ.get("ANDROID_HOME"), os.environ.get("ANDROID_SDK_ROOT")]
    local_properties = ROOT / "local.properties"
    if local_properties.exists():
        for line in local_properties.read_text(encoding="utf-8").splitlines():
            if line.startswith("sdk.dir="):
                sdk_roots.append(line.partition("=")[2].replace("\\:", ":").replace("\\\\", "\\"))
    candidates = []
    for sdk_root in filter(None, sdk_roots):
        candidates.extend((Path(sdk_root) / "build-tools").glob(f"*/{name}"))
    candidates = [path for path in candidates if path.is_file() and os.access(path, os.X_OK)]
    if candidates:
        return str(sorted(candidates, key=lambda p: [int(n) for n in re.findall(r"\d+", p.parent.name)])[-1])
    raise RuntimeError(f"Android SDK tool '{name}' not found; add build-tools to PATH or set ANDROID_HOME")


def run(*cmd: str) -> str:
    result = subprocess.run(cmd, text=True, capture_output=True, check=False)
    if result.returncode:
        detail = (result.stderr or result.stdout).strip()
        raise RuntimeError(f"{Path(cmd[0]).name} failed: {detail}")
    return result.stdout


def gradle_field(contents: str, field: str) -> str:
    match = re.search(rf"\b{re.escape(field)}\s*=\s*(\"[^\"]+\"|\d+)", contents)
    if not match:
        raise RuntimeError(f"Could not read {field} from app/build.gradle.kts")
    return match.group(1).strip('"')


def badging_field(contents: str, field: str) -> str:
    match = re.search(rf"\b{re.escape(field)}='([^']+)'", contents)
    if not match:
        raise RuntimeError(f"Could not read {field} from APK badging")
    return match.group(1)


def assert_boolean_false(xmltree: str, name: str) -> None:
    for line in xmltree.splitlines():
        if f"android:{name}(" not in line:
            continue
        match = re.search(r"\(type 0x12\)0x([0-9a-fA-F]+)", line)
        if not match:
            raise RuntimeError(f"Cannot determine AndroidManifest android:{name}: {line.strip()}")
        if int(match.group(1), 16):
            raise RuntimeError(f"Forbidden android:{name}=true in release APK")


def normalize_fingerprint(value: str) -> str:
    return re.sub(r"[^a-fA-F0-9]", "", value).lower()


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("apk", type=Path, help="APK to inspect")
    parser.add_argument(
        "--expected-cert-sha256",
        default=os.environ.get("EE_EXPECTED_CERT_SHA256"),
        help="Expected SHA-256 of the signing certificate; may also be provided by EE_EXPECTED_CERT_SHA256",
    )
    args = parser.parse_args()
    apk = args.apk.resolve()
    if not apk.is_file() or not apk.suffix.lower() == ".apk":
        raise RuntimeError(f"APK not found: {apk}")
    aapt, apksigner = sdk_tool("aapt"), sdk_tool("apksigner")
    gradle = BUILD.read_text(encoding="utf-8")
    expected_package = gradle_field(gradle, "applicationId")
    expected_code = gradle_field(gradle, "versionCode")
    expected_name = gradle_field(gradle, "versionName")

    badging = run(aapt, "dump", "badging", str(apk))
    actual_package = badging_field(badging, "name")
    actual_code = badging_field(badging, "versionCode")
    actual_name = badging_field(badging, "versionName")
    for field, expected, actual in (
        ("package", expected_package, actual_package),
        ("versionCode", expected_code, actual_code),
        ("versionName", expected_name, actual_name),
    ):
        if actual != expected:
            raise RuntimeError(f"Mismatch: {field} expected {expected!r}, got {actual!r}")
    if re.search(r"^application-debuggable\b", badging, re.MULTILINE):
        raise RuntimeError("APK is debuggable; build assembleRelease, not assembleDebug")
    manifest = run(aapt, "dump", "xmltree", str(apk), "AndroidManifest.xml")
    assert_boolean_false(manifest, "testOnly")
    assert_boolean_false(manifest, "debuggable")

    signature_report = run(apksigner, "verify", "--verbose", "--print-certs", str(apk))
    if not re.search(r"Verified using v[23] scheme.*:\s*true", signature_report):
        raise RuntimeError("APK has no verified v2/v3 signature")
    fingerprints = re.findall(
        r"Signer #\d+ certificate SHA-256 digest:\s*([a-fA-F0-9:]+)", signature_report
    )
    if len(fingerprints) != 1:
        raise RuntimeError("Expected exactly one signing certificate in APK")
    actual_fingerprint = normalize_fingerprint(fingerprints[0])
    if len(actual_fingerprint) != 64:
        raise RuntimeError("Unable to parse signing certificate SHA-256")
    if args.expected_cert_sha256:
        expected_fingerprint = normalize_fingerprint(args.expected_cert_sha256)
        if len(expected_fingerprint) != 64 or actual_fingerprint != expected_fingerprint:
            raise RuntimeError("APK signing certificate does not match the expected SHA-256")
    else:
        print("WARNING: no expected certificate fingerprint provided; verify this is your original signing key")

    print(f"PASS: package={actual_package}, versionName={actual_name}, versionCode={actual_code}")
    print("PASS: APK signature verifies; non-debuggable and non-testOnly")
    print(f"Signing certificate SHA-256: {actual_fingerprint}")
    print("NEXT: install using adb install -r on the intended device (never uninstall to work around signature mismatch)")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (RuntimeError, OSError) as exc:
        print(f"FAIL: {exc}", file=sys.stderr)
        sys.exit(1)
