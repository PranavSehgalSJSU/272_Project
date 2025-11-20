'use client';

import { useEffect } from 'react';
import { Bell, AlertTriangle, Users } from 'lucide-react';

export default function HomePage() {
  useEffect(() => {
    // Check if user is already logged in
    const token = localStorage.getItem('authToken');
    const isAdmin = localStorage.getItem('isAdmin') === 'true';
    
    if (token) {
      if (isAdmin) {
        window.location.href = '/dashboard';
      } else {
        window.location.href = '/user-portal';
      }
    } else {
      // Redirect to auth after a brief delay to show landing page
      setTimeout(() => {
        window.location.href = '/auth';
      }, 3000);
    }
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="text-center">
          <Bell className="mx-auto h-16 w-16 text-blue-600" />
          <h2 className="mt-6 text-3xl font-extrabold text-gray-900">
            Emergency Alert System
          </h2>
          <p className="mt-2 text-sm text-gray-600">
            Stay informed, stay safe
          </p>
        </div>

        <div className="mt-8 bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
          <div className="space-y-6">
            <div className="text-center">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
              <p className="mt-2 text-sm text-gray-600">Redirecting to login...</p>
            </div>

            <div className="grid grid-cols-1 gap-4">
              <div className="bg-blue-50 p-4 rounded-lg">
                <div className="flex items-center">
                  <AlertTriangle className="h-5 w-5 text-blue-600 mr-3" />
                  <div>
                    <h3 className="text-sm font-medium text-blue-800">For Administrators</h3>
                    <p className="text-xs text-blue-700">Create and manage alert rules, monitor system activity</p>
                  </div>
                </div>
              </div>
              
              <div className="bg-green-50 p-4 rounded-lg">
                <div className="flex items-center">
                  <Users className="h-5 w-5 text-green-600 mr-3" />
                  <div>
                    <h3 className="text-sm font-medium text-green-800">For Users</h3>
                    <p className="text-xs text-green-700">Receive emergency alerts and notifications</p>
                  </div>
                </div>
              </div>
            </div>
            
            <div className="text-center">
              <button
                onClick={() => window.location.href = '/auth'}
                className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
              >
                Continue to Login
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}