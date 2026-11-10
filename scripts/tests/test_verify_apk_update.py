from pathlib import Path
import sys
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from verify_apk_update import PACKAGE, parse_metadata, parse_certificate, validate

CERT = "8f7ee49d26e4c3ef23fd9f254f31a07d625a01493a9c81c98e01f12377f8b103"


class ReleaseVerificationTest(unittest.TestCase):
    def test_modern_package_metadata_does_not_match_codename(self):
        line = f"package: name='{PACKAGE}' versionCode='100017' versionName='2.4.1-emerald+ci.17' compileSdkVersionCodename='16'"
        self.assertEqual((PACKAGE, 100017), parse_metadata(line))

    def test_legacy_package_metadata(self):
        self.assertEqual((PACKAGE, 32), parse_metadata(f"package: name='{PACKAGE}' versionCode='32' versionName='2.2.0-emerald'"))

    def test_supported_signer_formats(self):
        for prefix in ["Signer #1", "Signer (minSdkVersion=26, maxSdkVersion=32)", "V3 Signer:", "V3.1 Signer:"]:
            for value in [CERT, ":".join(CERT[i:i+2] for i in range(0, 64, 2)).upper(), "\n" + CERT]:
                with self.subTest(prefix=prefix, value=value):
                    self.assertEqual(CERT, parse_certificate(f"{prefix} certificate SHA-256 digest: {value}"))

    def test_duplicate_same_certificate_is_accepted(self):
        self.assertEqual(CERT, parse_certificate(f"Signer #1 certificate SHA-256 digest: {CERT}\nSigner #2 certificate SHA-256 digest: {CERT}"))

    def test_ambiguous_or_malformed_or_missing_certificate_is_rejected(self):
        for text in ["", "Signer #1 certificate SHA-256 digest: malformed", f"Signer #1 certificate SHA-256 digest: {CERT}\nSigner #2 certificate SHA-256 digest: {'0'*64}"]:
            with self.subTest(text=text), self.assertRaises(ValueError):
                parse_certificate(text)

    def test_valid_update_is_accepted(self):
        validate((PACKAGE, 100017, CERT), (PACKAGE, 32, CERT))

    def test_package_signer_and_version_regressions_are_rejected(self):
        old = (PACKAGE, 37, CERT)
        for current in [("other.package", 100017, CERT), (PACKAGE, 100017, "0"*64), (PACKAGE, 37, CERT), (PACKAGE, 32, CERT)]:
            with self.subTest(current=current), self.assertRaises(ValueError):
                validate(current, old)


if __name__ == "__main__":
    unittest.main()
