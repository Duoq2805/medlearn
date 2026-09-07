---
session: ses_0d41
updated: 2026-07-04T09:06:54.517Z
---

<｜｜DSML｜｜tool_calls>
<｜｜DSML｜｜invoke name="bash">
<｜｜DSML｜｜parameter name="command" string="true">Set-Location -LiteralPath "D:\PersonalProject\medlearn_FirstVersion\medlearn\backend"; if ($?) { mvn test 2>&1 | Select-String -Pattern "BUILD|Tests run:|FAILURE|ERROR" }</｜｜DSML｜｜parameter>
<｜｜DSML｜｜parameter name="timeout" string="false">300000</｜｜DSML｜｜parameter>
</｜｜DSML｜｜invoke>
</｜｜DSML｜｜tool_calls>
