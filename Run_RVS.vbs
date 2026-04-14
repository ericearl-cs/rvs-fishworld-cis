' cSpell:words javaw rvsfishworld
Set shell = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
appDir = fso.GetParentFolderName(WScript.ScriptFullName)
cmd = "cmd /c cd /d """ & appDir & """ && javaw -cp ""out;lib\mysql-connector-j-9.6.0.jar"" com.rvsfishworld.App"
shell.Run cmd, 0, False
