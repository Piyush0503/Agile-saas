export interface User {
  id: string;
  name: string;
  email: string;
}

export interface Issue {
  id: string;
  title: string;
  description: string;
  status: 'todo' | 'in-progress' | 'done';
}
