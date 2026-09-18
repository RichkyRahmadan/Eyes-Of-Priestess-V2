<script lang="ts">
  interface UploadedFile {
    id: string;
    file: File;
    name: string;
    size: number;
    type: string;
    url: string;
    status: 'uploading' | 'completed' | 'error';
    progress: number;
  }

  interface Props {
    label?: string;
    hint?: string;
    accept?: string;
    maxSizeMb?: number;
    multiple?: boolean;
    disabled?: boolean;
    required?: boolean;
    error?: string;
    class?: string;
    files?: UploadedFile[];
    onfileschange?: (files: UploadedFile[]) => void;
  }

  let {
    label = 'Unggah Berkas Bukti / Dokumen',
    hint = 'Format didukung: JPG, PNG, WEBP, atau PDF (Maks. 10MB)',
    accept = 'image/jpeg,image/png,image/webp,application/pdf',
    maxSizeMb = 10,
    multiple = true,
    disabled = false,
    required = false,
    error: externalError = '',
    class: className = '',
    files = $bindable([]),
    onfileschange
  }: Props = $props();

  let isDragging = $state(false);
  let internalError = $state('');
  let fileInputRef = $state<HTMLInputElement | null>(null);

  const displayError = $derived(externalError || internalError);

  function formatBytes(bytes: number): string {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  }

  function validateAndAddFiles(incoming: FileList | File[]) {
    internalError = '';
    const fileArray = Array.from(incoming);

    for (const file of fileArray) {
      if (file.size > maxSizeMb * 1024 * 1024) {
        internalError = `Ukuran file ${file.name} melebihi batas ${maxSizeMb}MB.`;
        return;
      }

      // Check allowed extensions/mimes
      const isPdf = file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf');
      const isImg = file.type.startsWith('image/') || /\.(jpg|jpeg|png|webp)$/i.test(file.name);

      if (!isPdf && !isImg) {
        internalError = `Format file ${file.name} tidak valid. Hanya gambar dan PDF yang diizinkan.`;
        return;
      }

      const fileId = crypto.randomUUID();
      const reader = new FileReader();

      const newUploadedFile: UploadedFile = {
        id: fileId,
        file,
        name: file.name,
        size: file.size,
        type: file.type || (isPdf ? 'application/pdf' : 'image/jpeg'),
        url: '',
        status: 'uploading',
        progress: 30
      };

      if (!multiple) {
        files = [newUploadedFile];
      } else {
        files = [...files, newUploadedFile];
      }

      reader.onload = (e) => {
        const result = e.target?.result as string;
        files = files.map((item) => {
          if (item.id === fileId) {
            return {
              ...item,
              url: result,
              status: 'completed',
              progress: 100
            };
          }
          return item;
        });
        onfileschange?.(files);
      };

      reader.onerror = () => {
        files = files.map((item) => {
          if (item.id === fileId) {
            return { ...item, status: 'error', progress: 0 };
          }
          return item;
        });
      };

      reader.readAsDataURL(file);
    }
    onfileschange?.(files);
  }

  function handleDrop(e: DragEvent) {
    e.preventDefault();
    isDragging = false;
    if (disabled) return;
    if (e.dataTransfer?.files && e.dataTransfer.files.length > 0) {
      validateAndAddFiles(e.dataTransfer.files);
    }
  }

  function handleDragOver(e: DragEvent) {
    e.preventDefault();
    if (!disabled) isDragging = true;
  }

  function handleDragLeave() {
    isDragging = false;
  }

  function handleFileInputChange(e: Event) {
    const target = e.target as HTMLInputElement;
    if (target.files && target.files.length > 0) {
      validateAndAddFiles(target.files);
      target.value = '';
    }
  }

  function removeFile(id: string) {
    files = files.filter((f) => f.id !== id);
    onfileschange?.(files);
  }
</script>

