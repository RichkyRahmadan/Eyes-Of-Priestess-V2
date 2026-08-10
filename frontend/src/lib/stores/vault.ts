import { writable, derived } from 'svelte/store';
import type { Treasury, Chronicle } from '$lib/types/vault';

interface VaultState {
  treasury: Treasury | null;
  chronicles: Chronicle[];
  chroniclePage: number;
  hasMoreChronicles: boolean;
  isLoading: boolean;
}

const createVaultStore = () => {
  const { subscribe, set, update } = writable<VaultState>({
    treasury: null,
    chronicles: [],
    chroniclePage: 0,
    hasMoreChronicles: true,
    isLoading: false
  });

  return {
    subscribe,
    setTreasury:   (treasury: Treasury)     => update(s => ({ ...s, treasury })),
    setChronicles: (chronicles: Chronicle[]) => update(s => ({ ...s, chronicles, chroniclePage: 0 })),
    appendChronicles: (more: Chronicle[]) =>
      update(s => ({ ...s, chronicles: [...s.chronicles, ...more], chroniclePage: s.chroniclePage + 1, hasMoreChronicles: more.length > 0 })),
    prependChronicle: (c: Chronicle) => update(s => ({ ...s, chronicles: [c, ...s.chronicles] })),
    setLoading: (isLoading: boolean) => update(s => ({ ...s, isLoading })),
    clear: () => set({ treasury: null, chronicles: [], chroniclePage: 0, hasMoreChronicles: true, isLoading: false })
  };
};

export const vaultStore = createVaultStore();
export const treasury = derived(vaultStore, $v => $v.treasury);
