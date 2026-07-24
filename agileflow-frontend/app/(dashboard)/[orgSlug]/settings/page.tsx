export default function SettingsPage({ params }: { params: { orgSlug: string } }) {
  return (
    <div>
      <h1 className="text-2xl font-bold mb-6 text-foreground">Settings</h1>
      <div className="bg-card p-6 rounded-lg shadow-sm border">
        <h2 className="text-lg font-medium mb-4">Organization Settings</h2>
        {/* Settings form goes here */}
      </div>
    </div>
  );
}
