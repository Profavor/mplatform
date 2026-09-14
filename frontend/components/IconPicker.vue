<template>
  <div class="icon-picker">
    <va-input
      v-model="searchQuery"
      placeholder="Search icons (e.g., star, home, people)"
      class="mb-2"
      style="width: 100%;"
      clearable
    >
      <template #prependInner>
        <va-icon name="search" size="small" />
      </template>
    </va-input>
    
    <div class="icon-grid">
      <div 
        v-for="icon in filteredIcons" 
        :key="icon" 
        class="icon-item"
        :class="{ 'selected': icon === modelValue }"
        :title="icon"
        @click="selectIcon(icon)"
      >
        <va-icon :name="icon" style="font-size: 1.2rem;" />
      </div>
      <div v-if="filteredIcons.length === 0" class="no-icons">
        No icons found
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

const searchQuery = ref('')

// A comprehensive subset of popular material icons for the picker
const commonIcons = Array.from(new Set([
  // Action
  '3d_rotation', 'accessibility', 'accessibility_new', 'accessible', 'accessible_forward', 'account_balance', 'account_balance_wallet', 'account_box', 'account_circle', 'add_shopping_cart', 'alarm', 'alarm_add', 'alarm_off', 'alarm_on', 'all_inbox', 'all_out', 'analytics', 'anchor', 'android', 'announce', 'api', 'app_blocking', 'arrow_circle_down', 'arrow_circle_up', 'arrow_right_alt', 'article', 'aspect_ratio', 'assessment', 'assignment', 'assignment_ind', 'assignment_late', 'assignment_return', 'assignment_returned', 'assignment_turned_in', 'autorenew', 'backup', 'backup_table', 'batch_prediction', 'book', 'book_online', 'bookmark', 'bookmark_border', 'bookmarks', 'bug_report', 'build', 'build_circle', 'cached', 'calendar_today', 'calendar_view_day', 'camera_enhance', 'cancel_schedule_send', 'card_giftcard', 'card_membership', 'card_travel', 'change_history', 'check_circle', 'check_circle_outline', 'chrome_reader_mode', 'class', 'close_fullscreen', 'code', 'comment_bank', 'commute', 'compare_arrows', 'compass_calibration', 'contact_support', 'contactless', 'copyright', 'credit_card', 'credit_card_off', 'dashboard', 'dashboard_customize', 'date_range', 'delete', 'delete_forever', 'delete_outline', 'description', 'disabled_by_default', 'dns', 'done', 'done_all', 'done_outline', 'donut_large', 'donut_small', 'drag_indicator', 'dynamic_form', 'eco', 'edit_off', 'eject', 'euro_symbol', 'event', 'event_seat', 'exit_to_app', 'explore', 'explore_off', 'extension', 'face', 'face_unlock', 'fact_check', 'favorite', 'favorite_border', 'feedback', 'find_in_page', 'find_replace', 'fingerprint', 'fit_screen', 'flaky', 'flight_land', 'flight_takeoff', 'flip_to_back', 'flip_to_front', 'flutter_dash', 'format_list_bulleted', 'format_list_numbered', 'forum', 'g_translate', 'gavel', 'get_app', 'gif', 'grade', 'grading', 'group_work', 'help', 'help_center', 'help_outline', 'highlight_alt', 'highlight_off', 'history', 'history_toggle_off', 'home', 'horizontal_split', 'hourglass_bottom', 'hourglass_empty', 'hourglass_full', 'hourglass_top', 'http', 'https', 'important_devices', 'info', 'input', 'integration_instructions', 'invert_colors', 'label', 'label_important', 'label_off', 'language', 'launch', 'leaderboard', 'line_style', 'line_weight', 'list', 'list_alt', 'lock', 'lock_open', 'login', 'logout', 'loyalty', 'mark_as_unread', 'markunread_mailbox', 'maximize', 'mediation', 'minimize', 'model_training', 'next_plan', 'not_accessible', 'not_started', 'note_add', 'offline_bolt', 'offline_pin', 'online_prediction', 'opacity', 'open_in_browser', 'open_in_full', 'open_in_new', 'open_with', 'outbond', 'outbox', 'pageview', 'pan_tool', 'payment', 'pending', 'pending_actions', 'perm_camera_mic', 'perm_contact_calendar', 'perm_data_setting', 'perm_device_information', 'perm_identity', 'perm_media', 'perm_phone_msg', 'perm_scan_wifi', 'pets', 'picture_in_picture', 'picture_in_picture_alt', 'plagiarism', 'play_for_work', 'polymer', 'pool', 'pregnant_woman', 'preview', 'print', 'privacy_tip', 'published_with_changes', 'query_builder', 'question_answer', 'quickreply', 'receipt', 'record_voice_over', 'redeem', 'remove_shopping_cart', 'reorder', 'report_problem', 'request_page', 'restore', 'restore_from_trash', 'restore_page', 'room', 'rounded_corner', 'rowing', 'rule', 'rule_folder', 'saved_search', 'schedule', 'search', 'search_off', 'settings', 'settings_applications', 'settings_backup_restore', 'settings_bluetooth', 'settings_brightness', 'settings_cell', 'settings_ethernet', 'settings_input_antenna', 'settings_input_component', 'settings_input_composite', 'settings_input_hdmi', 'settings_input_svideo', 'settings_overscan', 'settings_phone', 'settings_power', 'settings_remote', 'settings_voice', 'shop', 'shop_two', 'shopping_bag', 'shopping_basket', 'shopping_cart', 'smart_button', 'source', 'speaker_notes', 'speaker_notes_off', 'spellcheck', 'star_rate', 'stars', 'sticky_note_2', 'store', 'subject', 'subtitles_off', 'supervised_user_circle', 'supervisor_account', 'support', 'support_agent', 'swap_horiz', 'swap_horizontal_circle', 'swap_vert', 'swap_vertical_circle', 'sync_alt', 'system_update_alt', 'tab', 'tab_unselected', 'table_view', 'text_rotate_up', 'text_rotate_vertical', 'text_rotation_angledown', 'text_rotation_angleup', 'text_rotation_down', 'text_rotation_none', 'theaters', 'thumb_down', 'thumb_up', 'thumbs_up_down', 'timeline', 'toc', 'today', 'toll', 'touch_app', 'tour', 'track_changes', 'translate', 'trending_down', 'trending_flat', 'trending_up', 'turned_in', 'turned_in_not', 'update', 'upgrade', 'verified', 'verified_user', 'vertical_split', 'view_agenda', 'view_array', 'view_carousel', 'view_column', 'view_day', 'view_headline', 'view_list', 'view_module', 'view_quilt', 'view_sidebar', 'view_stream', 'view_week', 'visibility', 'visibility_off', 'voice_over_off', 'watch_later', 'wifi_protected_setup', 'work', 'work_off', 'work_outline', 'wysiwyg', 'youtube_searched_for', 'zoom_in', 'zoom_out',
  // Communication
  'add_ic_call', 'alternate_email', 'business', 'call', 'call_end', 'call_made', 'call_merge', 'call_missed', 'call_missed_outgoing', 'call_received', 'call_split', 'cancel_presentation', 'chat', 'chat_bubble', 'chat_bubble_outline', 'clear_all', 'comment', 'contact_mail', 'contact_phone', 'contacts', 'desktop_access_disabled', 'dialer_sip', 'dialpad', 'domain_disabled', 'domain_verification', 'duo', 'email', 'forum', 'forward_to_inbox', 'import_contacts', 'import_export', 'invert_colors_off', 'list_alt', 'live_help', 'location_off', 'location_on', 'mail_outline', 'message', 'mobile_screen_share', 'more_time', 'nat', 'no_sim', 'pause_presentation', 'person_add_disabled', 'person_search', 'phone', 'phone_disabled', 'phone_enabled', 'portable_wifi_off', 'present_to_all', 'print_disabled', 'qr_code', 'qr_code_2', 'qr_code_scanner', 'read_more', 'ring_volume', 'rss_feed', 'screen_share', 'sentiment_satisfied_alt', 'speaker_phone', 'stay_current_landscape', 'stay_current_portrait', 'stay_primary_landscape', 'stay_primary_portrait', 'stop_screen_share', 'swap_calls', 'textsms', 'unsubscribe', 'voicemail', 'vpn_key', 'wifi_calling',
  // File
  'attach_email', 'attachment', 'cloud', 'cloud_circle', 'cloud_done', 'cloud_download', 'cloud_off', 'cloud_queue', 'cloud_upload', 'create_new_folder', 'download', 'download_done', 'folder', 'folder_open', 'folder_shared', 'request_quote', 'rule_folder', 'snippet_folder', 'text_snippet', 'topic', 'upload',
  // Hardware
  'browser_not_supported', 'cast', 'cast_connected', 'cast_for_education', 'computer', 'connected_tv', 'desktop_mac', 'desktop_windows', 'developer_board', 'device_hub', 'device_unknown', 'devices_other', 'dock', 'gamepad', 'headset', 'headset_mic', 'keyboard', 'keyboard_arrow_down', 'keyboard_arrow_left', 'keyboard_arrow_right', 'keyboard_arrow_up', 'keyboard_backspace', 'keyboard_capslock', 'keyboard_hide', 'keyboard_return', 'keyboard_tab', 'keyboard_voice', 'laptop', 'laptop_chromebook', 'laptop_mac', 'laptop_windows', 'memory', 'monitor', 'mouse', 'phone_android', 'phone_iphone', 'phonelink', 'phonelink_off', 'point_of_sale', 'power_input', 'router', 'scanner', 'security', 'sim_card', 'smart_display', 'smartphone', 'speaker', 'speaker_group', 'tablet', 'tablet_android', 'tablet_mac', 'toys', 'tv', 'videogame_asset', 'watch',
  // Navigation
  'app_settings_alt', 'apps', 'arrow_back', 'arrow_back_ios', 'arrow_downward', 'arrow_drop_down', 'arrow_drop_down_circle', 'arrow_drop_up', 'arrow_forward', 'arrow_forward_ios', 'arrow_left', 'arrow_right', 'arrow_upward', 'campaign', 'cancel', 'check', 'chevron_left', 'chevron_right', 'close', 'double_arrow', 'east', 'expand_less', 'expand_more', 'first_page', 'fullscreen', 'fullscreen_exit', 'home_work', 'last_page', 'legend_toggle', 'menu', 'menu_open', 'more_horiz', 'more_vert', 'north', 'north_east', 'north_west', 'offline_share', 'payments', 'refresh', 'south', 'south_east', 'south_west', 'subdirectory_arrow_left', 'subdirectory_arrow_right', 'switch_left', 'switch_right', 'unfold_less', 'unfold_more', 'waterfall_chart', 'west',
  // Social
  'cake', 'clean_hands', 'connect_without_contact', 'coronavirus', 'deck', 'domain', 'elderly', 'emoji_emotions', 'emoji_events', 'emoji_flags', 'emoji_food_beverage', 'emoji_nature', 'emoji_objects', 'emoji_people', 'emoji_symbols', 'emoji_transportation', 'engineering', 'facebook', 'fireplace', 'follow_the_signs', 'group', 'group_add', 'groups', 'history_edu', 'king_bed', 'location_city', 'luggage', 'masks', 'military_tech', 'mood', 'mood_bad', 'nights_stay', 'no_luggage', 'notifications', 'notifications_active', 'notifications_none', 'notifications_off', 'notifications_paused', 'outdoor_grill', 'pages', 'party_mode', 'people', 'people_alt', 'people_outline', 'person', 'person_add', 'person_add_alt_1', 'person_outline', 'person_remove', 'person_remove_alt_1', 'plus_one', 'poll', 'psychology', 'public', 'public_off', 'reduce_capacity', 'sanitizer', 'school', 'science', 'self_improvement', 'sentiment_dissatisfied', 'sentiment_satisfied', 'sentiment_very_dissatisfied', 'sentiment_very_satisfied', 'sick', 'single_bed', 'sports', 'sports_baseball', 'sports_basketball', 'sports_cricket', 'sports_esports', 'sports_football', 'sports_golf', 'sports_handball', 'sports_hockey', 'sports_kabaddi', 'sports_mma', 'sports_motorsports', 'sports_rugby', 'sports_soccer', 'sports_tennis', 'sports_volleyball', 'thumb_down_alt', 'thumb_up_alt', 'whatshot'
]))

const filteredIcons = computed(() => {
  if (!searchQuery.value) return commonIcons
  const q = searchQuery.value.toLowerCase()
  return commonIcons.filter(icon => icon.includes(q))
})

const selectIcon = (icon) => {
  if (props.modelValue === icon) {
    emit('update:modelValue', '') // toggle off
  } else {
    emit('update:modelValue', icon)
  }
}
</script>

<style scoped>
.icon-picker {
  border: 1px solid var(--va-background-border);
  border-radius: 8px;
  padding: 8px;
  background: var(--va-background-element);
}
.icon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(36px, 1fr));
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 4px;
}
.icon-item {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--va-text-primary);
  border: 2px solid transparent;
}
.icon-item:hover {
  background-color: var(--va-background-border);
  transform: scale(1.1);
}
.icon-item.selected {
  background-color: rgba(21, 78, 193, 0.1);
  color: var(--va-primary);
  border-color: var(--va-primary);
}
.no-icons {
  grid-column: 1 / -1;
  text-align: center;
  padding: 1rem;
  color: var(--va-text-secondary);
  font-size: 0.9rem;
}
</style>
