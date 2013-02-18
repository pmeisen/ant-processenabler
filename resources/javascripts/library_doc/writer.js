// function to write a library
var writeLibrary = function(library, basedir, outputSettings) {
  if (outputSettings == null) {
    fail("The outputSettings must be defined");
  }
  
  // the file to be created
  var libFile = getFilename(library, basedir, outputSettings);
  
  // write the file
  if (outputSettings.constructor === HtmlOutputSettings) {
    var objects = new java.util.HashMap();
    objects.put("library", library);
  
    writeHtml(libFile, outputSettings.libraryTemplate, objects, outputSettings);
  } else {
    fail("The used outputSettings are not supported");
  }
}

// function to write an index
var writeIndex = function(libraries, basedir, outputSettings) {
  if (outputSettings == null) {
    fail("The outputSettings must be defined");
  }

  // create the index
  var index = new Index();
  for (var i = 0; i < libraries.length; i++) {
    var libFile = getFilename(libraries[i], basedir, outputSettings);
    var entry = new IndexEntry(libraries[i].name, basedir, libFile);
    
    index.addEntry(entry);
  }

  // write the file
  if (outputSettings.constructor === HtmlOutputSettings) {
    var objects = new java.util.HashMap();
    objects.put("index", index);
    
    var indexFile = java.io.File(basedir, "index." + outputSettings.extension);
    writeHtml(indexFile, outputSettings.indexTemplate, objects, outputSettings);
  } else {
    fail("The used outputSettings are not supported");
  }
}

// function to retrieve the filename used for a library
var getFilename = function(library, basedir, outputSettings) {
  var name = library == null ? null : library.name;
  if (name == null) fail("Cannot create a library file for an unnamed library");
  
  // the file to be created
  return java.io.File(basedir, name + "." + outputSettings.extension);
}

