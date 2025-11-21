'use client';

import { useState } from 'react';
import { Rule } from '../page';
import { Play, Pause, Edit, Trash2, TestTube, Clock, CheckCircle, XCircle } from 'lucide-react';

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

  const getSourceIcon = (source: string, ruleName?: string, condition?: string) => {
    const ruleText = `${ruleName || ''} ${condition || ''}`.toLowerCase();
    
    // Weather-specific icons based on condition content
    if (source === 'WEATHER' || ruleText.includes('weather')) {
      // Check for specific alert types first (more specific matches)
      if (ruleText.includes('cold') || ruleText.includes('freeze') || ruleText.includes('frost')) return '🥶';
      if (ruleText.includes('fire') || ruleText.includes('wildfire') || ruleText.includes('dry')) return '🔥';
      if (ruleText.includes('wind') || ruleText.includes('gust')) return '💨';
      if (ruleText.includes('rain') || ruleText.includes('storm') || ruleText.includes('precipitation')) return '🌧️';
      if (ruleText.includes('snow') || ruleText.includes('blizzard')) return '🌨️';
      if (ruleText.includes('flood') || ruleText.includes('water')) return '🌊';
      if (ruleText.includes('tornado') || ruleText.includes('cyclone')) return '🌪️';
      if (ruleText.includes('earthquake') || ruleText.includes('seismic')) return '🏗️';
      if (ruleText.includes('humidity') || ruleText.includes('moisture')) return '💧';
      // Check for heat/temperature last (less specific)
      if (ruleText.includes('heat') || ruleText.includes('hot') || (ruleText.includes('temp') && ruleText.includes('>'))) return '🌡️';
      return '🌤️'; // Default weather icon
    }
    
    // Status and system alerts
    if (source === 'STATUS') {
      if (ruleText.includes('error') || ruleText.includes('fail') || ruleText.includes('down')) return '🚨';
      if (ruleText.includes('warning') || ruleText.includes('alert')) return '⚠️';
      if (ruleText.includes('security') || ruleText.includes('breach')) return '🔒';
      if (ruleText.includes('performance') || ruleText.includes('slow')) return '📊';
      return '🔍'; // Default status icon
    }
    
    // Custom alerts
    if (source === 'CUSTOM') {
      if (ruleText.includes('emergency') || ruleText.includes('urgent')) return '🚨';
      if (ruleText.includes('info') || ruleText.includes('notification')) return 'ℹ️';
      return '⚙️'; // Default custom icon
    }
    
    return '📢'; // Default fallback
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">
      {rules.map((rule) => (
        <div key={rule.id} className="bg-white rounded-xl shadow-md hover:shadow-lg transition-all duration-200 border border-gray-200 overflow-hidden">
          {/* Card Header */}
          <div className="bg-gradient-to-r from-blue-500 to-purple-600 p-4 text-white">
            <div className="flex items-center justify-between mb-2">
              <span className="text-3xl">{getSourceIcon(rule.source, rule.name, rule.condition)}</span>
              <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium ${
                rule.enabled 
                  ? 'bg-green-500 text-white' 
                  : 'bg-red-500 text-white'
              }`}>
                {rule.enabled ? (
                  <><CheckCircle className="h-3 w-3 mr-1" /> Active</>
                ) : (
                  <><XCircle className="h-3 w-3 mr-1" /> Inactive</>
                )}
              </span>
            </div>
            <h3 className="text-lg font-bold text-white truncate" title={rule.name}>
              {rule.name}
            </h3>
            <p className="text-blue-100 text-sm mt-1">
              {rule.source} Alert Rule
            </p>
          </div>

          {/* Card Body */}
          <div className="p-4 space-y-3">
            {/* Condition */}
            <div className="bg-gray-50 rounded-lg p-3">
              <div className="flex items-center mb-1">
                <div className="h-2 w-2 bg-blue-500 rounded-full mr-2"></div>
                <span className="font-semibold text-gray-700 text-sm">Condition</span>
              </div>
              <code className="text-xs text-gray-600 bg-white px-2 py-1 rounded border">
                {rule.condition}
              </code>
            </div>

            {/* Details Grid */}
            <div className="space-y-2 text-sm">
              <div className="flex items-center justify-between">
                <span className="text-gray-500 flex items-center">
                  <Clock className="h-4 w-4 mr-1" />
                  Cooldown
                </span>
                <span className="font-medium text-gray-900">{rule.cooldownMinutes}m</span>
              </div>
              
              <div className="flex items-center justify-between">
                <span className="text-gray-500">Channels</span>
                <div className="flex space-x-1">
                  {rule.message?.channels?.map((channel, index) => (
                    <span key={index} className="bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded-full">
                      {channel}
                    </span>
                  )) || <span className="text-gray-400">None</span>}
                </div>
              </div>



              <div className="flex items-center justify-between pt-2 border-t border-gray-100">
                <span className="text-gray-500 text-xs">Last Fired</span>
                <span className="text-xs text-gray-600">{formatLastFired(rule.lastFiredAt)}</span>
              </div>
            </div>
          </div>

          {/* Card Actions */}
          <div className="bg-gray-50 px-4 py-3 flex items-center justify-between">
            <div className="flex space-x-1">
              <button
                onClick={() => {
                  console.log('Test button clicked for rule:', rule.name);
                  onTest(rule);
                }}
                className="p-2 text-blue-600 hover:bg-blue-100 rounded-lg transition-colors group"
                title="Test Rule"
              >
                <TestTube className="h-4 w-4 group-hover:scale-110 transition-transform" />
              </button>
              
              <button
                onClick={() => onToggle(rule)}
                className={`p-2 rounded-lg transition-colors group ${
                  rule.enabled
                    ? 'text-orange-600 hover:bg-orange-100'
                    : 'text-green-600 hover:bg-green-100'
                }`}
                title={rule.enabled ? 'Disable Rule' : 'Enable Rule'}
              >
                {rule.enabled ? 
                  <Pause className="h-4 w-4 group-hover:scale-110 transition-transform" /> : 
                  <Play className="h-4 w-4 group-hover:scale-110 transition-transform" />
                }
              </button>
            </div>

            <div className="flex space-x-1">
              <button
                onClick={() => onEdit(rule)}
                className="p-2 text-gray-600 hover:bg-gray-200 rounded-lg transition-colors group"
                title="Edit Rule"
              >
                <Edit className="h-4 w-4 group-hover:scale-110 transition-transform" />
              </button>
              
              <button
                onClick={() => onDelete(rule)}
                className="p-2 text-red-600 hover:bg-red-100 rounded-lg transition-colors group"
                title="Delete Rule"
              >
                <Trash2 className="h-4 w-4 group-hover:scale-110 transition-transform" />
              </button>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}