// helper method to load js files
// thanks to http://stackoverflow.com/questions/650377/javascript-rhino-use-library-or-include-other-scripts
var importLibrary = function(property) {
  var jsFile = project.getProperty(property);
  var fileReader = new java.io.FileReader(jsFile);
  var fullRead = org.apache.tools.ant.util.FileUtils.readFully(fileReader);
  
  return "" + new java.lang.String(fullRead);
}

// load the libraries
eval(importLibrary("main.library.resfile.js.general"));