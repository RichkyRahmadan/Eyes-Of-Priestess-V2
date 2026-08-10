<script lang="ts">
  import { Gem } from "@lucide/svelte";
</script>

<div class="loading-crystal">
  <div class="crystal-outer">
    <div class="crystal-inner">
      <Gem size={28} color="var(--gold-400)" />
    </div>
  </div>
  <p class="loading-text">Channeling the Covenant…</p>
</div>

<style>
.loading-crystal {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  padding: 3rem;
}

.crystal-outer {
  width: 64px; height: 64px;
  border-radius: 50%;
  background: rgba(139,92,246,0.08);
  display: flex; align-items: center; justify-content: center;
  animation: pulse-glow 2s ease-in-out infinite;
}

.crystal-inner {
  width: 48px; height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--void-700), var(--void-900));
  display: flex; align-items: center; justify-content: center;
  animation: crystal-spin 3s linear infinite;
  border: 1px solid var(--border-gold);
}

.loading-text {
  font-size: 0.875rem;
  color: var(--text-muted);
  font-style: italic;
}

@keyframes pulse-glow {
  0%, 100% { box-shadow: 0 0 8px rgba(139,92,246,0.3); }
  50%       { box-shadow: 0 0 24px rgba(139,92,246,0.6); }
}

@keyframes crystal-spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}
</style>

