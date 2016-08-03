cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
    {
        "id": "cordova-plugin-printer.Printer",
        "file": "plugins/cordova-plugin-printer/www/printer.js",
        "pluginId": "cordova-plugin-printer",
        "clobbers": [
            "plugin.printer",
            "cordova.plugins.printer"
        ]
    },
    {
        "id": "cordova-plugin-printer.PrinterProxy",
        "file": "plugins/cordova-plugin-printer/src/windows/PrinterProxy.js",
        "pluginId": "cordova-plugin-printer",
        "merges": [
            ""
        ]
    }
];
module.exports.metadata = 
// TOP OF METADATA
{
    "cordova-plugin-printer": "0.7.2"
};
// BOTTOM OF METADATA
});