<div class="flex flex-col gap-2 {className}">
  {#if label}
    <div class="flex items-center justify-between">
      <span class="font-sans text-[var(--text-caption)] font-medium text-[var(--color-body)]">
        {label}
        {#if required}
          <span class="text-[var(--color-primary)]">*</span>
        {/if}
      </span>
      {#if files.length > 0}
        <span class="font-sans text-[11px] text-[var(--color-muted)]">
          {files.length} file dipilih
        </span>
      {/if}
    </div>
  {/if}

  <!-- Drop Zone -->
  <button
    type="button"
    aria-label="Upload file dropzone"
    class="w-full relative border-2 border-dashed rounded-[var(--radius-lg)] p-6 transition-all duration-200 flex flex-col items-center justify-center text-center cursor-pointer select-none {isDragging
      ? 'border-[var(--color-primary)] bg-[var(--color-primary)]/5'
      : 'border-[var(--color-hairline)] bg-[var(--color-subtle)] hover:border-[var(--color-muted-soft)] hover:bg-[var(--color-canvas)]'} {disabled ? 'opacity-50 cursor-not-allowed' : ''}"
    ondragover={handleDragOver}
    ondragleave={handleDragLeave}
    ondrop={handleDrop}
    onclick={() => !disabled && fileInputRef?.click()}
    {disabled}
  >
    <input
      type="file"
      {accept}
      {multiple}
      {disabled}
      bind:this={fileInputRef}
      onchange={handleFileInputChange}
      class="hidden"
      id="file-upload-input"
    />

    <div class="w-12 h-12 mb-3 rounded-full bg-[var(--color-primary)]/10 text-[var(--color-primary)] flex items-center justify-center pointer-events-none">
      <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.75" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
      </svg>
    </div>

    <p class="font-sans text-[var(--text-body-sm)] font-medium text-[var(--color-ink)] pointer-events-none">
      Tarik & letakkan dokumen di sini, atau <span class="text-[var(--color-primary)] underline">telusuri</span>
    </p>
    {#if hint}
      <p class="font-sans text-[11px] text-[var(--color-muted)] mt-1 pointer-events-none">{hint}</p>
    {/if}
  </button>

  {#if displayError}
    <p class="font-sans text-[var(--text-caption)] text-[var(--color-error)] mt-1">{displayError}</p>
  {/if}

  <!-- Uploaded Files List -->
  {#if files.length > 0}
    <div class="flex flex-col gap-2 mt-2">
      {#each files as f (f.id)}
        <div class="flex items-center justify-between p-3 bg-[var(--color-canvas)] border border-[var(--color-hairline)] rounded-[var(--radius-md)]">
          <div class="flex items-center gap-3 min-w-0">
            {#if f.type === 'application/pdf' || f.name.toLowerCase().endsWith('.pdf')}
              <div class="w-10 h-10 rounded-[var(--radius-sm)] bg-red-500/10 text-red-500 flex items-center justify-center shrink-0 font-display text-[11px] font-bold">
                PDF
              </div>
            {:else if f.url}
              <img src={f.url} alt={f.name} class="w-10 h-10 rounded-[var(--radius-sm)] object-cover shrink-0 border border-[var(--color-hairline)]" />
            {:else}
              <div class="w-10 h-10 rounded-[var(--radius-sm)] bg-[var(--color-primary)]/10 text-[var(--color-primary)] flex items-center justify-center shrink-0">
                <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
            {/if}

            <div class="min-w-0">
              <p class="font-sans text-[var(--text-body-sm)] font-medium text-[var(--color-ink)] truncate max-w-[200px] sm:max-w-xs">
                {f.name}
              </p>
              <div class="flex items-center gap-2 font-sans text-[11px] text-[var(--color-muted)]">
                <span>{formatBytes(f.size)}</span>
                {#if f.status === 'completed'}
                  <span class="text-[var(--color-success)] flex items-center gap-1">
                    <svg class="w-3.5 h-3.5" viewBox="0 0 20 20" fill="currentColor">
                      <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                    </svg>
                    Siap
                  </span>
                {:else if f.status === 'uploading'}
                  <span class="text-[var(--color-primary)] animate-pulse">Mengunggah...</span>
                {:else if f.status === 'error'}
                  <span class="text-[var(--color-error)]">Gagal</span>
                {/if}
              </div>
            </div>
          </div>

          <button
            type="button"
            onclick={() => removeFile(f.id)}
            class="p-1.5 text-[var(--color-muted)] hover:text-[var(--color-error)] hover:bg-[var(--color-error)]/10 rounded-[var(--radius-sm)] transition-colors"
            title="Hapus file"
          >
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      {/each}
    </div>
  {/if}
</div>
