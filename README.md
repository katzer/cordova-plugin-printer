Cordova Printer-Plugin
======================

A bunch of printing plugins for Cordova 3.x.x

by Sebastián Katzer ([github.com/katzer](https://github.com/katzer))

## Supported Platforms ##
- **iOS** *(Print from iOS devices to AirPrint compatible printers)*

## Adding the Plugin to your project ##
Through the [Command-line Interface](http://cordova.apache.org/docs/en/3.0.0/guide_cli_index.md.html#The%20Command-line%20Interface):

```bash
cordova plugin add https://github.com/katzer/cordova-plugin-printer.git
```

## Release Notes ##
#### Version 0.2.0 (11.08.2013) ####
- Added iOS support<br>
  *Based on the Print iOS plugin made by* ***Randy McMillan***

## Using the plugin ##
The plugin creates the object ```window.plugin.printer``` with two methods:

### isServiceAvailable() ###
Printing is only available on devices capable of multi-tasking (iPhone 3GS, iPhone 4 etc.) running iOS 4.2 or later. You can use this function to hide print functionality from users who will be unable to use it. Function takes a callback function, passed to which is a boolean property.

```javascript
/*
 * Find out if printing is available. Use this for showing/hiding print buttons.
 */
window.plugin.printer.isServiceAvailable(
    function (isAvailable) {
        alert(isavailable ? 'Service is available' : 'Service NOT available');
    }
);
```

### print() ###
Function takes an html string and (optionally) a success callback and a failure callback.

```javascript
// Get HTML string
var page = document.body.innerHTML;

/*
 * Pass an HTML and - optionally - success function, error function.
 */
window.plugin.printer.print(page);
```

#### Testing in the iOS Simulator ####
There's no need to waste lots of paper when testing - if you're using the iOS simulator, select File->Open Printer Simulator to open some dummy printers (print outs will appear as PDF files).

#### Adding Page Breaks to Printouts ####
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


#### Printing on Real Printers ####
Printing is only supported on AirPrint-enabled printers or with the use of third-party software on your computer. The following pages contain more information:
 - AirPrint-enabled printers: http://www.apple.com/ipad/features/airprint.html
 - Enabling AirPrint on your computer: http://reviews.cnet.com/8301-19512_7-20023976-233.html, or http://www.ecamm.com/mac/printopia/
