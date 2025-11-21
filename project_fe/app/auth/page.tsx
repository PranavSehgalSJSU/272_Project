'use client';

import { useState } from 'react';
import { User, Mail, Lock, Phone, Eye, EyeOff } from 'lucide-react';

type AuthMode = 'login' | 'signup';

export default function AuthPage() {
  const [mode, setMode] = useState<AuthMode>('login');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    email: '',
    phone: '',
    allowAlerts: true
  });

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const endpoint = mode === 'login' ? '/api/auth/login' : '/api/auth/signup';
      const body = mode === 'login' 
        ? { username: formData.username, password: formData.password }
        : formData;

      const response = await fetch(`http://localhost:8080/auth/${mode}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(body),
      });

      const data = await response.text();

      if (response.ok) {
        if (mode === 'login') {
          // Parse JSON response for login
          const loginData = JSON.parse(data);
          localStorage.setItem('authToken', loginData.token);
          localStorage.setItem('isAdmin', loginData.isAdmin);
          localStorage.setItem('username', loginData.username);
          localStorage.setItem('email', loginData.email);
          
          // Redirect based on admin status
          if (loginData.isAdmin) {
            window.location.href = '/dashboard';
          } else {
            window.location.href = '/user-portal';
          }
        } else {
          // Signup success
          alert('Registration successful! Please check your email for verification.');
          setMode('login');
          setFormData({
            username: '',
            password: '',
            email: '',
            phone: '',
            allowAlerts: true
          });
        }
      } else {
        setError(data);
      }
    } catch (error) {
      setError('Network error. Please try again.');
      console.error('Auth error:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
          <div className="sm:mx-auto sm:w-full sm:max-w-md mb-8">
            <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
              Emergency Alert System
            </h2>
            <p className="mt-2 text-center text-sm text-gray-600">
              {mode === 'login' ? 'Sign in to your account' : 'Create a new account'}
            </p>
          </div>

          <form className="space-y-6" onSubmit={handleSubmit}>
            {error && (
              <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
                {error}
              </div>
            )}

            <div>
              <label htmlFor="username" className="block text-sm font-medium text-gray-700">
                Username
              </label>
              <div className="mt-1 relative">
                <input
                  id="username"
                  name="username"
                  type="text"
                  required
                  className="appearance-none block w-full px-3 py-2 pl-10 border border-gray-300 rounded-md placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Enter username"
                  value={formData.username}
                  onChange={handleInputChange}
                />
                <User className="h-5 w-5 text-gray-400 absolute left-3 top-2.5" />
              </div>
            </div>

            {mode === 'signup' && (
              <div>
                <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                  Email address
                </label>
                <div className="mt-1 relative">
                  <input
                    id="email"
                    name="email"
                    type="email"
                    required
                    className="appearance-none block w-full px-3 py-2 pl-10 border border-gray-300 rounded-md placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    placeholder="Enter email"
                    value={formData.email}
                    onChange={handleInputChange}
                  />
                  <Mail className="h-5 w-5 text-gray-400 absolute left-3 top-2.5" />
                </div>
              </div>
            )}

            {mode === 'signup' && (
              <div>
                <label htmlFor="phone" className="block text-sm font-medium text-gray-700">
                  Phone number (optional)
                </label>
                <div className="mt-1 relative">
                  <input
                    id="phone"
                    name="phone"
                    type="tel"
                    className="appearance-none block w-full px-3 py-2 pl-10 border border-gray-300 rounded-md placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    placeholder="Enter phone number"
                    value={formData.phone}
                    onChange={handleInputChange}
                  />
                  <Phone className="h-5 w-5 text-gray-400 absolute left-3 top-2.5" />
                </div>
              </div>
            )}

            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                Password
              </label>
              <div className="mt-1 relative">
                <input
                  id="password"
                  name="password"
                  type={showPassword ? 'text' : 'password'}
                  required
                  className="appearance-none block w-full px-3 py-2 pl-10 pr-10 border border-gray-300 rounded-md placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Enter password"
                  value={formData.password}
                  onChange={handleInputChange}
                />
                <Lock className="h-5 w-5 text-gray-400 absolute left-3 top-2.5" />
                <button
                  type="button"
                  className="absolute right-3 top-2.5"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? (
                    <EyeOff className="h-5 w-5 text-gray-400" />
                  ) : (
                    <Eye className="h-5 w-5 text-gray-400" />
                  )}
                </button>
              </div>
            </div>

            {mode === 'signup' && (
              <div className="flex items-center">
                <input
                  id="allowAlerts"
                  name="allowAlerts"
                  type="checkbox"
                  checked={formData.allowAlerts}
                  onChange={handleInputChange}
                  className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label htmlFor="allowAlerts" className="ml-2 block text-sm text-gray-700">
                  I want to receive emergency alerts
                </label>
              </div>
            )}

            <div>
              <button
                type="submit"
                disabled={loading}
                className="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? 'Please wait...' : (mode === 'login' ? 'Sign in' : 'Sign up')}
              </button>
            </div>

            <div className="text-center">
              <button
                type="button"
                onClick={() => setMode(mode === 'login' ? 'signup' : 'login')}
                className="font-medium text-blue-600 hover:text-blue-500"
              >
                {mode === 'login' 
                  ? "Don't have an account? Sign up" 
                  : "Already have an account? Sign in"}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}