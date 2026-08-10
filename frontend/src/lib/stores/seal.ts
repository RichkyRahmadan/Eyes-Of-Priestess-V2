import { writable, derived } from 'svelte/store';
import type { Pilgrim, SealState } from '$lib/types/pilgrim';

const createSealStore = () => {
  const { subscribe, set, update } = writable<SealState>({
    pilgrim: null,
    accessSeal: null,
    refreshSeal: null,
    isAuthenticated: false,
    isLoading: true
  });

  return {
    subscribe,

    /** Called on login success */
    attune: (pilgrim: Pilgrim, accessSeal: string, refreshSeal: string) => {
      if (typeof localStorage !== 'undefined') {
        localStorage.setItem('accessSeal', accessSeal);
        localStorage.setItem('refreshSeal', refreshSeal);
      }
      set({ pilgrim, accessSeal, refreshSeal, isAuthenticated: true, isLoading: false });
    },

    /** Called on logout */
    sever: () => {
      if (typeof localStorage !== 'undefined') {
        localStorage.removeItem('accessSeal');
        localStorage.removeItem('refreshSeal');
      }
      set({ pilgrim: null, accessSeal: null, refreshSeal: null, isAuthenticated: false, isLoading: false });
    },

    setPilgrim: (pilgrim: Pilgrim) => update(s => ({ ...s, pilgrim })),
    setLoading:  (isLoading: boolean) => update(s => ({ ...s, isLoading })),

    /** Restore session from localStorage on app load */
    init: () => {
      if (typeof localStorage === 'undefined') {
        update(s => ({ ...s, isLoading: false }));
        return;
      }
      const accessSeal  = localStorage.getItem('accessSeal');
      const refreshSeal = localStorage.getItem('refreshSeal');
      if (accessSeal) {
        update(s => ({ ...s, accessSeal, refreshSeal, isLoading: false }));
        // Token is loaded; pilgrim profile will be fetched by layout
      } else {
        update(s => ({ ...s, isLoading: false }));
      }
    },

    updateSeal: (accessSeal: string) => {
      if (typeof localStorage !== 'undefined') {
        localStorage.setItem('accessSeal', accessSeal);
      }
      update(s => ({ ...s, accessSeal }));
    }
  };
};

export const sealStore = createSealStore();

export const isAuthenticated = derived(sealStore, $s => $s.isAuthenticated);
export const currentPilgrim  = derived(sealStore, $s => $s.pilgrim);
export const isOracle = derived(sealStore, $s => $s.pilgrim?.role === 'ORACLE');
