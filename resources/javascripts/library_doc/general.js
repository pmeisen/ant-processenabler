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
eval(importLibrary("file.library.resfile.js.general"));
eval(importLibrary("doc.library.resfile.js.datastructure"));
eval(importLibrary("doc.library.resfile.js.parser"));
eval(importLibrary("doc.library.resfile.js.writer"));

var createHtmlOutputSettings = function() {

  // define the outputSettings
  var template = project.getProperty("doc.template");
  var libraryTemplateFile = template + ".library.html";
  var indexTemplateFile = template + ".index.html";
  var propertiesTemplateFile = template + ".settings.properties";

  return new HtmlOutputSettings(libraryTemplateFile, indexTemplateFile, propertiesTemplateFile);
}