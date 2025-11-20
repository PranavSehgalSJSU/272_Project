'use client';

import { useState } from 'react';
import { Rule } from '../page';
import { Play, Pause, Edit, Trash2, TestTube, Clock, Users, CheckCircle, XCircle } from 'lucide-react';

type RulesListProps = {
  rules: Rule[];
  loading: boolean;
  onEdit: (rule: Rule) => void;
  onToggle: (rule: Rule) => void;
  onDelete: (rule: Rule) => void;
  onTest: (rule: Rule) => void;
};

export function RulesList({ rules, loading, onEdit, onToggle, onDelete, onTest }: RulesListProps) {
  if (loading) {
    return (
      <div className="card p-8">
        <div className="text-center text-gray-500">
          Loading rules...
        </div>
      </div>
    );
  }

  if (rules.length === 0) {
    return (
      <div className="card p-8">
        <div className="text-center text-gray-500">
          <div className="mb-4">
            <TestTube className="h-12 w-12 mx-auto text-gray-300" />
          </div>
          <h3 className="text-lg font-medium text-gray-900 mb-2">No Alert Rules</h3>
          <p>Create your first alert rule to start monitoring conditions.</p>
        </div>
      </div>
    );
  }

  const formatLastFired = (lastFiredAt?: string) => {
    if (!lastFiredAt) return 'Never';
    return new Date(lastFiredAt).toLocaleString();
  };

  const getSourceIcon = (source: string) => {
    switch (source) {
      case 'WEATHER': return '🌤️';
      case 'STATUS': return '🔍';
      default: return '⚙️';
    }
  };

  return (
    <div className="space-y-4">
      {rules.map((rule) => (
        <div key={rule.id} className="card p-6">
          <div className="flex items-center justify-between">
            <div className="flex-1">
              <div className="flex items-center space-x-3 mb-2">
                <span className="text-2xl">{getSourceIcon(rule.source)}</span>
                <h3 className="text-lg font-semibold text-gray-900">{rule.name}</h3>
                <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                  rule.enabled 
                    ? 'bg-green-100 text-green-800' 
                    : 'bg-red-100 text-red-800'
                }`}>
                  {rule.enabled ? (
                    <><CheckCircle className="h-3 w-3 mr-1" /> Enabled</>
                  ) : (
                    <><XCircle className="h-3 w-3 mr-1" /> Disabled</>
                  )}
                </span>
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm text-gray-600">
                <div>
                  <span className="font-medium">Source:</span> {rule.source}
                </div>
                <div>
                  <span className="font-medium">Condition:</span> 
                  <code className="ml-1 px-1 bg-gray-100 rounded">{rule.condition}</code>
                </div>
                <div className="flex items-center">
                  <Clock className="h-4 w-4 mr-1" />
                  <span className="font-medium">Cooldown:</span> {rule.cooldownMinutes} min
                </div>
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-3 text-sm text-gray-600">
                <div>
                  <span className="font-medium">Channels:</span> {rule.message?.channels?.join(', ') || 'None'}
                </div>
                <div>
                  <span className="font-medium">Last Fired:</span> {formatLastFired(rule.lastFiredAt)}
                </div>
              </div>
              
              {rule.audience && (
                <div className="mt-3 text-sm text-gray-600">
                  <div className="flex items-center">
                    <Users className="h-4 w-4 mr-1" />
                    <span className="font-medium">Audience:</span>
                    {rule.audience.city && <span className="ml-1">City: {rule.audience.city}</span>}
                    {rule.audience.tags?.length > 0 && (
                      <span className="ml-2">Tags: {rule.audience.tags.join(', ')}</span>
                    )}
                  </div>
                </div>
              )}
            </div>
            
            <div className="flex items-center space-x-2 ml-4">
              <button
                onClick={() => {
                  console.log('Test button clicked for rule:', rule.name);
                  onTest(rule);
                }}
                className="p-2 text-blue-600 hover:bg-blue-50 rounded-md transition-colors"
                title="Test Rule"
              >
                <TestTube className="h-4 w-4" />
              </button>
              
              <button
                onClick={() => onToggle(rule)}
                className={`p-2 rounded-md transition-colors ${
                  rule.enabled
                    ? 'text-orange-600 hover:bg-orange-50'
                    : 'text-green-600 hover:bg-green-50'
                }`}
                title={rule.enabled ? 'Disable Rule' : 'Enable Rule'}
              >
                {rule.enabled ? <Pause className="h-4 w-4" /> : <Play className="h-4 w-4" />}
              </button>
              
              <button
                onClick={() => onEdit(rule)}
                className="p-2 text-gray-600 hover:bg-gray-50 rounded-md transition-colors"
                title="Edit Rule"
              >
                <Edit className="h-4 w-4" />
              </button>
              
              <button
                onClick={() => onDelete(rule)}
                className="p-2 text-red-600 hover:bg-red-50 rounded-md transition-colors"
                title="Delete Rule"
              >
                <Trash2 className="h-4 w-4" />
              </button>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}