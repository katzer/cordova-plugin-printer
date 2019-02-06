
<p align="left">
    <b><a href="https://github.com/katzer/cordova-plugin-printer/blob/example/README.md">SAMPLE APP</a> :point_right:</b>
</p>

# Cordova Print Plugin <br> [![npm version](https://badge.fury.io/js/cordova-plugin-printer.svg)](http://badge.fury.io/js/cordova-plugin-printer) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![PayPayl donate button](https://img.shields.io/badge/paypal-donate-yellow.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=L3HKQCD9UA35A "Donate once-off to this project using Paypal")

<img width="280px" align="right" hspace="20" vspace="10" src="https://github.com/katzer/cordova-plugin-printer/blob/example/images/print-ios.png">

Plugin for [Cordova][cordova] to print documents or photos from iOS, Android and Windows Universal apps.

### Supported Printer Interfaces

- Apple AirPrint
- Android Print
- Windows Print

### Supported Content

- HTML
- Text
- Base64
- Images
- PDF

### Supported Platforms

- Android 4.4+
- iOS 10+
- Windows 10 UWP


## Installation

The plugin can be installed via [CLI][CLI] and is publicly available on [NPM][npm].

Execute from the projects root folder:

    $ cordova plugin add cordova-plugin-printer

Or install a specific version:

    $ cordova plugin add cordova-plugin-printer@VERSION

Or install the latest head version:

    $ cordova plugin add https://github.com/katzer/cordova-plugin-printer.git

Or install from local source:

    $ cordova plugin add <path> --nofetch --nosave

And then execute:

    cordova build


## Basics

The plugin creates the object `cordova.plugins.printer` and is accessible after the *deviceready* event has been fired.

To print plain text:

```javascript
cordova.plugins.printer.print("Hello\nWorld!");
```

Or HTML+CSS:

```javascript
cordova.plugins.printer.print('<h1>Hello World!</h1>');
```

Or images or documents:

```javascript
cordova.plugins.printer.print('file://img/logo.png');
```

Or Base64 content:

```javascript
cordova.plugins.printer.print('base64://...');
```

## Formatting

It's possible to pass by additional format options. The list of possible options depend on the platform and the content type:

```javascript
cordova.plugins.printer.print(content, options);
```

TODO

The method accepts a list of attributes. Not all are supported on each platform and by each printer!

| Name | Description | Type | Platform |
|:---- |:----------- |:----:| --------:|
| name | The name of the print job and of the document | String | all |
| duplex | Specifies the duplex mode to use for the print job.<br>Either double-sided on short site (duplex:'short'), double-sided on long site (duplex:'long') or single-sided (duplex:'none').<br>Defaults to: 'none' | String | all |
| landscape| The orientation of the printed content, portrait or landscape.<br>Defaults to: false | Boolean | all |
| graystyle | If your application only prints black text, setting this property to _true_ can result in better performance in many cases.<br>Defaults to: false | Boolean | all |
| printerId | The network URL to the printer. | String | iOS |
| border | Set to _false_ to skip any border. Useful for fullscreen images.<br>Defaults to: true | Boolean | iOS |
| hidePageRange | Set to _true_ to hide the control for the page range.<br>Defaults to: false | Boolean | iOS |
| hideNumberOfCopies | Set to _true_ to hide the control for the number of copies.<br>Defaults to: false | Boolean | iOS |
| hidePaperFormat | Set to _true_ to hide the control for the paper format.<br>Defaults to: false | Boolean | iOS |
| paperWidth | Ability to hint width of the paper – iOS will get a printer supported paperformat which fits the best to this width. Only works when `paperHeight` is given. Width in millimeters. | Number | iOS |
| paperHeight | Ability to hint height of the paper – iOS will get a printer paperformat which fits the best to this heigth. Only works when `paperWidth` is given. Height in millimeters. | Number | iOS |
| paperCutLength | On roll-fed printers you can decide after how many milimeters the printer cuts the paper. | Number | iOS |
| bounds | The Size and position of the print view<br>Defaults to: [40, 30, 0, 0] | Array | iPad |

Use `getDefaults()` or `setDefaults()` to specify default options.


## Direct Print

For iOS its possible to send the content directly to the printer without any dialog. Todo so pass the network URL as an option:

```javascript
cordova.plugins.printer.print(content, { printer: 'ipp://...' });
```

To let the user pick an available printer:

```javascript
cordova.plugins.printer.pick(function (url) {});
```

__Note:__ By passing an invalid URL, the application will throw an `Unable to connect to (null)` exception and possibly crash.


## Printable Document Types

The list of supported document types differ between mobile platforms. As of writing, Windows UWP only supports HTML and plain text.

To get a list of all printable document types:

```javascript
cordova.plugins.printer.getPrintableTypes(callback);
```

To check if printing is supported in general:

```javascript
cordova.plugins.printer.canPrintItem(callback);
```

Or in articular:

```javascript
cordova.plugins.printer.canPrintItem('file://css/index.css', callback);
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

© 2013 [appPlant GmbH][appplant]


[cordova]: https://cordova.apache.org
[CLI]: http://cordova.apache.org/docs/en/edge/guide_cli_index.md.html#The%20Command-line%20Interface
[npm]: https://www.npmjs.com/package/cordova-plugin-printer
[apache2_license]: http://opensource.org/licenses/Apache-2.0
[appplant]: www.appplant.de
