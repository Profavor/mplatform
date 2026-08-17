<template>
  <Teleport to="body" :disabled="!isFullscreen">
    <div 
      class="html-editor-container" 
      :class="{ 
        'is-readonly': readonly || disabled,
        'is-focused': isFocused,
        'has-error': hasError,
        'is-fullscreen': isFullscreen
      }"
      @click.stop
      @keydown.esc="onEscKey"
    >
      <!-- Fullscreen Top Navigation Bar -->
      <div v-if="isFullscreen" class="fullscreen-header-bar">
        <div class="fullscreen-title">
          <span>📝 {{ placeholder || t('editor_fullscreen') }}</span>
        </div>
        <va-button
          preset="primary"
          size="small"
          icon="fullscreen_exit"
          @click="toggleFullscreen"
        >
          {{ t('editor_exit_fullscreen') }} (Esc)
        </va-button>
      </div>

      <!-- Hidden file input for manual image upload -->
      <input
        ref="fileInputRef"
        type="file"
        accept="image/*"
        style="display: none;"
        @change="onImageFileSelected"
      />

    <!-- Uploading Overlay / Banner -->
    <div v-if="isUploadingImage" class="image-uploading-bar">
      <va-progress-circle indeterminate size="16px" color="primary" />
      <span>{{ t('uploading_image') }}</span>
    </div>

    <!-- Toolbar (Only visible in edit mode) -->
    <div v-if="!readonly && !disabled" class="html-editor-toolbar">
      <!-- Toolbar Row 1: Typography & Text Styling -->
      <div class="toolbar-row">
        <!-- Font Family Selector -->
        <div class="toolbar-group">
          <select 
            class="editor-select font-family-select" 
            :title="t('editor_font_family')"
            @change="setFontFamily(($event.target as HTMLSelectElement).value)"
          >
            <option value="">{{ t('editor_font_family') }} (기본)</option>
            <option value="Pretendard, sans-serif">Pretendard</option>
            <option value="'Noto Sans KR', sans-serif">Noto Sans KR</option>
            <option value="'Nanum Gothic', sans-serif">나눔고딕</option>
            <option value="Arial, sans-serif">Arial</option>
            <option value="'Times New Roman', serif">Times New Roman</option>
            <option value="'Courier New', monospace">Courier New</option>
            <option value="Consolas, monospace">Consolas</option>
          </select>
        </div>

        <!-- Font Size Selector -->
        <div class="toolbar-group">
          <select 
            class="editor-select font-size-select" 
            :title="t('editor_font_size')"
            @change="setFontSize(($event.target as HTMLSelectElement).value)"
          >
            <option value="">{{ t('editor_font_size') }}</option>
            <option value="12px">12px</option>
            <option value="14px">14px</option>
            <option value="16px">16px (기본)</option>
            <option value="18px">18px</option>
            <option value="20px">20px</option>
            <option value="24px">24px</option>
            <option value="28px">28px</option>
            <option value="32px">32px</option>
          </select>
        </div>

        <div class="toolbar-divider" />

        <!-- Basic Text Styles -->
        <div class="toolbar-group">
          <button
            type="button"
            class="editor-btn"
            :class="{ 'is-active': editor?.isActive('bold') }"
            :title="t('editor_bold')"
            @click="editor?.chain().focus().toggleBold().run()"
          >
            <b>B</b>
          </button>
          <button
            type="button"
            class="editor-btn"
            :class="{ 'is-active': editor?.isActive('italic') }"
            :title="t('editor_italic')"
            @click="editor?.chain().focus().toggleItalic().run()"
          >
            <i>I</i>
          </button>
          <button
            type="button"
            class="editor-btn"
            :class="{ 'is-active': editor?.isActive('underline') }"
            :title="t('editor_underline')"
            @click="editor?.chain().focus().toggleUnderline().run()"
          >
            <u>U</u>
          </button>
          <button
            type="button"
            class="editor-btn"
            :class="{ 'is-active': editor?.isActive('strike') }"
            :title="t('editor_strike')"
            @click="editor?.chain().focus().toggleStrike().run()"
          >
            <s>S</s>
          </button>
        </div>

        <div class="toolbar-divider" />

        <!-- Text Color Picker -->
        <div class="toolbar-group color-picker-group">
          <label class="editor-btn color-label" :title="t('editor_text_color')">
            <span class="color-indicator" :style="{ backgroundColor: currentTextColor }">A</span>
            <input 
              type="color" 
              class="hidden-color-input"
              :value="currentTextColor"
              @input="onTextColorChange(($event.target as HTMLInputElement).value)"
            />
          </label>
        </div>

        <!-- Highlight / Background Color Picker -->
        <div class="toolbar-group color-picker-group">
          <label class="editor-btn color-label" :title="t('editor_highlight')">
            <span class="highlight-indicator" :style="{ backgroundColor: currentHighlightColor }">🖊️</span>
            <input 
              type="color" 
              class="hidden-color-input"
              :value="currentHighlightColor"
              @input="onHighlightColorChange(($event.target as HTMLInputElement).value)"
            />
          </label>
        </div>

        <!-- Clear Formatting -->
        <div class="toolbar-group">
          <button
            type="button"
            class="editor-btn"
            :title="t('editor_clear_formatting')"
            @click="clearFormatting"
          >
            🧹
          </button>
        </div>

        <div class="toolbar-divider" />

        <!-- Alignment -->
        <div class="toolbar-group">
          <button
            type="button"
            class="editor-btn"
            :class="{ 'is-active': editor?.isActive({ textAlign: 'left' }) }"
            :title="t('editor_align_left')"
            @click="editor?.chain().focus().setTextAlign('left').run()"
          >
            ⇤
          </button>
          <button
            type="button"
            class="editor-btn"
            :class="{ 'is-active': editor?.isActive({ textAlign: 'center' }) }"
            :title="t('editor_align_center')"
            @click="editor?.chain().focus().setTextAlign('center').run()"
          >
            ⇹
          </button>
          <button
            type="button"
            class="editor-btn"
            :class="{ 'is-active': editor?.isActive({ textAlign: 'right' }) }"
            :title="t('editor_align_right')"
            @click="editor?.chain().focus().setTextAlign('right').run()"
          >
            ⇥
          </button>
          <button
            type="button"
            class="editor-btn"
            :class="{ 'is-active': editor?.isActive({ textAlign: 'justify' }) }"
            :title="t('editor_align_justify')"
            @click="editor?.chain().focus().setTextAlign('justify').run()"
          >
            ≡
          </button>
        </div>

        <!-- Fullscreen Button -->
        <div class="toolbar-group" style="margin-left: auto;">
          <button
            type="button"
            class="editor-btn"
            :class="{ 'is-active': isFullscreen }"
            :title="isFullscreen ? t('editor_exit_fullscreen') : t('editor_fullscreen')"
            @click="toggleFullscreen"
          >
            {{ isFullscreen ? '🗗' : '⛶' }}
          </button>
        </div>
      </div>

      <!-- Toolbar Row 2: Structure, Tables, Code, Media & History -->
      <div class="toolbar-row">
        <!-- Headings -->
        <div class="toolbar-group">
          <button
            type="button"
            class="editor-btn"
            :class="{ 'is-active': editor?.isActive('heading', { level: 1 }) }"
            :title="t('editor_heading1')"
            @click="editor?.chain().focus().toggleHeading({ level: 1 }).run()"
          >
            H1
          </button>
          <button
            type="button"
            class="editor-btn"
            :class="{ 'is-active': editor?.isActive('heading', { level: 2 }) }"
            :title="t('editor_heading2')"
            @click="editor?.chain().focus().toggleHeading({ level: 2 }).run()"
          >
            H2
          </button>
          <button
            type="button"
            class="editor-btn"
            :class="{ 'is-active': editor?.isActive('heading', { level: 3 }) }"
            :title="t('editor_heading3')"
            @click="editor?.chain().focus().toggleHeading({ level: 3 }).run()"
          >
            H3
          </button>
          <button
            type="button"
            class="editor-btn"
            :class="{ 'is-active': editor?.isActive('paragraph') && !editor?.isActive('heading') }"
            :title="t('editor_paragraph')"
            @click="editor?.chain().focus().setParagraph().run()"
          >
            P
          </button>
        </div>

        <div class="toolbar-divider" />

        <!-- Lists & Tasks -->
        <div class="toolbar-group">
          <button
            type="button"
            class="editor-btn"
            :class="{ 'is-active': editor?.isActive('bulletList') }"
            :title="t('editor_bullet_list')"
            @click="editor?.chain().focus().toggleBulletList().run()"
          >
            •≡
          </button>
          <button
            type="button"
            class="editor-btn"
            :class="{ 'is-active': editor?.isActive('orderedList') }"
            :title="t('editor_ordered_list')"
            @click="editor?.chain().focus().toggleOrderedList().run()"
          >
            1.≡
          </button>
          <button
            type="button"
            class="editor-btn"
            :class="{ 'is-active': editor?.isActive('taskList') }"
            :title="t('editor_task_list')"
            @click="editor?.chain().focus().toggleTaskList().run()"
          >
            ☑
          </button>
          <button
            type="button"
            class="editor-btn"
            :class="{ 'is-active': editor?.isActive('blockquote') }"
            :title="t('editor_blockquote')"
            @click="editor?.chain().focus().toggleBlockquote().run()"
          >
            ❝
          </button>
        </div>

        <div class="toolbar-divider" />

        <!-- Table Controls Dropdown -->
        <div class="toolbar-group table-tools-group">
          <button
            type="button"
            class="editor-btn"
            :class="{ 'is-active': editor?.isActive('table') }"
            :title="t('editor_table')"
            @click="toggleTableMenu"
          >
            ⊞ {{ t('editor_table') }} ▾
          </button>

          <!-- Table Action Menu Popup -->
          <div v-if="showTableMenu" class="table-menu-popup" @click.stop>
            <div class="table-menu-item" @click="insertTable">
              ➕ {{ t('editor_insert_table') }}
            </div>
            <div v-if="editor?.isActive('table')" class="table-menu-divider" />
            <template v-if="editor?.isActive('table')">
              <div class="table-menu-item" @click="editor?.chain().focus().addRowBefore().run(); showTableMenu = false;">
                ⬆️ {{ t('editor_add_row_before') }}
              </div>
              <div class="table-menu-item" @click="editor?.chain().focus().addRowAfter().run(); showTableMenu = false;">
                ⬇️ {{ t('editor_add_row_after') }}
              </div>
              <div class="table-menu-item" @click="editor?.chain().focus().deleteRow().run(); showTableMenu = false;">
                ❌ {{ t('editor_delete_row') }}
              </div>
              <div class="table-menu-divider" />
              <div class="table-menu-item" @click="editor?.chain().focus().addColumnBefore().run(); showTableMenu = false;">
                ⬅️ {{ t('editor_add_col_before') }}
              </div>
              <div class="table-menu-item" @click="editor?.chain().focus().addColumnAfter().run(); showTableMenu = false;">
                ➡️ {{ t('editor_add_col_after') }}
              </div>
              <div class="table-menu-item" @click="editor?.chain().focus().deleteColumn().run(); showTableMenu = false;">
                ❌ {{ t('editor_delete_col') }}
              </div>
              <div class="table-menu-divider" />
              <div class="table-menu-item" @click="editor?.chain().focus().mergeCells().run(); showTableMenu = false;">
                🔀 {{ t('editor_merge_cells') }}
              </div>
              <div class="table-menu-item" @click="editor?.chain().focus().splitCell().run(); showTableMenu = false;">
                🔀 {{ t('editor_split_cell') }}
              </div>
              <div class="table-menu-item" @click="editor?.chain().focus().toggleHeaderRow().run(); showTableMenu = false;">
                🏷️ {{ t('editor_toggle_header_row') }}
              </div>
              <div class="table-menu-divider" />
              <div class="table-menu-item table-menu-danger" @click="editor?.chain().focus().deleteTable().run(); showTableMenu = false;">
                🗑️ {{ t('editor_delete_table') }}
              </div>
            </template>
          </div>
        </div>

        <!-- Syntax Highlighted Code Block -->
        <div class="toolbar-group">
          <button
            type="button"
            class="editor-btn"
            :class="{ 'is-active': editor?.isActive('codeBlock') }"
            :title="t('editor_code_block')"
            @click="toggleCodeBlock"
          >
            &lt;/&gt;
          </button>
          
          <select
            v-if="editor?.isActive('codeBlock')"
            class="editor-select code-lang-select"
            :value="currentCodeBlockLanguage"
            @change="updateCodeBlockLanguage(($event.target as HTMLSelectElement).value)"
          >
            <option value="sql">SQL</option>
            <option value="java">Java</option>
            <option value="javascript">JavaScript</option>
            <option value="typescript">TypeScript</option>
            <option value="python">Python</option>
            <option value="json">JSON</option>
            <option value="html">HTML / XML</option>
            <option value="css">CSS</option>
            <option value="bash">Bash / Shell</option>
          </select>
        </div>

        <!-- Horizontal Line -->
        <div class="toolbar-group">
          <button
            type="button"
            class="editor-btn"
            :title="t('editor_horizontal_rule')"
            @click="editor?.chain().focus().setHorizontalRule().run()"
          >
            ―
          </button>
        </div>

        <div class="toolbar-divider" />

        <!-- Link Dialog -->
        <div class="toolbar-group">
          <button
            type="button"
            class="editor-btn"
            :class="{ 'is-active': editor?.isActive('link') }"
            :title="t('editor_link')"
            @click="setLink"
          >
            🔗
          </button>
          <button
            v-if="editor?.isActive('link')"
            type="button"
            class="editor-btn"
            :title="t('editor_unlink')"
            @click="editor?.chain().focus().unsetLink().run()"
          >
            ⛓️❌
          </button>
        </div>

        <!-- Image Upload Button -->
        <div class="toolbar-group">
          <button
            type="button"
            class="editor-btn"
            :disabled="isUploadingImage"
            :title="t('editor_image')"
            @click="triggerImagePicker"
          >
            🖼️
          </button>
        </div>

        <!-- History -->
        <div class="toolbar-group" style="margin-left: auto;">
          <button
            type="button"
            class="editor-btn"
            :disabled="!editor?.can().undo()"
            :title="t('editor_undo')"
            @click="editor?.chain().focus().undo().run()"
          >
            ↶
          </button>
          <button
            type="button"
            class="editor-btn"
            :disabled="!editor?.can().redo()"
            :title="t('editor_redo')"
            @click="editor?.chain().focus().redo().run()"
          >
            ↷
          </button>
        </div>
      </div>
    </div>

    <!-- Editor Content Area -->
    <div
      class="html-editor-content"
      :style="{ minHeight: isFullscreen ? 'calc(100vh - 120px)' : minHeight }"
      @click="handleContentClick"
    >
      <editor-content v-if="editor" :editor="editor" />
    </div>

    <!-- Footer Status / Character Counter -->
    <div v-if="editor" class="html-editor-footer">
      <span class="char-count-text">
        {{ t('editor_character_count', { count: characterCount }) }}
      </span>
    </div>

    <!-- Image Lightbox Modal for Viewer Mode -->
    <ImageLightboxModal
      v-model="isViewerModalOpen"
      :images="viewerImages"
      :initial-index="viewerInitialIndex"
    />
  </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import { Underline } from '@tiptap/extension-underline'
