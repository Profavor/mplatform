<template>
  <va-modal
    v-model="visible"
    fullscreen
    no-padding
    hide-default-actions
    without-transitions
    :z-index="99999"
    :zIndex="99999"
    class="record-layout-builder-modal"
  >
    <!-- Single Unified Compact Fixed Header Toolbar (Height: 46px) -->
    <div class="builder-unified-header">
      <!-- Left Section: Close Button + Title / Scope Badge + Preset Selector Group -->
      <div class="header-left-section">
        <!-- Close Button (Always First & Accessible) -->
        <va-button
          preset="plain"
          icon="close"
          color="secondary"
          size="small"
          class="btn-close-modal"
          :title="$t('close')"
          @click="visible = false"
        />

        <!-- Title & Scope Badge -->
        <div class="header-title-wrapper">
          <va-icon name="dashboard_customize" size="20px" color="primary" class="header-logo-icon" />
          <span class="builder-title-text">{{ $t('layout_builder_title') }}</span>
          <span class="target-scope-badge" :class="isDomainTarget ? 'badge-domain' : 'badge-node'">
            {{ formatTargetName || (isDomainTarget ? $t('layout_scope_domain') : $t('layout_scope_node')) }}
          </span>
        </div>

        <div class="toolbar-divider-v"></div>

        <!-- Presets Selection & Actions -->
        <div class="preset-selector-group">
          <!-- Normal Mode: Select Dropdown & Action Buttons -->
          <template v-if="!showInlineLayoutInput">
            <va-select
              v-model="activeLayoutId"
              :options="layoutSelectOptions"
              value-by="value"
              track-by="value"
              size="small"
              class="preset-select-control"
              @update:model-value="onLayoutChange"
            />

            <!-- New Layout Button -->
            <va-button
              preset="secondary"
              size="small"
              icon="add"
              class="compact-tool-btn"
              :title="$t('add_new_layout')"
              @click="openNewLayoutInline"
            />

            <!-- Duplicate Current Layout -->
            <va-button
              preset="secondary"
              size="small"
              icon="content_copy"
              class="compact-tool-btn"
              :title="$t('duplicate_layout')"
              @click="duplicateCurrentLayout"
            />

            <!-- Rename Current Layout -->
            <va-button
              preset="secondary"
              size="small"
              icon="edit"
              class="compact-tool-btn"
              :title="$t('rename_layout')"
              @click="openRenameLayoutInline"
            />

            <!-- Set as Default -->
            <va-button
              :preset="currentLayoutIsDefault ? 'primary' : 'secondary'"
              :color="currentLayoutIsDefault ? 'warning' : 'secondary'"
              size="small"
              :icon="currentLayoutIsDefault ? 'star' : 'star_border'"
              class="compact-tool-btn"
              :title="currentLayoutIsDefault ? $t('default_layout_badge') : $t('set_as_default_layout')"
              @click="toggleDefaultLayout"
            />

            <!-- Delete Layout -->
            <va-button
              preset="plain"
              color="danger"
              size="small"
              icon="delete"
              class="compact-tool-btn"
              :disabled="layouts.length <= 1"
              :title="$t('delete')"
              @click="deleteCurrentLayout"
            />
          </template>

          <!-- Inline Layout Name Editing Mode (Multilingual KO / EN) -->
          <template v-else>
            <div class="inline-name-edit-box">
              <va-input
                v-model="layoutNameKoInput"
                size="small"
                class="inline-name-input"
                :placeholder="$t('layout_name_ko_placeholder')"
                autofocus
                @keyup.enter="confirmInlineLayout"
                @keyup.esc="cancelInlineLayout"
              >
                <template #prependInner><span class="lang-tag">KO</span></template>
              </va-input>
              <va-input
                v-model="layoutNameEnInput"
                size="small"
                class="inline-name-input"
                :placeholder="$t('layout_name_en_placeholder')"
                @keyup.enter="confirmInlineLayout"
                @keyup.esc="cancelInlineLayout"
              >
                <template #prependInner><span class="lang-tag">EN</span></template>
              </va-input>
              <va-button size="small" color="primary" icon="check" @click="confirmInlineLayout" />
              <va-button size="small" preset="plain" color="secondary" icon="close" @click="cancelInlineLayout" />
            </div>
          </template>
        </div>
      </div>

      <!-- Right Section: Tools + Save Button -->
      <div class="header-right-section">
        <!-- Viewport Orientation Toggle (Landscape 12 cols vs Portrait 6 cols) -->
        <div class="viewport-mode-switcher">
          <button
            type="button"
            class="mode-switch-btn btn-mode-landscape"
            :class="{ active: orientationMode === 'landscape' }"
            @click="setOrientation('landscape')"
            :title="$t('layout_mode_landscape')"
          >
            <va-icon name="desktop_windows" size="14px" />
          </button>
          <button
            type="button"
            class="mode-switch-btn btn-mode-portrait"
            :class="{ active: orientationMode === 'portrait' }"
            @click="setOrientation('portrait')"
            :title="$t('layout_mode_portrait')"
          >
            <va-icon name="smartphone" size="14px" />
          </button>
        </div>

        <!-- Palette Toggle Button -->
        <button
          type="button"
          class="toolbar-action-btn btn-toggle-palette"
          :class="{ 'is-collapsed': isPaletteCollapsed }"
          :title="isPaletteCollapsed ? $t('palette_toggle_expand') : $t('palette_toggle_collapse')"
          @click="togglePalette"
        >
          <va-icon :name="isPaletteCollapsed ? 'menu_open' : 'menu'" size="14px" />
        </button>

        <!-- Auto Generate -->
        <va-button
          preset="secondary"
          color="info"
          size="small"
          icon="auto_awesome"
          class="compact-tool-btn"
          :title="$t('btn_auto_generate_layout')"
          @click="autoGenerateLayout"
        />

        <!-- Clear Layout -->
        <va-button
          preset="secondary"
          color="danger"
          size="small"
          icon="delete_sweep"
          class="compact-tool-btn"
          :title="$t('btn_clear_layout')"
          @click="clearLayout"
        />

        <!-- Save Button (Primary Solid) -->
        <va-button
          color="primary"
          size="small"
          icon="save"
          :loading="saving"
          class="action-btn-save"
          :title="$t('save')"
          @click="saveLayout"
        >
          <span class="save-btn-text">{{ $t('save') }}</span>
        </va-button>
      </div>
    </div>

    <!-- Main Builder Body (Collapsible Palette + Expansive Canvas + Slide-over Inspector) -->
    <div class="builder-body-wrapper">
      <!-- 1. LEFT PALETTE (COLLAPSIBLE) -->
      <div class="palette-sidebar" :class="{ 'is-collapsed': isPaletteCollapsed }">
        <div v-show="!isPaletteCollapsed" class="palette-inner-container">
          <div class="palette-tabs-header">
            <button
              type="button"
              :class="['palette-tab-btn', { active: activePaletteTab === 'fields' }]"
              @click="activePaletteTab = 'fields'"
            >
              <va-icon name="list_alt" size="small" class="mr-1" />
              {{ $t('palette_fields_tab') }} ({{ unplacedFields.length }})
            </button>
            <button
              type="button"
              :class="['palette-tab-btn', { active: activePaletteTab === 'widgets' }]"
              @click="activePaletteTab = 'widgets'"
            >
              <va-icon name="widgets" size="small" class="mr-1" />
              {{ $t('palette_widgets_tab') }}
            </button>
            <!-- Direct Collapse Button -->
            <button
              type="button"
              class="palette-close-btn"
              :title="$t('palette_toggle_collapse')"
              @click="isPaletteCollapsed = true"
            >
              <va-icon name="chevron_left" size="18px" />
            </button>
          </div>

          <div class="palette-content-scroll">
            <!-- Fields Pool Tab -->
            <div v-if="activePaletteTab === 'fields'" class="fields-palette-list">
              <va-input
                v-model="fieldSearchQuery"
                size="small"
                :placeholder="$t('search')"
                clearable
                class="mb-3 w-full"
              >
                <template #prependInner>
                  <va-icon name="search" size="small" />
                </template>
              </va-input>

              <div v-if="filteredUnplacedFields.length === 0" class="empty-palette-notice">
                {{ $t('no_unplaced_fields') }}
              </div>

              <div
                v-for="field in filteredUnplacedFields"
                :key="field.id || field.key"
                class="palette-item palette-field-item"
                @click="addPredefinedFieldWidget(field)"
              >
                <div class="palette-item-icon">
                  <va-icon :name="getFieldIcon(field.type)" size="small" color="primary" />
                </div>
                <div class="palette-item-info">
                  <span class="palette-item-name">{{ getFieldName(field) }}</span>
                  <span class="palette-item-key">{{ field.key }}</span>
                </div>
                <va-badge :text="field.type" color="secondary" size="small" />
              </div>
            </div>

            <!-- UI Widgets Tab (Categorized Rich Widget Palette) -->
            <div v-if="activePaletteTab === 'widgets'" class="widgets-palette-list">
              <div
                v-for="cat in widgetPaletteCategories"
                :key="cat.key"
                class="palette-category-group"
              >
                <div class="palette-category-header">
                  <va-icon :name="cat.icon" size="14px" color="primary" class="mr-1" />
                  <span class="palette-category-title">{{ $t(cat.labelKey) }}</span>
                </div>
                <div class="palette-category-items">
                  <div
                    v-for="item in cat.items"
                    :key="item.type"
                    class="palette-item"
                    @click="addCustomWidget(item.type, item.defaultW, item.defaultH, item.nameKey)"
                  >
                    <div class="palette-item-icon">
                      <va-icon :name="item.icon" size="small" color="primary" />
                    </div>
                    <div class="palette-item-info">
                      <span class="palette-item-name">{{ $t(item.nameKey) }}</span>
                      <span class="palette-item-desc">{{ item.desc }}</span>
                    </div>
                    <va-icon name="add" size="small" color="secondary" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Collapsed Strip Icon Button -->
        <div v-show="isPaletteCollapsed" class="palette-collapsed-strip" @click="isPaletteCollapsed = false">
          <va-icon name="chevron_right" size="medium" color="primary" />
          <span class="collapsed-vertical-text">{{ $t('palette_title') }}</span>
        </div>
      </div>

      <!-- 2. CENTER CANVAS (EXPANSIVE 2D GRID WORKSPACE) -->
      <div
        class="canvas-workspace-area"
        :class="{ 'is-portrait': orientationMode === 'portrait' }"
        ref="canvasAreaRef"
        @click="onCanvasBackgroundClick"
      >
        <!-- Floating Palette Expand Button when Collapsed -->
        <button
          v-if="isPaletteCollapsed"
          type="button"
          class="floating-palette-btn"
          :title="$t('palette_toggle_expand')"
          @click.stop="isPaletteCollapsed = false"
        >
          <va-icon name="widgets" size="small" class="mr-1" />
          <span>{{ $t('palette_title') }}</span>
          <va-icon name="chevron_right" size="small" class="ml-1" />
        </button>

        <!-- Inspector Backdrop Overlay on Mobile / Off-Canvas -->
        <div
          v-if="isInspectorOpen"
          class="inspector-backdrop"
          @click="closeInspector"
        />

        <div
          class="grid-canvas-container"
          :class="{ 'is-portrait': orientationMode === 'portrait' }"
          ref="gridContainerRef"
          :style="canvasGridStyle"
        >
          <div
            v-for="widget in widgets"
            :key="widget.id"
            :class="[
              'canvas-widget-box',
              'widget-' + (widget.type || '').toLowerCase(),
              { 'is-selected': selectedWidgetId === widget.id },
              { 'is-single-row': widget.h === 1 }
            ]"
            :style="getWidgetCanvasStyle(widget)"
            @click.stop="selectAndOpenInspector(widget)"
          >
            <!-- 1. SINGLE-ROW COMPACT MODE (h === 1) -->
            <template v-if="widget.h === 1">
              <div
                class="widget-single-row-content"
                @mousedown="startDragMove(widget, $event)"
              >
                <div class="single-row-left">
                  <va-icon :name="getWidgetIcon(widget.type)" size="14px" color="primary" class="mr-1" />
                  <span class="single-row-label">{{ getWidgetDisplayName(widget) }}</span>
                </div>
                <div class="single-row-center">
                  <!-- TEXT_BANNER -->
                  <div v-if="widget.type === 'TEXT_BANNER'" class="single-row-banner">
                    <strong class="banner-highlight-text">{{ getSampleValueForWidget(widget) }}</strong>
                  </div>
                  <!-- BADGE_TAG -->
                  <div v-else-if="widget.type === 'BADGE_TAG'" class="single-row-badge">
                    <va-badge :text="getSampleValueForWidget(widget)" color="primary" size="small" />
                  </div>
                  <!-- PROGRESS_BAR -->
                  <div v-else-if="widget.type === 'PROGRESS_BAR'" class="single-row-progress">
                    <div class="mini-progress-track"><div class="mini-progress-fill" style="width: 75%;" /></div>
                    <span class="mini-progress-text font-mono">75%</span>
                  </div>
                  <!-- RADIO_SEGMENT -->
                  <div v-else-if="widget.type === 'RADIO_SEGMENT'" class="single-row-segment">
                    <span class="mini-segment-btn active">Option A</span>
                    <span class="mini-segment-btn">Option B</span>
                  </div>
                  <!-- BOOLEAN_SWITCH -->
                  <div v-else-if="widget.type === 'BOOLEAN_SWITCH' || getFieldType(widget) === 'BOOLEAN'" class="single-row-bool">
                    <va-switch :model-value="true" size="small" readonly />
                    <span class="sample-val-text">ON</span>
                  </div>
                  <!-- MULTILINGUAL -->
                  <div v-else-if="widget.type === 'MULTILINGUAL_INPUT' || getFieldType(widget) === 'MULTILINGUAL'" class="single-row-multi">
                    <span class="sample-chip-tag">KO</span><span class="sample-val-text">{{ getMultilingualSample(widget).ko }}</span>
                    <span class="sample-chip-tag ml-1">EN</span><span class="sample-val-text">{{ getMultilingualSample(widget).en }}</span>
                  </div>
                  <!-- FILE -->
                  <div v-else-if="['FILE', 'FILE_ATTACHMENT'].includes(widget.type) || getFieldType(widget) === 'FILE'" class="single-row-file">
                    <va-icon name="attach_file" size="14px" color="info" />
                    <span class="sample-val-text">{{ getSampleValueForWidget(widget) }}</span>
                  </div>
                  <!-- SECTION -->
                  <div v-else-if="widget.type === 'SECTION'" class="single-row-section">
                    <strong>{{ getWidgetDisplayName(widget) }}</strong>
                  </div>
                  <!-- DIVIDER -->
                  <div v-else-if="widget.type === 'DIVIDER'" class="w-full">
                    <hr class="canvas-divider-hr" />
                  </div>
                  <!-- NUMBER_INPUT -->
                  <div v-else-if="['NUMBER_INPUT'].includes(widget.type) || ['NUMBER', 'INTEGER', 'DECIMAL', 'FLOAT'].includes(getFieldType(widget))" class="single-row-val-box">
                    <span class="mock-currency-badge">₩</span>
                    <span class="sample-val-text font-mono">{{ getSampleValueForWidget(widget) }}</span>
                  </div>
                  <!-- DEFAULT -->
                  <div v-else class="single-row-val-box">
                    <va-icon v-if="['DATE', 'DATE_PICKER'].includes(widget.type) || getFieldType(widget) === 'DATE'" name="event" size="12px" color="primary" class="mr-1" />
                    <va-icon v-else-if="widget.type === 'DATE_RANGE' || getFieldType(widget) === 'DATE_RANGE'" name="date_range" size="12px" color="primary" class="mr-1" />
                    <va-icon v-else-if="getFieldType(widget) === 'DATETIME'" name="schedule" size="12px" color="primary" class="mr-1" />
                    <va-icon v-else-if="['DOMAIN_REF_CARD', 'DOMAIN_REF', 'DOMAIN_REFERENCE'].includes(widget.type) || ['DOMAIN_REF', 'DOMAIN_REFERENCE'].includes(getFieldType(widget))" name="search" size="12px" color="primary" class="mr-1" />
                    <va-icon v-else-if="getFieldType(widget) === 'CALCULATED'" name="functions" size="12px" color="primary" class="mr-1" />
                    <span class="sample-val-text">{{ getSampleValueForWidget(widget) }}</span>
                  </div>
                </div>
                <div class="single-row-right">
                  <span class="widget-dimension-tag">{{ widget.w }}x{{ widget.h }}</span>
                  <!-- Edit Properties Button -->
                  <va-icon
                    name="tune"
                    size="13px"
                    class="widget-tool-btn"
                    :title="$t('widget_edit_properties')"
                    @click.stop="selectAndOpenInspector(widget)"
                  />
                  <!-- Delete Button -->
                  <va-icon
                    name="close"
                    size="13px"
                    class="widget-delete-btn"
                    @click.stop="deleteWidget(widget.id)"
                  />
                </div>
              </div>
            </template>

            <!-- 2. MULTI-ROW RICH MODE (h >= 2) -->
            <template v-else>
              <!-- Widget Header Bar (Move, Tools & Delete) -->
              <div
                class="widget-box-header"
                @mousedown="startDragMove(widget, $event)"
              >
                <div class="widget-box-title">
                  <va-icon :name="getWidgetIcon(widget.type)" size="14px" color="primary" class="mr-1" />
                  <span>{{ getWidgetDisplayName(widget) }}</span>
                </div>
                <div class="widget-box-tools">
                  <span class="widget-dimension-tag">{{ widget.w }} x {{ widget.h }}</span>
                  <va-icon
                    name="tune"
                    size="14px"
                    class="widget-tool-btn"
                    :title="$t('widget_edit_properties')"
                    @click.stop="selectAndOpenInspector(widget)"
                  />
                  <va-icon
                    name="close"
                    size="14px"
                    class="widget-delete-btn"
                    @click.stop="deleteWidget(widget.id)"
                  />
                </div>
              </div>

              <!-- Widget Body Preview with Rich Mock Data -->
              <div class="widget-box-body">
                <!-- IMAGE PREVIEW -->
                <div v-if="isImageField(widget)" class="inner-image-preview">
                  <div class="mock-image-container local-vector-placeholder">
                    <svg class="mock-vector-svg" viewBox="0 0 200 160" xmlns="http://www.w3.org/2000/svg" preserveAspectRatio="xMidYMid slice">
                      <defs>
                        <linearGradient id="bgGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                          <stop offset="0%" stop-color="#1e293b" />
                          <stop offset="100%" stop-color="#0f172a" />
                        </linearGradient>
                        <linearGradient id="avatarGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                          <stop offset="0%" stop-color="#3b82f6" />
                          <stop offset="100%" stop-color="#1d4ed8" />
                        </linearGradient>
                      </defs>
                      <rect width="200" height="160" fill="url(#bgGrad)" />
                      <circle cx="100" cy="55" r="28" fill="url(#avatarGrad)" opacity="0.85" />
                      <path d="M 55 135 C 55 98, 145 98, 145 135 Z" fill="url(#avatarGrad)" opacity="0.85" />
                      <circle cx="120" cy="72" r="10" fill="#0f172a" />
                      <circle cx="120" cy="72" r="8" fill="#10b981" />
                    </svg>
                    <div class="mock-image-badge">
                      <va-icon name="photo_camera" size="12px" color="#fff" />
                      <span>{{ widget.w }}x{{ widget.h }} {{ $t('widget_type_image') }}</span>
                    </div>
                  </div>
                </div>

                <!-- EDITOR PREVIEW -->
                <div v-else-if="isEditorField(widget)" class="inner-editor-preview">
                  <div class="editor-dummy-toolbar">
                    <span>B</span><span>I</span><span>U</span><span>H1</span><span>H2</span><span>List</span><span>Table</span>
                  </div>
                  <div class="editor-dummy-body mock-html-content">
                    <h4 class="mock-html-title">{{ $t('mock_notice_title') }}</h4>
                    <p class="mock-html-p">{{ $t('mock_notice_desc') }}</p>
                    <ul class="mock-html-ul">
                      <li>{{ $t('mock_notice_item1') }}</li>
                      <li>{{ $t('mock_notice_item2') }}</li>
                    </ul>
                  </div>
                </div>

                <!-- SECTION PREVIEW -->
                <div v-else-if="widget.type === 'SECTION'" class="inner-section-preview">
                  <va-icon name="folder_open" size="small" color="primary" class="mr-1" />
                  <strong>{{ getWidgetDisplayName(widget) }}</strong>
                </div>

                <!-- CALLOUT PREVIEW -->
                <div
                  v-else-if="widget.type === 'CALLOUT'"
                  class="inner-callout-preview"
                  :class="'callout-theme-' + (widget.options?.calloutType || 'warning').toLowerCase()"
                >
                  <va-icon
                    :name="widget.options?.calloutType === 'DANGER' ? 'error' : widget.options?.calloutType === 'SUCCESS' ? 'check_circle' : widget.options?.calloutType === 'INFO' ? 'info' : 'warning'"
                    size="small"
                    class="mr-2"
                  />
                  <span>{{ widget.options?.calloutText || getWidgetDisplayName(widget) || $t('mock_callout_desc') }}</span>
                </div>

                <!-- DIVIDER PREVIEW -->
                <div v-else-if="widget.type === 'DIVIDER'" class="inner-divider-preview">
                  <hr class="canvas-divider-hr" />
                </div>

                <!-- SPECIALIZED SUMMARY PREVIEW -->
                <div v-else-if="widget.type === 'SPECIALIZED_SUMMARY'" class="inner-specialized-preview">
                  <div class="specialized-preview-card">
                    <!-- Left Avatar -->
                    <div class="specialized-avatar-badge">
                      <va-icon name="account_box" size="large" color="primary" />
                    </div>

                    <!-- Main Wrapper -->
                    <div class="specialized-main-wrapper">
                      <!-- Top Row: Title/Domain on left, Audit Meta on right -->
                      <div class="specialized-top-row">
                        <div class="specialized-title-group">
                          <div class="specialized-title-row">
                            <strong class="specialized-title-name">{{ locale === 'en' ? 'Gildong Hong' : '홍길동' }}</strong>
                            <va-badge text="CUST-2026-000001" color="primary" size="small" />
                            <va-badge text="ACTIVE" color="success" size="small" />
                          </div>
                          <div class="specialized-domain-tag">
                            📁 {{ formatTargetName || $t('domain') }} · {{ getWidgetDisplayName(widget) }}
                          </div>
                        </div>

                        <!-- Top-Right Audit Meta Block (2-Column Grid, Date below User) -->
                        <div class="specialized-audit-meta">
                          <div class="audit-meta-grid">
                            <!-- 생성 정보 -->
                            <div class="audit-meta-col">
                              <div class="audit-user-line">
                                <span class="audit-meta-label">{{ $t('created_info') || '생성' }}:</span>
                                <span class="audit-meta-value">superadmin</span>
                              </div>
                              <div class="audit-date-line">2026-03-02 09:30</div>
                            </div>

                            <!-- 세로 구분선 -->
                            <div class="audit-col-divider" />

                            <!-- 수정 정보 -->
                            <div class="audit-meta-col">
                              <div class="audit-user-line">
                                <span class="audit-meta-label">{{ $t('updated_info') || '수정' }}:</span>
                                <span class="audit-meta-value">admin</span>
                              </div>
                              <div class="audit-date-line">
                                <span>2026-08-29 11:20</span>
                                <span class="version-tag">v2</span>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>

                      <!-- Bottom Sub-Fields Chips (Spread below divider) -->
                      <div v-if="widget.options?.subFieldKeys?.length > 0" class="specialized-subfields-chips">
                        <span
                          v-for="fKey in widget.options.subFieldKeys"
                          :key="fKey"
                          class="summary-field-chip"
                        >
                          <strong class="chip-label">{{ getFieldDisplayNameByKey(fKey) }}:</strong>
                          <span class="chip-val">{{ getSampleValueByKey(fKey) }}</span>
                        </span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- FIELD PREVIEW -->
                <div v-else class="inner-field-preview">
                  <!-- STAT_CARD (KPI 대형 숫자 지표) -->
                  <div v-if="widget.type === 'STAT_CARD'" class="mock-stat-card">
                    <div class="stat-card-top-label">{{ getWidgetDisplayName(widget) }}</div>
                    <div class="stat-card-main-val">
                      <span class="stat-num font-mono">{{ getSampleValueForWidget(widget) }}</span>
                      <span class="stat-unit">건</span>
                    </div>
                    <div class="stat-card-trend text-success">
                      <va-icon name="trending_up" size="14px" />
                      <span>+12.4% vs 지난달</span>
                    </div>
                  </div>

                  <!-- DATE_CALENDAR_CARD (캘린더 D-Day 카드) -->
                  <div v-else-if="widget.type === 'DATE_CALENDAR_CARD'" class="mock-calendar-card">
                    <div class="calendar-card-header">2026. 03</div>
                    <div class="calendar-card-body">
                      <span class="calendar-day-num">02</span>
                      <span class="calendar-day-text">월요일 · D-Day</span>
                    </div>
                  </div>

                  <!-- TEXT_BANNER (대형 강조 배너) -->
                  <div v-else-if="widget.type === 'TEXT_BANNER'" class="mock-banner-card">
                    <div class="banner-title">{{ getSampleValueForWidget(widget) }}</div>
                    <div class="banner-sub">{{ getWidgetDisplayName(widget) }}</div>
                  </div>

                  <!-- TEXT_AREA (멀티라인 메모) -->
                  <div v-else-if="widget.type === 'TEXT_AREA' || widget.type === 'EDITOR'" class="mock-textarea-card">
                    <p class="mock-textarea-line">1. {{ getSampleValueForWidget(widget) }}</p>
                    <p class="mock-textarea-line text-secondary">2. 고객 요청사항 및 특이사항 상세 기록</p>
                  </div>

                  <!-- MULTI_CHIP_SELECT (태그 칩 다중 선택) -->
                  <div v-else-if="widget.type === 'MULTI_CHIP_SELECT' || getFieldType(widget) === 'MULTI_SELECT'" class="mock-chips-card">
                    <va-badge text="VIP고객" color="primary" size="small" class="mr-1 mb-1" />
                    <va-badge text="이메일수신동의" color="success" size="small" class="mr-1 mb-1" />
                    <va-badge text="정기결제" color="info" size="small" class="mr-1 mb-1" />
                  </div>

                  <!-- BOOLEAN_CARD (대형 체크 상태 카드) -->
                  <div v-else-if="widget.type === 'BOOLEAN_CARD'" class="mock-bool-card-large">
                    <va-icon name="check_circle" size="28px" color="success" />
                    <div class="bool-card-text">
                      <strong class="bool-card-val text-success">YES (ACTIVE)</strong>
                      <span class="bool-card-label">{{ getWidgetDisplayName(widget) }}</span>
                    </div>
                  </div>

                  <!-- PROGRESS_BAR -->
                  <div v-else-if="widget.type === 'PROGRESS_BAR'" class="mock-progress-card">
                    <div class="progress-bar-labels">
                      <span>{{ getWidgetDisplayName(widget) }}</span>
                      <span class="font-mono font-bold text-primary">78%</span>
                    </div>
                    <div class="progress-track">
                      <div class="progress-fill" style="width: 78%;" />
                    </div>
                  </div>

                  <!-- RADIO_SEGMENT -->
                  <div v-else-if="widget.type === 'RADIO_SEGMENT'" class="mock-segment-card">
                    <button type="button" class="segment-btn active">Option A</button>
                    <button type="button" class="segment-btn">Option B</button>
                    <button type="button" class="segment-btn">Option C</button>
                  </div>

                  <!-- TABLE / JSON SUBTABLE PREVIEW -->
                  <div v-else-if="isTableField(widget) || widget.type === 'JSON_SUBTABLE'" class="canvas-subtable-preview">
                    <div class="subtable-mini-bar">
                      <span class="subtable-mini-label">{{ $t('mock_subtable_title') }}</span>
                      <va-badge :text="'+ ' + $t('add_row')" color="info" size="small" />
                    </div>
                    <table class="subtable-mini-table">
                      <thead>
                        <tr>
                          <th style="width: 24px; text-align: center;">#</th>
                          <th>{{ $t('mock_subtable_col_inst') }}</th>
                          <th>{{ $t('mock_subtable_col_major') }}</th>
                          <th>{{ $t('mock_subtable_col_degree') }}</th>
                          <th>{{ $t('mock_subtable_col_period') }}</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr>
                          <td style="text-align: center; color: var(--va-text-secondary);">1</td>
                          <td style="font-weight: 600; color: var(--va-primary);">{{ $t('mock_subtable_r1_inst') }}</td>
                          <td>{{ $t('mock_subtable_r1_major') }}</td>
                          <td>{{ $t('mock_subtable_r1_degree') }}</td>
                          <td>{{ $t('mock_subtable_r1_period') }}</td>
                        </tr>
                        <tr>
                          <td style="text-align: center; color: var(--va-text-secondary);">2</td>
                          <td style="font-weight: 600; color: var(--va-primary);">{{ $t('mock_subtable_r2_inst') }}</td>
                          <td>{{ $t('mock_subtable_r2_major') }}</td>
                          <td>{{ $t('mock_subtable_r2_degree') }}</td>
                          <td>{{ $t('mock_subtable_r2_period') }}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  <!-- DATE -->
                  <div v-else-if="['DATE', 'DATE_PICKER'].includes(widget.type) || getFieldType(widget) === 'DATE'" class="mock-field-card">
                    <va-icon name="calendar_month" size="16px" color="primary" />
                    <span class="mock-field-val">{{ getSampleValueForWidget(widget) }}</span>
                  </div>

                  <!-- DATETIME -->
                  <div v-else-if="getFieldType(widget) === 'DATETIME'" class="mock-field-card">
                    <va-icon name="schedule" size="16px" color="primary" />
                    <span class="mock-field-val">{{ getSampleValueForWidget(widget) }}</span>
                  </div>

                  <!-- NUMBER / CURRENCY -->
                  <div v-else-if="['NUMBER_INPUT'].includes(widget.type) || ['NUMBER', 'INTEGER', 'DECIMAL', 'FLOAT'].includes(getFieldType(widget))" class="mock-field-card">
                    <span class="mock-currency-badge">₩</span>
                    <span class="mock-field-val font-mono">{{ getSampleValueForWidget(widget) }}</span>
                  </div>

                  <!-- SELECT / CODE / ENUM -->
                  <div v-else-if="['SELECT_DROPDOWN'].includes(widget.type) || ['SELECT', 'CODE', 'ENUM'].includes(getFieldType(widget))" class="mock-field-card">
                    <va-badge text="ACTIVE" color="success" size="small" class="mr-1" />
                    <span class="mock-field-val">{{ getSampleValueForWidget(widget) }}</span>
                  </div>

                  <!-- BOOLEAN -->
                  <div v-else-if="['BOOLEAN_SWITCH'].includes(widget.type) || getFieldType(widget) === 'BOOLEAN'" class="mock-bool-card">
                    <va-switch :model-value="true" size="small" readonly />
                    <span class="mock-bool-label font-bold text-success">{{ $t('mock_bool_label') }}</span>
                  </div>

                  <!-- DOMAIN_REF -->
                  <div v-else-if="['DOMAIN_REF_CARD', 'DOMAIN_REF', 'DOMAIN_REFERENCE'].includes(widget.type) || ['DOMAIN_REF', 'DOMAIN_REFERENCE'].includes(getFieldType(widget))" class="mock-ref-card">
                    <va-icon name="corporate_fare" size="16px" color="primary" />
                    <span class="mock-field-val">{{ getSampleValueForWidget(widget) }}</span>
                    <va-icon name="open_in_new" size="14px" color="secondary" style="margin-left: auto;" />
                  </div>

                  <!-- MULTILINGUAL -->
                  <div v-else-if="getFieldType(widget) === 'MULTILINGUAL'" class="mock-multilingual-card">
                    <div class="mock-lang-row">
                      <span class="sample-chip-tag">KO</span>
                      <span class="mock-field-val">{{ getMultilingualSample(widget).ko }}</span>
                    </div>
                    <div class="mock-lang-row">
                      <span class="sample-chip-tag">EN</span>
                      <span class="mock-field-val">{{ getMultilingualSample(widget).en }}</span>
                    </div>
                  </div>

                  <!-- FILE -->
                  <div v-else-if="['FILE_ATTACHMENT', 'FILE'].includes(widget.type) || getFieldType(widget) === 'FILE'" class="mock-file-card">
                    <va-icon name="picture_as_pdf" size="18px" color="danger" />
                    <div class="mock-file-info">
                      <span class="mock-file-name">{{ $t('mock_file_name') }}</span>
                      <span class="mock-file-size">{{ $t('mock_file_size') }}</span>
                    </div>
                  </div>

                  <!-- CALCULATED -->
                  <div v-else-if="getFieldType(widget) === 'CALCULATED'" class="mock-calc-card">
                    <span class="mock-fx-badge">f(x)</span>
                    <span class="mock-field-val font-mono">{{ getSampleValueForWidget(widget) }}</span>
                  </div>

                  <!-- DEFAULT TEXT -->
                  <div v-else class="mock-field-card">
                    <va-icon name="badge" size="16px" color="secondary" />
                    <span class="mock-field-val">{{ getSampleValueForWidget(widget) }}</span>
                  </div>
                </div>
              </div>
            </template>

            <!-- 1. Right Edge Resize Handle (East / Width Only) -->
            <div
              class="widget-resize-handle-e"
              :title="$t('drag_width_resize_hint')"
              @mousedown.stop.prevent="startResize(widget, $event, 'e')"
            />

            <!-- 2. Bottom Edge Resize Handle (South / Height Only) -->
            <div
              class="widget-resize-handle-s"
              :title="$t('drag_height_resize_hint')"
              @mousedown.stop.prevent="startResize(widget, $event, 's')"
            />

            <!-- 3. Corner Resize Handle (South-East / Width & Height) -->
            <div
              class="widget-resize-handle-se"
              :title="$t('drag_to_resize_hint')"
              @mousedown.stop.prevent="startResize(widget, $event, 'se')"
            >
              <svg width="10" height="10" viewBox="0 0 10 10" fill="currentColor">
                <path d="M9 1L1 9M9 5L5 9M9 9L9 9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
              </svg>
            </div>

            <!-- Real-time Resizing Overlay -->
            <div v-if="resizingWidgetId === widget.id" class="resizing-badge-overlay">
              {{ resizingW }} x {{ resizingH }}
            </div>
          </div>
        </div>
      </div>

      <!-- 3. RIGHT SLIDE-OVER INSPECTOR DRAWER (OFF-CANVAS OVERLAY PANEL) -->
      <div
        class="inspector-slide-drawer"
        :class="{ 'is-open': isInspectorOpen }"
      >
        <!-- Drawer Header -->
        <div class="drawer-header">
          <div class="drawer-header-left">
            <va-icon name="tune" size="18px" color="primary" class="mr-2" />
            <div>
              <h4 class="drawer-heading">{{ $t('inspector_drawer_title') }}</h4>
              <span v-if="selectedWidget" class="drawer-subheading font-mono">
                {{ selectedWidget.w }}x{{ selectedWidget.h }} (x:{{ selectedWidget.x }}, y:{{ selectedWidget.y }})
              </span>
            </div>
          </div>
          <div class="drawer-header-right">
            <va-button
              size="small"
              color="primary"
              icon="check"
              class="mr-1 btn-inspector-apply btn-header-done"
              @click="applyAndCloseInspector"
            >
              {{ $t('btn_done') }}
            </va-button>
            <va-button
              preset="plain"
              icon="close"
              color="secondary"
              size="small"
              @click="closeInspector"
            />
          </div>
        </div>

        <!-- Drawer Content -->
        <div v-if="selectedWidget" class="drawer-content">
          <!-- 0. Data Field Binding Selector (For non-layout widgets) -->
          <div
            v-if="!['SECTION', 'DIVIDER', 'CALLOUT', 'SPECIALIZED_SUMMARY'].includes(selectedWidget.type)"
            class="inspector-section"
          >
            <span class="inspector-section-title">{{ $t('bind_field_label') }}</span>
            <div class="inspector-single-col-form">
              <va-select
                :model-value="selectedWidget.fieldKey"
                :options="fieldBindingSelectOptions"
                value-by="value"
                text-by="text"
                track-by="value"
                teleport="body"
                clearable
                size="small"
                class="w-full"
                :placeholder="$t('bind_field_placeholder')"
                @update:model-value="onWidgetFieldBound(selectedWidget, $event)"
              >
                <template #prependInner>
                  <va-icon name="link" size="small" color="primary" />
                </template>
              </va-select>
              <div v-if="!selectedWidget.fieldKey" class="unbound-hint-text">
                <va-icon name="info" size="12px" color="secondary" class="mr-1" />
                <span>{{ $t('unbound_widget_notice') }}</span>
              </div>
            </div>
          </div>

          <!-- 1-Column Widget Dimension / Grid Span Controls -->
          <div class="inspector-section">
            <span class="inspector-section-title">{{ $t('position_and_size') }}</span>
            <div class="inspector-single-col-form">
              <div class="inspector-field-item">
                <span class="inspector-label">{{ $t('widget_width') }} (1~{{ cols }})</span>
                <va-input
                  type="number"
                  v-model.number="selectedWidget.w"
                  :min="1"
                  :max="cols"
                  size="small"
                  class="w-full"
                  @change="onDimensionChange(selectedWidget)"
                />
              </div>

              <div class="inspector-field-item">
                <span class="inspector-label">{{ $t('widget_height') }} (1~30)</span>
                <va-input
                  type="number"
                  v-model.number="selectedWidget.h"
                  :min="1"
                  :max="30"
                  size="small"
                  class="w-full"
                  @change="onDimensionChange(selectedWidget)"
                />
              </div>

              <div class="inspector-field-item">
                <span class="inspector-label">{{ $t('widget_pos_x') }}</span>
                <va-input
                  type="number"
                  v-model.number="selectedWidget.x"
                  :min="0"
                  :max="cols - 1"
                  size="small"
                  class="w-full"
                  @change="onDimensionChange(selectedWidget)"
                />
              </div>

              <div class="inspector-field-item">
                <span class="inspector-label">{{ $t('widget_pos_y') }}</span>
                <va-input
                  type="number"
                  v-model.number="selectedWidget.y"
                  :min="0"
                  :max="50"
                  size="small"
                  class="w-full"
                  @change="onDimensionChange(selectedWidget)"
                />
              </div>
            </div>
          </div>

          <!-- 1-Column Label Override -->
          <div class="inspector-section">
            <span class="inspector-section-title">{{ $t('widget_label') }}</span>
            <div class="inspector-single-col-form">
              <div class="inspector-field-item">
                <span class="inspector-label">{{ $t('label_ko') }}</span>
                <va-input
                  v-model="selectedWidgetTitleKo"
                  size="small"
                  class="w-full"
                />
              </div>
              <div class="inspector-field-item">
                <span class="inspector-label">{{ $t('label_en') }}</span>
                <va-input
                  v-model="selectedWidgetTitleEn"
                  size="small"
                  class="w-full"
                />
              </div>
            </div>
          </div>

          <!-- 1-Column Options -->
          <div class="inspector-section">
            <span class="inspector-section-title">{{ $t('options') }}</span>
            <div class="inspector-single-col-form">
              <va-checkbox
                v-model="selectedWidgetOptions.highlight"
                :label="$t('widget_highlight')"
              />
              <va-checkbox
                v-model="selectedWidgetOptions.readOnly"
                :label="$t('widget_readonly')"
              />
              <va-checkbox
                v-model="selectedWidgetOptions.required"
                :label="$t('widget_required')"
              />
            </div>
          </div>

          <!-- SPECIALIZED_SUMMARY (도메인 헤더 요약 위젯) 전용 속성 필드 멀티 선택 섹션 -->
          <div v-if="selectedWidget.type === 'SPECIALIZED_SUMMARY'" class="inspector-section">
            <span class="inspector-section-title">{{ $t('header_summary_fields_select') }}</span>
            <span class="inspector-helper-desc">{{ $t('header_summary_fields_hint') }}</span>
            
            <div class="summary-field-selector-list">
              <div
                v-for="f in fields"
                :key="f.key || f.id"
                class="summary-field-option-item"
                :class="{ 'is-selected': isSubFieldSelected(f.key) }"
                @click="toggleSubField(f.key)"
              >
                <va-checkbox
                  :model-value="isSubFieldSelected(f.key)"
                  size="small"
                  class="mr-2"
                  @update:model-value="toggleSubField(f.key)"
                />
                <span class="summary-option-name">{{ getFieldName(f) }}</span>
                <span class="summary-option-key font-mono">{{ f.key }}</span>
              </div>
            </div>
          </div>

          <!-- CALLOUT (안내 콜아웃) 전용 설정 -->
          <div v-if="selectedWidget.type === 'CALLOUT'" class="inspector-section">
            <span class="inspector-section-title">{{ $t('callout_type') }}</span>
            <div class="callout-type-switcher">
              <button
                v-for="cType in ['INFO', 'WARNING', 'SUCCESS', 'DANGER']"
                :key="cType"
                type="button"
                :class="['callout-type-btn', 'btn-' + cType.toLowerCase(), { active: (selectedWidgetOptions.calloutType || 'WARNING') === cType }]"
                @click="selectedWidgetOptions.calloutType = cType"
              >
                {{ cType }}
              </button>
            </div>
            <div class="inspector-field-item mt-2">
              <span class="inspector-label">{{ $t('callout_text') }}</span>
              <va-textarea
                v-model="selectedWidgetOptions.calloutText"
                rows="2"
                size="small"
                class="w-full"
                :placeholder="$t('mock_callout_desc')"
              />
            </div>
          </div>

          <!-- Delete Widget -->
          <div class="inspector-section" style="border-bottom: none; margin-bottom: 0;">
            <va-button
              color="danger"
              preset="secondary"
              size="small"
              icon="delete"
              style="width: 100%;"
              @click="deleteSelectedWidget"
            >
              {{ $t('widget_delete') }}
            </va-button>
          </div>

          <!-- Drawer Sticky Footer Actions -->
          <div class="drawer-footer">
            <va-button
              preset="secondary"
              size="small"
              color="secondary"
              @click="closeInspector"
            >
              {{ $t('inspector_close') }}
            </va-button>
            <va-button
              size="small"
              color="primary"
              icon="check"
              class="btn-footer-apply"
              @click="applyAndCloseInspector"
            >
              {{ $t('btn_done') }}
            </va-button>
            <va-button
              size="small"
              color="info"
              icon="save"
              :loading="saving"
              @click="saveLayout"
            >
              {{ $t('save') }}
            </va-button>
          </div>
        </div>

        <div v-else class="empty-inspector-prompt">
          <va-icon name="touch_app" size="large" color="secondary" />
          <span>{{ $t('select_widget_to_edit_properties') }}</span>
        </div>
      </div>
    </div>
  </va-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from 'vuestic-ui'
