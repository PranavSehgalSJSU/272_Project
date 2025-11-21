import './globals.css'
import { Inter } from 'next/font/google'

const inter = Inter({ subsets: ['latin'] })

export const metadata = {
  title: 'Emergency Alert Dashboard',
  description: 'Manage and monitor emergency alert rules',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en">
      <body className={inter.className}>
        <div className="min-h-screen bg-gray-50">
          <header className="bg-white shadow-sm border-b">
            <div className="w-full px-4 sm:px-6 lg:px-8">
              <div className="flex justify-between items-center py-4">
                <h1 className="text-2xl font-bold text-gray-900">
                  Emergency Alert Dashboard
                </h1>
                <div className="text-sm text-gray-500">
                  Real-time alert management system
                </div>
              </div>
            </div>
          </header>
          <main className="w-full">
            {children}
          </main>
        </div>
      </body>
    </html>
  )
}
