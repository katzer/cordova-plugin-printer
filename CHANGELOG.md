## ChangeLog
#### Version 0.7.2 (not yet released)
- [__change__:] Changed plugin ID to `cordova-plugin-printer`
- [__change__:] Plugin requires Android KitKat or newer
- [__change__:] `isAvailable` returns false if no enabled print services can be found (Android)
- [enhancement:] `isAvailable` returns additional list of available print services (Android)
- [enhancement:] `print` returns bool value to indicate the result
- [enhancement:] Support `duplex` attribute (Android)


#### Version 0.7.1 (23.04.2015)
- [bugfix:] `isAvailable` does not block the main thread anymore.
- [bugfix:] iPad+iOS8 incompatibility (Thanks to __zmagyar__)
- [enhancement:] Print-View positioning on iPad
- [enhancement:] Send direct to printer when printerId: is specified.

#### Version 0.7.0 (12.09.2014)
- Android Printing Framework support
- [__change__:] Renamed `isServiceAvailable` to `isAvailable`
- [enhancement:] New print options like `name`, `landscape` or `duplex`
- [enhancement:] Ability to print remote content via URI
- [enhancement:] Callback support