@echo off
REM Launcher for the portable Trading Feed Reader.
REM Double-click this file (not TradingFeedReader.exe directly) so the console window
REM stays open after the tool exits — otherwise Windows closes it immediately and you
REM cannot see the final summary or any error messages.
REM
REM Any arguments passed to this .bat are forwarded to the .exe, so you can also run
REM Start.bat --feedDomain=... --tradingFeedId=... --duration=2m from a terminal.

"%~dp0TradingFeedReader.exe" %*
set EXIT_CODE=%ERRORLEVEL%

echo.
echo ---------------------------------------------------------------
echo  Session ended with exit code %EXIT_CODE%.
echo  Press any key to close this window.
echo ---------------------------------------------------------------
pause >nul

exit /b %EXIT_CODE%
