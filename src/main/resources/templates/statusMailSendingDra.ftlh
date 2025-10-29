<#--//@formatter:off -->
<#assign countryCodes = statistics.getCountryCodes()>
<#assign contractIds = statistics.getContractIds()>

Sending contracts to DRA during Perpetual Rights Backfill has been processed.

<#if countryCodes?size gt 0>
We attempted to send the data to DRA for the following countries: ${countryCodes?join(", ")}
<#elseif contractIds?size gt 0>
We attempted to send the data to DRA for the following contracts: ${contractIds?map(id->id?string["0"])?join(", ")}
</#if>

We sent ${statistics.sentCount} contracts to DRA.

We skipped ${statistics.skippedCount} contracts because those are not Rights-exported.

For further details or failed DRA sending see the log file.