import { TextAlign } from '@tiptap/extension-text-align'
import { Link } from '@tiptap/extension-link'
import { Image } from '@tiptap/extension-image'
import { Placeholder } from '@tiptap/extension-placeholder'
import { TextStyle, Color, FontFamily, FontSize } from '@tiptap/extension-text-style'
import { Highlight } from '@tiptap/extension-highlight'
import { Table, TableRow, TableCell, TableHeader } from '@tiptap/extension-table'
import { TaskList } from '@tiptap/extension-task-list'
import { TaskItem } from '@tiptap/extension-task-item'
import { CharacterCount } from '@tiptap/extension-character-count'
import { CodeBlockLowlight } from '@tiptap/extension-code-block-lowlight'
import { createLowlight, all } from 'lowlight'
import { useCustomFetch } from '~/composables/useCustomFetch'
import { useAuthenticatedImage } from '~/composables/useAuthenticatedImage'
import ImageLightboxModal from '~/components/common/ImageLightboxModal.vue'

// Initialize lowlight with all standard syntax highlighters (SQL, Java, JS, Python, etc.)
const lowlight = createLowlight(all)

const { t } = useI18n()
const { customFetch } = useCustomFetch()
const { getAuthenticatedImageUrl, transformHtmlImagesToBlob, restoreBlobImagesToOriginal } = useAuthenticatedImage()

