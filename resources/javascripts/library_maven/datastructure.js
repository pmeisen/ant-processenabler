var PomStructure = function() {}
PomStructure.parentNode       = "parent";
PomStructure.dependencyMgmNode= "dependencyManagement";
PomStructure.dependenciesNode = "dependencies";
PomStructure.dependencyNode   = "dependency";
PomStructure.groupIdNode      = "groupId";
PomStructure.artifactIdNode   = "artifactId";
PomStructure.versionNode      = "version";
  
// datastructore to hold a dependency
var Dependency = function(groupId, artifactId, version) {
  this.setGroupId(groupId);
  this.setArtifactId(artifactId);
  this.setVersion(version);
}
Dependency.prototype.setGroupId = function(groupId) {
  this.groupId = checkEmpty(groupId);
  if (this.groupId != null) {
    this.groupId = this.groupId.trim();
  }
}
Dependency.prototype.setArtifactId = function(artifactId) {
  this.artifactId = checkEmpty(artifactId);
  if (this.artifactId != null) {
    this.artifactId = this.artifactId.trim();
  }
}
Dependency.prototype.setVersion = function(version) {
  this.version = checkEmpty(version);
  if (this.version != null) {
    this.version = this.version.trim();
  }
}
Dependency.prototype.toString = function() {
  return this.groupId + ":" + this.artifactId + ":" + this.version;
}

// datastructore to hold meta information for a pom
var PomMeta = function(pomId, pomFile, settingsFile) {
  this.pomId = checkEmpty(pomId);
  
  if (this.pomId == null) {
    fail("The pomId cannot be null or empty");
  }
  
  this.setPomFile(pomFile);
  this.setSettingsFile(settingsFile);
}
PomMeta.prototype.setPomFile = function(pomFile) {
  this.pomFile = checkFile(pomFile, true);
  if (this.pomFile == null) {
    fail("The pomFile '" + pomFile + "' is of invalid type.");
  }

  this.lastModifiedPomFile = this.pomFile.lastModified();
}
PomMeta.prototype.setSettingsFile = function(settingsFile) {
  this.settingsFile = checkFile(settingsFile, true);
  if (this.settingsFile == null) {
    fail("The settingsFile '" + settingsFile + "' is of invalid type.");
  }
  
  // set the meta information of the file
  this.lastModifiedSettingsFile = this.settingsFile.lastModified();
}
/* 
  Returns the determine change:
  -1 -> the pomIds do not match, comparison invalid
   3 -> changed, the pom definition has changed
   2 -> changed, the settings have changed
   1 -> changed, the passed value was null
   0 -> no change, the passed meta information will create the same pom
 */
PomMeta.prototype.determineChange = function(pomMeta) {
  if (pomMeta == null) {
    return 1;
  } else if (!pomMeta.pomId.equals(this.pomId)) {
    return -1;
  } else if (!pomMeta.settingsFile.equals(this.settingsFile) || pomMeta.lastModifiedSettingsFile != this.lastModifiedSettingsFile) {
    return 2;
  } else if (!pomMeta.pomFile.equals(this.pomFile) || pomMeta.lastModifiedPomFile != this.lastModifiedPomFile) {
    return 3;
  } else {
    return 0;
  }
}
PomMeta.prototype.toString = function() {
  return this.pomId + " (" + this.pomFile.getCanonicalPath() + ", " + this.settingsFile.getCanonicalPath() + ")";
}

// datastructure to control the maven caches
var MavenCaches = function () {
  
  // initialize the cache-holders
  this.rawCaches = new java.util.HashMap();
  this.processedCaches = new java.util.HashMap();
  
  this.disableCaching();
}
MavenCaches.prototype.hasBuilder = function() {
  
  // update if we don't have one yet
  if (this.projectBuilder == null) {
    var container = project.getReference("org.codehaus.plexus.PlexusContainer");
    var projectBuilder = container == null ? null : container.lookup(org.apache.maven.project.MavenProjectBuilder.ROLE);
    
    if (projectBuilder != null && projectBuilder instanceof org.apache.maven.project.DefaultMavenProjectBuilder) {
      this.projectBuilder = projectBuilder;
    } else {
      this.projectBuilder = null;
    }
  }

  // still no builder, there is none so far
  return this.projectBuilder != null;
}
MavenCaches.prototype.disableCaching = function() {
  var hadBuilder = this.projectBuilder != null;
  
  // we need a builder
  if (!hadBuilder && !this.hasBuilder()) {
    return;
  } else if (hadBuilder) {
    // the builder was already configured
  } else {
    
    // set the new cache, i.e. the one that doesn't cache
    var fields = this.projectBuilder.getClass().getDeclaredFields();

    for (var i = 0; i < fields.length; i++) {
      var field = fields[i];
      var fieldName = field.getName();
      
      // empty the caches
      field.setAccessible(true);
      if (fieldName.equals("rawProjectCache")) {
        field.set(this.projectBuilder, new java.util.Map(emptyMapCallBack));
      } else if (fieldName.equals("processedProjectCache")) {
        field.set(this.projectBuilder, new java.util.Map(emptyMapCallBack));
      }
      field.setAccessible(false);
    }
  }
}

var emptyMapCallBack = {
  put: function(key, value) {
  },
  get: function(key) {
    return null;
  },
  clear: function() {
  },
  containsKey: function(key) {
    return false;
  },
  containsValue: function(value) {
    return false;
  },
  entrySet: function() {
    return new java.util.Set();
  },
  isEmpty: function() {
    return true;
  },
  keySet: function() {
    return new java.util.Set();
  },
  putAll: function(map) {
  },
  remove: function(key) {
  },
  size: function() {
    return 0;
  },
  values: function() {
    return new java.util.ArrayList();
  }
};