// function write the library to an html file
var writeHtml = function(file, template, objects, outputSettings) {
  
  // check the outputSettings
  if (outputSettings == null || !(outputSettings.constructor === HtmlOutputSettings)) {
    fail("Invalid outputSettings specified for html output.");
  }

  // define some matchers for parsing
  var propertyRegEx = "\\$\\{[a-zA-Z0-9\\.]+\\}";
  var propertyPattern = java.util.regex.Pattern.compile(propertyRegEx);
  var loopInitRegEx = "@\\(([a-zA-Z0-9]+)\\.([a-zA-Z0-9\\.]+)(?:\\[([a-zA-Z0-9]*)\\=([a-zA-Z0-9]+)\\])?\\s*=>\\s*([a-zA-Z0-9]+)\\)";
  var loopInitPattern = java.util.regex.Pattern.compile(loopInitRegEx);
  var loopStartRegEx = "\\{";
  var loopStartPattern = java.util.regex.Pattern.compile(loopStartRegEx);
  var loopEndRegEx = "\\}";
  var loopEndPattern = java.util.regex.Pattern.compile(loopEndRegEx);
  
  // read the template line by line and decide what to do
  var input = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(template), "UTF-8"));
  var output = new java.io.BufferedWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), "UTF-8"));
  var line = null;
  var lineNr = 0;
  
  // create a stack which contains the loops
  var rootLoop      = new HtmlLoopData();
  var nextLoop      = null;
  var currentLoop   = rootLoop;
  var skippedMarker = 0;
  
  // start parsing
  while ((line = input.readLine()) != null) {
    var parseLine = new java.lang.String(line);
    lineNr++;
    
    while (!"".equals(parseLine)) {
    
      // check the line for matches
      var propertyMatcher = propertyPattern.matcher(parseLine);
      var loopInitMatcher = loopInitPattern.matcher(parseLine);
      var loopStartMatcher = loopStartPattern.matcher(parseLine);
      var loopEndMatcher = loopEndPattern.matcher(parseLine);
      
      // check each matcher
      var availableMatcher = java.util.HashMap();
      if (propertyMatcher.find()) {
        availableMatcher.put("PROPERTY", propertyMatcher);
      }
      if (loopInitMatcher.find()) {
        availableMatcher.put("LOOPINIT", loopInitMatcher);
      }
      if (loopStartMatcher.find()) {      
        availableMatcher.put("LOOPSTART", loopStartMatcher);
      }
      if (loopEndMatcher.find()) {
        availableMatcher.put("LOOPEND", loopEndMatcher);
      }
      
      // get the next matcher
      var minPosition = -1;
      var nextMatcherType = null;
      var nextMatcher = null;
      var it = availableMatcher.entrySet().iterator();
      while (it.hasNext()) {
        var entry = it.next();
        var matcher = entry.getValue();
        
        // get the minimumPosition
        if (minPosition < 0 || minPosition > matcher.start()) {
          nextMatcherType = entry.getKey();
          nextMatcher     = matcher;
          minPosition     = matcher.start();
        }
      }
            
      // depending on the next value
      if ("PROPERTY".equals(nextMatcherType)) {
        currentLoop.append(parseLine.substring(0, nextMatcher.end()));
        parseLine   = parseLine.substring(nextMatcher.end());
      } else if ("LOOPINIT".equals(nextMatcherType)) {
        if (nextLoop != null) {
          fail("Invalid syntax for loop, please start a loop prior to initializing a new one!\r\nDetected in line " + lineNr);
        }
        
        // create the new loop
        nextLoop = new HtmlLoopData(currentLoop, nextMatcher.group(1), nextMatcher.group(2), nextMatcher.group(3), nextMatcher.group(4), nextMatcher.group(5));
        
        currentLoop.append(parseLine.substring(0, nextMatcher.start()));
        parseLine   = parseLine.substring(nextMatcher.end());
      } else if ("LOOPSTART".equals(nextMatcherType)) {
        var pre = new java.lang.String(parseLine.substring(0, nextMatcher.start()));
        
        // do some validation
        if (nextLoop == null) {

          // do nothing and ignore the sign
          currentLoop.append(parseLine.substring(0, nextMatcher.start()));
          currentLoop.append(nextMatcher.group(0));
          parseLine   = parseLine.substring(nextMatcher.end());
          
          // increase the skippedMarker
          skippedMarker++;
        } else if (!pre.matches("\\s*")) {
          fail("Invalid syntax for loop between initialization - @(...) - and opening - { - cannot be any non-whitespaces!\r\nDetected '" + pre + "' in line " + lineNr);
        } else {
        
          // add the pre-stuff of the loop
          currentLoop.append(pre);
          
          // add the new loop and make it the current one
          currentLoop = currentLoop.append(nextLoop);
          nextLoop    = null;
          
          // the rest has to be handled
          parseLine   = parseLine.substring(nextMatcher.end());
        }
      } else if ("LOOPEND".equals(nextMatcherType)) {
        
        if (skippedMarker == 0) {
          currentLoop.append(parseLine.substring(0, nextMatcher.start()));
       
          // get back to the parent loop
          currentLoop = currentLoop.parent;

          // the rest has to be done
          parseLine   = parseLine.substring(nextMatcher.end());
        } else {
          // do nothing and ignore the sign
          currentLoop.append(parseLine.substring(0, nextMatcher.start()));
          currentLoop.append(nextMatcher.group(0));
          parseLine   = parseLine.substring(nextMatcher.end());
        
          skippedMarker--;
        }
      } else {
        // add everything
        currentLoop.append(parseLine);
        parseLine   = "";
      }
    }
        
    // write the content
    currentLoop.append("\r\n");
  }
  
  // work through the loops detected and write the output
  rootLoop.finalize(output, objects, outputSettings);

  // flush and write the document
  output.flush();
  output.close();
  
  // close the libraryTemplateFile
  input.close();
}

