!define VERSION "0.2.0"

!include "MUI.nsh"
!include "Library.nsh"

Name "DreamChess ${VERSION}"
OutFile "dreamchess-${VERSION}-win32.exe"
InstallDir "$PROGRAMFILES\DreamChess"

!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\orange-install.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\orange-uninstall.ico"
!define MUI_FINISHPAGE_NOAUTOCLOSE
!define MUI_UNFINISHPAGE_NOAUTOCLOSE
!define MUI_WELCOMEFINISHPAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Wizard\orange.bmp" 
!define MUI_UNWELCOMEFINISHPAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Wizard\orange-uninstall.bmp" 
!define MUI_ABORTWARNING
!define MUI_UNABORTWARNING

Var MUI_TEMP
Var STARTMENU_FOLDER
Var ALREADY_INSTALLED

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "../../COPYING"

!insertmacro MUI_PAGE_DIRECTORY

!define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKCU"
!define MUI_STARTMENUPAGE_REGISTRY_KEY "Software\DreamChess"
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "StartMenuFolder"
!define MUI_STARTMENUPAGE_DEFAULTFOLDER "DreamChess"

!insertmacro MUI_PAGE_STARTMENU Application $STARTMENU_FOLDER
!insertmacro MUI_PAGE_INSTFILES

!define MUI_FINISHPAGE_RUN
!define MUI_FINISHPAGE_RUN_TEXT "Create Desktop Icon"
!define MUI_FINISHPAGE_RUN_FUNCTION "DesktopShortcut"

Function .onInit
 
  ReadRegStr $R0 HKLM \
  "Software\Microsoft\Windows\CurrentVersion\Uninstall\DreamChess" \
  "UninstallString"
  StrCmp $R0 "" done
 
  MessageBox MB_OKCANCEL|MB_ICONEXCLAMATION \
  "DreamChess is already installed. $\n$\nClick 'OK' to remove the \
  previous version or 'Cancel' to cancel this upgrade." \
  IDOK uninst
  Abort
  
;Run the uninstaller
uninst:
  ClearErrors
  Exec $R0
  
done:
 
FunctionEnd

Function "DesktopShortcut"
CreateShortCut "$DESKTOP\DreamChess.lnk" "$INSTDIR\DreamChess.exe"
FunctionEnd

!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH

!insertmacro MUI_LANGUAGE "English"

Section
  SetOutPath $INSTDIR

  File ..\..\src\DreamChess.exe
  File ..\..\src\dreamer\Dreamer.exe
  File /oname=Readme.txt ..\..\README
  File /oname=Authors.txt ..\..\AUTHORS
  File /oname=Copying.txt ..\..\COPYING
  File /oname=Copyright.txt ..\..\COPYRIGHT
  File /r /x .* ..\..\data
  File /r /x .* ..\..\doc

  WriteUninstaller "$INSTDIR\Uninstall.exe"

  IfFileExists "$INSTDIR\DreamChess.exe" 0 new_installation
    StrCpy $ALREADY_INSTALLED 1
  new_installation:

  !insertmacro InstallLib DLL $ALREADY_INSTALLED REBOOT_NOTPROTECTED SDL.dll $SYSDIR\SDL.dll $SYSDIR
  !insertmacro InstallLib DLL $ALREADY_INSTALLED REBOOT_NOTPROTECTED SDL_image.dll $SYSDIR\SDL_image.dll $SYSDIR
  !insertmacro InstallLib DLL $ALREADY_INSTALLED REBOOT_NOTPROTECTED jpeg.dll $SYSDIR\jpeg.dll $SYSDIR
  !insertmacro InstallLib DLL $ALREADY_INSTALLED REBOOT_NOTPROTECTED libtiff-3.dll $SYSDIR\libtiff-3.dll $SYSDIR
  !insertmacro InstallLib DLL $ALREADY_INSTALLED REBOOT_NOTPROTECTED libpng12-0.dll $SYSDIR\libpng12-0.dll $SYSDIR
  !insertmacro InstallLib DLL $ALREADY_INSTALLED REBOOT_NOTPROTECTED zlib1.dll $SYSDIR\zlib1.dll $SYSDIR
  !insertmacro InstallLib DLL $ALREADY_INSTALLED REBOOT_NOTPROTECTED smpeg.dll $SYSDIR\smpeg.dll $SYSDIR
  !insertmacro InstallLib DLL $ALREADY_INSTALLED REBOOT_NOTPROTECTED libogg-0.dll $SYSDIR\libogg-0.dll $SYSDIR
  !insertmacro InstallLib DLL $ALREADY_INSTALLED REBOOT_NOTPROTECTED libvorbis-0.dll $SYSDIR\libvorbis-0.dll $SYSDIR
  !insertmacro InstallLib DLL $ALREADY_INSTALLED REBOOT_NOTPROTECTED libvorbisfile-3.dll $SYSDIR\libvorbisfile-3.dll $SYSDIR
  !insertmacro InstallLib DLL $ALREADY_INSTALLED REBOOT_NOTPROTECTED SDL_mixer.dll $SYSDIR\SDL_mixer.dll $SYSDIR

  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    CreateDirectory "$SMPROGRAMS\$STARTMENU_FOLDER"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\DreamChess.lnk" "$INSTDIR\DreamChess.exe"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Readme.lnk" "$INSTDIR\Readme.txt"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Authors.lnk" "$INSTDIR\Authors.txt"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Copying.lnk" "$INSTDIR\Copying.txt"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Copying.lnk" "$INSTDIR\Copyright.txt"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Uninstall.lnk" "$INSTDIR\Uninstall.exe"
  !insertmacro MUI_STARTMENU_WRITE_END

  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DreamChess" "DisplayName" "DreamChess ${VERSION}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DreamChess" "InstallLocation" "$INSTDIR"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DreamChess" "UninstallString" "$INSTDIR\Uninstall.exe"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DreamChess" "DisplayIcon" "$INSTDIR\DreamChess.exe"
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DreamChess" "NoModify" "1"
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DreamChess" "NoRepair" "1"
SectionEnd

