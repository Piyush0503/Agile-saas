export default function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-board">
      <div className="w-full max-w-md p-8 bg-card rounded-lg shadow-sm border">
        {children}
      </div>
    </div>
  );
}
