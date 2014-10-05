requirejs.config({

  // define the baseUrl defined by the processenabler
  baseUrl: 'scripts',
  
  // add some test-function for the testcase
  testFunction: function(a, b) { return a + (a * b); },
  
  // shim three.js it's not AMD conform
  shim: { 'three': { exports: 'THREE' }}
});

require(['source'], function(src) {
  // nothing to do
});