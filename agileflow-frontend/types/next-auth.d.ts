import "next-auth"
import "next-auth/jwt"

declare module "next-auth" {
  interface Session {
    user: {
      id: string
      name?: string | null
      email?: string | null
      image?: string | null
      roles?: string[]
      orgSlug?: string | null
      orgId?: string | null
      accessToken: string
    }
    error?: "RefreshAccessTokenError"
  }

  interface User {
    id: string
    name?: string | null
    email?: string | null
    image?: string | null
    accessToken: string
    refreshToken: string
    roles?: string[]
    orgSlug?: string | null
    orgId?: string | null
  }
}

declare module "next-auth/jwt" {
  interface JWT {
    id: string
    accessToken: string
    refreshToken: string
    accessTokenExpires: number
    roles?: string[]
    orgSlug?: string | null
    orgId?: string | null
    error?: "RefreshAccessTokenError"
  }
}
