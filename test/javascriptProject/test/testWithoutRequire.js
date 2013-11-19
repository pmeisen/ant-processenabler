module('testSample');

test('testGlobalGimmeTwo', function() {
  equal(gimmeATwo(), 2);
  equal(gimmeASubFolderTwo(), 'subfolder 2');
});