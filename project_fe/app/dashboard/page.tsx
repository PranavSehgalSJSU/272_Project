'use client';

import { useState, useEffect } from 'react';
import { RulesList } from '../components/RulesList';
import { RuleForm } from '../components/RuleForm';
import { EventsList } from '../components/EventsList';
import { TestRuleModal } from '../components/TestRuleModal';
import { Plus, Shield, LogOut, Settings } from 'lucide-react';

export type Rule = {
  id?: string;
  name: string;
  source: 'WEATHER' | 'STATUS' | 'CUSTOM';
  params: Record<string, any>;
  condition: string;
  message: {
    header: string;
    content: string;
    channels: string[];
  };
  audience: {
    tags: string[];
    city: string;
  };
  cooldownMinutes: number;
  lastFiredAt?: string;
  enabled: boolean;
};

export type Event = {
  id: string;
  ruleId: string;
  ruleName: string;
  payload: Record<string, any>;
  firedAt: string;
  recipients: number;
  channelResults: Record<string, any>;
};

export default function AdminDashboard() {
  const [user, setUser] = useState<any>(null);
  const [activeTab, setActiveTab] = useState<'rules' | 'events'>('rules');
  const [showRuleForm, setShowRuleForm] = useState(false);
  const [editingRule, setEditingRule] = useState<Rule | null>(null);
  const [testingRule, setTestingRule] = useState<Rule | null>(null);
  const [rules, setRules] = useState<Rule[]>([]);
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check authentication and admin status
    const token = localStorage.getItem('authToken');
    const isAdmin = localStorage.getItem('isAdmin') === 'true';
    const username = localStorage.getItem('username');
    const email = localStorage.getItem('email');

    if (!token) {
      window.location.href = '/auth';
      return;
    }

    if (!isAdmin) {
      window.location.href = '/user-portal';
      return;
    }

    setUser({ username, email, isAdmin: true });
  }, []);

  useEffect(() => {
    if (user && activeTab === 'rules') {
      loadRules();
    } else if (user && activeTab === 'events') {
      loadEvents();
    }
  }, [activeTab, user]);

  const loadRules = async () => {
    try {
      setLoading(true);
      const response = await fetch('http://localhost:8080/api/rules');
      if (response.ok) {
        const data = await response.json();
        setRules(data);
      }
    } catch (error) {
      console.error('Error loading rules:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadEvents = async () => {
    try {
      setLoading(true);
      const response = await fetch('http://localhost:8080/api/events?limit=50');
      if (response.ok) {
        const data = await response.json();
        setEvents(data);
      }
    } catch (error) {
      console.error('Error loading events:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateRule = () => {
    setEditingRule(null);
    setShowRuleForm(true);
  };

  const handleEditRule = (rule: Rule) => {
    setEditingRule(rule);
    setShowRuleForm(true);
  };

  const handleRuleSubmit = async (rule: Rule) => {
    try {
      const method = editingRule ? 'PUT' : 'POST';
      const url = editingRule ? `http://localhost:8080/api/rules/${editingRule.id}` : 'http://localhost:8080/api/rules';
      
      const response = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(rule),
      });

      if (response.ok) {
        setShowRuleForm(false);
        setEditingRule(null);
        loadRules();
      }
    } catch (error) {
      console.error('Error saving rule:', error);
    }
  };

  const handleToggleRule = async (rule: Rule) => {
    try {
      const response = await fetch(`http://localhost:8080/api/rules/${rule.id}/toggle`, {
        method: 'PATCH',
      });
      if (response.ok) {
        loadRules();
      }
    } catch (error) {
      console.error('Error toggling rule:', error);
    }
  };

  const handleDeleteRule = async (rule: Rule) => {
    if (!confirm(`Are you sure you want to delete "${rule.name}"?`)) return;
    
    try {
      const response = await fetch(`http://localhost:8080/api/rules/${rule.id}`, {
        method: 'DELETE',
      });
      if (response.ok) {
        loadRules();
      }
    } catch (error) {
      console.error('Error deleting rule:', error);
    }
  };

  const handleTestRule = (rule: Rule) => {
    console.log('Testing rule:', rule.name);
    setTestingRule(rule);
  };

  const handleLogout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('isAdmin');
    localStorage.removeItem('username');
    localStorage.removeItem('email');
    window.location.href = '/auth';
  };

  if (!user) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
          <p className="mt-4 text-gray-600">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center">
              <Shield className="h-8 w-8 text-blue-600 mr-3" />
              <div>
                <h1 className="text-xl font-semibold text-gray-900">Admin Dashboard</h1>
                <p className="text-sm text-gray-500">Emergency Alert System</p>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <div className="flex items-center">
                <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
                  <Shield className="h-3 w-3 mr-1" />
                  Admin
                </span>
                <span className="ml-2 text-sm text-gray-700">{user.username}</span>
              </div>
              <button
                onClick={handleLogout}
                className="inline-flex items-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
              >
                <LogOut className="h-4 w-4 mr-2" />
                Sign out
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          <div className="space-y-6">
            {/* Navigation Tabs */}
            <div className="border-b border-gray-200">
              <nav className="-mb-px flex space-x-8">
                <button
                  onClick={() => setActiveTab('rules')}
                  className={`py-2 px-1 border-b-2 font-medium text-sm ${
                    activeTab === 'rules'
                      ? 'border-blue-500 text-blue-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }`}
                >
                  Alert Rules ({rules.length})
                </button>
                <button
                  onClick={() => setActiveTab('events')}
                  className={`py-2 px-1 border-b-2 font-medium text-sm ${
                    activeTab === 'events'
                      ? 'border-blue-500 text-blue-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }`}
                >
                  Recent Events ({events.length})
                </button>
              </nav>
            </div>

            {/* Tab Content */}
            {activeTab === 'rules' && (
              <div className="space-y-4">
                <div className="flex justify-between items-center">
                  <h2 className="text-xl font-semibold text-gray-900">Alert Rules</h2>
                  <button
                    onClick={handleCreateRule}
                    className="inline-flex items-center px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
                  >
                    <Plus className="h-4 w-4 mr-2" />
                    New Rule
                  </button>
                </div>

                <RulesList
                  rules={rules}
                  loading={loading}
                  onEdit={handleEditRule}
                  onToggle={handleToggleRule}
                  onDelete={handleDeleteRule}
                  onTest={handleTestRule}
                />
              </div>
            )}

            {activeTab === 'events' && (
              <div className="space-y-4">
                <h2 className="text-xl font-semibold text-gray-900">Recent Events</h2>
                <EventsList onRefresh={loadEvents} />
              </div>
            )}

            {/* Modals */}
            {showRuleForm && (
              <RuleForm
                rule={editingRule}
                onSubmit={handleRuleSubmit}
                onCancel={() => {
                  setShowRuleForm(false);
                  setEditingRule(null);
                }}
              />
            )}

            {testingRule && (
              <TestRuleModal
                rule={testingRule}
                isOpen={!!testingRule}
                onClose={() => setTestingRule(null)}
              />
            )}
          </div>
        </div>
      </div>
    </div>
  );
}