import { useCustomFetch } from '~/composables/useCustomFetch'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  targetNode: {
    type: Object,
    default: null
  },
  domainId: {
    type: String,
    default: ''
  },
  fields: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'saved'])

const { t, locale } = useI18n()
const toast = useToast()
const { customFetch } = useCustomFetch()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// Orientation & Viewport Mode (Landscape: 12 Cols vs Portrait: 6 Cols)
const orientationMode = ref<'landscape' | 'portrait'>('landscape')
const cols = ref(12)
const rowHeight = ref(42)

// Collapsible Palette & Slide-over Inspector States
const isPaletteCollapsed = ref(false)
const isInspectorOpen = ref(false)

const togglePalette = () => {
  isPaletteCollapsed.value = !isPaletteCollapsed.value
}

const widgets = ref<any[]>([])
const activePaletteTab = ref('fields')
const fieldSearchQuery = ref('')
const selectedWidgetId = ref<string | null>(null)
const saving = ref(false)

// Multi-Layout Presets State
const layouts = ref<any[]>([
  {
    id: 'layout_default',
    name: { ko: '기본 레이아웃', en: 'Default Layout' },
    isDefault: true,
    cols: 12,
    rowHeight: 42,
    orientation: 'landscape',
    widgets: [],
    options: {}
  }
])
const activeLayoutId = ref<string>('layout_default')
const showInlineLayoutInput = ref(false)
const isRenaming = ref(false)
const layoutNameKoInput = ref('')
const layoutNameEnInput = ref('')

