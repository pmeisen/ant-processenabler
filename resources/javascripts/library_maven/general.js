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

var mapPom = function(pomfile, outfile, fallbackGroupId, fallbackArtifactId) {
  var doc = createXmlDocument(pomfile);
  
  if (doc == null) {
    fail("The pom-file '" + pomfile + "' could not be read");
    return null;
  } else {
    var rootNode = doc.getDocumentElement();
    
    // get the direct depNode and iterate
    var mainGroupIdNode = null;
    var mainArtifactIdNode = null;
    var mainNodes = rootNode.getChildNodes();
    for (var i = 0; i < mainNodes.getLength(); i++) {
      var mainNode = mainNodes.item(i);
      var mainNodeName = mainNode.getNodeName();
      
      // check the important values
      if (PomStructure.groupIdNode.equals(mainNodeName)) {
        mainGroupIdNode = mainNode;
      } else if (PomStructure.artifactIdNode.equals(mainNodeName)) {
        mainArtifactIdNode = mainNode;
      } else if (PomStructure.parentNode.equalsIgnoreCase(mainNodeName)) {
        mapDependency(mainNode);
      } else if (PomStructure.dependencyMgmNode.equalsIgnoreCase(mainNodeName)) {
        var depMgmNodes = mainNode.getChildNodes();
        
        // find the dependencies node
        var depsNodes = null;
        for (var k = 0; k < depMgmNodes.getLength(); k++) {
          if (PomStructure.dependenciesNode.equalsIgnoreCase(depMgmNodes.item(k).getNodeName())) {
            depsNodes = depMgmNodes.item(k);
            break;
          }
        }
        
        if (depsNodes != null) {        
          for (var k = 0; k < depsNodes.getLength(); k++) {
            var depsNode = depsNodes.item(k);
            var depsNodeName = depsNode.getNodeName();
          
            if (PomStructure.dependencyNode.equalsIgnoreCase(depsNodeName)) {
              mapDependency(depsNode);
            }
          }
        }
      } else if (PomStructure.dependenciesNode.equalsIgnoreCase(mainNodeName)) {
      
        // validate the attributes
        var depsNodes = mainNode.getChildNodes();
        for (var k = 0; k < depsNodes.getLength(); k++) {
          var depsNode = depsNodes.item(k);
          var depsNodeName = depsNode.getNodeName();
        
          if (PomStructure.dependencyNode.equalsIgnoreCase(depsNodeName)) {
            mapDependency(depsNode);
          }
        }
      } 
    }
    
    // make sure we have a group and artifact
    var groupId = mainGroupIdNode == null ? fallbackGroupId : mainGroupIdNode.getTextContent().trim();
    var artifactId = mainArtifactIdNode == null ? fallbackArtifactId : mainArtifactIdNode.getTextContent().trim();
    
    // validate
    if (checkEmpty(fallbackGroupId) != null && !fallbackGroupId.equals(groupId)) {
      fail("The specified fallback groupId '" + fallbackGroupId + "' does not match the groupId determined '" + groupId + "'");
    } else if (checkEmpty(fallbackArtifactId) != null && !fallbackArtifactId.equals(artifactId)) {
      fail("The specified fallback artifactId '" + fallbackArtifactId + "' does not match the artifactId determined '" + artifactId + "'");
    }
    
    // do the main-replacements   
    var mapping = mapArtifact(groupId, artifactId);
    if (mainGroupIdNode == null) {
      mainGroupIdNode = doc.createElement(PomStructure.groupIdNode);
      rootNode.appendChild(mainGroupIdNode);
    }
    mainGroupIdNode.setTextContent(mapping[0]);
    if (mainGroupIdNode == null) {
      mainArtifactIdNode = doc.createElement(PomStructure.artifactIdNode);
      rootNode.appendChild(mainArtifactIdNode);
    } 
    mainArtifactIdNode.setTextContent(mapping[1]);
    
    // remove all the whitespaces to ensure nice indent
    removeWhiteSpacesInXml(rootNode);
    
    // write the new document
    var transformer = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		var source = new javax.xml.transform.dom.DOMSource(doc);

    // get the result
		var result = new javax.xml.transform.stream.StreamResult(new java.io.FileOutputStream(outfile == null ? pomfile : outfile));
		transformer.transform(source, result);
  }
}

