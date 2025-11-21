'use client';

import { useState, useEffect } from 'react';
import { Rule } from '../page';
import { X, Plus, Trash2 } from 'lucide-react';

type RuleFormProps = {
  rule: Rule | null;
  onSubmit: (rule: Rule) => void;
  onCancel: () => void;
};

export function RuleForm({ rule, onSubmit, onCancel }: RuleFormProps) {
  const [formData, setFormData] = useState<Rule>({
    name: '',
    source: 'WEATHER',
    params: {},
    condition: '',
    message: {
      header: '',
      content: '',
      channels: ['email']
    },
    audience: {
      tags: [],
      city: ''
    },
    cooldownMinutes: 60,
    enabled: true
  });

  const [newTag, setNewTag] = useState('');
  const [paramKey, setParamKey] = useState('');
  const [paramValue, setParamValue] = useState('');

  useEffect(() => {
    if (rule) {
      setFormData(rule);
    }
  }, [rule]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit(formData);
  };

  const addTag = () => {
    if (newTag.trim() && !formData.audience.tags.includes(newTag.trim())) {
      setFormData(prev => ({
        ...prev,
        audience: {
          ...prev.audience,
          tags: [...prev.audience.tags, newTag.trim()]
        }
      }));
      setNewTag('');
    }
  };

  const removeTag = (tag: string) => {
    setFormData(prev => ({
      ...prev,
      audience: {
        ...prev.audience,
        tags: prev.audience.tags.filter(t => t !== tag)
      }
    }));
  };

  const addParam = () => {
    if (paramKey.trim() && paramValue.trim()) {
      setFormData(prev => ({
        ...prev,
        params: {
          ...prev.params,
          [paramKey.trim()]: paramValue.trim()
        }
      }));
      setParamKey('');
      setParamValue('');
    }
  };

  const removeParam = (key: string) => {
    setFormData(prev => {
      const newParams = { ...prev.params };
      delete newParams[key];
      return { ...prev, params: newParams };
    });
  };

  const toggleChannel = (channel: string) => {
    setFormData(prev => ({
      ...prev,
      message: {
        ...prev.message,
        channels: prev.message.channels.includes(channel)
          ? prev.message.channels.filter(c => c !== channel)
          : [...prev.message.channels, channel]
      }
    }));
  };

  return (
    <div className="fixed inset-0 bg-gray-500 bg-opacity-75 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between p-6 border-b">
          <h2 className="text-xl font-semibold text-gray-900">
            {rule ? 'Edit Rule' : 'Create New Rule'}
          </h2>
          <button onClick={onCancel} className="text-gray-400 hover:text-gray-600">
            <X className="h-6 w-6" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-6">
          {/* Basic Info */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="label">Rule Name</label>
              <input
                type="text"
                className="input"
                value={formData.name}
                onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
                required
              />
            </div>
            <div>
              <label className="label">Source Type</label>
              <select
                className="input"
                value={formData.source}
                onChange={(e) => setFormData(prev => ({ ...prev, source: e.target.value as any }))}
              >
                <option value="WEATHER">Weather</option>
                <option value="STATUS">Status Check</option>
                <option value="CUSTOM">Custom</option>
              </select>
            </div>
          </div>

          {/* Source Parameters */}
          <div>
            <label className="label">Source Parameters</label>
            <div className="space-y-2">
              {Object.entries(formData.params).map(([key, value]) => (
                <div key={key} className="flex items-center space-x-2">
                  <input
                    type="text"
                    className="input flex-1"
                    value={key}
                    readOnly
                  />
                  <input
                    type="text"
                    className="input flex-1"
                    value={String(value)}
                    onChange={(e) => setFormData(prev => ({
                      ...prev,
                      params: { ...prev.params, [key]: e.target.value }
                    }))}
                  />
                  <button
                    type="button"
                    onClick={() => removeParam(key)}
                    className="p-2 text-red-600 hover:bg-red-50 rounded"
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>
              ))}
              <div className="flex items-center space-x-2">
                <input
                  type="text"
                  className="input flex-1"
                  placeholder="Parameter key"
                  value={paramKey}
                  onChange={(e) => setParamKey(e.target.value)}
                />
                <input
                  type="text"
                  className="input flex-1"
                  placeholder="Parameter value"
                  value={paramValue}
                  onChange={(e) => setParamValue(e.target.value)}
                />
                <button
                  type="button"
                  onClick={addParam}
                  className="p-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                >
                  <Plus className="h-4 w-4" />
                </button>
              </div>
            </div>
            {formData.source === 'WEATHER' && (
              <p className="text-sm text-gray-500 mt-1">
                Example: city = "San Francisco" or lat = "37.7749", lon = "-122.4194"
              </p>
            )}
            {formData.source === 'STATUS' && (
              <p className="text-sm text-gray-500 mt-1">
                Example: url = "https://example.com/health"
              </p>
            )}
          </div>

          {/* Condition */}
          <div>
            <label className="label">Condition</label>
            <input
              type="text"
              className="input"
              value={formData.condition}
              onChange={(e) => setFormData(prev => ({ ...prev, condition: e.target.value }))}
              placeholder="e.g., temp_c > 40 && humidity < 20"
              required
            />
            <p className="text-sm text-gray-500 mt-1">
              Use variables like temp_c, humidity, status. Operators: &gt;, &lt;, ==, !=, &amp;&amp;, ||
            </p>
          </div>

          {/* Message */}
          <div className="space-y-4">
            <h3 className="text-lg font-medium">Message Template</h3>
            <div>
              <label className="label">Header/Subject</label>
              <input
                type="text"
                className="input"
                value={formData.message.header}
                onChange={(e) => setFormData(prev => ({
                  ...prev,
                  message: { ...prev.message, header: e.target.value }
                }))}
                placeholder="e.g., Weather Alert for {{city}}"
                required
              />
            </div>
            <div>
              <label className="label">Content</label>
              <textarea
                className="input"
                rows={3}
                value={formData.message.content}
                onChange={(e) => setFormData(prev => ({
                  ...prev,
                  message: { ...prev.message, content: e.target.value }
                }))}
                placeholder="e.g., Temperature is {{temp_c}}°C in {{city}}"
                required
              />
              <p className="text-sm text-gray-500 mt-1">
                Use {'{'}variable{'}'} placeholders for dynamic content
              </p>
            </div>
            <div>
              <label className="label">Channels</label>
              <div className="flex space-x-4">
                {['email', 'sms'].map(channel => (
                  <label key={channel} className="flex items-center">
                    <input
                      type="checkbox"
                      checked={formData.message.channels.includes(channel)}
                      onChange={() => toggleChannel(channel)}
                      className="mr-2"
                    />
                    {channel.charAt(0).toUpperCase() + channel.slice(1)}
                  </label>
                ))}
              </div>
            </div>
          </div>

          {/* Audience */}
          <div className="space-y-4">
            <h3 className="text-lg font-medium">Audience Targeting</h3>
            <div>
              <label className="label">City</label>
              <input
                type="text"
                className="input"
                value={formData.audience.city}
                onChange={(e) => setFormData(prev => ({
                  ...prev,
                  audience: { ...prev.audience, city: e.target.value }
                }))}
                placeholder="e.g., San Francisco (leave empty for all cities)"
              />
            </div>
            <div>
              <label className="label">Tags</label>
              <div className="space-y-2">
                <div className="flex flex-wrap gap-2">
                  {formData.audience.tags.map(tag => (
                    <span key={tag} className="inline-flex items-center px-2 py-1 bg-blue-100 text-blue-800 rounded-full text-sm">
                      {tag}
                      <button
                        type="button"
                        onClick={() => removeTag(tag)}
                        className="ml-1 text-blue-600 hover:text-blue-800"
                      >
                        <X className="h-3 w-3" />
                      </button>
                    </span>
                  ))}
                </div>
                <div className="flex space-x-2">
                  <input
                    type="text"
                    className="input flex-1"
                    placeholder="Add tag"
                    value={newTag}
                    onChange={(e) => setNewTag(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), addTag())}
                  />
                  <button
                    type="button"
                    onClick={addTag}
                    className="px-3 py-2 bg-gray-600 text-white rounded hover:bg-gray-700"
                  >
                    Add
                  </button>
                </div>
              </div>
            </div>
          </div>

          {/* Settings */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="label">Cooldown (minutes)</label>
              <input
                type="number"
                className="input"
                min="1"
                value={formData.cooldownMinutes}
                onChange={(e) => setFormData(prev => ({ ...prev, cooldownMinutes: parseInt(e.target.value) || 60 }))}
              />
            </div>
            <div>
              <label className="flex items-center">
                <input
                  type="checkbox"
                  checked={formData.enabled}
                  onChange={(e) => setFormData(prev => ({ ...prev, enabled: e.target.checked }))}
                  className="mr-2"
                />
                Enable rule immediately
              </label>
            </div>
          </div>

          {/* Actions */}
          <div className="flex justify-end space-x-3 pt-6 border-t">
            <button
              type="button"
              onClick={onCancel}
              className="px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
            >
              {rule ? 'Update Rule' : 'Create Rule'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}