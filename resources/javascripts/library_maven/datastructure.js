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
}
MavenCaches.prototype.setCurrentBuilder = function() {
  var container = project.getReference("org.codehaus.plexus.PlexusContainer");
  var projectBuilder = container == null ? null : container.lookup(org.apache.maven.project.MavenProjectBuilder.ROLE);
  
  if (projectBuilder != null && projectBuilder instanceof org.apache.maven.project.DefaultMavenProjectBuilder) {
    this.projectBuilder = projectBuilder;
  } else {
    this.projectBuilder = null;
  }
}
MavenCaches.prototype.hasBuilder = function() {
  
  // update if we don't have one yet
  if (this.projectBuilder == null) {
  
    // get the current builder if there is one
    this.setCurrentBuilder();
  }

  // still no builder, there is none so far
  return this.projectBuilder != null;
}
MavenCaches.prototype.resetCaches = function() {
  if (!this.hasBuilder()) {
    return;
  }

  var fields = this.projectBuilder.getClass().getDeclaredFields();
  for (var i = 0; i < fields.length; i++) {
    var field = fields[i];
    var fieldName = field.getName();
    
    // empty the caches
    field.setAccessible(true);
    if (fieldName.equals("rawProjectCache")) {
      field.get(this.projectBuilder).clear();
    } else if (fieldName.equals("processedProjectCache")) {
      field.get(this.projectBuilder).clear();
    }
    field.setAccessible(false);
  }
}
MavenCaches.prototype.setCachesForSettings = function(settingsFile, oldSettingsFile) {
  
  // we need a builder
  if (!this.hasBuilder()) {
    return;
  }

  // check the passed files
  var fileNew = checkFile(settingsFile, true);
  var fileOld = checkFile(oldSettingsFile, true);
  echo(settingsFile);
  echo(oldSettingsFile);
  // the unique settingsFile id
  var uniqueId = fileNew.getCanonicalPath();
  var oldUniqueId = fileOld == null ? null : fileOld.getCanonicalPath();
  
  // check if we have the same file
  var reset = false;
  if (uniqueId.equals(oldUniqueId)) {
    if (fileNew.lastModified() == fileOld.lastModified()) {
      // the file is the same we have nothing to do, 
      // because the cache is already set
      echo("No modification " + fileNew + " vs. " + fileOld, "debug");
      return;
    } else {
      // the file was modified in it's content, we cannot
      // keep the cache, we just reset it and done
      reset = true;
    }
  }
  
  // get the caches
  var rawCache = this.rawCaches.get(uniqueId);
  var processedCache = this.processedCaches.get(uniqueId);
  
  // reset the chaches for the new settings
  if (reset || (rawCache == null && processedCache == null)) {
    rawCache = new java.util.HashMap();
    processedCache = new java.util.HashMap();
  }
  
  // now apply the found caches to the current builder
  var fields = this.projectBuilder.getClass().getDeclaredFields();
  
  echo("----------" + fileNew + "------------", "error");
  for (var i = 0; i < fields.length; i++) {
    var field = fields[i];
    var fieldName = field.getName();
    
    // empty the caches
    field.setAccessible(true);
    if (fieldName.equals("rawProjectCache")) {
      if (oldUniqueId != null) {
        this.rawCaches.put(oldUniqueId, field.get(this.projectBuilder));
      }
      field.set(this.projectBuilder, rawCache);
      echo("----------- RAW -----------", "error");
      echo("Using for raws " + uniqueId + ": " + rawCache, "error");
      //field.get(this.projectBuilder).clear();
    } else if (fieldName.equals("processedProjectCache")) {
      if (oldUniqueId != null) {
        this.processedCaches.put(oldUniqueId, field.get(this.projectBuilder));
      }
      field.set(this.projectBuilder, processedCache);
      echo("----------- PROCESS -----------", "error");
      echo("Using for processed " + uniqueId + ": " + processedCache, "error");
      //field.get(this.projectBuilder).clear();
    }
    field.setAccessible(false);
  }
  
  echo("----------------------", "error");
}
MavenCaches.prototype.toString = function() {
  var output = "Raw {" + this.rawCaches.toString() + "}";
  output += ", Processed {" + this.processedCaches.toString() + "}";
  
  return output;
}