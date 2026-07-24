import { redirect } from "next/navigation";

export default function Home() {
  // Normally check auth here and redirect to /login or /orgSlug/board
  redirect("/login");
}