var replaceProperties = function(prefix, object, input, nullReplacement) {
  var newLine = input;

  // loop through the objects properties
  if (object.constructor === Array) {
    for (var i = 0; i < object.length; i++) {
    echo("DA");
      newLine = replaceProperties(prefix, object[i], newLine, nullReplacement);
    }
  } else if (typeof object == "string" || object instanceof java.lang.String) {
      newLine = newLine.replace("${" + prefix + "}", object);
  } else {
    for (property in object) {
      var propertyValue = object[property];

      if (propertyValue == null) {
        newLine = newLine.replace("${" + prefix + "." + property + "}", nullReplacement == null ? "" : nullReplacement);
      } else if (typeof propertyValue == "function") {
        // skip it
      } else if (propertyValue.constructor === Array) {      
                  
        for (var key in propertyValue) {
          // exclude some default properties of arrays
          if (key === 'length' || !propertyValue.hasOwnProperty(key)) continue;
          
          // get the value of the array and make sure we have a string
          var value = propertyValue[key];
          if (typeof value != "string" && value instanceof java.lang.String == false) continue;

          // replace
          newLine = newLine.replace("${" + prefix + "." + property + "." + key + "}", value);
        }
      } else if (typeof propertyValue == "string" || propertyValue instanceof java.lang.String) {
        newLine = newLine.replace("${" + prefix + "." + property + "}", propertyValue);
      } else if (propertyValue instanceof java.lang.Object) {
        // an Object of the Java world is nothing from the internal datastructure, therefore we just parse it to a String
        // so that the loop comes to an end
        var value = propertyValue == null ? null : propertyValue.toString();
        newLine = replaceProperties(prefix + "." + property, value, newLine);
      } else {  
        newLine = replaceProperties(prefix + "." + property, propertyValue, newLine);
      }
    }
  }
  
  return newLine;
}

var replaceObjectsProperties = function(objects, input, nullReplacement) {
  var newLine = input;

  // loop through the objects properties
  var it = objects.entrySet().iterator();
  while (it.hasNext()) {
    var entry   = it.next();
    var prefix = entry.getKey();
    var object = entry.getValue(); 

    newLine = replaceProperties(prefix, object, newLine, nullReplacement);
  }
  
  return newLine;
}

// HtmlOutputSettings used for html output
var HtmlOutputSettings = function(libraryTemplate, indexTemplate, propertiesTemplate) {
  this.extension          = "html";
  this.nullReplacement    = "<span style=\"font-style: italic\">none</span>";
  this.css                = "<style type=\"text/css\"></style>";
  this.inlineCss          = true;
  this.cssFile            = null;
  this.cssClass           = new Array();
  this.libraryTemplate    = null;
  this.indexTemplate      = null;
  this.propertiesTemplate = null;
    
  // read the files
  this.setLibraryTemplate(libraryTemplate);
  this.setIndexTemplate(indexTemplate);
  this.setPropertiesTemplate(propertiesTemplate);
}
HtmlOutputSettings.prototype.setLibraryTemplate = function(libraryTemplate) {
  this.libraryTemplate = this.checkFile(libraryTemplate);
}
HtmlOutputSettings.prototype.setIndexTemplate = function(indexTemplate) {
  this.indexTemplate = this.checkFile(indexTemplate);
}
HtmlOutputSettings.prototype.setPropertiesTemplate = function(propertiesTemplate) {
  this.propertiesTemplate = this.checkFile(propertiesTemplate, false);
  this.readPropertiesTemplate();
  this.updateSettings();
}
HtmlOutputSettings.prototype.addCssClass = function(cssClassIdentifier, cssClass) {
  this.cssClass["" + cssClassIdentifier] = "" + cssClass;
}
HtmlOutputSettings.prototype.updateSettings = function(cssFile, inlineCss, nullReplacement) {
  if (cssFile != null) this.cssFile = cssFile;
  if (inlineCss != null) this.inlineCss = inlineCss;
  if (nullReplacement != null) this.nullReplacement = nullReplacement;
  
  // check if it has to be inline
  if ((this.inlineCss || "true".equals(this.inlineCss)) && this.cssFile != null) {
    
    // get the cssFile
    var cssFile = new java.io.File(this.libraryTemplate.getParentFile(), this.cssFile);
    cssFile = this.checkFile(cssFile, true);
    
    // read the file
    var input = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(cssFile)));
    var line = null;
    var cssFileContent = "";
    while ((line = input.readLine()) != null) {
      cssFileContent += line + "\r\n";
    }
    input.close();
    
    // set the css
    this.css = "<style type=\"text/css\">\r\n";
    this.css += cssFileContent;
    this.css += "</style>";
  } else if (!this.inlineCss && this.cssFile != null) {
    this.css = "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + this.cssFile + "\">";
  } else {
    this.css = "<style type=\"text/css\"></style>";
  }
}
HtmlOutputSettings.prototype.readPropertiesTemplate = function(file, mandatory) {
  // reset everything and check the file
  this.cssClass  = new Array();
  if (this.propertiesTemplate == null) return;
  
  // load the properties from the file
  var properties = new java.util.Properties();
  properties.load(new java.io.FileInputStream(this.propertiesTemplate));
  
  // read the properties
  var e = properties.propertyNames();
  while (e.hasMoreElements()) {
    var key = new java.lang.String(e.nextElement());
    var value = "" + properties.getProperty(key);
    
    if (key.startsWith("settings.")) {
      key = "" + key.replaceFirst("^settings.", "");
      this[key] = value;
    } else if (!key.contains(".") && !key.contains(" ")) {
      // we have a class
      this.addCssClass(key, value);
    } else {
      // some other property which we skip
      echo("The property '" + key + "' is specified as settings property, but cannot be interpreted and therefore will be skipped", "warn");
    }
  }
}
HtmlOutputSettings.prototype.checkFile = function(file, mandatory) {
  var mandatory = mandatory == null ? true : mandatory;

  // check the file
  if (file == null && mandatory) {
    fail("Invalid outputSettings a mandatory file is not set.");
  }
  var file = file instanceof java.io.File ? file : new java.io.File(file);
  
  // check the file
  if (!file.isFile() || !file.exists() || !file.canRead()) {
  
    if (mandatory) {
      fail("The file '" + file + "' cannot be found or accessed.");
    } else {
      return null;
    }
  }
  
  return file;
}

