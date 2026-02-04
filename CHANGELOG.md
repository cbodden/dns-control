# Changelog

All notable changes to DNS Control will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.3] - 2026-02-04

### Added
- **Saved Server Profiles** - Save multiple server configurations with custom names
  - Quick-switch between servers via dropdown in Settings
  - Delete saved profiles with confirmation dialog
  - Profiles store server URL and API token together
- **Custom Date Range for Stats** - Select specific start and end dates/times for statistics
  - Date picker for selecting start and end dates
  - Time picker for selecting start and end times
  - Apply button to fetch stats for the custom range
- **Flexible Stats Time Periods** - Choose from Last Hour, Day, Week, Month, Year, or Custom
- **GitHub Link** - Clickable link to project repository in Settings under build info
- **Backup & Restore** - Export and import server profiles
  - Export saved servers to JSON file with date-stamped filename
  - Import servers from backup file (skips duplicates by URL)
  - Automatic Google Drive backup via Android Auto Backup

### Changed
- Stats dropdown now includes all time period options
- Settings UI reorganized with saved servers section at top
- Improved date/time formatting for custom range display
- Build number incremented

### Removed
- GitHub Actions workflow (build.yml)

## [0.2] - 2025-02-03

### Added
- **Dashboard Stats Tab** - View real-time query statistics from Technitium DNS Server
  - Query summary: total queries, no error, server failures, NX domain, refused
  - Resolution types: authoritative, recursive, cached
  - Blocking stats: blocked and dropped query counts
  - Server info: total clients, zones, cached entries
  - Zone lists: allowed/blocked zones, allow/block list zone counts
- **Bottom Navigation** - Tabbed interface with Control, Stats, and Settings tabs
- **Build Info Display** - Version and build timestamp shown in Settings
- **Debug Window Toggle** - Setting to show/hide raw JSON debug window on Control tab

### Changed
- Default server URL is now blank (was previously hardcoded)
- Improved error messages with more context for debugging
- Settings screen uses bottom tabs instead of back navigation
- Settings persisted using Android DataStore

### Fixed
- API response parsing to handle different Technitium response formats
- Stats endpoint error handling with detailed error messages

## [0.1] - 2025-01-01

### Added
- **One-Tap Disable** - Temporarily disable DNS blocking for 1-120 minutes
- **Real-Time Status** - View current blocking status and resume time
- **Relative Time Display** - Human-readable format ("in 5 min", "in 1h 30m")
- **Auto-Connect** - Automatic server connectivity check on app launch
- **Settings Persistence** - Server URL, API token, and duration saved locally
- **Modern UI** - Material 3 dark theme with smooth animations
- **Debug Mode** - Raw JSON response display with copy-to-clipboard
- **Lightweight** - Minimal permissions (INTERNET, ACCESS_NETWORK_STATE only)