const props = withDefaults(defineProps<{
  modelValue?: string
  placeholder?: string
  readonly?: boolean
  disabled?: boolean
  minHeight?: string
  hasError?: boolean
}>(), {
  modelValue: '',
  placeholder: '',
  readonly: false,
  disabled: false,
  minHeight: '140px',
  hasError: false
})

const emit = defineEmits<{
  (e: 'update:modelValue', val: string): void
  (e: 'change', val: string): void
}>()

const isFocused = ref(false)
const isFullscreen = ref(false)
const isUploadingImage = ref(false)
const showTableMenu = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)

// Lightbox Viewer Modal State
const isViewerModalOpen = ref(false)
const viewerImages = ref<string[]>([])
const viewerInitialIndex = ref(0)

const handleContentClick = (e: MouseEvent) => {
  // Only trigger lightbox viewer in readonly or disabled mode
  if (!props.readonly && !props.disabled) return

  const target = e.target as HTMLElement
  if (target && target.tagName === 'IMG') {
    e.stopPropagation()
    const clickedSrc = target.getAttribute('src')
    if (!clickedSrc) return

    // Find all images within this editor container
    const container = target.closest('.html-editor-content')
    if (container) {
      const allImgs = Array.from(container.querySelectorAll('img'))
        .map(img => img.getAttribute('src'))
        .filter(Boolean) as string[]
      
      viewerImages.value = allImgs.length > 0 ? allImgs : [clickedSrc]
      const idx = allImgs.indexOf(clickedSrc)
      viewerInitialIndex.value = idx >= 0 ? idx : 0
    } else {
      viewerImages.value = [clickedSrc]
      viewerInitialIndex.value = 0
    }

    isViewerModalOpen.value = true
  }
}

