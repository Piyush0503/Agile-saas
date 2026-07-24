import NextAuth from "next-auth"
import CredentialsProvider from "next-auth/providers/credentials"
import GoogleProvider from "next-auth/providers/google"
import GitHubProvider from "next-auth/providers/github"
import { JWT } from "next-auth/jwt"

async function refreshAccessToken(token: JWT): Promise<JWT> {
  try {
    const url = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"
    const response = await fetch(`${url}/api/auth/refresh`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        refreshToken: token.refreshToken,
      }),
    })

    const refreshedTokens = await response.json()

    if (!response.ok) {
      throw refreshedTokens
    }

    return {
      ...token,
      accessToken: refreshedTokens.accessToken,
      refreshToken: refreshedTokens.refreshToken ?? token.refreshToken, // Fall back to old refresh token
      // We parse the new JWT to get its expiration, but for now let's set a default 15 min
      accessTokenExpires: Date.now() + 15 * 60 * 1000,
    }
  } catch (error) {
    console.error("Error refreshing access token", error)
    return {
      ...token,
      error: "RefreshAccessTokenError",
    }
  }
}

export const { handlers, auth, signIn, signOut } = NextAuth({
  pages: {
    signIn: "/login",
  },
  providers: [
    GoogleProvider({
      clientId: process.env.GOOGLE_CLIENT_ID,
      clientSecret: process.env.GOOGLE_CLIENT_SECRET,
    }),
    GitHubProvider({
      clientId: process.env.GITHUB_CLIENT_ID,
      clientSecret: process.env.GITHUB_CLIENT_SECRET,
    }),
    CredentialsProvider({
      name: "Credentials",
      credentials: {
        email: { label: "Email", type: "email" },
        password: { label: "Password", type: "password" },
        orgSlug: { label: "Organization Slug", type: "text" },
      },
      async authorize(credentials) {
        if (!credentials?.email || !credentials?.password) {
          return null
        }

        try {
          const url = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"
          const res = await fetch(`${url}/api/auth/login`, {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify({
              email: credentials.email,
              password: credentials.password,
              orgSlug: credentials.orgSlug,
            }),
          })

          const data = await res.json()

          if (res.ok && data) {
            return {
              id: data.user.id,
              name: data.user.name,
              email: data.user.email,
              image: data.user.avatarUrl,
              accessToken: data.accessToken,
              refreshToken: data.refreshToken,
              // Assuming roles and orgSlug are returned in login response or decoded from JWT
              // If not, we just rely on the JWT decoding later or backend providing it
            }
          }
          return null
        } catch (e) {
          console.error("Login failed", e)
          return null
        }
      },
    }),
  ],
  callbacks: {
    async jwt({ token, user, account }) {
      // Initial sign in
      if (user && account) {
        if (account.provider === "credentials") {
          return {
            ...token,
            id: user.id,
            accessToken: user.accessToken,
            refreshToken: user.refreshToken,
            accessTokenExpires: Date.now() + 15 * 60 * 1000,
            roles: user.roles,
            orgSlug: user.orgSlug,
            orgId: user.orgId,
          }
        } else {
          // TODO: For Google/GitHub, you should call your Spring Boot backend to exchange
          // the OAuth token for your custom JWT, then set token.accessToken = response.accessToken
          return { ...token, id: user.id }
        }
      }

      // Return previous token if the access token has not expired yet
      if (Date.now() < token.accessTokenExpires) {
        return token
      }

      // Access token has expired, try to update it
      return refreshAccessToken(token)
    },
    async session({ session, token }) {
      if (token) {
        session.user.id = token.id
        session.user.accessToken = token.accessToken
        session.user.roles = token.roles
        session.user.orgSlug = token.orgSlug
        session.user.orgId = token.orgId
        session.error = token.error
      }
      return session
    },
  },
})