const getLayoutDisplayName = (layout: any) => {
  if (!layout) return ''
  if (typeof layout.name === 'object' && layout.name !== null) {
    return layout.name[locale.value] || layout.name.ko || layout.name.en || layout.id
  }
  return layout.name || layout.id
}

const currentActiveLayout = computed(() => {
  return layouts.value.find(l => l.id === activeLayoutId.value) || layouts.value[0] || null
})

const currentLayoutIsDefault = computed(() => {
  return currentActiveLayout.value?.isDefault === true
})

const layoutSelectOptions = computed(() => {
  if (!layouts.value || layouts.value.length === 0) {
    return [{ text: t('default_layout_name') || 'Default Layout', value: 'layout_default' }]
  }
  return layouts.value.map(l => {
    const isDef = l.isDefault ? ` (${t('default_layout_badge')})` : ''
    const displayName = getLayoutDisplayName(l)
    return {
      text: `${displayName}${isDef}`,
      value: l.id
    }
  })
})

const setOrientation = (mode: 'landscape' | 'portrait') => {
  orientationMode.value = mode
  const targetCols = mode === 'portrait' ? 6 : 12
  cols.value = targetCols

  // Re-clamp all widgets to fit the new column span
  widgets.value.forEach(w => {
    if (w.w > targetCols) {
      w.w = targetCols
    }
    if (w.x + w.w > targetCols) {
      w.x = Math.max(0, targetCols - w.w)
    }
  })

  // Resolve collisions gracefully
  if (widgets.value.length > 0) {
    resolveWidgetCollisions(widgets.value[0])
  }
}

const selectAndOpenInspector = (widget: any) => {
  selectedWidgetId.value = widget.id
  isInspectorOpen.value = true
}

const closeInspector = () => {
  isInspectorOpen.value = false
}

