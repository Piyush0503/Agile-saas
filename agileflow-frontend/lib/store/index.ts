import { create } from 'zustand';

interface BoardState {
  searchQuery: string;
  setSearchQuery: (query: string) => void;
  // Add more state as needed
}

export const useBoardStore = create<BoardState>((set) => ({
  searchQuery: '',
  setSearchQuery: (query) => set({ searchQuery: query }),
}));