const currentTextColor = ref('#1e293b')
const currentHighlightColor = ref('#fef08a')

// Upload image file to MinIO file server and obtain authenticated blob URL
const uploadImageToMinio = async (file: File): Promise<string | null> => {
  if (!file) return null
  isUploadingImage.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const res: any = await customFetch('/api/files/upload', {
      method: 'POST',
      body: formData
    })
    
    const fileUrl = res?.url || (res?.fileId ? `/api/files/download/${res.fileId}?name=${encodeURIComponent(file.name)}` : null) || (typeof res === 'string' ? res : null)
    if (fileUrl) {
      const blobUrl = await getAuthenticatedImageUrl(fileUrl)
      return blobUrl || fileUrl
    }
    return null
  } catch (error) {
    console.error('Failed to upload image to MinIO:', error)
    return null
  } finally {
    isUploadingImage.value = false
  }
}

// Trigger manual file selection dialog
const triggerImagePicker = () => {
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
    fileInputRef.value.click()
  }
}

// Handle manual file selection
const onImageFileSelected = async (e: Event) => {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (file && editor.value) {
    const url = await uploadImageToMinio(file)
    if (url) {
      editor.value.chain().focus().setImage({ src: url, alt: file.name }).run()
    }
  }
}

// Editor instance
const editor = useEditor({
  content: '',
  editable: !props.readonly && !props.disabled,
  extensions: [
    StarterKit.configure({
      heading: {
        levels: [1, 2, 3]
      },
      link: false,
      underline: false,
      codeBlock: false
    } as any),
    Underline,
    TextStyle,
    FontFamily.configure({
      types: ['textStyle']
    }),
    FontSize,
    Color.configure({
      types: ['textStyle']
    }),
    Highlight.configure({
      multicolor: true
    }),
    Table.configure({
      resizable: true
    }),
    TableRow,
    TableHeader,
    TableCell,
    TaskList,
    TaskItem.configure({
      nested: true
    }),
    CodeBlockLowlight.configure({
      lowlight,
      defaultLanguage: 'sql'
    }),
    TextAlign.configure({
      types: ['heading', 'paragraph']
    }),
    Link.configure({
      openOnClick: false
    }),
    Image.configure({
      inline: true,
      allowBase64: false
    }),
    CharacterCount.configure(),
    Placeholder.configure({
      placeholder: props.placeholder || t('editor_placeholder')
    })
  ],
  editorProps: {
    handlePaste: (view, event) => {
      const items = event.clipboardData?.items
      if (!items) return false
      
      for (const item of Array.from(items)) {
        if (item.type.startsWith('image/')) {
          const file = item.getAsFile()
          if (file) {
            uploadImageToMinio(file).then((url) => {
              if (url && editor.value) {
                editor.value.chain().focus().setImage({ src: url, alt: file.name }).run()
              }
            })
            event.preventDefault()
            return true
          }
        }
      }
      return false
    },
    handleDrop: (view, event, slice, moved) => {
      if (!moved && event.dataTransfer && event.dataTransfer.files && event.dataTransfer.files.length > 0) {
        const file = event.dataTransfer.files[0]
        if (file && file.type.startsWith('image/')) {
          uploadImageToMinio(file).then((url) => {
            if (url && editor.value) {
              editor.value.chain().focus().setImage({ src: url, alt: file.name }).run()
            }
          })
          event.preventDefault()
          return true
        }
      }
      return false
    }
  },
  onUpdate: () => {
    if (!editor.value) return
    const html = editor.value.getHTML()
    const rawCleaned = html === '<p></p>' ? '' : html
    const restored = restoreBlobImagesToOriginal(rawCleaned)
    emit('update:modelValue', restored)
    emit('change', restored)
  },
  onFocus: () => {
    isFocused.value = true
  },
  onBlur: () => {
    isFocused.value = false
  }
})