const applyAndCloseInspector = () => {
  if (selectedWidget.value) {
    onDimensionChange(selectedWidget.value)
  }
  isInspectorOpen.value = false
}

const onCanvasBackgroundClick = () => {
  // If user clicked the canvas background area (not a widget)
}

const onDimensionChange = (widget: any) => {
  if (!widget) return
  widget.w = Math.max(1, Math.min(cols.value, Number(widget.w) || 1))
  widget.h = Math.max(1, Math.min(30, Number(widget.h) || 1))
  widget.x = Math.max(0, Math.min(cols.value - widget.w, Number(widget.x) || 0))
  widget.y = Math.max(0, Math.min(50, Number(widget.y) || 0))
  resolveWidgetCollisions(widget)
}

const deleteSelectedWidget = () => {
  if (selectedWidgetId.value) {
    deleteWidget(selectedWidgetId.value)
    isInspectorOpen.value = false
  }
}

const getSampleValueForWidget = (widget: any) => {
  const fType = getFieldType(widget)
  const key = (widget.fieldKey || '').toLowerCase()
  const title = (getWidgetDisplayName(widget) || '').toLowerCase()

  if (key.includes('emp') || key.includes('code') || key.includes('id') || title.includes('사번') || title.includes('코드')) {
    return 'EMP-2026-0492'
  }
  if (key.includes('name') || title.includes('이름') || title.includes('성명')) {
    return locale.value === 'en' ? 'Gildong Hong' : '홍길동'
  }
  if (key.includes('p_number') || key.includes('resident') || key.includes('ssn') || title.includes('주민')) {
    return '880512-1******'
  }
  if (key.includes('email') || title.includes('이메일')) {
    return 'gildong.hong@company.com'
  }
  if (key.includes('phone') || key.includes('tel') || title.includes('전화') || title.includes('연락처')) {
    return '010-9876-5432'
  }
  if (key.includes('addr') || title.includes('주소')) {
    return locale.value === 'en' ? '152 Teheran-ro, Gangnam-gu, Seoul' : '서울특별시 강남구 테헤란로 152'
  }
  if (key.includes('dept') || key.includes('team') || title.includes('부서') || title.includes('소속')) {
    return locale.value === 'en' ? 'Global Enterprise DX Dev Team' : '글로벌 엔터프라이즈 DX개발팀'
  }
  if (key.includes('pos') || key.includes('rank') || title.includes('직급') || title.includes('직책')) {
    return locale.value === 'en' ? 'Senior Lead Researcher' : '책임연구원 (Senior Lead)'
  }
  if (['NUMBER', 'INTEGER', 'DECIMAL', 'FLOAT'].includes(fType) || key.includes('salary') || key.includes('pay') || key.includes('price') || title.includes('연봉') || title.includes('금액')) {
    return '65,000,000'
  }
  if (fType === 'DATE' || key.includes('date') || title.includes('일') || title.includes('날짜')) {
    return '2026-03-02'
  }
  if (fType === 'DATETIME') {
    return '2026-03-02 09:30'
  }
  if (fType === 'DATE_RANGE') {
    return '2026-03-02 ~ 2027-03-01'
  }
  if (['SELECT', 'CODE', 'ENUM'].includes(fType)) {
    return locale.value === 'en' ? 'Full-time' : '정규직 (Full-time)'
  }
  if (fType === 'MULTI_SELECT') {
    return locale.value === 'en' ? 'Planning, IT/Systems, Global' : '경영기획, IT/시스템, 글로벌사업'
  }
  if (fType === 'BOOLEAN') {
    return t('mock_bool_label')
  }
  if (fType === 'FILE') {
    return t('mock_file_name')
  }
  if (['DOMAIN_REF', 'DOMAIN_REFERENCE'].includes(fType)) {
    return 'REC-77391'
  }
  if (fType === 'CALCULATED') {
    return '72,000,000 KRW'
  }
  return `${getWidgetDisplayName(widget)} ${t('mock_sample_val_suffix')}`
}

const getMultilingualSample = (widget: any) => {
  const key = (widget.fieldKey || '').toLowerCase()
  const title = (getWidgetDisplayName(widget) || '').toLowerCase()
  if (key.includes('name') || title.includes('이름') || title.includes('성명')) {
    return { ko: '홍길동', en: 'Gildong Hong' }
  }
  if (key.includes('dept') || title.includes('부서')) {
    return { ko: 'DX개발팀', en: 'DX Dev Team' }
  }
  return { ko: `${getWidgetDisplayName(widget)} (KO)`, en: `${getWidgetDisplayName(widget)} (EN)` }
}

const isSubFieldSelected = (fieldKey: string) => {
  if (!selectedWidget.value) return false
  if (!selectedWidgetOptions.value.subFieldKeys) return false
  return selectedWidgetOptions.value.subFieldKeys.includes(fieldKey)
}

const toggleSubField = (fieldKey: string) => {
  if (!selectedWidget.value) return
  if (!selectedWidgetOptions.value.subFieldKeys) {
    selectedWidgetOptions.value.subFieldKeys = []
  }
  const list = [...selectedWidgetOptions.value.subFieldKeys]
  const idx = list.indexOf(fieldKey)
  if (idx > -1) {
    list.splice(idx, 1)
  } else {
    list.push(fieldKey)
  }
  selectedWidgetOptions.value.subFieldKeys = list
  if (!selectedWidget.value.options) selectedWidget.value.options = {}
  selectedWidget.value.options.subFieldKeys = list
}

const getFieldDisplayNameByKey = (fieldKey: string) => {
  const f = props.fields.find(field => field.key === fieldKey)
  if (!f) return fieldKey
  return getFieldName(f)
}

const getSampleValueByKey = (fieldKey: string) => {
  const f = props.fields.find(field => field.key === fieldKey)
  if (!f) return 'Sample Data'
  const fType = f.type || 'STRING'
  if (['DATE', 'DATE_TIME', 'DATETIME'].includes(fType)) return '2026-03-02'
  if (['NUMBER', 'INT', 'INTEGER', 'LONG', 'BIGINT', 'FLOAT', 'DOUBLE', 'DECIMAL'].includes(fType)) return '1,250'
  if (['BOOLEAN', 'BOOL'].includes(fType)) return 'TRUE'
  if (['EMAIL'].includes(fType) || fieldKey.toLowerCase().includes('email')) return 'user@example.com'
  if (['PHONE'].includes(fType) || fieldKey.toLowerCase().includes('phone')) return '010-9876-5432'
  if (['SELECT', 'MULTI_SELECT', 'CODE', 'ENUM'].includes(fType)) return locale.value === 'en' ? 'Full-time' : '정규직 (Full-time)'
  return `${getFieldName(f)} ${t('mock_sample_val_suffix')}`
}

// Resizing state
const resizingWidgetId = ref<string | null>(null)
const resizingW = ref(1)
const resizingH = ref(1)

const canvasAreaRef = ref<HTMLElement | null>(null)
const gridContainerRef = ref<HTMLElement | null>(null)

const effectiveDomainId = computed(() => {
  return props.domainId || props.targetNode?.domainId || (props.targetNode?.type === 'domain' || props.targetNode?.isDomain ? props.targetNode?.id : '') || ''
})

const isDomainTarget = computed(() => {
  if (!props.targetNode) return true
  if (props.targetNode.type === 'domain' || props.targetNode.isDomain) return true
  if (effectiveDomainId.value && props.targetNode.id === effectiveDomainId.value) return true
  return false
})

const formatTargetName = computed(() => {
  if (!props.targetNode) return ''
  const nameObj = props.targetNode.name
  if (typeof nameObj === 'object' && nameObj !== null) {
    return nameObj[locale.value] || nameObj.ko || nameObj.en || ''
  }
  return String(nameObj || props.targetNode.label || '')
})

const unplacedFields = computed(() => {
  const placedKeys = new Set(widgets.value.filter(w => w.fieldKey).map(w => w.fieldKey))
  return props.fields.filter((f: any) => !placedKeys.has(f.key))
})

const filteredUnplacedFields = computed(() => {
  if (!fieldSearchQuery.value) return unplacedFields.value
  const query = fieldSearchQuery.value.toLowerCase()
  return unplacedFields.value.filter((f: any) => {
    const nameStr = getFieldName(f).toLowerCase()
    const keyStr = (f.key || '').toLowerCase()
    return nameStr.includes(query) || keyStr.includes(query)
  })
})

const selectedWidget = computed(() => {
  return widgets.value.find(w => w.id === selectedWidgetId.value)
})

const selectedWidgetTitleKo = computed({
  get: () => {
    if (!selectedWidget.value) return ''
    if (typeof selectedWidget.value.title === 'object') {
      return selectedWidget.value.title.ko || ''
    }
    return selectedWidget.value.title || ''
  },
  set: (val) => {
    if (!selectedWidget.value) return
    if (!selectedWidget.value.title || typeof selectedWidget.value.title !== 'object') {
      selectedWidget.value.title = { ko: '', en: '' }
    }
    selectedWidget.value.title.ko = val
  }
})

const selectedWidgetTitleEn = computed({
  get: () => {
    if (!selectedWidget.value) return ''
    if (typeof selectedWidget.value.title === 'object') {
      return selectedWidget.value.title.en || ''
    }
    return selectedWidget.value.title || ''
  },
  set: (val) => {
    if (!selectedWidget.value) return
    if (!selectedWidget.value.title || typeof selectedWidget.value.title !== 'object') {
      selectedWidget.value.title = { ko: '', en: '' }
    }
    selectedWidget.value.title.en = val
  }
})

const selectedWidgetOptions = computed(() => {
  if (!selectedWidget.value) return {}
  if (!selectedWidget.value.options) {
    selectedWidget.value.options = {}
  }
  return selectedWidget.value.options
})

const canvasGridStyle = computed(() => ({
  display: 'grid',
  gridTemplateColumns: `repeat(${cols.value}, minmax(0, 1fr))`,
  gridAutoRows: `${rowHeight.value}px`,
  gap: '8px',
  minHeight: '650px',
  width: '100%',
  position: 'relative' as const,
  boxSizing: 'border-box' as const
}))

const getWidgetCanvasStyle = (widget: any) => ({
  gridColumn: `${widget.x + 1} / span ${widget.w}`,
  gridRow: `${widget.y + 1} / span ${widget.h}`
})

const getFieldDefinition = (fieldKey: string) => {
  if (!fieldKey) return undefined
  return props.fields.find((f: any) => f.key && f.key.toUpperCase() === fieldKey.toUpperCase())
}

const getFieldType = (widget: any) => {
  const field: any = getFieldDefinition(widget.fieldKey)
  if (field && field.type) return field.type
  return widget.type || 'TEXT'
}

const isImageField = (widget: any) => {
  const type = getFieldType(widget)
  const key = (widget.fieldKey || '').toLowerCase()
  return widget.type === 'IMAGE' || type === 'IMAGE' || key.includes('photo') || key.includes('image') || key.includes('avatar')
}

const isEditorField = (widget: any) => {
  const type = getFieldType(widget)
  const key = (widget.fieldKey || '').toLowerCase()
  return widget.type === 'EDITOR' || type === 'EDITOR' || type === 'HTML' || type === 'HTML_TEXT' || type === 'RICHTEXT' || key.includes('html') || key.includes('desc') || key.includes('content')
}

const isTableField = (widget: any) => {
  const type = getFieldType(widget)
  const field: any = getFieldDefinition(widget.fieldKey)
  return type === 'TABLE' || type === 'JSON' || field?.isTable || (field?.options && String(field.options).includes('tableSchema'))
}

const getFieldName = (field: any) => {
  if (!field || !field.name) return field?.key || ''
  if (typeof field.name === 'object') {
    return field.name[locale.value] || field.name.ko || field.name.en || field.key
  }
  return field.name
}

const widgetPaletteCategories = [
  {
    key: 'text',
    labelKey: 'cat_text_typography',
    icon: 'text_fields',
    items: [
      { type: 'TEXT_INPUT', nameKey: 'widget_type_text_input', icon: 'edit_note', defaultW: 4, defaultH: 1, desc: '4x1 · 단일행 텍스트' },
      { type: 'MULTILINGUAL_INPUT', nameKey: 'widget_type_multilingual_input', icon: 'translate', defaultW: 6, defaultH: 1, desc: '6x1 · 다국어 텍스트' },
      { type: 'TEXT_BANNER', nameKey: 'widget_type_text_banner', icon: 'title', defaultW: 6, defaultH: 1, desc: '6x1 · 강조 타이틀' },
      { type: 'TEXT_AREA', nameKey: 'widget_type_text_area', icon: 'notes', defaultW: 6, defaultH: 2, desc: '6x2 · 메모/설명' },
      { type: 'BADGE_TAG', nameKey: 'widget_type_badge_tag', icon: 'label', defaultW: 3, defaultH: 1, desc: '3x1 · 상태 뱃지' }
    ]
  },
  {
    key: 'number',
    labelKey: 'cat_number_metrics',
    icon: 'numbers',
    items: [
      { type: 'NUMBER_INPUT', nameKey: 'widget_type_number_input', icon: 'pin', defaultW: 4, defaultH: 1, desc: '4x1 · 숫자/금액' },
      { type: 'STAT_CARD', nameKey: 'widget_type_stat_card', icon: 'query_stats', defaultW: 3, defaultH: 2, desc: '3x2 · KPI 지표 카드' },
      { type: 'PROGRESS_BAR', nameKey: 'widget_type_progress_bar', icon: 'linear_scale', defaultW: 4, defaultH: 1, desc: '4x1 · 진행률 바' }
    ]
  },
  {
    key: 'select',
    labelKey: 'cat_select_control',
    icon: 'tune',
    items: [
      { type: 'SELECT_DROPDOWN', nameKey: 'widget_type_select_dropdown', icon: 'arrow_drop_down_circle', defaultW: 4, defaultH: 1, desc: '4x1 · 드롭다운' },
      { type: 'RADIO_SEGMENT', nameKey: 'widget_type_radio_segment', icon: 'view_agenda', defaultW: 6, defaultH: 1, desc: '6x1 · 세그먼트 버튼' },
      { type: 'MULTI_CHIP_SELECT', nameKey: 'widget_type_multi_chip_select', icon: 'style', defaultW: 6, defaultH: 2, desc: '6x2 · 태그 칩 선택' },
      { type: 'BOOLEAN_SWITCH', nameKey: 'widget_type_boolean_switch', icon: 'toggle_on', defaultW: 3, defaultH: 1, desc: '3x1 · 토글 스위치' },
      { type: 'BOOLEAN_CARD', nameKey: 'widget_type_boolean_card', icon: 'check_box', defaultW: 3, defaultH: 2, desc: '3x2 · 체크 카드' }
    ]
  },
  {
    key: 'date',
    labelKey: 'cat_date_time',
    icon: 'calendar_month',
    items: [
      { type: 'DATE_PICKER', nameKey: 'widget_type_date_picker', icon: 'calendar_today', defaultW: 4, defaultH: 1, desc: '4x1 · 날짜 선택' },
      { type: 'DATE_RANGE', nameKey: 'widget_type_date_range', icon: 'date_range', defaultW: 6, defaultH: 1, desc: '6x1 · 기간 선택' },
      { type: 'DATE_CALENDAR_CARD', nameKey: 'widget_type_date_calendar_card', icon: 'event', defaultW: 3, defaultH: 2, desc: '3x2 · 캘린더 카드' }
    ]
  },
  {
    key: 'media',
    labelKey: 'cat_media_table',
    icon: 'perm_media',
    items: [
      { type: 'IMAGE_BOX', nameKey: 'widget_type_image_box', icon: 'image', defaultW: 3, defaultH: 2, desc: '3x2 · 이미지 박스' },
      { type: 'FILE_ATTACHMENT', nameKey: 'widget_type_file_attachment', icon: 'attach_file', defaultW: 6, defaultH: 2, desc: '6x2 · 파일 첨부' },
      { type: 'DOMAIN_REF_CARD', nameKey: 'widget_type_domain_ref_card', icon: 'link', defaultW: 4, defaultH: 1, desc: '4x1 · 도메인 참조' },
      { type: 'JSON_SUBTABLE', nameKey: 'widget_type_json_subtable', icon: 'table_rows', defaultW: 12, defaultH: 3, desc: '12x3 · 서브 테이블' }
    ]
  },
  {
    key: 'layout',
    labelKey: 'cat_layout_special',
    icon: 'space_dashboard',
    items: [
      { type: 'SPECIALIZED_SUMMARY', nameKey: 'widget_type_specialized_summary', icon: 'account_box', defaultW: 12, defaultH: 3, desc: '12x3 · 도메인 헤더 요약' },
      { type: 'SECTION', nameKey: 'widget_type_section', icon: 'folder_open', defaultW: 12, defaultH: 1, desc: '12x1 · 섹션 카드' },
      { type: 'CALLOUT', nameKey: 'widget_type_callout', icon: 'info', defaultW: 12, defaultH: 2, desc: '12x2 · 안내 콜아웃' },
      { type: 'DIVIDER', nameKey: 'widget_type_divider', icon: 'horizontal_rule', defaultW: 12, defaultH: 1, desc: '12x1 · 구분선' }
    ]
  }
]

