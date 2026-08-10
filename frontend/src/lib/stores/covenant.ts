import { writable } from 'svelte/store';
import type { CovenantResponse } from '$lib/types/covenant';

interface CovenantState {
  covenants: CovenantResponse[];
  current: CovenantResponse | null;
  isLoading: boolean;
}

const createCovenantStore = () => {
  const { subscribe, set, update } = writable<CovenantState>({
    covenants: [],
    current: null,
    isLoading: false
  });

  return {
    subscribe,
    setCovenants: (covenants: CovenantResponse[]) => update(s => ({ ...s, covenants })),
    setCurrent:   (current: CovenantResponse | null) => update(s => ({ ...s, current })),
    updateCurrent: (patch: Partial<CovenantResponse>) =>
      update(s => ({
        ...s,
        current: s.current ? { ...s.current, ...patch } : null,
        covenants: s.covenants.map(c => c.id === patch.id ? { ...c, ...patch } : c)
      })),
    setLoading: (isLoading: boolean) => update(s => ({ ...s, isLoading })),
    clear: () => set({ covenants: [], current: null, isLoading: false })
  };
};

export const covenantStore = createCovenantStore();
