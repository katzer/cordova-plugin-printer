cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
  {
    "id": "cordova-plugin-printer.Printer",
    "file": "plugins/cordova-plugin-printer/www/printer.js",
    "pluginId": "cordova-plugin-printer",
    "clobbers": [
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
  },
  {
    "id": "cordova-plugin-x-toast.Toast",
    "file": "plugins/cordova-plugin-x-toast/www/Toast.js",
    "pluginId": "cordova-plugin-x-toast",
    "clobbers": [
      "window.plugins.toast"
    ]
  },
  {
    "id": "cordova-plugin-x-toast.ToastProxy",
    "file": "plugins/cordova-plugin-x-toast/src/windows/toastProxy.js",
    "pluginId": "cordova-plugin-x-toast",
    "merges": [
      ""
    ]
  }
];
module.exports.metadata = 
// TOP OF METADATA
{
  "cordova-plugin-printer": "0.8.0.alpha",
  "cordova-plugin-x-toast": "2.7.2"
};
// BOTTOM OF METADATA
});