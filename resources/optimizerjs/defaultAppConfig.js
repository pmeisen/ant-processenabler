({
  
  // directory to find the sources (i.e. app at)
  appDir: '${javascript.tmp.optimizerSourcesDir}',
  
  // the directory to store the results at
  dir: '${javascript.tmp.optimizerResultDir}',
  
  // the main configuration of the application should be considered
  mainConfigFile: '${javascript.tmp.optimizerSourcesDir}/${javascript.server.scripts.dir}/${javascript.tmp.appPath}.js',
  
  // remove the once that are combined within another
  removeCombined: true,
  
  // define the optimization to be used
  optimize: '${javascript.optimizer.type}',
    
  // the modules to be optimized
  modules: [{
    name: '${javascript.tmp.appPath}'
  }]
})