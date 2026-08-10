import { writable } from 'svelte/store';

interface UIState {
  sidebarOpen: boolean;
  theme: 'dark';
}

const createUIStore = () => {
  const { subscribe, update } = writable<UIState>({
    sidebarOpen: false,
    theme: 'dark'
  });

  return {
    subscribe,
    toggleSidebar: () => update(s => ({ ...s, sidebarOpen: !s.sidebarOpen })),
    closeSidebar:  () => update(s => ({ ...s, sidebarOpen: false })),
    openSidebar:   () => update(s => ({ ...s, sidebarOpen: true }))
  };
};

export const uiStore = createUIStore();
