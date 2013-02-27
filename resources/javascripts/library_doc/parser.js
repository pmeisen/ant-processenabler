var parseLibraries = function(libraryFiles) {
  if (typeof libraryFiles == "string" || libraryFiles instanceof java.lang.String) {
    libraryFiles = libraryFiles.split(",");
  } else if (libraryFiles.constructor === Array) {
    // nothing to do
  } else if (libraryFiles instanceof java.util.Collection) {
    var tmpLibraryFiles = new Array();
    var iterator = libraryFiles.iterator();
    while(iterator.hasNext()) {
      tmpLibraryFiles.push(iterator.next());
    }
    libraryFiles = tmpLibraryFiles;
  }
  
  // set the global variable global_echo_silent to surpress any message
  var tmpSilent = global_echo_silent;
  global_echo_silent = true;
  
  var libraries = new Array();
  for (var i = 0; i < libraryFiles.length; i++) {
    var library = parseLibrary(libraryFiles[i]);
    libraries.push(library);
  }
  
  global_echo_silent = tmpSilent;
  
  return libraries;
}

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

// function to parse a library
var parseLibrary = function(fileLocation) {
  var targetNode   = "target";
  var propertyNode = "property";
  var macroNode    = "macrodef";
  var scriptNode   = "scriptdef";

  // read the document
  var doc = createXmlDocument(fileLocation);
  if (doc == null) {
    echo("The file '" + fileLocation + "' could not be found or is not readable", "error");
    return null;
  } else {
  
    var basedir = project.getProperty("main.root");
    var rootNode = doc.getDocumentElement();
    
    // get some information
    var name     = rootNode.getAttribute("name");
    var file     = getSubPath(basedir, fileLocation, true);
    var comment  = parseComment(rootNode);

    // create the Library
    var library = new Library();
    library.setName(name);
    library.setFile(file);
    library.setComment(comment);
    
    // validate the parameters
    if (library.comment.params.length > 0) {
      echo("The library '" + library.name + "' has parameters defined which is invalid for libraries.", "warn");
    }
    
    // get the direct childrens
    var attributeNodes = rootNode.getChildNodes();
    for (var temp = 0; temp < attributeNodes.getLength(); temp++) {
      var attributeNode = attributeNodes.item(temp);
      var nodeName = attributeNode.getNodeName();
      
      if (targetNode.equals(nodeName)) {
        library.addElement(parseTargetNode(attributeNode)); 
      } else if (propertyNode.equals(nodeName)) {
        library.addElement(parsePropertyNode(attributeNode)); 
      } else if (macroNode.equals(nodeName)) {
        library.addElement(parseMacroNode(attributeNode)); 
      } else if (scriptNode.equals(nodeName)) {
        library.addElement(parseScriptNode(attributeNode)); 
      }
    }
    
    return library;
  }
}

// function to parse a comment
var parseComment = function(textComment) {
  var comment = new Comment();
  
  if (textComment instanceof org.w3c.dom.Node) {
    if (textComment.getNodeType() != org.w3c.dom.Node.COMMENT_NODE) {
      textComment = findCommentNode(textComment);
    }
    
    // get the content of the textComment
    textComment = textComment == null ? null : textComment.getNodeValue();
  }
  
  // make sure we have a textComment
  if (textComment != null && !"".equals(textComment.trim())) {
    textComment = textComment.trim();
    
    // the markers used in the comment
    var validMarker = "@param|@fails|@author|@since";
    
    // get the description
    var descRegEx = "^\\s*((?:(?!^\\s*(?:" + validMarker + ")).)*)";
    var descPattern = java.util.regex.Pattern.compile(descRegEx, java.util.regex.Pattern.DOTALL + java.util.regex.Pattern.MULTILINE);
    var descMatcher = descPattern.matcher(textComment);
    if (descMatcher.find()) {
      comment.setDescription(descMatcher.group(1).trim());
    }
    
    // get the markers
    var markerRegEx = "^\\s*(" + validMarker + ")((?:(?!^\\s*(?:" + validMarker + ")).)*)";
    var markerPattern = java.util.regex.Pattern.compile(markerRegEx, java.util.regex.Pattern.DOTALL + java.util.regex.Pattern.MULTILINE);
    var markerMatcher = markerPattern.matcher(textComment);
    
    // check the marker
    while (markerMatcher.find()) {
      var marker = markerMatcher.group(1).trim();
      var value  = markerMatcher.group(2).trim();
      if ("@param".equals(marker)) {
        if (comment.hasParam(value)) {
          echo("The parameter '" + value + "' is defined multiple times in '" + textComment + "'", "error");
        } else {
          comment.addParam(value);
        }
      } else if ("@fails".equals(marker)) {
        comment.setFails(value);
      } else if ("@author".equals(marker)) {
        comment.setAuthor(value);
      } else if ("@since".equals(marker)) {
        comment.setSince(value);
      }
    }
  }
  
  return comment;
}

