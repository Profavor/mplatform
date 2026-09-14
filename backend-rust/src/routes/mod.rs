use crate::handlers::{
    approval, auth, chat, classification, code, dashboard, domain, dq, enterprise, excel,
    field_definition, file, governance, hash_chain, health, inbox, integration, matching, menu,
    notification, oidc, organization, permission, record, record_history, schema, search, system,
    two_factor, user, ws,
};
use crate::state::AppState;
use axum::{
    http::{header, HeaderValue, Method},
    routing::{delete, get, patch, post, put},
    Router,
};
use tower_http::cors::CorsLayer;
use tower_http::trace::TraceLayer;

pub fn create_router(state: AppState) -> Router {
    let allowed_origins: Vec<HeaderValue> = state
        .config
        .cors_allowed_origins
        .iter()
        .filter_map(|origin| origin.parse::<HeaderValue>().ok())
        .collect();

    let cors = CorsLayer::new()
        .allow_origin(allowed_origins)
        .allow_methods([
            Method::GET,
            Method::POST,
            Method::PUT,
            Method::DELETE,
            Method::OPTIONS,
            Method::PATCH,
        ])
        .allow_headers([
            header::AUTHORIZATION,
            header::ACCEPT,
            header::CONTENT_TYPE,
            header::ORIGIN,
        ])
        .allow_credentials(true);

    Router::new()
        // 1. Health & Status
        .route("/health", get(health::health_check))
        .route("/api/health", get(health::health_check))

        // 2. Auth & IAM & 2FA & OIDC
        .route("/api/auth/login", post(auth::login))
        .route("/api/auth/logout", post(auth::logout))
        .route("/api/auth/refresh", post(auth::refresh_token))
        .route("/api/auth/record-login", post(auth::record_login))
        .route("/api/auth/login-logs", get(auth::get_login_logs))
        .route("/api/auth/register", post(auth::self_register))
        .route("/api/auth/check-username", get(auth::check_username))
        .route("/api/auth/self-register", post(auth::self_register))
        .route("/api/auth/me", get(auth::get_current_user))
        .route("/api/auth/oidc/login", get(oidc::oidc_login))
        .route("/api/auth/oidc/callback", get(oidc::oidc_callback))
        .route("/api/auth/2fa/status", get(two_factor::get_status))
        .route("/api/auth/2fa/setup", post(two_factor::setup))
        .route("/api/auth/2fa/enable", post(two_factor::enable))
        .route("/api/auth/2fa/disable", post(two_factor::disable))
        .route("/api/auth/2fa/verify", post(two_factor::verify))
        .route("/api/auth/2fa/send-email", post(two_factor::send_email_otp))

        // 3. User Management & Profile
        .route("/api/users", get(user::get_all_users).post(user::create_user))
        .route("/api/users/map", get(user::get_user_map))
        .route("/api/users/me", put(user::update_self))
        .route("/api/users/me/password", put(user::change_my_password))
        .route("/api/users/timezone", post(user::update_timezone))
        .route("/api/users/:id/org-history", get(user::get_user_org_history))
        .route(
            "/api/users/:id",
            put(user::update_user).delete(user::delete_user),
        )
        .route("/api/users/:id/reset-password", post(user::reset_password))
        .route("/api/users/:id/temp-password", get(user::get_temp_password))

        // 4. Dynamic Domain & Schema
        .route("/api/domains", get(domain::get_domains).post(domain::create_domain))
        .route("/api/domains/dq-benchmark", get(domain::get_dq_benchmark))
        .route("/api/domains/dq-benchmark/trend", get(domain::get_dq_benchmark_trend))
        .route("/api/domains/specialized-templates", get(domain::get_specialized_templates))
        .route("/api/domains/specialized-provision", post(domain::provision_specialized_domain))
        .route("/api/domains/specialized-stock/seed-real-data", post(domain::seed_real_stock_data))
        .route("/api/domains/:id", get(domain::get_domain_by_id).put(domain::update_domain).delete(domain::delete_domain))
        .route("/api/domains/:domain_id/sectors", get(domain::get_sectors).post(domain::create_sector))
        .route("/api/domains/:domain_id/sectors/:sector_id", put(domain::update_sector).delete(domain::delete_sector))
        .route("/api/domains/:domain_id/groups", get(domain::get_groups).post(domain::create_group))
        .route("/api/domains/:domain_id/groups/:group_id", put(domain::update_group).delete(domain::delete_group))
        .route("/api/domains/:domain_id/layout", get(domain::get_domain_layout).put(domain::save_domain_layout))
        .route("/api/domains/:domain_id/nodes/:node_id/layout", get(domain::get_domain_node_layout).put(domain::save_domain_node_layout))
        .route("/api/nodes/:node_id/layout", get(domain::get_node_layout).put(domain::save_node_layout))
        .route("/api/classification-axes", get(classification::get_axes))
        .route("/api/classification-nodes", get(classification::get_nodes))
        .route("/api/domains/:domain_id/axes", get(classification::get_axes))
        .route("/api/domains/:domain_id/nodes", get(classification::get_nodes))
        .route("/api/domains/:domain_id/nodes/tree", get(field_definition::get_domain_nodes_tree))
        .route("/api/nodes/tree", get(field_definition::get_all_nodes_tree))
        .route("/api/nodes/:node_id", get(field_definition::get_node_by_id))
        .route("/api/nodes/:node_id/info", get(field_definition::get_node_info))

        // 5. Dynamic Field Definitions & Node Metadata
        .route("/api/nodes/:node_id/fields", post(field_definition::add_field))
        .route("/api/nodes/:node_id/fields/effective", get(field_definition::get_effective_fields))
        .route("/api/nodes/:node_id/fields/effective/page", get(field_definition::get_effective_fields_page))
        .route("/api/nodes/:node_id/fields/effective/as-of", get(field_definition::get_effective_fields_as_of))
        .route(
            "/api/nodes/:node_id/fields/:field_id",
            put(field_definition::update_field)
                .patch(field_definition::update_field)
                .delete(field_definition::delete_field),
        )
        .route("/api/domains/:domain_id/fields", get(field_definition::get_domain_fields).post(field_definition::add_domain_field))
        .route("/api/domains/:domain_id/fields/page", get(field_definition::get_domain_fields_page))
        .route(
            "/api/domains/:domain_id/fields/:field_id",
            put(field_definition::update_domain_field)
                .patch(field_definition::update_domain_field)
                .delete(field_definition::delete_domain_field),
        )
        .route("/api/fields/:field_id/dq-rules", get(field_definition::get_field_dq_rules).post(field_definition::save_field_dq_rules))

        // 6. Master Records & Node Operations
        .route("/api/records", get(record::get_records).post(record::create_record))
        .route("/api/records/:id", get(record::get_record_by_id))
        .route("/api/records/batch-validate", post(dq::batch_validate))
        .route("/api/records/merge", post(matching::merge_records))
        .route("/api/records/merge/auto", post(matching::merge_records))
        .route("/api/records/:id/unmerge", post(matching::unmerge_record))
        .route("/api/nodes/:node_id/records", get(record::get_node_records).post(record::create_node_record))
        .route("/api/nodes/:node_id/records/batch-upsert", post(record::batch_upsert_node_records))
        .route("/api/nodes/:node_id/records/batch-validate", post(record::batch_validate_node_records))
        .route("/api/records/domain/:domain_id", get(record::get_records_by_domain).delete(record::delete_records_by_domain))
        .route("/api/records/domain/:domain_id/search", get(record::search_records_by_domain))
        .route("/api/records/:id/secondary-nodes", get(record::get_secondary_nodes).post(record::add_secondary_nodes))
        .route("/api/records/:id/update-request", post(record::request_record_update))
        .route("/api/records/:id/delete-request", post(record::request_record_delete))
        .route("/api/records/search/complex", post(record::complex_search))

        // 7. Record History, Lineage & Time Machine & Masking
        .route("/api/records/:id/history", get(record_history::get_record_history))
        .route("/api/records/:id/rollback", post(record_history::rollback_record))
        .route("/api/records/:id/lineage", get(record_history::get_record_lineage))
        .route("/api/records/:id/timemachine/diff", get(record_history::get_record_timemachine_diff))
        .route("/api/records/:id/masked", get(record_history::get_masked_record))
        .route("/api/records/:id/masked/preview", post(record_history::preview_masked_record))

        // 8. Data Quality (DQ) Engine
        .route("/api/dq/rules", get(dq::get_rules).post(dq::create_rule))
        .route("/api/dq-rules", get(dq::get_rules).post(dq::create_rule))
        .route("/api/dq-rules/:rule_id", put(dq::update_rule).delete(dq::delete_rule))
        .route("/api/dq-rules/validate", post(dq::batch_validate))
        .route("/api/dq/scan/:domain_id", post(dq::scan_domain))
        .route("/api/dq/violations/:record_id", get(dq::get_record_violations))

        // 9. Matching & Golden Record
        .route("/api/matching-rules", get(matching::get_matching_rules))
        .route("/api/match-candidates", get(matching::get_candidates))
        .route("/api/domains/:domain_id/matching-rules", get(matching::get_matching_rules).post(matching::create_matching_rule))
        .route("/api/domains/:domain_id/matching-rules/:rule_id", put(matching::update_matching_rule).delete(matching::delete_matching_rule))
        .route("/api/domains/:domain_id/survivorship-rules", get(matching::get_survivorship_rules))
        .route("/api/records/domains/:domain_id/survivorship-rules", get(matching::get_survivorship_rules))

        // 10. Multi-step Approval Workflow & Approval-Requests
        .route("/api/approvals", get(approval::get_approvals).post(approval::create_approval))
        .route("/api/approvals/:id", get(approval::get_approval_by_id))
        .route("/api/approvals/:id/approve", post(approval::approve_step))
        .route("/api/approvals/:id/reject", post(approval::reject_step))
        .route("/api/approval-requests", get(approval::get_pending_requests).post(approval::create_approval))
        .route("/api/approval-requests/all", get(approval::get_all_requests))
        .route("/api/approval-requests/todos", get(approval::get_my_todos))
        .route("/api/approval-requests/my-requests", get(approval::get_my_requests))
        .route("/api/approvals/todos", get(approval::get_my_todos))
        .route("/api/approvals/my-requests", get(approval::get_my_requests))
        .route("/api/approval-requests/:id", get(approval::get_approval_by_id))
        .route("/api/approval-requests/steps/:id/approve", post(approval::approve_step))
        .route("/api/approval-requests/steps/:id/reject", post(approval::reject_step))
        .route("/api/approval-requests/steps/:id/admin-approve", post(approval::approve_step))
        .route("/api/approval-requests/steps/:id/admin-reject", post(approval::reject_step))
        .route("/api/approval-requests/memo", post(approval::add_memo))
        .route("/api/approval-requests/:id/cancel", post(approval::cancel_request))
        .route("/api/approval-requests/effective-workflow/:node_id", get(approval::get_effective_workflow))
        .route("/api/approval-requests/pending-schema-status", get(approval::get_pending_schema_status))
        .route("/api/approval-requests/available-workflows/:node_id", get(approval::get_available_workflows))
        .route("/api/approval-requests/effective-permission/:node_id", get(approval::get_effective_permission))
        .route("/api/approvals/delegations/my", get(approval::get_my_delegations))
        .route("/api/approvals/delegations", post(approval::create_delegation))
        .route("/api/approvals/delegations/:id", delete(approval::delete_delegation))
        .route("/api/approvals/escalate/scan", post(approval::scan_escalations))
        .route("/api/approvals/:id/sandbox-preview", get(approval::get_sandbox_preview))
        .route("/api/approvals/analytics/rejections", get(approval::get_rejection_analytics))
        .route("/api/approvals/routing-templates", get(approval::get_routing_templates).post(approval::create_routing_template))
        .route("/api/workflow-configs", get(approval::get_workflow_configs))
        .route("/api/workflow-configs/page", get(approval::get_workflow_configs_page))
        .route("/api/workflow-configs/:id", get(approval::get_workflow_config_by_id).delete(approval::delete_workflow_config))
        .route("/api/workflow-configs/domain/:domain_id", get(approval::get_node_workflow_configs).post(approval::save_workflow_config_for_domain))
        .route("/api/workflow-configs/node/:node_id", get(approval::get_node_workflow_configs).post(approval::save_workflow_config_for_node))

        // 11. Permissions, Scopes, Access Requests & Masking
        .route("/api/permissions/groups", get(permission::get_permission_groups).post(permission::create_permission_group))
        .route("/api/permissions/groups/:id", put(permission::update_permission_group).delete(permission::delete_permission_group))
        .route("/api/permissions/groups/:id/items", post(permission::add_permission_item))
        .route("/api/permissions/groups/items/:id", delete(permission::delete_permission_item))
        .route("/api/permissions/masking-policies", get(permission::get_masking_policies).post(permission::create_masking_policy))
        .route("/api/permissions/masking-policies/:id", delete(permission::delete_masking_policy))
        .route("/api/permissions/users/:user_id/scopes", get(permission::get_user_scopes).post(permission::add_user_scope))
        .route("/api/permissions/scopes/:id", delete(permission::delete_user_scope))
        .route("/api/permissions/users", get(permission::get_permissions_users))
        .route("/api/permissions/users/:user_id/domains", get(permission::get_user_domains))
        .route("/api/permissions/domains/available", get(permission::get_available_domains))
        .route(
            "/api/permissions/users/:user_id/domains/:domain_id",
            post(permission::assign_user_domain).delete(permission::revoke_user_domain),
        )
        .route("/api/permissions/requests/pending", get(permission::get_pending_access_requests))
        .route("/api/permissions/requests", post(permission::submit_access_request))
        .route("/api/permissions/requests/:id/approve", post(permission::approve_access_request))
        .route("/api/permissions/requests/:id/reject", post(permission::reject_access_request))
        .route("/api/permissions/requests/:id", delete(permission::delete_access_request))
        .route("/api/permissions/users/:user_id/tenant-info", put(permission::update_user_tenant_info))
        .route("/api/permissions/audit-logs", get(permission::get_permission_audit_logs))

        // 12. Schema Compatibility, Impact Analysis & Master Relations
        .route("/api/domains/:domain_id/schema-history", get(schema::get_schema_history))
        .route("/api/schema-history/:id", get(schema::get_schema_history_by_id))
        .route("/api/domains/:domain_id/schema/compatibility-check", get(schema::check_schema_compatibility))
        .route("/api/domains/:domain_id/schema/simulate-field-impact", post(schema::simulate_field_impact))
        .route("/api/domains/:domain_id/impact-analysis", post(schema::analyze_impact))
        .route("/api/domains/:domain_id/taxonomy-versions", get(schema::get_taxonomy_versions).post(schema::create_taxonomy_version))
        .route("/api/domains/snapshots", get(schema::get_domain_snapshots).post(schema::create_domain_snapshot))
        .route("/api/domains/:domain_id/multilingual", get(schema::get_domain_multilingual))
        .route("/api/domains/:domain_id/integrity", get(schema::check_domain_integrity))
        .route("/api/master-relations", get(schema::get_master_relations).post(schema::create_master_relation))
        .route("/api/master-relations/:id", put(schema::update_master_relation).delete(schema::delete_master_relation))

        // 13. Enterprise Security, Sensitive Data, Monitoring & Automation
        .route("/api/sensitive-data/statistics", get(enterprise::get_sensitive_data_statistics))
        .route("/api/sensitive-data/access-logs", get(enterprise::get_sensitive_data_access_logs))
        .route("/api/sensitive-data/record/:id/decrypt", post(enterprise::decrypt_record_data))
        .route("/api/sensitive-data/history/:id/decrypt", post(enterprise::decrypt_history_data))
        .route("/api/sensitive-data/approval/:id/decrypt", post(enterprise::decrypt_approval_data))
        .route("/api/integration/webhooks", get(enterprise::get_webhooks).post(enterprise::create_webhook))
        .route("/api/integration/webhooks/:id", delete(enterprise::delete_webhook))
        .route("/api/integration/api-keys", get(enterprise::get_api_keys).post(enterprise::create_api_key))
        .route("/api/integration/api-keys/:key_id", delete(enterprise::revoke_api_key))
        .route("/api/security/anomaly-detection", get(enterprise::get_anomaly_events))
        .route("/api/security/anomaly-detection/block", post(enterprise::block_anomaly_ip))
        .route("/api/system/freshness-heatmap", get(enterprise::get_freshness_heatmap))
        .route("/api/system/sla-contracts", get(enterprise::get_sla_contracts))
        .route("/api/system/volume-radar", get(enterprise::get_volume_radar))
        .route("/api/system/pipeline-healing", get(enterprise::get_pipeline_healing_status))
        .route("/api/system/pipeline-healing/trigger", post(enterprise::trigger_pipeline_healing))
        .route("/api/system/multi-region-conflicts", get(enterprise::get_multi_region_conflicts))
        .route("/api/system/archives", get(enterprise::get_system_archives))
        .route("/api/system/archives/:id/dr-simulate", post(enterprise::simulate_dr_recovery))
        .route("/api/system/master-orchestrator", get(enterprise::get_master_orchestrator))
        .route("/api/system/install-status", get(enterprise::get_system_install_status))
        .route("/api/system/install", get(enterprise::get_system_install_status))
        .route("/api/ontology/graph", get(enterprise::get_ontology_graph))
        .route("/api/ontology/search", get(enterprise::search_ontology))
        .route("/api/domains/:domain_id/smart-query", post(enterprise::smart_query_domain))
        .route("/api/music/state", get(enterprise::get_music_state))
        .route("/api/music/admin/play", post(enterprise::play_music))
        .route("/api/music/admin/stop", post(enterprise::stop_music))
        .route("/api/music/admin/youtube-config", get(enterprise::get_youtube_config).post(enterprise::save_youtube_config))
        .route("/api/inbox", get(inbox::get_inbox_messages))
        .route("/api/inbox/messages", get(inbox::get_inbox_messages).post(inbox::send_inbox_message))
        .route("/api/inbox/messages/:id", get(inbox::get_inbox_message).put(inbox::update_draft).delete(inbox::delete_inbox_message))
        .route("/api/inbox/messages/:id/reply", post(inbox::reply_message))
        .route("/api/inbox/messages/:id/reply-all", post(inbox::reply_all_message))
        .route("/api/inbox/messages/:id/forward", post(inbox::forward_message))
        .route("/api/inbox/messages/:id/read", patch(inbox::toggle_read))
        .route("/api/inbox/messages/:id/star", patch(inbox::toggle_star))
        .route("/api/inbox/messages/:id/folder", patch(inbox::move_to_folder))
        .route("/api/inbox/messages/:id/thread", get(inbox::get_thread))
        .route("/api/inbox/messages/:id/recall", post(inbox::recall_message))
        .route("/api/inbox/messages/bulk-action", post(inbox::bulk_inbox_action))
        .route("/api/inbox/unread-count", get(inbox::get_inbox_unread_count))
        .route("/api/inbox/folder-counts", get(inbox::get_inbox_folder_counts))
        .route("/api/inbox/track/open/:recipient_id", get(inbox::track_open))
        .route("/api/admin/mail", get(enterprise::get_mail_accounts))
        .route("/api/admin/mail/accounts", get(enterprise::get_mail_accounts))
        .route("/api/admin/mail/accounts/sync", post(enterprise::sync_mail_accounts))
        .route("/api/admin/mail/status", get(enterprise::get_mail_status))
        .route("/api/admin/mailing-lists", get(enterprise::get_mailing_lists))
        .route("/api/admin/mailing-lists/sync-aliases", post(enterprise::sync_mailing_list_aliases))

        // 14. Common Codes & Menus & Organizations
        .route("/api/codes", get(code::get_code_groups))
        .route("/api/codes/:id", get(code::get_details_by_group_id))
        .route("/api/code-groups", get(code::get_code_groups).post(code::create_code_group))
        .route("/api/code-groups/page", get(code::get_code_groups_paged))
        .route("/api/code-groups/code/:id/details", get(code::get_details_by_group_id))
        .route("/api/code-groups/details/:id", put(code::update_detail).delete(code::delete_detail))
        .route(
            "/api/code-groups/:id",
            get(code::get_code_group_by_id_or_code)
                .put(code::update_code_group)
                .delete(code::delete_code_group),
        )
        .route("/api/code-groups/:id/details", get(code::get_details_by_group_id).post(code::create_detail))
        .route("/api/code-groups/dump-seed", post(code::dump_seed))
        .route("/api/code-groups/sync-seed", post(code::sync_seed))
        .route("/api/code-groups/export", get(code::export_codes))
        .route("/api/code-groups/import", post(code::import_codes))
        .route("/api/menus", get(menu::get_menus).post(menu::create_menu))
        .route("/api/menus/:id", put(menu::update_menu).delete(menu::delete_menu))
        .route("/api/menus/tree", get(menu::get_menu_tree))
        .route("/api/menus/access", get(menu::get_my_recent_access).post(menu::log_menu_access))
        .route("/api/menus/logs", get(menu::get_access_logs))
        .route("/api/menus/dump-seed", post(menu::dump_seed))
        .route("/api/menus/sync-seed", post(menu::sync_seed))
        .route("/api/organizations", get(organization::get_organizations).post(organization::create_organization))
        .route("/api/organizations/:id", get(organization::get_organization_by_id).put(organization::update_organization).delete(organization::delete_organization))
        .route("/api/organizations/:id/departments", get(organization::get_departments).post(organization::create_department))
        .route("/api/organizations/:id/departments/:dept_id", put(organization::update_department).delete(organization::delete_department))
        .route("/api/organizations/:id/teams", get(organization::get_teams).post(organization::create_team))
        .route("/api/roles", get(organization::get_roles).post(organization::create_role))
        .route("/api/roles/:id", put(organization::update_role).delete(organization::delete_role))
        .route("/api/roles/org/:org_id", get(organization::get_roles_by_org))
        .route("/api/roles/org/:org_id/sync-defaults", post(organization::sync_role_defaults_for_org))
        .route("/api/roles/dump-seed", post(organization::dump_role_seed))
        .route("/api/roles/sync-defaults", post(organization::sync_role_defaults))

        // 15. File Storage System
        .route("/api/files/upload", post(file::upload_file))
        .route("/api/files/download/:file_name", get(file::download_file))
        .route("/api/files/info/:file_name", get(file::get_file_info))

        // 16. Realtime Notifications & WebSocket
        .route("/api/notifications", get(notification::get_my_notifications).post(notification::create_notification))
        .route("/api/notifications/:id", delete(notification::delete_notification))
        .route("/api/notifications/unread-count", get(notification::get_unread_count))
        .route("/api/notifications/clear-all", delete(notification::clear_all_notifications).post(notification::clear_all_notifications))
        .route("/api/notifications/mark-all-read", post(notification::mark_all_read))
        .route(
            "/api/notifications/:id/read",
            patch(notification::mark_as_read).put(notification::mark_as_read),
        )
        .route("/api/notifications/subscribe", get(ws::ws_handler))
        .route("/api/ws", get(ws::ws_handler))
        .route("/ws", get(ws::ws_handler))
        .route("/ws-stomp", get(ws::ws_handler))
        .route("/api/ws-stomp", get(ws::ws_handler))

        // 17. Realtime Chat Subsystem
        .route("/api/chat/rooms", get(chat::get_rooms).post(chat::create_room))
        .route("/api/chat/rooms/:room_id/members", get(chat::get_room_members).post(chat::invite_members).delete(chat::leave_room))
        .route("/api/chat/rooms/:room_id/members/:target_user_id", delete(chat::kick_member))
        .route("/api/chat/rooms/:room_id/creator", put(chat::delegate_creator))
        .route("/api/chat/rooms/:room_id/messages", get(chat::get_room_messages).post(chat::send_room_message))
        .route("/api/chat/rooms/:room_id/read", post(chat::mark_room_as_read))
        .route("/api/chat/rooms/:room_id", delete(chat::delete_room))
        .route("/api/chat/unread-count", get(chat::get_total_unread_count))
        .route("/api/chat/messages/:message_id", delete(chat::delete_message))
        .route("/api/chat/presence", get(chat::get_chat_presence))
        .route("/api/chat/translate", post(chat::translate_chat_message))
        .route("/api/chat/users", get(chat::get_chat_users))

        // 18. Global Search
        .route("/api/search", get(search::search))
        .route("/api/records/search", get(search::search))
        .route("/api/v1/search", get(search::search))

        // 19. Cryptographic Audit Ledger
        .route("/api/records/:record_id/ledger/verify", get(hash_chain::verify_ledger))

        // 20. Dashboard System
        .route("/api/dashboard/stats", get(dashboard::get_stats))
        .route("/api/dashboard/trends", get(dashboard::get_approval_trends))
        .route("/api/dashboard/domain-distribution", get(dashboard::get_domain_distribution))
        .route("/api/dashboard/dq-trends", get(dashboard::get_dq_trends))
        .route("/api/dashboard/dq-severity", get(dashboard::get_dq_severity_distribution))
        .route("/api/dashboard/lease-summary", get(dashboard::get_lease_summary))

        // 21. Excel Export & Bulk Import
        .route("/api/export/domains/:domain_id/template", get(excel::download_template))
        .route("/api/export/domains/:domain_id/records", get(excel::export_records))
        .route("/api/records/bulk-import/jobs", post(excel::start_import_job))
        .route("/api/records/bulk-import/jobs/:job_id/progress", get(excel::get_job_progress))

        // 22. Governance & Business Terms & Copilot & Compliance
        .route("/api/business-terms", get(governance::get_business_terms).post(governance::create_business_term))
        .route("/api/permissions/masking", get(governance::get_masking_policies).post(governance::create_masking_policy))
        .route("/api/governance/copilot/chat", post(governance::chat_copilot))
        .route("/api/governance/maturity", get(governance::get_maturity))
        .route("/api/governance/maturity-evaluation", get(governance::get_maturity))
        .route("/api/compliance/regulatory-audit", get(governance::run_compliance_regulatory_audit))

        // 23. System Diagnostics & Features & Config
        .route("/api/system/features", get(system::get_features))
        .route("/api/system/config", get(system::get_config))
        .route("/api/system/diagnostics", get(system::get_diagnostics))
        .route("/api/system/freshness", get(system::get_freshness))
        .route("/api/system/volume", get(system::get_volume))
        .route("/api/system/sla", get(system::get_sla))
        .route("/api/admin/error", get(system::get_error_logs))
        .route("/api/admin/error-logs", get(system::get_error_logs))

        // 24. Integration Channels & Logs
        .route("/api/admin/integration/channels", get(integration::get_channels).post(integration::create_channel))
        .route("/api/admin/integration/channels/:id", put(integration::update_channel).delete(integration::delete_channel))
        .route("/api/admin/integration/channels/:channel_id/test", post(integration::test_channel))
        .route("/api/admin/integration/channels/:channel_id/trigger-batch", post(integration::trigger_batch))
        .route("/api/integration/inbound/:channel_id", post(integration::handle_inbound))
        .route("/api/admin/integration/channels/stats", get(integration::get_channel_stats))
        .route("/api/admin/integration/channels/test-connection", post(integration::test_channel_connection))
        .route("/api/admin/integration/logs", get(integration::get_logs))
        .route("/api/admin/integration/logs/dead-letter", get(integration::get_dead_letter_logs))
        .route("/api/admin/integration/logs/dead-letter/retry-all", post(integration::retry_all_dead_letters))
        .route("/api/admin/integration/logs/by-record/:record_id", get(integration::get_logs_by_record))
        .route("/api/admin/integration/logs/:log_id/retry", post(integration::retry_log))
        .route("/api/admin/multi-tenant/routing-rules", get(integration::get_routing_rules))
        .layer(cors)
        .layer(TraceLayer::new_for_http())
        .with_state(state)
}
