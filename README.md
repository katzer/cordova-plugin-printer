Cordova Print Plugin
======================

[Cordova][cordova] plugin to print HTML documents using [AirPrint][AirPrint] for iOS and [Google Cloud Print][GCP] for Android.

### About Apple AirPrint
AirPrint is an Apple™ technology that helps you create full-quality printed output without the need to download or install drivers. AirPrint is built in to many printer models from most popular printer manufacturers. Just select an AirPrint printer on your local network to print from your favorite iOS or OS X app.
AirPrint printers are available for these devices when using the latest version of iOS available for them:
 - iPad (all models)
 - iPhone (3GS or later)
 - iPod touch (3rd generation or later)

### About Google Cloud Print
Google™ Cloud Print is a new technology that connects your printers to the web. Using Google Cloud Print, you can make your home and work printers available to you and anyone you choose, from the applications you use every day. Google Cloud Print works on your phone, tablet, Chromebook, PC, and any other web-connected device you want to print from.

### Supported Printers
Printing is only supported on AirPrint-enabled printers or Google Cloud Print-enabled printers. The following pages contain more information:
 - AirPrint-enabled printers: http://www.apple.com/ipad/features/airprint.html
 - Enabling AirPrint on your computer: http://reviews.cnet.com/8301-19512_7-20023976-233.html, or http://www.ecamm.com/mac/printopia/
 - Google Cloud-ready printers: http://www.google.com/cloudprint/learn/printers.html
 - Connect network printers with Google Cloud Print: https://support.google.com/cloudprint/answer/1686197?rd=1

### Plugin's Purpose
This Cordova plugin serves as a platform independent JavaScript bridge to call the underlying native SDKs.


## Supported Platforms
- **iOS** *(Print from iOS devices to AirPrint compatible printers)*<br>
See [Drawing and Printing Guide for iOS](http://developer.apple.com/library/ios/documentation/2ddrawing/conceptual/drawingprintingios/Printing/Printing.html) for detailed informations and screenshots.

- **Android** *(Print from Android devices to Google Cloud Print compatible printers)*


## Installation
The plugin can either be installed from git repository, from local file system through the [Command-line Interface][CLI] or cloud based through [PhoneGap Build][PGB].

### Local development environment
From master:
```bash
# ~~ from master ~~
cordova plugin add https://github.com/katzer/cordova-plugin-printer.git
```
from a local folder:
```bash
# ~~ local folder ~~
cordova plugin add de.appplant.cordova.plugin.cloudprint --searchpath path/to/plugin
```
or to use the last stable version:
```bash
# ~~ stable version ~~
cordova plugin add de.appplant.cordova.plugin.cloudprint
```

### PhoneGap Build
Add the following xml to your config.xml to always use the latest version of this plugin:
```xml
<gap:plugin name="de.appplant.cordova.plugin.cloudprint" />
```
or to use an specific version:
```xml
<gap:plugin name="de.appplant.cordova.plugin.cloudprint" version="0.6.0" />
```
More informations can be found [here][PGB_plugin].

### Removing the Plugin
Through the [Command-line Interface][CLI]:
```bash
cordova plugin rm de.appplant.cordova.plugin.printer
```


## ChangeLog
#### Version 0.6.0 (not yet released)
- AirPrint support
- Google Cloud Print support
- [__change__:] Renamed `isServiceAvailable` to `isAvailable`.

#### Further informations
- See [CHANGELOG.md][changelog] to get the full changelog for the plugin.


## Using the plugin
The plugin creates the object `cordova.plugins.printer` with the following methods:

1. [printer.isAvailable][available]
2. [printer.print][print]

### Plugin initialization
The plugin and its methods are not available before the *deviceready* event has been fired.

```javascript
document.addEventListener('deviceready', function () {
    // cordova.plugins.printer is now available
}, false);
```

### Find out if printing is available on the device
The device his printing capabilities can be reviewed through the `printer.isAvailable` interface.
You can use this function to hide print functionality from users who will be unable to use it.<br>
The method takes a callback function, passed to which is a boolean property. Optionally you can assign the scope in which the callback will be executed as a second parameter (default to *window*).

__Note:__ Printing is only available on devices capable of multi-tasking (iPhone 3GS, iPhone 4 etc.) running iOS 4.2 or later or when connected with the Internet (Android).<br>

```javascript
/**
 * Checks if the printer service is avaible (iOS)
 * or if connected to the Internet (Android).
 *
 * @param {Function} callback
 *      A callback function
 * @param {Object?} scope
 *      The scope of the callback (default: window)
 *
 * @return {Boolean}
 */
cordova.plugins.printer.isAvailable(
    function (isAvailable) {
        alert(isAvailable ? 'Service is available' : 'Service NOT available');
    }
);
```

### Send content to a printer
Content can be send to a printer through the `printer.print` interface.<br>
The method takes a string or a HTML DOM node. Optional parameters allows to specify the name of the document and a callback. The callback will be called if the user cancels or completes the print job.

#### Further informations
- See the [isAvailable][available] method to find out if printing is available on the device.
- All CSS rules needs to be embedded or accessible via absolute URLs in order to print out HTML encoded content.
- See the [examples][examples] to get an overview on how to use the plugin.

```javascript
/**
 * Sends the content to the Google Cloud Print service.
 *
 * @param {String} content
 *      HTML string or DOM node
 *      if latter, innerHTML is used to get the content
 * @param {Object} options
 *       Options for the print job
 * @param {Function?} callback
 *      A callback function
 * @param {Object?} scope
 *      The scope of the callback (default: window)
 */
cordova.plugins.printer.print(content, options, callback, scope);
```


## Examples
__NOTE:__ All CSS rules needs to be embedded or accessible via absolute URLs in order to print out HTML encoded content.

#### Print the whole HTML page
```javascript
// Either a DOM node or a string
var page = document.body;

cordova.plugins.printer.print(page, { name:'Document.html' }, function () {
	alert('printing finished or canceled')
});
```

#### Print custom specific content
```javascript
// Either a DOM node or a string
var page = '<h1>Hello Document</h1>';

cordova.plugins.printer.print(page, 'Document.html', function () {
    alert('printing finished or canceled')
});
```


## Quirks

### Testing in the iOS Simulator
There's no need to waste lots of paper when testing - if you're using the iOS simulator, select _File -> Open Printer Simulator_ to open some dummy printers (print outs will appear as PDF files).

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

__Note:__ you will need to add an extra top margin to new pages.


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
[GCP]: http://www.google.com/cloudprint/learn/index.html
[AirPrint]: http://support.apple.com/kb/ht4356
[CLI]: http://cordova.apache.org/docs/en/edge/guide_cli_index.md.html#The%20Command-line%20Interface
[PGB]: http://docs.build.phonegap.com/en_US/index.html
[PGB_plugin]: https://build.phonegap.com/plugins/
[changelog]: CHANGELOG.md
[available]: #find-out-if-printing-is-available-on-the-device
[print]: #send-content-to-a-printer
[examples]: #examples
[apache2_license]: http://opensource.org/licenses/Apache-2.0
[katzer]: katzer@appplant.de
[appplant]: www.appplant.de