// function to parse a parameter, i.e. <parameter> <parameterdescription>
var parseParameter = function(textParameter) {
  var parameter = null;
  var paramRegEx = "^([^\\s\\[\\]]+)(?:\\[([^\\s\\[\\]]*)\\])?\\s*(.*)$";
  var paramPattern = java.util.regex.Pattern.compile(paramRegEx, java.util.regex.Pattern.DOTALL);
  var paramMatcher = paramPattern.matcher(textParameter);
  
  // check if there is really a parameter
  if (paramMatcher.find()) {
    parameter = new Parameter();
    parameter.setName(paramMatcher.group(1));
    parameter.setDefault(paramMatcher.group(2));
    parameter.setDescription(paramMatcher.group(3));
  }
  
  return parameter;
}

var parseNodeContent = function(node) {

  // remove all the whitespaces to ensure nice indent
  var xpathFactory = javax.xml.xpath.XPathFactory.newInstance();
  var xpathExp = xpathFactory.newXPath().compile("//text()[normalize-space(.) = '']");  
  var emptyTextNodes = xpathExp.evaluate(node, javax.xml.xpath.XPathConstants.NODESET);
  for (var i = 0; i < emptyTextNodes.getLength(); i++) {
    var emptyTextNode = emptyTextNodes.item(i);
    emptyTextNode.getParentNode().removeChild(emptyTextNode);
  }
  
  // now use the transformer to format
  var transformer = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
  transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
  transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
  transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
  var xmlOutput = new javax.xml.transform.stream.StreamResult(new java.io.StringWriter());
  transformer.transform(new javax.xml.transform.dom.DOMSource(node), xmlOutput);
  
  return xmlOutput.getWriter().toString();
}

// function to find the first comment placed before a node
var findCommentNode = function(node) {
  var commentNode;

  // make sure we have an element node to be checked
  if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
  
    // look for the elements first none text node
    var prevNode = node.getPreviousSibling();
    while (prevNode != null && prevNode.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
      prevNode = prevNode.getPreviousSibling();
    }
    
    // check if the node is a comment-node, if so we found it
    commentNode = prevNode != null && prevNode.getNodeType() == org.w3c.dom.Node.COMMENT_NODE ? prevNode : null;
  } else {
    commentNode = null;
  }
  
  return commentNode;
}

// function to parse the information provided for a target node
var parseTargetNode = function(node) {
  var name        = node.getAttribute("name");
  var description = node.hasAttribute("description") ? node.getAttribute("description") : null;
  var comment     = parseComment(node);
  var content     = parseNodeContent(node);
  
  // get the element
  var element = new Element("target", name, comment, content);
  
  // validate the description
  if (description != null && element.comment.description != null) {
    echo("The " + node.getNodeName() + " '" + element.name + "' has two descriptions defined:", "warn");
    echo("- one via the comment ('" + element.comment.description + "') and", "warn");
    echo("- one via the attribute ('" + description + "').", "warn");
    echo("The one of the comment will be used!", "warn");
  } else if (description != null && element.comment.description == null) {
    echo("The " + node.getNodeName() + " '" + element.name + "' has no description defined via a comment, the one of the attribute will be used.", "info");
    element.comment.setDescription(description);
  } else if (element.comment.description != null && description == null) {
    // nicely done
  } else {
    echo("The " + node.getNodeName() + " '" + element.name + "' has no description defined, please do so via a comment.", "warn");
  }
  
  // validate the parameters
  if (element.comment.params.length > 0) {
    echo("The " + node.getNodeName() + " '" + element.name + "' has parameters defined which is invalid for targets.", "warn");
  }
  
  return element;
};