Section "Uninstall"
  RMDir /r /REBOOTOK $INSTDIR

  !insertmacro MUI_STARTMENU_GETFOLDER Application $MUI_TEMP

  Delete "$SMPROGRAMS\$MUI_TEMP\Uninstall.lnk"
  Delete "$SMPROGRAMS\$MUI_TEMP\DreamChess.lnk"
  Delete "$SMPROGRAMS\$MUI_TEMP\Readme.lnk"
  Delete "$SMPROGRAMS\$MUI_TEMP\Authors.lnk"
  Delete "$SMPROGRAMS\$MUI_TEMP\Copying.lnk"
  Delete "$SMPROGRAMS\$MUI_TEMP\Copyright.lnk"

  Delete "$DESKTOP\DreamChess.lnk"

  !insertmacro UnInstallLib DLL SHARED REBOOT_NOTPROTECTED $SYSDIR\SDL.dll
  !insertmacro UnInstallLib DLL SHARED REBOOT_NOTPROTECTED $SYSDIR\SDL_image.dll
  !insertmacro UnInstallLib DLL SHARED REBOOT_NOTPROTECTED $SYSDIR\jpeg.dll
  !insertmacro UnInstallLib DLL SHARED REBOOT_NOTPROTECTED $SYSDIR\libtiff-3.dll
  !insertmacro UnInstallLib DLL SHARED REBOOT_NOTPROTECTED $SYSDIR\libpng12-0.dll
  !insertmacro UnInstallLib DLL SHARED REBOOT_NOTPROTECTED $SYSDIR\zlib1.dll
  !insertmacro UnInstallLib DLL SHARED REBOOT_NOTPROTECTED $SYSDIR\smpeg.dll
  !insertmacro UnInstallLib DLL SHARED REBOOT_NOTPROTECTED $SYSDIR\libogg-0.dll
  !insertmacro UnInstallLib DLL SHARED REBOOT_NOTPROTECTED $SYSDIR\libvorbis-0.dll
  !insertmacro UnInstallLib DLL SHARED REBOOT_NOTPROTECTED $SYSDIR\libvorbisfile-3.dll
  !insertmacro UnInstallLib DLL SHARED REBOOT_NOTPROTECTED $SYSDIR\SDL_mixer.dll

  StrCpy $MUI_TEMP "$SMPROGRAMS\$MUI_TEMP"
 
  startMenuDeleteLoop:
    ClearErrors
    RMDir $MUI_TEMP
    GetFullPathName $MUI_TEMP "$MUI_TEMP\.."

    IfErrors startMenuDeleteLoopDone

    StrCmp $MUI_TEMP $SMPROGRAMS startMenuDeleteLoopDone startMenuDeleteLoop
  startMenuDeleteLoopDone:

  DeleteRegKey HKCU "Software\DreamChess"
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\DreamChess"
SectionEnd
