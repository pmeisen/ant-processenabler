var parseDependencies = function(pomfile) {
  var doc = createXmlDocument(pomfile);
    
  if (doc == null) {
    fail("The pom-file '" + pomfile + "' could not be read");
    return null;
  } else {
    var rootNode = doc.getDocumentElement();

    // collection to keep the dependencies
    var definedDependencies = new java.util.ArrayList();
    
    // get the direct children
    var attributeNodes = rootNode.getChildNodes();
    for (var i = 0; i < attributeNodes.getLength(); i++) {
      var attributeNode = attributeNodes.item(i);
      var nodeName = attributeNode.getNodeName();
      
      if (PomStructure.dependenciesNode.equalsIgnoreCase(nodeName)) {
      
        // validate the attributes
        var depsNodes = attributeNode.getChildNodes();
        for (var k = 0; k < depsNodes.getLength(); k++) {
          var depsNode = depsNodes.item(k);
          var depsNodeName = depsNode.getNodeName();
        
          if (PomStructure.dependencyNode.equalsIgnoreCase(depsNodeName)) {
            var dependency = parseDependency(depsNode);
            definedDependencies.add(dependency);
          }
        }
      } 
    }
    
    return definedDependencies;
  }
}

var parseDependency = function(node) {
  
  var definedDependency = new Dependency();
  var children = node.getChildNodes();
  for (var i = 0; i < children.getLength(); i++) {
    var child = children.item(i);
    var childName = child.getNodeName();
        
    if (PomStructure.groupIdNode.equalsIgnoreCase(childName)) {
      definedDependency.setGroupId(child.getTextContent());
    } else if (PomStructure.artifactIdNode.equalsIgnoreCase(childName)) {
      definedDependency.setArtifactId(child.getTextContent());
    } else if (PomStructure.versionNode.equalsIgnoreCase(childName)) {
      definedDependency.setVersion(child.getTextContent());
    } else if (PomStructure.typeNode.equalsIgnoreCase(childName)) {
      definedDependency.setType(child.getTextContent());
    } else if (PomStructure.classifierNode.equalsIgnoreCase(childName)) {
      definedDependency.setClassifier(child.getTextContent());
    }
  }

  return definedDependency;
}