// function to parse the information provided for a property node
var parsePropertyNode = function(node) {
  var name          = node.getAttribute("name");
  var description   = node.hasAttribute("description") ? node.getAttribute("description") : null;
  var value         = node.getAttribute("value");
  var location      = node.getAttribute("location");
  var comment       = parseComment(node);
  var content       = parseNodeContent(node);

  // get the element
  var element = new Element("property", name, comment, content);
  
  // validate the name
  if (element.name == null) {
    // skip
    return null;
  }
  
  // add an extra value to this element
  element["value"] = node.hasAttribute("value") ? value : location;
  
  // validate the description
  if (description != null && element.comment.description != null) {
    echo("The " + node.getNodeName() + " '" + element.name + "' has two descriptions defined:");
    echo("- one via the comment ('" + element.comment.description + "') and");
    echo("- one via the attribute ('" + description + "').");
    echo("The one of the description will be used!", "warn");
    element.comment.setDescription(description);
  } else if (description != null && element.comment.description == null) {
    if (checkEmpty(description) == null) {
      echo("The " + node.getNodeName() + " '" + element.name + "' has an empty description defined!", "warn");
    } else {
      element.comment.setDescription(description);
    }
  } else if (element.comment.description != null && description == null) {
    echo("The " + node.getNodeName() + " '" + element.name + "' has no description defined via the attribute, the one of the comment will be used.", "info");
  } else {
    echo("The " + node.getNodeName() + " '" + element.name + "' has no description defined, please do so via the attribute!", "warn");
  }
  
  // return
  return element;
};

// function to parse the information provided for a macro node
var parseMacroNode = function(node) {
  var name          = node.getAttribute("name");
  var description   = node.hasAttribute("description") ? node.getAttribute("description") : null;
  var comment       = parseComment(node);
  var content       = parseNodeContent(node);
  
  // get the element
  var element = new Element("task", name, comment, content);
  
  // validate the name
  if (element.name == null) {
    // skip
    return null;
  } 
  
  // validate the description
  if (description != null && element.comment.description != null) {
    echo("The " + node.getNodeName() + " '" + element.name + "' has two descriptions defined:", "warn");
    echo("- one via the comment ('" + element.comment.description + "') and", "warn");
    echo("- one via the attribute ('" + description + "').", "warn");
    echo("The one of the comment will be used!", "warn");
  } else if (description != null && element.comment.description == null) {
    echo("The " + node.getNodeName() + " '" + element.name + "' has no description defined via a comment, the one of the attribute will be used.", "info");
    element.comment.setDescription(description);
  } else if (element.comment.description != null && description == null) {
    // nicely done
  } else {
    echo("The " + node.getNodeName() + " '" + element.name + "' has no description defined, please do so via a comment", "warn");
  }
  
  // validate the attributes
  var definedAttributes = new java.util.HashSet();
  var attributeNodes = node.getChildNodes();
  for (var temp = 0; temp < attributeNodes.getLength(); temp++) {
    var attributeNode = attributeNodes.item(temp);
    if (!"attribute".equals(attributeNode.getNodeName())) continue;
    
    // get the values of the attribute
    var attrName = attributeNode.getAttribute("name");
    var attrDescription = attributeNode.hasAttribute("description") ? attributeNode.getAttribute("description") : null;
    var attrDef = attributeNode.hasAttribute("default") ? attributeNode.getAttribute("default") : null;
    
    // make sure there is a name and add it for checking later
    if (checkEmpty(attrName) == null) continue;
    definedAttributes.add(attrName);
    
    // check the values of the attribute
    if (element.comment.hasParam(attrName)) {
      var param = element.comment.getParam(attrName);
      
      // validate the description
      if (attrDescription != null && param.description != null) {
        echo("The " + attributeNode.getNodeName() + " '" + attrName + "' of element '" + element.name + "' has two descriptions defined:", "warn");
        echo("- one via the comment ('" + param.description + "') and", "warn");
        echo("- one via the attribute ('" + attrDescription + "').", "warn");
        echo("The one of the comment will be used!", "warn");
      } else if (attrDescription != null && param.description == null) {
        echo("The " + attributeNode.getNodeName() + " '" + attrName + "' of element '" + element.name + "' has no description defined via a comment, the one of the attribute will be used.", "info");
        param.setDescription(attrDescription);
      } else if (param.description != null && attrDescription == null) {
        // nicely done
      } else {
        echo("The " + attributeNode.getNodeName() + " '" + attrName + "' of element '" + element.name + "' has no description defined, please do so via a comment", "warn");
      }
      
      // validate the default-value
      if (attrDef != null && param.def != null) {
      
        if (!attrDef.equals(param.def)) {
          echo("The " + attributeNode.getNodeName() + " '" + attrName + "' of element '" + element.name + "' has two different defaults defined:", "warn");
          echo("- one via the comment ('" + param.def + "') and", "warn");
          echo("- one via the attribute ('" + attrDef + "').", "warn");
          echo("The one of the attribute will be used!", "warn");
          param.setDefault(attrDef);
        } else {
          // just fine
        }
      } else if (attrDef != null && param.def == null) {
        // thats just ok, lets set it
        param.setDefault(attrDef);
      } else if (param.def != null && attrDef == null) {
        echo("The " + attributeNode.getNodeName() + " '" + attrName + "' of element '" + element.name + "' has a default ('" + param.def + "') documented but not defined.", "warn");
      } else {
        // both have none, thats fine
      }
    } else {
      echo("The " + attributeNode.getNodeName() + " '" + attrName + "' of element '" + element.name + "' is not recognized in the comment, please specify it, until then the values of the definition will be used!", "warn");
      element.comment.addParam(new Parameter(attrName, attrDescription, attrDef));
    }
  }
  for (var i = 0; i < element.comment.params; i++) {
    var param = element.comment.params[i];
    if (!definedAttributes.contains(param.name)) {
      echo("The attribute '" + param.name + "' of element '" + element.name + "' is not defined and only documented, please remove it from the comment!", "warn");
    }
  }
  
  // return
  return element;
};

