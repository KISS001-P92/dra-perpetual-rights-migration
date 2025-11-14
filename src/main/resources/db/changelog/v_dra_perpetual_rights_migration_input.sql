CREATE OR REPLACE VIEW v_dra_perpetual_rights_migration_input AS
select dprus.id  id,
       dprus.country_id,
       dsa.document_id,
       c.contract_id,
       dprus."Contract"::varchar as contract_number,
        dprus."Recording" as recording,
       ni_grand_parent.node_instance_id contract_dr_node_instance_id,
       drcu_ni_grand_parent.territory as contract_dr_territory_expression,
       transform_territory_expression(drcu_ni_grand_parent.territory) as contract_dr_territory,
       ni_parent.node_instance_id period_dr_node_instance_id,
       drcu_ni_parent.territory period_dr_territory_expression,
       transform_territory_expression(drcu_ni_parent.territory) period_dr_territory,
       ni.node_instance_id  recording_dr_node_instance_id,
       drcu_ni.territory as recording_dr_territory_expression,
       transform_territory_expression(drcu_ni.territory) as recording_dr_territory,
       dprus."Territory" as territory_on_territory,
       dprus."Contract Master Clearance" as contract_master_clearance,
       dprus."Period Master Clearance" as period_master_clearance,
       dprus."Recording Master Clearance" as recording_master_clearance,
       dprus."Perpetual Rights" as perpetual_rights,
       dprus."Perpetual Rights Exception" as perpetual_rights_exception,
       case when  dprus."Recording Master Clearance"!='' then "Recording Master Clearance"
            when  dprus."Period Master Clearance"!='' then "Period Master Clearance"
            when  dprus."Contract Master Clearance"!='' then "Contract Master Clearance"
            else null
        end as inherited_clearance,
       case when  dprus."Perpetual Rights Exception"!='' then substring( dprus."Perpetual Rights Exception",1,1)
            when  dprus."Perpetual Rights"!='' then substring( dprus."Perpetual Rights",1,1)
            else null
        end as intended_clearance,
       dprus.status_flag,
       dprus.mod_stamp,
       dprus.mod_user
from dra_perpetual_rights_migration_input_us dprus
join document_sony_artist dsa on dprus."Contract"::text = dsa.contract_number::text and dsa.status_flag ='A'
join v_document vd on dsa.document_id = vd.document_id and dprus."Doc Type" = vd.document_type_name
left join contract c on dsa.document_id = c.document_id  and c.status_flag ='A'
left join node_instance ni on c.contract_id = ni.contract_id and path like '%Digital Rights%' and ni.status_flag ='A' and ni.period_id is not null and ni.adm_recording_id is  not null and ni.name=dprus."Recording"
left join digital_rights_contract_us drcu_ni on ni.node_instance_id = drcu_ni.node_instance_id  and drcu_ni.status_flag='A' and drcu_ni.dra_rights_hierarchy_id = 1
left join node_instance ni_parent on ni.parent_id = ni_parent.node_instance_id  and ni_parent.path like '%Digital Rights%' and ni_parent.status_flag ='A'
left join digital_rights_contract_us drcu_ni_parent on ni_parent.node_instance_id = drcu_ni_parent.node_instance_id and drcu_ni_parent.status_flag='A'and drcu_ni_parent.dra_rights_hierarchy_id = 1
left join node_instance ni_grand_parent on ni_parent.parent_id = ni_grand_parent.node_instance_id  and ni_grand_parent.path like '%Digital Rights%' and ni_grand_parent.status_flag ='A'
left join digital_rights_contract_us drcu_ni_grand_parent on ni_grand_parent.node_instance_id = drcu_ni_grand_parent.node_instance_id and drcu_ni_grand_parent.status_flag='A'and drcu_ni_grand_parent.dra_rights_hierarchy_id = 1
union
select dprus.id as id,
       dprus.country_id,
       dsa.document_id,
       c.contract_id,
       dprus."Contract"::varchar as contract_number,
        dprus."Recording" as recording,
       ni_grand_parent.node_instance_id contract_dr_node_instance_id,
       drcu_ni_grand_parent.territory as contract_dr_territory_expression,
       transform_territory_expression(drcu_ni_grand_parent.territory) as contract_dr_territory,
       ni_parent.node_instance_id period_dr_node_instance_id,
       drcu_ni_parent.territory period_dr_territory_expression,
       transform_territory_expression(drcu_ni_parent.territory) period_dr_territory,
       ni.node_instance_id  recording_dr_node_instance_id,
       drcu_ni.territory as recording_dr_territory_expression,
       transform_territory_expression(drcu_ni.territory) as recording_dr_territory,
       dprus."Territory" as territory_on_territory,
       drcu_ni_grand_parent.approval_term::varchar(50)  as contract_master_clearance,
        drcu_ni_parent.approval_term::varchar(50)  as period_master_clearance,
        drcu_ni.approval_term::varchar(50)  as recording_master_clearance,
        dprus."Perpetual Rights" as perpetual_rights,
       dprus."Perpetual Rights Exception" as perpetual_rights_exception,
       case when  dprus."Recording Master Clearance"!='' then "Recording Master Clearance"
            when  dprus."Period Master Clearance"!='' then "Period Master Clearance"
            when  dprus."Contract Master Clearance"!='' then "Contract Master Clearance"
            else null
        end as inherited_clearance,
       case when  dprus."Perpetual Rights Exception"!='' then substring( dprus."Perpetual Rights Exception",1,1)
            when  dprus."Perpetual Rights"!='' then substring( dprus."Perpetual Rights",1,1)
            else null
        end as intended_clearance,
       dprus.status_flag,
       dprus.mod_stamp,
       dprus.mod_user
from dra_perpetual_rights_migration_input_eu dprus
join document_european_ba dsa on dprus."Contract" = dsa.contract_number and dsa.status_flag ='A'
join v_document vd on dsa.document_id = vd.document_id and dprus."Doc Type" = vd.document_type_name
left join contract c on dsa.document_id = c.document_id  and c.status_flag ='A'
left join node_instance ni on c.contract_id = ni.contract_id and path like '%Digital Rights%' and ni.status_flag ='A' and ni.period_id is not null and ni.adm_recording_id is  not null and ni.name=dprus."Recording"
left join digital_rights_contract_eu drcu_ni on ni.node_instance_id = drcu_ni.node_instance_id  and drcu_ni.status_flag='A' and drcu_ni.dra_rights_hierarchy_id = 1
left join node_instance ni_parent on ni.parent_id = ni_parent.node_instance_id  and ni_parent.path like '%Digital Rights%' and ni_parent.status_flag ='A'
left join digital_rights_contract_eu drcu_ni_parent on ni_parent.node_instance_id = drcu_ni_parent.node_instance_id and drcu_ni_parent.status_flag='A'and drcu_ni_parent.dra_rights_hierarchy_id = 1
left join node_instance ni_grand_parent on ni_parent.parent_id = ni_grand_parent.node_instance_id  and ni_grand_parent.path like '%Digital Rights%' and ni_grand_parent.status_flag ='A'
left join digital_rights_contract_eu drcu_ni_grand_parent on ni_grand_parent.node_instance_id = drcu_ni_grand_parent.node_instance_id and drcu_ni_grand_parent.status_flag='A'and drcu_ni_grand_parent.dra_rights_hierarchy_id = 1