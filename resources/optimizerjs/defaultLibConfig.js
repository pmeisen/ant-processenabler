({
  
  // directory to find the sources (i.e. app at)
  appDir: '${javascript.tmp.optimizerSourcesDir}',
  
  // the scripts directory, i.e. the base directory of requireJs
  baseUrl: '${javascript.server.scripts.dir}',
  
  // define the optimization to be used
  optimize: '${javascript.tmp.optimizer.type}',
  
  // disable or enable wrapping
  wrap: ${javascript.tmp.optimizer.wrap},
  
  // the directory to store the results at
  dir: '${javascript.tmp.optimizerResultDir}'
})