// function to parse the information provided for a macro node
var parseScriptNode = function(node) {
  var name          = node.getAttribute("name");
  var comment       = parseComment(node);
  var content       = parseNodeContent(node);
  
  // get the element
  var element = new Element("task", name, comment, content);
  
  // validate
  if (element.name == null) {
    // skip
    return null;
  } 
  
  // validate the description
  if (element.comment.description == null) {
    echo("The " + node.getNodeName() + " '" + element.name + "' has no description defined, please do so via a comment", "warn");
  } else {
    // nicely done
  }
  
  // validate the attributes
  var definedAttributes = new java.util.HashSet();
  var attributeNodes = node.getChildNodes();
  for (var temp = 0; temp < attributeNodes.getLength(); temp++) {
    var attributeNode = attributeNodes.item(temp);
    if (!"attribute".equals(attributeNode.getNodeName())) continue;
    
    // get the values of the attribute
    var attrName = attributeNode.getAttribute("name");

    // make sure there is a name and add it for checking later
    if (checkEmpty(attrName) == null) continue;
    definedAttributes.add(attrName);
    
    // check the values of the attribute
    if (element.comment.hasParam(attrName)) {
      // just fine
    } else {
      echo("The " + attributeNode.getNodeName() + " '" + attrName + "' of element '" + element.name + "' is not recognized in the comment, please specify it, until then the values of the definition will be used!", "warn");
      element.comment.addParam(new Parameter(attrName));
    }
  }
  for (var i = 0; i < element.comment.params; i++) {
    var param = element.comment.params[i];
    if (!definedAttributes.contains(param.name)) {
      echo("The attribute '" + param.name + "' of element '" + element.name + "' is not defined and only documented, please remove it from the comment!", "warn");
    } else if (param.description == null) {
      echo("The attribute '" + param.name + "' of element '" + element.name + "' has no description specified, please do so in the comment!", "warn");
    }
  }
  
  // return
  return element;
};