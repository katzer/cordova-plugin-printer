PrinterProxy = {
  isServiceAvailable: function (successCallback, failCallback, args) {
    args[0](true);
  },

  print: function (successCallback, failCallback, args) {
    window.printContent = args[0];

    Windows.Graphics.Printing.PrintManager.showPrintUIAsync();
  },

  printTaskRequested: function (printEvent) {
    printEvent.request.createPrintTask("Print", function (args) {
      var documentFragment = document.createDocumentFragment();
      var content = document.createElement("html");
      content.innerHTML = window.printContent;
      documentFragment.appendChild(content);
      args.setSource(MSApp.getHtmlPrintDocumentSource(documentFragment));
    });
  }
};

var printManager = Windows.Graphics.Printing.PrintManager.getForCurrentView();
printManager.onprinttaskrequested = PrinterProxy.printTaskRequested;

require("cordova/windows8/commandProxy").add("Printer", PrinterProxy);