// 100% DB 공통코드(FIELD_TYPE) 표준 기반 위젯 호환 필드 매핑 정의
const WIDGET_COMPATIBLE_FIELD_TYPES: Record<string, string[]> = {
  // 1. 텍스트 & 타이포그래피
  TEXT_INPUT: ['TEXT', 'EMAIL'],
  MULTILINGUAL_INPUT: ['MULTILINGUAL'],
  TEXT_BANNER: ['TEXT'],
  FIELD: ['TEXT', 'EMAIL'],
  TEXT_AREA: ['TEXTAREA'],
  EDITOR: ['HTML_TEXT'],

  // 2. 숫자 & KPI 지표
  NUMBER_INPUT: ['NUMBER', 'CALCULATED'],
  STAT_CARD: ['NUMBER', 'CALCULATED'],
  PROGRESS_BAR: ['NUMBER', 'CALCULATED'],

  // 3. 선택 & 제어
  SELECT_DROPDOWN: ['SELECT'],
  RADIO_SEGMENT: ['SELECT'],
  MULTI_CHIP_SELECT: ['SELECT'],
  BADGE_TAG: ['SELECT', 'TEXT'],
  BOOLEAN_SWITCH: ['BOOLEAN'],
  BOOLEAN_CARD: ['BOOLEAN'],

  // 4. 날짜 & 시간
  DATE_PICKER: ['DATE', 'TIME'],
  DATE_RANGE: ['DATE_RANGE'],
  DATE_CALENDAR_CARD: ['DATE'],

  // 5. 미디어, 참조 & 서브테이블
  FILE_ATTACHMENT: ['FILE'],
  IMAGE_BOX: ['IMAGE'],
  DOMAIN_REF_CARD: ['DOMAIN_REFERENCE'],
  JSON_SUBTABLE: ['JSON']
}

const getCompatibleFieldTypes = (widgetType: string): string[] | null => {
  if (!widgetType) return null
  return WIDGET_COMPATIBLE_FIELD_TYPES[widgetType] || null
}

const fieldBindingSelectOptions = computed(() => {
  if (!props.fields || props.fields.length === 0) return []
  const wType = selectedWidget.value?.type
  const allowedTypes = getCompatibleFieldTypes(wType)
  const currentKey = selectedWidget.value?.fieldKey

  return props.fields
    .filter((f: any) => {
      if (currentKey && (f.key === currentKey || f.id === currentKey)) return true
      if (!allowedTypes) return true
      const fType = (f.type || 'TEXT').toUpperCase()
      return allowedTypes.includes(fType)
    })
    .map((f: any) => ({
      value: f.key,
      text: `${getFieldName(f)} (${f.key} · ${f.type || 'TEXT'})`
    }))
})

const onWidgetFieldBound = (widget: any, fieldKeyOrObj: any) => {
  if (!widget) return
  const rawKey = typeof fieldKeyOrObj === 'object' && fieldKeyOrObj !== null ? (fieldKeyOrObj.value || fieldKeyOrObj.key) : fieldKeyOrObj
  const fieldKey = rawKey ? String(rawKey) : null
  widget.fieldKey = fieldKey
  if (fieldKey) {
    const f = props.fields?.find((field: any) => field.key === fieldKey || field.id === fieldKey)
    if (f) {
      if (!widget.title || typeof widget.title !== 'object') {
        widget.title = { ko: '', en: '' }
      }
      if (typeof f.name === 'object') {
        widget.title.ko = f.name?.ko || f.name?.en || f.key
        widget.title.en = f.name?.en || f.name?.ko || f.key
      } else {
        widget.title.ko = String(f.name || f.key)
        widget.title.en = String(f.name || f.key)
      }
    }
  }
}

const getFieldIcon = (type: string) => {
  switch (type) {
    case 'IMAGE': return 'image'
    case 'FILE': return 'attach_file'
    case 'DATE': case 'TIME': return 'calendar_today'
    case 'DATE_RANGE': return 'date_range'
    case 'NUMBER': case 'CALCULATED': return 'pin'
    case 'SELECT': return 'arrow_drop_down_circle'
    case 'DOMAIN_REFERENCE': return 'link'
    case 'BOOLEAN': return 'toggle_on'
    case 'JSON': return 'table_rows'
    case 'TEXTAREA': return 'notes'
    case 'HTML_TEXT': return 'html'
    case 'MULTILINGUAL': return 'translate'
    case 'EMAIL': return 'mail'
    default: return 'text_fields'
  }
}

const getWidgetIcon = (type: string) => {
  switch (type) {
    case 'TEXT_INPUT': case 'FIELD': return 'edit_note'
    case 'MULTILINGUAL_INPUT': return 'translate'
    case 'TEXT_BANNER': return 'title'
    case 'TEXT_AREA': return 'notes'
    case 'EDITOR': return 'html'
    case 'BADGE_TAG': return 'label'
    case 'NUMBER_INPUT': return 'pin'
    case 'STAT_CARD': return 'query_stats'
    case 'PROGRESS_BAR': return 'linear_scale'
    case 'SELECT_DROPDOWN': return 'arrow_drop_down_circle'
    case 'RADIO_SEGMENT': return 'view_agenda'
    case 'MULTI_CHIP_SELECT': return 'style'
    case 'BOOLEAN_SWITCH': return 'toggle_on'
    case 'BOOLEAN_CARD': return 'check_box'
    case 'DATE_PICKER': return 'calendar_today'
    case 'DATE_RANGE': return 'date_range'
    case 'DATE_CALENDAR_CARD': return 'event'
    case 'IMAGE_BOX': return 'image'
    case 'FILE_ATTACHMENT': return 'attach_file'
    case 'DOMAIN_REF_CARD': return 'link'
    case 'JSON_SUBTABLE': return 'table_rows'
    case 'SECTION': return 'folder_open'
    case 'CALLOUT': return 'info'
    case 'DIVIDER': return 'horizontal_rule'
    case 'SPECIALIZED_SUMMARY': return 'account_box'
    default: return 'grid_view'
  }
}

const getWidgetDisplayName = (widget: any) => {
  if (widget.type === 'SPECIALIZED_SUMMARY') {
    return t('widget_type_specialized_summary') || '도메인 헤더 요약 위젯'
  }
  if (widget.title && typeof widget.title === 'object') {
    return widget.title[locale.value] || widget.title.ko || widget.title.en || widget.fieldKey || ''
  }
  if (widget.title && typeof widget.title === 'string') {
    return widget.title
  }
  if (widget.fieldKey) {
    const field: any = getFieldDefinition(widget.fieldKey)
    return getFieldName(field) || widget.fieldKey
  }
  return widget.type
}

// Add widgets
const addPredefinedFieldWidget = (field: any) => {
  const fType = (field.type || 'TEXT').toUpperCase()
  let widgetType = 'TEXT_INPUT'
  let w = 4
  let h = 1

  switch (fType) {
    case 'MULTILINGUAL':
      widgetType = 'MULTILINGUAL_INPUT'
      w = 6
      h = 1
      break
    case 'TEXTAREA':
      widgetType = 'TEXT_AREA'
      w = 6
      h = 2
      break
    case 'HTML_TEXT':
      widgetType = 'EDITOR'
      w = 12
      h = 3
      break
    case 'NUMBER':
    case 'CALCULATED':
      widgetType = 'NUMBER_INPUT'
      w = 4
      h = 1
      break
    case 'SELECT':
      widgetType = 'SELECT_DROPDOWN'
      w = 4
      h = 1
      break
    case 'BOOLEAN':
      widgetType = 'BOOLEAN_SWITCH'
      w = 3
      h = 1
      break
    case 'DATE':
    case 'TIME':
      widgetType = 'DATE_PICKER'
      w = 4
      h = 1
      break
    case 'DATE_RANGE':
      widgetType = 'DATE_RANGE'
      w = 6
      h = 1
      break
    case 'IMAGE':
      widgetType = 'IMAGE_BOX'
      w = 3
      h = 2
      break
    case 'FILE':
      widgetType = 'FILE_ATTACHMENT'
      w = 6
      h = 2
      break
    case 'DOMAIN_REFERENCE':
      widgetType = 'DOMAIN_REF_CARD'
      w = 4
      h = 1
      break
    case 'JSON':
      widgetType = 'JSON_SUBTABLE'
      w = cols.value
      h = 3
      break
    default:
      widgetType = 'TEXT_INPUT'
      w = 4
      h = 1
      break
  }

  const customCol = (field.colSpan || field.layoutWidth) && Number(field.colSpan || field.layoutWidth) <= cols.value ? Number(field.colSpan || field.layoutWidth) : null
  const targetW = orientationMode.value === 'portrait' ? Math.min(cols.value, 6) : (customCol || w)

  const nextPos = findNextAvailablePosition(targetW, h)

  const newWidget = {
    id: 'widget_' + Date.now() + '_' + Math.random().toString(36).substr(2, 4),
    type: widgetType,
    fieldKey: field.key,
    title: typeof field.name === 'object' ? { ...field.name } : { ko: getFieldName(field), en: field.key },
    x: nextPos.x,
    y: nextPos.y,
    w: targetW,
    h,
    options: {
      required: field.required || false,
      readOnly: field.isReadOnly || false,
      highlight: field.isHighlighted || false
    }
  }

  widgets.value.push(newWidget)
  selectedWidgetId.value = newWidget.id
  isInspectorOpen.value = true
  resolveWidgetCollisions(newWidget)
}

const addCustomWidget = (type: string, defaultW: number, defaultH: number, nameKey = '') => {
  const w = Math.min(cols.value, defaultW)
  const nextPos = findNextAvailablePosition(w, defaultH)
  const titleText = nameKey ? (t(nameKey) || type) : type
  const newWidget = {
    id: 'widget_' + Date.now() + '_' + Math.random().toString(36).substr(2, 4),
    type,
    fieldKey: null,
    title: { ko: titleText, en: type },
    x: nextPos.x,
    y: nextPos.y,
    w,
    h: defaultH,
    options: {}
  }

  widgets.value.push(newWidget)
  selectedWidgetId.value = newWidget.id
  isInspectorOpen.value = true
  resolveWidgetCollisions(newWidget)
}

const findNextAvailablePosition = (w: number, h: number) => {
  let maxY = 0
  for (const item of widgets.value) {
    const bottom = item.y + item.h
    if (bottom > maxY) {
      maxY = bottom
    }
  }
  return { x: 0, y: maxY }
}

const deleteWidget = (widgetId: string) => {
  widgets.value = widgets.value.filter(w => w.id !== widgetId)
  if (selectedWidgetId.value === widgetId) {
    selectedWidgetId.value = null
    isInspectorOpen.value = false
  }
}

// 2D Collision Push-down Algorithm Logic
const isOverlapping = (
  w1: { x: number; y: number; w: number; h: number },
  w2: { x: number; y: number; w: number; h: number }
) => {
  return !(
    w1.x + w1.w <= w2.x ||
    w2.x + w2.w <= w1.x ||
    w1.y + w1.h <= w2.y ||
    w2.y + w2.h <= w1.y
  )
}

const resolveWidgetCollisions = (targetWidget: any) => {
  if (!targetWidget) return
  const others = widgets.value.filter((w: any) => w.id !== targetWidget.id)
  let changed = true
  let iterations = 0
  const maxIterations = 50

  while (changed && iterations < maxIterations) {
    changed = false
    iterations++

    // 1. Check collisions with targetWidget
    for (const other of others) {
      if (isOverlapping(targetWidget, other)) {
        other.y = targetWidget.y + targetWidget.h
        changed = true
      }
    }

    // 2. Cascading check among other widgets (sorted by y ascending)
    others.sort((a: any, b: any) => a.y - b.y || a.x - b.x)
    for (let i = 0; i < others.length; i++) {
      for (let j = i + 1; j < others.length; j++) {
        if (isOverlapping(others[i], others[j])) {
          others[j].y = others[i].y + others[i].h
          changed = true
        }
      }
    }
  }
}

// 2D Mouse Drag Resizing
const startResize = (widget: any, event: MouseEvent, direction: 'e' | 's' | 'se' = 'se') => {
  event.preventDefault()
  event.stopPropagation()
  selectedWidgetId.value = widget.id
  resizingWidgetId.value = widget.id
  resizingW.value = widget.w
  resizingH.value = widget.h

  const startMouseX = event.clientX
  const startMouseY = event.clientY
  const startW = widget.w
  const startH = widget.h

  const containerEl = gridContainerRef.value || canvasAreaRef.value
  const gap = 8
  const containerWidth = containerEl ? (containerEl.clientWidth - 20) : (orientationMode.value === 'portrait' ? 480 : 960)
  const colStep = Math.max(30, (containerWidth - (cols.value - 1) * gap) / cols.value + gap)

  const onMouseMove = (moveEvent: MouseEvent) => {
    const deltaX = moveEvent.clientX - startMouseX
    const deltaY = moveEvent.clientY - startMouseY

    const colDelta = Math.round(deltaX / colStep)
    const rowDelta = Math.round(deltaY / (rowHeight.value + gap))

    let newW = startW
    let newH = startH

    if (direction === 'e' || direction === 'se') {
      newW = Math.max(1, Math.min(cols.value - widget.x, startW + colDelta))
    }
    if (direction === 's' || direction === 'se') {
      newH = Math.max(1, Math.min(30, startH + rowDelta))
    }

    widget.w = newW
    widget.h = newH
    resizingW.value = newW
    resizingH.value = newH
  }

  const onMouseUp = () => {
    resizingWidgetId.value = null
    window.removeEventListener('mousemove', onMouseMove)
    window.removeEventListener('mouseup', onMouseUp)
    resolveWidgetCollisions(widget)
  }

  window.addEventListener('mousemove', onMouseMove)
  window.addEventListener('mouseup', onMouseUp)
}

