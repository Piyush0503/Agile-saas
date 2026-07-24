export default function DashboardRootLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-screen bg-board">
      <div className="w-64 bg-sidebar text-white flex-shrink-0">
        <div className="p-4 font-bold text-xl border-b border-sidebar-active">AgileFlow</div>
        <nav className="p-4 space-y-2">
          {/* Navigation will go here */}
        </nav>
      </div>
      <main className="flex-1 flex flex-col min-h-screen overflow-hidden">
        <header className="h-14 border-b bg-card flex items-center px-6">
          <div className="flex-1"></div>
          <div>User Profile</div>
        </header>
        <div className="flex-1 overflow-auto p-6">
          {children}
        </div>
      </main>
    </div>
  );
}
