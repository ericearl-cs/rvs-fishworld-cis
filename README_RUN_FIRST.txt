RVS CIS quick start

Daily use
1. Run `Run_RVS.vbs`

After code changes
1. Run `compile.bat`
2. Run `Run_RVS.vbs`

Local FoxPro mirror refresh
1. Run `import_cis_data.bat`
2. Run `generate_import_report.bat`

Server and migration tools
- server batch files now live under `tools\server\`
- migration guides now live under `docs\setup\`
- the old root batch names still work as wrappers

Templates
- sample config files now live under `docs\setup\templates\`

Important
- keep `config.properties` in the root for local use
- do not edit the `out` folder by hand
- edit source files in `src`
