cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
    {
        "file": "plugins/cordova-plugin-printer/www/printer.js",
        "id": "cordova-plugin-printer.Printer",
        "pluginId": "cordova-plugin-printer",
        "clobbers": [
            "cordova.plugins.printer"
        ]
    },
    {
        "file": "plugins/cordova-plugin-printer/src/browser/PrinterProxy.js",
        "id": "cordova-plugin-printer.Printer.Proxy",
        "pluginId": "cordova-plugin-printer",
        "runs": true
    },
    {
        "file": "plugins/cordova-plugin-x-toast/www/Toast.js",
        "id": "cordova-plugin-x-toast.Toast",
        "pluginId": "cordova-plugin-x-toast",
        "clobbers": [
            "window.plugins.toast"
        ]
    }
];
module.exports.metadata = 
// TOP OF METADATA
{
    "cordova-plugin-printer": "0.8.0.alpha",
    "cordova-plugin-x-toast": "2.7.2"
}
// BOTTOM OF METADATA
});