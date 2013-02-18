// datastructore to hold a library
var Library = function(name, file, comment, elements) {
  this.elements = new Array();
  this.setName(name);
  this.setFile(file);
  this.addElements(elements);
  this.setComment(comment);
}
Library.prototype.setName = function(name) {
  this.name = checkEmpty(name);
}
Library.prototype.setFile = function(file) {
  this.file = checkEmpty(file);
}
Library.prototype.setComment = function(comment) {
  this.comment = comment == null || !(comment.constructor === Comment) ? null : comment;
}
Library.prototype.addElements = function(elements) {

  // the elements can be an array or an Element instance
  if (elements == null) {
    // skip
  } else if (elements.constructor === Array) {
      for (var i = 0; i < elements.length; i++) {
        this.addElement(elements[i]);
      }
  } else if (elements instanceof java.lang.Collection) {
      var iterator = elements.iterator();
      while(iterator.hasNext()) {
        this.addElement(iterator.next());
      }
  } else {
    this.addElement(elements);
  }
};
Library.prototype.addElement = function(element) {
          
  // the element should not be null or of unknown type
  if (element == null) {
    // skip
  } else if (element.constructor === Element) {
    this.elements.push(element);
  } else {
    // skip
  }
};

// datastructure to store an element of the library
var Element = function(type, name, comment, content) {
  this.setType(type);
  this.setName(name);
  this.setComment(comment);
  this.setContent(content);
}
Element.prototype.setType = function(type) {
  this.type = type;
}
Element.prototype.setName = function(name) {
  this.name = checkEmpty(name);
}
Element.prototype.setComment = function(comment) {
  this.comment = comment == null || !(comment.constructor === Comment) ? null : comment;
}
Element.prototype.setContent = function(content) {
  this.content = checkEmpty(content);
}

// datastructure to store a comment of the documentation
var Comment = function(description, params, fails, author, since) {
  this.params = new Array();
  this.addParams(params);
  this.setDescription(description);
  this.setFails(fails);
  this.setAuthor(author);
  this.setSince(since);
}
Comment.prototype.addParams = function(params) {

  // the parameters can be an array or a value
  if (params == null) {
    // skip
  } else if (params.constructor === Array) {
      for (var i = 0; i < params.length; i++) {
        addParam(value);
      }
  } else if (params instanceof java.lang.Collection) {
      var iterator = params.iterator();
      while(iterator.hasNext()) {
        addParam(value);
      }
  } else {
    addParam(params);
  }
};
Comment.prototype.addParam = function(param) {
          
  // the parameters can be a Parameter instance or a String, later will be parsed
  if (param == null) {
    // skip
  } else if (param.constructor === Parameter) {
    this.params.push(param);
  } else if (param instanceof String) {
    param = parseParameter(param);           
    if (param != null) this.params.push(param);
  } else {
    // skip
  }
};
Comment.prototype.hasParam = function(param) {
  return this.getParam(param) != null;
};
Comment.prototype.getParam = function(param) {
  var name = param.constructor === Parameter ? param.name : param;

  if (name == null) {
    return null;
  } else {
    for (var i = 0; i < this.params.length; i++) {
      if (name.equals(this.params[i].name)) {
        return this.params[i];
      }
    }
    
    return null;
  }
};
Comment.prototype.setDescription = function(description) {
  this.description = checkEmpty(description);
}
Comment.prototype.setFails = function(fails) {
  this.fails = checkEmpty(fails);
}
Comment.prototype.setAuthor = function(author) {
  this.author = checkEmpty(author);
}
Comment.prototype.setSince = function(since) {
  this.since = checkEmpty(since);
}

// datastructure to store a parameter used in a comment
var Parameter = function(name, description, def) {
  this.setName(name);
  this.setDescription(description);
  this.setDefault(def);
}
Parameter.prototype.setName = function(name) {
  this.name = checkEmpty(name);
}
Parameter.prototype.setDescription = function(description) {
  this.description = checkEmpty(description);
}
Parameter.prototype.setDefault = function(def) {
  this.def = def;
}

// datastructure to provide time information for the documentation
var DocumentationDate = function() {
  var formatter = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
  this.now = formatter.format(new java.util.Date());
}

// datastructure to store the elements of the index
var Index = function(entries) {
  this.entries = new Array();
  this.addEntries(entries);
}
Index.prototype.addEntries = function(entries) {

  // the entries can be an array or an IndexEntry instance
  if (entries == null) {
    // skip
  } else if (entries.constructor === Array) {
      for (var i = 0; i < entries.length; i++) {
        this.addEntry(entries[i]);
      }
  } else if (entries instanceof java.lang.Collection) {
      var iterator = entries.iterator();
      while(iterator.hasNext()) {
        this.addEntry(iterator.next());
      }
  } else {
    this.addEntry(entries);
  }
};
Index.prototype.addEntry = function(entry) {
          
  // the element should not be null or of unknown type
  if (entry == null) {
    // skip
  } else if (entry.constructor === IndexEntry) {
    this.entries.push(entry);
  } else {
    // skip
  }
};

// datastructure to store an entry of the index
var IndexEntry = function(name, basedir, file) {
  this.setName(name);
  this.setFile(file, basedir);
}
IndexEntry.prototype.setName = function(name) {
  this.name = checkEmpty(name);
}
IndexEntry.prototype.setFile = function(file, basedir) {

  // get the file to be indexed
  var file = file instanceof java.io.File ? file : new java.io.File(file);
  var subPath = getSubPath(basedir, file, true);

  this.fullPath = file.getCanonicalPath();
  this.path = subPath;
  this.relUrl = "" + (new java.lang.String(subPath)).replace("\\", "/");
}