var mapDependency = function(node) {
  var dependency = parseDependency(node);
  var nodeName = node.getNodeName();
  
  // get the mappings
  var mapping = mapArtifact(dependency.groupId, dependency.artifactId);
  
  // get the elements of the node
  var depNodes = node.getChildNodes();
  for (var h = 0; h < depNodes.getLength(); h++) {
    var depNode = depNodes.item(h);
    var depNodeName = depNode.getNodeName();
    
    if (PomStructure.groupIdNode.equalsIgnoreCase(depNodeName)) {
      depNode.setTextContent(mapping[0]);
    } else if (PomStructure.artifactIdNode.equalsIgnoreCase(depNodeName)) {
      depNode.setTextContent(mapping[1]);
    }
  }
}

var mapArtifact = function(group, artifact) {
  var mapperGroup      = "maven.mapped." + group;
  var mapperArtifact   = "maven.mapped." + group + "|" + artifact;

  // get the mapper
  var groupMapper    = project.getProperty(mapperGroup);
  var artifactMapper = project.getProperty(mapperArtifact);

  // get the results
  var mappedGroup    = group;
  var mappedArtifact = artifact;
  if (artifactMapper != null) {
    var splits = artifactMapper.split("\\|");
    mappedGroup    = splits[0];
    mappedArtifact = splits.length > 1 ? splits[1] : artifact;
  } else if (groupMapper != null) {
    mappedGroup = groupMapper;
  }
  
  // return as array
  var mapping = new Array();
  mapping[0]  = mappedGroup;
  mapping[1]  = mappedArtifact;
  return mapping;
}

var loadPom = function(pomId, pomFile, settingsfile) {
  var refIdPoms = "maven.mavenSetPom.mapOfPoms";
  var refIdCache = "maven.mavenSetPom.mavenCache";
  
  // get the map and create if not available
  var poms = project.getReference(refIdPoms);
  if (poms == null) {
    poms = new java.util.HashMap();
    project.addReference(refIdPoms, poms);
  } else if (poms instanceof java.util.HashMap == false) {
    fail("The collection of pom-projects could not be loaded, because it is of invalid type '" + poms.getClass().getName + "'");
  }
  
  // create the cache controller
  var cache = project.getReference(refIdCache);
  if (cache == null) {
    cache = new MavenCaches();
    project.addReference(refIdCache, cache);
  }
  
  // create the meta information of the new pomFile
  newPomMeta = new PomMeta(pomId, pomFile, settingsFile);
  
  // now let's check if we have this reference already and if we have to do something
  var curPomMeta = poms.get(pomId); 
  var changes = newPomMeta.determineChange(curPomMeta);
  
  if (changes < 0) {
    fail("Invalid comparison of poms '" + curPomMeta + "' and '" + newPomMeta + "'");
  } else if (changes > 0) {

    // a change of the settings indicates, that we may have a new repository
    // therefore we have to clear any caching which is quit difficult because of
    // the implementation of the DefaultMavenProjectBuilder
    if (changes < 3) {
      cache.setCachesForSettings(newPomMeta.settingsFile, curPomMeta == null ? null : curPomMeta.settingsFile);
    }
    
    // create the new pom
    var pomTask = project.createTask("antlib:org.apache.maven.artifact.ant:pom");
    pomTask.setId(pomId);
    pomTask.setInheritAllProperties(false);
    pomTask.setSettingsFile(newPomMeta.settingsFile);
    pomTask.setFile(newPomMeta.pomFile);
    pomTask.execute();
  
    // keep the new one
    poms.put(pomId, newPomMeta);
  }
  
  return project.getReference(pomId);
}