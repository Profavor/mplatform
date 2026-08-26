<template>
  <va-modal
    v-model="visible"
    fullscreen
    no-padding
    hide-default-actions
    without-transitions
    class="record-layout-builder-modal"
  >
    <!-- Custom Clean Header Bar -->
    <div class="builder-header-bar">
      <!-- Left: Title & Scope Info -->
      <div class="header-left">
        <va-icon name="dashboard_customize" size="24px" color="primary" class="mr-2" />
        <div class="title-text-group">
          <div class="title-row">
            <h3 class="builder-title">{{ $t('layout_builder_title') }}</h3>
            <span class="target-scope-badge" :class="isDomainTarget ? 'badge-domain' : 'badge-node'">
              {{ formatTargetName || (isDomainTarget ? $t('layout_scope_domain') : $t('layout_scope_node')) }}
            </span>
          </div>
          <span class="builder-subtitle">{{ $t('layout_builder_desc') }}</span>
        </div>
      </div>

      <!-- Right: Actions Toolbar & Close -->
      <div class="header-right">
        <!-- Auto Generate -->
        <va-button
          preset="secondary"
          color="info"
          size="small"
          icon="auto_awesome"
          class="nowrap-btn"
          @click="autoGenerateLayout"
        >
          {{ $t('btn_auto_generate_layout') }}
        </va-button>

        <!-- Clear Layout -->
        <va-button
          preset="secondary"
          color="danger"
          size="small"
          icon="delete_sweep"
          class="nowrap-btn"
          @click="clearLayout"
        >
          {{ $t('btn_clear_layout') }}
        </va-button>

        <!-- Save -->
        <va-button
          color="primary"
          size="small"
          icon="save"
          :loading="saving"
          class="nowrap-btn"
          @click="saveLayout"
        >
          {{ $t('save') }}
        </va-button>

        <!-- Close Button -->
        <va-button
          preset="plain"
          icon="close"
          color="secondary"
          size="small"
          @click="visible = false"
        />
      </div>
    </div>

    <!-- Layout Presets Control Bar -->
    <div class="layout-presets-bar">
      <div class="preset-selector-group">
        <span class="preset-bar-label">
          <va-icon name="view_quilt" size="18px" color="primary" class="mr-1" />
          {{ $t('layout_select_label') }}:
        </span>

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
            class="nowrap-btn"
            @click="openNewLayoutInline"
          >
            {{ $t('add_new_layout') }}
          </va-button>

          <!-- Duplicate Current Layout -->
          <va-button
            preset="secondary"
            size="small"
            icon="content_copy"
            class="nowrap-btn"
            @click="duplicateCurrentLayout"
          >
            {{ $t('duplicate_layout') }}
          </va-button>

          <!-- Rename Current Layout -->
          <va-button
            preset="secondary"
            size="small"
            icon="edit"
            class="nowrap-btn"
            @click="openRenameLayoutInline"
          >
            {{ $t('rename_layout') }}
          </va-button>

          <!-- Set as Default -->
          <va-button
            :preset="currentLayoutIsDefault ? 'primary' : 'secondary'"
            :color="currentLayoutIsDefault ? 'warning' : 'secondary'"
            size="small"
            :icon="currentLayoutIsDefault ? 'star' : 'star_border'"
            class="nowrap-btn"
            @click="toggleDefaultLayout"
          >
            {{ currentLayoutIsDefault ? $t('default_layout_badge') : $t('set_as_default_layout') }}
          </va-button>

          <!-- Delete Layout -->
          <va-button
            preset="plain"
            color="danger"
            size="small"
            icon="delete"
            :disabled="layouts.length <= 1"
            @click="deleteCurrentLayout"
          />
        </template>

        <!-- Inline Layout Name Editing Mode (Multilingual KO / EN) -->
        <template v-else>
          <div style="display: inline-flex; align-items: center; gap: 6px; flex-wrap: wrap;">
            <va-input
              v-model="layoutNameKoInput"
              size="small"
              style="width: 180px;"
              :placeholder="$t('layout_name_ko_placeholder')"
              autofocus
              @keyup.enter="confirmInlineLayout"
              @keyup.esc="cancelInlineLayout"
            >
              <template #prependInner><span style="font-size: 0.72rem; font-weight: 700; color: var(--va-text-secondary); margin-right: 4px;">KO</span></template>
            </va-input>
            <va-input
              v-model="layoutNameEnInput"
              size="small"
              style="width: 180px;"
              :placeholder="$t('layout_name_en_placeholder')"
              @keyup.enter="confirmInlineLayout"
              @keyup.esc="cancelInlineLayout"
            >
              <template #prependInner><span style="font-size: 0.72rem; font-weight: 700; color: var(--va-text-secondary); margin-right: 4px;">EN</span></template>
            </va-input>
            <va-button size="small" color="primary" icon="check" @click="confirmInlineLayout">
              {{ $t('confirm') }}
            </va-button>
            <va-button size="small" preset="plain" color="secondary" icon="close" @click="cancelInlineLayout">
              {{ $t('cancel') }}
            </va-button>
          </div>
        </template>
      </div>
    </div>

    <!-- Main Builder Body: 3-Column Layout (Palette | Canvas | Inspector) -->
    <div class="builder-body-wrapper">
      <!-- 1. LEFT PALETTE -->
      <div class="palette-sidebar">
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
              :key="field.id"
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

          <!-- UI Widgets Tab -->
          <div v-if="activePaletteTab === 'widgets'" class="widgets-palette-list">
            <!-- Image Box Widget -->
            <div class="palette-item" @click="addCustomWidget('IMAGE', 3, 4)">
              <div class="palette-item-icon">
                <va-icon name="image" size="small" color="success" />
              </div>
              <div class="palette-item-info">
                <span class="palette-item-name">{{ $t('widget_type_image') }}</span>
                <span class="palette-item-desc">{{ $t('widget_size_hint', { w: 3, h: 4 }) }}</span>
              </div>
              <va-icon name="add" size="small" color="secondary" />
            </div>

            <!-- Tiptap Rich Text Editor Widget -->
            <div class="palette-item" @click="addCustomWidget('EDITOR', 12, 8)">
              <div class="palette-item-icon">
                <va-icon name="edit_note" size="small" color="info" />
              </div>
              <div class="palette-item-info">
                <span class="palette-item-name">{{ $t('widget_type_editor') }}</span>
                <span class="palette-item-desc">{{ $t('widget_size_hint', { w: 12, h: 8 }) }}</span>
              </div>
              <va-icon name="add" size="small" color="secondary" />
            </div>

            <!-- Section Card Widget -->
            <div class="palette-item" @click="addCustomWidget('SECTION', 12, 1)">
              <div class="palette-item-icon">
                <va-icon name="folder_open" size="small" color="primary" />
              </div>
              <div class="palette-item-info">
                <span class="palette-item-name">{{ $t('widget_type_section') }}</span>
                <span class="palette-item-desc">{{ $t('widget_size_hint', { w: 12, h: 1 }) }}</span>
              </div>
              <va-icon name="add" size="small" color="secondary" />
            </div>

            <!-- Notice Callout Widget -->
            <div class="palette-item" @click="addCustomWidget('CALLOUT', 12, 2)">
              <div class="palette-item-icon">
                <va-icon name="info" size="small" color="warning" />
              </div>
              <div class="palette-item-info">
                <span class="palette-item-name">{{ $t('widget_type_callout') }}</span>
                <span class="palette-item-desc">{{ $t('widget_size_hint', { w: 12, h: 2 }) }}</span>
              </div>
              <va-icon name="add" size="small" color="secondary" />
            </div>

            <!-- Divider Widget -->
            <div class="palette-item" @click="addCustomWidget('DIVIDER', 12, 1)">
              <div class="palette-item-icon">
                <va-icon name="horizontal_rule" size="small" color="secondary" />
              </div>
              <div class="palette-item-info">
                <span class="palette-item-name">{{ $t('widget_type_divider') }}</span>
                <span class="palette-item-desc">{{ $t('widget_size_hint', { w: 12, h: 1 }) }}</span>
              </div>
              <va-icon name="add" size="small" color="secondary" />
            </div>

            <!-- Specialized Summary Widget -->
            <div class="palette-item" @click="addCustomWidget('SPECIALIZED_SUMMARY', 12, 4)">
              <div class="palette-item-icon">
                <va-icon name="auto_awesome" size="small" color="warning" />
              </div>
              <div class="palette-item-info">
                <span class="palette-item-name">{{ $t('specialized_templates') }}</span>
                <span class="palette-item-desc">{{ $t('widget_size_hint', { w: 12, h: 4 }) }}</span>
              </div>
              <va-icon name="add" size="small" color="secondary" />
            </div>
          </div>
        </div>
      </div>

      <!-- 2. CENTER CANVAS (2D GRID WORKSPACE) -->
      <div class="canvas-workspace-area" ref="canvasAreaRef">
        <div
          class="grid-canvas-container"
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
            @click.stop="selectedWidgetId = widget.id"
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
                  <div v-if="getFieldType(widget) === 'BOOLEAN'" class="single-row-bool">
                    <va-switch :model-value="true" size="small" readonly />
                    <span class="sample-val-text">ON</span>
                  </div>
                  <div v-else-if="getFieldType(widget) === 'MULTILINGUAL'" class="single-row-multi">
                    <span class="sample-chip-tag">KO</span><span class="sample-val-text">{{ getMultilingualSample(widget).ko }}</span>
                    <span class="sample-chip-tag ml-1">EN</span><span class="sample-val-text">{{ getMultilingualSample(widget).en }}</span>
                  </div>
                  <div v-else-if="getFieldType(widget) === 'FILE'" class="single-row-file">
                    <va-icon name="attach_file" size="14px" color="info" />
                    <span class="sample-val-text">{{ getSampleValueForWidget(widget) }}</span>
                  </div>
                  <div v-else-if="widget.type === 'SECTION'" class="single-row-section">
                    <strong>{{ getWidgetDisplayName(widget) }}</strong>
                  </div>
                  <div v-else-if="widget.type === 'DIVIDER'" class="w-full">
                    <hr class="canvas-divider-hr" />
                  </div>
                  <div v-else class="single-row-val-box">
                    <va-icon v-if="getFieldType(widget) === 'DATE'" name="event" size="12px" color="primary" class="mr-1" />
                    <va-icon v-else-if="getFieldType(widget) === 'DATETIME'" name="schedule" size="12px" color="primary" class="mr-1" />
                    <va-icon v-else-if="['DOMAIN_REF', 'DOMAIN_REFERENCE'].includes(getFieldType(widget))" name="search" size="12px" color="primary" class="mr-1" />
                    <va-icon v-else-if="getFieldType(widget) === 'CALCULATED'" name="functions" size="12px" color="primary" class="mr-1" />
                    <span class="sample-val-text">{{ getSampleValueForWidget(widget) }}</span>
                  </div>
                </div>
                <div class="single-row-right">
                  <span class="widget-dimension-tag">{{ widget.w }}x{{ widget.h }}</span>
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
              <!-- Widget Header Bar (Move & Delete) -->
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
                    name="close"
                    size="14px"
                    class="widget-delete-btn"
                    @click.stop="deleteWidget(widget.id)"
                  />
                </div>
              </div>

              <!-- Widget Body Preview with Rich Mock Data -->
              <div class="widget-box-body">
                <!-- IMAGE PREVIEW (Safe Local Vector Illustration) -->
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
                      <!-- Card outline & grid lines -->
                      <circle cx="100" cy="55" r="28" fill="url(#avatarGrad)" opacity="0.85" />
                      <path d="M 55 135 C 55 98, 145 98, 145 135 Z" fill="url(#avatarGrad)" opacity="0.85" />
                      <!-- Badge / Camera Icon -->
                      <circle cx="120" cy="72" r="10" fill="#0f172a" />
                      <circle cx="120" cy="72" r="8" fill="#10b981" />
                    </svg>
                    <div class="mock-image-badge">
                      <va-icon name="photo_camera" size="12px" color="#fff" />
                      <span>{{ widget.w }}x{{ widget.h }} {{ $t('widget_type_image') }}</span>
                    </div>
                  </div>
                </div>

                <!-- EDITOR PREVIEW (Rich Text Notice) -->
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
                <div v-else-if="widget.type === 'CALLOUT'" class="inner-callout-preview">
                  <va-icon name="info" size="small" color="warning" class="mr-1" />
                  <span>{{ getWidgetDisplayName(widget) || $t('mock_callout_desc') }}</span>
                </div>

                <!-- DIVIDER PREVIEW -->
                <div v-else-if="widget.type === 'DIVIDER'" class="inner-divider-preview">
                  <hr class="canvas-divider-hr" />
                </div>

                <!-- SPECIALIZED SUMMARY PREVIEW -->
                <div v-else-if="widget.type === 'SPECIALIZED_SUMMARY'" class="inner-specialized-preview" style="padding: 0.75rem; background: var(--va-background-element); border-radius: 8px;">
                  <div style="display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.5rem;">
                    <va-icon name="auto_awesome" color="warning" size="small" />
                    <strong style="color: var(--va-text-primary); font-size: 0.9rem;">{{ $t('specialized_templates') }}</strong>
                  </div>
                  <div style="font-size: 0.8rem; color: var(--va-text-secondary); line-height: 1.4;">
                    {{ $t('specialized_templates_desc') }}
                  </div>
                </div>

                <!-- FIELD PREVIEW (TYPE SPECIFIC WITH RICH MOCK DATA) -->
                <div v-else class="inner-field-preview">
                  <!-- TABLE / JSON SUBTABLE PREVIEW -->
                  <div v-if="isTableField(widget)" class="canvas-subtable-preview">
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
                  <div v-else-if="getFieldType(widget) === 'DATE'" class="mock-field-card">
                    <va-icon name="calendar_month" size="16px" color="primary" />
                    <span class="mock-field-val">{{ getSampleValueForWidget(widget) }}</span>
                  </div>

                  <!-- DATETIME -->
                  <div v-else-if="getFieldType(widget) === 'DATETIME'" class="mock-field-card">
                    <va-icon name="schedule" size="16px" color="primary" />
                    <span class="mock-field-val">{{ getSampleValueForWidget(widget) }}</span>
                  </div>

                  <!-- NUMBER / CURRENCY -->
                  <div v-else-if="['NUMBER', 'INTEGER', 'DECIMAL', 'FLOAT'].includes(getFieldType(widget))" class="mock-field-card">
                    <span class="mock-currency-badge">₩</span>
                    <span class="mock-field-val font-mono">{{ getSampleValueForWidget(widget) }}</span>
                  </div>

                  <!-- SELECT / CODE / ENUM -->
                  <div v-else-if="['SELECT', 'MULTI_SELECT', 'CODE', 'ENUM'].includes(getFieldType(widget))" class="mock-field-card">
                    <va-badge text="ACTIVE" color="success" size="small" class="mr-1" />
                    <span class="mock-field-val">{{ getSampleValueForWidget(widget) }}</span>
                  </div>

                  <!-- BOOLEAN -->
                  <div v-else-if="getFieldType(widget) === 'BOOLEAN'" class="mock-bool-card">
                    <va-switch :model-value="true" size="small" readonly />
                    <span class="mock-bool-label font-bold text-success">{{ $t('mock_bool_label') }}</span>
                  </div>

                  <!-- DOMAIN_REF -->
                  <div v-else-if="['DOMAIN_REF', 'DOMAIN_REFERENCE'].includes(getFieldType(widget))" class="mock-ref-card">
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
                  <div v-else-if="getFieldType(widget) === 'FILE'" class="mock-file-card">
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

            <!-- Resize Handle (Bottom-Right) -->
            <div
              class="widget-resize-handle"
              :title="$t('drag_to_resize_hint')"
              @mousedown.stop="startResize(widget, $event)"
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

      <!-- 3. RIGHT INSPECTOR -->
      <div class="inspector-sidebar">
        <div class="inspector-header">
          <va-icon name="tune" size="small" color="primary" class="mr-1" />
          <h4 class="inspector-heading">{{ $t('inspector_title') }}</h4>
        </div>

        <div v-if="selectedWidget" class="inspector-content">
          <!-- Widget Dimension / Grid Span Controls -->
          <div class="inspector-section">
            <span class="inspector-section-title">{{ $t('position_and_size') }}</span>
            <div class="inspector-row">
              <div class="inspector-col">
                <span class="inspector-label">{{ $t('widget_width') }}</span>
                <va-input
                  type="number"
                  v-model.number="selectedWidget.w"
                  :min="1"
                  :max="12"
                  size="small"
                />
              </div>
              <div class="inspector-col">
                <span class="inspector-label">{{ $t('widget_height') }}</span>
                <va-input
                  type="number"
                  v-model.number="selectedWidget.h"
                  :min="1"
                  :max="30"
                  size="small"
                />
              </div>
            </div>

            <div class="inspector-row mt-2">
              <div class="inspector-col">
                <span class="inspector-label">{{ $t('widget_pos_x') }}</span>
                <va-input
                  type="number"
                  v-model.number="selectedWidget.x"
                  :min="0"
                  :max="11"
                  size="small"
                />
              </div>
              <div class="inspector-col">
                <span class="inspector-label">{{ $t('widget_pos_y') }}</span>
                <va-input
                  type="number"
                  v-model.number="selectedWidget.y"
                  :min="0"
                  :max="50"
                  size="small"
                />
              </div>
            </div>
          </div>

          <!-- Label Override -->
          <div class="inspector-section">
            <span class="inspector-section-title">{{ $t('widget_label') }}</span>
            <va-input
              v-model="selectedWidgetTitleKo"
              :label="$t('label_ko')"
              size="small"
              class="mb-2 w-full"
            />
            <va-input
              v-model="selectedWidgetTitleEn"
              :label="$t('label_en')"
              size="small"
              class="w-full"
            />
          </div>

          <!-- Options -->
          <div class="inspector-section">
            <span class="inspector-section-title">{{ $t('options') }}</span>
            <va-checkbox
              v-model="selectedWidgetOptions.highlight"
              :label="$t('widget_highlight')"
              class="mb-2"
            />
            <va-checkbox
              v-model="selectedWidgetOptions.readOnly"
              :label="$t('widget_readonly')"
              class="mb-2"
            />
            <va-checkbox
              v-model="selectedWidgetOptions.required"
              :label="$t('widget_required')"
            />
          </div>

          <!-- Delete Widget -->
          <div class="inspector-section">
            <va-button
              color="danger"
              preset="secondary"
              size="small"
              icon="delete"
              style="width: 100%;"
              @click="deleteWidget(selectedWidget.id)"
            >
              {{ $t('widget_delete') }}
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