// Character count computed
const characterCount = computed(() => {
  return editor.value?.storage.characterCount.characters() || 0
})

// Current code block language
const currentCodeBlockLanguage = computed(() => {
  if (!editor.value) return 'sql'
  const attrs = editor.value.getAttributes('codeBlock')
  return attrs.language || 'sql'
})

// Format actions
const setFontFamily = (family: string) => {
  if (!editor.value) return
  if (!family) {
    editor.value.chain().focus().unsetFontFamily().run()
  } else {
    editor.value.chain().focus().setFontFamily(family).run()
  }
}

const setFontSize = (size: string) => {
  if (!editor.value) return
  if (!size) {
    (editor.value.chain().focus() as any).unsetFontSize().run()
  } else {
    (editor.value.chain().focus() as any).setFontSize(size).run()
  }
}

const onTextColorChange = (color: string) => {
  currentTextColor.value = color
  editor.value?.chain().focus().setColor(color).run()
}

const onHighlightColorChange = (color: string) => {
  currentHighlightColor.value = color
  editor.value?.chain().focus().toggleHighlight({ color }).run()
}

const clearFormatting = () => {
  editor.value?.chain().focus().clearNodes().unsetAllMarks().run()
}

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
}

const onEscKey = (e?: KeyboardEvent) => {
  if (isFullscreen.value) {
    if (e) {
      e.stopPropagation()
      e.preventDefault()
    }
    isFullscreen.value = false
  }
}

