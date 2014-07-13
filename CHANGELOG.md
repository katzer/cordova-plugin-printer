##ChangeLog
#### Version 0.6.0 (not yet released)
- [feature]: Added Windows8 support<br>
  *Thanks to* ***pirvudoru***

#### Version 0.5.3 (13.07.2014)
- [bugfix]: Printing wasn't possible because `isServiceAvailable` returns False IOS
- [bugfix]: Using plugin prevents openDatabase() on Android

#### Version 0.5.2 (22.03.2014)
- [bugfix:] `isServiceAvailable` on Android did not return a list of available printing apps.

#### Version 0.5.1 (15.12.2013)
- Removed Android KitKat support *(See kitkat branch)*

#### Version 0.5.0 (yanked)
- Release under the Apache 2.0 license.
- [***change:***] Removed the `callback` property from the `print` interface.
- [enhancement:] Added Android KitKat support<br>
  *Based on the Print Android plugin made by* ***Eion Robb***

#### Version 0.4.0 (24.08.2013)
- [feature]: Added Android support<br>
  *Based on the Print Android plugin made by* ***Eion Robb***
- [feature]: `print()` accepts a 4th arguments for platform specific properties.
- [change]: the callback of `print()` will be called with a result code about the user action.

#### Version 0.2.1 (13.08.2013)
- [feature]: Support for callback scopes.

#### Version 0.2.0 (11.08.2013)
- [feature]: Added iOS support<br>
  *Based on the Print iOS plugin made by* ***Randy McMillan***