const cols = ref(12)
const rowHeight = ref(42)
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

// Resizing state
const resizingWidgetId = ref<string | null>(null)
const resizingW = ref(1)
const resizingH = ref(1)

const canvasAreaRef = ref<HTMLElement | null>(null)

const isDomainTarget = computed(() => {
  if (!props.targetNode) return true
  if (props.targetNode.type === 'domain' || props.targetNode.isDomain) return true
  if (props.domainId && props.targetNode.id === props.domainId) return true
  return false
})

const formatTargetName = computed(() => {
  if (!props.targetNode) return ''
  const nameObj = props.targetNode.name
  if (typeof nameObj === 'object') {
    return nameObj[locale.value] || nameObj.ko || nameObj.en || ''
  }
  return String(nameObj || '')
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

const currentLayoutConfig = computed(() => ({
  cols: cols.value,
  rowHeight: rowHeight.value,
  widgets: widgets.value
}))

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
  return props.fields.find((f: any) => f.key === fieldKey)
}

const getFieldType = (widget: any) => {
  const field: any = getFieldDefinition(widget.fieldKey)
  if (field && field.type) return field.type
  return widget.type || 'TEXT'
}

const isFieldType = (widget: any, type: string) => {
  return getFieldType(widget) === type
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

const getFieldIcon = (type: string) => {
  switch (type) {
    case 'IMAGE': return 'image'
    case 'DATE': return 'calendar_today'
    case 'NUMBER': case 'INTEGER': case 'DECIMAL': return 'pin'
    case 'SELECT': case 'ENUM': return 'arrow_drop_down_circle'
    case 'DOMAIN_REF': return 'link'
    case 'BOOLEAN': return 'toggle_on'
    case 'TABLE': case 'JSON': return 'table_rows'
    default: return 'text_fields'
  }
}

const getWidgetIcon = (type: string) => {
  switch (type) {
    case 'IMAGE': return 'image'
    case 'EDITOR': return 'edit_note'
    case 'SECTION': return 'folder_open'
    case 'CALLOUT': return 'info'
    case 'DIVIDER': return 'horizontal_rule'
    case 'TABLE': return 'table_rows'
    default: return 'grid_view'
  }
}

const getWidgetDisplayName = (widget: any) => {
  if (widget.title && typeof widget.title === 'object') {
    return widget.title[locale.value] || widget.title.ko || widget.title.en || widget.fieldKey || ''
  }
  if (widget.title && typeof widget.title === 'string') {
    return widget.title
  }
  if (widget.fieldKey) {
    const field: any = props.fields.find((f: any) => f.key === widget.fieldKey)
    return getFieldName(field) || widget.fieldKey
  }
  return widget.type
}

// Add widgets
const addPredefinedFieldWidget = (field: any) => {
  const isImage = field.type === 'IMAGE' || field.key.includes('photo') || field.key.includes('image')
  const isEditor = field.type === 'HTML' || field.type === 'RICHTEXT' || field.key.includes('desc') || field.key.includes('content')
  const isTable = field.type === 'TABLE' || field.type === 'JSON' || field.isTable || (field.options && String(field.options).includes('tableSchema'))
  
  const w = isEditor || isTable ? 12 : (isImage ? 3 : (field.gridWidth || 4))
  const h = isEditor ? 8 : (isTable ? 5 : (isImage ? 4 : 1))
  const type = isImage ? 'IMAGE' : (isEditor ? 'EDITOR' : (isTable ? 'TABLE' : 'FIELD'))

  const nextPos = findNextAvailablePosition(w, h)

  const newWidget = {
    id: 'widget_' + Date.now() + '_' + Math.random().toString(36).substr(2, 4),
    type,
    fieldKey: field.key,
    title: { ko: getFieldName(field), en: field.key },
    x: nextPos.x,
    y: nextPos.y,
    w,
    h,
    options: {
      required: field.required || false,
      readOnly: field.isReadOnly || false,
      highlight: field.isHighlighted || false
    }
  }

  widgets.value.push(newWidget)
  selectedWidgetId.value = newWidget.id
  resolveWidgetCollisions(newWidget)
}

const addCustomWidget = (type: string, defaultW: number, defaultH: number) => {
  const nextPos = findNextAvailablePosition(defaultW, defaultH)
  const newWidget = {
    id: 'widget_' + Date.now() + '_' + Math.random().toString(36).substr(2, 4),
    type,
    title: { ko: t('widget_type_' + type.toLowerCase()), en: type },
    x: nextPos.x,
    y: nextPos.y,
    w: defaultW,
    h: defaultH,
    options: {}
  }

  widgets.value.push(newWidget)
  selectedWidgetId.value = newWidget.id
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
const startResize = (widget: any, event: MouseEvent) => {
  event.preventDefault()
  resizingWidgetId.value = widget.id
  resizingW.value = widget.w
  resizingH.value = widget.h

  const startMouseX = event.clientX
  const startMouseY = event.clientY
  const startW = widget.w
  const startH = widget.h

  const canvasEl = canvasAreaRef.value
  const colWidth = canvasEl ? canvasEl.clientWidth / cols.value : 80

  const onMouseMove = (moveEvent: MouseEvent) => {
    const deltaX = moveEvent.clientX - startMouseX
    const deltaY = moveEvent.clientY - startMouseY

    const colDelta = Math.round(deltaX / colWidth)
    const rowDelta = Math.round(deltaY / rowHeight.value)

    const newW = Math.max(1, Math.min(cols.value - widget.x, startW + colDelta))
    const newH = Math.max(1, Math.min(30, startH + rowDelta))

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
  selectedWidgetId.value = widget.id
  const startMouseX = event.clientX
  const startMouseY = event.clientY
  const startX = widget.x
  const startY = widget.y

  const canvasEl = canvasAreaRef.value
  const colWidth = canvasEl ? canvasEl.clientWidth / cols.value : 80

  const onMouseMove = (moveEvent: MouseEvent) => {
    const deltaX = moveEvent.clientX - startMouseX
    const deltaY = moveEvent.clientY - startMouseY

    const colDelta = Math.round(deltaX / colWidth)
    const rowDelta = Math.round(deltaY / rowHeight.value)

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

    const w = isEditor || isTable ? 12 : (isImage ? 3 : (field.gridWidth || 4))
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
}

const syncCurrentLayoutToMemory = () => {
  if (!activeLayoutId.value) return
  const current = layouts.value.find(l => l.id === activeLayoutId.value)
  if (current) {
    current.cols = cols.value
    current.rowHeight = rowHeight.value
    current.widgets = JSON.parse(JSON.stringify(widgets.value))
  }
}

const loadLayoutToCanvas = (targetLayout: any) => {
  if (!targetLayout) return
  cols.value = targetLayout.cols || 12
  rowHeight.value = targetLayout.rowHeight || 42
  widgets.value = JSON.parse(JSON.stringify(targetLayout.widgets || []))
  selectedWidgetId.value = null
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
      cols: 12,
      rowHeight: 42,
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
  if (isDomainTarget.value || !props.targetNode?.id || props.targetNode.id === props.domainId) {
    return `/api/domains/${props.domainId}/layout`
  }
  return `/api/domains/${props.domainId}/nodes/${props.targetNode.id}/layout`
}

const fetchLayout = async () => {
  if (!props.domainId && !props.targetNode?.id) return
  try {
    const url = getApiUrl()
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
  syncCurrentLayoutToMemory()
  saving.value = true
  try {
    const url = getApiUrl()
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
    fetchLayout()
  }
})
</script>

<style scoped>
/* Modal Header Bar */
.builder-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 0.65rem 1.25rem;
  background: var(--va-background-primary, #ffffff);
  border-bottom: 1px solid var(--va-background-border, #e2e8f0);
}

.header-left {
  display: flex;
  align-items: center;
}

.title-text-group {
  display: flex;
  flex-direction: column;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.builder-title {
  font-size: 1.1rem;
  font-weight: 800;
  margin: 0;
  color: var(--va-text-primary, #1e293b);
  white-space: nowrap;
}

.target-scope-badge {
  font-size: 0.75rem;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 12px;
  white-space: nowrap;
}

.badge-domain {
  background: rgba(21, 78, 193, 0.15);
  color: var(--va-primary, #154ec1);
  border: 1px solid rgba(21, 78, 193, 0.3);
}

.badge-node {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.builder-subtitle {
  font-size: 0.75rem;
  color: var(--va-text-secondary, #64748b);
  margin-top: 1px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: nowrap;
}

.nowrap-btn {
  white-space: nowrap !important;
}

/* Layout Presets Control Bar */
.layout-presets-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 1.25rem;
  background: var(--va-background-element, #141b2d);
  border-bottom: 1px solid var(--va-background-border, #334155);
  box-sizing: border-box;
}

.preset-selector-group {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.preset-bar-label {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--va-text-primary, #f8fafc);
  display: flex;
  align-items: center;
}

.preset-select-control {
  min-width: 200px;
}

.builder-body-wrapper {
  display: flex;
  width: 100%;
  height: calc(100vh - 110px);
  overflow: hidden;
  background: var(--va-background-secondary, #0f172a);
}

/* Palette Sidebar */
.palette-sidebar {
  width: 280px;
  min-width: 280px;
  max-width: 280px;
  flex: 0 0 280px;
  background: var(--va-background-primary, #1e2640);
  border-right: 1px solid var(--va-background-border, #334155);
  display: flex;
  flex-direction: column;
  height: 100%;
}

.palette-tabs-header {
  display: flex;
  border-bottom: 1px solid var(--va-background-border, #334155);
  padding: 4px;
}

.palette-tab-btn {
  flex: 1;
  border: none;
  background: transparent;
  padding: 8px 4px;
  font-size: 0.85rem;
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
  padding: 0.75rem;
}

.palette-item {
  display: flex;
  align-items: center;
  padding: 8px 10px;
  border: 1px solid var(--va-background-border, #334155);
  border-radius: 6px;
  margin-bottom: 6px;
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
  margin-right: 8px;
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
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--va-text-primary, #f8fafc);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.palette-item-key, .palette-item-desc {
  font-size: 0.75rem;
  color: var(--va-text-secondary, #94a3b8);
}

.empty-palette-notice {
  text-align: center;
  padding: 2rem 1rem;
  color: var(--va-text-secondary, #94a3b8);
  font-size: 0.85rem;
}

/* Canvas Area */
.canvas-workspace-area {
  flex: 1 1 0%;
  min-width: 0;
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 1.25rem;
  box-sizing: border-box;
  background-color: var(--va-background-secondary, #0f172a);
  background-image: radial-gradient(var(--va-background-border, #334155) 1.5px, transparent 1.5px);
  background-size: 24px 24px;
}

.grid-canvas-container {
  background: var(--va-background-primary, #1e2640);
  border: 2px dashed var(--va-background-border, #475569);
  border-radius: 8px;
  padding: 10px;
  width: 100%;
  box-sizing: border-box;
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
  box-shadow: 0 0 0 2px rgba(21, 78, 193, 0.4);
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
  gap: 4px;
}

.widget-dimension-tag {
  font-size: 0.68rem;
  background: var(--va-background-border, #334155);
  padding: 1px 4px;
  border-radius: 4px;
  color: var(--va-text-secondary, #94a3b8);
  font-weight: 600;
}

.widget-delete-btn {
  cursor: pointer;
  color: var(--va-text-secondary, #94a3b8);
  transition: color 0.2s;
}

.widget-delete-btn:hover {
  color: #ef4444;
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

/* Mock Image Photo */
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

.mock-profile-img, .mock-vector-svg {
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

/* Mock Editor HTML */
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

/* Mock Field Cards */
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

/* Subtable Canvas Mini Preview */
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

/* Resize Handle */
.widget-resize-handle {
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
}

.widget-resize-handle:hover {
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

/* Inspector Sidebar */
.inspector-sidebar {
  width: 320px;
  min-width: 320px;
  max-width: 320px;
  flex: 0 0 320px;
  background: var(--va-background-primary, #1e2640);
  border-left: 1px solid var(--va-background-border, #334155);
  display: flex;
  flex-direction: column;
  height: 100%;
}

.inspector-header {
  padding: 0.75rem 1rem;
  border-bottom: 1px solid var(--va-background-border, #334155);
  display: flex;
  align-items: center;
}

.inspector-heading {
  margin: 0;
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--va-text-primary, #f8fafc);
}

.inspector-content {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
}

.inspector-section {
  margin-bottom: 1.25rem;
  padding-bottom: 1rem;
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

.inspector-row {
  display: flex;
  gap: 0.5rem;
}

.inspector-col {
  flex: 1;
}

.inspector-label {
  font-size: 0.75rem;
  color: var(--va-text-secondary, #94a3b8);
  display: block;
  margin-bottom: 2px;
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

.preview-mode-container {
  background: var(--va-background-primary, #1e2640);
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
}
</style>
