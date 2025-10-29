<#--//@formatter:off -->

An Error occurred during the migration.

<#if configuration.hasCountryKeys() >
We attempted to migrate the following countries: ${configuration.countryKeys?join(", ")}
<#elseif configuration.hasContractIds() >
We attempted to migrate the following contracts: ${configuration.contractIds?map(id->id?string["0"])?join(", ")}
</#if>

But we got the following exception:
${exception}
