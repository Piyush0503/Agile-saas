import { auth } from "@/auth";
import { redirect } from "next/navigation";

export default async function Home() {
  const session = await auth();

  if (!session) {
    redirect("/login");
  }

  // If user is authenticated, redirect to their org board or a default dashboard
  const orgSlug = session.user?.orgSlug;
  if (orgSlug) {
    redirect(`/${orgSlug}/board`);
  }

  // If no org yet, show a simple dashboard/onboarding page
  return (
    <div className="min-h-screen flex items-center justify-center bg-[#0a0a0b] text-white">
      <div className="text-center space-y-4 p-8 bg-[#111113] rounded-2xl border border-zinc-800 max-w-md">
        <h1 className="text-2xl font-bold">Welcome to AgileFlow</h1>
        <p className="text-zinc-400">
          You are signed in! Create or join an organization to get started.
        </p>
      </div>
    </div>
  );
}
