import NextAuth from "next-auth"
import CredentialsProvider from "next-auth/providers/credentials"

export const { handlers, auth, signIn, signOut } = NextAuth({
  providers: [
    CredentialsProvider({
      name: "Credentials",
      credentials: {
        email: { label: "Email", type: "email" },
        password: { label: "Password", type: "password" }
      },
      async authorize(credentials) {
        // Implement your own authentication logic here
        // e.g. make an API call to your backend
        if (credentials?.email && credentials?.password) {
          return { id: "1", name: "User", email: credentials.email as string }
        }
        return null
      }
    })
  ],
  callbacks: {
    async jwt({ token, user }) {
      if (user) {
        token.id = user.id
        // token.accessToken = user.accessToken; // Example
      }
      return token
    },
    async session({ session, token }) {
      if (session.user) {
        session.user.id = token.id as string
        // (session.user as any).accessToken = token.accessToken; // Example
      }
      return session
    }
  },
  pages: {
    signIn: '/login',
  }
})
