
<p align="left">
    <b><a href="https://github.com/katzer/cordova-plugin-printer/blob/example/README.md">SAMPLE APP</a> :point_right:</b>
</p>

# Cordova Print Plugin <br> [![npm version](https://badge.fury.io/js/cordova-plugin-printer.svg)](http://badge.fury.io/js/cordova-plugin-printer) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![PayPayl donate button](https://img.shields.io/badge/paypal-donate-yellow.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=L3HKQCD9UA35A "Donate once-off to this project using Paypal")

Plugin for [Cordova][cordova] to print documents, photos, HTML and plain text from iOS, Android and Windows Universal apps.

```js
cordova.plugins.printer.print('<b>Hello Cordova!</b>');
```

<img width="280px" align="right" src="https://github.com/katzer/cordova-plugin-printer/blob/example/images/print.png">

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
- Browser

## Basics

The plugin creates the object `cordova.plugins.printer` and is accessible after the *deviceready* event has been fired.

```js
document.addEventListener('deviceready', function () {
    // cordova.plugins.printer is now available
}, false);
```

Prints the contents of the web view:

```javascript
cordova.plugins.printer.print();
```

Plain text:

```javascript
cordova.plugins.printer.print("Hello\nWorld!");
```

HTML & CSS:

```javascript
cordova.plugins.printer.print('<h1>Hello World!</h1>');
```

Images, PDF and other documents:

```javascript
cordova.plugins.printer.print('file://img/logo.png');
```

Base64 encoded content:

```javascript
cordova.plugins.printer.print('base64://...');
```

__Note:__ On the browser platform the plugin only supports to print the contents of the web view.

## Formatting

It's possible to pass format options to the print method that overrides the defaults:

```javascript
cordova.plugins.printer.print(content, options, callback);
```

The defaults are defined as follows:

```javascript
cordova.plugins.printer.setDefaults({ monochrome: true });
```

The list of possible options depend on the platform, the content type and the capabilities of the printer.

| Name | Description | Type | Platform |
|:---- |:----------- |:----:| --------:|
| name | The name of the print job and of the document. | String | all |
| copies | The number of copies for the print task. | Number | iOS<br>Windows |
| pageCount | Limits the pages to print even the document contains more.<br>To skip the last n pages you can assign a negative value on iOS. | Number | iOS<br>Android |
| duplex | Either double-sided on short site (duplex:'short'), double-sided on long site (duplex:'long') or single-sided (duplex:'none'). | String | all |
| orientation | The orientation of the printed content, `portrait` or `landscape`. | String | all |
| monochrome | If your application only prints black text, setting this property to _true_ can result in better performance in many cases. | Boolean | all |
| photo | Set to _true_ to change the media type to photography for higher quality. | Boolean | iOS<br>Windows |
| autoFit | Set to _false_ to disable downscaling the image to fit into the content aread. | Boolean | Android |
| printer | The network URL to the printer. | String | iOS |
| maxHeight<br>maxWidth | Defines the maximum size of the content area. | Unit | iOS |
| margin.top<br>margin.left<br>margin.right<br>margin.bottom | The margins for each printed page. Each printer might have its own minimum margins depends on media type and paper format. | Unit | iOS |
| ui.hideNumberOfCopies | Set to _true_ to hide the control for the number of copies. | Boolean | iOS |
| ui.hidePaperFormat | Set to _true_ to hide the control for the paper format. | Boolean | iOS |
| ui.top<br>ui.left | The position of the printer picker. | Number | iPad |
| ui.height<br>ui.width | The size of the printer picker. | Number | iPad |
| paper.width<br>paper.height | The dimensions of the paper – iOS will will try to choose a format which fits bests. | Unit | iOS |
| paper.name | The name of the format like `IsoA4` or `Roll22Inch`.<br>https://docs.microsoft.com/en-us/uwp/api/windows.graphics.printing.printmediasize | String | Windows |
| paper.length | On roll-fed printers you can decide when the printer cuts the paper. | Unit | iOS |
| font.name | The name of the font family | String | iOS |
| font.size | The size of the font | Number | iOS<br>Android |
| font.italic<br>font.bold | Set to _true_ to enable these font traits. | Boolean | iOS |
| font.align | Possible alignments are `left`, `right`, `center` and `justified`. | String | iOS |
| font.color | The color of the font in hexa-decimal RGB format - `"FF0000"` means red. | String | iOS |
| header.height<br>footer.height | The height of the header or footer on each page. | Unit | iOS |
| header.labels<br>footer.labels | An array of labels to display. Only use if there are more then one. | Array | iOS |
| header.label.text<br>footer.label.text | The plain text to display. Use `%ld` to indicate where to insert the page index.<br>For example `"Page %ld"` would result into `"Page 1"`, `"Page 2"`, ... | String | iOS |
| header.label.top<br>header.label.right<br>header.label.left<br>header.label.bottom<br>footer.label.* | The relative position where to place the label within the footer or header area. | Unit | iOS |
| header.label.font<br>footer.label.font | The font attributes for the label. | Object | iOS |
| header.label.showPageIndex<br>footer.label.showPageIndex | Set to _true_ if you want to display the page index.<br> | Boolean | iOS |

The `Unit` type can be either a (float) number or a string with a special suffix.

- Supported unit suffixes are `in` for inches, `mm` for millimeters, `cm` for centimeters and `pt` for points
- `"2in"` are two inches whereas `2.0` or `"2.0pt"` are identical for two points
- One inch are 72.0 points

## Direct Print

For iOS its possible to send the content directly to the printer without any dialog. Todo so pass the network URL as an option:

```javascript
cordova.plugins.printer.print(content, { printer: 'ipp://...' });
```

To let the user pick an available printer:

```javascript
cordova.plugins.printer.pick(function (url) {});
```

It's possible to specify the position of the picker:

```javascript
cordova.plugins.printer.pick({ top: 40, left: 30 }, callback);
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

Or in particular:

```javascript
cordova.plugins.printer.canPrintItem('file://css/index.css', callback);
```

## Sample

```js
var options = {
    font: {
        size: 22,
        italic: true,
        align: 'center'
    },
    header: {
        height: '6cm',
        label: {
            text: "\n\nDie Freuden",
            font: {
                bold: true,
                size: 37,
                align: 'center'
            }
        }
    },
    footer: {
        height: '4cm',
        label: {
            text: 'Johann Wolfgang von Goethe, 1749-1832, deutscher Dichter, Naturforscher',
            font: { align: 'center' }
        }
    }
};

cordova.plugins.printer.print("Es flattert um die Quelle\nDie wechselnde Libelle,...", options);
```

The result will look like this for iOS:

![ttt](https://github.com/katzer/cordova-plugin-printer/blob/example/images/sample.png)

## Installation

Execute from the projects root folder:

    $ cordova plugin add cordova-plugin-printer

Or install a specific version:

    $ cordova plugin add cordova-plugin-printer@VERSION

Or install the latest head version:

    $ cordova plugin add https://github.com/katzer/cordova-plugin-printer.git

Or install from local source:

    $ cordova plugin add <path> --nofetch --nosave

Then execute:

    cordova build

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
[apache2_license]: http://opensource.org/licenses/Apache-2.0
[appplant]: www.appplant.de
