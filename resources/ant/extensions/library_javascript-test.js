(function(parent) {
  
  // the map of dependencies
  var tests = {
    depScriptList: [${javascript.tmp.depList}],
    testScriptList: [${javascript.tmp.testList}],
    srcScriptList: [${javascript.tmp.srcList}],
    
    requireMatcher: /^.*\/require\.js$/,
    
    getRequireScript: function() {
      for (var i = 0, length = this.depScriptList.length; i < length; i++) {
        var depScript = this.depScriptList[i];
        if (this.requireMatcher.test(depScript)) {
          return depScript;
        }
      }
      
      // return undefined
      return;
    },
    
    addScript: function(url, callback) { 

      // make sure the script isn't loaded yet
      var scripts = document.getElementsByTagName('script');
      for (var i = 0, length = scripts.length; i < length; i++) {
        if (scripts[i].src == url) {
          callback && typeof callback == 'function' ? callback() : null;
          return;
        }
      }
    
      var js = document.createElement('script');
      js.setAttribute('type', 'text/javascript');
      js.setAttribute('src', url);
      document.getElementsByTagName('head')[0].appendChild(js);
      
      js.onreadystatechange = function () {
        if (js.readyState == 'complete') {
          callback && typeof callback == 'function' ? callback() : null;
        }
      }
   
      js.onload = function () {
        callback && typeof callback == 'function' ? callback() : null;
      }
    },
        
    addScripts: function(urls, callback) {
      if (Object.prototype.toString.call(urls) === '[object Array]') {
      
        // define some global variables
        var instance = this;

        (function() {
          var urlNr = 0;
        
          var innerCallback = function() {
            var url = urls[urlNr];
            
            // load the other urls or give the callback
            if (urlNr < urls.length) {
              urlNr++;
              instance.addScript(url, innerCallback);
            } else {
              callback && typeof callback == 'function' ? callback() : null;
            }
          }
          
          return innerCallback;
        }())();
      } else if (typeof urls === 'string') {
        this.addScript(urls, callback);
      }
    }
  };
  
  parent.${javascript.test.var} = tests;
})(this);