
# Cordova Print Plugin - Sample App

<img height="560px" align="right" src="images/overview.png">

Plugin for [Cordova][cordova] to print documents, photos, HTML and plain text from iOS, Android and Windows Universal apps.

Clone the _example_ branch:

    git clone -b example https://github.com/katzer/cordova-plugin-printer.git

Build the project:

    cordova build [ios|android|windows|browser]

Then execute:

    cordova run [ios|android|windows|browser]

These will lunch the simulator or any plugged in device and start the example application. It is also possible to open the project with [Android Studio][studio], [Xcode][xcode] or [Visual Studio][vs].

Please follow the plugin's [README][readme] for further requirements and informations.

## Testing in the iOS Simulator

There's no need to waste lots of paper when testing - if you're using the iOS simulator, install the [Hardware IO Tools for Xcode][xcode_io_tools] and select _Xcode -> Open Developer Tool -> Printer Simulator_ to open some dummy printers.

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
[readme]: https://github.com/katzer/cordova-plugin-printer/blob/master/README.md
[studio]: https://developer.android.com/sdk/installing/studio.html
[xcode]: https://developer.apple.com/xcode/
[vs]: https://www.visualstudio.com
[xcode_io_tools]: https://developer.apple.com/downloads/index.action?name=Hardware%20IO%20Tools%20for%20Xcode
[apache2_license]: http://opensource.org/licenses/Apache-2.0
[appplant]: www.appplant.de
