// define a global variable used to surpress outputs
var global_echo_silent = false;

// function to do some logging
var echo = function(message, level) {
  // set a default level
  level = level == null ? "info" : level;

  if (global_echo_silent && !"error".equals(level))
    return;
  
  // create the task to write the message
  var echoTask = project.createTask("echo");
  var logLevel = new org.apache.tools.ant.taskdefs.Echo.EchoLevel();
  logLevel.setValue(level.toLowerCase());
  echoTask.setMessage(message);
  echoTask.setLevel(logLevel);
  echoTask.execute();
}

// function to fail
var fail = function(message) {
  // create the task to fail with the message
  var failTask = project.createTask("fail");
  failTask.setMessage(message);
  failTask.execute();
}

// function which returns null if the passed value is null or an empty string, otherwise the value
var checkEmpty = function(value) {
  return value == null || "".equals(value) ? null : value;
}

// function to set a variable
var setVar = function(name, value) {
  var setVarTask = project.createTask("var");
  setVarTask.setName(name); 
  setVarTask.setValue(value); 
  setVarTask.execute();
}

// creates an XML document for the specified file
var createXmlDocument = function(fileLocation) {
  // get the file and the read the XML-Document
  var xmlFile = new java.io.File(fileLocation);

  if (xmlFile.isFile() && xmlFile.exists() && xmlFile.canRead()) {
    var dbFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
    var dBuilder = dbFactory.newDocumentBuilder();
    var doc = dBuilder.parse(xmlFile);
    
    // normalize - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
    doc.getDocumentElement().normalize();
    
    return doc;
  } else {
    return null;
  } 
}

// remove whitespaces in XML
var removeWhiteSpacesInXml = function(node) {

  var xpathFactory = javax.xml.xpath.XPathFactory.newInstance();
  var xpathExp = xpathFactory.newXPath().compile("//text()[normalize-space(.) = '']");  
  var emptyTextNodes = xpathExp.evaluate(node, javax.xml.xpath.XPathConstants.NODESET);
  for (var i = 0; i < emptyTextNodes.getLength(); i++) {
    var emptyTextNode = emptyTextNodes.item(i);
    emptyTextNode.getParentNode().removeChild(emptyTextNode);
  }
}

var isString = function(string) {
  return typeof string == "string" || string instanceof java.lang.String;
}

var checkFile = function(file, mustExist) {
  var retFile = null;
  
  // make sure we have a file as URI, String or File
  if (isString(file) || file instanceof java.net.URI) {
    retFile = java.io.File(file);
  } else if (file instanceof java.io.File) {
    retFile = new java.io.File(file);
  }
  
  // make sure the file exists
  if (mustExist && retFile != null && (!retFile.exists() || !retFile.isFile())) {
    return null;
  } else {
    return retFile;
  }
}