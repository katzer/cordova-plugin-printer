/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
    initialize: function() {
        this.bindEvents();
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicitly call 'app.receivedEvent(...);'
    onDeviceReady: function() {
        app.receivedEvent('deviceready');
        document.getElementById('check').onclick = app.check;
        document.getElementById('pick').onclick = app.pick;
        document.getElementById('print').onclick = app.print;
    },
    // Update DOM on a Received Event
    receivedEvent: function(id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

        console.log('Received Event: ' + id);
    },
    // Check for available printer or support in general
    check: function () {
        cordova.plugins.printer.isAvailable(function (avail, count) {
            alert(avail ? 'Found ' + count + ' services' : 'No');
        });
    },
    // Pick a printer for future usage
    pick: function () {
        cordova.plugins.printer.pick(function (url) {
            alert(url ? url : 'Canceled');
            app.printerId = url;
        });
    },
    // Picked printer
    printerId: null,
    // Print sample content
    print: function () {
        var options = { name: 'awesome', printerId: app.printerId },
            page    = '';

        page += '<style type="text/css">html, body {width: 100%; height: 100%; margin: 0px; padding: 0px;}html {background: rgb(246, 248, 248);}body {font-family: "Open Sans", sans-serif;font-size: 14px;font-weight: 400;}.promos {position: absolute;left: 50%;top: 50%;transform: translate(-50%, -50%) scale(1.3);width: 800px;margin-left: 25px;}.promo {width: 250px;background: #0F1012;color: #f9f9f9;float: left;}.deal {padding: 10px 0 0 0;}.deal span {display: block;text-align: center;}.deal span:first-of-type {font-size: 23px;}.deal span:last-of-type {font-size: 13px;}.promo .price {display: block;width: 250px;background: #292b2e;margin: 15px 0 10px 0;text-align: center;font-size: 23px;padding: 17px 0 17px 0;}ul {display: block;margin: 20px 0 10px 0;padding: 0;list-style-type: none;text-align: center;color: #999999;}li {display: block;margin: 10px 0 0 0;}button {border: none;border-radius: 40px;background: #292b2e;color: #f9f9f9;padding: 10px 37px;margin: 10px 0 20px 60px;}.scale {transform: scale(1.2);box-shadow: 0 0 4px 1px rgba(20, 20, 20, 0.8);}.scale button {background: #64AAA4;}.scale .price {color: #64AAA4;}</style>';
        page += '<body><div class="promos"><div class="promo"><div class="deal"><span>Premium</span><span>This is really a good deal!</span></div><span class="price">$79</span><ul class="features"><li>Some great feature</li><li>Another super feature</li><li>And more...</li></ul><button>Sign up</button></div><div class="promo scale"><div class="deal"><span>Plus</span><span>This is really a good deal!</span></div><span class="price">$89</span><ul class="features"><li>Some great feature</li><li>Another super feature</li><li>And more...</li></ul><button>Sign up</button></div><div class="promo"><div class="deal"><span>Basic</span><span>Basic membership</span></div><span class="price">$69</span><ul class="features"><li>Choose the one on the left</li><li>We need moneyy</li><li>And more...</li></ul><button>Sign up</button></div></div></body>';

        cordova.plugins.printer.print(page, options, function (res) {
            alert(res ? 'Done' : 'Canceled');
        });
    }
};

if (window.hasOwnProperty('Windows')) {
    alert = function (msg) { new Windows.UI.Popups.MessageDialog(msg).showAsync(); };
}

app.initialize();
