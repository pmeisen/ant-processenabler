var fs = module.require('fs');
var amdclean = require('amdclean');

var cleanedCode = amdclean.clean({
  'globalModules': [${javascript.npm.globalModules}],
  'prefixTransform': function(name) {
    return name.substr(name.lastIndexOf('_') + 1);
  },
  'filePath': '${javascript.npm.singleFile}'
});
fs.writeFileSync('${javascript.npm.singleFile}', cleanedCode);