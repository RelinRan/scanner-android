# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning 2.0.0](https://semver.org/).

## [1.1.0] - 2026-07-11

First normalized open-source release.

### Added
- Professional bilingual documentation (English default, Simplified Chinese) with cross-language links.
- MIT License.
- Software NOTICE (third-party attribution: ZXing core, AndroidX, Material Components).
- GitHub Actions workflow to automatically build and publish the AAR to GitHub Releases on tag push.
- Semantic Versioning: single source of truth for `VERSION_NAME` / `VERSION_CODE` in `gradle.properties`.

### Changed
- Rewrote README with accurate API, attributes table, and corrected repository (`RelinRan/Scanner`).
- Normalized version from legacy date-based scheme (`2022.5.12.1`) / `1.0` to SemVer `1.1.0`.
- Repository hygiene: added `.gitignore` and stopped tracking IDE/build artifacts (`.idea/`, `.gradle/`, `local.properties`).

### Removed
- Stray root-level build artifact `zxing_code_2022.5.12.1.aar`.

## [1.0.6] - 2021-06-30

- Huawei device preview compatibility.

## [1.0.5] - 2021

- ZXReader recognition-rate improvements.

## [1.0.0] - 2021-02-25

- Initial release: camera scanning, image decoding, barcode/QR generation.

[1.1.0]: https://github.com/RelinRan/Scanner/releases/tag/v1.1.0
[1.0.6]: https://github.com/RelinRan/Scanner/releases/tag/1.0.6
[1.0.5]: https://github.com/RelinRan/Scanner/releases/tag/1.0.5
[1.0.0]: https://github.com/RelinRan/Scanner/releases/tag/1.0.0
