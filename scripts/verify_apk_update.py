"""Verify release metadata and update compatibility using Android SDK tools."""
import argparse
from pathlib import Path
import re
import shlex
import subprocess

PACKAGE = "wseemann.media.romote.emerald"


def parse_metadata(output):
    line = next((line for line in output.splitlines() if line.startswith("package: ")), "")
    fields = dict(item.split("=", 1) for item in shlex.split(line.removeprefix("package: ")) if "=" in item)
    package = fields.get("name")
    code = fields.get("versionCode", "")
    if not package or not code.isdecimal():
        raise ValueError("Cannot identify APK package and versionCode")
    return package, int(code)


def parse_certificate(output):
    # Match the supported formats used by App-Store's verified signer parser.
    pattern = re.compile(
        r"^\s*(?:Signer (?:#\d+|\([^)]*\))|V\d+(?:\.\d+)? Signer:) certificate SHA-256 digest:\s*(.*)$",
        re.IGNORECASE,
    )
    lines = output.splitlines()
    digests = []
    for index, line in enumerate(lines):
        match = pattern.match(line)
        if match is None:
            continue
        raw = match.group(1).strip()
        if not raw and index + 1 < len(lines):
            raw = lines[index + 1].strip()
        digest = raw.replace(":", "").lower()
        if not re.fullmatch(r"[0-9a-f]{64}", digest):
            raise ValueError("Malformed signing certificate digest")
        digests.append(digest)
    if not digests:
        raise ValueError("Cannot identify verified APK signing certificate")
    if len(set(digests)) != 1:
        raise ValueError("Multiple distinct signing certificates; review required")
    return digests[0]


def inspect(path, tools):
    metadata = subprocess.run([str(tools / "aapt"), "dump", "badging", str(path)], check=True, capture_output=True, text=True).stdout
    signature = subprocess.run([str(tools / "apksigner"), "verify", "--print-certs", str(path)], check=True, capture_output=True, text=True).stdout
    package, code = parse_metadata(metadata)
    return package, code, parse_certificate(signature)


def validate(current, previous=None):
    if current[0] != PACKAGE:
        raise ValueError("Unexpected Android application ID")
    if previous is not None:
        if current[0] != previous[0]:
            raise ValueError("Android application ID changed")
        if current[2] != previous[2]:
            raise ValueError("Signing identity changed")
        if current[1] <= previous[1]:
            raise ValueError("versionCode has not increased")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--tools", required=True, type=Path)
    parser.add_argument("current", type=Path)
    parser.add_argument("previous", type=Path, nargs="?")
    args = parser.parse_args()
    current = inspect(args.current, args.tools)
    previous = inspect(args.previous, args.tools) if args.previous else None
    print(f"Current package={current[0]} versionCode={current[1]} certificate={current[2]}")
    if previous:
        print(f"Previous package={previous[0]} versionCode={previous[1]} certificate={previous[2]}")
    validate(current, previous)
    print("APK update compatibility verified.")


if __name__ == "__main__":
    main()
