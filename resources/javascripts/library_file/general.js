var getSubPath = function(basedir, file, isdir, removePrefix) {
  // get the file to look for
  var baseFile = new java.io.File(basedir);
  var fileFile = new java.io.File(file);
  
  // check if we have a file and get the directory
  if (isdir != null && !isdir) {
    fileFile = fileFile.getParentFile();
  }
  
  // get the relative path
  var relativePath = baseFile.toURI().relativize(fileFile.toURI()).getPath();
  
  // remove any separaters
  if (relativePath.endsWith("\\") || relativePath.endsWith("/")) {
    relativePath = relativePath.substring(0, relativePath.length() - 1);
  }
  
  // remove any prefix
  if (removePrefix != null && !"".equals(removePrefix)) {
    removePrefix = removePrefix.startsWith("\\") || removePrefix.startsWith("/") ? removePrefix.substring(1) : removePrefix;          
    var prefixFile = new java.io.File(removePrefix);
    var relativeFile = new java.io.File(relativePath);
    relativePath = prefixFile.toURI().relativize(relativeFile.toURI()).getPath();
  }
  
  // empty means the current, i.e. .
  if (relativePath.equals("")) {
    relativePath = ".";
  }
  
  return relativePath;
}