var HtmlLoopData = function(parent, objectName, property, selectorProperty, selectorValue, loopObjectName) {
  this.parent           = parent;
  this.objectName       = objectName;
  this.property         = property;
  this.selectorProperty = checkEmpty(selectorProperty);
  this.selectorValue    = selectorValue;
  this.loopObjectName   = loopObjectName;
  
  this.content          = new Array();
}
HtmlLoopData.prototype.append = function(content) {
  if (content == null) {
    fail("The content of a loop cannot be null!");
  } else if (typeof content == "string" || content instanceof java.lang.String) {
    this.content.push(content);
    return this;
  } else if (content.constructor === HtmlLoopData) {
    this.content.push(content);
    return content;
  } else {
    fail("The content of a loop must be a string or another loop!");
  }
}
HtmlLoopData.prototype.finalize = function(output, objects, settings) {

  // bind the settings as object
  if (!objects.containsKey("settings")) {
    objects.put("settings", settings);
  }
  if (!objects.containsKey("date")) {
    objects.put("date", new DocumentationDate());
  }

  // iterate through the content of the loop
  for (var i = 0; i < this.content.length; i++) {
    var content = this.content[i];

    if (typeof content == "string" || content instanceof java.lang.String) {
      output.append(replaceObjectsProperties(objects, content, settings.nullReplacement));
    } else if (content.constructor === HtmlLoopData) {
    
      // get the element we are interested in
      var element = objects.get(content.objectName);
      if (element == null) continue;
      
      // the property can be a stepwise property
      var splitProperty   = content.property.split("\\.");
      var selectorElement = element;
      for (var j = 0; j < splitProperty.length; j++) {
        selectorElement = selectorElement[splitProperty[j]];
      }
      if (selectorElement == null) continue;
      
      // it should be an array otherwise we just do it once
      if (selectorElement.constructor === Array) {        
        for (var k = 0; k < selectorElement.length; k++) {
          var elementObject     = selectorElement[k];
          var selectorProperty  = content.selectorProperty;

          // check the selector and run it
          if (selectorProperty == null || content.selectorValue.equals(elementObject[selectorProperty])) {       
            objects.put(content.loopObjectName, elementObject);
            content.finalize(output, objects, settings);
            objects.remove(content.loopObjectName);
          }
        }
      }
    }
  }
}