const onGlobalKeyDown = (e: KeyboardEvent) => {
  if (e.key === 'Escape' && isFullscreen.value) {
    e.stopPropagation()
    e.preventDefault()
    isFullscreen.value = false
  }
}

const toggleTableMenu = () => {
  showTableMenu.value = !showTableMenu.value
}

const insertTable = () => {
  editor.value?.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run()
  showTableMenu.value = false
}

const toggleCodeBlock = () => {
  editor.value?.chain().focus().toggleCodeBlock({ language: 'sql' }).run()
}

const updateCodeBlockLanguage = (lang: string) => {
  editor.value?.chain().focus().updateAttributes('codeBlock', { language: lang }).run()
}

const setLink = () => {
  if (!editor.value) return
  const previousUrl = editor.value.getAttributes('link').href
  const url = window.prompt('URL:', previousUrl)
  if (url === null) return
  if (url === '') {
    editor.value.chain().focus().extendMarkRange('link').unsetLink().run()
    return
  }
  editor.value.chain().focus().extendMarkRange('link').setLink({ href: url, target: '_blank' }).run()
}

// Close table menu when clicking outside
const onGlobalClick = (e: MouseEvent) => {
  if (typeof document === 'undefined') return
  const target = e.target as HTMLElement
  if (target && !target.closest('.table-tools-group')) {
    showTableMenu.value = false
  }
}

onMounted(async () => {
  if (typeof document !== 'undefined') {
    document.addEventListener('click', onGlobalClick)
    window.addEventListener('keydown', onGlobalKeyDown, true)
  }
  if (props.modelValue) {
    const transformed = await transformHtmlImagesToBlob(props.modelValue)
    if (editor.value && !editor.value.isDestroyed) {
      editor.value.commands.setContent(transformed, false)
    }
  }
})

// Watch modelValue from parent
watch(() => props.modelValue, async (newVal) => {
  if (!editor.value || editor.value.isDestroyed) return
  const currentHtml = editor.value.getHTML()
  const restoredCurrent = restoreBlobImagesToOriginal(currentHtml)
  const val = newVal || ''
  if (val !== restoredCurrent && (val !== '' || currentHtml !== '<p></p>')) {
    const transformed = await transformHtmlImagesToBlob(val)
    if (editor.value && !editor.value.isDestroyed) {
      editor.value.commands.setContent(transformed, false)
    }
  }
})

// Watch editable state
watch(() => [props.readonly, props.disabled], ([r, d]) => {
  if (!editor.value || editor.value.isDestroyed) return
  editor.value.setEditable(!r && !d)
})

onBeforeUnmount(() => {
  if (typeof document !== 'undefined') {
    document.removeEventListener('click', onGlobalClick)
    window.removeEventListener('keydown', onGlobalKeyDown, true)
  }
  if (editor.value && !editor.value.isDestroyed) {
    editor.value.destroy()
  }
})
</script>

