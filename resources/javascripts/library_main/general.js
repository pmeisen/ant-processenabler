// define a global variable used to surpress outputs
var global_echo_silent = false;

// function to do some logging
var echo = function(message, level) {
  // set a default level
  level = level == null ? "info" : level;

  if (global_echo_silent && !"error".equals(level))
    return;
  
  // create the task to write the message
  var echoTask = project.createTask("echo");
  var logLevel = new org.apache.tools.ant.taskdefs.Echo.EchoLevel();
  logLevel.setValue(level.toLowerCase());
  echoTask.setMessage(message);
  echoTask.setLevel(logLevel);
  echoTask.execute();
}

// function to fail
var fail = function(message) {
  // create the task to fail with the message
  var failTask = project.createTask("fail");
  failTask.setMessage(message);
  failTask.execute();
}

// function which returns null if the passed value is null or an empty string, otherwise the value
var checkEmpty = function(value) {
  return value == null || "".equals(value) ? null : value;
}