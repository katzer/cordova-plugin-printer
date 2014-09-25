
<p align="right">
    <a href="https://github.com/katzer/cordova-plugin-printer">master</a>
    <span>&nbsp;</span>
    <b><a href="#">v0.6</a></b>
    <span>&nbsp;</span>
    <a href="https://github.com/katzer/cordova-plugin-printer/tree/network-printer">v0.5</a>
    <span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
    <i><a href="https://github.com/katzer/cordova-plugin-printer/tree/example-google-cloud-print">EXAMPLE :point_right:</a></i>
</p>

Cordova Print Plugin
====================

[Cordova][cordova] plugin to print HTML documents using [__AirPrint__][AirPrint]  and [__Google Cloud Print__][GCP].

:bangbang:&nbsp;__Choose the right branch for you!__&nbsp;:bangbang:

The plugin provides multiple branches to support different printer types and android versions while _AirPrint_ is supported with each one.

- [master Branch][master_branch] for iOS and Android >= 4.4 (>= v0.7.x)
- [google-cloud-print Branch][google-cloud-print_branch] for Android <= 4.3 (~> v0.6.x)
- __Deprecated__ [network-printer Branch][network-printer_branch] for Android <= 4.3 (<= v0.5.x)

### About Apple AirPrint
AirPrint is an Apple™ technology that helps you create full-quality printed output without the need to download or install drivers. AirPrint is built in to many printer models from most popular printer manufacturers. Just select an AirPrint printer on your local network to print from your favorite iOS or OS X app.<br>
AirPrint printers are available for these devices when using the latest version of iOS available for them:
 - iPad (all models)
 - iPhone (3GS or later)
 - iPod touch (3rd generation or later)

<img src="http://static1.businessinsider.com/image/4cf67b8149e2aeb00b020000/only-12-printers-work-with-apples-airprint-heres-the-list.jpg" />

### About Google Cloud Print
Google™ Cloud Print is a new technology that connects your printers to the web. Using Google Cloud Print, you can make your home and work printers available to you and anyone you choose, from the applications you use every day. Google Cloud Print works on your phone, tablet, Chromebook, PC, and any other web-connected device you want to print from.

<img src="http://getandroidstuff.com/wp-content/uploads/2013/06/google-cloud-print-apk-for-andorid.jpg" />

### Supported Printers
Printing is only supported on AirPrint-enabled printers or Google Cloud Print-enabled printers. The following pages contain more information:
 - AirPrint-enabled printers: http://www.apple.com/ipad/features/airprint.html
 - Enabling AirPrint on your computer: http://reviews.cnet.com/8301-19512_7-20023976-233.html, or http://www.ecamm.com/mac/printopia/
 - Google Cloud-ready printers: http://www.google.com/cloudprint/learn/printers.html
 - Connect network printers with Google Cloud Print: https://support.google.com/cloudprint/answer/1686197?rd=1

### Plugin's Purpose
This Cordova plugin serves as a platform independent JavaScript bridge to call the underlying native SDKs.


## Overview
1. [Introduction](#cordova-print-plugin)
2. [Supported Platforms](#supported-platforms)
3. [Installation](#installation)
4. [ChangeLog](#changelog)
5. [Using the plugin](#using-the-plugin)
6. [Examples](#examples)
7. [Quirks](#quirks)


## Supported Platforms
- **iOS** *(Print from iOS devices to AirPrint compatible printers)*<br>
See [Drawing and Printing Guide for iOS][ios_guide] for detailed informations and screenshots.

- **Android** *(Print from Android devices to Google Cloud Print compatible printers)*


## Installation
The plugin can either be installed from git repository, from local file system through the [Command-line Interface][CLI]. Or cloud based through [PhoneGap Build][PGB].

### Local development environment
From master:
```bash
# ~~ from google-cloud-print branch ~~
cordova plugin add https://github.com/katzer/cordova-plugin-printer.git#google-cloud-print
```
from a local folder:
```bash
# ~~ local folder ~~
cordova plugin add de.appplant.cordova.plugin.printer --searchpath path/to/plugin
```
or to use the last stable version:
```bash
# ~~ stable version ~~
cordova plugin add de.appplant.cordova.plugin.printer@0.6.0
```

### PhoneGap Build
Add the following xml to your config.xml to always use the latest version of this plugin:
```xml
<gap:plugin name="de.appplant.cordova.plugin.printer" version="0.6.0" />
```
More informations can be found [here][PGB_plugin].

### Removing the Plugin
Through the [Command-line Interface][CLI]:
```bash
cordova plugin rm de.appplant.cordova.plugin.printer
```


## ChangeLog
#### Version 0.6.1 (25.09.2014)
- [enhancement:] Use native Google Cloud Print App if available
- [bugfix:] Keyboard input was ignored

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

#### Available Options
| Name | Description | Type | Support |
| ---- | ----------- |:----:| -------:|
| name | The name of the print job and of the document | String | all |
| printerId| An identifier of the printer to use for the print job. | String | iOS |
| duplex | Specifies the duplex mode to use for the print job.<br>Either double-sided (duplex:true) or single-sided (duplex:false).<br>Double-sided by default. | Boolean | iOS |
| landscape| The orientation of the printed content, portrait or landscape.<br>_Portrait_ by default. | Boolean | iOS |
| graystyle | If your application only prints black text, setting this property to _true_ can result in better performance in many cases.<br>_False_ by default. | Boolean | iOS |

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

#### 1. Print the whole HTML page
```javascript
// Either a DOM node or a string
var page = document.body;

cordova.plugins.printer.print(page, 'Document.html', function () {
	alert('printing finished or canceled')
});
```

#### 2. Print custom specific content
```javascript
// Either a DOM node or a string
var page = '<h1>Hello Document</h1>';

cordova.plugins.printer.print(page, 'Document.html', function () {
    alert('printing finished or canceled')
});
```

#### 3. Adjust the page
```javascript
cordova.plugins.printer.print('123', { name:'Document.html', landscape:true }, function () {
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

__Note:__ You will need to add an extra top margin to new pages.


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
[master_branch]: https://github.com/katzer/cordova-plugin-printer
[google-cloud-print_branch]: #
[network-printer_branch]: https://github.com/katzer/cordova-plugin-printer/tree/network-printer
[ios_guide]: http://developer.apple.com/library/ios/documentation/2ddrawing/conceptual/drawingprintingios/Printing/Printing.html
[CLI]: http://cordova.apache.org/docs/en/edge/guide_cli_index.md.html#The%20Command-line%20Interface
[PGB]: http://docs.build.phonegap.com/en_US/index.html
[PGB_plugin]: https://build.phonegap.com/plugins/1059
[changelog]: CHANGELOG.md
[available]: #find-out-if-printing-is-available-on-the-device
[print]: #send-content-to-a-printer
[examples]: #examples
[apache2_license]: http://opensource.org/licenses/Apache-2.0
[katzer]: katzer@appplant.de
[appplant]: www.appplant.de
