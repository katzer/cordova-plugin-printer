Cordova Printer-Plugin
======================

A bunch of printing plugins for Cordova 3.x.x

by Sebastián Katzer ([github.com/katzer](https://github.com/katzer))

## Supported Platforms
- **iOS** *(Print from iOS devices to AirPrint compatible printers)*<br>
See [Drawing and Printing Guide for iOS](http://developer.apple.com/library/ios/documentation/2ddrawing/conceptual/drawingprintingios/Printing/Printing.html) for detailed informations and screenshots.

- **Android** *(Print through 3rd party printing apps)*


## Installation
The plugin can either be installed from git repository or from local file system through the [Command-line Interface][CLI].<br>
Or cloud based through [PhoneGap Build][PGB].

### Local development environment
From master:
```bash
# ~~ from master ~~
cordova plugin add https://github.com/katzer/cordova-plugin-printer.git && cordova prepare
```
from a local folder:
```bash
# ~~ local folder ~~
cordova plugin add de.appplant.cordova.plugin.printer --searchpath path/to/plugin && cordova prepare
```
or to use the last stable version:
```bash
# ~~ stable version ~~
cordova plugin add de.appplant.cordova.plugin.printer && cordova prepare
```

### PhoneGap Build
Add the following xml to your config.xml to always use the latest version of this plugin:
```xml
<gap:plugin name="de.appplant.cordova.plugin.printer" />
```
or to use an specific version:
```xml
<gap:plugin name="de.appplant.cordova.plugin.printer" version="0.5.2" />
```
More informations can be found [here][PGB_plugin].

### Removing the Plugin
Through the [Command-line Interface][CLI]:
```bash
cordova plugin rm de.appplant.cordova.plugin.printer
```


## ChangeLog
#### Version 0.6.0 (not yet released)
- [feature]: Added Windows8 support<br>
  *Thanks to* ***pirvudoru***

#### Version 0.5.3 (13.07.2014)
- [bugfix]: Printing wasn't possible because `isServiceAvailable` returns False IOS
- [bugfix]: Using plugin prevents openDatabase() on Android

#### Further informations
- See [CHANGELOG.md][changelog] to get the full changelog for the plugin.


## Using the plugin
The plugin creates the object `window.plugin.printer` with the following methods:

1. [plugin.printer.isServiceAvailable][available]
2. [plugin.printer.print][print]

### Plugin initialization
The plugin and its methods are not available before the *deviceready* event has been fired.

```javascript
document.addEventListener('deviceready', function () {
    // window.plugin.printer is now available
}, false);
```

### Find out if printing is available on the device
The device his printing capabilities can be reviewed through the `printer.isServiceAvailable` interface.
You can use this function to hide print functionality from users who will be unable to use it.<br>
The method takes a callback function, passed to which is a boolean property. Optionally you can assign the scope in which the callback will be executed as a second parameter (default to *window*).

__Note:__ Printing is only available on devices capable of multi-tasking (iPhone 3GS, iPhone 4 etc.) running iOS 4.2 or later or through a pre-installed printer app (Android).<br>

```javascript
/**
 * Checks if the printer service is avaible (iOS)
 * or if a printing app is installed on the device (Android).
 *
 * @param {Function} callback
 *      A callback function
 * @param {Object?} scope
 *      The scope of the callback (default: window)
 *
 * @return {Boolean}
 */
window.plugin.printer.isServiceAvailable(
    function (isAvailable) {
        alert(isAvailable ? 'Service is available' : 'Service NOT available');
    }
);
```

### Send content to a printer
Content can be send to a printer through the `printer.print` interface.<br>
The method takes a string or a HTML DOM node.

#### Further informations
- See the [isServiceAvailable][available] method to find out if printing is available on the device.
- All required CSS rules needs to be included in order to print HTML encoded content.
- On Android the functionality between different print apps may vary.

```javascript
/**
 * Sends the content to a printer app or service.
 *
 * @param {String} content
 *      HTML string or DOM node
 *      if latter, innerHTML is used to get the content
 * @param {Object?} options
 *      Platform specific options
 */
window.plugin.printer.print(content, options);
```


## Example
The following exmaple demonstrates how to print out the whole HTML page.

```javascript
// Get the content
var page = document.body;

// Pass to the printer
window.plugin.printer.print(page);
```


## Platform specifics

### Get all available printing apps on Android
The callback function will be called with a second argument which is an array, indicating which printer apps are available for printing.
```javascript
window.plugin.printer.isServiceAvailable(
    function (isAvailable, installedAppIds) {
        alert('The following print apps are installed on your device: ' + installedAppIds.join(', '));
    }
);
```

### Specify printing app on Android
An App-ID can be assigned as a platform configuration to indicate which 3rd party printing app shall be used. Otherwise the first found application will be used.
```javascript
window.plugin.printer.print(page, { appId: 'epson.print' });
```

## Quirks

### Testing in the iOS Simulator
There's no need to waste lots of paper when testing - if you're using the iOS simulator, select File->Open Printer Simulator to open some dummy printers (print outs will appear as PDF files).

### Adding Page Breaks to Printouts
Use the 'page-break-before' property to specify a page break, e.g.

```html
<p>
First page.
</p>

<p style="page-break-before: always">
Second page.
</p>
```

See W3Schools for more more information: http://www.w3schools.com/cssref/pr_print_pagebb.asp

Note: you will need to add an extra top margin to new pages.


### Printing on Real Printers (iOS)
Printing is only supported on AirPrint-enabled printers or with the use of third-party software on your computer. The following pages contain more information:
 - AirPrint-enabled printers: http://www.apple.com/ipad/features/airprint.html
 - Enabling AirPrint on your computer: http://reviews.cnet.com/8301-19512_7-20023976-233.html, or http://www.ecamm.com/mac/printopia/

## Contributing

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

## License

This software is released under the [Apache 2.0 License][apache2_license].

© 2013-2014 appPlant UG, Inc. All rights reserved


[cordova]: https://cordova.apache.org
[CLI]: http://cordova.apache.org/docs/en/edge/guide_cli_index.md.html#The%20Command-line%20Interface
[PGB]: http://docs.build.phonegap.com/en_US/3.3.0/index.html
[PGB_plugin]: https://build.phonegap.com/plugins/676
[changelog]: CHANGELOG.md
[available]: #find-out-if-printing-is-available-on-the-device
[print]: #send-content-to-a-printer
[apache2_license]: http://opensource.org/licenses/Apache-2.0
[katzer]: katzer@appplant.de
[appplant]: www.appplant.de
