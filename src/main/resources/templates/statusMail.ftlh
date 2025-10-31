<#--//@formatter:off -->
<#assign countryCodes = statistics.getCountryCodes()>
<#assign contractIds = statistics.getContractIds()>

Perpetual Rights backfill to Digital Rights has been processed.

<#if countryCodes?size gt 0>
We attempted to migrate the following countries: ${countryCodes?join(", ")}
<#elseif contractIds?size gt 0>
We attempted to migrate the following contracts: ${contractIds?map(id->id?string["0"])?join(", ")}
</#if>

Of which contains ${statistics.digitalRightsCount} Perpetual Rights backfill.

<#if  statistics.skippedCount gt 0>
We skipped ${statistics.skippedCount} rows due to different reasons.
</#if>

<#if  statistics.failedCount gt 0>
There are  ${statistics.failedCount} failed rows due to different reasons.
</#if>


For further details see protocol tables with actionId = ${statistics.actionId}
