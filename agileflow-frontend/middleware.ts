import { auth } from "@/auth"
import { NextResponse } from "next/server"

export default auth((req) => {
  const isLoggedIn = !!req.auth;
  const { pathname } = req.nextUrl;
  
  const isAuthPage = pathname.startsWith('/login') || pathname.startsWith('/register') || pathname.startsWith('/forgot-password');

  // Auth pages: redirect authenticated users away
  if (isAuthPage) {
    if (isLoggedIn) {
      const url = req.nextUrl.clone();
      url.pathname = '/';
      return NextResponse.redirect(url);
    }
    // Not logged in → let them see login/register
    return NextResponse.next();
  }

  // Protected pages: redirect unauthenticated users to login
  if (!isLoggedIn) {
    const url = req.nextUrl.clone();
    url.pathname = '/login';
    return NextResponse.redirect(url);
  }

  // Authenticated users → let them through
  return NextResponse.next();
})

export const config = {
  // Only run middleware on app pages, not on API routes, static files, images, etc.
  matcher: ['/((?!api|_next/static|_next/image|favicon.ico).*)'],
}
