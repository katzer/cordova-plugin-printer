
<p align="left">
    <b><a href="https://github.com/katzer/cordova-plugin-printer/blob/example/README.md">SAMPLE APP</a> :point_right:</b>
</p>

Cordova Print Plugin [![npm version](https://badge.fury.io/js/cordova-plugin-printer.svg)](http://badge.fury.io/js/cordova-plugin-printer)
====================

Plugin for the [Cordova][cordova] framework to print HTML from iOS, Android and Windows Universal apps.

<p align="center">
    <img width="23.8%" src="https://github.com/katzer/cordova-plugin-printer/blob/example/images/print-ios.png"></img>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <img width="26.8%" src="https://github.com/katzer/cordova-plugin-printer/blob/example/images/print-windows.png"></img>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <img width="23.8%" src="https://github.com/katzer/cordova-plugin-printer/blob/example/images/print-android.png"></img>
</p>

### About Apple AirPrint
AirPrint is an Apple™ technology that helps you create full-quality printed output without the need to download or install drivers. AirPrint is built in to many printer models from most popular printer manufacturers. Just select an AirPrint printer on your local network to print from your favorite iOS or OS X app.

See [Drawing and Printing Guide for iOS][ios_guide] for detailed informations. 

### About Android Printing Framework
Starting with _KitKat_, most Android devices have print service plugins installed to enable printing using the Google Cloud Print and Google Drive services. Print service plugins from other printer manufactures are available through the App Store though the Google Cloud Print service plugin can also be used to print from an Android device to just about any printer type and model.<br>
In addition to supporting physical printers, it is also possible to save printed output to your Google Drive account or locally as a PDF file on the Android device.

See [Building Apps with Multimedia for Android][android_guide] for detailed informations. 


## Supported Platforms
- iOS 8 or up
- Android KitKat or up
- Universal Windows Platform


## Installation
Install the latest version:

    cordova plugin add cordova-plugin-printer

Or a specific version:

    cordova plugin add cordova-plugin-printer@VERSION

Or the latest dev version:

    cordova plugin add https://github.com/katzer/cordova-plugin-printer.git

Or a custom version:

    cordova plugin add cordova-plugin-printer --searchpath path/to/plugin

And then execute:

    cordova build


## ChangeLog
#### Version 0.7.2 (03.08.2016)
- Finally on __NPM__
- __Windows__ support
- New __pick__ method
- Fixed iOS build issue
- Various enhancements

See [CHANGELOG.md][changelog] to get the full changelog for the plugin.


## Usage
The plugin and its methods are not available before the *deviceready* event has been fired.

```javascript
document.addEventListener('deviceready', function () {
    // cordova.plugins.printer is now available
}, false);
```

### Check printer
The device his printing capabilities can be reviewed through the `printer.check` interface. Use this function to hide print functionality from users who will be unable to use it.

```javascript
/**
 * Checks if the printer service is avaible (iOS)
 * or if printer services are installed and enabled (Android).
 *
 * @param {Function} callback
 *      A callback function
 * @param {Object} scope
 *      Optional scope of the callback
 *      Defaults to: window
 */
cordova.plugins.printer.check(function (avail, count) {
    alert(avail ? 'Found ' + count + ' services' : 'No');
});
```

### Pick a printer
Displays a system interface allowing the user to select an available printer. 
To speak with a printer directly you need to know the network address by picking them before via `printer.pick`.

Note that picking a printer is not supported for windows platform.

```javascript
/**
 * Displays system interface for selecting a printer.
 *
 * @param {Function} callback
 *      A callback function
 */
cordova.plugins.printer.pick(function (uri) {
    alert(uri ? uri : 'Canceled');
});
```

### Print content
Content can be send to a printer through the `printer.print` interface. The method takes a string with HTML content, an URI pointing to another web page or any DOM node.

```javascript
/**
 * Sends the content to print service.
 *
 * @param {String} content
 *      HTML string or DOM node
 *      if latter, innerHTML is used to get the content
 * @param {Object} options
 *       Options for the print job
 * @param {Function} callback
 *      An optional callback function
 * @param {Object} scope
 *      An optional scope of the callback
 *      Defaults to: window
 */
cordova.plugins.printer.print('<html>..</html>', { duplex: 'long' }, function (res) {
    alert(res ? 'Done' : 'Canceled');
});
```

The method accepts a list of attributes. Not all are supported on each platform and by each printer!

| Name | Description | Type | Platform |
| ---- | ----------- |:----:| --------:|
| name | The name of the print job and of the document | String | all |
| duplex | Specifies the duplex mode to use for the print job.<br>Either double-sided on short site (duplex:'short'), double-sided on long site (duplex:'long') or single-sided (duplex:'none').<br>Defaults to: 'none' | String | all |
| landscape| The orientation of the printed content, portrait or landscape.<br>Defaults to: false | Boolean | all |
| graystyle | If your application only prints black text, setting this property to _true_ can result in better performance in many cases.<br>Defaults to: false | Boolean | all |
| printerId | The network URL to the printer. | String | iOS |
| border | Set to _true_ to skip any border. Useful for fullscreen images.<br>Defaults to: true | Boolean | iOS |
| hidePageRange | Set to _true_ to hide the control for the page range.<br>Defaults to: false | Boolean | iOS |
| hideNumberOfCopies | Set to _true_ to hide the control for the number of copies.<br>Defaults to: false | Boolean | iOS |
| hidePaperFormat | Set to _true_ to hide the control for the paper format.<br>Defaults to: false | Boolean | iOS |
| bounds | The Size and position of the print view<br>Defaults to: [40, 30, 0, 0] | Array | iPad |

#### Further informations
- All CSS rules needs to be embedded or accessible via absolute URLs in order to print out HTML encoded content.
- The string can contain HTML content or an URI pointing to another web page.


## Examples
__NOTE:__ All CSS rules needs to be embedded or accessible via absolute URLs in order to print out HTML encoded content.

Print the whole HTML page:

```javascript
var page = location.href;

cordova.plugins.printer.print(page, 'Document.html');
```

Print the content from one part of the page:

```javascript
var page = document.getElementById('legal-notice');

cordova.plugins.printer.print(page, 'Document.html');
```

Print some custom content:

```javascript
var page = '<h1>Hello Document</h1>';

cordova.plugins.printer.print(page, 'Document.html');
```

Print a remote web page:

```javascript
cordova.plugins.printer.print('http://blackberry.de', 'BB10');
```

Send to printer directly:

```javascript
cordova.plugins.printer.pick(function (uri) {
    cordova.plugins.printer.print(page, { printerId: uri });
});
```


## Contributing

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

## License

This software is released under the [Apache 2.0 License][apache2_license].

Made with :yum: from Leipzig

© 2016 [appPlant GmbH][appplant]


[cordova]: https://cordova.apache.org
[ios_guide]: http://developer.apple.com/library/ios/documentation/2ddrawing/conceptual/drawingprintingios/Printing/Printing.html
[android_guide]: https://developer.android.com/training/building-multimedia.html
[changelog]: CHANGELOG.md
[check]: #check-printer
[pick]: #pick-a-printer
[print]: #print-content
[apache2_license]: http://opensource.org/licenses/Apache-2.0
[appplant]: www.appplant.de
