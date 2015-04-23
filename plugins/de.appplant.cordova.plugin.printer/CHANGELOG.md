##ChangeLog
#### Version 0.6.2 (23.04.2015)
- [bugfix:] `isAvailable` did not check if a native Google Cloud Print App is available.
- [bugfix:] iPad+iOS8 incompatibility (Thanks to __zmagyar__)
- [enhancement:] Print-View positioning on iPad
- [enhancement:] Send direct to printer when printerId: is specified.

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