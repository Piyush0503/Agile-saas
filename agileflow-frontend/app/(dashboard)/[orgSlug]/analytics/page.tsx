"use client";

import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const data = [
  { name: 'To Do', count: 4 },
  { name: 'In Progress', count: 7 },
  { name: 'Done', count: 12 },
];

export default function AnalyticsPage({ params }: { params: { orgSlug: string } }) {
  return (
    <div>
      <h1 className="text-2xl font-bold mb-6 text-foreground">Analytics</h1>
      <div className="bg-card p-6 rounded-lg shadow-sm border h-96">
        <h2 className="text-lg font-medium mb-4">Issues by Status</h2>
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={data}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis />
            <Tooltip />
            <Legend />
            <Bar dataKey="count" fill="#3b82f6" />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