<style scoped>
.html-editor-container {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--va-background-border, #d1d5db);
  border-radius: 6px;
  background-color: var(--va-background-element, #ffffff);
  overflow: hidden;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  width: 100%;
  position: relative;
}

.html-editor-container.is-focused {
  border-color: var(--va-primary, #2563eb);
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.15);
}

.html-editor-container.has-error {
  border-color: var(--va-danger, #ef4444);
}

.html-editor-container.is-readonly {
  background-color: var(--va-background-secondary, #f8fafc);
  border-color: var(--va-background-border, #e2e8f0);
}

.html-editor-container.is-readonly .html-editor-content :deep(img) {
  cursor: zoom-in;
  transition: transform 0.15s ease, box-shadow 0.15s ease, outline 0.15s ease;
  border-radius: 4px;
}

.html-editor-container.is-readonly .html-editor-content :deep(img:hover) {
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.25);
  outline: 2px solid var(--va-primary, #2563eb);
  outline-offset: 2px;
}

/* Fullscreen Header Bar */
.fullscreen-header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 18px;
  background-color: var(--va-background-secondary, #f8fafc);
  color: var(--va-text-primary, #1e293b);
  border-bottom: 1px solid var(--va-background-border, #e2e8f0);
}

.fullscreen-title {
  font-size: 0.95rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--va-text-primary, #1e293b);
}

/* Fullscreen Mode */
.html-editor-container.is-fullscreen {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  z-index: 999999 !important;
  border-radius: 0 !important;
  height: 100vh !important;
  width: 100vw !important;
  max-width: 100vw !important;
  max-height: 100vh !important;
  box-shadow: none !important;
  background-color: var(--va-background-element, #ffffff) !important;
  display: flex !important;
  flex-direction: column !important;
}

.html-editor-container.is-fullscreen .html-editor-content {
  flex: 1 !important;
  height: calc(100vh - 120px) !important;
  max-height: calc(100vh - 120px) !important;
  padding: 24px 36px !important;
  font-size: 1.02rem !important;
  line-height: 1.75 !important;
  background-color: var(--va-background-element, #ffffff) !important;
}

/* Uploading bar */
.image-uploading-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
  background: rgba(37, 99, 235, 0.08);
  border-bottom: 1px solid rgba(37, 99, 235, 0.2);
  color: var(--va-primary, #2563eb);
  font-size: 0.8rem;
  font-weight: 600;
}

/* Toolbar */
.html-editor-toolbar {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 6px 8px;
  background-color: var(--va-background-secondary, #f1f5f9);
  border-bottom: 1px solid var(--va-background-border, #e2e8f0);
  user-select: none;
}

.toolbar-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 3px;
}

.toolbar-group {
  display: flex;
  align-items: center;
  gap: 2px;
  position: relative;
}

.toolbar-divider {
  width: 1px;
  height: 18px;
  background-color: var(--va-background-border, #cbd5e1);
  margin: 0 4px;
}

/* Select Inputs */
.editor-select {
  height: 26px;
  padding: 1px 6px;
  border-radius: 4px;
  border: 1px solid var(--va-background-border, #cbd5e1);
  background-color: var(--va-background-element, #ffffff);
  color: var(--va-text-primary, #1e293b);
  font-size: 0.8rem;
  font-weight: 500;
  outline: none;
  cursor: pointer;
  transition: border-color 0.15s ease;
}

.font-family-select {
  max-width: 120px;
}

.font-size-select {
  max-width: 85px;
}

.code-lang-select {
  max-width: 95px;
  margin-left: 2px;
}

.editor-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 26px;
  height: 26px;
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid transparent;
  background: transparent;
  color: var(--va-text-primary, #1e293b);
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s ease;
}

.editor-btn:hover:not(:disabled) {
  background-color: rgba(37, 99, 235, 0.08);
  color: var(--va-primary, #2563eb);
}

.editor-btn.is-active {
  background-color: var(--va-primary, #2563eb);
  color: #ffffff;
  border-color: var(--va-primary, #2563eb);
}

.editor-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

/* Color Picker Labels */
.color-label {
  position: relative;
  cursor: pointer;
}

.hidden-color-input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
  pointer-events: none;
}

.color-indicator {
  display: inline-block;
  font-weight: 800;
  color: #ffffff;
  padding: 0 4px;
  border-radius: 2px;
  line-height: 1.2;
}

.highlight-indicator {
  font-size: 0.8rem;
  border-radius: 2px;
  padding: 0 2px;
}

/* Table Menu Popup */
.table-menu-popup {
  position: absolute;
  top: 100%;
  left: 0;
  z-index: 1000;
  margin-top: 4px;
  background-color: var(--va-background-element, #ffffff);
  border: 1px solid var(--va-background-border, #cbd5e1);
  border-radius: 6px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.15);
  padding: 4px;
  min-width: 170px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.table-menu-item {
  padding: 5px 8px;
  border-radius: 4px;
  font-size: 0.78rem;
  font-weight: 500;
  color: var(--va-text-primary, #1e293b);
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.table-menu-item:hover {
  background-color: rgba(37, 99, 235, 0.08);
  color: var(--va-primary, #2563eb);
}

.table-menu-danger:hover {
  background-color: rgba(239, 68, 68, 0.1);
  color: var(--va-danger, #ef4444);
}

.table-menu-divider {
  height: 1px;
  background-color: var(--va-background-border, #e2e8f0);
  margin: 2px 0;
}

/* Content Area */
.html-editor-content {
  padding: 12px 14px;
  color: var(--va-text-primary, #1e293b);
  font-size: 0.92rem;
  line-height: 1.65;
  cursor: text;
  overflow-y: auto;
  flex: 1;
}

.is-readonly .html-editor-content {
  cursor: default;
}

/* Footer status */
.html-editor-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 4px 10px;
  background-color: var(--va-background-secondary, #f8fafc);
  border-top: 1px solid var(--va-background-border, #e2e8f0);
  font-size: 0.75rem;
  color: var(--va-text-secondary, #94a3b8);
}

/* ProseMirror Styles */
:deep(.ProseMirror) {
  outline: none;
  min-height: 120px;
}

:deep(.ProseMirror p.is-editor-empty:first-child::before) {
  color: var(--va-text-secondary, #94a3b8);
  content: attr(data-placeholder);
  float: left;
  height: 0;
  pointer-events: none;
}

:deep(.ProseMirror img) {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin: 0.5rem 0;
  display: block;
}

:deep(.ProseMirror img.ProseMirror-selectednode) {
  outline: 2px solid var(--va-primary, #2563eb);
}

:deep(.ProseMirror h1) {
  font-size: 1.45rem;
  font-weight: 800;
  margin: 0.7rem 0 0.4rem 0;
}

:deep(.ProseMirror h2) {
  font-size: 1.25rem;
  font-weight: 700;
  margin: 0.55rem 0 0.3rem 0;
}

:deep(.ProseMirror h3) {
  font-size: 1.1rem;
  font-weight: 700;
  margin: 0.45rem 0 0.2rem 0;
}

:deep(.ProseMirror p) {
  margin: 0.3rem 0;
}

:deep(.ProseMirror ul),
:deep(.ProseMirror ol) {
  padding-left: 1.4rem;
  margin: 0.4rem 0;
}

/* Task List */
:deep(ul[data-type="taskList"]) {
  list-style: none;
  padding: 0;
  margin: 0.4rem 0;
}

:deep(ul[data-type="taskList"] li) {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 0.25rem;
}

:deep(ul[data-type="taskList"] li > label) {
  margin-top: 3px;
  user-select: none;
  cursor: pointer;
}

:deep(ul[data-type="taskList"] li > label input[type="checkbox"]) {
  cursor: pointer;
  accent-color: var(--va-primary, #2563eb);
}

:deep(ul[data-type="taskList"] li > div) {
  flex: 1;
}

/* Blockquote */
:deep(.ProseMirror blockquote) {
  border-left: 4px solid var(--va-primary, #2563eb);
  background: rgba(37, 99, 235, 0.04);
  padding: 0.5rem 0.85rem;
  border-radius: 0 6px 6px 0;
  margin: 0.6rem 0;
  color: var(--va-text-secondary, #64748b);
  font-style: italic;
}

/* Table Styles */
:deep(.ProseMirror table) {
  border-collapse: collapse;
  table-layout: fixed;
  width: 100%;
  margin: 0.75rem 0;
  overflow: hidden;
  border-radius: 4px;
}

:deep(.ProseMirror td),
:deep(.ProseMirror th) {
  min-width: 1em;
  border: 1px solid var(--va-background-border, #cbd5e1);
  padding: 6px 10px;
  vertical-align: top;
  box-sizing: border-box;
  position: relative;
}

:deep(.ProseMirror th) {
  font-weight: 700;
  text-align: left;
  background-color: var(--va-background-secondary, #f1f5f9);
}

:deep(.ProseMirror .selectedCell:after) {
  z-index: 2;
  position: absolute;
  content: "";
  left: 0; right: 0; top: 0; bottom: 0;
  background: rgba(37, 99, 235, 0.15);
  pointer-events: none;
}

/* Code Block with Syntax Highlighting */
:deep(.ProseMirror pre) {
  background-color: #0f172a;
  color: #f8fafc;
  font-family: 'Consolas', 'Courier New', monospace;
  padding: 0.85rem 1.1rem;
  border-radius: 6px;
  margin: 0.75rem 0;
  overflow-x: auto;
  font-size: 0.88rem;
  line-height: 1.5;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.3);
}

:deep(.ProseMirror pre code) {
  color: inherit;
  padding: 0;
  background: none;
  font-size: 0.88rem;
}

/* Syntax Highlighting Colors */
:deep(.hljs-keyword),
:deep(.hljs-selector-tag),
:deep(.hljs-built_in) {
  color: #38bdf8;
  font-weight: 600;
}

:deep(.hljs-string),
:deep(.hljs-title),
:deep(.hljs-section),
:deep(.hljs-attribute),
:deep(.hljs-literal),
:deep(.hljs-template-tag),
:deep(.hljs-template-variable),
:deep(.hljs-type),
:deep(.hljs-addition) {
  color: #4ade80;
}

:deep(.hljs-comment),
:deep(.hljs-quote),
:deep(.hljs-deletion),
:deep(.hljs-meta) {
  color: #94a3b8;
  font-style: italic;
}

:deep(.hljs-number),
:deep(.hljs-symbol),
:deep(.hljs-bullet) {
  color: #f59e0b;
}

:deep(.hljs-function) {
  color: #a855f7;
}

:deep(.ProseMirror hr) {
  border: none;
  border-top: 1px solid var(--va-background-border, #cbd5e1);
  margin: 1rem 0;
}

:deep(.ProseMirror a) {
  color: var(--va-primary, #2563eb);
  text-decoration: underline;
  cursor: pointer;
}
</style>