// Drag Move
const startDragMove = (widget: any, event: MouseEvent) => {
  if (resizingWidgetId.value) return
  selectedWidgetId.value = widget.id
  const startMouseX = event.clientX
  const startMouseY = event.clientY
  const startX = widget.x
  const startY = widget.y

  const containerEl = gridContainerRef.value || canvasAreaRef.value
  const gap = 8
  const containerWidth = containerEl ? (containerEl.clientWidth - 20) : (orientationMode.value === 'portrait' ? 480 : 960)
  const colStep = Math.max(30, (containerWidth - (cols.value - 1) * gap) / cols.value + gap)

  const onMouseMove = (moveEvent: MouseEvent) => {
    const deltaX = moveEvent.clientX - startMouseX
    const deltaY = moveEvent.clientY - startMouseY

    const colDelta = Math.round(deltaX / colStep)
    const rowDelta = Math.round(deltaY / (rowHeight.value + gap))

    const newX = Math.max(0, Math.min(cols.value - widget.w, startX + colDelta))
    const newY = Math.max(0, Math.min(50, startY + rowDelta))

    widget.x = newX
    widget.y = newY
  }

  const onMouseUp = () => {
    window.removeEventListener('mousemove', onMouseMove)
    window.removeEventListener('mouseup', onMouseUp)
    resolveWidgetCollisions(widget)
  }

  window.addEventListener('mousemove', onMouseMove)
  window.addEventListener('mouseup', onMouseUp)
}

// Auto generate
const autoGenerateLayout = () => {
  widgets.value = []
  let currentX = 0
  let currentY = 0

  props.fields.forEach((field: any) => {
    const isImage = field.type === 'IMAGE' || field.key.includes('photo') || field.key.includes('image')
    const isEditor = field.type === 'HTML' || field.type === 'RICHTEXT' || field.key.includes('desc') || field.key.includes('content')
    const isTable = field.type === 'TABLE' || field.type === 'JSON' || field.isTable || (field.options && String(field.options).includes('tableSchema'))

    const customCol = (field.colSpan || field.layoutWidth) && Number(field.colSpan || field.layoutWidth) <= cols.value ? Number(field.colSpan || field.layoutWidth) : null
    const defaultW = orientationMode.value === 'portrait' ? Math.min(cols.value, 6) : (customCol || 4)
    const w = isEditor || isTable ? cols.value : (isImage ? Math.min(cols.value, 3) : defaultW)
    const h = isEditor ? 8 : (isTable ? 5 : (isImage ? 4 : 1))
    const type = isImage ? 'IMAGE' : (isEditor ? 'EDITOR' : (isTable ? 'TABLE' : 'FIELD'))

    if (currentX + w > cols.value) {
      currentX = 0
      currentY += 1
    }

    widgets.value.push({
      id: 'widget_' + Date.now() + '_' + Math.random().toString(36).substr(2, 4),
      type,
      fieldKey: field.key,
      title: { ko: getFieldName(field), en: field.key },
      x: currentX,
      y: currentY,
      w,
      h,
      options: {
        required: field.required || false,
        readOnly: field.isReadOnly || false,
        highlight: field.isHighlighted || false
      }
    })

    currentX += w
  })
}

const clearLayout = () => {
  widgets.value = []
  selectedWidgetId.value = null
  isInspectorOpen.value = false
}

const syncCurrentLayoutToMemory = () => {
  if (!activeLayoutId.value) return
  const current = layouts.value.find(l => l.id === activeLayoutId.value)
  if (current) {
    current.cols = cols.value
    current.rowHeight = rowHeight.value
    current.orientation = orientationMode.value
    current.widgets = JSON.parse(JSON.stringify(widgets.value))
  }
}

const loadLayoutToCanvas = (targetLayout: any) => {
  if (!targetLayout) return
  orientationMode.value = targetLayout.orientation || (targetLayout.cols === 6 ? 'portrait' : 'landscape')
  cols.value = targetLayout.cols || (orientationMode.value === 'portrait' ? 6 : 12)
  rowHeight.value = targetLayout.rowHeight || 42
  const rawWidgets = JSON.parse(JSON.stringify(targetLayout.widgets || []))
  widgets.value = rawWidgets.map((w: any) => {
    let sanitizedW = w.w || (orientationMode.value === 'portrait' ? 6 : 4)
    if (sanitizedW > cols.value) {
      sanitizedW = cols.value
    }
    sanitizedW = Math.max(1, Math.min(cols.value, sanitizedW))
    const sanitizedX = Math.max(0, Math.min(cols.value - sanitizedW, w.x || 0))
    return {
      ...w,
      w: sanitizedW,
      x: sanitizedX,
      h: Math.max(1, w.h || 1),
      y: Math.max(0, w.y || 0)
    }
  })
  selectedWidgetId.value = null
  isInspectorOpen.value = false
}

const onLayoutChange = (newLayoutId: string) => {
  syncCurrentLayoutToMemory()
  activeLayoutId.value = newLayoutId
  const target = layouts.value.find(l => l.id === newLayoutId)
  if (target) {
    loadLayoutToCanvas(target)
  }
}

const openNewLayoutInline = () => {
  isRenaming.value = false
  layoutNameKoInput.value = `${t('layout_select_label')} ${layouts.value.length + 1}`
  layoutNameEnInput.value = `Layout ${layouts.value.length + 1}`
  showInlineLayoutInput.value = true
}

const openRenameLayoutInline = () => {
  isRenaming.value = true
  const current = currentActiveLayout.value
  layoutNameKoInput.value = (typeof current?.name === 'object' ? current.name.ko : current?.name) || ''
  layoutNameEnInput.value = (typeof current?.name === 'object' ? current.name.en : '') || ''
  showInlineLayoutInput.value = true
}

const cancelInlineLayout = () => {
  showInlineLayoutInput.value = false
}

const confirmInlineLayout = () => {
  const nameKo = layoutNameKoInput.value.trim()
  const nameEn = layoutNameEnInput.value.trim()
  if (!nameKo && !nameEn) return

  const nameObj = {
    ko: nameKo || nameEn,
    en: nameEn || nameKo
  }

  if (isRenaming.value) {
    const current = currentActiveLayout.value
    if (current) {
      current.name = nameObj
    }
  } else {
    syncCurrentLayoutToMemory()
    const newId = 'layout_' + Date.now()
    const newLayout = {
      id: newId,
      name: nameObj,
      isDefault: layouts.value.length === 0,
      cols: orientationMode.value === 'portrait' ? 6 : 12,
      rowHeight: 42,
      orientation: orientationMode.value,
      widgets: [],
      options: {}
    }
    layouts.value.push(newLayout)
    activeLayoutId.value = newId
    loadLayoutToCanvas(newLayout)
    autoGenerateLayout()
  }
  showInlineLayoutInput.value = false
}

const duplicateCurrentLayout = () => {
  syncCurrentLayoutToMemory()
  const current = currentActiveLayout.value
  if (!current) return
  const newId = 'layout_' + Date.now()
  const duplicated = JSON.parse(JSON.stringify(current))
  duplicated.id = newId
  if (typeof current.name === 'object' && current.name !== null) {
    duplicated.name = {
      ko: `${current.name.ko || ''} (${t('duplicate_layout')})`,
      en: `${current.name.en || current.name.ko || ''} (Copy)`
    }
  } else {
    duplicated.name = `${current.name} (${t('duplicate_layout')})`
  }
  duplicated.isDefault = false
  layouts.value.push(duplicated)
  activeLayoutId.value = newId
  loadLayoutToCanvas(duplicated)
  toast.init({ message: t('duplicate_layout'), color: 'success' })
}

const toggleDefaultLayout = () => {
  layouts.value.forEach(l => {
    l.isDefault = (l.id === activeLayoutId.value)
  })
  toast.init({ message: t('set_as_default_layout'), color: 'success' })
}

const deleteCurrentLayout = () => {
  if (layouts.value.length <= 1) return
  const idx = layouts.value.findIndex(l => l.id === activeLayoutId.value)
  if (idx === -1) return
  const wasDefault = layouts.value[idx].isDefault
  layouts.value.splice(idx, 1)
  if (wasDefault && layouts.value.length > 0) {
    layouts.value[0].isDefault = true
  }
  activeLayoutId.value = layouts.value[0].id
  loadLayoutToCanvas(layouts.value[0])
  toast.init({ message: t('delete_layout'), color: 'info' })
}

const getApiUrl = () => {
  const dId = effectiveDomainId.value
  if (!dId) return ''
  if (isDomainTarget.value || !props.targetNode?.id || props.targetNode.id === dId) {
    return `/api/domains/${dId}/layout`
  }
  return `/api/domains/${dId}/nodes/${props.targetNode.id}/layout`
}

const fetchLayout = async () => {
  const dId = effectiveDomainId.value
  if (!dId) return
  try {
    const url = getApiUrl()
    if (!url) return
    const res = await customFetch(url)
    if (res && res.layouts && Array.isArray(res.layouts) && res.layouts.length > 0) {
      layouts.value = res.layouts
      const def = res.layouts.find((l: any) => l.isDefault) || res.layouts[0]
      activeLayoutId.value = res.activeLayoutId || def.id
      const target = layouts.value.find(l => l.id === activeLayoutId.value) || def
      loadLayoutToCanvas(target)
    } else if (res && res.widgets && Array.isArray(res.widgets) && res.widgets.length > 0) {
      const defLayout = {
        id: 'layout_default',
        name: { ko: '기본 레이아웃', en: 'Default Layout' },
        isDefault: true,
        cols: res.cols || 12,
        rowHeight: 42,
        orientation: res.cols === 6 ? 'portrait' : 'landscape',
        widgets: res.widgets,
        options: res.options || {}
      }
      layouts.value = [defLayout]
      activeLayoutId.value = 'layout_default'
      loadLayoutToCanvas(defLayout)
    } else {
      const defLayout = {
        id: 'layout_default',
        name: { ko: '기본 레이아웃', en: 'Default Layout' },
        isDefault: true,
        cols: 12,
        rowHeight: 42,
        orientation: 'landscape',
        widgets: [],
        options: {}
      }
      layouts.value = [defLayout]
      activeLayoutId.value = 'layout_default'
      loadLayoutToCanvas(defLayout)
      autoGenerateLayout()
    }
  } catch (e) {
    const defLayout = {
      id: 'layout_default',
      name: { ko: '기본 레이아웃', en: 'Default Layout' },
      isDefault: true,
      cols: 12,
      rowHeight: 42,
      orientation: 'landscape',
      widgets: [],
      options: {}
    }
    layouts.value = [defLayout]
    activeLayoutId.value = 'layout_default'
    loadLayoutToCanvas(defLayout)
    autoGenerateLayout()
  }
}

const saveLayout = async () => {
  const dId = effectiveDomainId.value
  if (!dId) return
  syncCurrentLayoutToMemory()
  saving.value = true
  try {
    const url = getApiUrl()
    if (!url) return
    const payload = {
      activeLayoutId: activeLayoutId.value,
      layouts: layouts.value
    }

    await customFetch(url, {
      method: 'PUT',
      body: payload
    })

    toast.init({ message: t('layout_saved_success'), color: 'success' })
    emit('saved', payload)
    visible.value = false
  } catch (e: any) {
    console.error('Failed to save layout', e)
    toast.init({ message: t('layout_save_failed'), color: 'danger' })
  } finally {
    saving.value = false
  }
}

watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    if (typeof window !== 'undefined') {
      if (window.innerWidth < 1024) {
        isPaletteCollapsed.value = true
      }
      if (window.innerWidth < 768 && orientationMode.value === 'landscape') {
        setOrientation('portrait')
      }
    }
    fetchLayout()
  }
}, { immediate: true })
</script>

