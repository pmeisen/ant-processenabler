module('testSample');

test('testRequireUsage', function() {
  
  var h = require('source');
  var s = require('subfolder/source');
  var $ = require('jquery');
  
  equal($("body>div").size(), 2);
  equal(s, 'subfolder 2');
  equal(h, 2);
});