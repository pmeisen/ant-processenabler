@echo off
if exist %~dp0antRunAsync.js goto RUN_IT
echo ERROR: %~dp0antRunAsync.js was not found!
goto END
:RUN_IT
cscript //NoLogo %~dp0antRunAsync.js %1 %2 %3 %4 %5 %6 %7 %8 %9
:END
