({
  
  // directory to find the sources (i.e. app at)
  appDir: '${javascript.tmp.optimizerSourcesDir}',
  
  // the scripts directory, i.e. the base directory of requireJs
  baseUrl: '${javascript.server.scripts.dir}',
  
  // the directory to store the results at
  dir: '${javascript.tmp.optimizerResultDir}',
  
  // remove the once that are combined within another
  removeCombined: true,
    
  // the modules to be optimized
  modules: [{
    name: '${javascript.tmp.appPath}'
  }]
})