export default function BoardPage({ params }: { params: { orgSlug: string } }) {
  return (
    <div>
      <h1 className="text-2xl font-bold mb-6 text-foreground">Kanban Board</h1>
      {/* Board component goes here */}
    </div>
  );
}
