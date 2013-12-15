Cordova Printer-Plugin
======================

A bunch of printing plugins for Cordova 3.x.x

by Sebastián Katzer ([github.com/katzer](https://github.com/katzer))

## Supported Platforms
- **iOS** *(Print from iOS devices to AirPrint compatible printers)*<br>
See [Drawing and Printing Guide for iOS](http://developer.apple.com/library/ios/documentation/2ddrawing/conceptual/drawingprintingios/Printing/Printing.html) for detailed informations and screenshots.

- **Android** *(SDK 19)*

## Adding the Plugin to your project
Through the [Command-line Interface](http://cordova.apache.org/docs/en/3.0.0/guide_cli_index.md.html#The%20Command-line%20Interface):

```bash
cordova plugin add https://github.com/katzer/cordova-plugin-printer.git
```

## Removing the Plugin from your project
Through the [Command-line Interface](http://cordova.apache.org/docs/en/3.0.0/guide_cli_index.md.html#The%20Command-line%20Interface):
```
cordova plugin rm de.appplant.cordova.plugin.printer
```

## Release Notes
#### Version 0.5.0 (11.12.2013)
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

## Using the plugin
The plugin creates the object ```window.plugin.printer``` with two methods:

### isServiceAvailable()
Printing is only available on devices capable of multi-tasking (iPhone 3GS, iPhone 4 etc.) running iOS 4.2 or later. You can use this function to hide print functionality from users who will be unable to use it.<br>
Function takes a callback function, passed to which is a boolean property. Optionally you can assign the scope in which the callback will be executed as a second parameter (default to *window*).

```javascript
/*
 * Find out if printing is available. Use this for showing/hiding print buttons.
 */
window.plugin.printer.isServiceAvailable(
    function (isAvailable) {
        alert(isAvailable ? 'Service is available' : 'Service NOT available');
    }
);
```

### print()
Function takes an html string.

**Note:** All required CSS rules needs to be included as well.

```javascript
// Get HTML string
var page = document.body.innerHTML;

// Pass the HTML
window.plugin.printer.print(page);
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

This software is released under the [Apache 2.0 License](http://opensource.org/licenses/Apache-2.0).
