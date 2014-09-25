##ChangeLog
#### Version 0.6.1 (25.09.2014)
- [enhancement:] Use native Google Cloud Print App if available
- [bugfix:] Keyboard input was ignored

#### Version 0.6.0 (12.09.2014)
- Google Cloud Print support
- [_change_:] Renamed `isServiceAvailable` to `isAvailable`.
- [enhancement:] New print options like `name`, `landscape` or `duplex`.
- [enhancement:] Ability to print remote content via URI (iOS only)
- [enhancement:] Callback support
- [bugfix:] `isAvailable` does not block the main thread anymore.