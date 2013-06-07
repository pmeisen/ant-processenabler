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

var quoteAntProperties = function(value) {
  var propertyRegEx = "\\$\\{([^\\}]+)\\}";
  var propertyPattern = java.util.regex.Pattern.compile(propertyRegEx);
  var propertyMatcher = propertyPattern.matcher(value);

  var offset = 0;
  while (propertyMatcher.find(offset)) {    
    var start = propertyMatcher.start();
    var end = propertyMatcher.end();
      
    // make sure that the property is not replaced by ant and quote it if necessary
    value = new java.lang.String(value.substring(0, start) + "$" + value.substring(start));
    offset = end + 1;
    
    // let's examine th new value
    propertyMatcher = propertyPattern.matcher(value);
  }

  return value;
}

var replaceProperties = function(value, properties, marker, nested) {
  if (properties == null || properties instanceof java.util.Map == false) {
    return value;
  } else {
    var propertyMarker = marker == null ? "$" : marker;
    var replaceNested = nested == null ? true : nested;
    var propertyRegEx = "\\Q" + propertyMarker + "\\E\\{([^\\}]+)\\}";
    var propertyPattern = java.util.regex.Pattern.compile(propertyRegEx);
    var propertyMatcher = propertyPattern.matcher(value);

    // create the properties to be replaced
    var modProperties = new java.util.Properties();
    
    // the @ needs lowerCase properties as well
    if (propertyMarker.equals("@")) {
      var iterator = properties.keySet().iterator();
      while(iterator.hasNext()) {
        var p = iterator.next();
        modProperties.put(p.toLowerCase(), properties.get(p));
      }
    }
    
    // everything else should be case-sensitive
    modProperties.putAll(properties);
    
    // replace the properties
    var newValue = value;
    var propertyRegEx = "\\Q" + propertyMarker + "\\E\\{([^\\}]+)\\}";
    var propertyPattern = java.util.regex.Pattern.compile(propertyRegEx);
    var propertyMatcher = propertyPattern.matcher(newValue);
    
    var offset = 0;
    while (propertyMatcher.find(offset)) {    
      var propName = propertyMatcher.group(1);
      var propValue = modProperties.getProperty(propName);
      var start = propertyMatcher.start();
      var end = propertyMatcher.end();
      
      if (propValue == null) {
        offset = end;
      } else {
      
        // make sure that the property is not replaced by ant and quote it if necessary
        newValue = new java.lang.String(newValue.substring(0, start) + propValue + newValue.substring(end));
        
        // increase the offset if we are done here
        if (!replaceNested) {
          offset = start + propValue.length();
        }
      }
     
      // let's examine th new value
      propertyMatcher = propertyPattern.matcher(newValue);
    }
    
    return newValue;
  }
}