<style scoped>
/* Unified Single Header Bar (Height: 46px, FIXED on screen top) */
.builder-unified-header {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100vw !important;
  height: 46px;
  min-height: 46px;
  max-height: 46px;
  flex-shrink: 0;
  padding: 0 0.5rem;
  padding-top: env(safe-area-inset-top, 0px);
  background: var(--va-background-element, #141b2d);
  border-bottom: 2px solid var(--va-primary, #154ec1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
  box-sizing: border-box;
  overflow-x: auto;
  overflow-y: hidden;
  gap: 6px;
  z-index: 999999 !important;
}

.header-left-section {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  flex: 1 1 auto;
  overflow-x: auto;
  overflow-y: hidden;
}

.header-right-section {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 0 0 auto;
  margin-left: auto;
}

.btn-close-modal {
  flex-shrink: 0;
}

.header-title-wrapper {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.header-logo-icon {
  flex-shrink: 0;
}

.builder-title-text {
  font-size: 0.88rem;
  font-weight: 800;
  color: var(--va-text-primary, #f8fafc);
  white-space: nowrap;
}

.target-scope-badge {
  font-size: 0.70rem;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 10px;
  white-space: nowrap;
  flex-shrink: 0;
}

.badge-domain {
  background: rgba(21, 78, 193, 0.25);
  color: #38bdf8;
  border: 1px solid rgba(56, 189, 248, 0.4);
}

.badge-node {
  background: rgba(16, 185, 129, 0.25);
  color: #34d399;
  border: 1px solid rgba(52, 211, 153, 0.4);
}

.toolbar-divider-v {
  width: 1px;
  height: 22px;
  background: var(--va-background-border, #334155);
  flex-shrink: 0;
  margin: 0 2px;
}

.preset-selector-group {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.preset-select-control {
  width: 170px;
  min-width: 120px;
  flex-shrink: 0;
}

.compact-tool-btn {
  flex-shrink: 0;
}

.inline-name-edit-box {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.inline-name-input {
  width: 130px;
  flex-shrink: 0;
}

.lang-tag {
  font-size: 0.70rem;
  font-weight: 700;
  color: var(--va-text-secondary);
  margin-right: 4px;
}

.viewport-mode-switcher {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 6px;
  padding: 2px;
  border: 1px solid var(--va-background-border, #334155);
  flex-shrink: 0;
}

.mode-switch-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 6px;
  border: none;
  background: transparent;
  border-radius: 4px;
  color: var(--va-text-secondary, #94a3b8);
  cursor: pointer;
  transition: all 0.2s;
}

.mode-switch-btn:hover {
  color: var(--va-text-primary, #ffffff);
}

.mode-switch-btn.active {
  background: var(--va-primary, #154ec1);
  color: #ffffff;
}

.toolbar-action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 6px;
  border: 1px solid var(--va-background-border, #334155);
  background: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
  color: var(--va-text-secondary, #94a3b8);
  cursor: pointer;
  transition: all 0.2s;
}

.toolbar-action-btn:hover {
  color: var(--va-text-primary, #ffffff);
  border-color: var(--va-primary, #154ec1);
}

.action-btn-save {
  flex-shrink: 0;
}

.save-btn-text {
  font-weight: 700;
  white-space: nowrap;
}

/* Builder Main Body (Palette + Canvas Workspace + Slide-over Inspector) */
.builder-body-wrapper {
  display: flex;
  position: relative;
  width: 100%;
  flex: 1 1 0% !important;
  min-height: 0 !important;
  padding-top: 46px !important;
  height: 100% !important;
  overflow: hidden !important;
  background: var(--va-background-secondary, #0f172a);
  box-sizing: border-box;
}

/* 1. Palette Sidebar (Collapsible) */
.palette-sidebar {
  width: 260px;
  min-width: 260px;
  max-width: 260px;
  flex: 0 0 260px;
  background: var(--va-background-primary, #1e2640);
  border-right: 1px solid var(--va-background-border, #334155);
  display: flex;
  flex-direction: column;
  height: 100%;
  transition: width 0.25s ease, min-width 0.25s ease, max-width 0.25s ease, flex 0.25s ease;
  overflow: hidden;
  z-index: 10;
}

.palette-sidebar.is-collapsed {
  width: 36px;
  min-width: 36px;
  max-width: 36px;
  flex: 0 0 36px;
  cursor: pointer;
  background: var(--va-background-secondary, #141b2d);
}

.palette-inner-container {
  display: flex;
  flex-direction: column;
  width: 260px;
  height: 100%;
}

.palette-collapsed-strip {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding-top: 1rem;
  height: 100%;
  gap: 12px;
}

.collapsed-vertical-text {
  writing-mode: vertical-lr;
  font-size: 0.78rem;
  font-weight: 700;
  color: var(--va-text-secondary, #94a3b8);
  letter-spacing: 2px;
}

.palette-tabs-header {
  display: flex;
  align-items: center;
  border-bottom: 1px solid var(--va-background-border, #334155);
  padding: 4px;
  gap: 2px;
}

.palette-close-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  color: var(--va-text-secondary, #94a3b8);
  border-radius: 4px;
  cursor: pointer;
  margin-left: 2px;
  transition: all 0.2s;
  flex-shrink: 0;
}

.palette-close-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--va-text-primary, #ffffff);
}

.palette-tab-btn {
  flex: 1;
  border: none;
  background: transparent;
  padding: 6px 4px;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--va-text-secondary, #94a3b8);
  transition: all 0.2s;
}

.palette-tab-btn.active {
  background: rgba(21, 78, 193, 0.15);
  color: var(--va-primary, #154ec1);
}

.palette-content-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 0.65rem;
}

.palette-item {
  display: flex;
  align-items: center;
  padding: 6px 8px;
  border: 1px solid var(--va-background-border, #334155);
  border-radius: 6px;
  margin-bottom: 5px;
  background: var(--va-background-element, var(--va-background-primary, #1e2640));
  cursor: pointer;
  transition: all 0.2s;
}

.palette-item:hover {
  border-color: var(--va-primary, #154ec1);
  background: rgba(21, 78, 193, 0.1);
  transform: translateY(-1px);
}

.palette-item-icon {
  margin-right: 6px;
  display: flex;
  align-items: center;
}

.palette-item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.palette-item-name {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--va-text-primary, #f8fafc);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.35;
  padding: 1px 0;
}

.palette-item-key, .palette-item-desc {
  font-size: 0.72rem;
  color: var(--va-text-secondary, #94a3b8);
  line-height: 1.25;
}

.empty-palette-notice {
  text-align: center;
  padding: 2rem 1rem;
  color: var(--va-text-secondary, #94a3b8);
  font-size: 0.82rem;
}

/* 2. Expansive Canvas Area */
.canvas-workspace-area {
  flex: 1 1 0%;
  min-width: 0;
  height: 100%;
  overflow-y: auto;
  overflow-x: auto;
  padding: 1rem;
  box-sizing: border-box;
  background-color: var(--va-background-secondary, #0f172a);
  background-image: radial-gradient(var(--va-background-border, #334155) 1.5px, transparent 1.5px);
  background-size: 24px 24px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.canvas-workspace-area.is-portrait {
  padding: 1rem 0.75rem;
  background-color: #0b1120;
}

.grid-canvas-container {
  background: var(--va-background-primary, #1e2640);
  border: 2px dashed var(--va-background-border, #475569);
  border-radius: 8px;
  padding: 10px;
  width: 100%;
  min-width: 0;
  max-width: 100%;
  box-sizing: border-box;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.25);
  transition: all 0.3s ease;
}

/* Portrait Device Viewport Frame */
.grid-canvas-container.is-portrait {
  max-width: 480px;
  min-width: 320px;
  margin: 0 auto;
  border: 3px solid var(--va-primary, #3b82f6);
  border-radius: 16px;
  background: #141d33;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.45);
}

.canvas-widget-box {
  background: var(--va-background-element, var(--va-background-primary, #1e2640));
  border: 1px solid var(--va-background-border, #334155);
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  position: relative;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  transition: border-color 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
  overflow: hidden;
}

.canvas-widget-box.is-selected {
  border-color: var(--va-primary, #154ec1);
  box-shadow: 0 0 0 2px rgba(21, 78, 193, 0.5);
}

/* Single-Row Compact Mode (h === 1) */
.canvas-widget-box.is-single-row {
  justify-content: center;
}

.widget-single-row-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 100%;
  padding: 0 8px;
  gap: 8px;
  cursor: grab;
  user-select: none;
  box-sizing: border-box;
}

.widget-single-row-content:active {
  cursor: grabbing;
}

.single-row-left {
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
  flex-shrink: 0;
}

.single-row-label {
  font-size: 0.78rem;
  font-weight: 700;
  color: var(--va-text-secondary, #94a3b8);
}

.single-row-center {
  flex: 1;
  display: flex;
  align-items: center;
  overflow: hidden;
  min-width: 0;
}

.single-row-right {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.single-row-val-box {
  display: flex;
  align-items: center;
  background: var(--va-background-secondary, #141b2d);
  padding: 2px 8px;
  border-radius: 4px;
  border: 1px solid var(--va-background-border, #334155);
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.single-row-bool, .single-row-file, .single-row-section {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.75rem;
}

.single-row-multi {
  display: flex;
  align-items: center;
  gap: 4px;
  overflow: hidden;
}

.sample-val-text {
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--va-text-primary, #f8fafc);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sample-chip-tag {
  font-size: 0.65rem;
  font-weight: 800;
  background: var(--va-background-border, #334155);
  color: var(--va-text-secondary, #94a3b8);
  padding: 1px 4px;
  border-radius: 3px;
}

.canvas-divider-hr {
  border: none;
  border-top: 1px dashed var(--va-background-border, #334155);
  margin: 0;
  width: 100%;
}

.widget-box-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 3px 8px;
  background: var(--va-background-secondary, #141b2d);
  border-bottom: 1px solid var(--va-background-border, #334155);
  border-top-left-radius: 6px;
  border-top-right-radius: 6px;
  cursor: grab;
  user-select: none;
  flex-shrink: 0;
}

.widget-box-header:active {
  cursor: grabbing;
}

.widget-box-title {
  font-size: 0.78rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  color: var(--va-text-primary, #f8fafc);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.widget-box-tools {
  display: flex;
  align-items: center;
  gap: 5px;
}

.widget-dimension-tag {
  font-size: 0.68rem;
  background: var(--va-background-border, #334155);
  padding: 1px 4px;
  border-radius: 4px;
  color: var(--va-text-secondary, #94a3b8);
  font-weight: 600;
}

.widget-tool-btn, .widget-delete-btn {
  cursor: pointer;
  color: var(--va-text-secondary, #94a3b8);
  transition: color 0.2s, transform 0.15s;
}

.widget-tool-btn:hover {
  color: var(--va-primary, #38bdf8);
  transform: scale(1.15);
}

.widget-delete-btn:hover {
  color: #ef4444;
  transform: scale(1.15);
}

.widget-box-body {
  flex: 1;
  padding: 4px 8px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  overflow: hidden;
  min-height: 0;
}

/* Mock Previews */
.inner-image-preview {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--va-background-secondary, #141b2d);
  border: 1px dashed var(--va-background-border, #334155);
  border-radius: 4px;
  overflow: hidden;
  height: 100%;
}

.mock-image-container {
  width: 100%;
  height: 100%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mock-vector-svg {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.mock-image-badge {
  position: absolute;
  bottom: 6px;
  left: 6px;
  background: rgba(0, 0, 0, 0.6);
  color: #ffffff;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 4px;
}

.inner-editor-preview {
  flex: 1;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--va-background-border, #334155);
  border-radius: 4px;
  overflow: hidden;
  height: 100%;
}

.editor-dummy-toolbar {
  display: flex;
  gap: 8px;
  padding: 3px 6px;
  background: var(--va-background-secondary, #141b2d);
  border-bottom: 1px solid var(--va-background-border, #334155);
  font-size: 0.68rem;
  font-weight: 700;
  color: var(--va-text-secondary, #94a3b8);
  flex-shrink: 0;
}

.editor-dummy-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 8px;
  background: var(--va-background-element, var(--va-background-primary, #1e2640));
  overflow-y: auto;
}

.mock-html-title {
  font-size: 0.82rem;
  font-weight: 800;
  color: var(--va-primary, #154ec1);
  margin: 0 0 4px 0;
}

.mock-html-p {
  font-size: 0.74rem;
  color: var(--va-text-primary, #f8fafc);
  margin: 0 0 6px 0;
  line-height: 1.4;
}

.mock-html-ul {
  margin: 0;
  padding-left: 1rem;
  font-size: 0.72rem;
  color: var(--va-text-secondary, #94a3b8);
}

.mock-field-card, .mock-ref-card, .mock-bool-card, .mock-calc-card {
  display: flex;
  align-items: center;
  gap: 6px;
  background: var(--va-background-secondary, #141b2d);
  padding: 6px 8px;
  border-radius: 4px;
  border: 1px solid var(--va-background-border, #334155);
  width: 100%;
}

.mock-field-val {
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--va-text-primary, #f8fafc);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mock-currency-badge, .mock-fx-badge {
  font-size: 0.72rem;
  font-weight: 800;
  background: var(--va-primary, #154ec1);
  color: #ffffff;
  padding: 1px 5px;
  border-radius: 3px;
}

.mock-multilingual-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  width: 100%;
}

.mock-lang-row {
  display: flex;
  align-items: center;
  gap: 6px;
  background: var(--va-background-secondary, #141b2d);
  padding: 3px 6px;
  border-radius: 4px;
  border: 1px solid var(--va-background-border, #334155);
}

.mock-file-card {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--va-background-secondary, #141b2d);
  padding: 6px 8px;
  border-radius: 4px;
  border: 1px solid var(--va-background-border, #334155);
  width: 100%;
}

.mock-file-info {
  display: flex;
  flex-direction: column;
}

.mock-file-name {
  font-size: 0.78rem;
  font-weight: 700;
  color: var(--va-text-primary, #f8fafc);
}

.mock-file-size {
  font-size: 0.68rem;
  color: var(--va-text-secondary, #94a3b8);
}

.inner-section-preview, .inner-callout-preview, .inner-divider-preview, .inner-field-preview {
  flex: 1;
  display: flex;
  align-items: center;
  width: 100%;
}

.canvas-subtable-preview {
  width: 100%;
  border: 1px solid var(--va-background-border, #334155);
  border-radius: 4px;
  overflow: hidden;
  background: var(--va-background-element, var(--va-background-primary, #1e2640));
}

.subtable-mini-bar {
  background: var(--va-background-secondary, #141b2d);
  padding: 3px 6px;
  border-bottom: 1px solid var(--va-background-border, #334155);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.subtable-mini-label {
  font-size: 0.72rem;
  font-weight: 700;
  color: var(--va-text-primary, #f8fafc);
}

.subtable-mini-table {
  width: 100%;
  font-size: 0.68rem;
  border-collapse: collapse;
}

.subtable-mini-table th {
  background: var(--va-background-secondary, #141b2d);
  padding: 3px 6px;
  text-align: left;
  border-bottom: 1px solid var(--va-background-border, #334155);
  color: var(--va-text-secondary, #94a3b8);
  font-weight: 600;
}

.subtable-mini-table td {
  padding: 3px 6px;
  border-bottom: 1px solid var(--va-background-border, #334155);
  color: var(--va-text-primary, #f8fafc);
}

/* Edge & Corner Resize Handles */
.widget-resize-handle-e {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 8px;
  cursor: ew-resize;
  z-index: 15;
  transition: background-color 0.15s, opacity 0.15s;
}

.widget-resize-handle-e:hover {
  background: var(--va-primary, #154ec1);
  opacity: 0.7;
}

.widget-resize-handle-s {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 8px;
  cursor: ns-resize;
  z-index: 15;
  transition: background-color 0.15s, opacity 0.15s;
}

.widget-resize-handle-s:hover {
  background: var(--va-primary, #154ec1);
  opacity: 0.7;
}

.widget-resize-handle-se {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 14px;
  height: 14px;
  cursor: nwse-resize;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--va-text-secondary, #94a3b8);
  z-index: 20;
}

.widget-resize-handle-se:hover {
  color: var(--va-primary, #154ec1);
}

.resizing-badge-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(21, 78, 193, 0.9);
  color: #ffffff;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 800;
  pointer-events: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
}

/* 3. Slide-over Inspector Drawer */
.inspector-slide-drawer {
  position: absolute;
  top: 46px;
  right: 0;
  width: 340px;
  max-width: 85vw;
  height: calc(100% - 46px);
  background: var(--va-background-primary, #1e2640);
  border-left: 1px solid var(--va-background-border, #334155);
  box-shadow: -4px 0 20px rgba(0, 0, 0, 0.4);
  transform: translateX(100%);
  transition: transform 0.28s cubic-bezier(0.16, 1, 0.3, 1);
  display: flex;
  flex-direction: column;
  z-index: 50;
  box-sizing: border-box;
}

.inspector-slide-drawer.is-open {
  transform: translateX(0);
}

.drawer-header {
  padding: 0.65rem 0.85rem;
  border-bottom: 1px solid var(--va-background-border, #334155);
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--va-background-secondary, #141b2d);
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 10;
}

.drawer-header-left {
  display: flex;
  align-items: center;
  min-width: 0;
}

.drawer-heading {
  margin: 0;
  font-size: 0.88rem;
  font-weight: 700;
  color: var(--va-text-primary, #f8fafc);
  white-space: nowrap;
}

.drawer-subheading {
  display: block;
  font-size: 0.7rem;
  color: var(--va-text-secondary, #94a3b8);
  margin-top: 1px;
}

.drawer-header-right {
  display: flex;
  align-items: center;
  gap: 4px;
}

.btn-header-done {
  font-weight: 700;
}

.drawer-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 0.85rem;
  box-sizing: border-box;
}

.drawer-footer {
  padding: 0.65rem 0.85rem;
  border-top: 1px solid var(--va-background-border, #334155);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
  background: var(--va-background-secondary, #141b2d);
  flex-shrink: 0;
  position: sticky;
  bottom: 0;
  z-index: 10;
}

.btn-footer-apply {
  font-weight: 700;
}

/* Floating Palette Expand Button */
.floating-palette-btn {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 25;
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 20px;
  background: var(--va-primary, #154ec1);
  color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.4);
  font-size: 0.78rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.floating-palette-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 18px rgba(21, 78, 193, 0.5);
}

/* Inspector Backdrop Overlay */
.inspector-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.35);
  z-index: 45;
  cursor: pointer;
}

.inspector-single-col-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.inspector-field-item {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.inspector-section {
  margin-bottom: 1.1rem;
  padding-bottom: 0.9rem;
  border-bottom: 1px solid var(--va-background-border, #334155);
}

.inspector-section-title {
  font-size: 0.78rem;
  font-weight: 700;
  color: var(--va-text-secondary, #94a3b8);
  display: block;
  margin-bottom: 0.5rem;
  text-transform: uppercase;
}

.inspector-label {
  font-size: 0.75rem;
  color: var(--va-text-secondary, #94a3b8);
  display: block;
  margin-bottom: 3px;
}

.empty-inspector-prompt {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--va-text-secondary, #94a3b8);
  font-size: 0.85rem;
  text-align: center;
  padding: 1rem;
  gap: 0.5rem;
}

/* Specialized Summary & Callout Preview & Inspector styles */
.specialized-preview-card {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.75rem;
  background: var(--va-background-element, #1e2640);
  border-radius: 8px;
  border: 1px solid var(--va-background-border, #334155);
  width: 100%;
  box-sizing: border-box;
}

.specialized-avatar-badge {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(44, 130, 224, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
}

.specialized-main-wrapper {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.specialized-top-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
  width: 100%;
}

.specialized-title-group {
  flex: 1;
  min-width: 0;
}

.specialized-title-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.specialized-title-name {
  color: var(--va-text-primary, #f8fafc);
  font-size: 0.95rem;
}

.specialized-domain-tag {
  font-size: 0.75rem;
  color: var(--va-text-secondary, #94a3b8);
  margin-top: 2px;
}

.specialized-subfields-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
  padding-top: 6px;
  border-top: 1px dashed var(--va-background-border, #334155);
}

.summary-field-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 0.72rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--va-background-border, #334155);
  border-radius: 4px;
  padding: 2px 6px;
}

.summary-field-chip .chip-label {
  color: var(--va-text-secondary, #94a3b8);
}

.summary-field-chip .chip-val {
  color: var(--va-primary, #2c82e0);
  font-weight: 600;
}

.specialized-audit-meta {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  margin-left: auto;
  font-size: 0.72rem;
  line-height: 1.4;
}

.specialized-audit-meta .audit-meta-grid {
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.specialized-audit-meta .audit-meta-col {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  text-align: right;
}

.specialized-audit-meta .audit-col-divider {
  width: 1px;
  height: 22px;
  background: var(--va-background-border, rgba(0, 0, 0, 0.08));
}

.specialized-audit-meta .audit-user-line {
  display: flex;
  align-items: center;
  gap: 3px;
  white-space: nowrap;
}

.specialized-audit-meta .audit-meta-label {
  font-weight: 500;
  color: var(--va-text-secondary, #64748b);
  font-size: 0.7rem;
}

.specialized-audit-meta .audit-meta-value {
  font-weight: 600;
  color: var(--va-text-primary, #334155);
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.specialized-audit-meta .audit-date-line {
  font-size: 0.68rem;
  color: var(--va-text-secondary, #94a3b8);
  font-variant-numeric: tabular-nums;
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

.specialized-audit-meta .version-tag {
  display: inline-block;
  background: rgba(0, 0, 0, 0.05);
  color: var(--va-text-secondary, #475569);
  border: 1px solid var(--va-background-border, rgba(0, 0, 0, 0.08));
  font-size: 0.6rem;
  font-weight: 700;
  padding: 0px 3px;
  border-radius: 3px;
}

/* Callout Themes */
.inner-callout-preview {
  display: flex;
  align-items: center;
  padding: 0.75rem 1rem;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 500;
}

.callout-theme-warning {
  background: rgba(245, 158, 11, 0.12);
  border: 1px solid rgba(245, 158, 11, 0.4);
  color: #f59e0b;
}

.callout-theme-info {
  background: rgba(59, 130, 246, 0.12);
  border: 1px solid rgba(59, 130, 246, 0.4);
  color: #3b82f6;
}

.callout-theme-success {
  background: rgba(16, 185, 129, 0.12);
  border: 1px solid rgba(16, 185, 129, 0.4);
  color: #10b981;
}

.callout-theme-danger {
  background: rgba(239, 68, 68, 0.12);
  border: 1px solid rgba(239, 68, 68, 0.4);
  color: #ef4444;
}

/* Callout Type Switcher Buttons */
.callout-type-switcher {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 4px;
}

.callout-type-btn {
  padding: 4px 0;
  font-size: 0.7rem;
  font-weight: 700;
  border-radius: 4px;
  border: 1px solid var(--va-background-border, #334155);
  background: transparent;
  color: var(--va-text-secondary, #94a3b8);
  cursor: pointer;
  transition: all 0.2s ease;
}

.callout-type-btn.active.btn-info {
  background: #3b82f6;
  color: #fff;
  border-color: #3b82f6;
}

.callout-type-btn.active.btn-warning {
  background: #f59e0b;
  color: #000;
  border-color: #f59e0b;
}

.callout-type-btn.active.btn-success {
  background: #10b981;
  color: #fff;
  border-color: #10b981;
}

.callout-type-btn.active.btn-danger {
  background: #ef4444;
  color: #fff;
  border-color: #ef4444;
}

/* Summary Field Selector in Inspector */
.inspector-helper-desc {
  font-size: 0.72rem;
  color: var(--va-text-secondary, #94a3b8);
  display: block;
  margin-bottom: 8px;
}

.summary-field-selector-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-height: 220px;
  overflow-y: auto;
  padding: 4px;
  background: rgba(0, 0, 0, 0.15);
  border-radius: 6px;
  border: 1px solid var(--va-background-border, #334155);
}

.summary-field-option-item {
  display: flex;
  align-items: center;
  padding: 4px 6px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.15s ease;
}

.summary-field-option-item:hover {
  background: rgba(255, 255, 255, 0.05);
}

.summary-field-option-item.is-selected {
  background: rgba(44, 130, 224, 0.15);
}

.summary-option-name {
  font-size: 0.78rem;
  font-weight: 500;
  color: var(--va-text-primary, #f8fafc);
  margin-right: 6px;
}

.summary-option-key {
  font-size: 0.68rem;
  color: var(--va-text-secondary, #94a3b8);
  margin-left: auto;
}

/* Categorized Palette Styles */
.palette-category-group {
  margin-bottom: 0.85rem;
}

.palette-category-header {
  display: flex;
  align-items: center;
  font-size: 0.72rem;
  font-weight: 700;
  color: var(--va-text-secondary, #94a3b8);
  padding: 3px 6px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px dashed var(--va-background-border, rgba(255, 255, 255, 0.1));
  margin-bottom: 4px;
}

.palette-category-items {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.unbound-hint-text {
  display: flex;
  align-items: center;
  font-size: 0.7rem;
  color: var(--va-text-secondary, #94a3b8);
  margin-top: 4px;
}

/* Rich Widget Multi-row Previews */
.mock-stat-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0.5rem 0.75rem;
  height: 100%;
  box-sizing: border-box;
}

.stat-card-top-label {
  font-size: 0.72rem;
  font-weight: 600;
  color: var(--va-text-secondary, #94a3b8);
  text-transform: uppercase;
}

.stat-card-main-val {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin: 2px 0;
}

.stat-card-main-val .stat-num {
  font-size: 1.6rem;
  font-weight: 800;
  color: var(--va-primary, #2c82e0);
  line-height: 1.1;
}

.stat-card-main-val .stat-unit {
  font-size: 0.8rem;
  color: var(--va-text-secondary, #94a3b8);
  font-weight: 500;
}

.stat-card-trend {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 0.7rem;
  font-weight: 600;
}

.mock-calendar-card {
  display: flex;
  flex-direction: column;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid var(--va-background-border, rgba(255, 255, 255, 0.1));
  background: var(--va-background-secondary, rgba(0, 0, 0, 0.2));
  text-align: center;
  height: 100%;
}

.calendar-card-header {
  background: var(--va-primary, #2c82e0);
  color: #fff;
  font-size: 0.68rem;
  font-weight: 700;
  padding: 2px 0;
}

.calendar-card-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: 4px;
}

.calendar-day-num {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--va-text-primary, #f8fafc);
  line-height: 1;
}

.calendar-day-text {
  font-size: 0.65rem;
  color: var(--va-text-secondary, #94a3b8);
  margin-top: 2px;
}

.mock-banner-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0.5rem 0.75rem;
  height: 100%;
  background: linear-gradient(135deg, rgba(44, 130, 224, 0.12), rgba(16, 185, 129, 0.08));
  border-radius: 6px;
  border-left: 3px solid var(--va-primary, #2c82e0);
}

.banner-title {
  font-size: 1.15rem;
  font-weight: 800;
  color: var(--va-text-primary, #f8fafc);
}

.banner-sub {
  font-size: 0.7rem;
  color: var(--va-text-secondary, #94a3b8);
}

.mock-textarea-card {
  display: flex;
  flex-direction: column;
  gap: 3px;
  padding: 0.4rem 0.6rem;
  font-size: 0.75rem;
  line-height: 1.4;
}

.mock-textarea-line {
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.mock-chips-card {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  padding: 0.4rem 0.6rem;
}

.mock-bool-card-large {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0.5rem 0.75rem;
  height: 100%;
}

.bool-card-text {
  display: flex;
  flex-direction: column;
}

.bool-card-val {
  font-size: 0.95rem;
}

.bool-card-label {
  font-size: 0.7rem;
  color: var(--va-text-secondary, #94a3b8);
}

.mock-progress-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0.4rem 0.75rem;
  height: 100%;
  gap: 4px;
}

.progress-bar-labels {
  display: flex;
  justify-content: space-between;
  font-size: 0.72rem;
}

.progress-track {
  width: 100%;
  height: 8px;
  background: var(--va-background-border, rgba(255, 255, 255, 0.15));
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #2c82e0, #10b981);
  border-radius: 4px;
}

.mock-segment-card {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0.4rem 0.6rem;
}

.segment-btn {
  padding: 4px 10px;
  border-radius: 4px;
  border: 1px solid var(--va-background-border, rgba(255, 255, 255, 0.2));
  background: transparent;
  color: var(--va-text-secondary, #94a3b8);
  font-size: 0.72rem;
  cursor: pointer;
}

.segment-btn.active {
  background: var(--va-primary, #2c82e0);
  color: #fff;
  border-color: var(--va-primary, #2c82e0);
  font-weight: 700;
}

/* Single row compact widgets */
.single-row-banner .banner-highlight-text {
  font-size: 0.95rem;
  font-weight: 800;
  color: var(--va-primary, #2c82e0);
}

.single-row-progress {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
}

.mini-progress-track {
  flex: 1;
  height: 6px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 3px;
  overflow: hidden;
}

.mini-progress-fill {
  height: 100%;
  background: #2c82e0;
  border-radius: 3px;
}

.mini-progress-text {
  font-size: 0.68rem;
  color: var(--va-text-secondary, #94a3b8);
}

.single-row-segment {
  display: flex;
  align-items: center;
  gap: 3px;
}

.mini-segment-btn {
  font-size: 0.68rem;
  padding: 1px 5px;
  border-radius: 3px;
  border: 1px solid var(--va-background-border, rgba(255, 255, 255, 0.15));
  color: var(--va-text-secondary, #94a3b8);
}

.mini-segment-btn.active {
  background: var(--va-primary, #2c82e0);
  color: #fff;
  font-weight: 700;
}

/* ========================================================================= */
/* Responsive Media Queries for Low Resolution & Small Screens */
/* ========================================================================= */
@media (max-width: 1200px) {
  .target-scope-badge {
    display: none;
  }
}

@media (max-width: 992px) {
  .header-logo-icon {
    display: none;
  }
  .builder-unified-header {
    padding: 0 0.4rem;
    height: 44px;
    min-height: 44px;
  }
  .palette-sidebar {
    width: 220px;
    min-width: 220px;
    max-width: 220px;
    flex: 0 0 220px;
  }
  .palette-inner-container {
    width: 220px;
  }
}

@media (max-width: 768px) {
  .builder-unified-header {
    padding: 0 0.25rem;
    height: 40px;
    min-height: 40px;
    max-height: 40px;
    gap: 3px;
  }
  .header-title-wrapper {
    display: none;
  }
  .toolbar-divider-v {
    display: none;
  }
  .preset-select-control {
    width: 120px;
    min-width: 90px;
    max-width: 150px;
  }
  .compact-tool-btn {
    min-width: 26px;
    padding: 0 3px !important;
  }
  .save-btn-text {
    display: none;
  }
  .canvas-workspace-area {
    padding: 0.35rem;
  }
  .builder-body-wrapper {
    padding-top: 40px !important;
  }

  /* Mobile: Palette becomes floating overlay, does NOT push canvas */
  .palette-sidebar {
    position: absolute !important;
    top: 0 !important;
    left: 0 !important;
    bottom: 0 !important;
    width: 260px !important;
    min-width: 260px !important;
    max-width: 260px !important;
    flex: none !important;
    z-index: 50 !important;
    box-shadow: 4px 0 16px rgba(0, 0, 0, 0.5) !important;
    transition: transform 0.25s ease !important;
    transform: translateX(0);
  }
  .palette-sidebar.is-collapsed {
    width: 0px !important;
    min-width: 0px !important;
    max-width: 0px !important;
    flex: none !important;
    box-shadow: none !important;
    transform: translateX(-100%);
    overflow: hidden !important;
  }
  .palette-inner-container {
    width: 260px;
  }

  /* Mobile: Inspector drawer with non-blocking width and correct top offset */
  .inspector-slide-drawer {
    top: 40px !important;
    height: calc(100% - 40px) !important;
    width: min(340px, 85vw) !important;
    max-width: 85vw !important;
    z-index: 60 !important;
  }
}
</style>

<style>
/* Global styles for Teleported Fullscreen VaModal - 100dvh & top alignment */
.va-modal.va-modal--fullscreen,
.va-modal.record-layout-builder-modal {
  z-index: 99999 !important;
}

.va-modal.va-modal--fullscreen .va-modal__overlay,
.va-modal.record-layout-builder-modal .va-modal__overlay {
  z-index: 99998 !important;
}

.va-modal.va-modal--fullscreen .va-modal__container,
.va-modal.record-layout-builder-modal .va-modal__container {
  z-index: 99999 !important;
  width: 100vw !important;
  height: 100dvh !important;
  height: 100vh !important;
  max-width: 100vw !important;
  max-height: 100dvh !important;
  max-height: 100vh !important;
  margin: 0 !important;
  padding: 0 !important;
  top: 0 !important;
  left: 0 !important;
  position: fixed !important;
  display: flex !important;
  flex-direction: column !important;
  justify-content: flex-start !important;
  align-items: stretch !important;
  overflow: hidden !important;
}

.va-modal.va-modal--fullscreen .va-modal__dialog,
.va-modal.record-layout-builder-modal .va-modal__dialog {
  z-index: 99999 !important;
  width: 100vw !important;
  height: 100dvh !important;
  height: 100vh !important;
  max-width: 100vw !important;
  max-height: 100dvh !important;
  max-height: 100vh !important;
  margin: 0 !important;
  padding: 0 !important;
  border: 0 !important;
  border-radius: 0 !important;
  display: flex !important;
  flex-direction: column !important;
  justify-content: flex-start !important;
  align-items: stretch !important;
  overflow: hidden !important;
  background-color: var(--va-background-primary, #0f172a) !important;
  flex: 1 1 100% !important;
  min-height: 0 !important;
}

.va-modal.record-layout-builder-modal .va-modal__inner {
  display: flex !important;
  flex-direction: column !important;
  justify-content: flex-start !important;
  align-items: stretch !important;
  width: 100% !important;
  height: 100% !important;
  flex: 1 1 100% !important;
  padding: 0 !important;
  margin: 0 !important;
  overflow: hidden !important;
  min-height: 0 !important;
}

.va-modal.record-layout-builder-modal .va-modal__message {
  display: flex !important;
  flex-direction: column !important;
  justify-content: flex-start !important;
  align-items: stretch !important;
  width: 100% !important;
  height: 100% !important;
  flex: 1 1 100% !important;
  padding: 0 !important;
  margin: 0 !important;
  overflow: hidden !important;
  min-height: 0 !important;
}

/* Ensure va-select and va-dropdown popups appear above the fullscreen builder modal */
.va-dropdown__content-wrapper,
.va-select-dropdown__content,
.va-dropdown__content,
.va-dropdown-content {
  z-index: 100005 !important;
}
</style>
