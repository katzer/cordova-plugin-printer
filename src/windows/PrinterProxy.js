PrinterProxy = {
  printDocumentSource: null,

  isAvailable: function (successCallback, failCallback, args) {
    successCallback(true, 0);
  },

  print: function (successCallback, failCallback, args) {
    var documentFragment = document.createDocumentFragment();
    var content = document.createElement("html");

    window.printContent = args[0];
    content.innerHTML = window.printContent;
    documentFragment.appendChild(content);
    PrinterProxy.getPrintDocumentSource(documentFragment).then(function(source) {
      PrinterProxy.printDocumentSource = source;
      Windows.Graphics.Printing.PrintManager.showPrintUIAsync();
    });
  },

  getPrintDocumentSource: function(documentFragment) {
    var promise;
    if(MSApp.getHtmlPrintDocumentSourceAsync) {
      promise = MSApp.getHtmlPrintDocumentSourceAsync(documentFragment);
    } else {
      promise = new WinJS.Promise(function(completeDispatch) {
        completeDispatch(MSApp.getHtmlPrintDocumentSource(documentFragment));
      });
    }

    return promise;
  },

  printTaskRequested: function (printEvent) {
    printEvent.request.createPrintTask("Print", function (args) {
      args.setSource(PrinterProxy.printDocumentSource);
    });
  }
};

var printManager = Windows.Graphics.Printing.PrintManager.getForCurrentView();
printManager.onprinttaskrequested = PrinterProxy.printTaskRequested;

require("cordova/exec/proxy").add("Printer", PrinterProxy);