// sleep.js - sleeps for the specified number of seconds in argument
//
// usage:
//   cscript //NoLogo sleep.js 10

var WshShell = WScript.CreateObject("WScript.Shell");
var seconds = 0;
var ms = 0;

if (WScript.Arguments.count() == 1) {
  seconds = WScript.Arguments(0);
  ms = seconds * 1000;
} else {
  WScript.Echo("usage: sleep.js [seconds]");
}

WScript.Sleep(ms);
